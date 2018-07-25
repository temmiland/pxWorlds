package com.pxworlds.game.gui;

import com.pxworlds.game.io.Window;
import org.joml.Vector2f;

import java.util.ArrayList;

public class MenuGui extends Gui {

    public MenuGui(Window window, String tileSheetTexture, int amountOfTiles) {
        super(window, tileSheetTexture, amountOfTiles);

        Logo logo = new Logo(new Vector2f(0, 250), new Vector2f(64, 64));
        Button playButton = new Button(new Vector2f(0, -50), new Vector2f(200, 45));
        Button optionButton = new Button(new Vector2f(0, -145), new Vector2f(200, 45));
        Button exitButton = new Button(new Vector2f(0, -240), new Vector2f(200, 45));

        registerLogos(logo);
        registerButtons(playButton, optionButton, exitButton);

    }
}