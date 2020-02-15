package com.wessles.rflex.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.wessles.mercury.framework.GameState;
import com.wessles.mercury.math.geometry.Rectangle;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;

import java.util.Calendar;
import java.util.Date;

public class TitleScreen extends GameState {
	public void onEnter() {
		Gdx.input.setCatchBackKey(false);
	}

	public void onLeave() {
		Gdx.input.setCatchBackKey(true);
	}

	public void update(double delta) {
		if (MultiInput.accepted()) {
			Menu.enter.play(Rflex.SFX_VOLUME);
			Rflex.self.switchGameState(Rflex.mainMenu);
		} else if (MultiInput.backed()) {
			Menu.exit.play(Rflex.SFX_VOLUME);
			Gdx.app.exit();
		}
	}

	public void render() {
		Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		Rflex.bg.brightness = 0.1f;
		Rflex.bg.render();
		Rflex.bg.brightness = 1f;

		Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Rflex.shapeRenderer.setColor(0f, 0f, 0f, 0.1f);
		new Rectangle(0, 0, dimension.x, Rflex.font_squares_o.getLineHeight() * 2).render(Rflex.shapeRenderer);
		Rflex.shapeRenderer.end();

		Rflex.batch.begin();
		double sine = Math.sin(Rflex.time / 100D);
		Rflex.font_squares_xxxx.setColor(Rflex.bg.targetColor);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		String title;
		if(c.get(Calendar.MONTH) == Calendar.APRIL && c.get(Calendar.DAY_OF_MONTH) == 1)
			title = "REFLEX";
		else
			title = "RFLEX";

		Rflex.font_squares_xxxx.draw(Rflex.batch, title, center.x, dimension.y * 7 / 8 - Rflex.font_squares_xxxx.getLineHeight() / 2 + (float) sine * Rflex.font_squares_xxxx.getLineHeight() / 15f, Align.center, Align.center, false);
		sine = Math.sin(Rflex.time / 750D);
		Rflex.font_squares_xx.draw(Rflex.batch, "[#FFFFFF22]" + (MultiInput.onMobile() ? "tap screen" : ("press " + (Controllers.getControllers().size > 0 ? "<A>" : "space"))), center.x, dimension.y * 3 / 8 + (float) sine * Rflex.font_squares_xxxx.getLineHeight() / 30f, Align.center, Align.center, false);

		Rflex.font_squares_o.getColor().a = 0.1f;
		Rflex.font_squares_o.draw(Rflex.batch, " version " + Rflex.VERSION, 0, Rflex.font_squares_o.getLineHeight() * 1.35f);
		Rflex.font_squares_o.draw(Rflex.batch, "(C) system void games 2015      ", dimension.x, Rflex.font_squares_o.getLineHeight() * 1.35f, Align.right, Align.right, false);
		Rflex.font_squares_o.draw(Rflex.batch, Controllers.getControllers().size > 0 ? "joystick detected" : "no joystick", center.x, Rflex.font_squares_o.getLineHeight() * 1.35f, Align.center, Align.center, false);
		Rflex.batch.end();
	}
}
