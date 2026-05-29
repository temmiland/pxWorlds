package com.pxworlds.game.gui;

import com.pxworlds.game.io.Window;
import org.joml.Vector2f;

public class PauseGui extends Gui {

    /** Breite der Buttons. */
    private static final float BUTTON_WIDTH = 200;
    /** Höhe der Buttons. */
    private static final float BUTTON_HEIGHT = 45;
    /** Y-Position des Weiter-Buttons. */
    private static final float RESUME_BUTTON_Y = 80;
    /** Y-Position des Optionen-Buttons. */
    private static final float OPTIONS_BUTTON_Y = -15;
    /** Y-Position des Hauptmenü-Buttons. */
    private static final float MAIN_MENU_BUTTON_Y = -110;

    /** Callback zum Fortsetzen des Spiels. */
    private final Runnable onResume;

    public PauseGui(Window window, String tileSheetTexture, int amountOfTiles, Runnable onResume, Runnable onMainMenu) {
        super(window, tileSheetTexture, amountOfTiles);
        this.onResume = onResume;

        final Button resumeButton = new Button(new Vector2f(0, RESUME_BUTTON_Y),
                new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT), "Resume", onResume);
        final Button optionsButton = new Button(new Vector2f(0, OPTIONS_BUTTON_Y),
                new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT), "Options");
        final Button mainMenuButton = new Button(new Vector2f(0, MAIN_MENU_BUTTON_Y),
                new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT), "Main Menu", onMainMenu);

        registerButtons(resumeButton, optionsButton, mainMenuButton);
    }
}
