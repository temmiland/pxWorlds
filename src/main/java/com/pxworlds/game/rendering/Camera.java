package com.pxworlds.game.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	/** Die aktuelle Kamera-Position. */
	private Vector3f position;
	/** Kamera-Position zu Beginn des letzten Ticks — wird für Render-Interpolation verwendet. */
	private Vector3f prevPosition;
	/** Die Projektionsmatrix. */
	private Matrix4f projection;
	/** Wiederverwendbare Scratch-Matrix für getProjection() — vermeidet Allokation pro Frame. */
	private final Matrix4f projectionScratch = new Matrix4f();
	/** Wiederverwendbare Scratch-Matrix für getProjection(alpha) — vermeidet Allokation pro Frame. */
	private final Matrix4f projectionAlphaScratch = new Matrix4f();
	/** Wiederverwendbarer Scratch-Vektor für Lerp in getProjection(alpha) — vermeidet Allokation pro Frame. */
	private final Vector3f interpolatedScratch = new Vector3f();

	/** Referenzhöhe zur proportionalen Skalierung bei Größänderungen. */
	private final int referenceHeight;
	/** Virtuelle Viewport-Breite (abgeleitet aus Referenzhöhe + Seitenverhältnis). */
	private int virtualWidth;
	/** Virtuelle Viewport-Höhe (= referenceHeight). */
	private int virtualHeight;

	/**
	 * Erstellt eine neue Kamera mit der angegebenen Viewport-Größe.
	 * @param width  Viewport-Breite in Pixeln
	 * @param height Viewport-Höhe in Pixeln
	 */
	public Camera(int width, int height) {
		position = new Vector3f(0, 0, 0);
		prevPosition = new Vector3f(0, 0, 0);
		referenceHeight = height;
		setProjection(width, height);
	}

	public void setProjection(int width, int height) {
		float aspect = (float) width / height;
		virtualHeight = referenceHeight;
		virtualWidth  = (int) (referenceHeight * aspect);
		projection = new Matrix4f().setOrtho2D(
				-virtualWidth / 2f, virtualWidth / 2f,
				-virtualHeight / 2f, virtualHeight / 2f);
	}

	/** @return virtuelle Viewport-Breite (in Weltpixel-Einheiten) */
	public int getVirtualWidth() { return virtualWidth; }
	/** @return virtuelle Viewport-Höhe (in Weltpixel-Einheiten) */
	public int getVirtualHeight() { return virtualHeight; }

	public void setPosition(Vector3f newPosition) {
		this.position.set(newPosition);
	}

	/** Setzt die Kamera-Position auf die angegebenen x/y-Werte (z bleibt unverändert). */
	public void setPosition(final float x, final float y) {
		this.position.set(x, y, this.position.z);
	}

	public void addPosition(Vector3f newPosition) {
		this.position.add(newPosition);
	}

	public Vector3f getPosition() {
		return position;
	}

	/**
	 * Sichert die aktuelle Position als prevPosition.
	 * Muss zu Beginn jedes Ticks aufgerufen werden, bevor eine Entity die Kamera aktualisiert.
	 */
	public void savePrevPosition() {
		prevPosition.set(position);
	}

	/**
	 * Gibt die untransformierte Projektionsmatrix zurück (ohne Kamera-Translation).
	 * @return die rohe Projektionsmatrix
	 */
	public Matrix4f getUntransformedProjection() {
		return projection;
	}

	/**
	 * Gibt die Projektionsmatrix mit der aktuellen Kamera-Position zurück.
	 * Nur verwenden, wo keine Interpolation benötigt wird (z.B. GUI).
	 * @return die Projektionsmatrix
	 */
	public Matrix4f getProjection() {
		return projection.translate(position, projectionScratch);
	}

	/**
	 * Gibt die Projektionsmatrix mit der zwischen letztem und aktuellem Tick
	 * interpolierten Kamera-Position zurück. Für alles In-World-Rendering verwenden.
	 * @param alpha Interpolationsfaktor 0.0–1.0 (aus der Spielschleife)
	 * @return die interpolierte Projektionsmatrix
	 */
	public Matrix4f getProjection(final float alpha) {
		prevPosition.lerp(position, alpha, interpolatedScratch);
		return projection.translate(interpolatedScratch, projectionAlphaScratch);
	}
}
