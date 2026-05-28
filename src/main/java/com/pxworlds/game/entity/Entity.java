package com.pxworlds.game.entity;

import com.pxworlds.game.assets.Assets;
import com.pxworlds.game.collision.AABB;
import com.pxworlds.game.collision.Collision;
import com.pxworlds.game.io.Window;
import com.pxworlds.game.rendering.Animation;
import com.pxworlds.game.rendering.Camera;
import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.world.World;
import org.joml.*;

import java.util.List;


public abstract class Entity {
    /** The bounding box for collision detection. */
    protected AABB boundingBox;
    /** The array of animations. */
    protected Animation[] animations;
    /** The index of the current animation. */
    private int useAnimation;

    /** The transform of the entity. */
    protected Transform transform;
    /** Position am Ende des letzten Ticks — Ausgangspunkt für Render-Interpolation. */
    protected Vector3f prevPos;

    /** The divide by two value. */
    private static final int DIVIDE_BY_TWO = 2;

    /**
     * Wiederverwendbare Sweep-AABB für {@link #moveClipped} — vermeidet
     * eine Heap-Allokation pro Tick.
     */
    private final AABB sweptBounds = new AABB(0, 0, 0, 0);

    /**
     * Gibt die aktuelle World-Space-Position der Entity zurück (live reference).
     * @return {@code transform.pos} — kein Defensive-Copy, nicht cachen
     */
    public Vector3f getPosition() {
        return transform.pos;
    }

    public Entity(int maxAnimations, Transform newTransform) {
        this.animations = new Animation[maxAnimations];

        this.transform = newTransform;
        this.useAnimation = 0;
        this.prevPos = new Vector3f(transform.pos);

        final float px = transform.pos.x;
        final float py = transform.pos.y;
        final float hw = transform.scale.x;
        final float hh = transform.scale.y;
        boundingBox = new AABB(px - hw, py - hh, px + hw, py + hh);
    }

    /**
     * Schreibt die auf {@code alpha} interpolierte AABB in {@code out} — allokationsfrei.
     * Verwendet dieselbe Formel wie {@link #render}, sodass die Debug-Box exakt
     * auf dem gerenderten Sprite liegt.
     *
     * @param alpha Interpolationsfaktor 0.0–1.0
     * @param out   Ziel-AABB (wird in-place überschrieben)
     */
    public void getInterpolatedBoundingBox(final float alpha, final AABB out) {
        final float rx = prevPos.x + (transform.pos.x - prevPos.x) * alpha;
        final float ry = prevPos.y + (transform.pos.y - prevPos.y) * alpha;
        final float hw = boundingBox.getHalfWidth();
        final float hh = boundingBox.getHalfHeight();
        out.minX = rx - hw;
        out.maxX = rx + hw;
        out.minY = ry - hh;
        out.maxY = ry + hh;
    }

    /**
     * Setzt die Hitbox-Ausdehnung unabhängig von {@code transform.scale}.
     * Ermöglicht Subklassen, eine kleinere Hitbox als den Render-Sprite zu definieren.
     *
     * @param hw  halbe Breite in Tile-Einheiten
     * @param hh  halbe Höhe in Tile-Einheiten
     */
    protected void setBoundingBoxSize(final float hw, final float hh) {
        final float cx = transform.pos.x;
        final float cy = transform.pos.y;
        boundingBox.minX = cx - hw;
        boundingBox.maxX = cx + hw;
        boundingBox.minY = cy - hh;
        boundingBox.maxY = cy + hh;
    }

    protected void setAnimation(int index, Animation animation) {
        animations[index] = animation;
    }

    public void useAnimation(int index) {
        this.useAnimation = index;
    }

    /**
     * Bewegt die Entity achsenweise mit Collision-Clipping gegen Welt-Tiles.
     * Y wird vor X geclippt, um Ecken-Bugs zu vermeiden.
     *
     * @param xa    Bewegung auf der X-Achse (Tile-Einheiten)
     * @param ya    Bewegung auf der Y-Achse (Tile-Einheiten)
     * @param world Quelle der Tile-AABBs
     */
    public void moveClipped(float xa, float ya, World world) {
        boundingBox.expandInto(xa, ya, sweptBounds);
        final List<AABB> boxes = world.getCollidingBoxes(sweptBounds);

        // Y zuerst: verhindert Ecken-Bugs beim gleichzeitigen X/Y-Kontakt
        for (AABB b : boxes) {
            ya = b.clipY(boundingBox, ya);
        }
        boundingBox.move(0, ya);

        for (AABB b : boxes) {
            xa = b.clipX(boundingBox, xa);
        }
        boundingBox.move(xa, 0);

        transform.pos.x = boundingBox.getCenterX();
        transform.pos.y = boundingBox.getCenterY();
    }

    /**
     * Schiebt diese Entity aus allen Tile-AABBs heraus, mit denen sie aktuell überlappt.
     *
     * <p>Wird nach dem Entity-Entity-Push-out in {@link World#update} aufgerufen,
     * damit eine durch eine andere Entity in eine Wand gedrückte Entity korrekt
     * herauskorrigiert wird. Der Push-out wird nicht interpoliert (prevPos wird
     * mitgesetzt), weil er eine physikalische Korrektur, keine Bewegung ist.</p>
     *
     * @param world Spielwelt — Quelle der Tile-AABBs
     */
    public void resolveAgainstTiles(final World world) {
        final List<AABB> overlapping = world.getCollidingBoxes(boundingBox);
        boolean moved = false;
        for (final AABB tile : overlapping) {
            final Collision col = boundingBox.getCollision(tile);
            if (col.isIntersecting) {
                boundingBox.correctPosition(tile, col);
                moved = true;
            }
        }
        if (moved) {
            transform.pos.x = boundingBox.getCenterX();
            transform.pos.y = boundingBox.getCenterY();
            // Keine Interpolation über die Korrektur — prevPos nachziehen
            prevPos.set(transform.pos);
        }
    }

    public abstract void update(float delta, Window window, Camera camera, World world);

    /**
     * Sichert die aktuelle Position als prevPos und ruft dann update() auf.
     * Wird von World.update() aufgerufen, damit prevPos immer den Stand vor dem Tick enthält.
     *
     * @param delta    Zeit seit dem letzten Tick in Sekunden
     * @param window   das Fenster
     * @param camera   die aktive Kamera
     * @param world    die Spielwelt
     */
    public final void tick(float delta, Window window, Camera camera, World world) {
        prevPos.set(transform.pos);
        update(delta, window, camera, world);
    }

    public void render(Shader shader, Camera camera, World world, float alpha) {
        final Matrix4f target = camera.getProjection(alpha);
        target.mul(world.getWorldMatrix());

        final float renderX = prevPos.x + (transform.pos.x - prevPos.x) * alpha;
        final float renderY = prevPos.y + (transform.pos.y - prevPos.y) * alpha;
        target.translate(renderX, renderY, transform.pos.z);
        target.scale(transform.scale);

        shader.bind();
        shader.setUniform("sampler", 0);
        shader.setUniform("projection", target);
        animations[useAnimation].bind(0);
        Assets.getModel().render();
    }

    /**
     * Push-out-Kollision zwischen zwei Entities: trennt beide symmetrisch entlang
     * der Achse mit geringster Überlappung.
     *
     * <p>Die Trennungsrichtung wird aus den Positionen vor jeder Verschiebung berechnet,
     * damit Entity 2 nicht vom bereits verschobenen Zustand von Entity 1 beeinflusst wird.</p>
     *
     * @param entity  die andere Entity
     */
    public void collideWithEntity(Entity entity) {
        final Collision collision = boundingBox.getCollision(entity.boundingBox);

        if (collision.isIntersecting) {
            final float sepX = entity.boundingBox.getCenterX() - boundingBox.getCenterX();
            final float sepY = entity.boundingBox.getCenterY() - boundingBox.getCenterY();

            // Overlap gleichmäßig auf beide Entities aufteilen
            final float halfDx = collision.distance.x / DIVIDE_BY_TWO;
            final float halfDy = collision.distance.y / DIVIDE_BY_TWO;

            if (collision.distance.x > collision.distance.y) {
                final float dirX;
                if (sepX > 0) {
                    dirX = 1f;
                } else {
                    dirX = -1f;
                }
                boundingBox.move(halfDx * dirX, 0);
                entity.boundingBox.move(halfDx * -dirX, 0);
            } else {
                final float dirY;
                if (sepY > 0) {
                    dirY = 1f;
                } else {
                    dirY = -1f;
                }
                boundingBox.move(0, halfDy * dirY);
                entity.boundingBox.move(0, halfDy * -dirY);
            }

            transform.pos.set(boundingBox.getCenterX(), boundingBox.getCenterY(), 0);
            // prevPos mitsynchronisieren — sonst interpoliert der Renderer
            // über den gesamten Push-out-Delta und erzeugt einen sichtbaren
            // Positions-Pop in der nächsten Render-Frame.
            prevPos.set(transform.pos);

            entity.transform.pos.set(entity.boundingBox.getCenterX(), entity.boundingBox.getCenterY(), 0);
            entity.prevPos.set(entity.transform.pos);
        }
    }
}
