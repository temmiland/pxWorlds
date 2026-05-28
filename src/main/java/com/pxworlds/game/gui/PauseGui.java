package com.pxworlds.game.gui;

import com.pxworlds.game.PxWorlds;
import com.pxworlds.game.io.Window;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class PauseGui extends Gui {

    /** The width of buttons. */
    private static final float BUTTON_WIDTH = 200;
    /** The height of buttons. */
    private static final float BUTTON_HEIGHT = 45;
    /** The Y position of the resume button. */
    private static final float RESUME_BUTTON_Y = 80;
    /** The Y position of the options button. */
    private static final float OPTIONS_BUTTON_Y = -15;
    /** The Y position of the main menu button. */
    private static final float MAIN_MENU_BUTTON_Y = -110;

    /** Callback to unpause the game. */
    private final Runnable onResume;

    public PauseGui(Window window, String tileSheetTexture, int amountOfTiles, Runnable onResume) {
        super(window, tileSheetTexture, amountOfTiles);
        this.onResume = onResume;

        final Button resumeButton = new Button(new Vector2f(0, RESUME_BUTTON_Y),
                new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT), "Resume", onResume);
        final Button optionsButton = new Button(new Vector2f(0, OPTIONS_BUTTON_Y),
                new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT), "Options");
        final Button mainMenuButton = new Button(new Vector2f(0, MAIN_MENU_BUTTON_Y),
                new Vector2f(BUTTON_WIDTH, BUTTON_HEIGHT), "Main Menu",
                () -> PxWorlds.getInstance().gsm.setState(1));

        registerButtons(resumeButton, optionsButton, mainMenuButton);
    }
}
