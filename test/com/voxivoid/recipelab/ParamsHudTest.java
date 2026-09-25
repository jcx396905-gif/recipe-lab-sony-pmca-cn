package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Fixtures.*;
import static com.voxivoid.recipelab.Params.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** The overlay text: the meta line under the recipe name, the minimal pill, the quality prompt. */
class ParamsHudTest {

    @Test void metaLineForACreativeStyleRecipe() {
        int[] cur = factoryRows();
        assertEquals("Standard  ·  WB auto", metaLine(cur, cur.clone(), null));
        int[] e = staged(recipe("Kodak Portra 400"), cur, Q_FINE);
        assertEquals("Portrait  ·  WB auto  ·  EV +0.7", metaLine(cur, e, null));
        e = staged(recipe("Velvia"), cur, Q_FINE);
        assertEquals("Vivid  ·  WB auto  ·  PP3 matrix", metaLine(cur, e, null));
        e = staged(recipe("Kodak Vision3 500T (daylight)"), cur, Q_FINE);
        assertEquals("Neutral  ·  WB 3200K  ·  EV +0.3  ·  DRO Lv3", metaLine(cur, e, null));
    }

    @Test void metaLineForAnEffectRecipe() {
        int[] cur = factoryRows();
        int[] e = staged(recipe("Sony SH (soft high-key)"), cur, Q_FINE);
        assertEquals("Picture Effect High-key blue (Creative Style ignored, JPEG only)  ·  WB auto  ·  EV +1.0", metaLine(cur, e, null));
        e = staged(recipe("Acros +R (red filter)"), cur, Q_FINE);
        assertEquals("Picture Effect HC mono (Creative Style ignored, JPEG only)  ·  WB 2500K", metaLine(cur, e, null));
    }

    @Test void metaLineAnnouncesAQualityChangeAndRawUnderAnEffect() {
        int[] cur = factoryRows(); cur[R_QUAL] = Q_RAW;
        int[] e = staged(recipe("GR Retro"), cur, Q_RAW);
        assertEquals("Picture Effect Retro (Creative Style ignored, JPEG only)  ·  WB auto  ·  QUALITY → JPG Fine (now RAW)", metaLine(cur, e, null));
        e[R_QUAL] = Q_RAW;   // the user forced RAW back on
        assertEquals("Picture Effect Retro (Creative Style ignored, JPEG only)  ·  WB auto  ·  RAW is on: effect ignored", metaLine(cur, e, null));
    }

    @Test void metaLineShowsAnUnknownWhiteBalanceModeAndThePreviewError() {
        int[] cur = factoryRows(); int[] e = cur.clone(); e[R_WBMODE] = 3;
        assertEquals("Standard  ·  WB mode 3  ·  no live preview: CameraEx not found", metaLine(cur, e, "CameraEx not found"));
    }

    @Test void miniPill() {
        int[] cur = factoryRows();
        int i = indexOf("Kodak Portra 400");
        int[] e = staged(Recipes.ALL[i], cur, Q_FINE);
        assertEquals("CS  Kodak Portra 400   " + (i + 1) + " / 77   · preview", miniLine(i, cur, e, true));
        assertEquals("CS  Kodak Portra 400   " + (i + 1) + " / 77   · active", miniLine(i, cur, e, false));
        i = indexOf("GR Retro"); cur[R_QUAL] = Q_RAW;
        e = staged(Recipes.ALL[i], cur, Q_RAW);
        assertEquals("PE  GR Retro   " + (i + 1) + " / 77   · preview   · quality → JPG Fine", miniLine(i, cur, e, true));
    }

    @Test void qualityPromptExplainsWhyTheQualityMoves() {
        int[] cur = factoryRows(); cur[R_QUAL] = Q_RAW;
        int[] e = staged(recipe("GR Retro"), cur, Q_RAW);
        assertArrayEquals(new String[] { "Quality: RAW  →  JPG Fine", "JPEG is needed to apply this recipe." }, qualityPrompt(cur, e));
        e = cur.clone(); e[R_QUAL] = Q_STD;
        assertArrayEquals(new String[] { "Quality: RAW  →  JPG Std", "Creative Style recipes use the Factory recipe's quality." }, qualityPrompt(cur, e));
    }
}
