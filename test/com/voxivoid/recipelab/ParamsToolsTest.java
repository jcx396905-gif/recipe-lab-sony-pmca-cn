package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Params.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The C1 snapshot / diff tool: the id list it walks and the lines it prints. */
class ParamsToolsTest {

    private static List<int[]> parse(String text) throws IOException {
        List<int[]> ids = new ArrayList<int[]>();
        parseIds(new BufferedReader(new StringReader(text)), ids);
        return ids;
    }

    @Test void parsesHexIdAndDecimalSizeAndSkipsOtherLines() throws IOException {
        List<int[]> ids = parse("00020000 4\n\n  01070175 1  \n# comment line\nffff0000 16\nbroken\n");
        assertEquals(3, ids.size());
        assertArrayEquals(new int[] { 0x00020000, 4 }, ids.get(0));
        assertArrayEquals(new int[] { 0x01070175, 1 }, ids.get(1));
        assertArrayEquals(new int[] { 0xffff0000, 16 }, ids.get(2), "ids above 0x7fffffff wrap into an int like the JNI side expects");
    }

    @Test void aBadLineLeavesWhatWasParsedBeforeIt() {
        List<int[]> ids = new ArrayList<int[]>();
        assertThrows(NumberFormatException.class, () -> parseIds(new BufferedReader(new StringReader("00020000 4\nzz 1\n00020001 4\n")), ids));
        assertEquals(1, ids.size());
    }

    @Test void theShippedIdListIsWellFormedAndCoversEverySlotTheAppWrites() throws IOException {
        File f = new File("res/raw/ids.txt");
        assumeTrue(f.isFile(), "run from the repository root to check res/raw/ids.txt");
        List<int[]> ids = new ArrayList<int[]>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        try { parseIds(br, ids); } finally { br.close(); }
        assertTrue(ids.size() > 10000, "expected the full store, got " + ids.size());
        Set<Integer> seen = new HashSet<Integer>();
        for (int[] e : ids) {
            assertTrue(e[1] >= 1 && e[1] <= 16, String.format("%08x has size %d; the tool only snapshots entries of 16 bytes or less", e[0], e[1]));
            assertTrue(seen.add(e[0]), String.format("%08x is listed twice", e[0]));
        }
        int[] written = { ID_STYLE, ID_CON, ID_SAT, ID_SHARP, ID_PP_NO, ID_WB_MODE, ID_WB_TEMP, ID_WB_AB, ID_WB_GM, ID_WB_AB_AWB, ID_WB_GM_AWB,
                ID_WB_AB_K, ID_WB_GM_K, ID_PE, ID_EV, ID_EV2, ID_DRO, ID_DRO_LVL, ID_QFMT, ID_QJPG, ID_QFMT2, ID_QJPG2,
                Recipes.subId(Recipes.PE_HIGHKEY), Recipes.subId(Recipes.PE_TOY), Recipes.subId(6), Recipes.subId(3) };
        for (int id : written) assertTrue(seen.contains(id), String.format("slot %08x is written by the app but missing from ids.txt", id));
    }

    @Test void hexShowsAtMostFourBytes() {
        assertEquals("", hex(new byte[0]));
        assertEquals("01", hex(new byte[] { 1 }));
        assertEquals("ff7f", hex(new byte[] { (byte) 0xff, 0x7f }));
        assertEquals("00010203", hex(new byte[] { 0, 1, 2, 3, 4, 5 }));
    }

    @Test void diffEntryIsIdColonOldGreaterThanNew() {
        assertEquals("01070175:01>02  ", diffEntry(ID_STYLE, new byte[] { 1 }, new byte[] { 2 }));
        assertEquals("ffff0000:>0a  ", diffEntry(0xffff0000, new byte[0], new byte[] { 10 }));
    }
}
