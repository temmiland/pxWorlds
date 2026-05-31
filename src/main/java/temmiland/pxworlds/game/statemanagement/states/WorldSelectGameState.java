package temmiland.pxworlds.game.statemanagement.states;

import temmiland.pxworlds.game.PxWorlds;
import temmiland.pxworlds.game.gui.PxGuiInput;
import temmiland.pxworlds.game.gui.PxGuiRenderer;
import temmiland.pxworlds.game.gui.widgets.PxButtonSkin;
import temmiland.pxworlds.game.gui.widgets.PxTextFieldSkin;

import temmiland.pxworlds.game.statemanagement.PxGameStateManager;
import temmiland.rollercoaster.game.state.GameState;
import temmiland.pxworlds.game.statemanagement.PxGameStates;
import temmiland.pxworlds.game.world.WorldMeta;
import temmiland.pxworlds.game.world.WorldMetaReader;
import temmiland.pxworlds.game.world.migration.MigrationRunner;
import temmiland.rollercoaster.gui.GuiContext;
import temmiland.rollercoaster.gui.widgets.textfield.TextField;
import temmiland.rollercoaster.platform.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Weltenauswahl-Screen im Immediate-Mode-GUI.
 *
 * <p>Der angezeigte Inhalt wird durch {@link #mode} bestimmt (Liste, Lösch-/Umbenenn-/
 * Migrations-Dialoge, Fehleranzeige). Moduswechsel sind reine Feld-Zuweisungen — es wird
 * kein GUI mehr neu aufgebaut.</p>
 */
public class WorldSelectGameState extends GameState<PxGameStateManager> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorldSelectGameState.class);

    /** Anzeigemodus des Screens. */
    private enum Mode {
        /** Weltenliste. */
        LIST,
        /** Lösch-Bestätigung. */
        CONFIRM_DELETE,
        /** Umbenenn-Dialog. */
        RENAME,
        /** Migrations-Hinweis (normale Welt). */
        CONFIRM_MIGRATION,
        /** Migrations-Warnung (Backup-Welt). */
        CONFIRM_BACKUP_MIGRATION,
        /** Anzeige eines Migrationsfehlers. */
        MIGRATION_ERROR
    }

    /** GUI-Tilesheet-Skalierung. */
    private static final int GUI_SCALE = 21;
    /** Maximale Anzahl sichtbarer Welten. */
    private static final int MAX_VISIBLE = 5;
    /** Halbierungsfaktor. */
    private static final float HALF = 2.0f;

    // --- Liste ---
    /** Y-Position des Titels. */
    private static final float TITLE_Y = 222f;
    /** Skalierung des Titels. */
    private static final float TITLE_SCALE = 2.0f;
    /** Y-Position des ersten Eintrags. */
    private static final float ENTRY_Y_START = 140f;
    /** Vertikaler Abstand zwischen Einträgen. */
    private static final float ENTRY_STEP = 70f;
    /** X-Mittelpunkt des Play-Buttons. */
    private static final float ENTRY_X = -70f;
    /** Halbe Breite des Play-Buttons. */
    private static final float ENTRY_W = 190f;
    /** Halbe Höhe der Eintrags-Buttons. */
    private static final float ENTRY_H = 22f;
    /** X-Mittelpunkt des Umbenenn-Buttons. */
    private static final float RENAME_BTN_X = 160f;
    /** Halbe Breite des Umbenenn-Buttons. */
    private static final float RENAME_W = 30f;
    /** X-Mittelpunkt des Lösch-Buttons. */
    private static final float DEL_X = 230f;
    /** Halbe Breite des Lösch-Buttons. */
    private static final float DEL_W = 30f;
    /** Y-Offset der Metadaten-Zeile unter dem Eintrag. */
    private static final float META_Y_OFFSET = 28f;
    /** Skalierung der Metadaten-Zeile. */
    private static final float META_SCALE = 1.5f;
    /** Y-Position der Aktions-Buttons (New World, Back). */
    private static final float ACTION_Y = -230f;
    /** X-Position des New-World-Buttons. */
    private static final float NEW_X = -115f;
    /** X-Position des Back-Buttons. */
    private static final float BACK_X = 115f;
    /** Halbe Breite der Aktions-Buttons. */
    private static final float ACTION_W = 100f;
    /** Halbe Höhe der Aktions-Buttons. */
    private static final float ACTION_H = 22f;
    /** Maximale Anzeigelänge eines Weltnamens. */
    private static final int MAX_DISPLAY_CHARS = TextField.MAX_LENGTH;
    /** Länge des Abschneide-Suffix ("..."). */
    private static final int TRUNCATE_SUFFIX_LEN = 3;

    // --- Dialog-Buttons (Bestätigung/Migration) ---
    /** X-Position des linken Dialog-Buttons. */
    private static final float DIALOG_LEFT_X = -115f;
    /** X-Position des rechten Dialog-Buttons. */
    private static final float DIALOG_RIGHT_X = 115f;
    /** Halbe Breite der Dialog-Buttons. */
    private static final float DIALOG_BTN_W = 100f;
    /** Halbe Höhe der Dialog-Buttons. */
    private static final float DIALOG_BTN_H = 22f;

    // --- Lösch-Dialog ---
    /** Y-Position der Lösch-Frage. */
    private static final float DELETE_QUESTION_Y = 60f;
    /** Y-Position des Lösch-Hinweises. */
    private static final float DELETE_HINT_Y = 20f;
    /** Y-Position der Lösch-Buttons. */
    private static final float DELETE_BTN_Y = -50f;
    /** Skalierung der Lösch-Frage. */
    private static final float DELETE_SCALE = 2.2f;
    /** Skalierung des Lösch-Hinweises. */
    private static final float DELETE_HINT_SCALE = 1.6f;

    // --- Migration / Backup-Migration ---
    /** Y-Position des Migrationstitels. */
    private static final float MIGRATE_TITLE_Y = 90f;
    /** Y-Position der ersten Hinweiszeile. */
    private static final float MIGRATE_LINE1_Y = 35f;
    /** Y-Position der zweiten Hinweiszeile. */
    private static final float MIGRATE_LINE2_Y = -5f;
    /** Y-Position der Migrations-Buttons. */
    private static final float MIGRATE_BTN_Y = -80f;
    /** Skalierung der Migrationstitel. */
    private static final float MIGRATE_TITLE_SCALE = 2.2f;
    /** Skalierung der Hinweiszeilen. */
    private static final float MIGRATE_LINE_SCALE = 1.5f;

    // --- Migrationsfehler ---
    /** Y-Position des Fehlertitels. */
    private static final float ERROR_TITLE_Y = 60f;
    /** Y-Position der Fehlermeldung. */
    private static final float ERROR_MSG_Y = 10f;
    /** Y-Position des Zurück-Buttons. */
    private static final float ERROR_BTN_Y = -70f;
    /** Skalierung des Fehlertitels. */
    private static final float ERROR_TITLE_SCALE = 2.2f;
    /** Skalierung der Fehlermeldung. */
    private static final float ERROR_MSG_SCALE = 1.4f;

    // --- Umbenennen ---
    /** Y-Position des Umbenenn-Titels. */
    private static final float RENAME_TITLE_Y = 110f;
    /** Y-Position des Untertitels. */
    private static final float RENAME_SUBTITLE_Y = 60f;
    /** Halbe Breite des Eingabefelds. */
    private static final float RENAME_INPUT_HW = 150f;
    /** Halbe Höhe des Eingabefelds. */
    private static final float RENAME_INPUT_HH = 22f;
    /** Y-Position der Umbenenn-Fehlermeldung. */
    private static final float RENAME_ERROR_Y = -55f;
    /** Y-Position der Umbenenn-Aktions-Buttons. */
    private static final float RENAME_ACTION_Y = -120f;
    /** Skalierung des Umbenenn-Titels. */
    private static final float RENAME_TITLE_SCALE = 2.2f;
    /** Skalierung des Untertitels. */
    private static final float RENAME_SUBTITLE_SCALE = 1.6f;
    /** Skalierung der Umbenenn-Fehlermeldung. */
    private static final float RENAME_ERROR_SCALE = 1.8f;

    // --- Farben ---
    /** Weiß. */
    private static final float WHITE = 1.0f;
    /** Metadaten-Grau (normal). */
    private static final float META_NORMAL = 0.6f;
    /** Migrations-Akzent Grün. */
    private static final float ACCENT_MIGRATE_G = 0.8f;
    /** Migrations-Akzent Blau. */
    private static final float ACCENT_MIGRATE_B = 0.2f;
    /** Untertitel-/Hinweis-Grau hell. */
    private static final float GRAY_LIGHT = 0.7f;
    /** Hinweis-Grau mittel. */
    private static final float GRAY_MID = 0.75f;
    /** Hinweis-Grau dunkel. */
    private static final float GRAY_DARK = 0.65f;
    /** Fehler-/Lösch-Akzent Grün/Blau. */
    private static final float DANGER_GB = 0.4f;
    /** Fehlerfarbe Grün/Blau (kräftig). */
    private static final float ERROR_GB = 0.3f;
    /** Fehlermeldungs-Grau. */
    private static final float MSG_GRAY = 0.8f;
    /** Migrationstitel-Gelb Grün. */
    private static final float MIGRATE_TITLE_G = 0.85f;
    /** Migrationstitel-Gelb Blau. */
    private static final float MIGRATE_TITLE_B = 0.3f;
    /** Backup-Warn-Orange Grün. */
    private static final float BACKUP_TITLE_G = 0.75f;
    /** Backup-Warn-Orange Blau. */
    private static final float BACKUP_TITLE_B = 0.2f;

    // --- Stift-Icon ---
    /** Pixel-Code: Körper. */
    private static final int PIX_BODY = 1;
    /** Pixel-Code: Radierer. */
    private static final int PIX_ERASER = 2;
    /** Pixel-Code: Spitze. */
    private static final int PIX_TIP = 3;
    /** Kantenlänge eines Icon-Pixels in GUI-Einheiten. */
    private static final float PENCIL_PX = 3f;
    /** Halbe Kantenlänge eines Icon-Pixels. */
    private static final float PENCIL_HALF = PENCIL_PX / HALF;
    /** Körperfarbe Grün. */
    private static final float PENCIL_BODY_G = 0.85f;
    /** Körperfarbe Blau. */
    private static final float PENCIL_BODY_B = 0.2f;
    /** Radiererfarbe Grün. */
    private static final float PENCIL_ERASER_G = 0.55f;
    /** Radiererfarbe Blau. */
    private static final float PENCIL_ERASER_B = 0.60f;
    /** Spitzenfarbe (Grauwert). */
    private static final float PENCIL_TIP = 0.25f;
    /** Stift-Pixel-Matrix (0=leer, 1=Körper, 2=Radierer, 3=Spitze). */
    private static final int[][] PENCIL_ICON = {
        {0, 0, 0, 0, 0, 2, 2},
        {0, 0, 0, 0, 2, 1, 0},
        {0, 0, 0, 1, 1, 0, 0},
        {0, 0, 1, 1, 0, 0, 0},
        {0, 1, 1, 0, 0, 0, 0},
        {3, 1, 0, 0, 0, 0, 0},
        {3, 0, 0, 0, 0, 0, 0},
    };

    /** Datumsformat der Letztspiel-Anzeige. */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private GuiContext gui;
    private PxGuiInput guiInput;
    /** Aktueller Anzeigemodus. */
    private Mode mode = Mode.LIST;
    /** Welt, auf die sich der aktuelle Dialog bezieht (null in der Liste). */
    private WorldMeta pending;
    /** Aktuelle Migrations-Fehlermeldung (null = keiner). */
    private String migrationError;
    /** Aktuell sichtbare Welten. */
    private List<WorldMeta> worlds = new ArrayList<>();
    /** Eingabefeld des Umbenenn-Dialogs (null außerhalb von RENAME). */
    private TextField renameInput;
    /** Fehlermeldung des Umbenenn-Dialogs. */
    private String renameError = "";

    public WorldSelectGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    /**
     * Setzt eine Migrations-Fehlermeldung und schaltet in den Fehlermodus.
     * Wird vom {@link PxGameStateManager} vor dem Statewechsel aufgerufen.
     *
     * @param message die anzuzeigende Fehlermeldung
     */
    public void setMigrationError(final String message) {
        this.migrationError = message;
        this.mode = Mode.MIGRATION_ERROR;
    }

    @Override
    protected void onInit(final Window newWindow) {
        final PxGuiRenderer renderer = new PxGuiRenderer(newWindow, "menustate", GUI_SCALE);
        this.gui = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(newWindow, renderer);
        this.pending = null;
        this.renameError = "";
        rescan();
        if (migrationError == null) {
            this.mode = Mode.LIST;
        }
    }

    @Override
    protected void onResize() {
        gui.resize(window.getWidth(), window.getHeight());
    }

    @Override
    protected void onUpdate(final double delta) {
        gui.interact(guiInput, delta, this::buildGui);
    }

    @Override
    protected void onRender(final float alpha) { }

    @Override
    protected void onRenderGui(final float alpha) {
        gui.render(guiInput, this::buildGui);
    }

    private void buildGui(final GuiContext g) {
        switch (mode) {
            case LIST: buildList(g); break;
            case CONFIRM_DELETE: buildDeleteConfirm(g); break;
            case RENAME: buildRename(g); break;
            case CONFIRM_MIGRATION: buildMigration(g); break;
            case CONFIRM_BACKUP_MIGRATION: buildBackupMigration(g); break;
            case MIGRATION_ERROR: buildError(g); break;
            default: break;
        }
    }

    private void buildList(final GuiContext g) {
        g.label("Select World", 0, TITLE_Y, TITLE_SCALE, WHITE, WHITE, WHITE);

        if (worlds.isEmpty()) {
            g.label("No worlds found.", 0, ENTRY_Y_START, META_SCALE, GRAY_LIGHT, GRAY_LIGHT, GRAY_LIGHT);
        }

        final int count = Math.min(worlds.size(), MAX_VISIBLE);
        for (int i = 0; i < count; i++) {
            final WorldMeta meta = worlds.get(i);
            final float y = ENTRY_Y_START - i * ENTRY_STEP;

            if (g.button(truncate(meta.name()), ENTRY_X, y, ENTRY_W, ENTRY_H)) {
                onPlayWorld(meta);
            }
            if (g.button("", RENAME_BTN_X, y, RENAME_W, ENTRY_H)) {
                enterRename(meta);
            }
            if (g.button("X", DEL_X, y, DEL_W, ENTRY_H)) {
                pending = meta;
                mode = Mode.CONFIRM_DELETE;
            }

            drawMetaLine(g, meta, y - META_Y_OFFSET);
            drawPencil(g, RENAME_BTN_X, y);
        }

        if (g.button("New World", NEW_X, ACTION_Y, ACTION_W, ACTION_H)) {
            gsm.setCurrentState(PxGameStates.WORLD_NEW);
        }
        if (g.button("Back", BACK_X, ACTION_Y, ACTION_W, ACTION_H)) {
            gsm.setCurrentState(PxGameStates.MENU);
        }
    }

    private void buildDeleteConfirm(final GuiContext g) {
        g.label("Delete \"" + pending.name() + "\"?", 0, DELETE_QUESTION_Y, DELETE_SCALE,
                WHITE, DANGER_GB, DANGER_GB);
        g.label("This cannot be undone.", 0, DELETE_HINT_Y, DELETE_HINT_SCALE,
                GRAY_DARK, GRAY_DARK, GRAY_DARK);
        if (g.button("Yes, Delete", DIALOG_LEFT_X, DELETE_BTN_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            onDeleteConfirm();
        }
        if (g.button("Cancel", DIALOG_RIGHT_X, DELETE_BTN_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            cancelToList();
        }
    }

    private void buildRename(final GuiContext g) {
        g.label("Rename World", 0, RENAME_TITLE_Y, RENAME_TITLE_SCALE, WHITE, WHITE, WHITE);
        g.label("\"" + pending.name() + "\"", 0, RENAME_SUBTITLE_Y, RENAME_SUBTITLE_SCALE,
                GRAY_LIGHT, GRAY_LIGHT, GRAY_LIGHT);

        if (g.textField(renameInput)) {
            onRenameConfirm();
        }

        if (!renameError.isEmpty()) {
            g.label(renameError, 0, RENAME_ERROR_Y, RENAME_ERROR_SCALE, WHITE, ERROR_GB, ERROR_GB);
        }

        if (g.button("Rename", DIALOG_LEFT_X, RENAME_ACTION_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            onRenameConfirm();
        }
        if (g.button("Cancel", DIALOG_RIGHT_X, RENAME_ACTION_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            cancelToList();
        }
    }

    private void buildMigration(final GuiContext g) {
        g.label("World Update Required", 0, MIGRATE_TITLE_Y, MIGRATE_TITLE_SCALE,
                WHITE, MIGRATE_TITLE_G, MIGRATE_TITLE_B);
        g.label("A backup will be created as '" + pending.name() + "_backup'.",
                0, MIGRATE_LINE1_Y, MIGRATE_LINE_SCALE, GRAY_MID, GRAY_MID, GRAY_MID);
        g.label("To revert, open the backup world instead.",
                0, MIGRATE_LINE2_Y, MIGRATE_LINE_SCALE, GRAY_MID, GRAY_MID, GRAY_MID);
        if (g.button("Update", DIALOG_LEFT_X, MIGRATE_BTN_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            onMigrationConfirm();
        }
        if (g.button("Cancel", DIALOG_RIGHT_X, MIGRATE_BTN_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            cancelToList();
        }
    }

    private void buildBackupMigration(final GuiContext g) {
        g.label("Backup World", 0, MIGRATE_TITLE_Y, MIGRATE_TITLE_SCALE,
                WHITE, BACKUP_TITLE_G, BACKUP_TITLE_B);
        g.label("This is a backup. No backup will be created before migration.",
                0, MIGRATE_LINE1_Y, MIGRATE_LINE_SCALE, GRAY_MID, GRAY_MID, GRAY_MID);
        g.label("To keep a backup, rename this world and remove '_backup'.",
                0, MIGRATE_LINE2_Y, MIGRATE_LINE_SCALE, GRAY_MID, GRAY_MID, GRAY_MID);
        if (g.button("Continue", DIALOG_LEFT_X, MIGRATE_BTN_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            onBackupMigrationConfirm();
        }
        if (g.button("Cancel", DIALOG_RIGHT_X, MIGRATE_BTN_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            cancelToList();
        }
    }

    private void buildError(final GuiContext g) {
        g.label("Migration Failed", 0, ERROR_TITLE_Y, ERROR_TITLE_SCALE, WHITE, ERROR_GB, ERROR_GB);
        g.label(migrationError, 0, ERROR_MSG_Y, ERROR_MSG_SCALE, MSG_GRAY, MSG_GRAY, MSG_GRAY);
        if (g.button("Back", 0, ERROR_BTN_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            migrationError = null;
            mode = Mode.LIST;
        }
    }

    private void drawMetaLine(final GuiContext g, final WorldMeta meta, final float y) {
        final String datePart = DATE_FMT.format(Instant.ofEpochMilli(meta.lastPlayed()));
        final String versionPart;
        if (meta.gameVersion() != null) {
            versionPart = "  v" + meta.gameVersion();
        } else {
            versionPart = "  (legacy)";
        }
        final String updatePart;
        if (meta.needsMigration()) {
            updatePart = "  [! Update]";
        } else {
            updatePart = "";
        }

        final float r;
        final float gg;
        final float b;
        if (meta.needsMigration()) {
            r = WHITE;
            gg = ACCENT_MIGRATE_G;
            b = ACCENT_MIGRATE_B;
        } else {
            r = META_NORMAL;
            gg = META_NORMAL;
            b = META_NORMAL;
        }
        g.label(datePart + versionPart + updatePart, ENTRY_X, y, META_SCALE, r, gg, b);
    }

    private void drawPencil(final GuiContext g, final float cx, final float cy) {
        final int rows = PENCIL_ICON.length;
        final int cols = PENCIL_ICON[0].length;
        final float totalW = cols * PENCIL_PX;
        final float totalH = rows * PENCIL_PX;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final int pixel = PENCIL_ICON[row][col];
                if (pixel == 0) {
                    continue;
                }
                final float px = cx - totalW / HALF + PENCIL_HALF + col * PENCIL_PX;
                final float py = cy + totalH / HALF - PENCIL_HALF - row * PENCIL_PX;

                final float r;
                final float gg;
                final float b;
                if (pixel == PIX_BODY) {
                    r = WHITE;
                    gg = PENCIL_BODY_G;
                    b = PENCIL_BODY_B;
                } else if (pixel == PIX_ERASER) {
                    r = WHITE;
                    gg = PENCIL_ERASER_G;
                    b = PENCIL_ERASER_B;
                } else {
                    r = PENCIL_TIP;
                    gg = PENCIL_TIP;
                    b = PENCIL_TIP;
                }
                g.quad(px, py, PENCIL_HALF, PENCIL_HALF, r, gg, b);
            }
        }
    }

    private void onPlayWorld(final WorldMeta meta) {
        if (meta.needsMigration() && meta.name().endsWith("_backup")) {
            pending = meta;
            mode = Mode.CONFIRM_BACKUP_MIGRATION;
            return;
        }
        if (meta.needsMigration()) {
            pending = meta;
            mode = Mode.CONFIRM_MIGRATION;
            return;
        }
        gsm.setLevelState(meta.name());
    }

    private void onMigrationConfirm() {
        runMigration(pending);
    }

    private void onBackupMigrationConfirm() {
        runMigration(pending);
    }

    private void runMigration(final WorldMeta meta) {
        if (meta == null) {
            return;
        }
        try {
            MigrationRunner.run(meta);
            final String name = meta.name();
            pending = null;
            gsm.setLevelState(name);
        } catch (final Exception e) {
            LOGGER.error("Migration fehlgeschlagen für '{}'", meta.name(), e);
            migrationError = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            pending = null;
            mode = Mode.MIGRATION_ERROR;
        }
    }

    private void onDeleteConfirm() {
        if (pending != null) {
            final File file = new File(PxWorlds.WORLD_DIRECTORY, pending.name() + ".pxwl");
            if (file.exists() && !file.delete()) {
                LOGGER.error("Konnte Weltdatei nicht löschen: {}", file.getAbsolutePath());
            } else {
                LOGGER.info("Welt '{}' gelöscht.", pending.name());
            }
            pending = null;
        }
        rescan();
        mode = Mode.LIST;
    }

    private void enterRename(final WorldMeta meta) {
        pending = meta;
        renameError = "";
        renameInput = new TextField(0, 0, RENAME_INPUT_HW, RENAME_INPUT_HH);
        renameInput.setValue(meta.name());
        mode = Mode.RENAME;
    }

    private void onRenameConfirm() {
        final String newName = renameInput.getValue().trim();

        if (newName.isEmpty()) {
            renameError = "Name must not be empty.";
            return;
        }
        if (newName.equals(pending.name())) {
            cancelToList();
            return;
        }

        final File target = new File(PxWorlds.WORLD_DIRECTORY, newName + ".pxwl");
        if (target.exists()) {
            renameError = "A world with this name already exists.";
            return;
        }

        final File source = new File(PxWorlds.WORLD_DIRECTORY, pending.name() + ".pxwl");
        if (!source.renameTo(target)) {
            LOGGER.error("Konnte Weltdatei nicht umbenennen: {} → {}", source, target);
            renameError = "Rename failed.";
            return;
        }

        LOGGER.info("Welt '{}' umbenannt zu '{}'.", pending.name(), newName);
        pending = null;
        rescan();
        mode = Mode.LIST;
    }

    private void cancelToList() {
        pending = null;
        renameError = "";
        mode = Mode.LIST;
    }

    private void rescan() {
        final List<WorldMeta> result = new ArrayList<>();
        final File dir = new File(PxWorlds.WORLD_DIRECTORY);
        if (dir.isDirectory()) {
            final File[] files = dir.listFiles((d, name) -> name.endsWith(".pxwl"));
            if (files != null) {
                for (final File f : files) {
                    WorldMetaReader.read(f).ifPresent(result::add);
                }
            }
        }
        result.sort(Comparator.comparingLong(WorldMeta::lastPlayed).reversed());
        this.worlds = result;
    }

    private static String truncate(final String name) {
        if (name.length() <= MAX_DISPLAY_CHARS) {
            return name;
        }
        return name.substring(0, MAX_DISPLAY_CHARS - TRUNCATE_SUFFIX_LEN) + "...";
    }
}
