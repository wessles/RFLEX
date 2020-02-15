package com.wessles.rflex.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;

import java.util.Calendar;

public class MainMenu extends Menu {
	PolyButton play, credits, exit;

	public MainMenu() {
		super("RFLEX");
		this.actions.add(new Action("PLAY GAME") {
			@Override
			public void doAction() {
				Rflex.levelSelect.currentLevel = 0;
				Rflex.self.switchGameState(Rflex.levelSelect);
			}
		});
		this.actions.add(new Action("OPTIONS") {
			@Override
			public void doAction() {
				Rflex.self.switchGameState(Rflex.optionsMenu);
			}
		});
		this.actions.add(new Action("CREDITS") {
			@Override
			public void doAction() {
				Rflex.self.switchGameState(Rflex.credits);
			}
		});
		this.actions.add(new Action("EXIT") {
			@Override
			public void doAction() {
				Gdx.app.exit();
			}
		});
	}

	@Override
	public void update(double delta) {
		if (MultiInput.backed()) {
			Menu.exit.play(Rflex.SFX_VOLUME);
			Rflex.self.switchGameState(Rflex.titleScreen);
		}

		if (!MultiInput.onMobile())
			super.update(delta);
		else {
			play.update(delta);
			credits.update(delta);
			exit.update(delta);
		}
	}

	public void render() {
		Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		if (!MultiInput.onMobile())
			super.render();
		else {
			Rflex.batch.begin();
			Rflex.font_squares_xxx.setColor(new Color(1f, 0f, 0f, 1f));
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			if(c.get(Calendar.MONTH) == Calendar.APRIL && c.get(Calendar.DAY_OF_MONTH) == 1)
				title = "REFLEX";
			else
				title = "RFLEX";
			Rflex.font_squares_xxx.draw(Rflex.batch, this.title, center.x, dimension.y * 5 / 6f + (float) Math.cos(Rflex.currentTimeMillis() / 1000.0) * 5, Align.center, Align.center, false);
			Rflex.batch.end();

			float entryWidth = dimension.x / 4;
			float currentX = entryWidth;

			play.render(currentX, dimension.y / 3f, entryWidth);
			currentX += entryWidth;
			credits.render(currentX, dimension.y / 3f, entryWidth);
			currentX += entryWidth;
			exit.render(currentX, dimension.y / 3f, entryWidth);
		}

		Rflex.batch.begin();
		Rflex.font_squares_x.draw(Rflex.batch, "[#FFFFFF22]" + (Controllers.getControllers().size > 0 ? "press <A>" : "space/enter"), center.x, center.y / 2f, Align.center, Align.center, false);
		Rflex.batch.end();
	}
}
