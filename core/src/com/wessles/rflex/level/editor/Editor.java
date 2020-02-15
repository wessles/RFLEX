package com.wessles.rflex.level.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.wessles.mercury.framework.GameState;
import com.wessles.mercury.math.geometry.Figure;
import com.wessles.mercury.math.geometry.Polygon;
import com.wessles.mercury.math.geometry.Rectangle;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;
import com.wessles.rflex.game.Block;
import com.wessles.rflex.level.Level;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class Editor extends GameState {
	private Level level;

	private int currentPattern;
	private int currentFrame;

	private int cursorX;
	private int cursorY;
	private boolean showPreviewFrame;

	private boolean showHelp;
	private float helpProgress;

	private int currentProperty;
	private Property[] properties;

	public Editor() {
		this.currentFrame = 0;
		this.showPreviewFrame = false;
		this.showHelp = false;
		this.helpProgress = 0.0f;
	}

	@Override
	public void onEnter() {
		level = Rflex.levelSelect.getCurrentLevel();
		properties = new Property[]{
				new Property("blockSpeed", 0.25f) {
					@Override
					public void step(int mult) {
						level.blockSpeed += step * mult;
					}

					public float get() {
						return level.blockSpeed;
					}
				},
				new Property("finalBlockSpeed", 0.25f) {
					@Override
					public void step(int mult) {
						level.finalBlockSpeed += step * mult;
					}

					public float get() {
						return level.finalBlockSpeed;
					}
				},
				new Property("completionistSpeed", 0.25f) {
					@Override
					public void step(int mult) {
						level.completionistSpeed += step * mult;
					}

					public float get() {
						return level.completionistSpeed;
					}
				},
				new Property("flip", 1) {
					@Override
					public void step(int mult) {
						level.flip = !level.flip;
					}

					public float get() {
						return level.flip ? 1f : 0f;
					}
				},
				new Property("flash", 1) {
					@Override
					public void step(int mult) {
						level.flash = !level.flash;
					}

					public float get() {
						return level.flash ? 1f : 0f;
					}
				},
				new Property("rotatingChance", 0.1f) {
					@Override
					public void step(int mult) {
						level.rotatingChance += step * mult;
					}

					public float get() {
						return level.rotatingChance;
					}
				},
				new Property("rotationKick", 1) {
					@Override
					public void step(int mult) {
						level.rotKick += step * mult;
					}

					public float get() {
						return level.rotKick;
					}
				},
				new Property("swayTime", 1) {
					@Override
					public void step(int mult) {
						level.swayTime += step * mult;
					}

					public float get() {
						return level.swayTime;
					}
				},
				new Property("swayAmount", 1) {
					@Override
					public void step(int mult) {
						level.swayAmount += step * mult;
					}

					public float get() {
						return level.swayAmount;
					}
				},
				new Property("color_r", 0.05f) {
					@Override
					public void step(int mult) {
						level.backgroundColor.r += step * mult;
						level.backgroundColor.r = Math.round(level.backgroundColor.r * 100f) / 100f;
					}

					@Override
					public float get() {
						return level.backgroundColor.r;
					}
				},
				new Property("color_g", 0.05f) {
					@Override
					public void step(int mult) {
						level.backgroundColor.g += step * mult;
						level.backgroundColor.g = Math.round(level.backgroundColor.g * 100f) / 100f;
					}

					@Override
					public float get() {
						return level.backgroundColor.g;
					}
				},
				new Property("color_b", 0.05f) {
					@Override
					public void step(int mult) {
						level.backgroundColor.b += step * mult;
						level.backgroundColor.b = Math.round(level.backgroundColor.b * 100f) / 100f;
					}

					@Override
					public float get() {
						return level.backgroundColor.b;
					}
				}
		};

		Rflex.game.manager.reset();
		Rflex.game.player.kill();
		this.showPreviewFrame = true;

		currentPattern = 0;
		currentFrame = 0;
	}

	@Override
	public void onLeave() {
		Rflex.levelSelect.getCurrentLevel().save();
		this.currentFrame = 0;
	}

	@Override
	public void update(double delta) {
		if(Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
			Rflex.self.switchGameState(Rflex.levelSelect);
			return;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
			if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {

				int newFrame = this.currentFrame;
				int newPattern = this.currentPattern;
				if (MultiInput.leftClicked())
					--newFrame;
				else if (MultiInput.rightClicked())
					++newFrame;
				else if (MultiInput.downClicked())
					--newPattern;
				else if (MultiInput.upClicked())
					++newPattern;
				else if (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)) {
					getPattern().frames.remove(this.currentFrame);
					--newFrame;
					newFrame = Math.max(0, Math.min(getPattern().frames.size() - 1, newFrame));
					this.showPreviewFrame = true;
				} else if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
					level.patterns.remove(this.currentPattern);
					--newPattern;
					newPattern = Math.max(0, Math.min(level.patterns.size() - 1, newPattern));
					this.showPreviewFrame = true;
				}

				if (this.currentPattern != newPattern)
					this.showPreviewFrame = true;

				this.setCurrentPattern(newPattern);

				if (this.currentFrame != newFrame)
					this.showPreviewFrame = true;
				else
					newFrame = Math.max(0, Math.min(getPattern().frames.size() - 1, newFrame));

				this.setCurrentFrame(newFrame);

			} else {
				if (MultiInput.downClicked())
					++currentProperty;
				else if (MultiInput.upClicked())
					--currentProperty;

				if (currentProperty < 0)
					currentProperty += properties.length;
				else if (currentProperty >= properties.length)
					currentProperty -= properties.length;

				if (MultiInput.leftClicked())
					properties[currentProperty].step(-1);
				else if (MultiInput.rightClicked())
					properties[currentProperty].step(1);

			}
		} else if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
			this.removeBlock();

			this.showPreviewFrame = true;

		} else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {

			int side;
			int pos;

			if (MultiInput.leftClicked()) {
				side = 3;
				pos = -this.cursorY;
			} else if (MultiInput.rightClicked()) {
				side = 1;
				pos = this.cursorY;
			} else if (MultiInput.downClicked()) {
				side = 2;
				pos = this.cursorX;
			} else {
				if (!MultiInput.upClicked())
					return;

				side = 0;
				pos = -this.cursorX;
			}

			final Block.BlockData blockData = new Block.BlockData(pos, side);
			this.addBlock(blockData);

			this.showPreviewFrame = true;

		} else if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
			this.showPreviewFrame = true;
		else if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
			this.showHelp = !this.showHelp;
		else if (MultiInput.leftClicked())
			--this.cursorX;
		else if (MultiInput.rightClicked())
			++this.cursorX;
		else if (MultiInput.downClicked())
			--this.cursorY;
		else if (MultiInput.upClicked())
			++this.cursorY;

		if (this.showPreviewFrame) {
			Rflex.game.manager.blocks.clear();
			Rflex.game.manager.parseFrame(this.getFrame());
		}

		this.showPreviewFrame = false;

		Rflex.game.manager.update(delta);
		if (MultiInput.backed())
			Rflex.self.switchGameState(Rflex.game);

	}

	final String help = "" +
			"arrow keys                    move cursor\n" +
			"SHIFT + arrow keys            create block in direction\n" +
			"\n" +
			"alt + arrow keys              destroy block in given direction\n" +
			"\n" +
			"control + up/down             navigate properties\n" +
			"control + left/right          modify properties\n" +
			"\n" +
			"control + SHIFT + up/down     go back/forward/create a pattern\n" +
			"control + SHIFT + left/right  go back/forward/create a frame\n" +
			"control + SHIFT + delete      delete current frame\n" +
			"control + SHIFT + backspace   delete current pattern\n";

	@Override
	public void render() {
		Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		Rflex.cam.zoom = 2f;

		if (!this.showHelp) {
			Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

			Rflex.game.player.renderLockPoints();

			Figure.lineWidth = 2f * Rflex.sizeY;

			Polygon cursorBounds = new Polygon(center.x + this.cursorX * Rflex.gridSize, center.y + this.cursorY * Rflex.gridSize, Rflex.sizeY * 20.0f, 5);
			Rflex.shapeRenderer.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));
			cursorBounds.render(Rflex.shapeRenderer);

			Rflex.game.manager.render();

			Rflex.shapeRenderer.end();
		}

		Rflex.batch.setProjectionMatrix(Rflex.unscaledCam.combined);
		Rflex.shapeRenderer.setProjectionMatrix(Rflex.unscaledCam.combined);

		if (this.showHelp)
			this.helpProgress += (help.length() - this.helpProgress) / 3.0f;
		else
			this.helpProgress -= this.helpProgress / 3.0f;

		Rflex.batch.begin();
		Rflex.font_squares_x.draw(Rflex.batch, this.help.substring(0, (int) helpProgress), 0, dimension.y - Rflex.font_squares_x.getLineHeight() / 3f);
		Rflex.font_squares_x.draw(Rflex.batch, this.showHelp ? "F1 to disable help" : "F1 for help", 0, Rflex.font_squares_x.getLineHeight() * 2f / 3f);
		Rflex.batch.end();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		if (!this.showHelp) {
			Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

			Rflex.shapeRenderer.setColor(new Color(0f, 0f, 0f, 0.2f));
			Rflex.shapeRenderer.rect(0, dimension.y, dimension.x, -Rflex.font_squares_x.getLineHeight());

			Rflex.shapeRenderer.getColor().a = 0.3f;

			float frameBarWidth = dimension.x / 2f;
			float frameIconWidth = frameBarWidth / getPattern().frames.size();
			Rflex.shapeRenderer.rect(dimension.x / 2f - frameBarWidth / 2f, dimension.y, frameBarWidth, -Rflex.font_squares_x.getLineHeight());

			for (int f = 0; f < getPattern().frames.size(); f++) {
				Rflex.shapeRenderer.setColor(Color.BLACK);
				Rflex.shapeRenderer.getColor().a = (f % 2) * 0.2f + 0.1f;

				Rflex.shapeRenderer.rect(dimension.x / 2f - frameBarWidth / 2f + frameIconWidth * f, dimension.y, frameIconWidth, -Rflex.font_squares_x.getLineHeight());

				if (f == currentFrame) {
					Rflex.shapeRenderer.setColor(Color.GRAY);
					new Rectangle(dimension.x / 2f - frameBarWidth / 2f + frameIconWidth * f + frameIconWidth / 2f - (Rflex.font_squares_x.getLineHeight() / 3f) / 2f, dimension.y - Rflex.font_squares_x.getLineHeight() / 3f, Rflex.font_squares_x.getLineHeight() / 3f, -Rflex.font_squares_x.getLineHeight() / 3f).rotate((float) Rflex.time / 200f).render(Rflex.shapeRenderer);
				}
			}

			Rflex.shapeRenderer.end();

			Rflex.batch.begin();

			Rflex.batch.setColor(Color.WHITE.cpy());

			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			String prop = properties[currentProperty].name;
			String propValue = "" + decimalFormat.format(properties[currentProperty].get());

			Rflex.font_squares_x.draw(Rflex.batch, " " + prop + "=" + propValue, 0, dimension.y - Rflex.font_squares_x.getLineHeight() / 3f);
			String patternIndexString = "pattern " + (currentPattern + 1) + "/" + level.patterns.size() + "   ";
			Rflex.font_squares_x.draw(Rflex.batch, patternIndexString, dimension.x, dimension.y - Rflex.font_squares_x.getLineHeight() / 3f, Align.right, Align.top, false);

			Rflex.batch.end();
		}
	}

	public boolean setCurrentFrame(final int frame) {
		final int oldFrame = this.currentFrame;
		this.currentFrame = frame;
		this.getFrame();
		if (frame != oldFrame) {
			Rflex.game.manager.reset();
			return true;
		}
		return false;
	}

	public Level.Frame getFrame() {
		if (this.currentFrame > getPattern().frames.size() - 1 || this.currentFrame < 0) {
			final ArrayList<Block.BlockData> blockData = new ArrayList<Block.BlockData>();
			final Level.Frame newFrame = new Level.Frame(blockData);
			this.currentFrame = Math.max(0, this.currentFrame);
			getPattern().frames.add(this.currentFrame, newFrame);
		}
		return getPattern().frames.get(this.currentFrame);
	}

	public boolean setCurrentPattern(final int pattern) {
		final int oldPattern = this.currentPattern;
		this.currentPattern = pattern;
		this.getPattern();
		if (pattern != oldPattern) {
			Rflex.game.manager.reset();
			return true;
		}
		return false;
	}

	public Level.Pattern getPattern() {
		if (this.currentPattern > level.patterns.size() - 1 || this.currentPattern < 0) {
			final Level.Pattern newPattern = new Level.Pattern();
			this.currentPattern = Math.max(0, this.currentPattern);
			level.patterns.add(this.currentPattern, newPattern);
		}
		return level.patterns.get(this.currentPattern);
	}

	public void addBlock(final Block.BlockData newBlockData) {
		final Iterator<Block.BlockData> i = this.getFrame().blockData.iterator();
		while (i.hasNext()) {
			final Block.BlockData blockData = i.next();
			if (blockData != newBlockData && blockData.pos == newBlockData.pos && blockData.side == newBlockData.side) {
				i.remove();
				break;
			}
		}
		this.getFrame().blockData.add(newBlockData);
	}

	public boolean removeBlock() {
		final boolean l = MultiInput.leftClicked();
		final boolean r = MultiInput.rightClicked();
		final boolean u = MultiInput.downClicked();
		final boolean d = MultiInput.upClicked();
		if (!l && !r && !u && !d)
			return false;

		final Iterator<Block.BlockData> i = this.getFrame().blockData.iterator();
		while (i.hasNext()) {
			final Block.BlockData blockData = i.next();
			if (r && blockData.side == 3 && blockData.pos == -this.cursorY)
				i.remove();
			else if (l && blockData.side == 1 && blockData.pos == this.cursorY)
				i.remove();
			else if (d && blockData.side == 2 && blockData.pos == this.cursorX)
				i.remove();
			else {
				if (!u || blockData.side != 0 || blockData.pos != -this.cursorX)
					continue;
				i.remove();
			}
		}
		return true;
	}

	public static abstract class Property {
		public String name;
		public float step;

		public Property(String name, float step) {
			this.name = name;
			this.step = step;
		}

		public abstract void step(int mult);

		public abstract float get();
	}
}
