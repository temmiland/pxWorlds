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
    /** Kollisions-AABB der Entity. */
    protected AABB boundingBox;
    /** Array der Animationen. */
    protected Animation[] animations;
    /** Index der aktuell genutzten Animation. */
    private int useAnimation;

    /** World-Space-Position der Entity. */
    protected float posX, posY;
    /** Render-Skalierung der Entity. */
    protected final float scaleX, scaleY;
    /** Position am Ende des letzten Ticks — Ausgangspunkt für Render-Interpolation. */
    protected float prevX, prevY;

    /** Divisor für symmetrischen Push-out zwischen zwei Entities. */
    private static final int DIVIDE_BY_TWO = 2;

    /**
     * Wiederverwendbare Sweep-AABB für {@link #moveClipped} — vermeidet
     * eine Heap-Allokation pro Tick.
     */
    private final AABB sweptBounds = new AABB(0, 0, 0, 0);

    /** @return aktuelle X-Position im World-Space */
    public float getPosX() {
        return posX;
    }

    /** @return aktuelle Y-Position im World-Space */
    public float getPosY() {
        return posY;
    }

    public Entity(int maxAnimations, float initPosX, float initPosY, float initScaleX, float initScaleY) {
        this.animations = new Animation[maxAnimations];
        this.posX   = initPosX;
        this.posY   = initPosY;
        this.scaleX = initScaleX;
        this.scaleY = initScaleY;
        this.useAnimation = 0;
        this.prevX  = initPosX;
        this.prevY  = initPosY;
        boundingBox = new AABB(initPosX - initScaleX, initPosY - initScaleY, initPosX + initScaleX, initPosY + initScaleY);
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
        final float rx = prevX + (posX - prevX) * alpha;
        final float ry = prevY + (posY - prevY) * alpha;
        final float hw = boundingBox.getHalfWidth();
        final float hh = boundingBox.getHalfHeight();
        out.minX = rx - hw;
        out.maxX = rx + hw;
        out.minY = ry - hh;
        out.maxY = ry + hh;
    }

    /**
     * Setzt die Hitbox-Ausdehnung unabhängig von der Render-Skalierung.
     * Ermöglicht Subklassen, eine kleinere Hitbox als den Render-Sprite zu definieren.
     *
     * @param hw  halbe Breite in Tile-Einheiten
     * @param hh  halbe Höhe in Tile-Einheiten
     */
    protected void setBoundingBoxSize(final float hw, final float hh) {
        final float cx = posX;
        final float cy = posY;
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

    /** Pausiert alle Animationen dieser Entity. */
    public void pauseAnimations() {
        for (final Animation anim : animations) {
            if (anim != null) {
                anim.pause();
            }
        }
    }

    /** Setzt alle Animationen dieser Entity fort. */
    public void resumeAnimations() {
        for (final Animation anim : animations) {
            if (anim != null) {
                anim.resume();
            }
        }
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

        posX = boundingBox.getCenterX();
        posY = boundingBox.getCenterY();
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
            if (col.isIntersecting()) {
                boundingBox.correctPosition(tile, col);
                moved = true;
            }
        }
        if (moved) {
            posX = boundingBox.getCenterX();
            posY = boundingBox.getCenterY();
            // Keine Interpolation über die Korrektur — prevPos nachziehen
            prevX = posX;
            prevY = posY;
        }
    }

    public abstract void update(float delta, Window window, Camera camera, World world);

    /** Synchronisiert prevPos auf die aktuelle Position — stoppt Render-Interpolation. */
    public void snapPrevPosition() {
        prevX = posX;
        prevY = posY;
    }

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
        prevX = posX;
        prevY = posY;
        update(delta, window, camera, world);
    }

    public void render(Shader shader, Camera camera, World world, float alpha) {
        final Matrix4f target = camera.getProjection(alpha);
        target.mul(world.getWorldMatrix());

        final float renderX = prevX + (posX - prevX) * alpha;
        final float renderY = prevY + (posY - prevY) * alpha;
        target.translate(renderX, renderY, 0f);
        target.scale(scaleX, scaleY, 1f);

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

        if (collision.isIntersecting()) {
            final float sepX = entity.boundingBox.getCenterX() - boundingBox.getCenterX();
            final float sepY = entity.boundingBox.getCenterY() - boundingBox.getCenterY();

            // Overlap gleichmäßig auf beide Entities aufteilen
            final float halfDx = collision.getDistance().x / DIVIDE_BY_TWO;
            final float halfDy = collision.getDistance().y / DIVIDE_BY_TWO;

            if (collision.getDistance().x > collision.getDistance().y) {
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

            posX = boundingBox.getCenterX();
            posY = boundingBox.getCenterY();
            // prevPos mitsynchronisieren — sonst interpoliert der Renderer
            // über den gesamten Push-out-Delta und erzeugt einen sichtbaren
            // Positions-Pop in der nächsten Render-Frame.
            prevX = posX;
            prevY = posY;

            entity.posX = entity.boundingBox.getCenterX();
            entity.posY = entity.boundingBox.getCenterY();
            entity.prevX = entity.posX;
            entity.prevY = entity.posY;
        }
    }
}
