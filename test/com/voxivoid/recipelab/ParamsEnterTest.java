package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Params.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** The centre button: what a short press does, and where a hold marks a favourite instead. */
class ParamsEnterTest {

    @Test void theChipStripOnlyTakesTheKeysUnderTheFullPanel() {
        assertTrue(onRecipeLine(OV_FULL, R_RECIPE));
        assertFalse(onRecipeLine(OV_FULL, R_SAT), "the full panel with a chip highlighted");
        assertTrue(onRecipeLine(OV_PILL, R_SAT), "no chips under the pill, so the keys stay on the recipe");
        assertTrue(onRecipeLine(OV_HIDDEN, R_SAT), "nor with the overlay hidden");
    }

    @Test void aShortPressPicksTheRecipeFromTheRecipeLine() {
        assertEquals(ENTER_PICK, enterAction(OV_FULL, R_RECIPE, COL_RECIPES));
        assertEquals(ENTER_PICK, enterAction(OV_PILL, R_SAT, COL_RECIPES), "the pill has no chip to focus");
        assertEquals(ENTER_PICK, enterAction(OV_HIDDEN, R_SAT, COL_RECIPES));
    }

    @Test void aShortPressFocusesAChipFromTheChipStrip() {
        assertEquals(ENTER_FOCUS, enterAction(OV_FULL, R_SAT, COL_RECIPES));
        assertEquals(ENTER_FOCUS, enterAction(OV_FULL, R_QUAL, COL_GROUPS), "the browser column is meaningless here");
    }

    @Test void aShortPressInTheBrowserStepsIntoTheRecipeColumnThenPicks() {
        assertEquals(ENTER_BROWSER_COLUMN, enterAction(OV_BROWSER, R_RECIPE, COL_GROUPS));
        assertEquals(ENTER_BROWSER_PICK, enterAction(OV_BROWSER, R_RECIPE, COL_RECIPES));
        assertEquals(ENTER_BROWSER_COLUMN, enterAction(OV_BROWSER, R_SAT, COL_GROUPS), "the chip row is not reachable in the browser");
    }

    @Test void aHoldMarksAFavouriteWhereverARecipeIsWhatTheScreenIsAbout() {
        assertTrue(holdMarksFavourite(OV_FULL, R_RECIPE, COL_RECIPES));
        assertTrue(holdMarksFavourite(OV_PILL, R_SAT, COL_RECIPES), "the pill names a recipe");
        assertTrue(holdMarksFavourite(OV_HIDDEN, R_SAT, COL_RECIPES));
        assertTrue(holdMarksFavourite(OV_BROWSER, R_RECIPE, COL_RECIPES));
    }

    @Test void aHoldDoesNothingOnAChipOrOnTheGroupColumn() {
        assertFalse(holdMarksFavourite(OV_FULL, R_SAT, COL_RECIPES), "the highlight is a value, not a recipe");
        assertFalse(holdMarksFavourite(OV_BROWSER, R_RECIPE, COL_GROUPS), "the highlight is a group");
    }

    @Test void aHoldNeverStealsTheKeyFromFocusingAChip() {
        // the two must agree: wherever a hold marks, the short press is a pick; wherever it focuses, a hold is inert
        for (int overlay : new int[] { OV_FULL, OV_PILL, OV_HIDDEN, OV_BROWSER }) {
            for (int row : new int[] { R_RECIPE, R_SAT, R_QUAL }) {
                for (int col : new int[] { COL_GROUPS, COL_RECIPES }) {
                    int action = enterAction(overlay, row, col);
                    boolean marks = holdMarksFavourite(overlay, row, col);
                    String at = "overlay " + overlay + " row " + row + " col " + col;
                    if (action == ENTER_FOCUS || action == ENTER_BROWSER_COLUMN) assertFalse(marks, at);
                    else assertTrue(marks, at);
                }
            }
        }
    }
}
