package land.temmi.pxworlds.entity;

import land.temmi.rollercoaster.physics.AABB;
import land.temmi.rollercoaster.physics.Collision;
import land.temmi.rollercoaster.platform.Window;
import land.temmi.rollercoaster.graphics.Animation;
import land.temmi.rollercoaster.graphics.Camera;
import land.temmi.rollercoaster.graphics.SpriteBatch;
import land.temmi.rollercoaster.graphics.TextureAtlas;
import land.temmi.pxworlds.world.World;

import java.util.List;

/** Base class for all movable, collidable, animated game objects. */
public abstract class Entity {

    protected AABB boundingBox;
    protected Animation[] animations;
    private int useAnimation;

    protected float posX, posY;
    protected final float scaleX, scaleY;
    protected float prevX, prevY;

    // Reused each tick to avoid a heap allocation in moveClipped
    private final AABB sweptBounds = new AABB(0, 0, 0, 0);

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

    public float getPosX() { return posX; }
    public float getPosY() { return posY; }

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

    protected void setBoundingBoxSize(final float hw, final float hh) {
        boundingBox.minX = posX - hw;
        boundingBox.maxX = posX + hw;
        boundingBox.minY = posY - hh;
        boundingBox.maxY = posY + hh;
    }

    protected void setAnimation(int index, Animation animation) {
        animations[index] = animation;
    }

    public void useAnimation(int index) {
        this.useAnimation = index;
    }

    public void pauseAnimations() {
        for (final Animation anim : animations) {
            if (anim != null) anim.pause();
        }
    }

    public void resumeAnimations() {
        for (final Animation anim : animations) {
            if (anim != null) anim.resume();
        }
    }

    public void moveClipped(float xa, float ya, World world) {
        boundingBox.expandInto(xa, ya, sweptBounds);
        final List<AABB> boxes = world.getCollidingBoxes(sweptBounds);

        // Y first: prevents corner bugs on simultaneous X/Y contact
        for (AABB b : boxes) ya = b.clipY(boundingBox, ya);
        boundingBox.move(0, ya);

        for (AABB b : boxes) xa = b.clipX(boundingBox, xa);
        boundingBox.move(xa, 0);

        posX = boundingBox.getCenterX();
        posY = boundingBox.getCenterY();
    }

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
            prevX = posX;
            prevY = posY;
        }
    }

    public abstract void update(float delta, Window window, Camera camera, World world);

    public void snapPrevPosition() {
        prevX = posX;
        prevY = posY;
    }

    public final void tick(float delta, Window window, Camera camera, World world) {
        prevX = posX;
        prevY = posY;
        update(delta, window, camera, world);
    }

    public void render(final SpriteBatch batch, final TextureAtlas atlas, final float alpha) {
        final float[] uv = animations[useAnimation].getUV(atlas);
        if (uv == null) return;
        final float rx = prevX + (posX - prevX) * alpha;
        final float ry = prevY + (posY - prevY) * alpha;
        batch.draw(rx, ry, scaleX, scaleY, uv[0], uv[1], uv[2], uv[3]);
    }

    public void collideWithEntity(Entity entity) {
        final Collision collision = boundingBox.getCollision(entity.boundingBox);
        if (!collision.isIntersecting()) return;

        final float sepX = entity.boundingBox.getCenterX() - boundingBox.getCenterX();
        final float sepY = entity.boundingBox.getCenterY() - boundingBox.getCenterY();

        final float halfDx = collision.getDistance().x * 0.5f;
        final float halfDy = collision.getDistance().y * 0.5f;

        if (collision.getDistance().x > collision.getDistance().y) {
            final float dirX = sepX > 0 ? 1f : -1f;
            boundingBox.move(halfDx * dirX, 0);
            entity.boundingBox.move(halfDx * -dirX, 0);
        } else {
            final float dirY = sepY > 0 ? 1f : -1f;
            boundingBox.move(0, halfDy * dirY);
            entity.boundingBox.move(0, halfDy * -dirY);
        }

        posX = boundingBox.getCenterX();
        posY = boundingBox.getCenterY();
        // Drag prevPos along — otherwise the renderer interpolates across the push-out and pops
        prevX = posX;
        prevY = posY;

        entity.posX = entity.boundingBox.getCenterX();
        entity.posY = entity.boundingBox.getCenterY();
        entity.prevX = entity.posX;
        entity.prevY = entity.posY;
    }
}
