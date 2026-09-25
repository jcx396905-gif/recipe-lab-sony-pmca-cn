package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Fixtures.*;
import static com.voxivoid.recipelab.Params.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** The chip strip: which chips show, how a value steps, where LEFT/RIGHT and UP/DOWN land, what a chip says. */
class ParamsChipsTest {

    @Test void creativeStyleChipsHideUnderAnEffect() {
        int[] e = factoryRows();
        for (int r : new int[] { R_STYLE, R_SAT, R_CON, R_SHARP, R_MTX }) assertTrue(rowVisible(r, e), ROW_NAME[r]);
        assertFalse(rowVisible(R_SUB, e));
        e[R_PE] = Recipes.PE_RETRO;
        for (int r : new int[] { R_STYLE, R_SAT, R_CON, R_SHARP, R_MTX }) assertFalse(rowVisible(r, e), ROW_NAME[r]);
        assertFalse(rowVisible(R_SUB, e), "Retro has no sub-parameter");
        e[R_PE] = Recipes.PE_TOY;
        assertTrue(rowVisible(R_SUB, e));
        for (int r : new int[] { R_PE, R_WBMODE, R_AB, R_GM, R_EV, R_DRO, R_QUAL }) assertTrue(rowVisible(r, e), ROW_NAME[r]);
    }

    @Test void kelvinChipOnlyInColourTemperatureMode() {
        int[] e = factoryRows();
        assertFalse(rowVisible(R_KELVIN, e));
        e[R_WBMODE] = WB_KELVIN;
        assertTrue(rowVisible(R_KELVIN, e));
    }

    @Test void choicesWrapNumbersClamp() {
        for (int r : new int[] { R_STYLE, R_MTX, R_PE, R_SUB, R_QUAL, R_DRO, R_WBMODE }) assertTrue(isChoice(r), ROW_NAME[r]);
        for (int r : new int[] { R_SAT, R_CON, R_SHARP, R_KELVIN, R_AB, R_GM, R_EV }) assertFalse(isChoice(r), ROW_NAME[r]);

        int[] e = factoryRows();
        e[R_STYLE] = Recipes.SEPIA; step(e, R_STYLE, +1, Q_FINE); assertEquals(1, e[R_STYLE], "past Sepia comes Standard");
        step(e, R_STYLE, -1, Q_FINE); assertEquals(Recipes.SEPIA, e[R_STYLE]);
        e[R_STYLE] = Recipes.AUTUMN; step(e, R_STYLE, +1, Q_FINE); assertEquals(Recipes.SEPIA, e[R_STYLE], "13 is unidentified and skipped");
        step(e, R_STYLE, -1, Q_FINE); assertEquals(Recipes.AUTUMN, e[R_STYLE], "and skipped backwards too");
        e[R_DRO] = Recipes.DRO_AUTO; step(e, R_DRO, +1, Q_FINE); assertEquals(Recipes.DRO_OFF, e[R_DRO]);
        e[R_QUAL] = Q_STD; step(e, R_QUAL, +1, Q_FINE); assertEquals(Q_RAW, e[R_QUAL]);

        e[R_SAT] = 16; step(e, R_SAT, +1, Q_FINE); assertEquals(16, e[R_SAT], "saturation stops at +16");
        e[R_SAT] = -16; step(e, R_SAT, -1, Q_FINE); assertEquals(-16, e[R_SAT]);
        e[R_EV] = 0; step(e, R_EV, -1, Q_FINE); assertEquals(-1, e[R_EV]);
        e[R_KELVIN] = 99; step(e, R_KELVIN, +1, Q_FINE); assertEquals(99, e[R_KELVIN]);
        e[R_KELVIN] = 25; step(e, R_KELVIN, -1, Q_FINE); assertEquals(25, e[R_KELVIN]);
        e[R_AB] = 7; step(e, R_AB, +1, Q_FINE); assertEquals(7, e[R_AB]);
    }

    @Test void whiteBalanceTogglesBetweenAutoAndKelvin() {
        int[] e = factoryRows();
        assertFalse(step(e, R_WBMODE, +1, Q_FINE)); assertEquals(WB_KELVIN, e[R_WBMODE]);
        step(e, R_WBMODE, +1, Q_FINE); assertEquals(WB_AUTO, e[R_WBMODE]);
        step(e, R_WBMODE, -1, Q_FINE); assertEquals(WB_KELVIN, e[R_WBMODE]);
        e[R_WBMODE] = 5; step(e, R_WBMODE, +1, Q_FINE); assertEquals(WB_KELVIN, e[R_WBMODE], "any other stored mode goes to kelvin first");
    }

    @Test void subCyclesTheStagedEffectsValues() {
        int[] e = factoryRows(); e[R_PE] = Recipes.PE_HIGHKEY;   // blue, pink, green
        step(e, R_SUB, +1, Q_FINE); assertEquals(1, e[R_SUB]);
        step(e, R_SUB, +1, Q_FINE); assertEquals(2, e[R_SUB]);
        step(e, R_SUB, +1, Q_FINE); assertEquals(0, e[R_SUB]);
        step(e, R_SUB, -1, Q_FINE); assertEquals(2, e[R_SUB]);
        e[R_PE] = Recipes.PE_RETRO; e[R_SUB] = 0;
        step(e, R_SUB, +1, Q_FINE); assertEquals(0, e[R_SUB], "no values to cycle");
    }

    @Test void changingTheEffectResetsSubAndRederivesQuality() {
        int[] e = factoryRows(); e[R_PE] = Recipes.PE_HIGHKEY; e[R_SUB] = 2; e[R_QUAL] = Q_RAW;
        assertFalse(step(e, R_PE, -1, Q_RAW));
        assertEquals(Recipes.PE_RETRO, e[R_PE]);
        assertEquals(0, e[R_SUB]);
        assertEquals(Q_FINE, e[R_QUAL], "an effect cannot shoot RAW");

        e[R_PE] = Recipes.PE_TOY; e[R_QUAL] = Q_FINE;
        step(e, R_PE, -1, Q_RAW);
        assertEquals(Recipes.PE_OFF, e[R_PE]);
        assertEquals(Q_RAW, e[R_QUAL], "back to the recipe's quality once the effect is off");

        e[R_PE] = Recipes.PE_OFF; step(e, R_PE, +1, Q_STD);
        assertEquals(Recipes.PE_TOY, e[R_PE]);
        assertEquals(Q_STD, e[R_QUAL], "a JPEG base is kept as is");
    }

    @Test void onlyAQualityStepReportsAChoice() {
        int[] e = factoryRows();
        assertTrue(step(e, R_QUAL, +1, Q_FINE));
        assertFalse(step(e, R_SAT, +1, Q_FINE));
        assertFalse(step(e, R_PE, +1, Q_FINE));
        assertFalse(step(e, R_SUB, +1, Q_FINE));
    }

    @Test void leftRightSkipHiddenChipsAndWrap() {
        int[] e = factoryRows();
        assertEquals(R_STYLE, nextChip(R_QUAL, +1, e));
        assertEquals(R_DRO, nextChip(R_QUAL, -1, e));
        assertEquals(R_QUAL, nextChip(R_DRO, +1, e));
        assertEquals(R_AB, nextChip(R_WBMODE, +1, e), "kelvin hidden in AWB");
        e[R_WBMODE] = WB_KELVIN;
        assertEquals(R_KELVIN, nextChip(R_WBMODE, +1, e));
        e[R_PE] = Recipes.PE_HIGHKEY;
        assertEquals(R_PE, nextChip(R_QUAL, +1, e), "style chips hidden under an effect");
        assertEquals(R_SUB, nextChip(R_PE, +1, e));
        assertEquals(R_QUAL, nextChip(R_PE, -1, e));
        e[R_PE] = Recipes.PE_RETRO;
        assertEquals(R_WBMODE, nextChip(R_PE, +1, e), "no SUB for Retro");
    }

    @Test void fromTheRecipeLineRightGoesToTheSecondChipLikeTheOriginal() {
        // row 0 is not in ORDER; it counts as position 0 (QUALITY), so RIGHT from it lands after QUALITY
        int[] e = factoryRows();
        assertEquals(R_STYLE, nextChip(R_RECIPE, +1, e));
        assertEquals(R_DRO, nextChip(R_RECIPE, -1, e));
    }

    @Test void downFromTheRecipeLineReturnsToTheLastChipIfStillVisible() {
        int[] e = factoryRows();
        assertEquals(R_QUAL, enterChips(R_RECIPE, e), "first time: the first chip");
        assertEquals(R_SAT, enterChips(R_SAT, e));
        e[R_PE] = Recipes.PE_TOY;
        assertEquals(R_QUAL, enterChips(R_SAT, e), "SAT hidden now, so the first visible chip");
        assertEquals(R_KELVIN == enterChips(R_KELVIN, e) ? R_KELVIN : R_QUAL, enterChips(R_KELVIN, e));
        e[R_WBMODE] = WB_KELVIN;
        assertEquals(R_KELVIN, enterChips(R_KELVIN, e));
    }

    @Test void chipTextPerRow() {
        int[] e = factoryRows();
        assertEquals("Standard", fmt(R_STYLE, Recipes.STD, e));
        assertEquals("?0", fmt(R_STYLE, 0, e));
        assertEquals("+3", fmt(R_SAT, 3, e));
        assertEquals("-2", fmt(R_CON, -2, e));
        assertEquals("0", fmt(R_SHARP, 0, e));
        assertEquals("off", fmt(R_MTX, 0, e));
        assertEquals("PP3", fmt(R_MTX, 1, e));
        assertEquals("auto", fmt(R_WBMODE, WB_AUTO, e));
        assertEquals("kelvin", fmt(R_WBMODE, WB_KELVIN, e));
        assertEquals("3", fmt(R_WBMODE, 3, e));
        assertEquals("-", fmt(R_KELVIN, 55, e), "meaningless in AWB");
        e[R_WBMODE] = WB_KELVIN;
        assertEquals("5500K", fmt(R_KELVIN, 55, e));
        assertEquals("0", fmt(R_AB, 0, e)); assertEquals("A2", fmt(R_AB, 2, e)); assertEquals("B3", fmt(R_AB, -3, e));
        assertEquals("0", fmt(R_GM, 0, e)); assertEquals("G4", fmt(R_GM, 4, e)); assertEquals("M1", fmt(R_GM, -1, e));
        assertEquals("off", fmt(R_PE, 0, e)); assertEquals("Retro", fmt(R_PE, Recipes.PE_RETRO, e)); assertEquals("?14", fmt(R_PE, 14, e));
        assertEquals("-", fmt(R_SUB, 0, e), "no sub for the staged effect");
        e[R_PE] = Recipes.PE_HIGHKEY;
        assertEquals("pink", fmt(R_SUB, 1, e));
        assertEquals("?7", fmt(R_SUB, 7, e));
        assertEquals("+0.7", fmt(R_EV, 2, e));
        assertEquals("Lv2", fmt(R_DRO, 2, e));
        assertEquals("RAW+JPG", fmt(R_QUAL, Q_RAWJPG, e));
        assertEquals("?4", fmt(R_QUAL, 4, e));
    }
}
