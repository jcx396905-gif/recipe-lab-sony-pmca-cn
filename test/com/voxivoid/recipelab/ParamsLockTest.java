package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Fixtures.*;
import static com.voxivoid.recipelab.Params.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The read-only slots of the settings store: which attribute means the camera may refuse a write, what the app
 * checks before it writes, and what it says when it finds one (issue #19 — the PROTECTED badge probed one
 * unrelated read-only slot and answered for every recipe).
 */
class ParamsLockTest {

    private static List<Integer> ids(int... v) {
        List<Integer> l = new ArrayList<Integer>();
        for (int i : v) l.add(i);
        return l;
    }

    // ---- the attribute
    @Test void onlyTheReadOnlyBitLocksASlot() {
        assertFalse(slotLocked(0), "an ordinary setting: writable whatever backup protection says");
        assertTrue(slotLocked(ATTR_READ_ONLY));
        assertTrue(slotLocked(ATTR_READ_ONLY | 0x12), "other attribute bits do not clear it");
        assertFalse(slotLocked(0x12), "a slot with other attributes but not this one is not locked");
    }

    // ---- the slots that are checked
    @Test void everySlotAnyRecipeCanWriteIsInTheCheckedList() {
        Set<Integer> checked = new HashSet<Integer>(allSlots());
        int[] cur = factoryRows();
        for (Recipes.Recipe r : Recipes.ALL) {
            for (int q = Q_RAW; q <= Q_STD; q++) {
                int[] edit = staged(r, cur, q);
                for (Write w : writes(cur, edit, 0))
                    assertTrue(checked.contains(w.id), String.format("%08x is written by %s but never checked", w.id, r.name));
            }
        }
        // the colour-temperature fine-tune pair is only written from a Kelvin recipe, the AWB pair only from an auto one
        assertTrue(checked.containsAll(ids(ID_WB_AB_K, ID_WB_GM_K, ID_WB_AB_AWB, ID_WB_GM_AWB)));
    }

    @Test void theCheckedListHasNoDuplicatesAndNoRowMarkers() {
        List<Integer> all = allSlots();
        assertEquals(new HashSet<Integer>(all).size(), all.size(), "a slot is checked once");
        for (int id : all) assertTrue(id > 0, String.format("%d is a ROW_ID marker, not a slot", id));
        assertEquals(26, all.size(), "12 row slots, 6 copies, the quality pair with its mirrors, and the four effect sub-slots");
    }

    // ---- the check that runs before a write
    @Test void onlyTheFlaggedSlotsOfThePendingWriteAreLocked() {
        List<Write> ws = Arrays.asList(w(ID_STYLE, 2), w(ID_SAT, 5), w(ID_EV, 2));
        assertEquals(ids(), lockedFrom(ws, new int[] { 0, 0, 0 }), "an unflagged body locks nothing");
        assertEquals(ids(ID_SAT), lockedFrom(ws, new int[] { 0, ATTR_READ_ONLY, 0 }));
        assertEquals(ids(ID_STYLE, ID_EV), lockedFrom(ws, new int[] { ATTR_READ_ONLY, 0, ATTR_READ_ONLY | 4 }),
                "other attribute bits alongside the read-only one still lock the slot");
    }

    @Test void aSlotTheCameraWillNotAnswerForCountsAsWritable() {
        // the write path names the slot that refuses it, so a failed probe must not stop a recipe that would go in
        List<Write> ws = Arrays.asList(w(ID_STYLE, 2), w(ID_SAT, 5));
        assertEquals(ids(), lockedFrom(ws, new int[] { -1, -1 }));
        assertEquals(ids(ID_SAT), lockedFrom(ws, new int[] { -1, ATTR_READ_ONLY }), "one unreadable slot does not hide a flagged one");
    }

    // ---- naming a slot
    @Test void aSlotIsNamedAfterTheRowThatOwnsIt() {
        assertEquals("STYLE", slotName(ID_STYLE));
        assertEquals("EV", slotName(ID_EV));
        assertEquals("EV", slotName(ID_EV2), "the companion copy belongs to the same row");
        assertEquals("QUALITY", slotName(ID_QJPG2));
        assertEquals("A-B", slotName(ID_WB_AB_K));
        assertEquals("G-M", slotName(ID_WB_GM_AWB));
        assertEquals("DRO", slotName(ID_DRO_LVL));
        assertEquals("MATRIX", slotName(ID_PP_NO));
        assertEquals("WB", slotName(ID_WB_MODE));
        assertEquals("KELVIN", slotName(ID_WB_TEMP));
        assertEquals("EFFECT", slotName(ID_PE));
        assertEquals("SUB High-key", slotName(Recipes.subId(Recipes.PE_HIGHKEY)));
        assertEquals("00e70000", slotName(0x00e70000), "a slot no row owns is named by its id");
        assertEquals("00000000", slotName(0), "NO_SLOT owns nothing — least of all the effect that has no sub-slot");
    }

    // ---- what the user is told
    @Test void theLockedMessageNamesTheSettingsAndTheWayOut() {
        String one = lockedMessage(ids(ID_STYLE));
        assertTrue(one.startsWith("Not written — the camera holds this setting read-only: STYLE."), one);
        assertTrue(one.contains("OpenMemories-Tweak"), "the message carries the only fix there is");

        String many = lockedMessage(ids(ID_EV, ID_EV2, ID_STYLE));
        assertTrue(many.contains("these settings read-only: EV, STYLE"), many);
        assertFalse(many.contains("EV, EV"), "a row whose copies are both locked is named once: " + many);
    }

    @Test void theWriteFailureNamesTheSlotAndHowFarItGot() {
        assertEquals("WRITE FAILED on STYLE (01070175): Protection enabled — nothing was written",
                writeFailedMessage(ID_STYLE, "Protection enabled", 0));
        assertEquals("WRITE FAILED on G-M (0107067e): Backup_write failed — 1 byte written before it stopped",
                writeFailedMessage(ID_WB_GM_AWB, "Backup_write failed", 1));
        assertTrue(writeFailedMessage(ID_EV, "boom", 4).endsWith("4 bytes written before it stopped"));
    }

    // ---- the developer menu's report
    @Test void aBodyWithNothingFlaggedReportsNothingReadOnly() {
        List<Integer> ids = allSlots();
        int[] attrs = new int[ids.size()];
        assertEquals("26 recipe slots checked  ·  none read-only", lockReport(ids, attrs));
    }

    @Test void theReportNamesTheLockedRowsAndCountsTheSlotsThatWouldNotAnswer() {
        List<Integer> ids = ids(ID_STYLE, ID_EV, ID_EV2, ID_SAT);
        int[] attrs = { ATTR_READ_ONLY, ATTR_READ_ONLY, ATTR_READ_ONLY, -1 };
        assertEquals("4 recipe slots checked  ·  3 read-only: STYLE, EV  ·  1 would not answer", lockReport(ids, attrs));
    }

    @Test void theReportFileHasOneLinePerSlot() {
        String lines = lockLines(ids(ID_STYLE, ID_SAT, ID_EV), new int[] { ATTR_READ_ONLY, 0, -1 });
        assertEquals(Arrays.asList(
                "01070175 STYLE attr=1 READ_ONLY",
                "01070187 SAT attr=0",
                "010700b8 EV attr=?"),
                Arrays.asList(lines.split("\n")));
    }
}
