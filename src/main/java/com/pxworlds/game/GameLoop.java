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
 * Fixed-timestep game loop with an uncapped (FPS-limited) render pass.
 *
 * <p>Tick logic runs at {@link Constants#TICK_RATE} TPS.
 * Rendering is capped to the configured {@code fpsRate}.</p>
 *
 * <p>A Spiral-of-Death guard limits the maximum number of catch-up ticks
 * per iteration to {@link Constants#MAX_UPDATES_PER_FRAME}.</p>
 */
public final class GameLoop {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);

    /** Reusable empty debug-lines array — avoids allocation when F3 is off. */
    private static final String[] NO_DEBUG_LINES = new String[0];

    private final GameStateManager gsm;
    private final DebugOverlay     debugOverlay;
    private final int              fpsRate;
    /** Called when the loop exits normally or via shutdown hook to persist state. */
    private final Consumer<Boolean> onShutdown;

    /**
     * @param gsm         the game state manager to drive each frame
     * @param debugOverlay the HUD overlay for FPS and debug info
     * @param fpsRate     target render frames per second
     * @param onShutdown  called with {@code true} when the shutdown-hook fires,
     *                    {@code false} when the loop exits normally
     */
    public GameLoop(final GameStateManager gsm, final DebugOverlay debugOverlay,
                    final int fpsRate, final Consumer<Boolean> onShutdown) {
        this.gsm          = gsm;
        this.debugOverlay = debugOverlay;
        this.fpsRate      = fpsRate;
        this.onShutdown   = onShutdown;
    }

    /**
     * Runs the loop until the window requests close.
     *
     * @param window the GLFW window to poll and render into
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
