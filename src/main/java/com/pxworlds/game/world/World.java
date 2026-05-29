package com.pxworlds.game.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.pxworlds.Constants;
import com.pxworlds.game.collision.AABB;
import com.pxworlds.game.entity.Entity;
import com.pxworlds.game.entity.Player;
import com.pxworlds.game.entity.Transform;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.TextureAtlas;
import com.pxworlds.game.rendering.tiles.Tile;
import com.pxworlds.game.rendering.tiles.TileRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Spielwelt — verwaltet Tiles (Chunk-Grid), Kollisions-AABBs und Entities.
 *
 * <h3>Tile-Speicherung</h3>
 * Tiles liegen in einem {@code Chunk[][]}-Grid. Jeder Chunk ist
 * {@value Constants#CHUNK_SIZE}×{@value Constants#CHUNK_SIZE} Tiles groß
 * und besitzt einen eigenen OpenGL-VBO. {@link #setTile} markiert den
 * betroffenen Chunk als dirty und benachrichtigt alle {@link WorldListener}.
 *
 * <h3>Kollision</h3>
 * Für Kollisionsabfragen existiert weiterhin ein flaches {@code AABB[]}-Array
 * (global indexiert als {@code x + y * width}). Das Array liegt parallel
 * zum Chunk-Grid und wird bei {@link #setTile} synchron aktualisiert.
 *
 * <h3>Rendering</h3>
 * {@link #render} iteriert nur die sichtbaren Chunks (Viewport-Culling),
 * baut bei Bedarf ihren VBO neu und rendert sie in einem einzigen
 * {@code glDrawArrays}-Call pro Chunk.
 */
public class World {

    // -------------------------------------------------------------------------
    // Konstanten

    /** Tile-Kacheln links/rechts/oben/unten außerhalb des berechneten Viewports rendern. */
    static final int VIEW_OFFSET   = 4;
    static final int HALF_DIVISOR  = 2;
    static final int RENDER_OFFSET = 1;

    /** AABB-Halb-Ausdehnung für solid Tiles (= TILE_SIZE / 2). */
    static final float BOUNDING_BOX_SIZE = 1.0f;
    /** Dicke der unsichtbaren Grenz-Wände an den Welträndern (in World-Space-Einheiten). */
    private static final float BOUNDARY_THICKNESS = 8.0f;

    /** Sentinel-Wert für „kein Spawn gesetzt". */
    public static final int NO_SPAWN = -1;

    // -------------------------------------------------------------------------
    // Felder

    /** Breite/Höhe der Welt in Tiles. */
    private int width;
    private int height;
    /** World-Scale (Pixel-Multiplikator). */
    private int scale;

    /** Chunk-Grid [chunkX][chunkY]. */
    private Chunk[][] chunks;
    /** Anzahl Chunks horizontal / vertikal. */
    private int chunkCountX;
    private int chunkCountY;

    // kein boundingBoxes-Array mehr — AABBs werden in getCollidingBoxes on-the-fly erzeugt

    /** Alle Entities in der Welt. */
    private List<Entity> entities;

    /** Listener die bei Tile-Änderungen benachrichtigt werden. */
    private final List<WorldListener> listeners = new ArrayList<>();

    /** Viewportgröße in Tiles — berechnet in {@link #calculateView}. */
    private int viewX;
    private int viewY;

    /** World-Transform-Matrix (Skalierung um {@link #scale}). */
    private Matrix4f world;

    // Scratch-Objekte — vermeiden Heap-Allokationen im Hot-Path
    private final List<AABB>   scratchColliders = new ArrayList<>();
    /** Reusable array for getDebugLines() — avoids per-frame String[] allocation. */
    private final String[]     debugLines       = new String[3];

    /**
     * Unsichtbare Grenz-Wände an den vier Welträndern —
     * verhindern, dass Entities die Welt verlassen.
     * Werden einmalig in {@link #initBoundaries()} erzeugt.
     */
    private AABB boundaryLeft;
    private AABB boundaryRight;
    private AABB boundaryTop;
    private AABB boundaryBottom;

    /** Spawn-Punkt und Letztposition des Spielers. */
    private final PlayerSpawnState spawnState = new PlayerSpawnState();

    // Debug-Statistiken — aktualisiert in render()
    private int lastRenderedChunks = 0;
    private int lastRebuiltChunks  = 0;

    // -------------------------------------------------------------------------
    // Konstruktoren

    /**
     * Erzeugt eine leere Welt mit den angegebenen Dimensionen.
     * Intern verwendet von {@link WorldIO} zum Aufbau einer geladenen Welt.
     *
     * @param newWidth   Weltbreite in Tiles
     * @param newHeight  Welthöhe in Tiles
     * @param newScale   World-Scale (Pixel-Multiplikator)
     */
    World(final int newWidth, final int newHeight, final int newScale) {
        this.width  = newWidth;
        this.height = newHeight;
        this.scale  = newScale;
        initChunks();
        initWorld();
    }

    // -------------------------------------------------------------------------
    // Initialisierung

    private void initChunks() {
        chunkCountX = (width  + Chunk.SIZE - 1) / Chunk.SIZE;
        chunkCountY = (height + Chunk.SIZE - 1) / Chunk.SIZE;
        chunks      = new Chunk[chunkCountX][chunkCountY];
        for (int cy = 0; cy < chunkCountY; cy++) {
            for (int cx = 0; cx < chunkCountX; cx++) {
                chunks[cx][cy] = new Chunk(cx, cy);
            }
        }
    }

    private void initWorld() {
        entities = new ArrayList<>();
        world         = new Matrix4f().setTranslation(new Vector3f(0));
        world.scale(scale);
        initBoundaries();
    }

    /**
     * Baut die vier unsichtbaren Grenz-Wände auf, die Entities am Verlassen
     * der Welt hindern. Die Ränder entsprechen den äußeren Kanten der
     * Randtiles (d.h. dem Übergang Tile-AABB → Luft).
     *
     * <p>Koordinaten im Entity-World-Space (vor dem Scale-Matrix):
     * <ul>
     *   <li>linker  Rand: x = −{@value #BOUNDING_BOX_SIZE} (linke Kante von Tile 0)</li>
     *   <li>rechter Rand: x = width×{@value com.pxworlds.Constants#TILE_SIZE} − {@value #BOUNDING_BOX_SIZE}</li>
     *   <li>oberer  Rand: y = +{@value #BOUNDING_BOX_SIZE} (obere Kante von Tile 0)</li>
     *   <li>unterer Rand: y = −height×{@value com.pxworlds.Constants#TILE_SIZE} + {@value #BOUNDING_BOX_SIZE}</li>
     * </ul></p>
     */
    private void initBoundaries() {
        // Äußere Kanten des Welt-Rechtecks im Entity-Space
        final float left   = -BOUNDING_BOX_SIZE;
        final float right  =  width  * Constants.TILE_SIZE - BOUNDING_BOX_SIZE;
        final float top    =  BOUNDING_BOX_SIZE;
        final float bottom = -height * Constants.TILE_SIZE + BOUNDING_BOX_SIZE;

        boundaryLeft   = new AABB(left - BOUNDARY_THICKNESS, bottom, left,   top);
        boundaryRight  = new AABB(right, bottom, right + BOUNDARY_THICKNESS, top);
        boundaryTop    = new AABB(left, top,    right, top    + BOUNDARY_THICKNESS);
        boundaryBottom = new AABB(left, bottom  - BOUNDARY_THICKNESS, right, bottom);
    }

    // -------------------------------------------------------------------------
    // Listener

    public void addListener(final WorldListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final WorldListener listener) {
        listeners.remove(listener);
    }

    // -------------------------------------------------------------------------
    // Update

    public void calculateView(final Camera camera) {
        viewX = (camera.getVirtualWidth()  / (scale * HALF_DIVISOR)) + VIEW_OFFSET;
        viewY = (camera.getVirtualHeight() / (scale * HALF_DIVISOR)) + VIEW_OFFSET;
    }

    public void update(final float delta, final Window window, final Camera camera) {
        camera.savePrevPosition();
        for (final Entity entity : entities) {
            entity.tick(delta, window, camera, this);
        }
        // Entity-Entity Push-out nach allen Ticks
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                entities.get(i).collideWithEntity(entities.get(j));
            }
        }
        // Tile-Recheck nach dem Push-out: eine in eine Wand geschobene Entity
        // wird hier herauskorrigiert (clipX/clipY allein genügen nicht, weil
        // sie nur verhindernde, keine korrigierende Geometrie anwenden).
        for (final Entity entity : entities) {
            entity.resolveAgainstTiles(this);
        }
    }

    // -------------------------------------------------------------------------
    // Rendering

    /**
     * Rendert alle sichtbaren Chunks und anschließend alle Entities.
     *
     * @param tileRenderer  stellt Atlas und Batch-Shader bereit
     * @param entityShader  Shader für Entities (bestehender "shader")
     * @param cam           aktive Kamera
     * @param alpha         Interpolationsfaktor 0.0–1.0
     */
    public void render(final TileRenderer tileRenderer, final Shader entityShader,
                       final Camera cam, final float alpha) {

        final TextureAtlas atlas       = tileRenderer.getAtlas();
        final Shader       batchShader = tileRenderer.getBatchShader();

        // --- Sichtbaren Chunk-Bereich berechnen ---
        // posX/posY: Kamera-Offset in Tile-Koordinaten
        final int posX = (int) cam.getPosition().x / (scale * HALF_DIVISOR);
        final int posY = (int) cam.getPosition().y / (scale * HALF_DIVISOR);

        final int halfX = viewX / HALF_DIVISOR;
        final int halfY = viewY / HALF_DIVISOR;

        // Sichtbare Tile-Range (mit VIEW_OFFSET-Puffer)
        final int minTileX = -posX - halfX - 1;
        final int maxTileX = -posX + halfX + 1;
        final int minTileY =  posY - halfY - 1;
        final int maxTileY =  posY + halfY + 1;

        // In Chunk-Koordinaten umrechnen und auf Welt-Bounds klemmen
        final int minCX = Math.max(0,              Math.floorDiv(minTileX, Chunk.SIZE));
        final int maxCX = Math.min(chunkCountX - 1, Math.floorDiv(maxTileX, Chunk.SIZE));
        final int minCY = Math.max(0,              Math.floorDiv(minTileY, Chunk.SIZE));
        final int maxCY = Math.min(chunkCountY - 1, Math.floorDiv(maxTileY, Chunk.SIZE));

        // --- Dirty Chunks neu bauen und Statistik führen ---
        int rebuilt  = 0;
        int rendered = 0;
        for (int cy = minCY; cy <= maxCY; cy++) {
            for (int cx = minCX; cx <= maxCX; cx++) {
                if (chunks[cx][cy].rebuildIfDirty(atlas)) {
                    rebuilt++;
                }
            }
        }

        // --- Chunks rendern (ein Draw-Call pro Chunk) ---
        final Matrix4f viewProj = cam.getProjection(alpha).mul(world);

        batchShader.bind();
        batchShader.setUniform("sampler", 0);
        batchShader.setUniform("projection", viewProj);
        atlas.bind(0);

        for (int cy = minCY; cy <= maxCY; cy++) {
            for (int cx = minCX; cx <= maxCX; cx++) {
                chunks[cx][cy].render();
                rendered++;
            }
        }

        lastRenderedChunks = rendered;
        lastRebuiltChunks  = rebuilt;

        // --- Entities rendern (bestehende Per-Entity-Methode) ---
        for (final Entity entity : entities) {
            entity.render(entityShader, cam, this, alpha);
        }
    }

    public void correctCamera(final Camera camera) {
        final Vector3f pos = camera.getPosition();
        final int w = -width  * scale * HALF_DIVISOR;
        final int h =  height * scale * HALF_DIVISOR;

        final int halfVW = camera.getVirtualWidth()  / HALF_DIVISOR;
        final int halfVH = camera.getVirtualHeight() / HALF_DIVISOR;

        if (pos.x > -halfVW + scale) {
            pos.x = -halfVW + scale;
        }
        if (pos.x < w + halfVW + scale) {
            pos.x =  w + halfVW + scale;
        }
        if (pos.y < halfVH - scale) {
            pos.y = halfVH - scale;
        }
        if (pos.y > h - halfVH - scale) {
            pos.y = h - halfVH - scale;
        }
    }

    // -------------------------------------------------------------------------
    // Tile-Zugriff

    /**
     * Setzt ein Tile an der globalen Position und markiert den betroffenen Chunk als dirty.
     *
     * @param tile  das neue Tile oder {@code null} für Luft
     * @param x     globale X-Tile-Koordinate
     * @param y     globale Y-Tile-Koordinate
     */
    public void setTile(final Tile tile, final int x, final int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }

        // Chunk aktualisieren — AABB wird on-the-fly in getCollidingBoxes berechnet
        final int cx = x / Chunk.SIZE;
        final int cy = y / Chunk.SIZE;
        chunks[cx][cy].setTile(x % Chunk.SIZE, y % Chunk.SIZE, tile);

        // Listener benachrichtigen
        for (final WorldListener listener : listeners) {
            listener.tileChanged(x, y);
        }
    }

    /**
     * Gibt das Tile an der globalen Position zurück oder {@code null} für Luft
     * bzw. außerhalb der Welt.
     */
    public Tile getTile(final int x, final int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        final int cx = x / Chunk.SIZE;
        final int cy = y / Chunk.SIZE;
        final int lx = x % Chunk.SIZE;
        final int ly = y % Chunk.SIZE;
        return chunks[cx][cy].getTile(lx, ly);
    }

    /**
     * Berechnet die AABB eines soliden Tiles on-the-fly.
     *
     * @param x  globale X-Tile-Koordinate
     * @param y  globale Y-Tile-Koordinate
     * @return   berechnete AABB oder {@code null} wenn Luft / außerhalb der Welt
     */
    public AABB getTileBoundingBox(final int x, final int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        final Tile tile = getTile(x, y);
        if (tile == null || !tile.isSolid()) {
            return null;
        }
        final float bx = x * Constants.TILE_SIZE;
        final float by = -y * Constants.TILE_SIZE;
        return new AABB(bx - BOUNDING_BOX_SIZE, by - BOUNDING_BOX_SIZE,
                        bx + BOUNDING_BOX_SIZE, by + BOUNDING_BOX_SIZE);
    }

    /**
     * Liefert alle AABB von soliden Tiles die {@code area} überschneiden.
     *
     * <p>Die AABBs werden on-the-fly berechnet — kein paralleles AABB-Array nötig.
     * Das zurückgegebene Scratch-Objekt darf nicht gecacht werden und ist nach
     * dem nächsten Aufruf ungültig.</p>
     *
     * @param area  Suchbereich
     * @return      Referenz auf {@code scratchColliders} (nie null, nicht cachen!)
     */
    public List<AABB> getCollidingBoxes(final AABB area) {
        scratchColliders.clear();

        // Weltgrenz-Wände — verhindern, dass Entities den Welt-Bereich verlassen
        if (area.minX < boundaryLeft.maxX)   { scratchColliders.add(boundaryLeft);   }
        if (area.maxX > boundaryRight.minX)  { scratchColliders.add(boundaryRight);  }
        if (area.maxY > boundaryTop.minY)    { scratchColliders.add(boundaryTop);    }
        if (area.minY < boundaryBottom.maxY) { scratchColliders.add(boundaryBottom); }

        final int x0 = (int) Math.floor( area.minX / Constants.TILE_SIZE) - 1;
        final int x1 = (int) Math.ceil(  area.maxX / Constants.TILE_SIZE) + 1;
        final int y0 = (int) Math.floor(-area.maxY / Constants.TILE_SIZE) - 1;
        final int y1 = (int) Math.ceil( -area.minY / Constants.TILE_SIZE) + 1;

        for (int y = Math.max(0, y0); y <= Math.min(height - 1, y1); y++) {
            for (int x = Math.max(0, x0); x <= Math.min(width - 1, x1); x++) {
                final Tile tile = getTile(x, y);
                if (tile == null || !tile.isSolid()) {
                    continue;
                }
                final float bx  = x * Constants.TILE_SIZE;
                final float by  = -y * Constants.TILE_SIZE;
                final float mnX = bx - BOUNDING_BOX_SIZE;
                final float mnY = by - BOUNDING_BOX_SIZE;
                final float mxX = bx + BOUNDING_BOX_SIZE;
                final float mxY = by + BOUNDING_BOX_SIZE;
                // Inline-Intersection-Check verhindert AABB-Allokation für Nicht-Treffer
                if (mxX > area.minX && mnX < area.maxX && mxY > area.minY && mnY < area.maxY) {
                    scratchColliders.add(new AABB(mnX, mnY, mxX, mxY));
                }
            }
        }
        return scratchColliders;
    }

    // -------------------------------------------------------------------------
    // Getter

    public Matrix4f getWorldMatrix() {
        return world;
    }

    /** @return Weltbreite in Tiles */
    public int getWidth()  { return width;  }
    /** @return Welthöhe in Tiles */
    public int getHeight() { return height; }
    /** @return World-Scale (Pixel-Multiplikator) */
    public int getScale()  { return scale;  }

    /** @return last viewport X-range in tiles (for {@link WorldDebugRenderer}) */
    int getViewX() { return viewX; }
    /** @return last viewport Y-range in tiles (for {@link WorldDebugRenderer}) */
    int getViewY() { return viewY; }
    /** @return number of chunks horizontally (for {@link WorldDebugRenderer}) */
    int getChunkCountX() { return chunkCountX; }
    /** @return number of chunks vertically (for {@link WorldDebugRenderer}) */
    int getChunkCountY() { return chunkCountY; }
    /** @return unmodifiable view of the entity list (for {@link WorldDebugRenderer}) */
    List<Entity> getEntities() { return Collections.unmodifiableList(entities); }

    /**
     * Liefert Debug-Zeilen über den aktuellen Welt-Zustand:
     * Spieler-Tile-Position, Chunk-Position, Chunk-Statistiken.
     *
     * <p>Die Tile-Position wird direkt aus der World-Space-Position des ersten
     * {@link Player}-Entity berechnet, nicht aus der Kamera, um Rundungs-Artefakte
     * durch den Kamera-Umweg zu vermeiden.
     * Ist kein Player vorhanden, wird {@code cam} als Fallback genutzt.</p>
     *
     * @param cam aktive Kamera (Fallback wenn kein Player-Entity gefunden)
     * @return Debug-Zeilen zur Anzeige im F3-Panel
     */
    public String[] getDebugLines(final Camera cam) {
        // Direkt aus der Entity-Position berechnen — kein Kamera-Umweg
        Entity playerEntity = null;
        for (final Entity e : entities) {
            if (e instanceof Player) {
                playerEntity = e;
                break;
            }
        }

        final int tileX;
        final int tileY;
        if (playerEntity != null) {
            final float wx = playerEntity.getPosition().x;
            final float wy = playerEntity.getPosition().y;
            tileX = (int) Math.floor( wx / Constants.TILE_SIZE);
            tileY = (int) Math.floor(-wy / Constants.TILE_SIZE);
        } else {
            // Fallback: Kamera-Mittelpunkt → Tile (identisch zur Render-Logik)
            tileX =  -(int) cam.getPosition().x / (scale * HALF_DIVISOR);
            tileY =   (int) cam.getPosition().y / (scale * HALF_DIVISOR);
        }

        final int chunkX = Math.floorDiv(tileX, Chunk.SIZE);
        final int chunkY = Math.floorDiv(tileY, Chunk.SIZE);

        debugLines[0] = "Tile:    " + tileX  + ", " + tileY;
        debugLines[1] = "Chunk:   " + chunkX + ", " + chunkY
                + "  (" + chunkCountX + "x" + chunkCountY + " total)";
        debugLines[2] = "Chunks:  " + lastRenderedChunks + " rendered, "
                + lastRebuiltChunks + " rebuilt";
        return debugLines;
    }

    /**
     * Markiert alle Chunks als dirty, sodass ihre VBOs beim nächsten Render-Aufruf
     * vollständig neu gebaut werden.
     *
     * <p>Muss aufgerufen werden, wenn der {@link com.pxworlds.game.rendering.TextureAtlas}
     * ausgetauscht wird (z.B. Ressourcen-Reload), da sich die UV-Koordinaten aller Tiles
     * ändern. Off-screen-Chunks haben {@code dirty=false} und würden sonst beim
     * Hereinrollen veraltete UVs rendern.</p>
     */
    public void markAllChunksDirty() {
        for (int cy = 0; cy < chunkCountY; cy++) {
            for (int cx = 0; cx < chunkCountX; cx++) {
                chunks[cx][cy].markDirty();
            }
        }
    }

    /** Stoppt die Render-Interpolation aller Entities (prevPos = pos). */
    public void snapAllEntities() {
        for (final Entity entity : entities) {
            entity.snapPrevPosition();
        }
    }

    /** Pausiert die Animationen aller Entities. */
    public void pauseAllAnimations() {
        for (final Entity entity : entities) {
            entity.pauseAnimations();
        }
    }

    /** Setzt die Animationen aller Entities fort. */
    public void resumeAllAnimations() {
        for (final Entity entity : entities) {
            entity.resumeAnimations();
        }
    }

    /** @return Spawn-Tile-X oder {@value #NO_SPAWN} wenn kein Spawn gesetzt. */
    public int getSpawnTileX() { return spawnState.getSpawnTileX(); }
    /** @return Spawn-Tile-Y oder {@value #NO_SPAWN} wenn kein Spawn gesetzt. */
    public int getSpawnTileY() { return spawnState.getSpawnTileY(); }

    /**
     * Setzt den Spawn-Punkt direkt (in Tile-Koordinaten).
     * Wird von {@link WorldIO} nach dem Laden einer Version-2/3-Datei aufgerufen.
     */
    public void setSpawn(final int tileX, final int tileY) {
        spawnState.setSpawn(tileX, tileY);
    }

    /** @return letzte gespeicherte X-Position oder {@link Float#NaN} wenn nicht gesetzt. */
    public float getLastSavedPosX() { return spawnState.getLastSavedPosX(); }
    /** @return letzte gespeicherte Y-Position oder {@link Float#NaN} wenn nicht gesetzt. */
    public float getLastSavedPosY() { return spawnState.getLastSavedPosY(); }
    /** @return {@code true} wenn eine Letztposition gespeichert wurde. */
    public boolean hasLastPos() { return spawnState.hasLastPos(); }

    /**
     * Setzt die Letztposition direkt (in Entity-World-Space-Koordinaten).
     * Wird von {@link WorldIO} nach dem Laden einer Version-3-Datei aufgerufen.
     */
    public void setLastPos(final float x, final float y) {
        spawnState.setLastPos(x, y);
    }

    /**
     * Liest die aktuelle Position des ersten {@link Player}-Entity und
     * speichert sie als Letztposition. Muss vor {@link WorldIO#save} aufgerufen werden.
     *
     * <p>Hat keinen Effekt wenn kein Player-Entity gefunden wird.</p>
     */
    public void capturePlayerPosition() {
        for (final Entity e : entities) {
            if (e instanceof Player) {
                spawnState.setLastPos(e.getPosition().x, e.getPosition().y);
                return;
            }
        }
    }

    /**
     * Erzeugt den Spieler und setzt die Kamera.
     *
     * <p>Priorität der Startposition:
     * <ol>
     *   <li>Letztposition — wenn gesetzt</li>
     *   <li>Spawn-Tile — als Fallback</li>
     * </ol>
     * Hat keinen Effekt wenn weder Letztposition noch Spawn-Tile bekannt sind.</p>
     *
     * @param camera    wird auf die Startposition gesetzt
     * @param playable  ob ein spielbarer Player erzeugt werden soll
     */
    public void spawnPlayer(final Camera camera, final boolean playable) {
        final float posX;
        final float posY;
        if (spawnState.hasLastPos()) {
            posX = spawnState.getLastSavedPosX();
            posY = spawnState.getLastSavedPosY();
        } else if (spawnState.getSpawnTileX() != NO_SPAWN && spawnState.getSpawnTileY() != NO_SPAWN) {
            posX =  spawnState.getSpawnTileX() * Constants.TILE_SIZE;
            posY = -spawnState.getSpawnTileY() * Constants.TILE_SIZE;
        } else {
            return; // keine Position bekannt
        }
        final Transform transform = new Transform();
        transform.pos.x = posX;
        transform.pos.y = posY;
        if (playable) {
            entities.add(new Player(transform));
        }
        camera.getPosition().set(transform.pos.mul(-scale, new Vector3f()));
    }

    /** Gibt alle OpenGL-Ressourcen der Chunks frei. Muss im GL-Thread aufgerufen werden. */
    public void close() {
        for (int cy = 0; cy < chunkCountY; cy++) {
            for (int cx = 0; cx < chunkCountX; cx++) {
                chunks[cx][cy].close();
            }
        }
    }
}
