package temmiland.pxworlds.game.gui.widgets;

import temmiland.rollercoaster.gui.GuiContext;

/**
 * Spielspezifisches pxWorlds-Logo, zusammengesetzt aus Tilesheet-Kacheln.
 *
 * <p>Reines Layout-Helferlein für das Immediate-Mode-GUI: zeichnet das mehrzeilige
 * Logo über {@link GuiContext#sprite} und hält selbst keinen Zustand.</p>
 */
public final class Logo {

    /** Oberer Versatz (in halben Höhen) für die obere Logo-Kachelreihe. */
    private static final int TOP_OFFSET = 2;
    /** Unterer Versatz (in halben Höhen) für die untere Logo-Kachelreihe. */
    private static final int BOTTOM_OFFSET = 2;
    /** Tilesheet-Zeile für den oberen Logo-Teil. */
    private static final int TILE_ROW_TOP = 3;
    /** Tilesheet-Zeile für den mittleren Logo-Teil. */
    private static final int TILE_ROW_MIDDLE = 4;
    /** Tilesheet-Zeile für den unteren Logo-Teil. */
    private static final int TILE_ROW_BOTTOM = 5;

    /** Spalten-Faktor und Tilesheet-Spalte je Logo-Zeile (horizontaler Versatz, Spalte). */
    private static final int[][] LINES = {
        {10, 0}, {8, 1}, {6, 2}, {4, 3}, {2, 4},
        {0, 5}, {-2, 6}, {-4, 7}, {-6, 8}, {-8, 9}, {-10, 10}
    };

    private Logo() { }

    /**
     * Zeichnet das Logo zentriert auf {@code (cx, cy)}.
     *
     * @param g  der GUI-Kontext
     * @param cx Mittelpunkt X (GUI-Koordinaten)
     * @param cy Mittelpunkt Y (GUI-Koordinaten)
     * @param hw halbe Breite einer Kachel
     * @param hh halbe Höhe einer Kachel
     */
    public static void draw(final GuiContext g, final float cx, final float cy,
                            final float hw, final float hh) {
        for (final int[] line : LINES) {
            final int factor = line[0];
            final int col = line[1];
            final float x = cx - hw * factor;
            g.sprite(col, TILE_ROW_TOP, x, cy + hh * TOP_OFFSET, hw, hh);
            g.sprite(col, TILE_ROW_MIDDLE, x, cy, hw, hh);
            g.sprite(col, TILE_ROW_BOTTOM, x, cy - hh * BOTTOM_OFFSET, hw, hh);
        }
    }
}
