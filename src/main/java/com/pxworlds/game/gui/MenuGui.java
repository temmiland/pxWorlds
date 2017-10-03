package com.pxworlds.game.gui;

import com.pxworlds.game.io.Window;
import org.joml.Vector2f;

import java.util.ArrayList;

public class MenuGui extends Gui {

    public MenuGui(Window window, String tileSheetTexture, int amountOfTiles) {
        super(window, tileSheetTexture, amountOfTiles);

        Logo logo = new Logo(new Vector2f(0, 100), new Vector2f(144, 48));
        Button playButton = new Button(new Vector2f(0, 0), new Vector2f(200, 45));
        Button optionButton = new Button(new Vector2f(0, -95), new Vector2f(200, 45));
        Button exitButton = new Button(new Vector2f(0, -190), new Vector2f(200, 45));

        registerLogos(logo);
        registerButtons(playButton, optionButton, exitButton);

    }
}