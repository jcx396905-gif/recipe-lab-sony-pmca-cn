package com.voxivoid.recipelab;

/**
 * The developer menu behind C1, and the sample run it can start: the rows the menu has, the settle delay the run
 * waits between applying a recipe and firing the shutter, the progress lines it shows, and the manifest it writes
 * so the frames can be matched to recipes afterwards.
 *
 * The run itself is a timed loop in MainActivity (stage a recipe → wait → shutter → wait → next); everything it
 * decides without the camera is here. No android.* import may appear in this class (tools/test.sh).
 */
final class DevTools {
    private DevTools() {}

    static final String TITLE = "开发工具";

    /** menu rows, in display order */
    static final int ROW_SNAPSHOT = 0, ROW_LOCKS = 1, ROW_SAMPLES = 2, ROW_SETTLE = 3, ROWS = 4;

    /**
     * Settle delays to pick from, in ms: how long the preview pipeline gets after a recipe is applied before the
     * shutter fires. The right one is a property of the camera, not of this table — a frame that still carries the
     * previous look means the delay is too short, which is why the row exists instead of a constant.
     */
    static final int[] SETTLE_MS = { 800, 1200, 2000, 3000, 5000 };
    /** the delay a fresh install starts on */
    static final int SETTLE_DEFAULT = 1;
    /** what the run gives a capture before it stages the next recipe, in ms (the shutter key's press → release, automated) */
    static final int SHUTTER_MS = 2500;

    /** the frame list the run writes into the app's files dir; the frames themselves are named by the camera */
    static final String MANIFEST = "samples.txt";
    /** field separator of a manifest line; no recipe name contains it (RecipesTest) */
    static final String SEP = "|";

    // ------------------------------------------------------------ the menu
    /** the row above / below, wrapping */
    static int nextRow(int row, int dir) { return (row + ROWS + dir) % ROWS; }

    /** a row's title; the snapshot row and the delay row say what they will do next */
    static String rowLabel(int row, boolean snapshotTaken, int settle) {
        switch (row) {
            case ROW_SNAPSHOT: return snapshotTaken ? "设置对比" : "设置快照";
            case ROW_LOCKS: return "只读检查 —— " + Params.allSlots().size() + " 个插槽";
            case ROW_SAMPLES: return "拍摄样片 —— " + Recipes.ALL.length + " 个配方";
            case ROW_SETTLE: return "稳定延迟 —— " + settleLabel(settle);
            default: return "?" + row;
        }
    }

    /** the line under a row's title */
    static String rowDetail(int row, boolean snapshotTaken) {
        switch (row) {
            case ROW_SNAPSHOT: return snapshotTaken ? "将每个设置 ID 与快照逐一对比" : "记录每个设置 ID 的当前值";
            case ROW_LOCKS: return "检测配方写入的每个插槽是否只读";
            case ROW_SAMPLES: return "每个配方拍一张 JPEG，按表序进行 —— MENU 停止";
            case ROW_SETTLE: return "应用配方后、快门释放前的等待时间";
            default: return "";
        }
    }

    // ------------------------------------------------------------ the settle delay
    /** a stored delay index brought back into the table */
    static int clampSettle(int idx) { return idx >= 0 && idx < SETTLE_MS.length ? idx : SETTLE_DEFAULT; }

    /** the next / previous delay, wrapping */
    static int nextSettle(int idx, int dir) { return (clampSettle(idx) + SETTLE_MS.length + dir) % SETTLE_MS.length; }

    /** a delay as the menu shows it: "1.2 s" (built by hand — String.format would follow the camera's locale) */
    static String settleLabel(int idx) {
        int ms = SETTLE_MS[clampSettle(idx)];
        return (ms / 1000) + "." + (ms % 1000) / 100 + " s";
    }

    // ------------------------------------------------------------ the sample run
    /** the run needs the live camera: without it nothing is applied and nothing can be shot */
    static final String NO_PREVIEW = "无实时预览 —— 样片拍摄需要相机";

    /** the sticky line while the run walks the table; frames count from 1 */
    static String progress(int frame, int total, String recipeName) {
        return "拍摄 " + frame + " / " + total + "  ·  " + recipeName + "   ——   MENU 停止";
    }

    /** the run reached the end of the table */
    static String doneMessage(int shot, int total) {
        return "样片完成 —— 已拍 " + shot + " / " + total + " 张，清单见 " + MANIFEST;
    }

    /** MENU during the run */
    static String stoppedMessage(int shot, int total) {
        return shot == 0 ? "样片拍摄在第一张之前停止"
                : "样片拍摄已停止 —— 已拍 " + shot + " / " + total + " 张，清单见 " + MANIFEST;
    }

    /** the camera refused a capture: the run cannot go on, and the frames so far are still listed */
    static String shootFailed(int frame, int shot, String error) {
        return "第 " + frame + " 张快门失败：" + error + "  ——  已拍 " + shot + " 张，清单见 " + MANIFEST;
    }

    // ------------------------------------------------------------ the manifest
    /**
     * The first line of a run: what the columns are, and the delay it was shot with. Appended to, so a file can
     * hold several runs and each one says how it was made.
     */
    static String manifestHeader(int total, int settleMs) {
        return "# recipe-lab samples  ·  " + total + " frames in recipe order  ·  settle " + settleMs + " ms"
                + "  ·  frame" + SEP + "recipe" + SEP + "brand" + SEP + "values";
    }

    /** one frame: its number in the run, and the recipe that was applied for it */
    static String manifestLine(int frame, int recipeIndex) {
        Recipes.Recipe r = Recipes.ALL[recipeIndex];
        return pad2(frame) + SEP + r.name + SEP + Recipes.GROUPS[r.group] + SEP + r.summary();
    }

    private static String pad2(int n) { return n < 10 ? "0" + n : String.valueOf(n); }
}
