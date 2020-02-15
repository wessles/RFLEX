package com.wessles.rflex.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.wessles.rflex.Rflex;
import com.wessles.rflex.menu.LevelSelect;

public class Background {
	public static boolean isEpileptic = false;
	public static float screenCurve = 0.4f, screenCurveX = screenCurve, screenCurveY = screenCurve;

	public Color col;
	public Color targetColor;

	public Background() {
		col = Color.BLACK.cpy();
		targetColor = col.cpy();
	}

	public float brightness = 1f;

	public void render() {
		if (Rflex.inGame()) {
			targetColor = Rflex.levelSelect.getCurrentLevel().backgroundColor.cpy();
			if(!LevelSelect.sober)
				targetColor.add(0.04f, 0.12f, 0.03f, 0);
		} else
			targetColor = Rflex.levelSelect.levels.get(0).backgroundColor.cpy();

		if (Rflex.game.localWin && !Rflex.game.player.isDead())
			targetColor.add((1f - targetColor.r) * 2 / 5f, (1f - targetColor.g) * 2 / 5f, (1f - targetColor.b) * 2 / 5f, 0f);

		if (!isEpileptic) {
			float waveAmplitude = Rflex.game.getProgress();
			if (!Rflex.levelSelect.getCurrentLevel().flash)
				waveAmplitude *= 0.15f;

			targetColor.add((float) Math.sin(Rflex.currentTimeMillis() / 200D) * waveAmplitude, (float) Math.cos(Rflex.currentTimeMillis() / 200D) * waveAmplitude, (float) Math.sin(Rflex.currentTimeMillis() / 200D) * waveAmplitude, 0f);
		}

		col.r = (col.r + (targetColor.r - col.r) / 15.0f);
		col.g = (col.g + (targetColor.g - col.g) / 15.0f);
		col.b = (col.b + (targetColor.b - col.b) / 15.0f);

		Gdx.gl.glClearColor(col.r * brightness, col.g * brightness, col.b * brightness, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	public static Color getDarkColor() {
		Color color = new Color(1f, 1f, 1f, 0.1f);
		color.r *= color.a;
		color.g *= color.a;
		color.b *= color.a;
		Color bgColor = Rflex.bg.targetColor.cpy();
		bgColor.mul(0.15f * 0.9f, 0.15f * 0.9f, 0.15f * 0.9f, 0.9f);
		color.add(bgColor);
		return color;
	}
}