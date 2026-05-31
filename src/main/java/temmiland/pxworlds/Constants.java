package temmiland.pxworlds;

public class Constants {



    // --- Welt / Tiles ---
    /** Größe eines Tiles in Weltkoordinaten.
     *  Wird für Tile-Rendering (TileRenderer) und Entity/AABB-Positionierung (World) verwendet.
     *  Ändert man diesen Wert, skaliert die gesamte Welt konsistent. */
    public static final float TILE_SIZE = 2.0f;

    /** Anzahl Tiles pro Chunk-Seite (Chunks sind CHUNK_SIZE × CHUNK_SIZE Tiles).
     *  Wird vom Chunk-System und der prozeduralen Weltgenerierung verwendet. */
    public static final int CHUNK_SIZE = 16;

}
