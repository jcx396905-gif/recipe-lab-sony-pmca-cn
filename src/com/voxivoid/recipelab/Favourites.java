package com.voxivoid.recipelab;

import java.util.ArrayList;
import java.util.List;

/**
 * The favourites list and the browser's group order, without the camera: which recipes are marked, in the order
 * they were marked, how the list is kept in the app's preferences, and how the browser walks it.
 *
 * A favourite is remembered by recipe <em>name</em>, not index, so the marks survive a build that inserts a recipe
 * in the middle of the table; a name the table no longer has is dropped on load. The list lives in the app's own
 * storage (SharedPreferences), not in the camera settings store, so it goes with an uninstall.
 *
 * MainActivity owns the list itself; this class decides. No android.* import may appear here (tools/test.sh).
 */
final class Favourites {
    private Favourites() {}

    /** the browser group that lists the favourites; brands are 0..GROUPS.length-1 */
    static final int GROUP = -1;
    static final String NAME = "收藏夹";
    /** separates names in the stored string; no recipe name contains it (RecipesTest) */
    static final String SEP = "|";
    /** the right column of an empty Favourites group */
    static final String EMPTY_TITLE = "暂无收藏", EMPTY_HINT = "在配方上长按中央按钮即可收藏到此处";

    // ------------------------------------------------------------ storage
    /** the stored string -> recipe indexes in marking order; unknown names and repeats are dropped */
    static List<Integer> decode(String stored) {
        List<Integer> favs = new ArrayList<Integer>();
        if (stored == null || stored.isEmpty()) return favs;
        for (String name : stored.split("\\" + SEP)) {
            int i = indexOf(name);
            if (i >= 0 && !favs.contains(i)) favs.add(i);
        }
        return favs;
    }

    /** recipe indexes -> the stored string */
    static String encode(List<Integer> favs) {
        StringBuilder s = new StringBuilder();
        for (int i : favs) { if (s.length() > 0) s.append(SEP); s.append(Recipes.ALL[i].name); }
        return s.toString();
    }

    private static int indexOf(String name) {
        for (int i = 0; i < Recipes.ALL.length; i++) if (Recipes.ALL[i].name.equals(name)) return i;
        return -1;
    }

    // ------------------------------------------------------------ marking
    /** marks an unmarked recipe (at the end) or unmarks a marked one; returns whether it is a favourite now */
    static boolean toggle(List<Integer> favs, int recipe) {
        int pos = favs.indexOf(recipe);
        if (pos >= 0) { favs.remove(pos); return false; }
        favs.add(recipe);
        return true;
    }

    /** the favourite to highlight after the one at {@code pos} was removed: the next one, else the new last, -1 when none */
    static int afterRemoval(List<Integer> favs, int pos) {
        if (favs.isEmpty()) return -1;
        return favs.get(Math.min(Math.max(0, pos), favs.size() - 1));
    }

    /** the toast after a toggle */
    static String toggleMessage(String recipeName, boolean on) {
        return recipeName + (on ? " 已加入" : " 已移出") + NAME;
    }

    // ------------------------------------------------------------ browser navigation
    /** the group above / below in the brand column: Favourites sits first, everything wraps */
    static int nextGroup(int group, int dir) {
        int n = Recipes.GROUPS.length + 1, pos = group + 1;      // Favourites -> 0, brand g -> g + 1
        return (pos + n + dir) % n - 1;
    }

    /** the brand column's row for a group, Favourites first */
    static int groupRow(int group) { return group + 1; }

    /** the recipe the highlight lands on when the brand column moves onto {@code group}; -1 when there is nothing to land on */
    static int landing(int group, List<Integer> favs) {
        if (group == GROUP) return favs.isEmpty() ? -1 : favs.get(0);
        return Recipes.GROUP_START[group];
    }

    /** the favourite after / before {@code recipe} in marking order, wrapping; the first one when it is not marked; -1 when none */
    static int next(List<Integer> favs, int recipe, int dir) {
        if (favs.isEmpty()) return -1;
        int pos = favs.indexOf(recipe);
        if (pos < 0) return favs.get(0);
        return favs.get((pos + favs.size() + dir) % favs.size());
    }

    /** the group the browser opens on: Favourites when the recipe is one, else its brand */
    static int openingGroup(List<Integer> favs, int recipe) { return favs.contains(recipe) ? GROUP : Recipes.ALL[recipe].group; }

    /** the group's name as the brand column shows it */
    static String groupName(int group) { return group == GROUP ? NAME : Recipes.GROUPS[group]; }

    /** how many recipes a group lists */
    static int groupCount(int group, List<Integer> favs) { return group == GROUP ? favs.size() : Recipes.GROUP_COUNT[group]; }

    /** whether a group has anything in its recipe column — an empty Favourites list has not */
    static boolean hasRecipes(int group, List<Integer> favs) { return groupCount(group, favs) > 0; }

    /** the k-th recipe of a group */
    static int recipeAt(int group, int k, List<Integer> favs) { return group == GROUP ? favs.get(k) : Recipes.GROUP_START[group] + k; }

    /** the position of a recipe inside a group's list, -1 when it is not there */
    static int positionIn(int group, int recipe, List<Integer> favs) {
        if (group == GROUP) return favs.indexOf(recipe);
        return Recipes.ALL[recipe].group == group ? recipe - Recipes.GROUP_START[group] : -1;
    }
}
