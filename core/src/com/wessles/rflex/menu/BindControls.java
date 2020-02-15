package com.wessles.rflex.menu;

import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.wessles.mercury.framework.GameState;
import com.wessles.rflex.Rflex;

import java.util.ArrayList;

public class BindControls extends GameState {

	public static int
			BUTTON_X = 2,
			BUTTON_Y = 3,
			BUTTON_A = 0,
			BUTTON_B = 1,
			AXIS_LEFT = 1, LEFT_POLARITY = -1,
			AXIS_RIGHT = 1,  RIGHT_POLARITY = 1,
			AXIS_UP = 0, UP_POLARITY = -1,
			AXIS_DOWN = 0, DOWN_POLARITY = 1;

	public static abstract class Binding {
		public final String name;

		public Binding(String name) {
			this.name = name;
		}

		public abstract void check();

		public abstract void apply();
	}

	public static abstract class AxisBinding extends Binding {
		public AxisBinding(String name) {
			super(name);
		}

		public int axis = -1, polarity = -1;

		@Override
		public void check() {
			if (Controllers.getControllers().size > 0) {
				if (BindControls.axis != -1) {
					axis = BindControls.axis;
					polarity = BindControls.polarity;

					clearInputs();

					apply();
				}
			}
		}

		public abstract void apply();
	}

	public static abstract class ButtonBinding extends Binding {
		public ButtonBinding(String name) {
			super(name);
		}

		public int button = -1;

		@Override
		public void check() {
			if (Controllers.getControllers().size > 0) {
				if (BindControls.button != -1) {
					button = BindControls.button;

					clearInputs();

					apply();
				}
			}
		}

		public abstract void apply();
	}

	public ArrayList<Binding> bindings = new ArrayList<>();

	public BindControls() {
		bindings.add(new AxisBinding("left") {
			@Override
			public void apply() {
				AXIS_LEFT = axis;
				LEFT_POLARITY = polarity;
				Rflex.prefs.putInteger("axis_left", AXIS_LEFT);
				currentBind++;
			}
		});
		bindings.add(new AxisBinding("right") {
			@Override
			public void apply() {
				AXIS_RIGHT = axis;
				RIGHT_POLARITY = polarity;
				Rflex.prefs.putInteger("axis_right", AXIS_RIGHT);
				currentBind++;
			}
		});
		bindings.add(new AxisBinding("up") {
			@Override
			public void apply() {
				AXIS_UP = axis;
				UP_POLARITY = polarity;
				Rflex.prefs.putInteger("axis_up", AXIS_UP);
				currentBind++;
			}
		});
		bindings.add(new AxisBinding("down") {
			@Override
			public void apply() {
				AXIS_DOWN = axis;
				DOWN_POLARITY = polarity;
				Rflex.prefs.putInteger("axis_down", AXIS_DOWN);
				currentBind++;
			}
		});
		bindings.add(new ButtonBinding("<A>") {
			@Override
			public void apply() {
				BUTTON_A = button;
				Rflex.prefs.putInteger("button_a", BUTTON_A);
				currentBind++;
			}
		});
		bindings.add(new ButtonBinding("<B>") {
			@Override
			public void apply() {
				BUTTON_B = button;
				Rflex.prefs.putInteger("button_b", BUTTON_B);
				currentBind++;
			}
		});
		bindings.add(new ButtonBinding("<X>") {
			@Override
			public void apply() {
				BUTTON_X = button;
				Rflex.prefs.putInteger("button_x", BUTTON_X);
				currentBind++;
			}
		});
		bindings.add(new ButtonBinding("<Y>") {
			@Override
			public void apply() {
				BUTTON_Y = button;
				Rflex.prefs.putInteger("button_y", BUTTON_Y);
				currentBind++;
			}
		});
	}

	public int currentBind = 0;

	@Override
	public void onEnter() {
		clearInputs();
		currentBind = 0;
	}

	public void update(double delta) {
		bindings.get(currentBind).check();
		if (currentBind >= bindings.size())
			Rflex.self.switchGameState(Rflex.optionsMenu);
	}

	public void render() {
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);
		Rflex.bg.brightness = 0.2f;
		Rflex.bg.render();
		Rflex.bg.brightness = 1f;
		Rflex.batch.begin();
		Rflex.font_squares_xxx.draw(Rflex.batch, bindings.get(currentBind).name, center.x, center.y, Align.center, Align.center, false);
		Rflex.batch.end();
	}

	public static int axis = -1, polarity = -1, button = -1;

	public static void setLastAxis(int axis, int polarity) {
		BindControls.axis = axis;
		BindControls.polarity = polarity;
	}

	public static void setLastPressed(int button) {
		BindControls.button = button;
	}

	public static void clearInputs() {
		axis = -1;
		polarity = -1;
		button = -1;
	}

	public static void loadPrefs() {
		if (Rflex.prefs.contains("axis_left"))
			AXIS_LEFT = Rflex.prefs.getInteger("axis_left");
		if (Rflex.prefs.contains("axis_right"))
			AXIS_RIGHT = Rflex.prefs.getInteger("axis_right");
		if (Rflex.prefs.contains("axis_up"))
			AXIS_UP = Rflex.prefs.getInteger("axis_up");
		if (Rflex.prefs.contains("axis_down"))
			AXIS_DOWN = Rflex.prefs.getInteger("axis_down");

		if (Rflex.prefs.contains("left_polarity"))
			LEFT_POLARITY = Rflex.prefs.getInteger("left_polarity");
		if (Rflex.prefs.contains("right_polarity"))
			RIGHT_POLARITY = Rflex.prefs.getInteger("right_polarity");
		if (Rflex.prefs.contains("up_polarity"))
			UP_POLARITY = Rflex.prefs.getInteger("up_polarity");
		if (Rflex.prefs.contains("down_polarity"))
			DOWN_POLARITY = Rflex.prefs.getInteger("down_polarity");

		if (Rflex.prefs.contains("button_a"))
			BUTTON_A = Rflex.prefs.getInteger("button_a");
		if (Rflex.prefs.contains("button_b"))
			BUTTON_B = Rflex.prefs.getInteger("button_b");
		if (Rflex.prefs.contains("button_x"))
			BUTTON_X = Rflex.prefs.getInteger("button_x");
		if (Rflex.prefs.contains("button_y"))
			BUTTON_Y = Rflex.prefs.getInteger("button_y");
	}

	public static void savePrefs() {
		Rflex.prefs.putInteger("axis_left", AXIS_LEFT);
		Rflex.prefs.putInteger("axis_right", AXIS_RIGHT);
		Rflex.prefs.putInteger("axis_up", AXIS_UP);
		Rflex.prefs.putInteger("axis_down", AXIS_DOWN);

		Rflex.prefs.putInteger("left_polarity", LEFT_POLARITY);
		Rflex.prefs.putInteger("right_polarity", RIGHT_POLARITY);
		Rflex.prefs.putInteger("up_polarity", UP_POLARITY);
		Rflex.prefs.putInteger("down_polarity", DOWN_POLARITY);

		Rflex.prefs.putInteger("button_a", BUTTON_A);
		Rflex.prefs.putInteger("button_b", BUTTON_B);
		Rflex.prefs.putInteger("button_x", BUTTON_X);
		Rflex.prefs.putInteger("button_y", BUTTON_Y);
	}
}
