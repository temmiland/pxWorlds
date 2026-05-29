package com.pxworlds.game.states;

import com.pxworlds.Constants;
import com.pxworlds.game.gui.WorldSelectGui;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.world.WorldMeta;
import com.pxworlds.game.world.WorldMetaReader;
import com.pxworlds.game.world.migration.MigrationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Zeigt das Welten-Auswahlmenü.
 *
 * <p>Scannt beim Initialisieren alle {@code .pxwl}-Dateien im Benutzerverzeichnis
 * und sortiert sie nach Letztspiel-Zeitstempel (neueste zuerst).</p>
 *
 * <p>Lösch-Flow: DEL-Klick → Bestätigungsansicht → "Yes, Delete" löscht Datei.</p>
 * <p>Umbenennen-Flow: Ren-Klick → Textfeld → "Rename" benennt die Datei um.</p>
 */
public class WorldSelectState extends GameState {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldSelectState.class);

    private WorldSelectGui gui;
    private Window window;
    private WorldMeta pendingDeleteMeta;
    private WorldMeta pendingRenameMeta;
    private WorldMeta pendingMigrationMeta;
    private WorldMeta pendingBackupMigrationMeta;
    private String migrationError;

    public WorldSelectState(final GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(final Window newWindow) {
        this.window = newWindow;
        final List<WorldMeta> metas = scanWorlds();
        this.gui = new WorldSelectGui(
                newWindow,
                metas,
                pendingDeleteMeta,
                pendingRenameMeta,
                pendingMigrationMeta,
                pendingBackupMigrationMeta,
                migrationError,
                this::onPlayWorld,
                this::onDeleteRequest,
                this::onRenameRequest,
                () -> gsm.setState(GameStateType.NEW_WORLD),
                () -> gsm.setState(GameStateType.MENU),
                this::onDeleteConfirm,
                this::onDeleteCancel,
                this::onRenameConfirm,
                this::onRenameCancel,
                this::onMigrationConfirm,
                this::onMigrationCancel,
                this::onBackupMigrationConfirm,
                this::onBackupMigrationCancel,
                this::onMigrationErrorBack);
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

    private void onPlayWorld(final WorldMeta meta) {
        if (meta.needsMigration() && meta.name().endsWith("_backup")) {
            pendingBackupMigrationMeta = meta;
            init(window);
            return;
        }
        if (meta.needsMigration()) {
            pendingMigrationMeta = meta;
            init(window);
            return;
        }
        gsm.startLevel(meta.name());
    }

    private void onMigrationConfirm() {
        if (pendingMigrationMeta != null) {
            try {
                MigrationRunner.run(pendingMigrationMeta);
                final String name = pendingMigrationMeta.name();
                pendingMigrationMeta = null;
                gsm.startLevel(name);
            } catch (final Exception e) {
                LOGGER.error("Migration fehlgeschlagen für '{}'", pendingMigrationMeta.name(), e);
                migrationError = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                pendingMigrationMeta = null;
                init(window);
            }
        }
    }

    private void onMigrationCancel() {
        pendingMigrationMeta = null;
        init(window);
    }

    private void onDeleteRequest(final WorldMeta meta) {
        pendingDeleteMeta = meta;
        init(window);
    }

    private void onDeleteConfirm() {
        if (pendingDeleteMeta != null) {
            final File file = new File(Constants.WORLDS_DIRECTORY_PATH,
                    pendingDeleteMeta.name() + ".pxwl");
            if (file.exists() && !file.delete()) {
                LOGGER.error("Konnte Weltdatei nicht löschen: {}", file.getAbsolutePath());
            } else {
                LOGGER.info("Welt '{}' gelöscht.", pendingDeleteMeta.name());
            }
            pendingDeleteMeta = null;
        }
        init(window);
    }

    private void onDeleteCancel() {
        pendingDeleteMeta = null;
        init(window);
    }

    private void onRenameRequest(final WorldMeta meta) {
        pendingRenameMeta = meta;
        init(window);
    }

    private void onRenameConfirm() {
        final String newName = gui.getNewName().trim();

        if (newName.isEmpty()) {
            gui.setRenameError("Name must not be empty.");
            return;
        }

        if (newName.equals(pendingRenameMeta.name())) {
            pendingRenameMeta = null;
            init(window);
            return;
        }

        final File target = new File(Constants.WORLDS_DIRECTORY_PATH, newName + ".pxwl");
        if (target.exists()) {
            gui.setRenameError("A world with this name already exists.");
            return;
        }

        final File source = new File(Constants.WORLDS_DIRECTORY_PATH,
                pendingRenameMeta.name() + ".pxwl");
        if (!source.renameTo(target)) {
            LOGGER.error("Konnte Weltdatei nicht umbenennen: {} → {}", source, target);
            gui.setRenameError("Rename failed.");
            return;
        }

        LOGGER.info("Welt '{}' umbenannt zu '{}'.", pendingRenameMeta.name(), newName);
        pendingRenameMeta = null;
        init(window);
    }

    private void onRenameCancel() {
        pendingRenameMeta = null;
        init(window);
    }

    private void onBackupMigrationConfirm() {
        if (pendingBackupMigrationMeta != null) {
            try {
                MigrationRunner.run(pendingBackupMigrationMeta);
                final String name = pendingBackupMigrationMeta.name();
                pendingBackupMigrationMeta = null;
                gsm.startLevel(name);
            } catch (final Exception e) {
                LOGGER.error("Migration fehlgeschlagen für '{}'", pendingBackupMigrationMeta.name(), e);
                migrationError = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                pendingBackupMigrationMeta = null;
                init(window);
            }
        }
    }

    private void onBackupMigrationCancel() {
        pendingBackupMigrationMeta = null;
        init(window);
    }

    private void onMigrationErrorBack() {
        migrationError = null;
        init(window);
    }

    public void setMigrationError(final String message) {
        this.migrationError = message;
    }

    private static List<WorldMeta> scanWorlds() {
        final List<WorldMeta> result = new ArrayList<>();
        final File dir = new File(Constants.WORLDS_DIRECTORY_PATH);
        if (dir.isDirectory()) {
            final File[] files = dir.listFiles((d, name) -> name.endsWith(".pxwl"));
            if (files != null) {
                for (final File f : files) {
                    WorldMetaReader.read(f).ifPresent(result::add);
                }
            }
        }
        result.sort(Comparator.comparingLong(WorldMeta::lastPlayed).reversed());
        return result;
    }
}
