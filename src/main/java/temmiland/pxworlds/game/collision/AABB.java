package temmiland.pxworlds.game.collision;

import org.joml.Vector2f;
import temmiland.pxworlds.game.entity.Entity;

/**
 * Axis-Aligned Bounding Box — gespeichert als min/max-Ecken.
 * Ermöglicht achsenweises Velocity-Clipping, das Überlappungen verhindert,
 * bevor sie entstehen.
 */
public class AABB {

    /** Faktor für Mittelpunkt-Berechnung aus min+max. */
    private static final float HALF = 0.5f;

    /**
     * Toleranz für Achsen-Guards in {@link #clipX} und {@link #clipY}.
     *
     * <p>Durch Floating-Point-Akkumulation (movementSpeed × delta) driftet die
     * Spieler-AABB leicht von exakten Tile-Grenzen ab (z.B. maxY = -8.9999 statt -9.0).
     * Ohne diesen Wert würde der Guard fälschlicherweise auslösen, obwohl der Spieler
     * geometrisch im Freiraum liegt. Der Wert entspricht ≈ 0.01 % einer Tile-Einheit —
     * weit genug unter dem Mindestabstand zwischen Kanten (1 Einheit), um keine echten
     * Kollisionen zu beeinflussen.</p>
     */
    private static final float CLIP_EPSILON = 1e-4f;

    /** Linke Kante (kleinstes X). */
    public float minX;
    /** Untere Kante (kleinstes Y). */
    public float minY;
    /** Rechte Kante (größtes X). */
    public float maxX;
    /** Obere Kante (größtes Y). */
    public float maxY;

    /**
     * Erstellt eine neue AABB aus min/max-Koordinaten.
     *
     * @param newMinX  linke Kante
     * @param newMinY  untere Kante
     * @param newMaxX  rechte Kante
     * @param newMaxY  obere Kante
     */
    public AABB(float newMinX, float newMinY, float newMaxX, float newMaxY) {
        this.minX = newMinX;
        this.minY = newMinY;
        this.maxX = newMaxX;
        this.maxY = newMaxY;
    }

    /**
     * @return X-Koordinate des Mittelpunkts
     */
    public float getCenterX() {
        return (minX + maxX) * HALF;
    }

    /**
     * @return Y-Koordinate des Mittelpunkts
     */
    public float getCenterY() {
        return (minY + maxY) * HALF;
    }

    /**
     * @return halbe Breite
     */
    public float getHalfWidth() {
        return (maxX - minX) * HALF;
    }

    /**
     * @return halbe Höhe
     */
    public float getHalfHeight() {
        return (maxY - minY) * HALF;
    }

    /**
     * @return Mittelpunkt als neuer Vector2f
     */
    public Vector2f getCenter() {
        return new Vector2f(getCenterX(), getCenterY());
    }

    /**
     * @return halbe Ausdehnung als neuer Vector2f
     */
    public Vector2f getHalfExtent() {
        return new Vector2f(getHalfWidth(), getHalfHeight());
    }

    /**
     * Prüft, ob ein Punkt innerhalb dieser AABB liegt.
     * Wird für GUI-Hover-Checks verwendet.
     *
     * @param point  der zu prüfende Punkt
     * @return Kollisionsdaten; {@code isIntersecting == true} wenn der Punkt innen liegt
     */
    public Collision getCollision(Vector2f point) {
        final float dx = Math.abs(point.x - getCenterX()) - getHalfWidth();
        final float dy = Math.abs(point.y - getCenterY()) - getHalfHeight();
        return new Collision(new Vector2f(dx, dy), dx < 0 && dy < 0);
    }

    /**
     * Verschiebt diese AABB in-place.
     *
     * @param dx  Versatz auf der X-Achse
     * @param dy  Versatz auf der Y-Achse
     */
    public void move(float dx, float dy) {
        minX += dx;
        maxX += dx;
        minY += dy;
        maxY += dy;
    }

    /**
     * Gibt eine neue AABB zurück, die in Richtung der gegebenen Velocity expandiert ist.
     * Dient dazu, alle potenziellen Kollisions-Kandidaten vor dem Clipping zu finden.
     *
     * @param xa  beabsichtigte X-Geschwindigkeit
     * @param ya  beabsichtigte Y-Geschwindigkeit
     * @return expandierte Kopie dieser AABB
     */
    public AABB expand(float xa, float ya) {
        float eMinX = minX;
        float eMinY = minY;
        float eMaxX = maxX;
        float eMaxY = maxY;
        if (xa < 0) {
            eMinX += xa;
        } else {
            eMaxX += xa;
        }
        if (ya < 0) {
            eMinY += ya;
        } else {
            eMaxY += ya;
        }
        return new AABB(eMinX, eMinY, eMaxX, eMaxY);
    }

    /**
     * Schreibt die in Velocity-Richtung expandierte AABB in {@code target} — allokationsfrei.
     * Äquivalent zu {@link #expand(float, float)}, aber ohne Heap-Allokation.
     *
     * @param xa     beabsichtigte X-Geschwindigkeit
     * @param ya     beabsichtigte Y-Geschwindigkeit
     * @param target Ziel-AABB (wird in-place überschrieben)
     */
    public void expandInto(float xa, float ya, AABB target) {
        if (xa < 0) {
            target.minX = minX + xa;
            target.maxX = maxX;
        } else {
            target.minX = minX;
            target.maxX = maxX + xa;
        }
        if (ya < 0) {
            target.minY = minY + ya;
            target.maxY = maxY;
        } else {
            target.minY = minY;
            target.maxY = maxY + ya;
        }
    }

    /**
     * @param other  die andere AABB
     * @return true, wenn diese AABB {@code other} überlappt
     */
    public boolean intersects(AABB other) {
        return maxX > other.minX && minX < other.maxX
            && maxY > other.minY && minY < other.maxY;
    }

    /**
     * Kürzt die X-Geschwindigkeit {@code xa} der sich bewegenden AABB
     * so, dass sie diese (statische) AABB nicht durchdringt.
     *
     * <p>Gibt {@code xa} unverändert zurück, wenn keine Y-Überschneidung vorliegt.
     *
     * @param moving  die AABB, die sich bewegen will
     * @param xa      beabsichtigte X-Geschwindigkeit (positiv = rechts)
     * @return geklippte X-Geschwindigkeit
     */
    public float clipX(AABB moving, float xa) {
        if (moving.maxY - CLIP_EPSILON <= minY || moving.minY + CLIP_EPSILON >= maxY) {
            return xa;
        }
        if (xa > 0 && moving.maxX <= minX) {
            final float d = minX - moving.maxX;
            if (d < xa) {
                xa = d;
            }
        } else if (xa < 0 && moving.minX >= maxX) {
            final float d = maxX - moving.minX;
            if (d > xa) {
                xa = d;
            }
        }
        return xa;
    }

    /**
     * Kürzt die Y-Geschwindigkeit {@code ya} der sich bewegenden AABB
     * so, dass sie diese (statische) AABB nicht durchdringt.
     *
     * <p>Gibt {@code ya} unverändert zurück, wenn keine X-Überschneidung vorliegt.
     *
     * @param moving  die AABB, die sich bewegen will
     * @param ya      beabsichtigte Y-Geschwindigkeit (positiv = oben)
     * @return geklippte Y-Geschwindigkeit
     */
    public float clipY(AABB moving, float ya) {
        if (moving.maxX - CLIP_EPSILON <= minX || moving.minX + CLIP_EPSILON >= maxX) {
            return ya;
        }
        if (ya > 0 && moving.maxY <= minY) {
            final float d = minY - moving.maxY;
            if (d < ya) {
                ya = d;
            }
        } else if (ya < 0 && moving.minY >= maxY) {
            final float d = maxY - moving.minY;
            if (d > ya) {
                ya = d;
            }
        }
        return ya;
    }

    /**
     * Berechnet die Eindringtiefe zwischen dieser AABB und {@code box2}.
     * Negative Werte bedeuten Überlappung.
     *
     * @param box2  die andere AABB
     * @return Kollisionsdaten mit Abstandsvektor und Überschneidungs-Flag
     */
    public Collision getCollision(AABB box2) {
        final float dx = Math.abs(box2.getCenterX() - getCenterX()) - (getHalfWidth()  + box2.getHalfWidth());
        final float dy = Math.abs(box2.getCenterY() - getCenterY()) - (getHalfHeight() + box2.getHalfHeight());
        return new Collision(new Vector2f(dx, dy), dx < 0 && dy < 0);
    }

    /**
     * Push-out-Korrektur: verschiebt diese AABB entlang der Achse mit
     * geringster Eindringtiefe, bis sie {@code box2} nicht mehr überlappt.
     *
     * <p>{@code data.distance} muss die gewünschte Separation enthalten.
     * Für gleichmäßige Aufteilung zwischen zwei Entities muss der Aufrufer
     * den Wert vorher halbieren oder {@link Entity#collideWithEntity}
     * verwenden, das beide Entities symmetrisch verschiebt.</p>
     *
     * @param box2  die andere AABB
     * @param data  Kollisionsdaten von {@link #getCollision(AABB)}
     */
    public void correctPosition(AABB box2, Collision data) {
        if (data.getDistance().x > data.getDistance().y) {
            final float dirX;
            if (box2.getCenterX() - getCenterX() > 0) {
                dirX = 1f;
            } else {
                dirX = -1f;
            }
            move(data.getDistance().x * dirX, 0);
        } else {
            final float dirY;
            if (box2.getCenterY() - getCenterY() > 0) {
                dirY = 1f;
            } else {
                dirY = -1f;
            }
            move(0, data.getDistance().y * dirY);
        }
    }
}
