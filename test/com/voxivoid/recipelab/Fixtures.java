package com.voxivoid.recipelab;

import static com.voxivoid.recipelab.Params.*;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Shared test data: a factory-fresh camera as rows and as store bytes, and a fake store to write into. */
final class Fixtures {
    private Fixtures() {}

    static Recipes.Recipe recipe(String name) { return Recipes.ALL[indexOf(name)]; }

    static int indexOf(String name) {
        for (int i = 0; i < Recipes.ALL.length; i++) if (Recipes.ALL[i].name.equals(name)) return i;
        fail("no recipe named " + name);
        return -1;
    }

    /** the rows as the app reads them from a factory-fresh camera: Standard, AWB, 5500K on the dial, DRO auto, JPEG Fine */
    static int[] factoryRows() {
        int[] r = new int[N];
        r[R_STYLE] = Recipes.STD; r[R_WBMODE] = WB_AUTO; r[R_KELVIN] = 55; r[R_DRO] = Recipes.DRO_AUTO; r[R_QUAL] = Q_FINE;
        return r;
    }

    /** the same camera's settings store, byte by byte (values 0..255 as the store holds them) */
    static Map<Integer, Integer> factoryStore() {
        Map<Integer, Integer> s = new HashMap<Integer, Integer>();
        s.put(ID_STYLE, 1); s.put(ID_CON, 0); s.put(ID_SAT, 0); s.put(ID_SHARP, 0); s.put(ID_PP_NO, 0);
        s.put(ID_WB_MODE, WB_AUTO); s.put(ID_WB_TEMP, 55);
        s.put(ID_WB_AB, 0); s.put(ID_WB_GM, 0); s.put(ID_WB_AB_AWB, 0); s.put(ID_WB_GM_AWB, 0); s.put(ID_WB_AB_K, 0); s.put(ID_WB_GM_K, 0);
        s.put(ID_PE, 0); s.put(ID_EV, 0); s.put(ID_EV2, 0);
        s.put(ID_DRO, 1); s.put(ID_DRO_LVL, 1);
        s.put(ID_QFMT, 0); s.put(ID_QJPG, 1); s.put(ID_QFMT2, 0); s.put(ID_QJPG2, 1);
        return s;
    }

    /** what MainActivity.stageRecipe() leaves in edit: the recipe over the current rows, quality from the Factory base */
    static int[] staged(Recipes.Recipe r, int[] cur, int baseQuality) {
        int[] e = cur.clone();
        Params.stage(r, e);
        e[R_QUAL] = Params.recipeQuality(r, baseQuality);
        return e;
    }

    /** a byte as NativeBackup.readByte returns it: sign-extended; 0 for a slot the store has never seen */
    static int byteAt(Map<Integer, Integer> store, int id) {
        Integer v = store.get(id);
        return v == null ? 0 : (byte) (int) v;
    }

    /** what MainActivity.load() reads from this store -- keep in step with that method */
    static int[] load(Map<Integer, Integer> store) {
        int[] cur = new int[N];
        for (int i = 1; i < N; i++) {
            int id = ROW_ID[i];
            if (id == SUB_SLOT) { int sid = Recipes.subId(cur[R_PE]); cur[i] = sid == 0 ? 0 : byteAt(store, sid) & 0xff; continue; }
            if (id == QUALITY_SLOTS) { cur[i] = Params.qualityFromStore(byteAt(store, ID_QFMT) & 0xff, byteAt(store, ID_QJPG) & 0xff); continue; }
            cur[i] = Params.fromStore(id, byteAt(store, id));
        }
        return cur;
    }

    /** the SUB byte MainActivity.storedSub() would read for the staged effect */
    static int storedSub(Map<Integer, Integer> store, int[] edit) {
        int sid = Recipes.subId(edit[R_PE]);
        return sid == 0 ? edit[R_SUB] : byteAt(store, sid) & 0xff;
    }

    /** performs the writes the way NativeBackup.writeByte does: one byte each */
    static void apply(Map<Integer, Integer> store, List<Write> writes) {
        for (Write w : writes) store.put(w.id, w.value & 0xff);
    }

    static Write w(int id, int value) { return new Write(id, value); }
}
