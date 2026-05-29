package com.pxworlds.game.gui;

import com.pxworlds.game.io.InputProcessor;
import com.pxworlds.game.io.Window;
import org.joml.Vector2f;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.tiles.TileSheet;

import java.util.ArrayList;
import java.util.Arrays;

public class Gui {

    /** Das Spielfenster. */
    private Window window;
    /** Shader für das Rendering. */
    private Shader shader;
    /** Kamera für das Rendering (protected – Subklassen können eigene Overlays rendern). */
    protected Camera camera;
    /** TileSheet für GUI-Elemente. */
    private TileSheet sheet;

    /** Liste der Buttons. */
    private ArrayList<Button> buttons;
    /** Liste der Logos. */
    private ArrayList<Logo> logos;

    public Gui(Window newWindow, String tileSheetTexture, int amountOfTiles) {
        this.window = newWindow;
        shader = new Shader("gui");
        camera = new Camera(newWindow.getWidth(), newWindow.getHeight());
        sheet = new TileSheet(tileSheetTexture + ".png", amountOfTiles);
        this.buttons = new ArrayList<>();
        this.logos = new ArrayList<>();
    }

    public void resizeCamera() {
        camera.setProjection(window.getWidth(), window.getHeight());
    }

    /**
     * Aktualisiert alle GUI-Elemente.
     *
     * @param input     der aktuelle {@link InputProcessor}
     * @param mousePos  die transformierte Mausposition (Fenstermitte = Ursprung, Y oben)
     * @param windowWidth  aktuelle Fensterbreite in Pixeln
     * @param windowHeight aktuelle Fensterhöhe in Pixeln
     */
    public void update(final InputProcessor input, final Vector2f mousePos,
                       final int windowWidth, final int windowHeight) {
        // Mausposition von Fenster-Pixeln in virtuelle Projektionskoordinaten umrechnen
        final float scaleX = (float) camera.getVirtualWidth()  / windowWidth;
        final float scaleY = (float) camera.getVirtualHeight() / windowHeight;
        final Vector2f virtualMouse = new Vector2f(mousePos.x * scaleX, mousePos.y * scaleY);

        for (Button button : buttons) {
            button.update(input, virtualMouse);
        }
        for (Logo logo : logos) {
            logo.update(input, virtualMouse);
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

    public void registerButtons(Button... newButtons) {
        this.buttons.addAll(Arrays.asList(newButtons));
    }

    public void registerLogos(Logo... newLogos) {
        this.logos.addAll(Arrays.asList(newLogos));
    }

}
