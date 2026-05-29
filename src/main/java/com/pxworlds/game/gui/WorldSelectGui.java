package com.pxworlds.game.gui;

import com.pxworlds.game.debug.DebugRenderer;
import com.pxworlds.game.io.InputProcessor;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.world.WorldMeta;
import org.joml.Vector2f;
import org.lwjgl.stb.STBEasyFont;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * GUI-Screen für die Weltenauswahl.
 *
 * <p>Normaler Modus: Liste aller Welten mit Metadaten, Play-, Ren- und DEL-Button pro Eintrag,
 * sowie "New World"- und "Back"-Buttons unten.</p>
 *
 * <p>Lösch-Bestätigungsmodus ({@code pendingDelete != null}): Bestätigungsdialog mit
 * "Yes, Delete" und "Cancel".</p>
 *
 * <p>Umbenenn-Modus ({@code pendingRename != null}): Textfeld für den neuen Namen
 * mit "Rename" und "Cancel".</p>
 */
public class WorldSelectGui extends Gui {

    /** Maximale Anzahl sichtbarer Welten in der Liste. */
    public static final int MAX_VISIBLE = 5;
    /** Maximale angezeigte Zeichenanzahl im Weltname-Button. */
    private static final int MAX_DISPLAY_CHARS = TextInput.MAX_LENGTH;

    /** Tilesheet-Name für das GUI. */
    private static final String GUI_TILESHEET  = "menustate";
    /** Anzahl Tiles im Tilesheet. */
    private static final int    TILESHEET_TILES = 21;

    // --- Normaler Modus ---
    /** Y-Position des Titels. */
    private static final float TITLE_Y       = 210f;
    /** Skalierung des Titels. */
    private static final float TITLE_SCALE   = 2.0f;
    /** Y-Position des ersten Listeneintrags. */
    private static final float ENTRY_Y_START = 140f;
    /** Vertikaler Abstand zwischen Listeneinträgen. */
    private static final float ENTRY_STEP    = 70f;
    /** Halbe Breite des Play-Buttons. */
    private static final float ENTRY_W       = 190f;
    /** Halbe Höhe der Eintrags-Buttons. */
    private static final float ENTRY_H       = 22f;
    /** Lücke zwischen Buttons. */
    private static final float GAP           = 10f;
    /** Halbe Breite des Ren-Buttons. */
    private static final float RENAME_W      = 30f;
    /** Halbe Breite des DEL-Buttons. */
    private static final float DEL_W         = 30f;
    /** X-Mittelpunkt des Play-Buttons. */
    private static final float ENTRY_X       = -70f;
    /** X-Mittelpunkt des Ren-Buttons. */
    private static final float RENAME_BTN_X  = 160f;
    /** X-Mittelpunkt des DEL-Buttons. */
    private static final float DEL_X         = 230f;
    /** Y-Offset der Metadaten-Zeile unter dem Eintrag. */
    private static final float META_Y_OFFSET = 28f;
    /** Skalierung der Metadaten-Zeile. */
    private static final float META_SCALE    = 1.5f;
    /** Y-Position der Aktions-Buttons (New World, Back). */
    private static final float ACTION_Y      = -230f;
    /** X-Position des New-World-Buttons. */
    private static final float NEW_X         = -115f;
    /** X-Position des Back-Buttons. */
    private static final float BACK_X        =  115f;
    /** Halbe Breite der Aktions-Buttons. */
    private static final float ACTION_W      = 100f;
    /** Halbe Höhe der Aktions-Buttons. */
    private static final float ACTION_H      = 22f;

    // --- Bestätigungsmodus ---
    /** Y-Position der Bestätigungsfrage. */
    private static final float CONFIRM_QUESTION_Y = 60f;
    /** Y-Position des Hinweistexts. */
    private static final float CONFIRM_HINT_Y     = 20f;
    /** X-Position des Ja-Buttons. */
    private static final float CONFIRM_YES_X      = -115f;
    /** X-Position des Nein-Buttons. */
    private static final float CONFIRM_NO_X       =  115f;
    /** Y-Position der Bestätigungs-Buttons. */
    private static final float CONFIRM_BTN_Y      = -50f;
    /** Halbe Breite der Bestätigungs-Buttons. */
    private static final float CONFIRM_BTN_W      = 100f;
    /** Halbe Höhe der Bestätigungs-Buttons. */
    private static final float CONFIRM_BTN_H      = 22f;
    /** Skalierung des Bestätigungstexts. */
    private static final float CONFIRM_SCALE      = 2.2f;
    /** Skalierung des Hinweistexts. */
    private static final float CONFIRM_HINT_SCALE = 1.6f;

    // --- Migrations-Fehlermodus ---
    /** Y-Position des Fehlertitels. */
    private static final float ERROR_TITLE_Y  = 60f;
    /** Y-Position der Fehlermeldung. */
    private static final float ERROR_MSG_Y    = 10f;
    /** Y-Position des Zurück-Buttons. */
    private static final float ERROR_BTN_Y    = -70f;
    /** Skalierung des Fehlertitels. */
    private static final float ERROR_TITLE_SCALE = 2.2f;
    /** Skalierung der Fehlermeldung. */
    private static final float ERROR_MSG_SCALE   = 1.4f;

    // --- Migrations-Hinweismodus (normale Welt) ---
    /** Y-Position des Migrationstitels. */
    private static final float MIGRATE_TITLE_Y  = 90f;
    /** Y-Position der ersten Hinweiszeile. */
    private static final float MIGRATE_LINE1_Y  = 35f;
    /** Y-Position der zweiten Hinweiszeile. */
    private static final float MIGRATE_LINE2_Y  = -5f;
    /** Y-Position des Migrations-Buttons. */
    private static final float MIGRATE_BTN_Y    = -80f;
    /** Skalierung des Migrationstitels. */
    private static final float MIGRATE_TITLE_SCALE = 2.2f;
    /** Skalierung der Hinweiszeilen. */
    private static final float MIGRATE_LINE_SCALE  = 1.5f;

    // --- Backup-Migrations-Warnmodus ---
    /** Y-Position des Backup-Warnungstitels. */
    private static final float BACKUP_WARN_TITLE_Y  = 90f;
    /** Y-Position der ersten Warnzeile. */
    private static final float BACKUP_WARN_LINE1_Y  = 35f;
    /** Y-Position der zweiten Warnzeile. */
    private static final float BACKUP_WARN_LINE2_Y  = -5f;
    /** Y-Position der Backup-Warn-Buttons. */
    private static final float BACKUP_WARN_BTN_Y    = -80f;
    /** Skalierung des Backup-Warn-Titels. */
    private static final float BACKUP_WARN_TITLE_SCALE = 2.2f;
    /** Skalierung der Backup-Warnzeilen. */
    private static final float BACKUP_WARN_LINE_SCALE  = 1.5f;

    // --- Umbenenn-Modus ---
    /** Y-Position des Umbenenn-Titels. */
    private static final float RENAME_TITLE_Y        = 110f;
    /** Y-Position des Untertitels. */
    private static final float RENAME_SUBTITLE_Y     = 60f;
    /** Y-Position des Eingabefelds. */
    private static final float RENAME_INPUT_Y        = 0f;
    /** Halbe Breite des Eingabefelds. */
    private static final float RENAME_INPUT_HW       = 150f;
    /** Halbe Höhe des Eingabefelds. */
    private static final float RENAME_INPUT_HH       = 22f;
    /** Y-Position der Umbenenn-Fehlermeldung. */
    private static final float RENAME_ERROR_Y        = -55f;
    /** X-Position des Rename-Buttons. */
    private static final float RENAME_OK_X           = -115f;
    /** X-Position des Cancel-Buttons. */
    private static final float RENAME_CANCEL_X       =  115f;
    /** Y-Position der Umbenenn-Aktions-Buttons. */
    private static final float RENAME_ACTION_Y       = -120f;
    /** Halbe Breite der Umbenenn-Aktions-Buttons. */
    private static final float RENAME_ACTION_W       = 100f;
    /** Halbe Höhe der Umbenenn-Aktions-Buttons. */
    private static final float RENAME_ACTION_H       = 22f;
    /** Skalierung des Umbenenn-Titels. */
    private static final float RENAME_TITLE_SCALE    = 2.2f;
    /** Skalierung des Umbenenn-Untertitels. */
    private static final float RENAME_SUBTITLE_SCALE = 1.6f;
    /** Skalierung der Umbenenn-Fehlermeldung. */
    private static final float RENAME_ERROR_SCALE    = 1.8f;

    // --- Stift-Icon (Pixel-Art, Tip links-unten, Radierer rechts-oben) ---
    // 0=transparent, 1=Körper (gelb), 2=Radierer (rosa), 3=Spitze (dunkel)
    /** Pixel-Größe jedes Icon-Pixels in Bildschirmkoordinaten. */
    private static final int    PENCIL_PX       = 3;
    /** Pixel-Wert im PENCIL_ICON-Array für die Stift-Spitze. */
    private static final int    PENCIL_IDX_TIP  = 3;
    /** Stift-Icon-Pixel-Matrix. */
    private static final int[][] PENCIL_ICON = {
        { 0, 0, 0, 0, 0, 2, 2 },
        { 0, 0, 0, 0, 2, 1, 0 },
        { 0, 0, 0, 1, 1, 0, 0 },
        { 0, 0, 1, 1, 0, 0, 0 },
        { 0, 1, 1, 0, 0, 0, 0 },
        { 3, 1, 0, 0, 0, 0, 0 },
        { 3, 0, 0, 0, 0, 0, 0 },
    };

    // --- Render-Farben ---
    /** Fehlerfarbe Rot (Titelzeile). */
    private static final float COLOR_ERROR_R = 1.0f;
    /** Fehlerfarbe Grün (Titelzeile). */
    private static final float COLOR_ERROR_G = 0.3f;
    /** Fehlertext-Grau (Nachrichtenzeile). */
    private static final float COLOR_MSG_GRAY = 0.8f;
    /** Migrationstitel-Gelb Grün. */
    private static final float COLOR_MIGRATE_G = 0.85f;
    /** Migrationstitel-Gelb Blau. */
    private static final float COLOR_MIGRATE_B = 0.3f;
    /** Textfarbe Grau für Hinweise. */
    private static final float COLOR_TEXT_GRAY = 0.75f;
    /** Backup-Warn-Orange Grün. */
    private static final float COLOR_BACKUP_WARN_G = 0.75f;
    /** Backup-Warn-Orange Blau. */
    private static final float COLOR_BACKUP_WARN_B = 0.2f;
    /** Lösch-Rot Grün/Blau. */
    private static final float COLOR_DELETE_GB = 0.4f;
    /** Hinweis-Grau Wert. */
    private static final float COLOR_HINT_GRAY = 0.65f;
    /** Untertitel-Grau. */
    private static final float COLOR_SUBTITLE_GRAY = 0.7f;
    /** Metadaten-Farbe Normal (grau). */
    private static final float COLOR_META_NORMAL = 0.6f;
    /** Migrations-Meta-Farbe Grün. */
    private static final float COLOR_META_MIGRATION_G = 0.8f;
    /** Migrations-Meta-Farbe Blau. */
    private static final float COLOR_META_MIGRATION_B = 0.2f;
    /** Stift-Körperfarbe Grün (gelb). */
    private static final float PENCIL_BODY_G = 0.85f;
    /** Stift-Körperfarbe Blau (gelb). */
    private static final float PENCIL_BODY_B = 0.2f;
    /** Stift-Radiererfarbe Grün (rosa). */
    private static final float PENCIL_ERASER_G = 0.55f;
    /** Stift-Radiererfarbe Blau (rosa). */
    private static final float PENCIL_ERASER_B = 0.60f;
    /** Stift-Spitzenfarbe (dunkelgrau). */
    private static final float PENCIL_TIP_COLOR = 0.25f;
    /** Y-Offset des Listentitels. */
    private static final float TITLE_Y_OFFSET = 12f;
    /** Anzahl Zeichen des Abschneidungs-Suffix ("..."). */
    private static final int TRUNCATE_SUFFIX_LEN = 3;

    /** Datumsformat für Letztspiel-Anzeige. */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    /** Welt, deren Löschung bestätigt werden muss (null = kein Lösch-Dialog). */
    private final WorldMeta pendingDelete;
    /** Welt, die umbenannt werden soll (null = kein Umbenenn-Dialog). */
    private final WorldMeta pendingRename;
    /** Welt, die migriert werden soll (null = kein Migrations-Dialog). */
    private final WorldMeta pendingMigration;
    /** Backup-Welt, die migriert werden soll (null = kein Backup-Migrations-Dialog). */
    private final WorldMeta pendingBackupMigration;
    /** Fehlermeldung der letzten Migration (null = kein Fehler). */
    private final String migrationError;
    /** Sichtbare Welteinträge in der aktuellen Liste. */
    private final List<WorldMeta> visibleMetas = new ArrayList<>();
    /** Texteingabefeld für den Umbenenn-Modus. */
    private final TextInput renameInput;
    /** Aktuell angezeigte Umbenenn-Fehlermeldung. */
    private String renameError = "";

    public WorldSelectGui(
            final Window window,
            final List<WorldMeta> worlds,
            final WorldMeta newPendingDelete,
            final WorldMeta newPendingRename,
            final WorldMeta newPendingMigration,
            final WorldMeta newPendingBackupMigration,
            final String newMigrationError,
            final Consumer<WorldMeta> onPlay,
            final Consumer<WorldMeta> onDeleteRequest,
            final Consumer<WorldMeta> onRenameRequest,
            final Runnable onNewWorld,
            final Runnable onBack,
            final Runnable onDeleteConfirm,
            final Runnable onDeleteCancel,
            final Runnable onRenameConfirm,
            final Runnable onRenameCancel,
            final Runnable onMigrationConfirm,
            final Runnable onMigrationCancel,
            final Runnable onBackupMigrationConfirm,
            final Runnable onBackupMigrationCancel,
            final Runnable onMigrationErrorBack) {
        super(window, GUI_TILESHEET, TILESHEET_TILES);
        this.pendingDelete = newPendingDelete;
        this.pendingRename = newPendingRename;
        this.pendingMigration = newPendingMigration;
        this.pendingBackupMigration = newPendingBackupMigration;
        this.migrationError = newMigrationError;

        if (migrationError != null) {
            this.renameInput = null;
            registerButtons(
                    new Button(new Vector2f(0, ERROR_BTN_Y),
                            new Vector2f(CONFIRM_BTN_W, CONFIRM_BTN_H),
                            "Back", onMigrationErrorBack));
        } else if (pendingMigration != null) {
            this.renameInput = null;
            registerButtons(
                    new Button(new Vector2f(CONFIRM_YES_X, MIGRATE_BTN_Y),
                            new Vector2f(CONFIRM_BTN_W, CONFIRM_BTN_H),
                            "Update", onMigrationConfirm),
                    new Button(new Vector2f(CONFIRM_NO_X, MIGRATE_BTN_Y),
                            new Vector2f(CONFIRM_BTN_W, CONFIRM_BTN_H),
                            "Cancel", onMigrationCancel));
        } else if (pendingBackupMigration != null) {
            this.renameInput = null;
            registerButtons(
                    new Button(new Vector2f(CONFIRM_YES_X, BACKUP_WARN_BTN_Y),
                            new Vector2f(CONFIRM_BTN_W, CONFIRM_BTN_H),
                            "Continue", onBackupMigrationConfirm),
                    new Button(new Vector2f(CONFIRM_NO_X, BACKUP_WARN_BTN_Y),
                            new Vector2f(CONFIRM_BTN_W, CONFIRM_BTN_H),
                            "Cancel", onBackupMigrationCancel));
        } else if (pendingDelete != null) {
            this.renameInput = null;
            registerButtons(
                    new Button(new Vector2f(CONFIRM_YES_X, CONFIRM_BTN_Y),
                            new Vector2f(CONFIRM_BTN_W, CONFIRM_BTN_H),
                            "Yes, Delete", onDeleteConfirm),
                    new Button(new Vector2f(CONFIRM_NO_X, CONFIRM_BTN_Y),
                            new Vector2f(CONFIRM_BTN_W, CONFIRM_BTN_H),
                            "Cancel", onDeleteCancel));
        } else if (pendingRename != null) {
            this.renameInput = new TextInput(
                    new Vector2f(0, RENAME_INPUT_Y),
                    new Vector2f(RENAME_INPUT_HW, RENAME_INPUT_HH),
                    onRenameConfirm);
            this.renameInput.setValue(pendingRename.name());
            registerButtons(
                    new Button(new Vector2f(RENAME_OK_X, RENAME_ACTION_Y),
                            new Vector2f(RENAME_ACTION_W, RENAME_ACTION_H),
                            "Rename", onRenameConfirm),
                    new Button(new Vector2f(RENAME_CANCEL_X, RENAME_ACTION_Y),
                            new Vector2f(RENAME_ACTION_W, RENAME_ACTION_H),
                            "Cancel", onRenameCancel));
        } else {
            this.renameInput = null;
            final int count = Math.min(worlds.size(), MAX_VISIBLE);
            for (int i = 0; i < count; i++) {
                final WorldMeta meta = worlds.get(i);
                visibleMetas.add(meta);
                final float y = ENTRY_Y_START - i * ENTRY_STEP;
                final WorldMeta captured = meta;
                registerButtons(
                        new Button(new Vector2f(ENTRY_X, y), new Vector2f(ENTRY_W, ENTRY_H),
                                truncate(meta.name()), () -> onPlay.accept(captured)),
                        new Button(new Vector2f(RENAME_BTN_X, y), new Vector2f(RENAME_W, ENTRY_H),
                                "", () -> onRenameRequest.accept(captured)),
                        new Button(new Vector2f(DEL_X, y), new Vector2f(DEL_W, ENTRY_H),
                                "X", () -> onDeleteRequest.accept(captured)));
            }
            registerButtons(
                    new Button(new Vector2f(NEW_X, ACTION_Y), new Vector2f(ACTION_W, ACTION_H),
                            "New World", onNewWorld),
                    new Button(new Vector2f(BACK_X, ACTION_Y), new Vector2f(ACTION_W, ACTION_H),
                            "Back", onBack));
        }
    }

    @Override
    public void update(final InputProcessor input, final Vector2f mousePos,
                       final int windowWidth, final int windowHeight) {
        update(input, mousePos, windowWidth, windowHeight, 0.0);
    }

    public void update(final InputProcessor input, final Vector2f mousePos,
                       final int windowWidth, final int windowHeight, final double delta) {
        super.update(input, mousePos, windowWidth, windowHeight);
        if (renameInput != null) {
            renameInput.update(input, delta);
        }
    }

    /** @return aktueller Eingabetext im Umbenenn-Modus. */
    public String getNewName() {
        if (renameInput != null) {
            return renameInput.getValue();
        }
        return "";
    }

    /**
     * Setzt die Fehlermeldung im Umbenenn-Modus (leer = keine Anzeige).
     * @param message anzuzeigende Fehlermeldung oder {@code null}
     */
    public void setRenameError(final String message) {
        if (message == null) {
            this.renameError = "";
        } else {
            this.renameError = message;
        }
    }

    @Override
    public void render() {
        super.render();

        if (migrationError != null) {
            drawLabel("Migration Failed", 0, ERROR_TITLE_Y, ERROR_TITLE_SCALE, COLOR_ERROR_R, COLOR_ERROR_G, COLOR_ERROR_G);
            drawLabel(migrationError, 0, ERROR_MSG_Y, ERROR_MSG_SCALE, COLOR_MSG_GRAY, COLOR_MSG_GRAY, COLOR_MSG_GRAY);
            return;
        }

        if (pendingMigration != null) {
            drawLabel("World Update Required", 0, MIGRATE_TITLE_Y,
                    MIGRATE_TITLE_SCALE, 1.0f, COLOR_MIGRATE_G, COLOR_MIGRATE_B);
            drawLabel("A backup will be created as '" + pendingMigration.name() + "_backup'.",
                    0, MIGRATE_LINE1_Y, MIGRATE_LINE_SCALE, COLOR_TEXT_GRAY, COLOR_TEXT_GRAY, COLOR_TEXT_GRAY);
            drawLabel("To revert, open the backup world instead.",
                    0, MIGRATE_LINE2_Y, MIGRATE_LINE_SCALE, COLOR_TEXT_GRAY, COLOR_TEXT_GRAY, COLOR_TEXT_GRAY);
            return;
        }

        if (pendingBackupMigration != null) {
            drawLabel("Backup World", 0, BACKUP_WARN_TITLE_Y,
                    BACKUP_WARN_TITLE_SCALE, 1.0f, COLOR_BACKUP_WARN_G, COLOR_BACKUP_WARN_B);
            drawLabel("This is a backup. No backup will be created before migration.",
                    0, BACKUP_WARN_LINE1_Y, BACKUP_WARN_LINE_SCALE, COLOR_TEXT_GRAY, COLOR_TEXT_GRAY, COLOR_TEXT_GRAY);
            drawLabel("To keep a backup, rename this world and remove '_backup'.",
                    0, BACKUP_WARN_LINE2_Y, BACKUP_WARN_LINE_SCALE, COLOR_TEXT_GRAY, COLOR_TEXT_GRAY, COLOR_TEXT_GRAY);
            return;
        }

        if (pendingDelete != null) {
            drawLabel("Delete \"" + pendingDelete.name() + "\"?",
                    0, CONFIRM_QUESTION_Y, CONFIRM_SCALE, 1f, COLOR_DELETE_GB, COLOR_DELETE_GB);
            drawLabel("This cannot be undone.",
                    0, CONFIRM_HINT_Y, CONFIRM_HINT_SCALE, COLOR_HINT_GRAY, COLOR_HINT_GRAY, COLOR_HINT_GRAY);
            return;
        }

        if (pendingRename != null) {
            drawLabel("Rename World", 0, RENAME_TITLE_Y, RENAME_TITLE_SCALE, 1f, 1f, 1f);
            drawLabel("\"" + pendingRename.name() + "\"",
                    0, RENAME_SUBTITLE_Y, RENAME_SUBTITLE_SCALE, COLOR_SUBTITLE_GRAY, COLOR_SUBTITLE_GRAY, COLOR_SUBTITLE_GRAY);
            renameInput.render(camera);
            if (!renameError.isEmpty()) {
                drawLabel(renameError, 0, RENAME_ERROR_Y, RENAME_ERROR_SCALE, 1f, COLOR_ERROR_G, COLOR_ERROR_G);
            }
            return;
        }

        drawLabel("Select World", 0, TITLE_Y + TITLE_Y_OFFSET, TITLE_SCALE, 1f, 1f, 1f);

        if (visibleMetas.isEmpty()) {
            drawLabel("No worlds found.", 0, ENTRY_Y_START, META_SCALE,
                    COLOR_SUBTITLE_GRAY, COLOR_SUBTITLE_GRAY, COLOR_SUBTITLE_GRAY);
        }

        for (int i = 0; i < visibleMetas.size(); i++) {
            final WorldMeta meta  = visibleMetas.get(i);
            final float entryY    = ENTRY_Y_START - i * ENTRY_STEP;
            final float metaY     = entryY - META_Y_OFFSET;

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
            final String metaLine = datePart + versionPart + updatePart;

            final float r;
            final float g;
            final float b;
            if (meta.needsMigration()) {
                r = 1.0f;
                g = COLOR_META_MIGRATION_G;
                b = COLOR_META_MIGRATION_B;
            } else {
                r = COLOR_META_NORMAL;
                g = COLOR_META_NORMAL;
                b = COLOR_META_NORMAL;
            }
            drawLabel(metaLine, ENTRY_X, metaY, META_SCALE, r, g, b);
            drawPencilIcon(RENAME_BTN_X, entryY);
        }
    }

    private static String truncate(final String name) {
        if (name.length() <= MAX_DISPLAY_CHARS) {
            return name;
        }
        return name.substring(0, MAX_DISPLAY_CHARS - TRUNCATE_SUFFIX_LEN) + "...";
    }

    private void drawPencilIcon(final float guiX, final float guiY) {
        final int vw  = camera.getVirtualWidth();
        final int vh  = camera.getVirtualHeight();
        final int cols = PENCIL_ICON[0].length;
        final int rows = PENCIL_ICON.length;
        final float originX = guiX + vw / 2.0f - (cols * PENCIL_PX) / 2.0f;
        final float originY = -guiY + vh / 2.0f - (rows * PENCIL_PX) / 2.0f;

        glUseProgram(0);
        glDisable(GL_TEXTURE_2D);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, vw, vh, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glBegin(GL_QUADS);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final int pixel = PENCIL_ICON[row][col];
                if (pixel == 0) {
                    continue;
                }
                switch (pixel) {
                    case 1 -> glColor3f(1.0f, PENCIL_BODY_G, PENCIL_BODY_B);       // Körper: Gelb
                    case 2 -> glColor3f(1.0f, PENCIL_ERASER_G, PENCIL_ERASER_B);  // Radierer: Rosa
                    case PENCIL_IDX_TIP -> glColor3f(PENCIL_TIP_COLOR, PENCIL_TIP_COLOR, PENCIL_TIP_COLOR); // Spitze: Dunkel
                    default -> { /* unbekannter Pixel-Wert – überspringen */ }
                }
                final float x = originX + col * PENCIL_PX;
                final float y = originY + row * PENCIL_PX;
                glVertex2f(x,              y);
                glVertex2f(x + PENCIL_PX, y);
                glVertex2f(x + PENCIL_PX, y + PENCIL_PX);
                glVertex2f(x,              y + PENCIL_PX);
            }
        }
        glEnd();

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
    }

    private void drawLabel(final String text, final float guiX, final float guiY,
                           final float scale, final float r, final float g, final float b) {
        final int vw = camera.getVirtualWidth();
        final int vh = camera.getVirtualHeight();
        final float fontH = 7f * scale;
        final float textW = STBEasyFont.stb_easy_font_width(text) * scale;
        final float drawX = (guiX - textW / 2.0f + vw / 2.0f) / scale;
        final float drawY = (-guiY - fontH / 2.0f + vh / 2.0f) / scale;

        glUseProgram(0);
        glDisable(GL_TEXTURE_2D);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, vw, vh, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glScalef(scale, scale, 1f);
        glColor3f(r, g, b);
        DebugRenderer.drawString(text + " ", drawX, drawY);
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
    }
}
