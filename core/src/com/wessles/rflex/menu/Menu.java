package com.wessles.rflex.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.wessles.mercury.framework.GameState;
import com.wessles.mercury.math.geometry.Polygon;
import com.wessles.mercury.utilities.misc.Updatable;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Menu extends GameState {
	public static Sound enter, exit, moveCursor;

	public String title;
	protected ArrayList<Action> actions;
	public int selectedAction;

	private PolyButton left, right;

	public Menu(final String title) {
		enter = enter == null ? Gdx.audio.newSound(Gdx.files.internal("assets/audio/SFX/menuenter.ogg")) : enter;
		exit = exit == null ? Gdx.audio.newSound(Gdx.files.internal("assets/audio/SFX/menuexit.ogg")) : exit;
		moveCursor = moveCursor == null ? Gdx.audio.newSound(Gdx.files.internal("assets/audio/SFX/movecursor.ogg")) : moveCursor;

		this.title = title;
		this.actions = new ArrayList<Action>();
		this.selectedAction = 0;

		this.left = new PolyButton("<") {
			@Override
			public void doAction() {
				moveCursor.play(Rflex.SFX_VOLUME);
				--selectedAction;
			}
		};

		this.right = new PolyButton(">") {
			@Override
			public void doAction() {
				moveCursor.play(Rflex.SFX_VOLUME);
				++selectedAction;
			}
		};
	}

	@Override
	public void onEnter() {
		selectedAction = 0;
		LevelSelect.sober = true;
	}

	@Override
	public void update(double delta) {
		left.update(delta);
		right.update(delta);

		if (selectedAction >= actions.size())
			selectedAction = 0;
		else if (selectedAction < 0)
			selectedAction = actions.size() - 1;

		if (MultiInput.accepted()) {
			enter.play(Rflex.SFX_VOLUME);
			this.actions.get(this.selectedAction).doAction();
		}

		this.actions.get(this.selectedAction).update(delta);
	}

	float shapeRotation = 0f;
	public static float depression = 0f;

	@Override
	public void render() {
		Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		Rflex.bg.brightness = 0.1f;
		Rflex.bg.render();
		Rflex.bg.brightness = 1f;

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Rflex.shapeRenderer.setColor(new Color(1f, 1f, 1f, 0.1f));
		shapeRotation += (selectedAction * 6.28f / 7f - shapeRotation) / 3f;
		if (MultiInput.accepted())
			depression += (-0.02f - depression) / 1.5f;
		else
			depression += -depression / 1.5f;
		new Polygon(center.x, -center.y * 2 / 3f, (1f + (float) Math.cos(Rflex.time / 20D) * 0.005f + depression) * dimension.y * 4 / 5f, 7).rotate(shapeRotation).render(Rflex.shapeRenderer);
		Rflex.shapeRenderer.end();

		Rflex.batch.begin();

		Rflex.font_squares_xxxx.setColor(Rflex.bg.targetColor);
		Rflex.font_squares_xxxx.draw(Rflex.batch, this.title, center.x, dimension.y * 7 / 8f + (float) Math.cos(Rflex.currentTimeMillis() / 1000.0) * 5, Align.center, Align.center, false);

		Rflex.batch.end();

		actions.get(selectedAction).render(center.x, dimension.y / 3f, -1);

		left.render(center.y * 0.5f / 3, center.y * 0.5f / 3, center.y / 3);
		right.render(dimension.x - center.y * 0.5f / 3, center.y * 0.5f / 3, center.y / 3f);
	}

	protected abstract class Action implements Updatable {
		protected String name;
		public boolean disabled;

		public Action(final String name) {
			this.disabled = false;
			this.name = name;
		}

		public abstract void doAction();

		public void render(final float x, final float y, float entryWidth) {
			final float xCos = (float) Math.abs(Math.cos(Rflex.currentTimeMillis() / 200.0));
			final boolean selected = this.equals(Menu.this.actions.get(Menu.this.selectedAction));
			Rflex.batch.begin();
			Rflex.font_squares_xx.draw(Rflex.batch, this.name, x, y + (selected ? (xCos * 5.0f) : 0.0f), Align.center, Align.center, false);
			Rflex.batch.end();
		}

		public void update(double delta) {
		}
	}

	protected class ActionSwitcher extends Action {
		public Action[] switches;
		public int currentSwitch;

		public ActionSwitcher(final String name, final Action... switches) {
			super(name);
			this.currentSwitch = 0;
			this.switches = switches;
		}

		@Override
		public final void doAction() {
		}

		@Override
		public void update(double delta) {
			if (this.disabled)
				return;

			final int b4Switch = this.currentSwitch;

			if (MultiInput.downClicked())
				--this.currentSwitch;
			else if (MultiInput.upClicked() || MultiInput.accepted())
				++this.currentSwitch;

			if (this.currentSwitch < 0)
				this.currentSwitch += this.switches.length;
			else if (this.currentSwitch > this.switches.length - 1)
				this.currentSwitch %= this.switches.length;

			if (b4Switch != this.currentSwitch)
				this.switches[this.currentSwitch].doAction();
		}

		public void render(final float x, final float y, float entryWidth) {
			final float xCos = (float) Math.abs(Math.cos(Rflex.currentTimeMillis() / 200.0));
			final boolean selected = this.equals(Menu.this.actions.get(Menu.this.selectedAction));
			Rflex.batch.begin();
			Rflex.font_squares_xx.draw(Rflex.batch, this.name + ": " + this.switches[this.currentSwitch].name, x, y + (selected ? (xCos * 5.0f) : 0.0f), Align.center, Align.center, false);
			Rflex.batch.end();
		}
	}

	protected abstract class PercentAction extends Action {
		public float amount, min, max;

		public PercentAction(final String name, float min, float max) {
			super(name);
			this.amount = -1.0f;
			this.min = min;
			this.max = max;
		}

		public PercentAction(final String name) {
			this(name, 0f, 1f);
		}

		@Override
		public void update(double delta) {
			if (this.disabled)
				return;

			final float b4Amount = this.amount;

			if (MultiInput.down())
				this.amount -= 0.005f;
			else if (MultiInput.up())
				this.amount += 0.005f;

			this.amount = Math.max(min, Math.min(max, this.amount));
			if (b4Amount != this.amount)
				this.doAction();
		}

		public void render(final float x, final float y, float entryWidth) {
			final float xCos = (float) Math.abs(Math.cos(Rflex.currentTimeMillis() / 200.0));
			final boolean selected = this.equals(Menu.this.actions.get(Menu.this.selectedAction));
			Rflex.batch.begin();
			Rflex.font_squares_xx.draw(Rflex.batch, this.name + ": " + new DecimalFormat("#").format(Math.round(amount * 50) * 100 / 50) + "%", x, y + (selected ? (xCos * 5.0f) : 0.0f), Align.center, Align.center, false);
			Rflex.batch.end();
		}
	}

	protected class BooleanAction extends Action {
		boolean bool;

		public BooleanAction(final String name) {
			super(name);
			this.bool = false;
		}

		@Override
		public void update(double delta) {
			if (this.disabled)
				return;

			if (MultiInput.upClicked() || MultiInput.downClicked() || MultiInput.accepted()) {
				this.bool = !this.bool;
				this.doAction();
			}
		}

		@Override
		public void doAction() {
		}

		public void render(final float x, final float y, float entryWidth) {
			final float xCos = (float) Math.abs(Math.cos(Rflex.currentTimeMillis() / 200.0));
			final boolean selected = this.equals(Menu.this.actions.get(Menu.this.selectedAction));
			Rflex.batch.begin();
			Rflex.font_squares_xx.draw(Rflex.batch, this.name + ": " + bool, x, y + (selected ? (xCos * 5.0f) : 0.0f), Align.center, Align.center, false);
			Rflex.batch.end();
		}
	}
}
