package io.nom.jbnu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Runner {

    public enum State { RUN, JUMP, CRASH }

    private State currentState = State.RUN;
    private TextureRegion currentFrame;

    private final Animation<TextureRegion> runAnim;
    private final Animation<TextureRegion> crashAnim;
    private final TextureRegion jumpFrame;

    private final Texture runSheet;
    private final Texture jumpSheet;
    private final Texture crashSheet;

    private float stateTime = 0f;
    private float crashTimer = 0f;

    public final Vector2 pos = new Vector2(80, 100);
    public final Vector2 vel = new Vector2(0, 0);
    public final Vector2 acc = new Vector2(0, -1500f);

    private boolean jumping = false;
    private boolean crashed = false;

    private int lives = 3;

    public Runner() {
        runSheet = new Texture(Gdx.files.internal("runner_run.png"));
        jumpSheet = new Texture(Gdx.files.internal("runner_jump.png"));
        crashSheet = new Texture(Gdx.files.internal("runner_bang.png"));

        runAnim = loadAnimation(runSheet, 2, 0.1f);
        crashAnim = loadAnimation(crashSheet, 2, 0.1f);
        jumpFrame = new TextureRegion(jumpSheet);

        currentFrame = runAnim.getKeyFrame(0);
    }

    private Animation<TextureRegion> loadAnimation(Texture sheet, int frames, float frameDuration) {
        int frameWidth = sheet.getWidth() / frames;
        int frameHeight = sheet.getHeight();
        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight);
        TextureRegion[] arr = new TextureRegion[frames];
        for (int i = 0; i < frames; i++) arr[i] = tmp[0][i];
        return new Animation<>(frameDuration, arr);
    }

    public void update(float delta, float groundY) {
        if (delta == 0) return;
        stateTime += delta;

        if (crashed) {
            updateCrashState(delta);
            return;
        }

        vel.add(acc.x * delta, acc.y * delta);
        pos.add(vel.x * delta, vel.y * delta);

        if (!crashed) pos.x += 100 * delta;

        float terrainY = GameScreen.getGroundHeightStatic(pos.x);

        if (pos.y <= groundY) {
            pos.y = groundY;
            jumping = false;
            vel.y = 0;
            if (!crashed) currentState = State.RUN;
        }

        updateAnimationState();
    }

    public void jump() {
        if (!jumping && !crashed) {
            vel.y = 700f;
            jumping = true;
            currentState = State.JUMP;
            stateTime = 0f;
        }
    }

    public void crash() {
        if (!crashed) {
            crashed = true;
            currentState = State.CRASH;
            vel.setZero();
            stateTime = 0f;
            crashTimer = 0f;
            lives = Math.max(0, lives - 1);
        }
    }

    private void updateCrashState(float delta) {
        crashTimer += delta;
        currentFrame = crashAnim.getKeyFrame(stateTime, true);
        pos.x -= 100 * delta;

        if (crashTimer >= 1.0f) {
            resetAfterCrash();
        }
    }

    private void applyPhysics(float delta) {
        vel.add(acc.x * delta, acc.y * delta);
        pos.add(vel.x * delta, vel.y * delta);

        float groundY = 100f;
        if (pos.y <= groundY) {
            pos.y = groundY;
            jumping = false;
            vel.y = 0;
            if (!crashed) currentState = State.RUN;
        }
    }

    private void updateAnimationState() {
        switch (currentState) {
            case RUN:
                currentFrame = runAnim.getKeyFrame(stateTime, true);
                break;
            case JUMP:
                currentFrame = jumpFrame;
                break;
            case CRASH:
                currentFrame = crashAnim.getKeyFrame(stateTime, true);
                break;
        }
    }

    private void resetAfterCrash() {
        crashed = false;
        currentState = State.RUN;
        pos.x = MathUtils.lerp(pos.x, 80, 0.4f);
        stateTime = 0f;
        crashTimer = 0f;
    }

    public void draw(SpriteBatch batch) {
        if (currentFrame == null) currentFrame = runAnim.getKeyFrame(0);
        float drawOffsetY = -15f;
        batch.draw(currentFrame, pos.x, pos.y + drawOffsetY);
    }

    public void reset() {
        pos.set(80, 100);
        vel.set(0, 0);
        acc.set(0, -1500f);
        lives = 3;
        crashed = false;
        jumping = false;
        currentState = State.RUN;
    }

    public float getX() { return pos.x; }
    public int getLives() { return lives; }
    public boolean isCrashed() { return crashed; }

    public void dispose() {
        runSheet.dispose();
        jumpSheet.dispose();
        crashSheet.dispose();
    }
}
