package com.pxworlds.game.gui;

import com.pxworlds.game.io.Window;
import org.joml.Vector2f;

import java.util.ArrayList;

public class MenuGui extends Gui {

    // Menu GUI layout constants
    /** The Y position of the logo. */
    private static final float LOGO_Y_POSITION = 250;
    /** The scale of the logo. */
    private static final float LOGO_SCALE = 64;
    /** The Y position of the play button. */
    private static final float PLAY_BUTTON_Y_POSITION = -50;
    /** The width of buttons. */
    private static final float BUTTON_WIDTH = 200;
    /** The height of buttons. */
    private static final float BUTTON_HEIGHT = 45;
    /** The Y position of the option button. */
    private static final float OPTION_BUTTON_Y_POSITION = -145;
    /** The Y position of the exit button. */
    private static final float EXIT_BUTTON_Y_POSITION = -240;

    public MenuGui(Window window, String tileSheetTexture, int amountOfTiles) {
        super(window, tileSheetTexture, amountOfTiles);

        final Logo logo = new Logo(new Vector2f(0, LOGO_Y_POSITION), new Vector2f(LOGO_SCALE, LOGO_SCALE));
        final Button playButton = new Button(new Vector2f(0, PLAY_BUTTON_Y_POSITION), new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT));
        final Button optionButton = new Button(new Vector2f(0, OPTION_BUTTON_Y_POSITION), new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT));
        final Button exitButton = new Button(new Vector2f(0, EXIT_BUTTON_Y_POSITION), new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT));

        registerLogos(logo);
        registerButtons(playButton, optionButton, exitButton);

    }
}
