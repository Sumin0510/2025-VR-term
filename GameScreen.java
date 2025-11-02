package io.nom.jbnu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private final OrthographicCamera cam;
    private final Runner runner;
    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer;

    private Texture[] obstacleTextures;
    private Array<Obstacle> obstacles;
    private float obstacleSpawnTimer = 0f;
    private float spawnInterval = 1.5f;
    private float lastObstacleX = 0f;

    private Music currentMusic;
    private Sound hitSound;

    private int level = 1;
    private float baseGroundY = 100f;

    private Texture clearSheet;
    private Animation<TextureRegion> clearAnim;
    private float clearAnimTime = 0f;

    private boolean paused = false;
    private boolean gameOver = false;
    private boolean levelCleared = false;

    private static class Obstacle {
        Rectangle rect;
        int type;
        Obstacle(Rectangle rect, int type) {
            this.rect = rect;
            this.type = type;
        }
    }

    public GameScreen(Main game) {
        this.game = game;
        this.batch = game.batch;
        this.cam = new OrthographicCamera(1280, 720);
        this.runner = new Runner();
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {
        obstacleTextures = new Texture[]{
            new Texture(Gdx.files.internal("obstacle_box.png")),
            new Texture(Gdx.files.internal("obstacle_rock.png")),
            new Texture(Gdx.files.internal("obstacle_sign.png"))
        };
        hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.wav"));
        startMusic("stage1.mp3");
        obstacles = new Array<>();
        spawnObstacle();
        cam.position.set(640, 360, 0);
        clearSheet = new Texture(Gdx.files.internal("clear_sheet.png"));
        TextureRegion[][] tmp = TextureRegion.split(clearSheet, clearSheet.getWidth() / 2, clearSheet.getHeight());
        TextureRegion[] frames = new TextureRegion[2];
        frames[0] = tmp[0][0];
        frames[1] = tmp[0][1];
        clearAnim = new Animation<>(0.2f, frames);
    }

    private void startMusic(String path) {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
        }
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentMusic.setLooping(true);
        currentMusic.setVolume(0.5f);
        currentMusic.play();
    }

    private void spawnObstacle() {
        float difficulty = MathUtils.clamp(runner.getX() / 5000f, 0f, 1f);
        float minGap = MathUtils.lerp(600f, 300f, difficulty);
        float maxGap = MathUtils.lerp(1000f, 600f, difficulty);
        float gap = MathUtils.random(minGap, maxGap);
        float x = Math.max(runner.getX() + 800, lastObstacleX + gap);
        float y = getGroundHeight(x) - 4f;
        float w = 64, h = 64;
        int type = MathUtils.random(0, obstacleTextures.length - 1);
        obstacles.add(new Obstacle(new Rectangle(x, y, w, h), type));
        lastObstacleX = x;
    }

    @Override
    public void render(float delta) {
        switch (level) {
            case 1:
                ScreenUtils.clear(0.7f, 0.85f, 1f, 1f);
                break;
            case 2:
                ScreenUtils.clear(0.75f, 0.6f, 0.9f, 1f);
                break;
            case 3:
                ScreenUtils.clear(0.7f, 0.7f, 0.7f, 1f);
                break;
        }

        handleInput();
        if (!paused && !gameOver) updateGameLogic(delta);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        drawObstacles();
        runner.draw(batch);
        batch.end();
        drawGround();
        batch.begin();
        drawHUD();
        if (levelCleared) drawClearAnimation(delta);
        else if (paused) {
            font.getData().setScale(2f);
            font.draw(batch, "PAUSED", cam.position.x - 60, cam.viewportHeight / 2 + 40);
        } else if (gameOver) {
            font.getData().setScale(2f);
            font.draw(batch, "CONTINUE?", cam.position.x - 100, cam.viewportHeight / 2 + 40);
        }
        batch.end();
    }

    private void handleInput() {
        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) resetGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !paused) runner.jump();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            if (paused && currentMusic != null) currentMusic.pause();
            else if (!paused && currentMusic != null) currentMusic.play();
        }
        if (levelCleared && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            resetGame();
            return;
        }
    }

    private void updateGameLogic(float delta) {
        float groundY = (level == 1) ? baseGroundY : getGroundHeight(runner.getX());
        runner.update(delta, groundY);
        if (levelCleared) return;
        obstacleSpawnTimer += delta;
        if (level == 3 && runner.getX() > 15000 && !levelCleared) {
            levelCleared = true;
            paused = false;
            gameOver = false;
            if (currentMusic != null) currentMusic.stop();
        }
        if (obstacleSpawnTimer >= spawnInterval) {
            spawnObstacle();
            spawnInterval = MathUtils.random(0.1f, 1f);
            obstacleSpawnTimer = 0f;
        }
        float hitboxMargin = 8f;
        Rectangle runnerRect = new Rectangle(
            runner.pos.x + hitboxMargin,
            runner.pos.y + hitboxMargin,
            64 - hitboxMargin * 2,
            64 - hitboxMargin * 2
        );
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle obs = obstacles.get(i);
            obs.rect.x -= 300 * delta;
            float newY = getGroundHeight(obs.rect.x) - 4f;
            obs.rect.y = newY;
            Rectangle obsRect = new Rectangle(
                obs.rect.x + 4, obs.rect.y + 4,
                obs.rect.width - 8, obs.rect.height - 8
            );
            boolean fromAbove = runner.pos.y > obs.rect.y + obs.rect.height * 0.7f;
            if (!runner.isCrashed() && runnerRect.overlaps(obsRect) && !fromAbove) {
                hitSound.play(0.8f);
                runner.crash();
                if (runner.getLives() <= 0) {
                    gameOver = true;
                    if (currentMusic != null) currentMusic.stop();
                }
            }
            if (obs.rect.x + obs.rect.width < runner.getX() - 200) {
                obstacles.removeIndex(i);
                i--;
            }
        }
        if (cam.position.x > 5000 && level == 1) {
            level = 2;
            startMusic("stage2.mp3");
        } else if (cam.position.x > 10000 && level == 2) {
            level = 3;
            startMusic("stage3.mp3");
        }
        cam.position.x = runner.getX() + 300;
    }

    private void drawClearAnimation(float delta) {
        clearAnimTime += delta;
        TextureRegion frame = clearAnim.getKeyFrame(clearAnimTime, true);
        float clearX = cam.position.x + cam.viewportWidth / 2 - 250;
        float clearY = baseGroundY - 15;
        batch.draw(frame, clearX, clearY, 250, 250);
        font.getData().setScale(2f);
        font.draw(batch, "LEVEL CLEAR!", cam.position.x - 150, cam.viewportHeight / 2 + 40);
        font.getData().setScale(2f);
        font.draw(batch, "Press SPACE to Restart", cam.position.x - 160, cam.viewportHeight / 2 - 60);
        runner.update(delta, getGroundHeight(runner.getX()));
        cam.position.x = runner.getX() + 300;
        cam.update();
    }

    private void resetGame() {
        gameOver = false;
        level = 1;
        runner.reset();
        obstacles.clear();
        obstacleSpawnTimer = 0f;
        lastObstacleX = 0f;
        spawnInterval = MathUtils.random(1.0f, 2.5f);
        spawnObstacle();
        startMusic("stage1.mp3");
    }

    private void drawObstacles() {
        for (Obstacle obs : obstacles)
            batch.draw(obstacleTextures[obs.type], obs.rect.x, obs.rect.y, obs.rect.width, obs.rect.height);
    }

    private void drawHUD() {
        font.getData().setScale(1.3f);
        font.draw(batch, "LIVES : " + runner.getLives(), cam.position.x - cam.viewportWidth / 2 + 50, cam.viewportHeight - 50);
        font.draw(batch, "LEVEL: " + level, cam.position.x - cam.viewportWidth / 2 + 50, cam.viewportHeight - 80);
    }

    private void drawGround() {
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        float startX = cam.position.x - cam.viewportWidth / 2;
        float endX = cam.position.x + cam.viewportWidth / 2;
        float step = 20f;
        for (float x = startX; x < endX; x += step) {
            float y = getGroundHeight(x);
            shapeRenderer.rect(x, 0, step, y);
        }
        shapeRenderer.end();
    }

    private float getGroundHeight(float x) {
        switch (level) {
            case 1:
                return baseGroundY;
            case 2:
                if (x < 5000) return baseGroundY;
                else if (x < 5800) {
                    float rise = (x - 5000) * 0.125f;
                    return Math.min(baseGroundY + rise, 200f);
                } else if (x < 7300) return 200f;
                else if (x < 8100) {
                    float fall = (x - 7300) * 0.125f;
                    return Math.max(200f - fall, baseGroundY);
                } else return baseGroundY;
            case 3:
                if (x < 4000) return baseGroundY;
                else if (x < 4800) {
                    float rise = (x - 4000) * 0.125f;
                    return Math.min(baseGroundY + rise, 200f);
                } else if (x < 6000) return 200f;
                else if (x < 6800) {
                    float fall = (x - 6000) * 0.125f;
                    return Math.max(200f - fall, baseGroundY);
                } else if (x < 7600) {
                    float rise = (x - 6800) * 0.125f;
                    return Math.min(baseGroundY + rise, 200f);
                } else if (x < 8600) return 200f;
                else if (x < 9400) {
                    float fall = (x - 8600) * 0.125f;
                    return Math.max(200f - fall, baseGroundY);
                } else return baseGroundY;
            default:
                return baseGroundY;
        }
    }

    public static float getGroundHeightStatic(float x) {
        float base = 100f;
        if (x < 4000) return base + (x / 40f);
        else if (x < 8000) return base + 1000 - ((x - 4000) / 40f);
        else if (x < 12000) return base + ((x - 8000) / 50f);
        else return base;
    }

    @Override
    public void dispose() {
        for (Texture tex : obstacleTextures) tex.dispose();
        if (currentMusic != null) currentMusic.dispose();
        if (hitSound != null) hitSound.dispose();
        runner.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
