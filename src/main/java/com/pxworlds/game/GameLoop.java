package com.pxworlds.game;

import com.pxworlds.Constants;
import com.pxworlds.game.debug.DebugOverlay;
import com.pxworlds.game.io.InputAction;
import com.pxworlds.game.io.Timer;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.states.GameStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Spielschleife mit festem Zeitschritt und separatem (FPS-limitiertem) Render-Pass.
 *
 * <p>Die Spiellogik läuft mit {@link Constants#TICK_RATE} TPS.
 * Das Rendering wird auf das konfigurierte {@code fpsRate} begrenzt.</p>
 *
 * <p>Ein Spiral-of-Death-Schutz begrenzt die maximale Anzahl der Nachholschritte
 * pro Iteration auf {@link Constants#MAX_UPDATES_PER_FRAME}.</p>
 */
public final class GameLoop {

    /** Logger dieser Klasse. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);

    /** Wiederverwendbares leeres Debug-Zeilen-Array — vermeidet Allokation wenn F3 inaktiv. */
    private static final String[] NO_DEBUG_LINES = new String[0];

    /** GameStateManager, der jeden Frame gesteuert wird. */
    private final GameStateManager gsm;
    /** HUD-Overlay für FPS- und Debug-Informationen. */
    private final DebugOverlay     debugOverlay;
    /** Ziel-Render-FPS. */
    private final int              fpsRate;
    /** Wird beim normalen Ende der Schleife oder über den Shutdown-Hook aufgerufen, um den Zustand zu persistieren. */
    private final Consumer<Boolean> onShutdown;

    /**
     * @param newGsm          der GameStateManager, der jeden Frame gesteuert wird
     * @param newDebugOverlay das HUD-Overlay für FPS- und Debug-Informationen
     * @param newFpsRate      Ziel-Render-FPS
     * @param newOnShutdown   wird mit {@code true} aufgerufen wenn der Shutdown-Hook feuert,
     *                        mit {@code false} wenn die Schleife normal endet
     */
    public GameLoop(final GameStateManager newGsm, final DebugOverlay newDebugOverlay,
                    final int newFpsRate, final Consumer<Boolean> newOnShutdown) {
        this.gsm          = newGsm;
        this.debugOverlay = newDebugOverlay;
        this.fpsRate      = newFpsRate;
        this.onShutdown   = newOnShutdown;
    }

    /**
     * Führt die Schleife aus, bis das Fenster geschlossen werden soll.
     *
     * @param window das GLFW-Fenster, das abgefragt und gerendert wird
     */
    public void run(final Window window) {
        final AtomicBoolean saveGuard = new AtomicBoolean(false);

        // Shutdown-Hook: persists game state on IDE-stop (SIGTERM).
        // AtomicBoolean prevents a double-save when the loop exits normally.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (saveGuard.compareAndSet(false, true)) {
                LOGGER.info("Shutdown-Hook: Spielstand wird gesichert...");
                onShutdown.accept(true);
            }
        }, "pxworlds-save-hook"));

        final double tickDuration = 1.0 / Constants.TICK_RATE;
        final double renderCap   = 1.0 / fpsRate;

        double tickAccumulator   = 0;
        double renderAccumulator = 0;
        double frameTime         = 0;
        int    frames            = 0;
        double time              = Timer.getTime();

        while (!window.shouldClose()) {
            final double now    = Timer.getTime();
            final double passed = now - time;
            time = now;

            tickAccumulator   += passed;
            renderAccumulator += passed;
            frameTime         += passed;

            // Spiral-of-Death guard: cap catch-up ticks on lag spikes
            if (tickAccumulator > tickDuration * Constants.MAX_UPDATES_PER_FRAME) {
                tickAccumulator = tickDuration * Constants.MAX_UPDATES_PER_FRAME;
            }

            // --- Logic: fixed timestep (TICK_RATE TPS) ---
            while (tickAccumulator >= tickDuration) {
                tickAccumulator -= tickDuration;

                window.update();

                if (window.hasResized()) {
                    gsm.resize();
                    glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
                }

                if (window.getInputProcessor().wasPressed(InputAction.DEBUG_TOGGLE)) {
                    debugOverlay.toggleDetailed();
                }

                gsm.update(tickDuration);
            }

            // --- Rendering: fpsRate cap ---
            if (renderAccumulator >= renderCap) {
                renderAccumulator -= renderCap;

                // How far between last and next tick? (0.0 – 1.0)
                final float alpha = (float) (tickAccumulator / tickDuration);

                glClear(GL_COLOR_BUFFER_BIT);
                gsm.render(alpha);

                final String[] debugLines;
                if (debugOverlay.isShowDetailed()) {
                    gsm.renderDebug(alpha);
                    debugLines = gsm.getDebugLines();
                } else {
                    debugLines = NO_DEBUG_LINES;
                }
                gsm.renderGui(alpha);
                debugOverlay.render(window, alpha, debugLines);
                window.swapBuffers();
                frames++;

                if (frameTime >= 1.0) {
                    frameTime = 0;
                    debugOverlay.updateFps(frames);
                    frames = 0;
                }
            }
        }

        // Normal exit path: saveGuard prevents double-save with the shutdown hook
        if (saveGuard.compareAndSet(false, true)) {
            onShutdown.accept(false);
        }
    }
}
