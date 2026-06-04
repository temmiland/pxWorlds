package land.temmi.pxworlds.statemanagement.states;

import land.temmi.pxworlds.PxWorlds;
import land.temmi.pxworlds.gui.PxGuiInput;
import land.temmi.pxworlds.gui.PxGuiRenderer;
import land.temmi.pxworlds.gui.widgets.PxButtonSkin;
import land.temmi.pxworlds.gui.widgets.PxTextFieldSkin;
import land.temmi.pxworlds.tiles.tile.WorldTiles;
import land.temmi.rollercoaster.platform.input.Key;
import land.temmi.pxworlds.statemanagement.PxGameStateManager;
import land.temmi.rollercoaster.game.state.GameState;
import land.temmi.pxworlds.statemanagement.PxGameStates;
import land.temmi.pxworlds.world.WorldMeta;
import land.temmi.pxworlds.world.WorldMetaReader;
import land.temmi.pxworlds.world.migration.MigrationRunner;
import land.temmi.rollercoaster.gui.GuiContext;
import land.temmi.rollercoaster.gui.widgets.textfield.TextField;
import land.temmi.rollercoaster.platform.Window;
import org.joml.Vector2f;
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
 * World selection screen using an immediate-mode GUI.
 *
 * <p>The displayed content is determined by {@link #mode} (list, delete/rename/
 * migration dialogs, error display). Mode changes are plain field assignments —
 * no GUI is rebuilt.</p>
 */
public class WorldSelectGameState extends GameState<PxGameStateManager> {

    /** Logger of this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldSelectGameState.class);

    /** Display mode of the screen. */
    private enum Mode {
        /** World list. */
        LIST,
        /** Delete confirmation. */
        CONFIRM_DELETE,
        /** Rename dialog. */
        RENAME,
        /** Migration notice (normal world). */
        CONFIRM_MIGRATION,
        /** Migration warning (backup world). */
        CONFIRM_BACKUP_MIGRATION,
        /** Display of a migration error. */
        MIGRATION_ERROR
    }

    /** GUI tilesheet scale. */
    private static final int GUI_SCALE = 21;
    /** Maximum number of visible worlds. */
    private static final int MAX_VISIBLE = 5;
    /** Halving factor. */
    private static final float HALF = 2.0f;

    // --- List ---
    /** Y position of the title. */
    private static final float TITLE_Y = 222f;
    /** Scale of the title. */
    private static final float TITLE_SCALE = 2.0f;
    /** Y position of the first entry. */
    private static final float ENTRY_Y_START = 140f;
    /** Vertical spacing between entries. */
    private static final float ENTRY_STEP = 70f;
    /** X center of the play button. */
    private static final float ENTRY_X = -70f;
    /** Half width of the play button. */
    private static final float ENTRY_W = 190f;
    /** Half height of the entry buttons. */
    private static final float ENTRY_H = 22f;
    /** X center of the rename button. */
    private static final float RENAME_BTN_X = 160f;
    /** Half width of the rename button. */
    private static final float RENAME_W = 30f;
    /** X center of the delete button. */
    private static final float DEL_X = 230f;
    /** Half width of the delete button. */
    private static final float DEL_W = 30f;
    /** Y offset of the metadata line below an entry. */
    private static final float META_Y_OFFSET = 28f;
    /** Scale of the metadata line. */
    private static final float META_SCALE = 1.5f;
    /** Y position of the action buttons (New World, Back). */
    private static final float ACTION_Y = -230f;
    /** X position of the New World button. */
    private static final float NEW_X = -115f;
    /** X position of the Back button. */
    private static final float BACK_X = 115f;
    /** Half width of the action buttons. */
    private static final float ACTION_W = 100f;
    /** Half height of the action buttons. */
    private static final float ACTION_H = 22f;
    /** Maximum display length of a world name. */
    private static final int MAX_DISPLAY_CHARS = TextField.MAX_LENGTH;
    /** Length of the truncation suffix ("..."). */
    private static final int TRUNCATE_SUFFIX_LEN = 3;

    // --- Dialog buttons (confirmation/migration) ---
    /** X position of the left dialog button. */
    private static final float DIALOG_LEFT_X = -115f;
    /** X position of the right dialog button. */
    private static final float DIALOG_RIGHT_X = 115f;
    /** Half width of the dialog buttons. */
    private static final float DIALOG_BTN_W = 100f;
    /** Half height of the dialog buttons. */
    private static final float DIALOG_BTN_H = 22f;

    // --- Delete dialog ---
    /** Y position of the delete question. */
    private static final float DELETE_QUESTION_Y = 60f;
    /** Y position of the delete hint. */
    private static final float DELETE_HINT_Y = 20f;
    /** Y position of the delete buttons. */
    private static final float DELETE_BTN_Y = -50f;
    /** Scale of the delete question. */
    private static final float DELETE_SCALE = 2.2f;
    /** Scale of the delete hint. */
    private static final float DELETE_HINT_SCALE = 1.6f;

    // --- Migration / backup migration ---
    /** Y position of the migration title. */
    private static final float MIGRATE_TITLE_Y = 90f;
    /** Y position of the first hint line. */
    private static final float MIGRATE_LINE1_Y = 35f;
    /** Y position of the second hint line. */
    private static final float MIGRATE_LINE2_Y = -5f;
    /** Y position of the migration buttons. */
    private static final float MIGRATE_BTN_Y = -80f;
    /** Scale of the migration title. */
    private static final float MIGRATE_TITLE_SCALE = 2.2f;
    /** Scale of the hint lines. */
    private static final float MIGRATE_LINE_SCALE = 1.5f;

    // --- Migration error ---
    /** Y position of the error title. */
    private static final float ERROR_TITLE_Y = 60f;
    /** Y position of the error message. */
    private static final float ERROR_MSG_Y = 10f;
    /** Y position of the back button. */
    private static final float ERROR_BTN_Y = -70f;
    /** Scale of the error title. */
    private static final float ERROR_TITLE_SCALE = 2.2f;
    /** Scale of the error message. */
    private static final float ERROR_MSG_SCALE = 1.4f;

    // --- Scrollbar ---
    /** X position of the scrollbar. */
    private static final float SCROLLBAR_X = 300f;
    /** Y position of the scrollbar top (just below the title). */
    private static final float SCROLLBAR_Y_TOP = TITLE_Y - 50f;
    /** Y position of the scrollbar bottom (just above the action buttons). */
    private static final float SCROLLBAR_Y_BOT = ACTION_Y + 55f;
    /** Half width of the scrollbar. */
    private static final float SCROLLBAR_W = 6f;
    /** Half height of the scrollbar thumb. */
    private static final float SCROLLBAR_THUMB_H = 10.5f;
    /** Half width of the clickable/grabbable scrollbar area (generous for easy grabbing). */
    private static final float SCROLLBAR_HIT_W = 14f;

    // --- Rename dialog ---
    /** Y position of the rename title. */
    private static final float RENAME_TITLE_Y = 110f;
    /** Y position of the subtitle. */
    private static final float RENAME_SUBTITLE_Y = 60f;
    /** Half width of the input field. */
    private static final float RENAME_INPUT_HW = 150f;
    /** Half height of the input field. */
    private static final float RENAME_INPUT_HH = 22f;
    /** Y position of the rename error message. */
    private static final float RENAME_ERROR_Y = -55f;
    /** Y position of the rename action buttons. */
    private static final float RENAME_ACTION_Y = -120f;
    /** Scale of the rename title. */
    private static final float RENAME_TITLE_SCALE = 2.2f;
    /** Scale of the subtitle. */
    private static final float RENAME_SUBTITLE_SCALE = 1.6f;
    /** Scale of the rename error message. */
    private static final float RENAME_ERROR_SCALE = 1.8f;

    // --- Colors ---
    /** White. */
    private static final float WHITE = 1.0f;
    /** Metadata gray (normal). */
    private static final float META_NORMAL = 0.6f;
    /** Migration accent green. */
    private static final float ACCENT_MIGRATE_G = 0.8f;
    /** Migration accent blue. */
    private static final float ACCENT_MIGRATE_B = 0.2f;
    /** Subtitle/hint light gray. */
    private static final float GRAY_LIGHT = 0.7f;
    /** Hint mid gray. */
    private static final float GRAY_MID = 0.75f;
    /** Hint dark gray. */
    private static final float GRAY_DARK = 0.65f;
    /** Danger/delete accent green/blue. */
    private static final float DANGER_GB = 0.4f;
    /** Error color green/blue (vivid). */
    private static final float ERROR_GB = 0.3f;
    /** Error message gray. */
    private static final float MSG_GRAY = 0.8f;
    /** Migration title yellow green. */
    private static final float MIGRATE_TITLE_G = 0.85f;
    /** Migration title yellow blue. */
    private static final float MIGRATE_TITLE_B = 0.3f;
    /** Backup warning orange green. */
    private static final float BACKUP_TITLE_G = 0.75f;
    /** Backup warning orange blue. */
    private static final float BACKUP_TITLE_B = 0.2f;

    // --- Pencil icon ---
    /** Pixel code: body. */
    private static final int PIX_BODY = 1;
    /** Pixel code: eraser. */
    private static final int PIX_ERASER = 2;
    /** Pixel code: tip. */
    private static final int PIX_TIP = 3;
    /** Side length of one icon pixel in GUI units. */
    private static final float PENCIL_PX = 3f;
    /** Half side length of one icon pixel. */
    private static final float PENCIL_HALF = PENCIL_PX / HALF;
    /** Body color green. */
    private static final float PENCIL_BODY_G = 0.85f;
    /** Body color blue. */
    private static final float PENCIL_BODY_B = 0.2f;
    /** Eraser color green. */
    private static final float PENCIL_ERASER_G = 0.55f;
    /** Eraser color blue. */
    private static final float PENCIL_ERASER_B = 0.60f;
    /** Tip color (gray value). */
    private static final float PENCIL_TIP = 0.25f;
    /** Pencil pixel matrix (0=empty, 1=body, 2=eraser, 3=tip). */
    private static final int[][] PENCIL_ICON = {
        {0, 0, 0, 0, 0, 2, 2},
        {0, 0, 0, 0, 2, 1, 0},
        {0, 0, 0, 1, 1, 0, 0},
        {0, 0, 1, 1, 0, 0, 0},
        {0, 1, 1, 0, 0, 0, 0},
        {3, 1, 0, 0, 0, 0, 0},
        {3, 0, 0, 0, 0, 0, 0},
    };

    /** Date format for the last-played display. */
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    /** The immediate-mode GUI context. */
    private GuiContext gui;
    /** GUI input adapter. */
    private PxGuiInput guiInput;
    /** Current display mode. */
    private Mode mode = Mode.LIST;
    /** World that the current dialog refers to (null in the list). */
    private WorldMeta pending;
    /** Current migration error message (null = none). */
    private String migrationError;
    /** Currently visible worlds. */
    private List<WorldMeta> worlds = new ArrayList<>();
    /** Input field of the rename dialog (null outside of RENAME). */
    private TextField renameInput;
    /** Error message of the rename dialog. */
    private String renameError = "";
    /** Scroll offset for the world list. */
    private int scrollOffset = 0;
    /** Whether the scrollbar thumb is currently being dragged with the mouse. */
    private boolean draggingScrollbar = false;

    /**
     * Creates the world select state.
     *
     * @param gsm the game state manager
     */
    public WorldSelectGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    /**
     * Sets a migration error message and switches to the error mode.
     * Called by {@link PxGameStateManager} before the state transition.
     *
     * @param message the error message to display
     */
    public void setMigrationError(final String message) {
        this.migrationError = message;
        this.mode = Mode.MIGRATION_ERROR;
    }

    @Override
    protected void onInit(final Window window) {
        final PxGuiRenderer renderer = new PxGuiRenderer(window);
        this.gui = new GuiContext(renderer, PxButtonSkin.INSTANCE, PxTextFieldSkin.INSTANCE);
        this.guiInput = new PxGuiInput(window, renderer);
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
        if (mode == Mode.LIST && worlds.size() > MAX_VISIBLE) {
            final var input = window.getInputProcessor();
            if (input.wasPressed(Key.UP)) {
                scrollOffset = Math.min(worlds.size() - MAX_VISIBLE, scrollOffset + 1);
            }
            if (input.wasPressed(Key.DOWN)) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            }
            final double scrollY = input.getScrollY();
            if (scrollY > 0) {
                scrollOffset = Math.min(worlds.size() - MAX_VISIBLE, scrollOffset + 1);
            } else if (scrollY < 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            }
            updateScrollbarDrag();
        } else {
            draggingScrollbar = false;
        }
        gui.interact(guiInput, delta, this::buildGui);
    }

    /**
     * Handles dragging of the scrollbar thumb with the left mouse button.
     * The drag starts when the button is pressed over the thumb and continues
     * until the button is released, mapping the cursor's Y position onto the
     * scroll offset.
     */
    private void updateScrollbarDrag() {
        final Vector2f mouse = guiInput.mousePosition();

        final float trackHeight = SCROLLBAR_Y_TOP - SCROLLBAR_Y_BOT;
        final float thumbRatio = (float) MAX_VISIBLE / worlds.size();
        final float thumbHalfH = Math.max(SCROLLBAR_THUMB_H, trackHeight * thumbRatio / HALF);
        final float centerTop = SCROLLBAR_Y_TOP - thumbHalfH;
        final float centerBot = SCROLLBAR_Y_BOT + thumbHalfH;
        final float scrollProgress = (float) scrollOffset / (worlds.size() - MAX_VISIBLE);
        final float thumbY = centerTop + (centerBot - centerTop) * scrollProgress;

        final boolean onThumb = Math.abs(mouse.x - SCROLLBAR_X) <= SCROLLBAR_HIT_W
                && Math.abs(mouse.y - thumbY) <= thumbHalfH;
        if (guiInput.mousePressed() && onThumb) {
            draggingScrollbar = true;
        }
        if (!guiInput.mouseDown()) {
            draggingScrollbar = false;
        }

        if (draggingScrollbar) {
            final float progress = (centerTop - mouse.y) / (centerTop - centerBot);
            final float clamped = Math.max(0f, Math.min(1f, progress));
            scrollOffset = Math.round(clamped * (worlds.size() - MAX_VISIBLE));
        }
    }

    @Override
    protected void onRender(final float alpha) { }

    @Override
    protected void onRenderGui(final float alpha) {
        gui.render(guiInput, this::buildGui);
    }

    /**
     * Dispatches to the sub-builder matching the current mode.
     *
     * @param g the GUI context
     */
    private void buildGui(final GuiContext g) {
        g.background(WorldTiles.DARK_BRICK.getTexture());

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

    /**
     * Builds the world list view.
     *
     * @param g the GUI context
     */
    private void buildList(final GuiContext g) {
        g.label("Select World", 0, TITLE_Y, TITLE_SCALE, WHITE, WHITE, WHITE);

        if (worlds.isEmpty()) {
            g.label("No worlds found.", 0, ENTRY_Y_START, META_SCALE, GRAY_LIGHT, GRAY_LIGHT, GRAY_LIGHT);
        }

        final int count = Math.min(worlds.size() - scrollOffset, MAX_VISIBLE);
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, worlds.size() - MAX_VISIBLE)));

        for (int i = 0; i < count; i++) {
            final WorldMeta meta = worlds.get(scrollOffset + i);
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

        drawScrollbar(g);

        if (g.button("New World", NEW_X, ACTION_Y, ACTION_W, ACTION_H)) {
            gsm.setCurrentState(PxGameStates.WORLD_NEW);
        }
        if (g.button("Back", BACK_X, ACTION_Y, ACTION_W, ACTION_H)) {
            gsm.setCurrentState(PxGameStates.MENU);
        }
    }

    /**
     * Builds the delete confirmation dialog.
     *
     * @param g the GUI context
     */
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

    /**
     * Builds the rename dialog.
     *
     * @param g the GUI context
     */
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

    /**
     * Builds the migration confirmation dialog for a normal world.
     *
     * @param g the GUI context
     */
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

    /**
     * Builds the migration confirmation dialog for a backup world.
     *
     * @param g the GUI context
     */
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

    /**
     * Builds the migration error display.
     *
     * @param g the GUI context
     */
    private void buildError(final GuiContext g) {
        g.label("Migration Failed", 0, ERROR_TITLE_Y, ERROR_TITLE_SCALE, WHITE, ERROR_GB, ERROR_GB);
        g.label(migrationError, 0, ERROR_MSG_Y, ERROR_MSG_SCALE, MSG_GRAY, MSG_GRAY, MSG_GRAY);
        if (g.button("Back", 0, ERROR_BTN_Y, DIALOG_BTN_W, DIALOG_BTN_H)) {
            migrationError = null;
            mode = Mode.LIST;
        }
    }

    /**
     * Draws the metadata line (date, version, migration flag) below a world entry.
     *
     * @param g    the GUI context
     * @param meta the world metadata
     * @param y    the Y position
     */
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

    /**
     * Draws the pixel-art pencil icon centered on {@code (cx, cy)}.
     *
     * @param g  the GUI context
     * @param cx center X
     * @param cy center Y
     */
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

    /**
     * Handles a click on a world entry: opens a migration dialog if needed,
     * otherwise enters the level directly.
     *
     * @param meta the world metadata
     */
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

    /** Confirms and starts a migration for a normal world. */
    private void onMigrationConfirm() {
        runMigration(pending);
    }

    /** Confirms and starts a migration for a backup world. */
    private void onBackupMigrationConfirm() {
        runMigration(pending);
    }

    /**
     * Runs the migration for the given world and enters it on success, or
     * switches to the error screen on failure.
     *
     * @param meta the world to migrate
     */
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
            LOGGER.error("Migration failed for '{}'", meta.name(), e);
            final String msg = e.getMessage();
            if (msg != null) {
                migrationError = msg;
            } else {
                migrationError = e.getClass().getSimpleName();
            }
            pending = null;
            mode = Mode.MIGRATION_ERROR;
        }
    }

    /** Confirms and executes the deletion of the pending world. */
    private void onDeleteConfirm() {
        if (pending != null) {
            final File file = new File(PxWorlds.WORLD_DIRECTORY, pending.name() + ".pxwl");
            if (file.exists() && !file.delete()) {
                LOGGER.error("Could not delete world file: {}", file.getAbsolutePath());
            } else {
                LOGGER.info("World '{}' deleted.", pending.name());
            }
            pending = null;
        }
        rescan();
        mode = Mode.LIST;
    }

    /**
     * Opens the rename dialog for the given world.
     *
     * @param meta the world to rename
     */
    private void enterRename(final WorldMeta meta) {
        pending = meta;
        renameError = "";
        renameInput = new TextField(0, 0, RENAME_INPUT_HW, RENAME_INPUT_HH);
        renameInput.setValue(meta.name());
        mode = Mode.RENAME;
    }

    /** Validates the rename input and, if valid, renames the world file on disk. */
    private void onRenameConfirm() {
        final String name = renameInput.getValue().trim();

        if (name.isEmpty()) {
            renameError = "Name must not be empty.";
            return;
        }
        if (name.equals(pending.name())) {
            cancelToList();
            return;
        }

        final File target = new File(PxWorlds.WORLD_DIRECTORY, name + ".pxwl");
        if (target.exists()) {
            renameError = "A world with this name already exists.";
            return;
        }

        final File source = new File(PxWorlds.WORLD_DIRECTORY, pending.name() + ".pxwl");
        if (!source.renameTo(target)) {
            LOGGER.error("Could not rename world file: {} -> {}", source, target);
            renameError = "Rename failed.";
            return;
        }

        LOGGER.info("World '{}' renamed to '{}'.", pending.name(), name);
        pending = null;
        rescan();
        mode = Mode.LIST;
    }

    /** Cancels any pending dialog and returns to the world list. */
    private void cancelToList() {
        pending = null;
        renameError = "";
        scrollOffset = 0;
        mode = Mode.LIST;
    }

    /** Re-scans the worlds directory and refreshes the displayed list. */
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
        this.scrollOffset = 0;
    }

    /**
     * Truncates a world name to the maximum display length if needed.
     *
     * @param name the world name
     * @return the name, possibly truncated with "..."
     */
    private static String truncate(final String name) {
        if (name.length() <= MAX_DISPLAY_CHARS) {
            return name;
        }
        return name.substring(0, MAX_DISPLAY_CHARS - TRUNCATE_SUFFIX_LEN) + "...";
    }

    /**
     * Draws the scrollbar indicator on the right side of the world list.
     *
     * @param g the GUI context
     */
    private void drawScrollbar(final GuiContext g) {
        if (worlds.size() <= MAX_VISIBLE) {
            return;
        }

        final float trackHeight = SCROLLBAR_Y_TOP - SCROLLBAR_Y_BOT;
        final float thumbRatio = (float) MAX_VISIBLE / worlds.size();
        final float thumbHalfH = Math.max(SCROLLBAR_THUMB_H, trackHeight * thumbRatio / HALF);
        final float scrollProgress = (float) scrollOffset / (worlds.size() - MAX_VISIBLE);

        // Thumb top edge at SCROLLBAR_Y_TOP when at the start, bottom edge at
        // SCROLLBAR_Y_BOT when fully scrolled down. Y increases upward, so the
        // center travels downward as scrollProgress grows.
        final float centerTop = SCROLLBAR_Y_TOP - thumbHalfH;
        final float centerBot = SCROLLBAR_Y_BOT + thumbHalfH;
        final float thumbY = centerTop + (centerBot - centerTop) * scrollProgress;

        g.quad(SCROLLBAR_X, thumbY, SCROLLBAR_W, thumbHalfH, GRAY_LIGHT, GRAY_LIGHT, GRAY_LIGHT);
    }
}
