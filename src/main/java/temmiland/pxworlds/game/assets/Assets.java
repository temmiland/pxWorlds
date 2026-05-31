package temmiland.pxworlds.game.assets;

import temmiland.pxworlds.game.rendering.Model;

public class Assets {
	/** Gemeinsames Quad-Modell. */
	private static Model model;

	// Quad-Geometrie-Konstanten
	/** Z-Tiefe des Quads. */
	private static final float Z_DEPTH = 0.0f;
	/** Vertices des Quads. */
	private static final float[] VERTICES = new float[] {
		-1f, 1f, Z_DEPTH, //OBEN LINKS   0
		1f, 1f, Z_DEPTH,  //OBEN RECHTS  1
		1f, -1f, Z_DEPTH, //UNTEN RECHTS 2
		-1f, -1f, Z_DEPTH,//UNTEN LINKS  3
	};

	/** Texturkoordinaten des Quads. */
	private static final float[] TEXTURE_COORDS = new float[] {
		0,0,
		1,0,
		1,1,
		0,1,
	};

	/** Indizes des Quads. */
	private static final int[] INDICES = new int[] {
		0,1,2,
		2,3,0
	};

	public static Model getModel() {
		return model;
	}

	public static void initAsset() {
		model = new Model(VERTICES, TEXTURE_COORDS, INDICES);
	}

	public static void deleteAsset() {
		model = null;
	}
}
