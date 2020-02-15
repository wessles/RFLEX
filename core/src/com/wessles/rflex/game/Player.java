package com.wessles.rflex.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.codedisaster.steamworks.SteamAPI;
import com.wessles.mercury.math.RandomUtil;
import com.wessles.mercury.math.geometry.Polygon;
import com.wessles.mercury.math.geometry.Rectangle;
import com.wessles.mercury.utilities.misc.Renderable;
import com.wessles.mercury.utilities.misc.Updatable;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;
import com.wessles.rflex.level.Level;
import com.wessles.rflex.menu.BindControls;
import com.wessles.rflex.menu.LevelSelect;

public class Player implements Updatable, Renderable {
	public static boolean VARIABLE_GAMEPAD_CONTROL = false;

	public Polygon bounds;
	private boolean dead;
	public boolean burnt;
	public boolean smacked;
	private float dx;
	private float dy;
	public Color lockPointColor;

	public Sound resurrectionNoise;
	public Sound deathNoise;

	public Player() {
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		this.dead = false;
		this.burnt = false;
		this.smacked = false;
		this.dx = 0.0f;
		this.dy = 0.0f;
		this.lockPointColor = Color.BLACK.cpy();
		this.lastKill = 0L;
		this.bounds = new Polygon(center.x, center.y, 1f, 5);

		resurrectionNoise = Gdx.audio.newSound(Gdx.files.internal("assets/audio/SFX/resurrection.ogg"));
		deathNoise = Gdx.audio.newSound(Gdx.files.internal("assets/audio/SFX/death.ogg"));
	}

	private float radiusPolarityTarget = 1f;
	public float radiusPolarity = radiusPolarityTarget;

	@Override
	public void update(double delta) {
		if (!isDead() && !inShakyMiniCutScene())
			if (RandomUtil.chance(0.001f) && !LevelSelect.sober) {
				for (Block block : Rflex.game.manager.blocks)
					block.nerf();
				radiusPolarityTarget = -radiusPolarityTarget;
			}

		radiusPolarity += (radiusPolarityTarget - radiusPolarity) / 15f;

		if (Rflex.inGame() && !this.dead) {
			this.dy = 0;
			this.dx = 0;

			if (Rflex.prefs.getBoolean("tutorialized") || Rflex.game.tutorialBashTextManager.done)
				if (MultiInput.onMobile()) {
					Vector2 inputVector = MultiInput.getTouchControls();

					if (inputVector.y < -0.5f)
						this.dy += 1;
					if (inputVector.x < -0.5f)
						this.dx -= 1;
					if (inputVector.y > 0.5f)
						this.dy -= 1;
					if (inputVector.x > 0.5f)
						this.dx += 1;
				} else {
					if (VARIABLE_GAMEPAD_CONTROL && Controllers.getControllers().size > 0) {
						this.dx = Controllers.getControllers().get(0).getAxis(BindControls.AXIS_RIGHT) * BindControls.RIGHT_POLARITY;
						this.dy = Controllers.getControllers().get(0).getAxis(BindControls.AXIS_UP) * BindControls.UP_POLARITY;
					}

					if (MultiInput.up())
						this.dy += 1;
					if (MultiInput.down())
						this.dy -= 1;
					if (MultiInput.left())
						this.dx -= 1;
					if (MultiInput.right())
						this.dx += 1;
				}

			if (!LevelSelect.sober) {
				this.dx *= -radiusPolarity;
				this.dy *= -radiusPolarity;
			}

			this.dx = Math.max(-1, Math.min(1, this.dx));
			this.dy = Math.max(-1, Math.min(1, this.dy));

			if (!Rflex.game.previewMode) {
				for (final Block b : Rflex.game.manager.blocks)
					if (b.bounds.intersects(this.bounds) && !b.nerfed) {
						this.kill();
						this.smacked = true;
					}
			}
		}

		updateBounds(delta);
	}

	public void updateBounds(double delta) {
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		if (!isDead()) {
			this.bounds.translate((float) delta * (center.x + this.dx * Rflex.gridSize - this.bounds.getCenter().x) / 3.5f, (float) delta * (center.y + this.dy * Rflex.gridSize - this.bounds.getCenter().y) / 3.5f);
			this.bounds.rotate((float) delta * 0.05f);
		}

		if (radiusPolarity != 0)
			this.bounds.setDilation(Rflex.gridSize * radiusPolarity / 3f);
	}

	public void render() {
		double delta = Rflex.getDelta();

		boolean shaking = inShakyMiniCutScene();
		double shakeMagnitude = 2f, shakeX = RandomUtil.negpos() * shakeMagnitude * delta, shakeY = RandomUtil.negpos() * shakeMagnitude * delta;
		if (shaking)
			this.bounds.translate((float) shakeX, (float) shakeY);

		Rflex.shapeRenderer.setColor(Color.BLACK.cpy());
		Color bgColor = Rflex.bg.targetColor.cpy();
		Rflex.shapeRenderer.getColor().r = bgColor.r * 0.15f;
		Rflex.shapeRenderer.getColor().g = bgColor.g * 0.15f;
		Rflex.shapeRenderer.getColor().b = bgColor.b * 0.15f;

		this.bounds.render(Rflex.shapeRenderer);

		if (shaking)
			this.bounds.translate((float) -shakeX, (float) -shakeY);
	}

	float lockPointsRotation = 0f, lockPointsRotationTarget = 0f;

	public void renderLockPoints() {
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		lockPointsRotation += (lockPointsRotationTarget - lockPointsRotation) / 10f;
		if (Rflex.levelSelect.currentLevel >= 2)
			if (RandomUtil.chance(0.0012f))
				lockPointsRotationTarget += Math.PI * 0.5f;

		Rflex.shapeRenderer.setColor(this.lockPointColor.cpy());
		if (Rflex.game.previewMode)
			Rflex.shapeRenderer.getColor().a = 0.6f;
		float pointSize = 10 * Rflex.sizeY;
		for (int x = -1; x < 2; ++x)
			for (int y = -1; y < 2; ++y)
				new Rectangle(center.x + x * Rflex.gridSize - pointSize / 2f, center.y + y * Rflex.gridSize - pointSize / 2f, pointSize).rotate(center.x, center.y, lockPointsRotation).render(Rflex.shapeRenderer);
	}

	public boolean isDead() {
		return this.dead;
	}

	private long lastKill;

	public void kill() {
		deathNoise.play(Rflex.SFX_VOLUME);
		Rflex.game.screenShake();

		updateScoreThings();

		this.lastKill = Rflex.currentTimeMillis();
		this.dead = true;
	}

	public void updateScoreThings() {
		if (Rflex.levelSelect.getCurrentLevel() != null) {
			final Level currentLevel = Rflex.levelSelect.getCurrentLevel();

			currentLevel.deaths++;
			Rflex.game.deathTime = Rflex.game.time;
			currentLevel.totalTime += Rflex.game.time;

			long b4BestTime = currentLevel.getBestTime();

			currentLevel.setBestTime(Math.max(currentLevel.getBestTime(), Rflex.game.time));

			long bestTime = currentLevel.getBestTime();

			if (SteamAPI.isSteamRunning() && Rflex.leaderboardMap.size() >= 5 && b4BestTime < bestTime) {
				Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(LevelSelect.sober ? Rflex.levelSelect.currentLevel : Rflex.levelSelect.currentLevel + Rflex.levelSelect.levels.size())).update(bestTime);
				Rflex.Leaderboard.updateQueue = true;
			}

			if (currentLevel.deaths > 0) {
				Rflex.steamUserStats.setAchievement("fom");
				Rflex.steamUserStats.storeStats();
			}

			if (bestTime >= Game.WIN_TIME * 1000f) {
				Rflex.steamUserStats.setAchievement("won_" + (LevelSelect.sober ? Rflex.levelSelect.currentLevel : Rflex.levelSelect.levels.size() + Rflex.levelSelect.currentLevel));
				Rflex.steamUserStats.storeStats();
			}
		}
	}

	public long timeSinceDeath() {
		return Rflex.currentTimeMillis() - this.lastKill;
	}

	public boolean inShakyMiniCutScene() {
		return (Rflex.cinematic || (timeSinceDeath() < 800f || (Rflex.game.justWon && !Rflex.game.bashTextManager.done))) && isDead();
	}

	public void resurrect() {
		this.dead = false;
		this.smacked = false;
		this.burnt = false;
		final float n = 0.0f;
		this.radiusPolarityTarget = radiusPolarity = 1f;
		this.dy = n;
		this.dx = n;
	}
}
