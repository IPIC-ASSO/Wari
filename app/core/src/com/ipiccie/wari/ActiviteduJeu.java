package com.ipiccie.wari;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.ipiccie.wari.Screens.EcranMenu;

public class ActiviteduJeu  extends Game {
	static public Skin gameSkin;
	public SpriteBatch batch;
	public ShapeRenderer shapeRenderer;
	public BitmapFont font;

	@java.lang.Override
	public void create() {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		font = new BitmapFont();
		this.setScreen(new EcranMenu(this));
	}

	public void render () {
		super.render();
	}


	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
		font.dispose();
	}
}