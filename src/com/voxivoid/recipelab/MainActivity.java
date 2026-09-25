package com.voxivoid.recipelab;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.voxivoid.recipelab.Params.*;

/**
 * Recipe Lab — film recipes with LIVE PREVIEW, then persistent write (photo + video, survives power-cycle).
 *
 * Preview = runtime camera parameters. ENTER picks the recipe: writes its bytes + sync → power-cycle applies it everywhere.
 *
 * Keys: wheel / LEFT / RIGHT recipe · UP / DOWN parameter · top dial adjust · Fn brand browser · ENTER pick
 *       hold ENTER favourite (the centre button exists on every body; Fn / AEL / C1 do not — see issue #18)
 *       AEL / DISP overlay: full → pill → hidden · TRASH stage factory · C1 developer menu (settings snapshot / diff, sample run)
 *       SHUTTER photo · MENU exit
 *
 * This class holds the state and talks to the camera, the store and the views. What a value means, how the store
 * encodes it, what the preview sets and where a key press lands is decided in {@link Params}, which has no Android
 * in it and is covered by tools/test.sh.
 */
public class MainActivity extends Activity implements SurfaceHolder.Callback {
    // ScalarInput scan codes
    private static final int K_UP = 103, K_DOWN = 108, K_LEFT = 105, K_RIGHT = 106, K_ENTER = 232, K_MENU = 514, K_SK1 = 229,
            K_DELETE = 595, K_SK2 = 513, K_PLAY = 207, K_DISP = 608, K_FN = 520, K_AEL = 532, K_C1 = 622, K_S1 = 516, K_S2 = 518,
            K_WHEEL_CW = 522, K_WHEEL_CCW = 523, K_DIAL_CW = 525, K_DIAL_CCW = 526;

    private static final int ACCENT = 0xFFF2B85C, INK = 0xFF1A1208, WHITE = 0xFFFFFFFF, DIM = 0x99FFFFFF;
    /** how long the centre button is held before it means "favourite" instead of "pick" */
    private static final long HOLD_MS = 600;

    private View panel;
    private PickerView picker;
    private HorizontalScrollView chipScroll;
    private boolean swallowMenuUp = false;
    private TextView name, badge, tag, count, meta, mini, toast;
    private StarView fav;
    private PromptView prompt;
    private int promptSel = 0; private boolean promptOpen = false;
    private SharedPreferences prefs;
    private MenuView menu;
    private boolean menuOpen = false;
    private int menuSel = 0, settleIdx = DevTools.SETTLE_DEFAULT;   // developer menu: highlighted row, chosen settle delay
    private HintBar hints;
    private LinearLayout chips;
    private final TextView[] chipLabel = new TextView[N], chipValue = new TextView[N];
    private final View[] chip = new View[N];
    private final Handler handler = new Handler();
    private final Runnable hideToast = new Runnable() { public void run() { toast.setVisibility(View.GONE); } };
    private boolean enterHeld = false, enterLong = false;       // the centre button is down / has already fired its hold action
    private final Runnable enterHold = new Runnable() { public void run() { enterLong = true; toggleFavourite(); } };
    private List<Integer> favs = new ArrayList<Integer>();      // marked recipes, in marking order (Favourites decides, this holds)

    // the sample run (developer menu): one frame per recipe, driven by the handler — stage, settle, shutter, next
    private boolean running = false;
    private int runFrame = 0, runReturnTo = 0;                  // frames shot so far = the next recipe index · the recipe to come back to
    private StringBuilder runLog;                               // manifest lines, written when the run ends
    private final Runnable runStage = new Runnable() { public void run() { sampleStage(); } };
    private final Runnable runShoot = new Runnable() { public void run() { sampleShoot(); } };
    private final Runnable runNext = new Runnable() { public void run() { cancelCapture(); resumePreview(); sampleStage(); } };

    private SurfaceHolder holder;
    private Object cameraEx; private Camera camera; private String origFlat;
    private int row = 0, recipe = 0, overlay = OV_FULL;   // Params.OV_*: the full panel, the pill, nothing, the browser
    private boolean focus = false;                        // a chip is focused: UP/DOWN change its value
    private int browserCol = COL_RECIPES;                 // browser: Params.COL_GROUPS or COL_RECIPES
    private int browserGroup = 0;                     // browser: the group the brand column is on — Favourites.GROUP or a brand
    private int lastChip = 0;                         // chip to return to when leaving the recipe line
    private final int[] cur = new int[N], edit = new int[N];
    private boolean previewOk = false;
    private String previewErr = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.main);
        prefs = getPreferences(MODE_PRIVATE);
        recipe = Math.max(0, Math.min(Recipes.ALL.length - 1, prefs.getInt("recipe", 0)));
        favs = Favourites.decode(prefs.getString("favourites", ""));
        settleIdx = DevTools.clampSettle(prefs.getInt("settle", DevTools.SETTLE_DEFAULT));
        panel = findViewById(R.id.panel);
        picker = (PickerView) findViewById(R.id.picker);
        chipScroll = (HorizontalScrollView) findViewById(R.id.chipscroll);
        name = (TextView) findViewById(R.id.name);
        badge = (TextView) findViewById(R.id.badge);
        tag = (TextView) findViewById(R.id.tag);
        fav = (StarView) findViewById(R.id.fav);
        count = (TextView) findViewById(R.id.count);
        meta = (TextView) findViewById(R.id.meta);
        hints = (HintBar) findViewById(R.id.hints);
        mini = (TextView) findViewById(R.id.mini);
        toast = (TextView) findViewById(R.id.toast);
        prompt = (PromptView) findViewById(R.id.prompt);
        menu = (MenuView) findViewById(R.id.menu);
        chips = (LinearLayout) findViewById(R.id.chips);
        Cn.apply(this, name); Cn.apply(this, badge); Cn.apply(this, tag); Cn.apply(this, count);
        Cn.apply(this, meta); Cn.apply(this, mini); Cn.apply(this, toast);
        buildChips();
        SurfaceView sv = (SurfaceView) findViewById(R.id.surface);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    private void buildChips() {
        for (int i : ORDER) {
            LinearLayout c = new LinearLayout(this);
            c.setOrientation(LinearLayout.VERTICAL);
            c.setGravity(Gravity.CENTER_HORIZONTAL);
            c.setPadding(dp(8), dp(3), dp(8), dp(4));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dp(5);
            c.setLayoutParams(lp);
            TextView l = new TextView(this); l.setTextSize(9); l.setText(ROW_NAME[i]);
            TextView v = new TextView(this); v.setTextSize(13); v.setTypeface(Typeface.DEFAULT_BOLD); v.setSingleLine(true);
            Cn.apply(this, l); Cn.apply(this, v);
            c.addView(l); c.addView(v);
            chips.addView(c);
            chip[i] = c; chipLabel[i] = l; chipValue[i] = v;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
        try {
            Class<?> cx = Class.forName("com.sony.scalar.hardware.CameraEx");
            Method open = cx.getMethod("open", int.class, Class.forName("com.sony.scalar.hardware.CameraEx$OpenOptions"));
            cameraEx = open.invoke(null, 0, null);
            camera = (Camera) cx.getMethod("getNormalCamera").invoke(cameraEx);
            origFlat = camera.getParameters().flatten();
            holder.addCallback(this);
            previewOk = true;
        } catch (Throwable t) { previewOk = false; previewErr = String.valueOf(t); }
        stageRecipe(); applyPreview(); render();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRun(false);                                          // a run cannot outlive the camera it shoots with
        closeMenu();
        handler.removeCallbacks(hideToast);
        handler.removeCallbacks(enterHold); enterHeld = false; enterLong = false;
        holder.removeCallback(this);
        // leave the live parameters equal to what is STORED (not to the launch snapshot): the camera writes some live
        // values (exposure bias, WB fine-tune) straight back into the settings store, which would undo a fresh store
        try { if (camera != null) { load(); System.arraycopy(cur, 0, edit, 0, N); applyPreview(); } } catch (Throwable t) {}
        try { if (camera != null) camera.stopPreview(); } catch (Throwable t) {}
        try { if (cameraEx != null) cameraEx.getClass().getMethod("release").invoke(cameraEx); } catch (Throwable t) {}
        cameraEx = null; camera = null;
    }

    public void surfaceCreated(SurfaceHolder h) {
        try { camera.setPreviewDisplay(h); camera.startPreview(); }
        catch (Throwable t) { previewOk = false; previewErr = String.valueOf(t); render(); }
    }
    public void surfaceChanged(SurfaceHolder h, int f, int w, int hh) {}
    public void surfaceDestroyed(SurfaceHolder h) {}

    // ------------------------------------------------------------ stored settings
    /** a slot's byte as an unsigned index */
    private int rdu(int id) throws NativeException { return NativeBackup.readByte(id) & 0xff; }

    private void load() {
        try {
            for (int i = 1; i < N; i++) {
                int id = ROW_ID[i];
                if (id == SUB_SLOT) { int sid = Recipes.subId(cur[R_PE]); cur[i] = edit[i] = sid == 0 ? 0 : rdu(sid); continue; }
                if (id == QUALITY_SLOTS) { cur[i] = edit[i] = readQuality(); continue; }
                cur[i] = edit[i] = Params.fromStore(id, NativeBackup.readByte(id));
            }
        } catch (Throwable t) { showToast("读取失败：" + t.getMessage(), 0); }
    }

    private void stageRecipe() {
        Recipes.Recipe r = Recipes.ALL[recipe];
        Params.stage(r, edit);
        edit[R_QUAL] = recipeQuality(r);
        prefs.edit().putInt("recipe", recipe).commit();          // reopen on the last selected recipe
    }

    /** quality from the two stored bytes; falls back to the runtime value when the slots are not known yet */
    private int readQuality() {
        try {
            int q = Params.qualityFromStore(rdu(ID_QFMT), rdu(ID_QJPG));
            if (q >= 0) return q;
            if (camera != null) {
                Camera.Parameters p = camera.getParameters();
                return Params.qualityFromRuntime(p.get("storage-fmt"), p.get("jpeg-quality"));
            }
        } catch (Throwable t) {}
        return Q_FINE;
    }

    /** the Factory recipe's quality: the camera's current one until the user changes it in the app, then remembered */
    private int baseQuality() { int b = prefs.getInt("baseQuality", -1); return b >= 0 && b < 4 ? b : cur[R_QUAL]; }
    private int recipeQuality(Recipes.Recipe r) { return Params.recipeQuality(r, baseQuality()); }
    /** user changed quality on the current recipe: CS recipes and JPEG choices on PE recipes redefine the Factory quality */
    private void qualityChanged() {
        if (Params.redefinesBaseQuality(Recipes.ALL[recipe], edit[R_QUAL])) prefs.edit().putInt("baseQuality", edit[R_QUAL]).commit();
    }
    private boolean qualityPersistent() { return ID_QFMT != 0; }

    /** stored value of the SUB slot for the staged effect (the slot changes with the effect) */
    private int storedSub() { int sid = Recipes.subId(edit[R_PE]); if (sid == 0) return edit[R_SUB]; try { return rdu(sid); } catch (Throwable t) { return edit[R_SUB]; } }

    private boolean qualityChanges() { return edit[R_QUAL] != cur[R_QUAL]; }

    private boolean rowDirty(int i) { return Params.rowDirty(i, cur, edit, i == R_SUB ? storedSub() : 0); }
    private boolean dirty() { for (int i = 1; i < N; i++) if (rowDirty(i)) return true; return false; }
    private boolean rowVisible(int i) { return Params.rowVisible(i, edit); }

    private void writeAll() { writeAll(false); }

    /** the attribute of every slot a pending write touches; -1 where the camera would not answer */
    private int[] attrsOf(List<Params.Write> ws) {
        int[] attrs = new int[ws.size()];
        for (int i = 0; i < attrs.length; i++) {
            try { attrs[i] = NativeBackup.attr(ws.get(i).id); } catch (Throwable t) { attrs[i] = -1; }
        }
        return attrs;
    }

    private void writeAll(boolean confirmed) {
        if (!confirmed && qualityChanges()) { openPrompt(); return; }
        if (!dirty()) { showToast("已是当前设定 —— 无需写入", 2500); return; }
        int storedSub = storedSub();
        int n = Params.dirtyRows(cur, edit, storedSub);
        List<Params.Write> ws = Params.writes(cur, edit, storedSub);
        List<Integer> locked = Params.lockedFrom(ws, attrsOf(ws));   // the slots backup protection would refuse (issue #19)
        if (!locked.isEmpty()) { showToast(Params.lockedMessage(locked), 0); return; }   // nothing written, so nothing half-applied

        String msg = null;
        int written = 0;
        for (Params.Write w : ws) {
            try { NativeBackup.writeByte(w.id, w.value); written++; }
            catch (Throwable t) { msg = Params.writeFailedMessage(w.id, String.valueOf(t.getMessage()), written); break; }
        }
        if (written > 0) NativeBackup.sync();                    // Backup_sync_all is void: nothing to catch, nothing to report
        boolean ok = msg == null;
        if (ok) msg = "已选定 —— 已写入 " + n + " 项数值，重启相机后在所有模式生效";
        load(); stageRecipe();
        showToast(msg, ok ? 5000 : 0); render();
    }

    // ------------------------------------------------------------ RAW vs Picture Effect prompt
    private static final String[] PROMPT_OPTS = { "接受", "取消" };

    private void openPrompt() { promptOpen = true; promptSel = 0; renderPrompt(); }

    private void renderPrompt() {
        String[] q = Params.qualityPrompt(cur, edit);
        prompt.set(q[0], q[1], PROMPT_OPTS, promptSel, qualityPersistent() ? null : "画质插槽尚未定位 —— 仅实时预览");
        prompt.setVisibility(View.VISIBLE);
    }

    private void closePrompt() { prompt.setVisibility(View.GONE); promptOpen = false; }

    private boolean promptKey(int sc) {
        switch (sc) {
            case K_LEFT: case K_WHEEL_CCW: case K_DIAL_CCW: case K_RIGHT: case K_WHEEL_CW: case K_DIAL_CW: promptSel ^= 1; renderPrompt(); return true;
            case K_ENTER:
                closePrompt();
                if (promptSel == 0) writeAll(true); else showToast("未选定", 2000);   // cancel: recipe stays previewed only
                render(); return true;
            case K_MENU: case K_SK1: swallowMenuUp = true; closePrompt(); render(); return true;
        }
        return true;
    }

    private void cycleQuality() {
        edit[R_QUAL] = (edit[R_QUAL] + 1) % 4; qualityChanged(); applyPreview(); render();
        showToast("画质：" + Q_LABEL[edit[R_QUAL]] + (qualityPersistent() ? "  —— 按中央键选定" : "  （插槽定位前仅实时预览）"), 2500);
    }

    // ------------------------------------------------------------ snapshot / diff of the whole settings store (developer menu)
    private File snapFile() { return new File(getFilesDir(), "snapshot.bin"); }

    private List<int[]> idList() {
        List<int[]> ids = new ArrayList<int[]>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.ids)));
            try { Params.parseIds(br, ids); } finally { br.close(); }
        } catch (Throwable t) {}
        return ids;
    }

    private void snapshotOrDiff() {
        List<int[]> ids = idList();
        File f = snapFile();
        try {
            if (!f.exists()) {
                FileOutputStream o = new FileOutputStream(f);
                for (int[] e : ids) { byte[] v; try { v = NativeBackup.read(e[0]); } catch (Throwable t) { v = new byte[0]; } o.write(v.length); o.write(v); }
                o.close();
                showToast("已建立 " + ids.size() + " 项设置的快照。请在菜单中修改一项设置后重新打开，再按 C1。", 6000);
                return;
            }
            FileInputStream in = new FileInputStream(f);
            StringBuilder sb = new StringBuilder(); int changed = 0;
            for (int[] e : ids) {
                int len = in.read(); byte[] old = new byte[Math.max(0, len)]; if (len > 0) in.read(old);
                byte[] now; try { now = NativeBackup.read(e[0]); } catch (Throwable t) { now = new byte[0]; }
                if (!java.util.Arrays.equals(old, now)) {
                    changed++;
                    if (changed <= 14) sb.append(Params.diffEntry(e[0], old, now));
                }
            }
            in.close(); f.delete();
            String text = changed + " 项已变化  " + sb;
            java.io.FileWriter w = new java.io.FileWriter(new File(getFilesDir(), "diff.txt"), true); w.write(text + "\n"); w.close();
            showToast(text, 0);
        } catch (Throwable t) { showToast("快照错误：" + t, 0); }
    }

    // ------------------------------------------------------------ read-only check of the slots a recipe writes
    /**
     * Whether this body holds any slot a recipe writes read-only — the question the PROTECTED badge used to ask
     * of one unrelated slot (issue #19). Every slot of {@link Params#allSlots()} is listed with its attribute in
     * locks.txt, so a compatibility report can quote it.
     */
    private void lockCheck() {
        List<Integer> ids = Params.allSlots();
        int[] attrs = new int[ids.size()];
        for (int i = 0; i < attrs.length; i++) {
            try { attrs[i] = NativeBackup.attr(ids.get(i)); } catch (Throwable t) { attrs[i] = -1; }
        }
        String text = Params.lockReport(ids, attrs);
        try {
            java.io.FileWriter w = new java.io.FileWriter(new File(getFilesDir(), "locks.txt"), true);
            try { w.write(text + "\n" + Params.lockLines(ids, attrs)); } finally { w.close(); }
        } catch (Throwable t) { text += "  ·  locks.txt failed: " + t; }
        showToast(text, 0);
    }

    // ------------------------------------------------------------ developer menu (C1) and the sample run
    private void openMenu() {
        if (running) return;
        menuOpen = true; renderMenu();
    }

    private void renderMenu() {
        boolean snapshotTaken = snapFile().exists();
        String[] labels = new String[DevTools.ROWS], details = new String[DevTools.ROWS];
        for (int i = 0; i < DevTools.ROWS; i++) { labels[i] = DevTools.rowLabel(i, snapshotTaken, settleIdx); details[i] = DevTools.rowDetail(i, snapshotTaken); }
        menu.set(labels, details, menuSel);
        menu.setVisibility(View.VISIBLE);
    }

    private void closeMenu() { menu.setVisibility(View.GONE); menuOpen = false; }

    /** the centre button on a menu row: the tools close the menu and run, the delay row stays open and cycles */
    private void pickMenuRow() {
        switch (menuSel) {
            case DevTools.ROW_SNAPSHOT: closeMenu(); snapshotOrDiff(); break;
            case DevTools.ROW_LOCKS: closeMenu(); lockCheck(); break;
            case DevTools.ROW_SAMPLES: closeMenu(); startRun(); break;
            case DevTools.ROW_SETTLE:
                settleIdx = DevTools.nextSettle(settleIdx, +1);
                prefs.edit().putInt("settle", settleIdx).commit();
                renderMenu(); break;
        }
    }

    private boolean menuKey(int sc) {
        switch (sc) {
            case K_UP: case K_LEFT: case K_WHEEL_CCW: case K_DIAL_CCW: menuSel = DevTools.nextRow(menuSel, -1); renderMenu(); return true;
            case K_DOWN: case K_RIGHT: case K_WHEEL_CW: case K_DIAL_CW: menuSel = DevTools.nextRow(menuSel, +1); renderMenu(); return true;
            case K_ENTER: pickMenuRow(); return true;
            case K_MENU: case K_SK1: swallowMenuUp = true; closeMenu(); return true;
            case K_C1: closeMenu(); return true;
        }
        return true;
    }

    /** every key is swallowed while the run walks the table, so nothing changes the recipe mid-run; MENU stops it */
    private boolean runKey(int sc) {
        if (sc == K_MENU || sc == K_SK1) { swallowMenuUp = true; stopRun(true); }
        return true;
    }

    private int settleMs() { return DevTools.SETTLE_MS[DevTools.clampSettle(settleIdx)]; }

    /** shoot one frame per recipe, in table order: the gallery of issue #17, and a preview-pipeline test */
    private void startRun() {
        if (camera == null || !previewOk) { showToast(DevTools.NO_PREVIEW, 5000); return; }
        running = true; runFrame = 0; runReturnTo = recipe;
        runLog = new StringBuilder(DevTools.manifestHeader(Recipes.ALL.length, settleMs())).append('\n');
        handler.post(runStage);
    }

    /** apply the next recipe and give the preview pipeline the settle delay before the shutter */
    private void sampleStage() {
        if (!running) return;
        if (runFrame >= Recipes.ALL.length) { endRun(DevTools.doneMessage(runFrame, Recipes.ALL.length)); return; }
        recipe = runFrame;
        stageRecipe();
        edit[R_QUAL] = Q_FINE;                                  // a sample is only a sample as a JPEG with the look in it, whatever the user shoots
        applyPreview(); render();
        showToast(DevTools.progress(runFrame + 1, Recipes.ALL.length, Recipes.ALL[runFrame].name), 0);
        handler.postDelayed(runShoot, settleMs());
    }

    /** fire the shutter, then let the capture finish before the next recipe is applied */
    private void sampleShoot() {
        if (!running) return;
        try { camera.takePicture(null, null, null); }
        catch (Throwable t) {
            String msg = DevTools.shootFailed(runFrame + 1, runFrame, String.valueOf(t.getMessage()));
            endRun(msg); return;
        }
        runLog.append(DevTools.manifestLine(runFrame + 1, runFrame)).append('\n');
        runFrame++;
        handler.postDelayed(runNext, DevTools.SHUTTER_MS);
    }

    /** MENU during the run, or the camera going away under it */
    private void stopRun(boolean tell) {
        if (!running) return;
        endRun(tell ? DevTools.stoppedMessage(runFrame, Recipes.ALL.length) : null);
    }

    private void endRun(String msg) {
        running = false;
        handler.removeCallbacks(runStage); handler.removeCallbacks(runShoot); handler.removeCallbacks(runNext);
        cancelCapture();
        String failed = writeManifest();
        recipe = runReturnTo; stageRecipe(); applyPreview();
        if (msg != null) showToast(msg + failed, 0);
        render();
    }

    /** the frame list of the run: which recipe each frame was shot with, in order — the camera names the files */
    private String writeManifest() {
        if (runLog == null || runFrame == 0) return "";
        try {
            java.io.FileWriter w = new java.io.FileWriter(new File(getFilesDir(), DevTools.MANIFEST), true);
            try { w.write(runLog.toString()); } finally { w.close(); }
        } catch (Throwable t) { return "  ·  " + DevTools.MANIFEST + " failed: " + t; }
        finally { runLog = null; }
        return "";
    }

    /** the shutter key's release, as the run and the key handler both need it */
    private void cancelCapture() { try { if (cameraEx != null) cameraEx.getClass().getMethod("cancelTakePicture").invoke(cameraEx); } catch (Throwable t) {} }

    /**
     * Between two frames of a run: a capture leaves the preview stopped on a plain Android camera, and nothing is
     * applied to a stopped preview. Whether this body needs it is a question for the camera — on one that restarts
     * the preview itself the call is a no-op, and a HAL that objects to it throws in here rather than out there.
     */
    private void resumePreview() { try { if (camera != null) camera.startPreview(); } catch (Throwable t) {} }

    // ------------------------------------------------------------ favourites (app storage, not the camera store: lost on uninstall)
    private void saveFavourites() { prefs.edit().putString("favourites", Favourites.encode(favs)).commit(); }

    /** hold on the centre button: mark / unmark the highlighted recipe */
    private void toggleFavourite() {
        int pos = favs.indexOf(recipe);
        boolean on = Favourites.toggle(favs, recipe);
        saveFavourites();
        showToast(Favourites.toggleMessage(Recipes.ALL[recipe].name, on), 2500);
        if (overlay == OV_BROWSER && browserGroup == Favourites.GROUP && !on) {
            // unmarked inside the Favourites list: the highlight moves to a neighbour, or back to the brand column when the list is empty
            int next = Favourites.afterRemoval(favs, pos);
            if (next < 0) browserCol = COL_GROUPS;
            else { recipe = next; stageRecipe(); applyPreview(); }
        }
        render();
    }

    // ------------------------------------------------------------ live preview (runtime params)
    private void applyPreview() {
        if (camera == null) return;
        try {
            Camera.Parameters p = camera.getParameters();
            for (Map.Entry<String, String> e : Params.preview(edit).entrySet()) p.set(e.getKey(), e.getValue());
            camera.setParameters(p);
            previewOk = true;
        } catch (Throwable t) { previewOk = false; previewErr = String.valueOf(t.getMessage()); }
    }

    // ------------------------------------------------------------ UI
    private void showToast(String msg, int ms) {
        toast.setText(msg); toast.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideToast);
        if (ms > 0) handler.postDelayed(hideToast, ms);
    }

    private void render() {
        Recipes.Recipe r = Recipes.ALL[recipe];
        boolean dirty = dirty();
        String pos = (recipe + 1) + " / " + Recipes.ALL.length;
        String grp = Recipes.GROUPS[r.group].toUpperCase();
        picker.setVisibility(overlay == OV_BROWSER ? View.VISIBLE : View.GONE);
        if (overlay == OV_BROWSER) { panel.setVisibility(View.GONE); mini.setVisibility(View.GONE); picker.set(recipe, browserCol, browserGroup, favs); return; }
        if (overlay == OV_FULL) {
            panel.setVisibility(View.VISIBLE); mini.setVisibility(View.GONE);
            name.setText(r.name);
            name.setTextColor(row == 0 ? ACCENT : WHITE);
            count.setText(grp + "   " + pos);
            tag.setText(edit[R_PE] != 0 ? "PE" : "CS");
            tag.setTextColor(edit[R_PE] != 0 ? ACCENT : 0xDDFFFFFF);
            fav.setVisibility(favs.contains(recipe) ? View.VISIBLE : View.GONE);
            if (dirty) { badge.setText("PREVIEW"); badge.setBackgroundResource(R.drawable.badge_warn); }
            else { badge.setText("ACTIVE"); badge.setBackgroundResource(R.drawable.badge_ok); }
            meta.setText(Params.metaLine(cur, edit, previewOk ? null : previewErr));
            for (int i = 1; i < N; i++) {
                chip[i].setVisibility(rowVisible(i) ? View.VISIBLE : View.GONE);
                boolean sel = i == row, ch = rowDirty(i), foc = sel && focus;
                chip[i].setBackgroundResource(foc ? R.drawable.chip_sel : sel ? R.drawable.chip_hi : R.drawable.chip);
                chipLabel[i].setTextColor(foc ? INK : sel ? ACCENT : DIM);
                chipValue[i].setTextColor(foc ? INK : ch ? ACCENT : WHITE);
                chipValue[i].setText(Params.fmt(i, edit[i], edit));
            }
            if (row == 0) chipScroll.post(new Runnable() { public void run() { chipScroll.smoothScrollTo(0, 0); } });
            else {
                final View c = chip[row];
                chipScroll.post(new Runnable() { public void run() {
                    int l = c.getLeft(), rgt = c.getRight(), sx = chipScroll.getScrollX(), w = chipScroll.getWidth();
                    if (l < sx) chipScroll.smoothScrollTo(l - dp(8), 0); else if (rgt > sx + w) chipScroll.smoothScrollTo(rgt - w + dp(8), 0);
                } });
            }
            hints.setMode(row == 0 ? HintBar.RECIPE : focus ? HintBar.EDIT : HintBar.CHIPS);
        } else if (overlay == OV_PILL) {
            panel.setVisibility(View.GONE); mini.setVisibility(View.VISIBLE);
            mini.setText(Params.miniLine(recipe, cur, edit, dirty));
        } else {
            panel.setVisibility(View.GONE); mini.setVisibility(View.GONE);
        }
    }

    // ------------------------------------------------------------ input
    /** change the value of the focused chip */
    private void stepValue(int dir) {
        if (row == 0) return;
        if (Params.step(edit, row, dir, recipeQuality(Recipes.ALL[recipe]))) qualityChanged();
        applyPreview(); render();
    }

    /** LEFT/RIGHT inside the chip strip: next / previous visible chip, wrapping */
    private void moveChip(int dir) { row = Params.nextChip(row, dir, edit); lastChip = row; render(); }

    /** UP/DOWN: switch between the recipe line and the chip strip */
    private void toggleLine() {
        if (row == 0) row = Params.enterChips(lastChip, edit);
        else { lastChip = row; row = 0; }
        render();
    }

    private void setFocus(boolean f) { focus = f && row != 0; render(); }

    private void nextRecipe(int dir) { recipe = Recipes.next(recipe, dir); stageRecipe(); applyPreview(); render(); }

    /** brand column: the group above / below, its first recipe previewed (an empty Favourites list leaves the recipe alone) */
    private void nextGroup(int dir) {
        browserGroup = Favourites.nextGroup(browserGroup, dir);
        int land = Favourites.landing(browserGroup, favs);
        if (land >= 0) { recipe = land; stageRecipe(); applyPreview(); }
        render();
    }

    private void openBrowser(boolean open) {
        overlay = open ? OV_BROWSER : OV_FULL; row = 0; focus = false;
        browserGroup = Favourites.openingGroup(favs, recipe);
        browserCol = COL_RECIPES;
        render();
    }

    /** recipe column: the next / previous recipe of the group the browser is on, wrapping */
    private void nextInGroup(int dir) {
        recipe = browserGroup == Favourites.GROUP ? Favourites.next(favs, recipe, dir) : Recipes.nextInGroup(recipe, dir);
        stageRecipe(); applyPreview(); render();
    }

    /** the recipe column is not reachable while the Favourites list is empty */
    private boolean enterRecipeColumn() {
        if (!Favourites.hasRecipes(browserGroup, favs)) { showToast(Favourites.EMPTY_HINT, 3000); return false; }
        browserCol = COL_RECIPES; render(); return true;
    }

    /** the centre button on a recipe in the browser: close it, leaving that recipe previewed */
    private void pickInBrowser() {
        openBrowser(false); showToast(Recipes.ALL[recipe].name + " previewed — ENTER to pick", 3000);
    }

    private void stageFactory() { recipe = 0; stageRecipe(); applyPreview(); showToast("Factory values staged — ENTER to pick", 3000); render(); }

    private boolean browserKey(int sc) {
        switch (sc) {
            case K_UP: case K_WHEEL_CCW: case K_DIAL_CCW: if (browserCol == COL_GROUPS) nextGroup(-1); else nextInGroup(-1); return true;
            case K_DOWN: case K_WHEEL_CW: case K_DIAL_CW: if (browserCol == COL_GROUPS) nextGroup(+1); else nextInGroup(+1); return true;
            case K_LEFT: case K_RIGHT: if (browserCol == COL_RECIPES) { browserCol = COL_GROUPS; render(); } else enterRecipeColumn(); return true;
            case K_MENU: case K_SK1: swallowMenuUp = true; openBrowser(false); return true;
            case K_FN: case K_AEL: case K_DISP: openBrowser(false); return true;
            case K_C1: openMenu(); return true;
            case K_DELETE: case K_SK2: stageFactory(); return true;
            case K_S1: try { camera.autoFocus(null); } catch (Throwable t) {} return true;
            case K_S2: try { camera.takePicture(null, null, null); } catch (Throwable t) {} return true;
        }
        return true;
    }

    /** handle keys before any focusable view (the chip scroller would otherwise eat LEFT/RIGHT) */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_DOWN) return onKeyDown(e.getKeyCode(), e);
        if (e.getAction() == KeyEvent.ACTION_UP) return onKeyUp(e.getKeyCode(), e);
        return super.dispatchKeyEvent(e);
    }

    /** whether a hold on the centre button marks a favourite where the user is now */
    private boolean holdMarksFavourite() { return Params.holdMarksFavourite(overlay, row, browserCol); }

    /** the centre button pressed: the short action waits for the release, a hold becomes "favourite" */
    private void enterDown() {
        if (enterHeld) return;                                   // key repeat while held
        enterHeld = true; enterLong = false;
        if (holdMarksFavourite()) handler.postDelayed(enterHold, HOLD_MS);
    }

    /** the centre button released before the hold fired: what ENTER used to do on the press */
    private void enterUp() {
        handler.removeCallbacks(enterHold);
        boolean held = enterHeld, fired = enterLong;
        enterHeld = false; enterLong = false;
        if (!held || fired) return;
        switch (Params.enterAction(overlay, row, browserCol)) {
            case ENTER_BROWSER_COLUMN: enterRecipeColumn(); break;
            case ENTER_BROWSER_PICK: pickInBrowser(); break;
            case ENTER_PICK: writeAll(); break;
            default: setFocus(!focus); break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (running) return runKey(e.getScanCode());
        if (promptOpen) return promptKey(e.getScanCode());
        if (menuOpen) return menuKey(e.getScanCode());
        if (e.getScanCode() == K_ENTER) { enterDown(); return true; }
        if (overlay == OV_BROWSER && e.getScanCode() != K_PLAY) return browserKey(e.getScanCode());
        switch (e.getScanCode()) {
            case K_LEFT: case K_RIGHT: {
                int dir = e.getScanCode() == K_RIGHT ? +1 : -1;
                if (focus) stepValue(dir); else if (Params.onRecipeLine(overlay, row)) nextRecipe(dir); else moveChip(dir);
                return true;
            }
            case K_WHEEL_CW: case K_WHEEL_CCW: {
                int dir = e.getScanCode() == K_WHEEL_CW ? +1 : -1;
                if (focus) stepValue(dir); else nextRecipe(dir);
                return true;
            }
            case K_DIAL_CW: case K_DIAL_CCW: {
                int dir = e.getScanCode() == K_DIAL_CW ? +1 : -1;
                if (focus) stepValue(dir); else if (Params.onRecipeLine(overlay, row)) nextRecipe(dir); else moveChip(dir);
                return true;
            }
            case K_UP: case K_DOWN: {
                if (overlay != OV_FULL) return true;
                if (focus) stepValue(e.getScanCode() == K_UP ? +1 : -1); else toggleLine();
                return true;
            }
            case K_AEL: case K_DISP: overlay = (overlay + 1) % 3; render(); return true;   // full → pill → hidden; the browser is not in the cycle
            case K_FN: openBrowser(true); return true;
            case K_C1: openMenu(); return true;
            case K_DELETE: case K_SK2: stageFactory(); return true;
            case K_S1: try { camera.autoFocus(null); } catch (Throwable t) {} return true;
            case K_S2: try { camera.takePicture(null, null, null); } catch (Throwable t) {} return true;
            case K_MENU: case K_SK1: if (focus) { swallowMenuUp = true; setFocus(false); } return true;
            case K_PLAY: return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) { finish(); return true; }
        return super.onKeyDown(keyCode, e);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent e) {
        if (promptOpen) { if (e.getScanCode() == K_MENU || e.getScanCode() == K_SK1) swallowMenuUp = false; return true; }
        if (running) return true;                               // the release of whatever key started or stopped the run
        switch (e.getScanCode()) {
            case K_ENTER: enterUp(); return true;
            case K_FN: return true;
            case K_MENU: case K_SK1: if (swallowMenuUp) { swallowMenuUp = false; return true; } finish(); return true;
            case K_S1: try { camera.cancelAutoFocus(); } catch (Throwable t) {} return true;
            case K_S2: cancelCapture(); return true;
            case K_UP: case K_DOWN: case K_LEFT: case K_RIGHT: case K_PLAY: case K_DISP:
            case K_DELETE: case K_SK2: case K_C1: case K_AEL: case K_WHEEL_CW: case K_WHEEL_CCW: case K_DIAL_CW: case K_DIAL_CCW: return true;
        }
        return super.onKeyUp(keyCode, e);
    }
}
