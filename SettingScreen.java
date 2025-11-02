package io.nom.jbnu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class SettingScreen {
    private final SpriteBatch batch;
    private final BitmapFont font;

    public SettingScreen(SpriteBatch batch) {
        this.batch = batch;
        this.font = new BitmapFont();
    }

    public void show() {
        Gdx.app.log("SettingScreen", "환경설정 화면 표시");
    }

    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
        font.draw(batch, "환경설정 화면입니다", 200, 300);
        font.draw(batch, "ESC 누르면 돌아갑니다", 200, 250);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    public void dispose() {
        font.dispose();
    }
}
