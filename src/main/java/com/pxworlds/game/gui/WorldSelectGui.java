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

    public static final int MAX_VISIBLE = 5;
    private static final int MAX_DISPLAY_CHARS = TextInput.MAX_LENGTH;

    private static final String GUI_TILESHEET  = "menustate";
    private static final int    TILESHEET_TILES = 21;

    // --- Normaler Modus ---
    private static final float TITLE_Y       = 210f;
    private static final float TITLE_SCALE   = 2.0f;
    private static final float ENTRY_Y_START = 140f;
    private static final float ENTRY_STEP    = 70f;
    private static final float ENTRY_W       = 190f;  // half-width Play-Button
    private static final float ENTRY_H       = 22f;
    private static final float GAP           = 10f;
    private static final float RENAME_W      = 30f;   // half-width Ren-Button
    private static final float DEL_W         = 30f;   // half-width DEL-Button
    // Play + Ren + DEL zentriert: 2*190 + 10 + 2*30 + 10 + 2*30 = 520, half = 260
    private static final float ENTRY_X       = -70f;  // Mittelpunkt Play (= -260 + 190)
    private static final float RENAME_BTN_X  = 160f;  // Mittelpunkt Ren  (= 130 + 30)
    private static final float DEL_X         = 230f;  // Mittelpunkt DEL  (= 200 + 30)
    private static final float META_Y_OFFSET = 28f;
    private static final float META_SCALE    = 1.5f;
    private static final float ACTION_Y      = -230f;
    private static final float NEW_X         = -115f;
    private static final float BACK_X        =  115f;
    private static final float ACTION_W      = 100f;
    private static final float ACTION_H      = 22f;

    // --- Bestätigungsmodus ---
    private static final float CONFIRM_QUESTION_Y = 60f;
    private static final float CONFIRM_HINT_Y     = 20f;
    private static final float CONFIRM_YES_X      = -115f;
    private static final float CONFIRM_NO_X       =  115f;
    private static final float CONFIRM_BTN_Y      = -50f;
    private static final float CONFIRM_BTN_W      = 100f;
    private static final float CONFIRM_BTN_H      = 22f;
    private static final float CONFIRM_SCALE      = 2.2f;
    private static final float CONFIRM_HINT_SCALE = 1.6f;

    // --- Migrations-Fehlermodus ---
    private static final float ERROR_TITLE_Y  = 60f;
    private static final float ERROR_MSG_Y    = 10f;
    private static final float ERROR_BTN_Y    = -70f;
    private static final float ERROR_TITLE_SCALE = 2.2f;
    private static final float ERROR_MSG_SCALE   = 1.4f;

    // --- Migrations-Hinweismodus (normale Welt) ---
    private static final float MIGRATE_TITLE_Y  = 90f;
    private static final float MIGRATE_LINE1_Y  = 35f;
    private static final float MIGRATE_LINE2_Y  = -5f;
    private static final float MIGRATE_BTN_Y    = -80f;
    private static final float MIGRATE_TITLE_SCALE = 2.2f;
    private static final float MIGRATE_LINE_SCALE  = 1.5f;

    // --- Backup-Migrations-Warnmodus ---
    private static final float BACKUP_WARN_TITLE_Y  = 90f;
    private static final float BACKUP_WARN_LINE1_Y  = 35f;
    private static final float BACKUP_WARN_LINE2_Y  = -5f;
    private static final float BACKUP_WARN_BTN_Y    = -80f;
    private static final float BACKUP_WARN_TITLE_SCALE = 2.2f;
    private static final float BACKUP_WARN_LINE_SCALE  = 1.5f;

    // --- Umbenenn-Modus ---
    private static final float RENAME_TITLE_Y        = 110f;
    private static final float RENAME_SUBTITLE_Y     = 60f;
    private static final float RENAME_INPUT_Y        = 0f;
    private static final float RENAME_INPUT_HW       = 150f;
    private static final float RENAME_INPUT_HH       = 22f;
    private static final float RENAME_ERROR_Y        = -55f;
    private static final float RENAME_OK_X           = -115f;
    private static final float RENAME_CANCEL_X       =  115f;
    private static final float RENAME_ACTION_Y       = -120f;
    private static final float RENAME_ACTION_W       = 100f;
    private static final float RENAME_ACTION_H       = 22f;
    private static final float RENAME_TITLE_SCALE    = 2.2f;
    private static final float RENAME_SUBTITLE_SCALE = 1.6f;
    private static final float RENAME_ERROR_SCALE    = 1.8f;

    // --- Stift-Icon (Pixel-Art, Tip links-unten, Radierer rechts-oben) ---
    // 0=transparent, 1=Körper (gelb), 2=Radierer (rosa), 3=Spitze (dunkel)
    private static final int    PENCIL_PX   = 3;
    private static final int[][] PENCIL_ICON = {
        { 0, 0, 0, 0, 0, 2, 2 },
        { 0, 0, 0, 0, 2, 1, 0 },
        { 0, 0, 0, 1, 1, 0, 0 },
        { 0, 0, 1, 1, 0, 0, 0 },
        { 0, 1, 1, 0, 0, 0, 0 },
        { 3, 1, 0, 0, 0, 0, 0 },
        { 3, 0, 0, 0, 0, 0, 0 },
    };

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final WorldMeta pendingDelete;
    private final WorldMeta pendingRename;
    private final WorldMeta pendingMigration;
    private final WorldMeta pendingBackupMigration;
    private final String migrationError;
    private final List<WorldMeta> visibleMetas = new ArrayList<>();
    private final TextInput renameInput;
    private String renameError = "";

    public WorldSelectGui(
            final Window window,
            final List<WorldMeta> worlds,
            final WorldMeta pendingDelete,
            final WorldMeta pendingRename,
            final WorldMeta pendingMigration,
            final WorldMeta pendingBackupMigration,
            final String migrationError,
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
        this.pendingDelete = pendingDelete;
        this.pendingRename = pendingRename;
        this.pendingMigration = pendingMigration;
        this.pendingBackupMigration = pendingBackupMigration;
        this.migrationError = migrationError;

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
        return renameInput != null ? renameInput.getValue() : "";
    }

    /** Setzt die Fehlermeldung im Umbenenn-Modus (leer = keine Anzeige). */
    public void setRenameError(final String message) {
        this.renameError = message == null ? "" : message;
    }

    @Override
    public void render() {
        super.render();

        if (migrationError != null) {
            drawLabel("Migration Failed", 0, ERROR_TITLE_Y, ERROR_TITLE_SCALE, 1f, 0.3f, 0.3f);
            drawLabel(migrationError, 0, ERROR_MSG_Y, ERROR_MSG_SCALE, 0.8f, 0.8f, 0.8f);
            return;
        }

        if (pendingMigration != null) {
            drawLabel("World Update Required", 0, MIGRATE_TITLE_Y,
                    MIGRATE_TITLE_SCALE, 1.0f, 0.85f, 0.3f);
            drawLabel("A backup will be created as '" + pendingMigration.name() + "_backup'.",
                    0, MIGRATE_LINE1_Y, MIGRATE_LINE_SCALE, 0.75f, 0.75f, 0.75f);
            drawLabel("To revert, open the backup world instead.",
                    0, MIGRATE_LINE2_Y, MIGRATE_LINE_SCALE, 0.75f, 0.75f, 0.75f);
            return;
        }

        if (pendingBackupMigration != null) {
            drawLabel("Backup World", 0, BACKUP_WARN_TITLE_Y,
                    BACKUP_WARN_TITLE_SCALE, 1.0f, 0.75f, 0.2f);
            drawLabel("This is a backup. No backup will be created before migration.",
                    0, BACKUP_WARN_LINE1_Y, BACKUP_WARN_LINE_SCALE, 0.75f, 0.75f, 0.75f);
            drawLabel("To keep a backup, rename this world and remove '_backup'.",
                    0, BACKUP_WARN_LINE2_Y, BACKUP_WARN_LINE_SCALE, 0.75f, 0.75f, 0.75f);
            return;
        }

        if (pendingDelete != null) {
            drawLabel("Delete \"" + pendingDelete.name() + "\"?",
                    0, CONFIRM_QUESTION_Y, CONFIRM_SCALE, 1f, 0.4f, 0.4f);
            drawLabel("This cannot be undone.",
                    0, CONFIRM_HINT_Y, CONFIRM_HINT_SCALE, 0.65f, 0.65f, 0.65f);
            return;
        }

        if (pendingRename != null) {
            drawLabel("Rename World", 0, RENAME_TITLE_Y, RENAME_TITLE_SCALE, 1f, 1f, 1f);
            drawLabel("\"" + pendingRename.name() + "\"",
                    0, RENAME_SUBTITLE_Y, RENAME_SUBTITLE_SCALE, 0.7f, 0.7f, 0.7f);
            renameInput.render(camera);
            if (!renameError.isEmpty()) {
                drawLabel(renameError, 0, RENAME_ERROR_Y, RENAME_ERROR_SCALE, 1f, 0.3f, 0.3f);
            }
            return;
        }

        drawLabel("Select World", 0, TITLE_Y + 12, TITLE_SCALE, 1f, 1f, 1f);

        if (visibleMetas.isEmpty()) {
            drawLabel("No worlds found.", 0, ENTRY_Y_START, META_SCALE, 0.7f, 0.7f, 0.7f);
        }

        for (int i = 0; i < visibleMetas.size(); i++) {
            final WorldMeta meta  = visibleMetas.get(i);
            final float entryY    = ENTRY_Y_START - i * ENTRY_STEP;
            final float metaY     = entryY - META_Y_OFFSET;

            final String datePart    = DATE_FMT.format(Instant.ofEpochMilli(meta.lastPlayed()));
            final String versionPart = meta.gameVersion() != null ? "  v" + meta.gameVersion() : "  (legacy)";
            final String updatePart  = meta.needsMigration() ? "  [! Update]" : "";
            final String metaLine    = datePart + versionPart + updatePart;

            final float r = meta.needsMigration() ? 1.0f : 0.6f;
            final float g = meta.needsMigration() ? 0.8f : 0.6f;
            final float b = meta.needsMigration() ? 0.2f : 0.6f;
            drawLabel(metaLine, ENTRY_X, metaY, META_SCALE, r, g, b);
            drawPencilIcon(RENAME_BTN_X, entryY);
        }
    }

    private static String truncate(final String name) {
        if (name.length() <= MAX_DISPLAY_CHARS) {
            return name;
        }
        return name.substring(0, MAX_DISPLAY_CHARS - 3) + "...";
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
                if (pixel == 0) continue;
                switch (pixel) {
                    case 1 -> glColor3f(1.0f,  0.85f, 0.2f);   // Körper: Gelb
                    case 2 -> glColor3f(1.0f,  0.55f, 0.60f);  // Radierer: Rosa
                    case 3 -> glColor3f(0.25f, 0.25f, 0.25f);  // Spitze: Dunkel
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
