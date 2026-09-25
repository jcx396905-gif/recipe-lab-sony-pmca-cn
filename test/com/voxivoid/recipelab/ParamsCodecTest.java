package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Params.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** How a row value is encoded in the settings store and decoded back. */
class ParamsCodecTest {

    @Test void slotOfARowFollowsTheTable() {
        assertEquals(ID_STYLE, slot(R_STYLE, 0));
        assertEquals(ID_DRO, slot(R_DRO, Recipes.PE_TOY));
        assertEquals(NO_SLOT, slot(R_RECIPE, 0));
        assertEquals(QUALITY_SLOTS, slot(R_QUAL, 0));
    }

    @Test void subSlotFollowsTheStagedEffect() {
        assertEquals(0x010709d8, slot(R_SUB, Recipes.PE_HIGHKEY));
        assertEquals(0x010706f3, slot(R_SUB, Recipes.PE_TOY));
        assertEquals(0, slot(R_SUB, Recipes.PE_OFF));
        assertEquals(0, slot(R_SUB, Recipes.PE_RETRO));
    }

    @Test void onlyIndexSlotsAreUnsigned() {
        assertTrue(unsignedSlot(ID_STYLE)); assertTrue(unsignedSlot(ID_PE)); assertTrue(unsignedSlot(ID_WB_MODE)); assertTrue(unsignedSlot(ID_WB_TEMP));
        assertFalse(unsignedSlot(ID_SAT)); assertFalse(unsignedSlot(ID_CON)); assertFalse(unsignedSlot(ID_SHARP));
        assertFalse(unsignedSlot(ID_WB_AB)); assertFalse(unsignedSlot(ID_WB_GM)); assertFalse(unsignedSlot(ID_EV));
    }

    @Test void droStoreBytesAreOffAutoThenLevelsPlusOne() {
        assertEquals(0, droFromStore(0));
        assertEquals(Recipes.DRO_AUTO, droFromStore(1));
        assertEquals(1, droFromStore(2));
        assertEquals(5, droFromStore(6));
        assertEquals(5, droFromStore(9), "anything past Lv5 clamps");

        assertEquals(0, droMainToStore(0)); assertEquals(1, droLevelToStore(0));
        assertEquals(1, droMainToStore(Recipes.DRO_AUTO)); assertEquals(1, droLevelToStore(Recipes.DRO_AUTO));
        assertEquals(2, droMainToStore(1)); assertEquals(2, droLevelToStore(1));
        assertEquals(6, droMainToStore(5)); assertEquals(6, droLevelToStore(5));
        for (int dro = 0; dro <= Recipes.DRO_AUTO; dro++) assertEquals(dro, droFromStore(droMainToStore(dro)), "DRO " + dro + " round trip");
    }

    @Test void matrixIsPictureProfileThree() {
        assertEquals(0, matrixToStore(0));
        assertEquals(3, matrixToStore(1));
        assertEquals(0, matrixFromStore(0));
        assertEquals(1, matrixFromStore(3));
        assertEquals(1, matrixFromStore(7), "any profile reads as the alternate matrix");
    }

    @Test void greenMagentaIsStoredMagentaPositive() {
        assertEquals(-4, gmToStore(4));
        assertEquals(1, gmFromStore(-1), "menu G1 is 0xff in the store");
        for (int gm = -7; gm <= 7; gm++) assertEquals(gm, gmFromStore(gmToStore(gm)));
    }

    @Test void fineTuneCopiesFollowTheWhiteBalanceMode() {
        assertEquals(ID_WB_AB_AWB, abSlot(WB_AUTO)); assertEquals(ID_WB_GM_AWB, gmSlot(WB_AUTO));
        assertEquals(ID_WB_AB_K, abSlot(WB_KELVIN)); assertEquals(ID_WB_GM_K, gmSlot(WB_KELVIN));
        assertEquals(ID_WB_AB_AWB, abSlot(0), "an unknown mode falls back to the AWB pair");
    }

    @Test void fromStoreReadsSignedOffsetsAndUnsignedIndexes() {
        assertEquals(-16, fromStore(ID_SAT, 0xF0), "0..255 form");
        assertEquals(-16, fromStore(ID_SAT, -16), "sign-extended form");
        assertEquals(3, fromStore(ID_CON, 3));
        assertEquals(-2, fromStore(ID_EV, 0xFE));
        assertEquals(Recipes.MONO, fromStore(ID_STYLE, 6));
        assertEquals(13, fromStore(ID_PE, 13));
        assertEquals(200, fromStore(ID_WB_TEMP, 200), "kelvin/100 goes past 127 on paper, so it is unsigned");
        assertEquals(200, fromStore(ID_WB_TEMP, (byte) 200));
        assertEquals(WB_KELVIN, fromStore(ID_WB_MODE, 14));
    }

    @Test void fromStoreAppliesTheRowCodecs() {
        assertEquals(1, fromStore(ID_PP_NO, 3));
        assertEquals(0, fromStore(ID_PP_NO, 0));
        assertEquals(1, fromStore(ID_WB_GM, 0xFF), "0xff = G1");
        assertEquals(-3, fromStore(ID_WB_GM, 3), "magenta 3 = G-M -3");
        assertEquals(3, fromStore(ID_DRO, 4));
        assertEquals(Recipes.DRO_AUTO, fromStore(ID_DRO, 1));
        assertEquals(2, fromStore(ID_WB_AB, 2), "A-B is stored as is");
    }

    @Test void qualityFromTheStoredPair() {
        assertEquals(Q_RAW, qualityFromStore(1, 0));
        assertEquals(Q_RAW, qualityFromStore(1, 1));
        assertEquals(Q_RAWJPG, qualityFromStore(2, 1));
        assertEquals(Q_STD, qualityFromStore(0, 0));
        assertEquals(Q_FINE, qualityFromStore(0, 1));
        assertEquals(-1, qualityFromStore(3, 1), "an unknown format defers to the runtime");
        assertEquals(-1, qualityFromStore(0xFF, 0));
    }

    @Test void qualityFromTheRuntimeParameters() {
        assertEquals(Q_RAW, qualityFromRuntime("raw", "50"));
        assertEquals(Q_RAWJPG, qualityFromRuntime("rawjpeg", "50"));
        assertEquals(Q_FINE, qualityFromRuntime("jpeg", "50"));
        assertEquals(Q_STD, qualityFromRuntime("jpeg", "25"));
        assertEquals(Q_FINE, qualityFromRuntime(null, null), "no answer means JPEG Fine");
    }

    @Test void qualityCodesMatchTheVerifiedMenuDiff() {
        assertArrayEquals(new int[] { 1, 2, 0, 0 }, Q_FMT_CODE);
        assertArrayEquals(new int[] { 1, 1, 1, 0 }, Q_JPG_CODE);
        for (int q = 0; q < Q_LABEL.length; q++) assertEquals(q, qualityFromStore(Q_FMT_CODE[q], Q_JPG_CODE[q]), Q_LABEL[q] + " round trip");
        for (int q = 0; q < Q_LABEL.length; q++) assertEquals(q, qualityFromRuntime(Q_FMT[q], Q_JPG[q]), Q_LABEL[q] + " runtime round trip");
    }

    @Test void effectRecipesForceJpegOverARawBase() {
        Recipes.Recipe cs = Fixtures.recipe("Velvia"), pe = Fixtures.recipe("GR Retro");
        for (int base = 0; base < 4; base++) assertEquals(base, recipeQuality(cs, base), "a Creative Style recipe keeps the Factory quality");
        assertEquals(Q_FINE, recipeQuality(pe, Q_RAW));
        assertEquals(Q_FINE, recipeQuality(pe, Q_RAWJPG));
        assertEquals(Q_FINE, recipeQuality(pe, Q_FINE));
        assertEquals(Q_STD, recipeQuality(pe, Q_STD));
    }

    @Test void onlyAFreeChoiceRedefinesTheFactoryQuality() {
        Recipes.Recipe cs = Fixtures.recipe("Velvia"), pe = Fixtures.recipe("GR Retro");
        for (int q = 0; q < 4; q++) assertTrue(redefinesBaseQuality(cs, q));
        assertFalse(redefinesBaseQuality(pe, Q_RAW), "RAW under an effect is the camera's, not a choice");
        assertFalse(redefinesBaseQuality(pe, Q_RAWJPG));
        assertTrue(redefinesBaseQuality(pe, Q_FINE));
        assertTrue(redefinesBaseQuality(pe, Q_STD));
    }
}
