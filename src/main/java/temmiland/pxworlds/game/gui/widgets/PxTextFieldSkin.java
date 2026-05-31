package temmiland.pxworlds.game.gui.widgets;

import temmiland.rollercoaster.gui.GuiPrimitives;
import temmiland.rollercoaster.gui.widgets.textfield.TextFieldSkin;

/**
 * pxWorlds-Erscheinungsbild eines Texteingabefelds: Rahmen, dunkler Hintergrund, zentrierter
 * schattierter Text und ein blinkender Strich-Cursor. Implementiert den Engine-Vertrag
 * {@link TextFieldSkin}.
 *
 * <p>Zustandslos und über {@link #INSTANCE} geteilt; komponiert ausschließlich aus den
 * {@link GuiPrimitives}.</p>
 */
public enum PxTextFieldSkin implements TextFieldSkin {

    /** Geteilte, zustandslose Instanz. */
    INSTANCE;

    /** Schrift-Skalierung. */
    private static final float FONT_SCALE = 2.5f;
    /** Hintergrund-Grauwert. */
    private static final float BG = 0.15f;
    /** Rahmen-Grauwert. */
    private static final float BORDER_COLOR = 0.9f;
    /** Rahmendicke in GUI-Einheiten. */
    private static final float BORDER = 2f;
    /** Textfarbe (weiß). */
    private static final float WHITE = 1.0f;

    @Override
    public void draw(final GuiPrimitives p, final String text, final float cx, final float cy,
                     final float hw, final float hh, final boolean cursorVisible) {
        p.drawQuad(cx, cy, hw + BORDER, hh + BORDER, BORDER_COLOR, BORDER_COLOR, BORDER_COLOR);
        p.drawQuad(cx, cy, hw, hh, BG, BG, BG);

        final String cursor;
        if (cursorVisible) {
            cursor = "|";
        } else {
            cursor = " ";
        }
        p.drawText(text + cursor, cx, cy, FONT_SCALE, WHITE, WHITE, WHITE, true);
    }
}
