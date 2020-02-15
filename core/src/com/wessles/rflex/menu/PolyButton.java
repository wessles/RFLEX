package com.wessles.rflex.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.wessles.mercury.math.geometry.Polygon;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;
import com.wessles.rflex.game.Background;

import java.util.Random;

public abstract class PolyButton {
	public String text;
	public Polygon bounds;

	public PolyButton(String text) {
		this.text = text;
		bounds = new Polygon(0, 0, 0.5f, 5);
	}

	public boolean canClick = true;

	public void render(float x, float y, float diameter) {
		final float cos = (float) Math.abs(Math.cos(Rflex.time / 100D));
		bounds.setDilation((diameter - (canClick ? diameter * 0.1f * cos * cos * cos * cos * cos * cos : 0)) / (touched ? 1.3f : 1f));
		bounds.translate(x - bounds.getCenter().x, y - bounds.getCenter().y);
		if (canClick)
			bounds.rotate(0.003f + new Random().nextInt(2) * 0.001f);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Rflex.shapeRenderer.setColor(Background.getDarkColor());
		bounds.render(Rflex.shapeRenderer);
		Rflex.shapeRenderer.end();

		bounds.setDilation(diameter);

		Rflex.batch.begin();
		Rflex.font_squares_xx.draw(Rflex.batch, this.text, x, y + Rflex.font_squares_xx.getLineHeight() / 3f, Align.center, Align.center, false);
		Rflex.batch.end();
	}

	boolean touched = false;

	public void update(double delta) {
		if (touched && canClick)
			doAction();

		touched = false;

		Vector3 click = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		Rflex.cam.unproject(click);

		if (bounds.contains(new Vector2(click.x, click.y)))
			touched = MultiInput.justTouched();

		if (text.equals("<"))
			touched = MultiInput.leftClicked();
		else if (text.equals(">"))
			touched = MultiInput.rightClicked();
	}

	public abstract void doAction();
}
