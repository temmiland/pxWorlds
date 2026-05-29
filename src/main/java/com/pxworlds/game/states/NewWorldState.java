package com.pxworlds.game.states;

import com.pxworlds.Constants;
import com.pxworlds.game.gui.NewWorldGui;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.world.WorldLoadingService;

import java.io.File;

/**
 * State zum Anlegen einer neuen Welt.
 *
 * <p>Der Spieler gibt einen eindeutigen Namen ein. Bei Bestätigung wird eine
 * leere Welt mit Standarddimensionen gespeichert und sofort geladen.</p>
 */
public class NewWorldState extends GameState {


    private NewWorldGui gui;
    private Window window;

    public NewWorldState(final GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(final Window newWindow) {
        this.window = newWindow;
        this.gui = new NewWorldGui(newWindow, this::onCreateConfirm,
                () -> gsm.setState(GameStateType.WORLD_SELECT));
        gui.reset();
    }

    @Override
    public void resize() {
        gui.resizeCamera();
    }

    @Override
    public void update(final double delta) {
        gui.update(window.getInputProcessor(), window.getMousePosition(),
                   window.getWidth(), window.getHeight(), delta);
    }

    @Override
    public void render(final float alpha) {
        // Kein Hintergrund-Level
    }

    @Override
    public void renderGui(final float alpha) {
        gui.render();
    }

    // -------------------------------------------------------------------------

    private void onCreateConfirm() {
        final String name = gui.getWorldName().trim();

        if (name.isEmpty()) {
            gui.setError("Name must not be empty.");
            return;
        }

        final File target = new File(Constants.WORLDS_DIRECTORY_PATH, name + ".pxwl");
        if (target.exists()) {
            gui.setError("A world with this name already exists.");
            return;
        }

        // Welt aus Vorlage erstellen und sofort laden
        WorldLoadingService.createNew(name);
        gsm.startLevel(name);
    }
}
