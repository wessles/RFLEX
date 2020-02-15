package com.wessles.rflex;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.wessles.mercury.math.geometry.Polygon;

public class MultiInput {

	public static void create() {
		Rflex.multiplexer.addProcessor(new InputProcessor() {
			@Override
			public boolean keyDown(int keycode) {
				if (keycode == Input.Keys.BACK)
					justBack = true;
				return true;
			}

			@Override
			public boolean keyUp(int keycode) {
				return false;
			}

			@Override
			public boolean keyTyped(char character) {
				return false;
			}

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if (Rflex.inGame())
					joyStickAnchor = new Vector2(Gdx.input.getX(0), Gdx.input.getY(0));
				justTouched = true;
				return true;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				return false;
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				return false;
			}

			@Override
			public boolean mouseMoved(int screenX, int screenY) {
				return false;
			}

			@Override
			public boolean scrolled(int amount) {
				return false;
			}
		});
	}

	public static boolean onMobile() {
		return Gdx.app.getType() == Application.ApplicationType.Android;
//		return true;
	}

	public static boolean cAccept = false, cBack = false, cAlt = false, cCtrled = false;

	public static boolean accepted() {
		boolean cAccept = MultiInput.cAccept;
		MultiInput.cAccept = false;
		return !onMobile() ? Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || cAccept : justTouched();
	}

	public static boolean backed() {
		boolean cBack = MultiInput.cBack;
		MultiInput.cBack = false;
		return !onMobile() ? Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || cBack : justBack();
	}

	public static boolean alt() {
		boolean cAlt = MultiInput.cAlt;
		MultiInput.cAlt = false;
		return Gdx.input.isKeyJustPressed(Input.Keys.TAB) || cAlt;
	}

	public static boolean ctrled() {
		boolean cCtrled = MultiInput.cCtrled;
		MultiInput.cCtrled = false;
		return Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_RIGHT) || cCtrled;
	}

	public static boolean justTouched = false;

	public static boolean justTouched() {
		boolean justTouched = MultiInput.justTouched;
		MultiInput.justTouched = false;
		return justTouched;
	}

	private static boolean justBack = false;

	public static boolean justBack() {
		boolean justBack = MultiInput.justBack;
		MultiInput.justBack = false;
		return justBack;
	}

	public static boolean cUp = false, cDown = false, cLeft = false, cRight = false;
	public static boolean cUpped = false, cDowned = false, cLefted = false, cRighted = false;

	public static boolean up() {
		return Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W) || cUp;
	}

	public static boolean down() {
		return Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S) || cDown;
	}

	public static boolean left() {
		return Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A) || cLeft;
	}

	public static boolean right() {
		return Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D) || cRight;
	}

	public static boolean upClicked() {
		boolean cUp = MultiInput.cUp && !cUpped;
		if (cUp)
			cUpped = true;
		return Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W) || cUp;
	}

	public static boolean downClicked() {
		boolean cDown = MultiInput.cDown && !cDowned;
		if (cDown)
			cDowned = true;
		return Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S) || cDown;
	}

	public static boolean leftClicked() {
		boolean cLeft = MultiInput.cLeft && !cLefted;
		if (cLeft)
			cLefted = true;
		return Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A) || cLeft;
	}

	public static boolean rightClicked() {
		boolean cRight = MultiInput.cRight && !cRighted;
		if (cRight)
			cRighted = true;
		return Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D) || cRight;
	}

	public static Vector2 joyStickAnchor = null;

	public static Vector2 getTouchControls() {
		if (Gdx.input.isTouched() && onMobile() && joyStickAnchor != null) {
			joyStickAnchor.x = Math.min(Gdx.graphics.getWidth() - getJoyStickRadius(), Math.max(getJoyStickRadius(), joyStickAnchor.x));
			joyStickAnchor.y = Math.min(Gdx.graphics.getHeight() - getJoyStickRadius(), Math.max(getJoyStickRadius(), joyStickAnchor.y));
			Vector2 touchPos = new Vector2(Gdx.input.getX(0), Gdx.input.getY(0));
			touchPos.sub(joyStickAnchor);
			touchPos.scl(1 / (getJoyStickRadius()));
			touchPos.x = Math.min(1f, Math.max(-1f, touchPos.x));
			touchPos.y = Math.min(1f, Math.max(-1f, touchPos.y));
			return touchPos;
		} else
			return new Vector2(0, 0);
	}

	public static void drawJoyStick() {
		if (!onMobile() || joyStickAnchor == null)
			return;

		Vector2 unprojectedJoyStickAnchor = new Vector2(joyStickAnchor.x, -joyStickAnchor.y + Gdx.graphics.getHeight());
		Vector2 unprojectedTouch = new Vector2(Gdx.input.getX(0), -Gdx.input.getY(0) + Gdx.graphics.getHeight());
		if (!Gdx.input.isTouched() || !onMobile())
			unprojectedTouch = unprojectedJoyStickAnchor.cpy();

		unprojectedTouch.x = Math.min(unprojectedJoyStickAnchor.x + getJoyStickRadius() * 3f / 4f, Math.max(unprojectedJoyStickAnchor.x - getJoyStickRadius() * 3f / 4f, unprojectedTouch.x));
		unprojectedTouch.y = Math.min(unprojectedJoyStickAnchor.y + getJoyStickRadius() * 3f / 4f, Math.max(unprojectedJoyStickAnchor.y - getJoyStickRadius() * 3f / 4f, unprojectedTouch.y));

		Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Rflex.shapeRenderer.setColor(new Color(0.1f, 0.1f, 0.1f, 0.3f));
		new Polygon(unprojectedJoyStickAnchor.x, unprojectedJoyStickAnchor.y, getJoyStickRadius(), 14).render(Rflex.shapeRenderer);
		Rflex.shapeRenderer.getColor().a = 0.4f;
		new Polygon(unprojectedTouch.x, unprojectedTouch.y, getJoyStickRadius() / 2.5f, 5).render(Rflex.shapeRenderer);
		Rflex.shapeRenderer.end();
	}

	private static float getJoyStickRadius() {
		return Gdx.graphics.getWidth() / 8f;
	}

	public static void clear() {
		justTouched = false;
		justBack = false;
	}
}
