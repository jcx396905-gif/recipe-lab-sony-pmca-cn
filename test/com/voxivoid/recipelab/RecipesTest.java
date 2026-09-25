package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Fixtures.indexOf;
import static com.voxivoid.recipelab.Fixtures.recipe;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The recipe table and its helpers. */
class RecipesTest {

    @Test void hasTheDocumentedNumberOfRecipes() {
        assertEquals(77, Recipes.ALL.length,
                "README.md, CLAUDE.md, docs/DEVELOPMENT.md and docs/FAQ.md quote the recipe count -- update them together with this number");
    }

    @Test void namesArePresentAndUnique() {
        Set<String> seen = new HashSet<String>();
        for (Recipes.Recipe r : Recipes.ALL) {
            assertFalse(r.name.trim().isEmpty(), "unnamed recipe");
            assertTrue(seen.add(r.name), "duplicate recipe name: " + r.name);
        }
    }

    @Test void recipesAreListedInGroupOrderAndEveryBrandHasSome() {
        int last = 0;
        for (Recipes.Recipe r : Recipes.ALL) {
            assertTrue(r.group >= last, r.name + " is listed out of group order");
            assertTrue(r.group < Recipes.GROUPS.length, r.name + " names an unknown group");
            last = r.group;
        }
        int total = 0;
        for (int g = 0; g < Recipes.GROUPS.length; g++) {
            assertTrue(Recipes.GROUP_COUNT[g] > 0, Recipes.GROUPS[g] + " has no recipes");
            assertEquals(g, Recipes.ALL[Recipes.GROUP_START[g]].group, "GROUP_START of " + Recipes.GROUPS[g]);
            int end = g + 1 < Recipes.GROUPS.length ? Recipes.GROUP_START[g + 1] : Recipes.ALL.length;
            assertEquals(end, Recipes.GROUP_START[g] + Recipes.GROUP_COUNT[g], Recipes.GROUPS[g] + " is not contiguous");
            total += Recipes.GROUP_COUNT[g];
        }
        assertEquals(Recipes.ALL.length, total);
    }

    @Test void theFirstRecipeIsTheFactoryLook() {
        Recipes.Recipe f = Recipes.ALL[0];   // TRASH stages recipe 0 as "factory"
        assertTrue(f.name.startsWith("FACTORY"), f.name);
        assertEquals(Recipes.STD, f.style);
        assertEquals(0, f.sat); assertEquals(0, f.con); assertEquals(0, f.sharp); assertEquals(0, f.matrix);
        assertEquals(Params.WB_AUTO, f.wbMode); assertEquals(0, f.ab); assertEquals(0, f.gm);
        assertEquals(0, f.pe); assertEquals(0, f.ev); assertEquals(Recipes.DRO_AUTO, f.dro); assertEquals(0, f.sub);
        assertFalse(f.isEffect());
    }

    @Test void everyValueFitsItsRow() {
        for (Recipes.Recipe r : Recipes.ALL) {
            String n = r.name;
            assertRange(n, "style", r.style, Params.R_STYLE);
            assertRange(n, "sat", r.sat, Params.R_SAT);
            assertRange(n, "con", r.con, Params.R_CON);
            assertRange(n, "sharp", r.sharp, Params.R_SHARP);
            assertRange(n, "matrix", r.matrix, Params.R_MTX);
            assertRange(n, "pe", r.pe, Params.R_PE);
            assertRange(n, "ev", r.ev, Params.R_EV);
            assertRange(n, "dro", r.dro, Params.R_DRO);
            assertRange(n, "ab", r.ab, Params.R_AB);
            assertRange(n, "gm", r.gm, Params.R_GM);
            assertTrue(r.wbMode == 0 || r.wbMode == Params.WB_AUTO || r.wbMode == Params.WB_KELVIN, n + ": wbMode " + r.wbMode);
            if (r.wbMode == Params.WB_KELVIN) {
                assertEquals(0, r.kelvin % 100, n + ": kelvin is stored in hundreds, " + r.kelvin + " would be truncated");
                assertRange(n, "kelvin/100", r.kelvin / 100, Params.R_KELVIN);
            } else assertEquals(0, r.kelvin, n + ": kelvin set without colour-temperature WB");
            String[] sub = Recipes.subValues(r.pe);
            if (sub == null) assertEquals(0, r.sub, n + ": sub set on an effect without one");
            else assertTrue(r.sub >= 0 && r.sub < sub.length, n + ": sub " + r.sub + " outside " + sub.length + " values");
        }
    }

    private static void assertRange(String recipe, String what, int v, int row) {
        assertTrue(v >= Params.ROW_MIN[row] && v <= Params.ROW_MAX[row],
                recipe + ": " + what + " = " + v + " outside " + Params.ROW_MIN[row] + ".." + Params.ROW_MAX[row]);
    }

    @Test void styleTablesLineUpWithTheStoredEnum() {
        assertEquals(15, Recipes.STYLE_NAMES.length);
        assertEquals(Recipes.STYLE_NAMES.length, Recipes.STYLE_LABEL.length);
        assertEquals(Recipes.STYLE_NAMES.length - 1, Params.ROW_MAX[Params.R_STYLE]);
        assertEquals("standard", Recipes.STYLE_NAMES[Recipes.STD]);
        assertEquals("vivid", Recipes.STYLE_NAMES[Recipes.VIVID]);
        assertEquals("neutral", Recipes.STYLE_NAMES[Recipes.NEUTRAL]);
        assertEquals("mono", Recipes.STYLE_NAMES[Recipes.MONO]);
        assertEquals("red-leaves", Recipes.STYLE_NAMES[Recipes.AUTUMN]);
        assertEquals(14, Recipes.SEPIA, "sepia is 0x0e in the store, measured by menu diff");
        assertEquals("sepia", Recipes.STYLE_NAMES[Recipes.SEPIA]);
        assertEquals("B&W", Recipes.STYLE_LABEL[Recipes.MONO]);
    }

    @Test void unidentifiedStyleValuesAreMarkedUnknown() {
        assertNull(Recipes.STYLE_NAMES[13], "13 sits between Autumn and Sepia and has never been identified");
        assertFalse(Recipes.styleKnown(13));
        assertFalse(Recipes.styleKnown(0));
        assertFalse(Recipes.styleKnown(Recipes.STYLE_NAMES.length));
        assertTrue(Recipes.styleKnown(Recipes.SEPIA));
        assertEquals("?13", Recipes.styleLabel(13));
        for (Recipes.Recipe r : Recipes.ALL) assertTrue(Recipes.styleKnown(r.style), r.name + " uses an unidentified style");
    }

    @Test void effectTablesLineUpWithTheStoredIndex() {
        assertEquals(14, Recipes.PE_KEYS.length);
        assertEquals(Recipes.PE_KEYS.length, Recipes.PE_LABEL.length);
        assertEquals(Recipes.PE_KEYS.length - 1, Params.ROW_MAX[Params.R_PE]);
        assertEquals("off", Recipes.PE_KEYS[Recipes.PE_OFF]);
        assertEquals("toy-camera", Recipes.PE_KEYS[Recipes.PE_TOY]);
        assertEquals("pop-color", Recipes.PE_KEYS[Recipes.PE_POP]);
        assertEquals("retro-photo", Recipes.PE_KEYS[Recipes.PE_RETRO]);
        assertEquals("soft-high-key", Recipes.PE_KEYS[Recipes.PE_HIGHKEY]);
        assertEquals("rough-mono", Recipes.PE_KEYS[Recipes.PE_HCMONO]);
    }

    @Test void effectSubParametersAreConsistent() {
        int longest = 0;
        for (int pe = 0; pe < Recipes.PE_KEYS.length; pe++) {
            String key = Recipes.subKey(pe); int id = Recipes.subId(pe); String[] values = Recipes.subValues(pe);
            if (key == null) {
                assertEquals(0, id, "effect " + pe + " has a sub slot but no key");
                assertNull(values, "effect " + pe + " has sub values but no key");
                assertNull(Recipes.subLabel(pe, 0));
            } else {
                assertTrue(id != 0, "effect " + pe + " has a sub key but no slot");
                assertNotNull(values);
                for (int i = 0; i < values.length; i++) assertEquals(values[i].replace("posterization-", ""), Recipes.subLabel(pe, i));
                assertEquals("?" + values.length, Recipes.subLabel(pe, values.length));
                assertEquals("?-1", Recipes.subLabel(pe, -1));
                longest = Math.max(longest, values.length);
            }
        }
        assertEquals(longest - 1, Params.ROW_MAX[Params.R_SUB], "the SUB row must reach the longest value list");
        assertEquals(0x010709d8, Recipes.subId(Recipes.PE_HIGHKEY));
        assertEquals(0x010706f3, Recipes.subId(Recipes.PE_TOY));
        assertEquals(0x010706ee, Recipes.subId(6));
        assertEquals(0x010706ef, Recipes.subId(3));
        assertEquals("bw", Recipes.subLabel(3, 1));
        assertEquals("color", Recipes.subLabel(3, 0));
    }

    @Test void evLabelIsThirdsWithASign() {
        assertEquals("0", Recipes.evLabel(0));
        assertEquals("+0.3", Recipes.evLabel(1));
        assertEquals("+0.7", Recipes.evLabel(2));
        assertEquals("+1.0", Recipes.evLabel(3));
        assertEquals("-0.3", Recipes.evLabel(-1));
        assertEquals("-1.3", Recipes.evLabel(-4));
        assertEquals("-1.7", Recipes.evLabel(-5));
        assertEquals("+5.0", Recipes.evLabel(15));
        assertEquals("-5.0", Recipes.evLabel(-15));
    }

    @Test void droLabel() {
        assertEquals("off", Recipes.droLabel(Recipes.DRO_OFF));
        assertEquals("auto", Recipes.droLabel(Recipes.DRO_AUTO));
        assertEquals("Lv1", Recipes.droLabel(1));
        assertEquals("Lv5", Recipes.droLabel(5));
    }

    @Test void styleAndEffectLabelsFallBackToTheRawValue() {
        assertEquals("Neutral", Recipes.styleLabel(Recipes.NEUTRAL));
        assertEquals("Sepia", Recipes.styleLabel(Recipes.SEPIA));
        assertEquals("?0", Recipes.styleLabel(0));
        assertEquals("?15", Recipes.styleLabel(15));
        assertEquals("off", Recipes.peLabel(0));
        assertEquals("Retro", Recipes.peLabel(Recipes.PE_RETRO));
        assertEquals("?14", Recipes.peLabel(14));
        assertEquals("?-1", Recipes.peLabel(-1));
    }

    @Test void isEffectMeansAPictureEffectIsOn() {
        assertFalse(recipe("Velvia").isEffect());
        assertTrue(recipe("GR Retro").isEffect());
        assertTrue(recipe("Sony SH (soft high-key)").isEffect());
    }

    @Test void summaryReadsLikeTheBrowserLine() {
        assertEquals("Standard  0/0", Recipes.ALL[0].summary());
        assertEquals("Neutral  -4/-1  A1", recipe("Sony FL (film-like)").summary());
        assertEquals("Vivid  +5/+2  MTX", recipe("Velvia").summary());
        assertEquals("B&W  0/+1  4000K", recipe("Acros +Ye (yellow filter)").summary());
        assertEquals("B&W  0/+1  5600K  G4", recipe("Acros +G (green filter)").summary());
        assertEquals("Standard  -2/-1  5500K  B1  M1", recipe("Cinestill 50D (Blue Velvet)").summary());
        assertEquals("Neutral  -4/-2  -0.3  DRO Lv3", recipe("Eterna").summary());
        assertEquals("Neutral  -2/-2  DRO Lv5", recipe("Rec709 Video (flat-ish)").summary());
        assertEquals("Portrait  -1/-1  +0.7  A3  G1", recipe("Kodak Portra 400").summary());
        assertEquals("Neutral  -1/0  +0.3  DRO Lv3  3200K", recipe("Kodak Vision3 500T (daylight)").summary());
    }

    @Test void summaryOfAnEffectNamesTheEffectAndItsSubParameter() {
        assertEquals("High-key blue  +1.0  A1", recipe("Sony SH (soft high-key)").summary());
        assertEquals("Retro  A3  M1", recipe("GR Retro").summary());
        assertEquals("HC mono  2500K", recipe("Acros +R (red filter)").summary());
        // a matrix is meaningless under an effect and stays out of the line
        Recipes.Recipe r = new Recipes.Recipe(0, "x", Recipes.STD, 0, 0, 0, 1, Params.WB_AUTO, 0, 0, 0, Recipes.PE_POP, 0, Recipes.DRO_AUTO);
        assertEquals("Pop", r.summary());
    }

    @Test void nextWrapsOverTheWholeTable() {
        int last = Recipes.ALL.length - 1;
        assertEquals(1, Recipes.next(0, +1));
        assertEquals(0, Recipes.next(last, +1));
        assertEquals(last, Recipes.next(0, -1));
    }

    @Test void nextGroupStartLandsOnTheFirstRecipeOfTheNeighbouringBrand() {
        int sony = 0, lastBrand = Recipes.GROUPS.length - 1;
        assertEquals(Recipes.GROUP_START[1], Recipes.nextGroupStart(Recipes.GROUP_START[sony] + 3, +1));
        assertEquals(Recipes.GROUP_START[lastBrand], Recipes.nextGroupStart(sony, -1));
        assertEquals(0, Recipes.nextGroupStart(Recipes.GROUP_START[lastBrand] + 1, +1));
    }

    @Test void nextInGroupWrapsInsideTheBrand() {
        int start = Recipes.GROUP_START[0], n = Recipes.GROUP_COUNT[0];
        assertEquals(start + 1, Recipes.nextInGroup(start, +1));
        assertEquals(start, Recipes.nextInGroup(start + n - 1, +1));
        assertEquals(start + n - 1, Recipes.nextInGroup(start, -1));
        int solo = indexOf("Hasselblad HNCS Natural");
        assertEquals(1, Recipes.GROUP_COUNT[Recipes.ALL[solo].group]);
        assertEquals(solo, Recipes.nextInGroup(solo, +1));
        assertEquals(solo, Recipes.nextInGroup(solo, -1));
    }
}
