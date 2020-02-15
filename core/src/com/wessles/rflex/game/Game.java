package com.wessles.rflex.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.wessles.mercury.framework.GameState;
import com.wessles.mercury.math.RandomUtil;
import com.wessles.mercury.math.geometry.Polygon;
import com.wessles.mercury.math.geometry.Rectangle;
import com.wessles.mercury.utilities.logging.Logger;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;
import com.wessles.rflex.level.Level;
import com.wessles.rflex.menu.LevelSelect;
import com.wessles.rflex.menu.Menu;

import java.text.DecimalFormat;

public class Game extends GameState {
	public boolean localWin;
	public boolean newRecord;

	public Player player;
	public Manager manager;

	public boolean previewMode;

	public FrameBuffer shadowFrameBuffer, glitchFrameBuffer, topFrameBuffer;
	public ShaderProgram inversionShader;

	public float scaleBase;

	public long lastBeat;
	public float kickRot;

	Sound newRecordNoise, winNoise, boomNoise;

	public Game() {
		this.localWin = false;
		this.newRecord = false;

		this.player = new Player();
		this.manager = new Manager();

		this.previewMode = true;

		resizeFrameBuffers(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		inversionShader = new ShaderProgram(Gdx.files.internal("assets/gfx/shaders/shader.vert"), Gdx.files.internal("assets/gfx/shaders/inversion.frag"));
		Logger.log(inversionShader.getLog() + "\n" + (inversionShader.isCompiled() ? "compiled" : "not compiled"));

		this.scaleBase = 2.0f;

		this.lastBeat = 0L;
		this.kickRot = 0.0f;

		newRecordNoise = Gdx.audio.newSound(Gdx.files.internal("assets/audio/SFX/newrecord.ogg"));
		winNoise = Gdx.audio.newSound(Gdx.files.internal("assets/audio/SFX/beatlevel.ogg"));
		boomNoise = Gdx.audio.newSound(Gdx.files.internal("assets/audio/SFX/boom.mp3"));

		if (Controllers.getControllers().size > 0)
			tutorialBashTextManager = new BashTextManager("-............", "SURVIVE-...", "-.", "60-...", "-.", "SECONDS-.........", "-.", "USE-...", "-.", "JOYSTICK-......", "-.........");
		else
			tutorialBashTextManager = new BashTextManager("-............", "SURVIVE-...", "-.", "60-...", "-.", "SECONDS-.........", "-.", "USE-...", "-.", "WASD-......", "-.", "OR-...", "-.", "ARROW-...", "-.", "KEYS-...", "-.........");
		bashTextManager = new BashTextManager("-.");
	}

	public void resizeFrameBuffers(int width, int height) {
		this.shadowFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
		this.glitchFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
		this.topFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
	}

	public long time = 0, deathTime = 0, start = 0;

	public BashTextManager bashTextManager, tutorialBashTextManager;

	@Override
	public void onEnter() {
		this.previewMode = false;
		this.localWin = false;
		this.newRecord = false;

		this.manager.reset();
		this.player.resurrect();

		if (Rflex.prefs.getBoolean("tutorialized")) {
			Rflex.levelSelect.getCurrentLevel().song.audio.setVolume(0f);
			Rflex.levelSelect.getCurrentLevel().song.play();
		}

		screenShake = 0;
		textAlpha = 0f;
		recordAlpha = 0f;
		winAlpha = 0f;

		time = 0;
		start = Rflex.currentTimeMillis();

		if (Rflex.levelSelect.currentLevel == 0)
			if (LevelSelect.sober)
				bashTextManager = new BashTextManager("-............", "YOU-...", "-.", "ARE-...", "-.", "SLIGHTLY-...", "-.", "ABOVE-...", "-.", "AVERAGE.-...", "-......", "CONGRATS.-......", "-............");
			else
				bashTextManager = new BashTextManager("-............", "YOU-...", "-.", "WERE-...", "-.", "ONLY-...", "-.", "SLIGHTLY-...", "-.", "DRUNK.-...", "-......", "CHEATER.-......", "-............");
		else if (Rflex.levelSelect.currentLevel == 1)
			if (LevelSelect.sober)
				bashTextManager = new BashTextManager("-............", "WOW-...", "-......", "YOU-...", "-.", "ARE-...", "-.", "ALMOST-...", "-.", "GOOD.-...", "-......", "ALMOST.-......", "-............");
			else
				bashTextManager = new BashTextManager("-............", "WOW-...", "-......", "YOU-...", "-.", "ARE-...", "-.", "ALMOST-...", "-.", "DRUNK.-...", "-......", "ALMOST.-......", "-............");
		else if (Rflex.levelSelect.currentLevel == 2)
			if (LevelSelect.sober)
				bashTextManager = new BashTextManager("-............", "YOU-...", "-.", "AREN'T-...", "-.", "THE-...", "-.", "FIRST-...", "-.", "TO-...", "-.", "BEAT-...", "-.", "THIS.-......", "-............");
			else
				bashTextManager = new BashTextManager("-............", "YOU-...", "-.", "AREN'T-...", "-.", "THE-...", "-.", "FIRST-...", "-.", "TO-...", "-.", "DRINK-...", "-.", "THIS.-......", "-............");
		else if (Rflex.levelSelect.currentLevel == 3)
			if (LevelSelect.sober)
				bashTextManager = new BashTextManager("-............", "GOOD-...", "-.", "JOB.-......", "-.", "THIS-...", "-.", "IS-...", "-.", "YOUR-...", "-.", "LIFE-...", "-.", "NOW.-......", "-............");
			else
				bashTextManager = new BashTextManager("-............", "GOOD-...", "-.", "JOB.-......", "-.", "BOOZE-...", "-.", "IS-...", "-.", "YOUR-...", "-.", "LIFE-...", "-.", "NOW.-......", "-............");
		else if (Rflex.levelSelect.currentLevel == 4)
			if (LevelSelect.sober)
				bashTextManager = new BashTextManager("-............", "CONGRATS.-......", "-.", "YOU-...", "-.", "BEAT-...", "-.", "THE-...", "-.", "GAME!-...............", "-..................");
			else
				bashTextManager = new BashTextManager("-............", "CONGRATS.-......", "-.", "YOU-...", "-.", "BEAT-...", "-.", "THE-...", "-.", "GAME!-...............", "-.", "WHILE-...", "-.", "DRUNK.-............", "-.", "WOW.-.........", "-..................");
	}

	@Override
	public void onLeave() {
		this.previewMode = true;
		this.localWin = false;
		Rflex.levelSelect.getCurrentLevel().song.pause();
		this.scaleBase = 2.0f;

		Rflex.levelSelect.getCurrentLevel().lastTime = deathTime;

		if (player.isDead())
			this.player.resurrect();

		progress = 0;
		upsideDown = false;
		textAlpha = 0f;
		recordAlpha = 0f;
		winAlpha = 0f;

		time = 0;
		start = Rflex.currentTimeMillis();
	}

	public boolean upsideDown = false;
	public float upsideDownProgress = 0f;

	public float gameZoom = 1f, deadZoom = 0.0001f;

	String lastGrade = "";

	private boolean leftOrRighted = false, upOrDowned = false;

	public boolean justWon = false;

	@Override
	public void update(double delta) {
		// TODO fix
//		if (Gdx.input.isKeyJustPressed(Input.Keys.Q))
//			Rflex.self.switchGameState(Rflex.editor);

		if (Gdx.graphics.getFramesPerSecond() == 0)
			time += 1000f / 60f;
		else
			time += 1000f / Gdx.graphics.getFramesPerSecond();

		// check for abort
		if (this.previewMode) {
			if (!player.isDead())
				manager.update(delta);

			player.update(delta);

			return;
		}

		if (!Rflex.prefs.getBoolean("tutorialized") && Rflex.game.tutorialBashTextManager.done) {
			if (MultiInput.left() || MultiInput.right())
				leftOrRighted = true;
			if (MultiInput.up() || MultiInput.down())
				upOrDowned = true;
			if (leftOrRighted && upOrDowned) {
				Rflex.prefs.putBoolean("tutorialized", true);
				Rflex.self.switchGameState(Rflex.game);
				return;
			}
		}

		Rflex.levelSelect.getCurrentLevel().song.audio.setVolume(Rflex.MUSIC_VOLUME * Math.min(1f, time / 750f));

		// check for change in state
		if (MultiInput.backed() && Rflex.levelSelect.transitionProgress <= 0f) {
			Menu.exit.play(Rflex.SFX_VOLUME);
			Rflex.self.switchGameState(Rflex.levelSelect);
			return;
		}

		boolean wasDead = player.isDead();

		// update player and game
		this.player.update(delta);
		if (Rflex.prefs.getBoolean("tutorialized"))
			this.manager.update(delta);

		if (!localWin)
			justWon = false;

		// Update game
		if (!this.player.isDead()) {
			// zoom out if alive
			if ((!Rflex.prefs.getBoolean("tutorialized") && !tutorialBashTextManager.done))
				this.scaleBase = (deadZoom + gameZoom) / 2f;
			else
				this.scaleBase = gameZoom;

			if (!Rflex.prefs.getBoolean("tutorialized"))
				return;

			if (!newRecord && time > Rflex.levelSelect.getCurrentLevel().getBestTime() && Rflex.levelSelect.getCurrentLevel().getBestTime() != 0) {
				newRecordNoise.play(Rflex.SFX_VOLUME);
				recordAlpha = 100f;
				winAlpha = 0f;
				newRecord = true;
			}

			if (!localWin && time >= WIN_TIME * 1000f) {
				winNoise.play(Rflex.SFX_VOLUME);
				winAlpha = 100f;
				recordAlpha = 0f;
				localWin = true;
				justWon = Rflex.levelSelect.getCurrentLevel().getBestTime() / 1000f < WIN_TIME;
				screenShake(50);
			}
		} else {
			if (!player.inShakyMiniCutScene()) {
				// zoom in if dead
				this.scaleBase = deadZoom;
				screenShake = 0;
			} else {
				this.scaleBase = (gameZoom + deadZoom + deadZoom) / 3f;
				if (player.isDead() != wasDead)
					Rflex.levelSelect.getCurrentLevel().song.pause();
			}

			// if A / space is pressed, and we win, transition
			if (!(justWon && player.inShakyMiniCutScene()))
				if (MultiInput.accepted() || ((MultiInput.left() || MultiInput.right() || MultiInput.up() || MultiInput.down()) && time - deathTime > 800)) {
					player.resurrectionNoise.play(Rflex.SFX_VOLUME);
					if (Rflex.levelSelect.currentLevel == Rflex.levelSelect.levels.size() - 1 && justWon)
						Rflex.self.switchGameState(Rflex.credits);
					else
						Rflex.self.switchGameState(Rflex.game);
				}
		}

		lastGrade = getGrade();
	}

	private float screenRotation = 0;

	public float screenShake = 0;

	public void screenShake() {
		screenShake(40f);
	}

	public void screenShake(float val) {
		if (Background.isEpileptic)
			screenShake = val / 3f;
		else
			screenShake = val;
	}

	private float deadToTop = 0f;

	private float textAlpha = 0f, recordAlpha = 0f, winAlpha = 0f;

	private float glitchFrameBufferBrightness = 1f;
	private float cinematicBars = 0;

	@Override
	public void render() {
		if (!LevelSelect.sober)
			Rflex.bg.brightness = 0.95f + 0.07f * (float) Math.cos(Rflex.currentTimeMillis() / 700D);
		else
			Rflex.bg.brightness = 1f;

		double delta = Rflex.getDelta();

		Level level = Rflex.levelSelect.getCurrentLevel();

		Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		Rflex.frameBuffer.end();
		shadowFrameBuffer.begin();

		// moving
		Vector3 camTarget;
		if (!player.isDead())
			camTarget = new Vector3(center.x + (player.bounds.getCenter().x - center.x) / 3f, center.y + (player.bounds.getCenter().y - center.y) / 3f, 0);
		else
			camTarget = new Vector3(player.bounds.getCenter().x, player.bounds.getCenter().y, 0);

		Rflex.cam.position.add((camTarget.x - Rflex.cam.position.x) / 8f, (camTarget.y - Rflex.cam.position.y) / 8f, 0);
		Rflex.cam.position.x += (screenShake * Math.sin(Rflex.currentTimeMillis() / 20D * (screenShake / 20f))) * delta;
		Rflex.cam.position.y += (screenShake * Math.cos(Rflex.currentTimeMillis() / 20D * (screenShake / 20f))) * delta;
		screenShake *= 0.92f;

		// zooming
		final float sine = (float) Math.sin(Rflex.time / 30.0 / Math.PI * 2.0);
		Rflex.cam.zoom += delta * ((this.scaleBase - this.scaleBase * 0.13f * sine) + (localWin && !player.isDead() ? -0.15f : 0f) - Rflex.cam.zoom) / 10f;

		// rotation
		Rflex.cam.up.set(0, 1, 0);
		Rflex.cam.direction.set(0, 0, -1);
		if (!player.isDead() || player.inShakyMiniCutScene() || previewMode) {
			float cosine = (float) Math.cos(Rflex.time / Rflex.levelSelect.getCurrentLevel().swayTime);
			final float n = cosine * 360f * Rflex.levelSelect.getCurrentLevel().swayAmount / 100f;
			final float kickRot = this.kickRot * 0.9f;
			this.kickRot = kickRot;
			screenRotation += ((n + kickRot + (this.player.bounds.getCenter().x - center.x + (this.player.bounds.getCenter().y - center.y)) / 10.0f) - screenRotation) / 10f;
			Rflex.cam.rotate(screenRotation);
		}

		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		if (!player.isDead() || player.inShakyMiniCutScene())
			this.player.renderLockPoints();
		this.manager.render();
		if (player.isDead() || !previewMode)
			this.player.render();
		Rflex.shapeRenderer.end();

		shadowFrameBuffer.end();

		Rflex.batch.setProjectionMatrix(Rflex.unscaledCam.combined);
		Rflex.shapeRenderer.setProjectionMatrix(Rflex.unscaledCam.combined);

		glitchFrameBuffer.begin();

		if (!player.inShakyMiniCutScene() || RandomUtil.chance(0.25f))
			Rflex.bg.render();

		Rflex.batch.begin();
		// shadows
		float shadowIntensity = 5f * Rflex.sizeY;
		if (!LevelSelect.sober)
			shadowIntensity = (float) Math.cos(Rflex.currentTimeMillis() / 400D) * 12f * Rflex.sizeY;
		final float sin = (float) Math.sin(Rflex.time / 40.0);
		final float cos = LevelSelect.sober ? (float) Math.cos(Rflex.time / 40.0) : (float) Math.cos(Rflex.time / 50.0);
		if (player.isDead() || !upsideDown)
			upsideDownProgress += -upsideDownProgress / 10f;
		else
			upsideDownProgress += (1f - upsideDownProgress) / 10f;

		Sprite framebufferSprite = new Sprite(shadowFrameBuffer.getColorBufferTexture());
		if (LevelSelect.sober) {
			framebufferSprite.setAlpha(0.5f);
			framebufferSprite.setBounds(cos * shadowIntensity, sin * shadowIntensity + shadowFrameBuffer.getHeight() - shadowFrameBuffer.getHeight() * upsideDownProgress, shadowFrameBuffer.getWidth(), -shadowFrameBuffer.getHeight() + shadowFrameBuffer.getHeight() * upsideDownProgress * 2);
			framebufferSprite.draw(Rflex.batch);
			// game
			framebufferSprite.setAlpha(1f);
			framebufferSprite.setBounds(0, shadowFrameBuffer.getHeight() - shadowFrameBuffer.getHeight() * upsideDownProgress, shadowFrameBuffer.getWidth(), -shadowFrameBuffer.getHeight() + shadowFrameBuffer.getHeight() * upsideDownProgress * 2);
			framebufferSprite.draw(Rflex.batch);
		} else {
			float width = topFrameBuffer.getWidth(), height = -topFrameBuffer.getHeight();
			if (!player.isDead()) {
				width *= 1f + 0.1f * (float) Math.cos(Rflex.currentTimeMillis() / 400D);
				height *= 1f + 0.1f * (float) Math.sin(Rflex.currentTimeMillis() / 500D);
			}
			framebufferSprite.setBounds(center.x - width / 2, center.y - height / 2, width, height);

			framebufferSprite.setAlpha(1f);
			framebufferSprite.draw(Rflex.batch);
		}

		Rflex.batch.end();

		glitchFrameBuffer.end();

		if (previewMode)
			return;

		topFrameBuffer.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (!Rflex.cinematic) {

			Rflex.batch.begin();

			if (player.isDead()) {
				Rflex.font_squares_xxxx.setColor(Rflex.bg.targetColor.r, Rflex.bg.targetColor.g, Rflex.bg.targetColor.b, Rflex.font_squares_xxxx.getColor().a);
				Rflex.font_squares_xxxx.getColor().a *= 0.92f;
				if (RandomUtil.chance(Math.min(1f, (time - deathTime) / 2000f - 0.35f)))
					Rflex.font_squares_xxxx.getColor().a = 1f;
				if (!player.inShakyMiniCutScene())
					deadToTop += (1f - deadToTop) / 20f;
				if (!player.inShakyMiniCutScene()) {
					String whatToSay;
					if (justWon)
						if (Rflex.levelSelect.currentLevel == Rflex.levelSelect.levels.size() - 1)
							whatToSay = "THE END";
						else
							whatToSay = "BEATEN";
					else if (newRecord)
						whatToSay = "HI SCORE";
					else if (localWin)
						whatToSay = "BEATEN";
					else
						whatToSay = "DEAD";
					Rflex.font_squares_xxxx.draw(Rflex.batch, whatToSay, center.x, center.y + (dimension.y * 1.8f / 3f - center.y) * deadToTop + Rflex.font_squares_xxxx.getLineHeight() / 2f + cos * Rflex.font_squares_xxxx.getLineHeight() / 25f, Align.center, Align.center, false);
				}
			} else {
				Rflex.font_squares_xxxx.getColor().a = 0f;
				deadToTop += -deadToTop / 2f;
			}

			Rflex.batch.end();

			if (!player.isDead())
				MultiInput.drawJoyStick();

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			if (!Rflex.prefs.getBoolean("tutorialized")) {
				tutorialBashTextManager.update();
				if (!tutorialBashTextManager.done) {
					Color b4Color = Rflex.font_squares_xxxx.getColor().cpy();

					Rflex.batch.begin();
					Rflex.font_squares_xxxx.setColor(Rflex.bg.targetColor.cpy());
					Rflex.font_squares_xxxx.draw(Rflex.batch, tutorialBashTextManager.getBashText(), center.x, center.y + Rflex.font_squares_xxxx.getLineHeight() / 3f, Align.center, Align.center, false);
					Rflex.batch.end();

					Rflex.font_squares_xxxx.setColor(b4Color);
				}
			} else if (!player.inShakyMiniCutScene()) {
				if (!player.isDead()) {
					float b4BarHeight;
					float barHeight = b4BarHeight = Rflex.font_squares_xx.getLineHeight() / 5f;
					if (Rflex.levelSelect.getCurrentLevel().getBestTime() != 0 && time >= Rflex.levelSelect.getCurrentLevel().getBestTime())
						barHeight *= 1f + 0.4f * Math.sin(Rflex.time / 10f);

					float playerProgress = (time / (WIN_TIME * 1000f));
					Rectangle backBar = new Rectangle(0, b4BarHeight * 2f - barHeight / 2f, dimension.x, barHeight);
					Rectangle progressBar = new Rectangle(center.x, b4BarHeight * 2f - barHeight / 2f, dimension.x, barHeight);
					progressBar.translate(-playerProgress * center.x, 0);

					Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
					Rflex.shapeRenderer.setColor(Rflex.bg.col.cpy());

					// Backbar
					Rflex.shapeRenderer.getColor().mul(0.7f, 0.7f, 0.7f, 1f);
					backBar.render(Rflex.shapeRenderer);

					// Winbar
					Rflex.shapeRenderer.getColor().mul(0.8f, 0.8f, 0.8f, 1f);
					progressBar.duplicate().translate(-playerProgress * center.x, 0f).render(Rflex.shapeRenderer);

					// Player
					Rflex.shapeRenderer.getColor().mul(0.5f, 0.5f, 0.5f, 1f);
					new Polygon(center.x, progressBar.getCenter().y, progressBar.getHeight() * 1.2f, 5).rotate(player.bounds.getRotation()).render(Rflex.shapeRenderer);

					// Record ghost
					Rflex.shapeRenderer.end();
					Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
					Rflex.shapeRenderer.polygon(new Polygon(Math.max(center.x, ((Rflex.levelSelect.getCurrentLevel().getBestTime() / (WIN_TIME * 1000f)) - playerProgress) * dimension.x + center.x), progressBar.getCenter().y, progressBar.getHeight() * (1.2f + 0.225f * (float) Math.sin(Rflex.currentTimeMillis() / 150D)), 5).rotate(player.bounds.getRotation()).getGdxVertices());
					Rflex.shapeRenderer.end();

					Rflex.batch.begin();
					Rflex.font_squares_xx.setColor(Rflex.bg.targetColor.cpy());
					Rflex.font_squares_xx.getColor().r *= 0.4f;
					Rflex.font_squares_xx.getColor().g *= 0.4f;
					Rflex.font_squares_xx.getColor().b *= 0.4f;
					textAlpha += (1f - textAlpha) / 20f;
					Rflex.font_squares_xx.getColor().a = textAlpha;

					// new record
					Rflex.font_squares_xxx.setColor(Rflex.font_squares_xx.getColor().cpy());
					if (!Background.isEpileptic)
						Rflex.font_squares_xxx.getColor().a = Math.sin(Rflex.currentTimeMillis() / 800f) > 0f ? 0.5f : 1f;
					else
						Rflex.font_squares_xxx.getColor().a = 1f;
					recordAlpha += -recordAlpha / 15f;
					Rflex.font_squares_xxx.getColor().a *= Background.isEpileptic ? Math.min(1f, recordAlpha) : recordAlpha;
					Rflex.font_squares_xxx.draw(Rflex.batch, "NEW RECORD", center.x, Rflex.font_squares_xxx.getLineHeight() * 4 / 3f, Align.center, Align.center, false);
					// level beaten
					Rflex.font_squares_xxx.setColor(Rflex.font_squares_xx.getColor().cpy());
					if (!Background.isEpileptic)
						Rflex.font_squares_xxx.getColor().a = Math.sin(Rflex.currentTimeMillis() / 800f) > 0f ? 0.5f : 1f;
					else
						Rflex.font_squares_xxx.getColor().a = 1f;
					winAlpha += -winAlpha / 15f;
					Rflex.font_squares_xxx.getColor().a *= Background.isEpileptic ? Math.min(1f, winAlpha) : winAlpha;
					Rflex.font_squares_xxx.draw(Rflex.batch, "LEVEL BEATEN", center.x, Rflex.font_squares_xxx.getLineHeight() * 4 / 3f, Align.center, Align.center, false);

					// time
					String timeString = " " + new DecimalFormat("#.##").format(time / 1000f);
					Rflex.font_squares_xx.draw(Rflex.batch, timeString, 0, dimension.y - Rflex.font_squares_xx.getLineHeight() / 3f);

					// death count
					String deaths = "try #" + (Rflex.levelSelect.getCurrentLevel().deaths + 1) + "    ";
					Rflex.font_squares_xx.draw(Rflex.batch, deaths, dimension.x, dimension.y - Rflex.font_squares_xx.getLineHeight() / 3f, Align.bottomRight, Align.bottomRight, false);
				} else {
					Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
					Rflex.shapeRenderer.setColor(new Color(1f, 1f, 1f, 0.4f));
					new Polygon(center.y / 3.5f, -center.y / 5f, center.y * 6 / 5f, 7).rotate(-(float) Rflex.time / 90f).render(Rflex.shapeRenderer);
					new Polygon(dimension.x - center.y / 3.5f, -center.y / 5f, center.y * 6 / 5f, 7).rotate((float) Rflex.time / 90f).render(Rflex.shapeRenderer);
					Rflex.shapeRenderer.end();

					Rflex.batch.begin();

					Rflex.font_squares_x.draw(Rflex.batch, Rflex.levelSelect.getCurrentLevel().getBestTime() >= WIN_TIME * 1000f ? "You've beaten this level by  " + getOverallGrade().substring(0, getOverallGrade().length() - 1) + new DecimalFormat("#.##").format(Rflex.levelSelect.getCurrentLevel().getBestTime() / 1000f - 60f) + "[] seconds!" : ("Your high score is only  " + getOverallGrade().substring(0, getOverallGrade().length() - 1) + new DecimalFormat("#.##").format(60f - Rflex.levelSelect.getCurrentLevel().getBestTime() / 1000f) + "[] seconds short!"), center.x, dimension.y - Rflex.font_squares_xx.getLineHeight() / 2f, Align.center, Align.center, false);

					String data = "";
					data += "[#FFFFFF66] TIME:[] " + new DecimalFormat("#.##").format(this.deathTime / 1000f) + "\n";
					data += "[#FFFFFF66] PREV:[] " + (level.lastTime != 0 ? new DecimalFormat("#.##").format(level.lastTime / 1000f) : "N/A") + "\n";
					data += "[#FFFFFF66] BEST:[] " + new DecimalFormat("#.##").format(Rflex.levelSelect.getCurrentLevel().getBestTime() / 1000f) + "\n";
					data += "[#FFFFFF66] AVERAGE:[] " + new DecimalFormat("#.##").format((level.totalTime / Rflex.levelSelect.getCurrentLevel().deaths) / 1000f) + "\n";
					Rflex.font_squares_xx.draw(Rflex.batch, data, 0, Rflex.font_squares_xx.getLineHeight() * 4f);

					String grade = getGrade();
					Rflex.font_squares_xxxx.draw(Rflex.batch, grade, center.x * 1.75f, Rflex.font_squares_xx.getLineHeight() * 2f + Rflex.font_squares_xxxx.getLineHeight() * 1 / 3f, Align.center, Align.center, false);

					String newDeathCount = "" + (level.deaths + 1);
					if (Integer.valueOf(newDeathCount) > 20 || Integer.valueOf(newDeathCount) < 10) {
						if (newDeathCount.endsWith("1"))
							newDeathCount += "st";
						else if (newDeathCount.endsWith("2"))
							newDeathCount += "nd";
						else if (newDeathCount.endsWith("3"))
							newDeathCount += "rd";
						else
							newDeathCount += "th";
					} else
						newDeathCount += "th";

					Rflex.font_squares_x.draw(Rflex.batch, "[#FFFFFFAA]" + (MultiInput.onMobile() ? "tap screen" : ("press " + (Controllers.getControllers().size > 0 ? "<A>" : "space"))) + "\nto try the " + newDeathCount + " time", center.x, center.y, Align.center, Align.center, false);
				}
				Rflex.font_squares_xx.setColor(Color.WHITE);
				Rflex.batch.end();
			} else if (justWon) {
				bashTextManager.update();
				if (!bashTextManager.done) {
					Color b4Color = Rflex.font_squares_xxxx.getColor().cpy();

					Rflex.batch.begin();
					Rflex.font_squares_xxxx.setColor(Rflex.bg.targetColor.cpy());
					Rflex.font_squares_xxxx.draw(Rflex.batch, bashTextManager.getBashText(), center.x, center.y + Rflex.font_squares_xxxx.getLineHeight() / 3f, Align.center, Align.center, false);
					Rflex.batch.end();

					Rflex.font_squares_xxxx.setColor(b4Color);
				}
			}

			Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			Rflex.shapeRenderer.setColor(0f, 0f, 0f, 1f);
			if ((justWon && bashTextManager.currentTextIndex < bashTextManager.text.length - 1 && player.isDead()) || (!Rflex.prefs.getBoolean("tutorialized") && tutorialBashTextManager.currentTextIndex < tutorialBashTextManager.text.length - 1))
				cinematicBars += (1f - cinematicBars) / 15f;
			else
				cinematicBars += -cinematicBars / 15f;
			new Rectangle(0, 0, dimension.x, center.y * cinematicBars / 4f).render(Rflex.shapeRenderer);
			new Rectangle(0, dimension.y, dimension.x, -center.y * cinematicBars / 3f).render(Rflex.shapeRenderer);
			Rflex.shapeRenderer.end();
		}
		topFrameBuffer.end();

		Rflex.frameBuffer.begin();

		if (localWin && !player.isDead())
			Rflex.batch.setShader(inversionShader);

		Rflex.batch.begin();

		if (player.inShakyMiniCutScene() || (!Rflex.prefs.getBoolean("tutorialized") && !tutorialBashTextManager.done)) {
			Rflex.batch.setShader(Rflex.glitchShader);
			if (RandomUtil.chance(0.4f))
				deathTime += delta;
			Rflex.glitchShader.setUniformf("u_time", deathTime);
		} else if (Rflex.prefs.getBoolean("tutorialized") || tutorialBashTextManager.done)
			glitchFrameBufferBrightness += (1f - glitchFrameBufferBrightness) / 30f;

		if ((!Rflex.prefs.getBoolean("tutorialized") && !tutorialBashTextManager.done) || (justWon && player.isDead()))
			glitchFrameBufferBrightness += ((!Rflex.prefs.getBoolean("tutorialized") ? 0.05f : 0.2f) - glitchFrameBufferBrightness) / 30f;


		Rflex.batch.setColor(Rflex.batch.getColor().r * glitchFrameBufferBrightness, Rflex.batch.getColor().g * glitchFrameBufferBrightness, Rflex.batch.getColor().b * glitchFrameBufferBrightness, Rflex.batch.getColor().a);
		Rflex.batch.draw(glitchFrameBuffer.getColorBufferTexture(), 0, glitchFrameBuffer.getHeight(), glitchFrameBuffer.getWidth(), -glitchFrameBuffer.getHeight());
		Rflex.batch.setColor(Rflex.batch.getColor().r / glitchFrameBufferBrightness, Rflex.batch.getColor().g / glitchFrameBufferBrightness, Rflex.batch.getColor().b / glitchFrameBufferBrightness, Rflex.batch.getColor().a);

		Rflex.batch.setShader(null);

		Sprite topFrameBufferSprite = new Sprite(topFrameBuffer.getColorBufferTexture());

		topFrameBufferSprite.setBounds(0, topFrameBuffer.getHeight(), topFrameBuffer.getWidth(), -topFrameBuffer.getHeight());

		if (!LevelSelect.sober) {
			topFrameBufferSprite.setAlpha(0.1f);
			topFrameBufferSprite.setBounds((float) Math.cos(Rflex.currentTimeMillis() / 200D) * 10 * Rflex.sizeY, topFrameBuffer.getHeight() + (float) Math.sin(Rflex.currentTimeMillis() / 500D) * 10 * Rflex.sizeY, topFrameBuffer.getWidth(), -topFrameBuffer.getHeight());
			topFrameBufferSprite.draw(Rflex.batch);

			topFrameBufferSprite.setAlpha(0.1f);
			topFrameBufferSprite.setBounds((float) Math.cos(Rflex.currentTimeMillis() / 400D) * 20 * Rflex.sizeY, topFrameBuffer.getHeight() + (float) Math.sin(Rflex.currentTimeMillis() / 550D) * 20 * Rflex.sizeY, topFrameBuffer.getWidth(), -topFrameBuffer.getHeight());
			topFrameBufferSprite.draw(Rflex.batch);

			topFrameBufferSprite.setBounds((float) Math.cos(Rflex.currentTimeMillis() / 300D) * 5 * Rflex.sizeY, topFrameBuffer.getHeight() + (float) Math.sin(Rflex.currentTimeMillis() / 450D) * 15 * Rflex.sizeY, topFrameBuffer.getWidth(), -topFrameBuffer.getHeight());
		}

		topFrameBufferSprite.setAlpha(1f);
		topFrameBufferSprite.draw(Rflex.batch);
		Rflex.batch.end();

		if (!LevelSelect.sober) {
			Background.screenCurveX += (Background.screenCurve * (1f + 0.25f * (float) Math.cos(Rflex.currentTimeMillis() / 400D)) - Background.screenCurveX) / 15f;
			Background.screenCurveY += (Background.screenCurve * (1f + 0.25f * (float) Math.cos(Rflex.currentTimeMillis() / 500D)) - Background.screenCurveY) / 15f;
		}

		Rflex.batch.setShader(null);
	}

	public static final float WIN_TIME = 60f;

	private float progress = 0;

	public float getProgress() {
		if (Rflex.self.getCurrentState() != Rflex.game)
			return 0;

		if (time / 1000f >= WIN_TIME) {
			float x = (time - WIN_TIME * 1000f) / (WIN_TIME * 1000f);
			float y = -5 * ((float) Math.pow(x, 1.2)) + 6 * x;
			if (time / 1000f <= 2 * WIN_TIME)
				progress = y;
			else
				progress = Math.max(progress, y);
		} else {
			float x = time / (WIN_TIME * 1000f);
			progress = -5 * ((float) Math.pow(x, 1.2)) + 6 * x;
		}

		return progress;
	}

	public String getGrade(long time) {
		String grade;
		if (time < 10000)
			grade = "[#FF0000FF]F";
		else if (time < 20000)
			grade = "[#FF0000FF]E";
		else if (time < 30000)
			grade = "[#FF0000FF]D";
		else if (time < 40000)
			grade = "[#FF0000FF]C";
		else if (time < 50000)
			grade = "[#FFFF55FF]B";
		else if (time < 60000)
			grade = "[#11FF11FF]A";
		else
			grade = "[#FFBB11FF]S";

		return grade;
	}

	public String getGrade() {
		return getGrade(Math.min(time, deathTime));
	}

	public String getOverallGrade() {
		return getGrade(Rflex.levelSelect.getCurrentLevel().getBestTime());
	}

	public static class BashTextManager {
		public boolean done = false;
		long lastSwitch;
		int currentTextIndex = -1;
		String[] text;

		public BashTextManager(String... text) {
			this.text = text;
		}

		public void update() {
			if (currentTextIndex == -1) {
				lastSwitch = Rflex.currentTimeMillis();
				currentTextIndex = 0;
			}

			if (Rflex.currentTimeMillis() - lastSwitch > waitingTime()) {
				lastSwitch = Rflex.currentTimeMillis();
				currentTextIndex++;
				if (currentTextIndex >= text.length)
					done = true;
				if (!done)
					if (!getBashText().equals(""))
						Rflex.game.boomNoise.play(Rflex.SFX_VOLUME);
			}
		}

		public String getBashText() {
			if (currentTextIndex < text.length)
				return text[currentTextIndex].split("-")[0];
			return null;
		}

		public float waitingTime() {
			if (currentTextIndex < text.length)
				return text[currentTextIndex].split("-")[1].length() * 500f / 3f;
			return -1;
		}
	}
}
