package io.nom.jbnu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.audio.Music;

public class MenuScreen implements Screen {
    private final Main game;
    private final SpriteBatch batch;
    private final Texture logo;
    private final Texture background;
    private final BitmapFont font;
    private Music menuMusic;

    public MenuScreen(Main game) {
        this.game = game;
        this.batch = game.batch;
        this.logo = new Texture("logo.png");
        this.background = new Texture("menu_bg.png");
        this.font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        batch.begin();
        batch.draw(background, 0, 0, screenWidth, screenHeight);
        float logoX = (Gdx.graphics.getWidth() - logo.getWidth()) / 2f;
        float logoY = Gdx.graphics.getHeight() / 2f + 40;
        batch.draw(logo, logoX, logoY);
        font.getData().setScale(2f);
        String startText = "PRESS SPACE TO START";
        font.draw(batch, startText, screenWidth / 2f - 200, screenHeight / 2f - 100);
        batch.end();
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            menuMusic.stop();
            game.setScreen(new StoryScreen(game));
        }
    }

    @Override
    public void show() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.5f);
        menuMusic.play();
    }

    @Override
    public void hide() {
        if (menuMusic != null) menuMusic.stop();
    }

    @Override
    public void dispose() {
        if (menuMusic != null) menuMusic.dispose();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
}
