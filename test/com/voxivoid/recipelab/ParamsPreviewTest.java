package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Fixtures.*;
import static com.voxivoid.recipelab.Params.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The Camera.Parameters the live preview sets for a staged set of rows. */
class ParamsPreviewTest {

    private static Map<String, String> previewOf(String recipe, int baseQuality) {
        return preview(staged(Fixtures.recipe(recipe), factoryRows(), baseQuality));
    }

    @Test void factoryLook() {
        Map<String, String> p = previewOf("FACTORY (ST)", Q_FINE);
        assertEquals("standard", p.get("color-mode"));
        assertEquals("0", p.get("saturation"));
        assertEquals("0", p.get("contrast"));
        assertEquals("0", p.get("sharpness"));
        assertEquals("false", p.get("rgb-matrix-mode"));
        assertFalse(p.containsKey("rgb-matrix"), "the matrix is only sent when it is on");
        assertEquals("auto", p.get("whitebalance"));
        assertFalse(p.containsKey("color-temperture-white-balance"));
        assertEquals("0", p.get("light-balance-for-white-balance"));
        assertEquals("0", p.get("color-compensation-for-white-balance"));
        assertEquals("jpeg", p.get("storage-fmt"));
        assertEquals("50", p.get("jpeg-quality"));
        assertEquals("off", p.get("picture-effect"));
        assertEquals("0", p.get("exposure-compensation"));
        assertEquals("auto", p.get("dro-mode"));
        assertFalse(p.containsKey("dro-level"));
        assertEquals(13, p.size(), "no stray keys: " + p.keySet());
    }

    @Test void keysAreSetInAStableOrder() {
        Map<String, String> p = previewOf("FACTORY (ST)", Q_FINE);
        assertEquals("color-mode", new ArrayList<String>(p.keySet()).get(0));
        assertEquals("dro-mode", new ArrayList<String>(p.keySet()).get(p.size() - 1));
    }

    @Test void creativeStyleAndAdjustments() {
        Map<String, String> p = previewOf("Sony FL (film-like)", Q_FINE);
        assertEquals("neutral", p.get("color-mode"));
        assertEquals("-4", p.get("saturation"));
        assertEquals("-1", p.get("contrast"));
        assertEquals("1", p.get("light-balance-for-white-balance"));
    }

    @Test void contrastAndSharpnessAreClampedToTheMenuRangeButSaturationIsNot() {
        int[] e = factoryRows();
        e[R_SAT] = -9; e[R_CON] = 8; e[R_SHARP] = -8;
        Map<String, String> p = preview(e);
        assertEquals("-9", p.get("saturation"), "the core accepts saturation to ±16");
        assertEquals("3", p.get("contrast"));
        assertEquals("-3", p.get("sharpness"));
    }

    @Test void theMatrixIsThePp3MeasurementInQ10() {
        Map<String, String> p = previewOf("Velvia", Q_FINE);
        assertEquals("true", p.get("rgb-matrix-mode"));
        assertEquals("1331,-307,-51,-205,1331,-123,-20,-461,1485", p.get("rgb-matrix"));
    }

    @Test void colourTemperatureWhiteBalance() {
        Map<String, String> p = previewOf("Acros +Ye (yellow filter)", Q_FINE);
        assertEquals("color-temp", p.get("whitebalance"));
        assertEquals("4000", p.get("color-temperture-white-balance"));
        assertEquals("mono", p.get("color-mode"));
    }

    @Test void greenMagentaIsSentMagentaPositive() {
        Map<String, String> p = previewOf("Acros +G (green filter)", Q_FINE);
        assertEquals("-4", p.get("color-compensation-for-white-balance"));
        p = previewOf("Cinestill 50D (Blue Velvet)", Q_FINE);
        assertEquals("-1", p.get("light-balance-for-white-balance"), "B1 is amber -1");
    }

    @Test void anUnknownWhiteBalanceModeLeavesTheCamerasOwn() {
        int[] e = factoryRows(); e[R_WBMODE] = 0;
        assertFalse(preview(e).containsKey("whitebalance"));
    }

    @Test void pictureEffectWithItsSubParameter() {
        Map<String, String> p = previewOf("Sony SH (soft high-key)", Q_FINE);
        assertEquals("soft-high-key", p.get("picture-effect"));
        assertEquals("blue", p.get("pe-soft-high-key-effect"));
        assertEquals("3", p.get("exposure-compensation"));
        int[] e = staged(recipe("Sony SH (soft high-key)"), factoryRows(), Q_FINE); e[R_SUB] = 2;   // no recipe uses a non-zero sub today
        assertEquals("green", preview(e).get("pe-soft-high-key-effect"));
        p = previewOf("GR Retro", Q_FINE);
        assertEquals("retro-photo", p.get("picture-effect"));
        assertFalse(p.containsKey("pe-soft-high-key-effect"));
        assertEquals("standard", p.get("color-mode"), "the style is still sent; the camera ignores it under an effect");
    }

    @Test void anOutOfRangeSubParameterIsNotSent() {
        int[] e = factoryRows(); e[R_PE] = Recipes.PE_HIGHKEY; e[R_SUB] = 3;
        assertFalse(preview(e).containsKey("pe-soft-high-key-effect"));
    }

    @Test void anUnknownStyleFallsBackToStandard() {
        int[] e = factoryRows(); e[R_STYLE] = 0;
        assertEquals("standard", preview(e).get("color-mode"));
        e[R_STYLE] = 13;                                        // in range, but no runtime name is known for it
        assertEquals("standard", preview(e).get("color-mode"));
        e[R_STYLE] = 15;
        assertEquals("standard", preview(e).get("color-mode"));
    }

    @Test void sepiaIsSentByName() {
        int[] e = factoryRows(); e[R_STYLE] = Recipes.SEPIA;
        assertEquals("sepia", preview(e).get("color-mode"));
    }

    @Test void droLevelsAndOff() {
        Map<String, String> p = previewOf("Eterna", Q_FINE);
        assertEquals("on", p.get("dro-mode"));
        assertEquals("3", p.get("dro-level"));
        int[] e = factoryRows(); e[R_DRO] = Recipes.DRO_OFF;
        p = preview(e);
        assertEquals("off", p.get("dro-mode"));
        assertFalse(p.containsKey("dro-level"));
    }

    @Test void qualityDrivesFormatAndJpegLevel() {
        int[] e = factoryRows();
        e[R_QUAL] = Q_RAW;    assertEquals("raw", preview(e).get("storage-fmt"));
        e[R_QUAL] = Q_RAWJPG; assertEquals("rawjpeg", preview(e).get("storage-fmt"));
        e[R_QUAL] = Q_STD;    assertEquals("jpeg", preview(e).get("storage-fmt")); assertEquals("25", preview(e).get("jpeg-quality"));
        e[R_QUAL] = Q_FINE;   assertEquals("50", preview(e).get("jpeg-quality"));
    }

    @Test void everyRecipeProducesACompleteParameterSet() {
        for (Recipes.Recipe r : Recipes.ALL) {
            Map<String, String> p = preview(staged(r, factoryRows(), Q_FINE));
            for (String k : new String[] { "color-mode", "saturation", "contrast", "sharpness", "rgb-matrix-mode", "whitebalance",
                    "light-balance-for-white-balance", "color-compensation-for-white-balance", "storage-fmt", "jpeg-quality",
                    "picture-effect", "exposure-compensation", "dro-mode" })
                assertNotNull(p.get(k), r.name + " sets no " + k);
        }
    }

    @Test void effectRecipesPreviewJpegEvenOnARawCamera() {
        for (Recipes.Recipe r : Recipes.ALL) {
            int[] cur = factoryRows(); cur[R_QUAL] = Q_RAW;
            String fmt = preview(staged(r, cur, Q_RAW)).get("storage-fmt");
            if (r.isEffect()) assertEquals("jpeg", fmt, r.name + " would preview an effect in RAW, which the camera ignores");
            else assertEquals("raw", fmt, r.name + " should keep the camera's RAW");
        }
    }
}
