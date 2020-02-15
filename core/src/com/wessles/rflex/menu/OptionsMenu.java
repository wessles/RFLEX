package com.wessles.rflex.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.codedisaster.steamworks.SteamAPI;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;
import com.wessles.rflex.game.Background;
import com.wessles.rflex.game.Player;
import com.wessles.rflex.level.Level;

public class OptionsMenu extends Menu {
	Sound sound;
	PercentAction sfxVolume;
	PercentAction musicVolume;
	PercentAction brightness;
	BooleanAction epileptic;
	BooleanAction uploadScores;
	PercentAction screenCurve;
	BooleanAction fullscreen;
	Action rebindController;
	BooleanAction variableGamepad;
	BooleanAction cinematic;
	public boolean fullscreenChanged = false;

	public OptionsMenu() {
		super("Options");

		sound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/SFX/test.ogg"));

		this.actions.add(this.sfxVolume = new PercentAction("SFX Volume") {
			@Override
			public void doAction() {
				if (MultiInput.upClicked() || MultiInput.downClicked() || Math.round(Rflex.SFX_VOLUME * 20f) / 20f != Math.round(this.amount * 20f) / 20f)
					sound.play(Rflex.SFX_VOLUME);

				Rflex.SFX_VOLUME = this.amount;
			}
		});
		this.actions.add(this.musicVolume = new PercentAction("Music Volume") {
			@Override
			public void doAction() {
				if (MultiInput.upClicked() || MultiInput.downClicked() || Math.round(Rflex.MUSIC_VOLUME * 20f) / 20f != Math.round(this.amount * 20f) / 20f)
					sound.play(Rflex.MUSIC_VOLUME);

				Rflex.MUSIC_VOLUME = this.amount;
			}
		});
		this.actions.add(this.brightness = new PercentAction("Brightness") {
			@Override
			public void doAction() {
				Rflex.brightness = this.amount;
			}
		});
		this.actions.add(this.screenCurve = new PercentAction("Screen Curve", 0, 2f) {
			@Override
			public void doAction() {
				Background.screenCurve = this.amount;
			}
		});
		this.actions.add(this.epileptic = new BooleanAction("Epileptic Friendly") {
			@Override
			public void doAction() {
				if (!bool)
					return;

				Rflex.steamUserStats.setAchievement("epileptic_mode");
				Rflex.steamUserStats.storeStats();
			}
		});
		this.actions.add(this.cinematic = new BooleanAction("Cinematic Mode") {
			@Override
			public void doAction() {
				Rflex.cinematic = bool;
			}
		});
		this.actions.add(this.fullscreen = new BooleanAction("Fullscreen") {
			@Override
			public void doAction() {
				fullscreenChanged = true;
				onLeave();
			}
		});
		this.actions.add(this.variableGamepad = new BooleanAction("Variable Joystick") {
			@Override
			public void doAction() {
				Player.VARIABLE_GAMEPAD_CONTROL = this.bool;
			}
		});
		this.actions.add(rebindController = new Action("rebind gamepad") {
			@Override
			public void doAction() {
				if (Controllers.getControllers().size > 0)
					Rflex.self.switchGameState(Rflex.bindControls);
				else
					name = "no gamepad found";
			}
		});
		this.actions.add(uploadScores = new BooleanAction("Upload Leaderboard Scores") {
			@Override
			public void doAction() {
				Rflex.uploadScores = this.bool;
			}
		});
		this.actions.add(new Action("Clear Scores") {
			@Override
			public void render(final float x, final float y, float entryWidth) {
				final float xCos = (float) Math.abs(Math.cos(Rflex.currentTimeMillis() / 200.0));
				final boolean selected = this.equals(OptionsMenu.this.actions.get(OptionsMenu.this.selectedAction));
				Rflex.batch.begin();
				if (name.toUpperCase().equals("CLEAR SCORES") || name.toUpperCase().equals("CLEARED"))
					Rflex.font_squares_xx.setColor(Color.RED);
				Rflex.font_squares_xx.draw(Rflex.batch, this.name, x, y + (selected ? (xCos * 5.0f) : 0.0f), Align.center, Align.center, false);
				Rflex.font_squares_xx.setColor(Color.WHITE);
				Rflex.batch.end();
			}

			@Override
			public void doAction() {
				if (this.name.equals("CLEARED"))
					return;

				if (!this.name.startsWith("Really")) {
					this.name = "Really?";
					return;
				} else if (!this.name.startsWith("Really really?")) {
					this.name = "Really really?";
					return;
				}

				Menu.exit.play(Rflex.SFX_VOLUME);

				for (Level level : Rflex.levelSelect.levels) {
					level.deaths = 0;
					level.lastTime = 0;
					level.totalTime = 0;
					level.bestTime = 0;
					level.bestDrunkTime = 0;
				}

				if (SteamAPI.isSteamRunning() && Rflex.leaderboardMap.size() >= 5) {
					for (int i = 0; i < Rflex.levelSelect.levels.size() * 2; i++)
						Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(i)).update(0);

					Rflex.Leaderboard.updateQueue = true;
				}

				Rflex.steamUserStats.resetAllStats(true);

				for (int i = 0; i < Rflex.levelSelect.levels.size(); i++)
					Rflex.steamUserStats.clearAchievement("won_" + i);

				this.name = "CLEARED";
			}
		});
		this.actions.add(new Action("Back") {
			@Override
			public void doAction() {
				Rflex.self.switchGameState(Rflex.mainMenu);
			}
		});

		onEnter();
		onLeave();
	}

	@Override
	public void onEnter() {
		super.onEnter();
		this.sfxVolume.amount = Rflex.SFX_VOLUME;
		this.musicVolume.amount = Rflex.MUSIC_VOLUME;
		this.brightness.amount = Rflex.brightness;
		this.fullscreen.bool = Gdx.graphics.isFullscreen();
		this.epileptic.bool = Background.isEpileptic;
		this.uploadScores.bool = Rflex.uploadScores;
		this.screenCurve.amount = Background.screenCurve;
		this.variableGamepad.bool = Player.VARIABLE_GAMEPAD_CONTROL;
		this.cinematic.bool = Rflex.cinematic;
		rebindController.name = "rebind controller";
	}

	@Override
	public void onLeave() {
		Rflex.prefs.putFloat("sfx_volume", sfxVolume.amount);
		Rflex.prefs.putFloat("music_volume", musicVolume.amount);
		Rflex.prefs.putFloat("brightness", brightness.amount);
		Rflex.prefs.putBoolean("fullscreen", this.fullscreen.bool);
		Rflex.prefs.putBoolean("is_epileptic", this.epileptic.bool);
		Rflex.prefs.putBoolean("upload_scores", this.uploadScores.bool);
		Rflex.prefs.putFloat("screenCurve", this.screenCurve.amount);
		Rflex.prefs.putBoolean("variable_gamepad_control", this.variableGamepad.bool);
		Rflex.prefs.putBoolean("cinematic", this.cinematic.bool);
		BindControls.savePrefs();

		Rflex.self.loadPreferences();
		fullscreenChanged = false;

		actions.get(actions.size() - 2).name = "Clear Scores";
	}

	@Override
	public void update(double delta) {
		super.update(delta);
		if (MultiInput.backed()) {
			Menu.exit.play(Rflex.SFX_VOLUME);
			Rflex.self.switchGameState(Rflex.mainMenu);
		}
	}

	@Override
	public void render() {
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		super.render();
		Rflex.batch.begin();
		Rflex.font_squares_x.draw(Rflex.batch, "[#FFFFFF22]" + (super.selectedAction >= actions.size() - 2 ? (Controllers.getControllers().size > 0 ? "press <A>" : "space/enter") : "up/down"), center.x, center.y / 2f, Align.center, Align.center, false);
		Rflex.batch.end();
	}
}
