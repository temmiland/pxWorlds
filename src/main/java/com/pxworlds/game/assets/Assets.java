package com.pxworlds.game.assets;

import com.pxworlds.game.rendering.Model;

public class Assets {
	/** The shared model asset. */
	private static Model model;

	// Quad geometry constants
	/** The Z depth for the quad. */
	private static final float Z_DEPTH = 0.0f;
	/** The vertices for the quad. */
	private static final float[] VERTICES = new float[] {
		-1f, 1f, Z_DEPTH, //TOP LEFT     0
		1f, 1f, Z_DEPTH,  //TOP RIGHT    1
		1f, -1f, Z_DEPTH, //BOTTOM RIGHT 2
		-1f, -1f, Z_DEPTH,//BOTTOM LEFT  3
	};

	/** The texture coordinates for the quad. */
	private static final float[] TEXTURE_COORDS = new float[] {
		0,0,
		1,0,
		1,1,
		0,1,
	};

	/** The indices for the quad. */
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
