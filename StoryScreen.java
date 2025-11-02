package io.nom.jbnu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.audio.Music;


public class StoryScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private Music storyMusic;

    private final OrthographicCamera cam;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Texture runnerTexture;

    private final String[] storyLines = {
        "He runs.",
        "Running away from everything in this world,",
        "he keeps running without a plan.",
        "But He is already late...",
        "And you are alone, too."
    };


    private int currentLine = 0;
    private float charTimer = 0f;
    private float charDelay = 0.05f;
    private int charsVisible = 0;
    private boolean lineFinished = false;
    private float groundHeight = 100f;

    public StoryScreen(Main game) {
        this.game = game;
        this.batch = game.batch;
        this.cam = new OrthographicCamera(1280, 720);
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.runnerTexture = new Texture(Gdx.files.internal("runner_stand.png"));
        cam.position.set(640, 360, 0);
    }

    @Override
    public void show() {
        charsVisible = 0;
        lineFinished = false;
        currentLine = 0;
        storyMusic = Gdx.audio.newMusic(Gdx.files.internal("story.mp3"));
        storyMusic.setLooping(true);
        storyMusic.setVolume(0.4f);
        storyMusic.play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.7f, 0.85f, 1f, 1f);
        cam.update();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (lineFinished) {
                currentLine++;
                if (currentLine >= storyLines.length) {
                    storyMusic.stop();
                    game.setScreen(new GameScreen(game));
                    dispose();
                    return;
                } else {
                    charsVisible = 0;
                    lineFinished = false;
                }
            } else {
                charsVisible = storyLines[currentLine].length();
                lineFinished = true;
            }
        }

        if (!lineFinished) {
            charTimer += delta;
            if (charTimer >= charDelay) {
                charTimer = 0f;
                charsVisible++;
                if (charsVisible >= storyLines[currentLine].length()) {
                    charsVisible = storyLines[currentLine].length();
                    lineFinished = true;
                }
            }
        }

        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(0, 0, 1280, groundHeight);
        shapeRenderer.end();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        batch.draw(runnerTexture, 550, groundHeight, 120, 160);

        font.getData().setScale(2f);
        font.setColor(0f, 0f, 0f, 1f);
        String visibleText = storyLines[currentLine].substring(0, Math.min(charsVisible, storyLines[currentLine].length()));
        font.draw(batch, visibleText, 330, 400);

        if (lineFinished) {
            font.getData().setScale(1f);
            font.draw(batch, "Press SPACE", 550, 350);
        }

        batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        runnerTexture.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
