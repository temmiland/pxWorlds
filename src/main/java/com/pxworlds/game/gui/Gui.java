package com.pxworlds.game.gui;

import com.pxworlds.game.io.Input;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileSheet;

import java.util.ArrayList;
import java.util.Arrays;

public class Gui {

    private Window window;
    private Shader shader;
    private Camera camera;
    private TileSheet sheet;

    public ArrayList<Button> buttons;
    public ArrayList<Logo> logos;

    public Gui(Window window, String tileSheetTexture, int amountOfTiles) {
        this.window = window;
        shader = new Shader("gui");
        camera = new Camera(window.getWidth(), window.getHeight());
        sheet = new TileSheet(tileSheetTexture + ".png", amountOfTiles);
        this.buttons = new ArrayList<>();
        this.logos = new ArrayList<>();
    }

    public void resizeCamera() {
        camera.setProjection(window.getWidth(), window.getHeight());
    }

    public void update(Input input) {
        for(Button button : buttons) {
            button.update(input);
        }
        for(Logo logo : logos) {
            logo.update(input);
        }
    }

    public void render() {
        shader.bind();
        for(Button button : buttons) {
            button.render(camera, sheet, shader);
        }
        for(Logo logo : logos) {
            logo.render(camera, sheet, shader);
        }
    }

    public void registerButtons(Button... buttons) {
        this.buttons.addAll(Arrays.asList(buttons));
    }

    public void registerLogos(Logo... logos) {
        this.logos.addAll(Arrays.asList(logos));
    }

}
