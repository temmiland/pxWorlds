package com.pxworlds.game.gui;

import com.pxworlds.game.io.Window;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class MenuGui extends Gui {

    // Menü-GUI-Layout-Konstanten
    /** Y-Position des Logos. */
    private static final float LOGO_Y_POSITION = 250;
    /** Skalierung des Logos. */
    private static final float LOGO_SCALE = 64;
    /** Y-Position des Play-Buttons. */
    private static final float PLAY_BUTTON_Y_POSITION = -50;
    /** Breite der Buttons. */
    private static final float BUTTON_WIDTH = 200;
    /** Höhe der Buttons. */
    private static final float BUTTON_HEIGHT = 45;
    /** Y-Position des Optionen-Buttons. */
    private static final float OPTION_BUTTON_Y_POSITION = -145;
    /** Y-Position des Beenden-Buttons. */
    private static final float EXIT_BUTTON_Y_POSITION = -240;

    public MenuGui(Window window, String tileSheetTexture, int amountOfTiles, Runnable onPlay) {
        super(window, tileSheetTexture, amountOfTiles);

        final Logo logo = new Logo(new Vector2f(0, LOGO_Y_POSITION), new Vector2f(LOGO_SCALE, LOGO_SCALE));
        final Button playButton = new Button(new Vector2f(0, PLAY_BUTTON_Y_POSITION),
                new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT), "Play", onPlay);
        final Button optionButton = new Button(new Vector2f(0, OPTION_BUTTON_Y_POSITION),
                new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT), "Options");
        final Button exitButton = new Button(new Vector2f(0, EXIT_BUTTON_Y_POSITION),
                new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT), "Exit",
                () -> glfwSetWindowShouldClose(window.getWindow(), true));

        registerLogos(logo);
        registerButtons(playButton, optionButton, exitButton);

    }
}
