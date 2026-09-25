package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Fixtures.*;
import static com.voxivoid.recipelab.Params.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Which bytes ENTER writes, and that reading them back gives the recipe that was stored. */
class ParamsWritesTest {

    private static List<Write> writesFromFactory(String recipe, int baseQuality) {
        int[] cur = factoryRows();
        int[] edit = staged(Fixtures.recipe(recipe), cur, baseQuality);
        return writes(cur, edit, storedSub(factoryStore(), edit));
    }

    @Test void nothingToWriteWhenTheStagedValuesAreTheStoredOnes() {
        int[] cur = factoryRows();
        assertEquals(0, dirtyRows(cur, cur.clone(), 0));
        assertTrue(writes(cur, cur.clone(), 0).isEmpty());
        // the Factory recipe on a factory camera is the same thing
        assertTrue(writesFromFactory("FACTORY (ST)", Q_FINE).isEmpty());
    }

    @Test void portra400FromFactory() {
        assertEquals(Arrays.asList(
                w(ID_STYLE, Recipes.PORTRAIT),
                w(ID_SAT, -1), w(ID_CON, -1),
                w(ID_WB_AB, 3), w(ID_WB_AB_AWB, 3),
                w(ID_WB_GM, -1), w(ID_WB_GM_AWB, -1),   // G1 goes in magenta-positive
                w(ID_EV, 2), w(ID_EV2, 2)),            // exposure bias and its companion copy
                writesFromFactory("Kodak Portra 400", Q_FINE));
    }

    @Test void velviaWritesPictureProfileThreeForTheMatrix() {
        assertEquals(Arrays.asList(w(ID_STYLE, Recipes.VIVID), w(ID_SAT, 5), w(ID_CON, 2), w(ID_PP_NO, 3)),
                writesFromFactory("Velvia", Q_FINE));
    }

    @Test void eternaWritesBothDroBytes() {
        assertEquals(Arrays.asList(w(ID_STYLE, Recipes.NEUTRAL), w(ID_SAT, -4), w(ID_CON, -2), w(ID_SHARP, -1),
                w(ID_EV, -1), w(ID_EV2, -1), w(ID_DRO, 4), w(ID_DRO_LVL, 4)),
                writesFromFactory("Eterna", Q_FINE));
    }

    @Test void droOffIsZeroWithLevelOne() {
        int[] cur = factoryRows(), edit = cur.clone();
        edit[R_DRO] = Recipes.DRO_OFF;
        assertEquals(Arrays.asList(w(ID_DRO, 0), w(ID_DRO_LVL, 1)), writes(cur, edit, 0));
    }

    @Test void switchingToKelvinWritesTheColourTemperaturePairEvenWhenFineTuneIsUnchanged() {
        // Acros +R: mono, HC Mono effect, 2500K, no fine tune -- the K pair must still be written or the camera applies stale AWB values
        assertEquals(Arrays.asList(
                w(ID_STYLE, Recipes.MONO),
                w(ID_PE, Recipes.PE_HCMONO),
                w(ID_WB_MODE, WB_KELVIN), w(ID_WB_TEMP, 25),
                w(ID_WB_AB, 0), w(ID_WB_AB_K, 0),
                w(ID_WB_GM, 0), w(ID_WB_GM_K, 0)),
                writesFromFactory("Acros +R (red filter)", Q_FINE));
    }

    @Test void anEffectOverARawBaseAlsoSwitchesTheCameraToJpegFine() {
        List<Write> ws = writesFromFactory("Acros +R (red filter)", Q_RAW);
        // cur says JPEG Fine (factoryRows), the staged quality is JPEG Fine too -- no quality write
        assertFalse(ws.contains(w(ID_QFMT, 0)));

        int[] cur = factoryRows(); cur[R_QUAL] = Q_RAW;
        int[] edit = staged(Fixtures.recipe("Acros +R (red filter)"), cur, Q_RAW);
        assertEquals(Q_FINE, edit[R_QUAL]);
        ws = writes(cur, edit, 0);
        assertEquals(Arrays.asList(w(ID_QFMT, 0), w(ID_QFMT2, 0), w(ID_QJPG, 1), w(ID_QJPG2, 1)), ws.subList(ws.size() - 4, ws.size()),
                "the quality pair and its mirrors come last");
    }

    @Test void qualityCountsAsOneValueHoweverManyBytesItTakes() {
        int[] cur = factoryRows(), edit = cur.clone();
        edit[R_QUAL] = Q_RAWJPG;
        assertEquals(1, dirtyRows(cur, edit, 0));
        assertEquals(Arrays.asList(w(ID_QFMT, 2), w(ID_QFMT2, 2), w(ID_QJPG, 1), w(ID_QJPG2, 1)), writes(cur, edit, 0));
    }

    @Test void theSubParameterGoesToTheStagedEffectsSlot() {
        int[] cur = factoryRows();
        int[] edit = staged(Fixtures.recipe("Sony SH (soft high-key)"), cur, Q_FINE); edit[R_SUB] = 2;   // green tint; no recipe ships a non-zero sub today
        List<Write> ws = writes(cur, edit, 0);
        assertTrue(ws.contains(w(0x010709d8, 2)), "green tint of Soft High-key: " + ws);
        assertTrue(ws.contains(w(ID_PE, Recipes.PE_HIGHKEY)));
    }

    @Test void subIsOnlyDirtyAgainstTheByteInThatSlot() {
        int[] cur = factoryRows(), edit = cur.clone();
        edit[R_PE] = Recipes.PE_HIGHKEY; edit[R_SUB] = 0;
        assertFalse(rowDirty(R_SUB, cur, edit, 0), "blue tint already stored");
        assertTrue(rowDirty(R_SUB, cur, edit, 1), "pink stored, blue staged");
        edit[R_PE] = Recipes.PE_RETRO; edit[R_SUB] = 1;
        assertFalse(rowDirty(R_SUB, cur, edit, 0), "Retro has no sub-parameter, so SUB never needs writing");
        assertFalse(writes(cur, edit, 0).contains(w(0, 1)));
    }

    @Test void fineTuneIsRewrittenWhenTheWhiteBalanceModeChanges() {
        int[] cur = factoryRows(), edit = cur.clone();
        edit[R_WBMODE] = WB_KELVIN; edit[R_KELVIN] = 40;
        assertTrue(rowDirty(R_AB, cur, edit, 0));
        assertTrue(rowDirty(R_GM, cur, edit, 0));
        assertEquals(Arrays.asList(w(ID_WB_MODE, 14), w(ID_WB_TEMP, 40), w(ID_WB_AB, 0), w(ID_WB_AB_K, 0), w(ID_WB_GM, 0), w(ID_WB_GM_K, 0)), writes(cur, edit, 0));
    }

    @Test void theRecipeRowNeverWrites() {
        int[] cur = factoryRows(), edit = cur.clone();
        edit[R_RECIPE] = 5;
        assertFalse(rowDirty(R_RECIPE, cur, edit, 0));
        assertTrue(writes(cur, edit, 0).isEmpty());
    }

    @Test void stagingLeavesWhiteBalanceAloneWhenTheRecipeSaysSo() {
        int[] edit = factoryRows();
        edit[R_WBMODE] = WB_KELVIN; edit[R_KELVIN] = 32;
        Params.stage(new Recipes.Recipe(0, "wb as is", Recipes.VIVID, 1, 0, 0, 0, 0, 0, 0, 0), edit);
        assertEquals(WB_KELVIN, edit[R_WBMODE]);
        assertEquals(32, edit[R_KELVIN]);
        assertEquals(Recipes.VIVID, edit[R_STYLE]);
    }

    @Test void stagingAKelvinRecipeConvertsToHundreds() {
        int[] edit = factoryRows();
        Params.stage(Fixtures.recipe("Acros +Ye (yellow filter)"), edit);
        assertEquals(WB_KELVIN, edit[R_WBMODE]);
        assertEquals(40, edit[R_KELVIN]);
    }

    /** Store every recipe over a factory camera, read the store back: it must say what was staged. */
    @Test void everyRecipeReadsBackAsItWasStored() {
        for (int base : new int[] { Q_RAW, Q_RAWJPG, Q_FINE, Q_STD }) {
            for (Recipes.Recipe r : Recipes.ALL) {
                Map<Integer, Integer> store = factoryStore();
                int[] cur = load(store);
                assertArrayEquals(factoryRows(), cur, "the factory store decodes to the factory rows");
                cur[R_QUAL] = base;           // a camera set to this quality
                int[] edit = staged(r, cur, base);
                apply(store, writes(cur, edit, storedSub(store, edit)));
                store.put(ID_QFMT, Q_FMT_CODE[edit[R_QUAL]]); store.put(ID_QJPG, Q_JPG_CODE[edit[R_QUAL]]);   // when quality was not dirty the store already held it
                assertArrayEquals(edit, load(store), r.name + " over " + Q_LABEL[base] + ": what was stored is not what reads back");
            }
        }
    }

    /** Scroll through the whole table storing each recipe on top of the previous one, then back to factory. */
    @Test void storingRecipesOnTopOfEachOtherNeverLeavesAStaleByteBehind() {
        Map<Integer, Integer> store = factoryStore();
        int[] path = new int[Recipes.ALL.length + 1];
        for (int i = 0; i < Recipes.ALL.length; i++) path[i] = i;
        path[Recipes.ALL.length] = 0;
        for (int i : path) {
            int[] cur = load(store);
            int[] edit = staged(Recipes.ALL[i], cur, Q_FINE);
            apply(store, writes(cur, edit, storedSub(store, edit)));
            int[] back = load(store);
            assertArrayEquals(edit, back, Recipes.ALL[i].name + " after " + (i > 0 ? Recipes.ALL[i - 1].name : "factory"));
            // and a second ENTER has nothing left to do
            assertEquals(0, dirtyRows(back, edit, storedSub(store, edit)), Recipes.ALL[i].name + " still dirty after storing");
        }
        // back at the factory look -- except the colour temperature on the dial, which an AWB recipe leaves as the
        // last kelvin recipe set it (Classic Cinema, 6000K); the Factory recipe says "auto", not "5500K"
        int[] expected = factoryRows(); expected[R_KELVIN] = 60;
        assertArrayEquals(expected, load(store), "back at the factory look");
    }
}
