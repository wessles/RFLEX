package com.wessles.rflex.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.wessles.mercury.math.geometry.Figure;
import com.wessles.mercury.math.geometry.Line;
import com.wessles.mercury.math.geometry.Rectangle;
import com.wessles.mercury.utilities.Negpos;
import com.wessles.mercury.utilities.Wipeable;
import com.wessles.mercury.utilities.misc.Renderable;
import com.wessles.mercury.utilities.misc.Updatable;
import com.wessles.rflex.Rflex;

public class Block implements Updatable, Renderable, Wipeable {
	public final int pos, side;
	public final float vel;
	public final boolean rotate;

	public Rectangle bounds;
	public Vector2 velocity;
	public float progress = 0;
	public float beamAlpha;
	public float renderingsSinceCreation;

	private boolean wiped;

	public Block(BlockData blockdata, boolean rotate) {
		this(blockdata.pos, blockdata.side, rotate);
	}

	public Block(int pos, int side, boolean rotate) {
		this.pos = pos;
		this.side = side;

		if (Rflex.game.time > Game.WIN_TIME * 1000f && Rflex.self.getCurrentState() == Rflex.game)
			this.vel = Rflex.levelSelect.getCurrentLevel().finalBlockSpeed + Rflex.game.getProgress() * (Rflex.levelSelect.getCurrentLevel().completionistSpeed - Rflex.levelSelect.getCurrentLevel().finalBlockSpeed);
		else
			this.vel = Rflex.levelSelect.getCurrentLevel().blockSpeed + Rflex.game.getProgress() * (Rflex.levelSelect.getCurrentLevel().finalBlockSpeed - Rflex.levelSelect.getCurrentLevel().blockSpeed);

		this.rotate = rotate;

		this.beamAlpha = 1.0f;
		this.renderingsSinceCreation = 0;
		this.wiped = false;

		reAlign();
	}

	@Override
	public void update(double delta) {
		Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		float blockSpeed = Rflex.levelSelect.getCurrentLevel().blockSpeed;
		float finalBlockSpeed = Rflex.levelSelect.getCurrentLevel().finalBlockSpeed;
		float velocity = blockSpeed + (finalBlockSpeed - blockSpeed) * Rflex.game.getProgress();

		if ((this.velocity.x < 0.0f && this.bounds.getX2() < -dimension.x) || (this.velocity.y < 0.0f && this.bounds.getY2() < -dimension.y) || (this.velocity.x > 0.0f && this.bounds.getX() > dimension.x * 2) || (this.velocity.y > 0.0f && this.bounds.getY() > dimension.y * 2))
			this.wipe();

		if (Math.abs(this.velocity.x) > Math.abs(this.velocity.y)) {
			velocity *= Negpos.negpos(this.velocity.x);
			this.velocity.x = velocity;
		} else {
			velocity *= Negpos.negpos(this.velocity.y);
			this.velocity.y = velocity;
		}

		float polarity = (float) Math.abs(Math.round(Rflex.game.player.radiusPolarity * 100.0) / 100.0);
		this.progress += 0.003f * vel * delta * polarity * polarity;

		reAlign();
		if (rotate)
			if (Math.abs(this.velocity.x) > Math.abs(this.velocity.y))
				this.bounds.setRotation((6.28f * ((center.x - this.bounds.getCenter().x) / center.x) / 2));
			else
				this.bounds.setRotation((6.28f * ((center.y - this.bounds.getCenter().y) / center.y) / 2));
	}

	@Override
	public void render() {
		Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		Rflex.shapeRenderer.setColor(Color.BLACK.cpy());

		if (Rflex.self.getCurrentState() == Rflex.editor)
			Rflex.shapeRenderer.getColor().a = 1f;
		else {
			if (Math.abs(velocity.x) > Math.abs(velocity.y))
				Rflex.shapeRenderer.getColor().a = (1f - Math.abs((center.x - this.bounds.getCenter().x) / Math.min(center.x, center.y))) * 3f;
			else
				Rflex.shapeRenderer.getColor().a = (1f - Math.abs((center.y - this.bounds.getCenter().y) / Math.min(center.x, center.y))) * 3f;

			bounds.setDilation(Math.min(1f, Rflex.shapeRenderer.getColor().a + 0.2f));

			Rflex.shapeRenderer.getColor().a = Math.max(0, Math.min(1, Rflex.shapeRenderer.getColor().a));
			if (nerfed)
				Rflex.shapeRenderer.getColor().a *= 0.6f;
		}

		this.bounds.render(Rflex.shapeRenderer);

		this.renderingsSinceCreation += 1;
	}

	public void renderLines() {
		Rflex.shapeRenderer.setColor(Color.BLACK.cpy());

		if (Rflex.game.player.isDead() && Rflex.inGame())
			this.beamAlpha = 0;
		Rflex.shapeRenderer.getColor().a = (this.beamAlpha *= 0.96f);
		Figure.lineWidth = Rflex.sizeY * 10.0f * this.beamAlpha;
		if (Rflex.self.getCurrentState() != Rflex.editor)
			new Line(
					this.sourcePos.x,
					this.sourcePos.y,
					this.sourcePos.x + (this.velocity.x * 10 * this.renderingsSinceCreation),
					this.sourcePos.y + (this.velocity.y * 10 * this.renderingsSinceCreation)
			).render(Rflex.shapeRenderer);
	}

	Vector2 sourcePos = new Vector2(0, 0);

	public void reAlign() {
		Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		int pos = this.pos;
		float vel = this.vel;

		// geometry
		float windowMinDimension = Math.min(center.x, center.y);
		float cy;
		float cx;
		float w;
		float h;

		if (side == 0 || side == 2) {
			pos *= ((side == 0) ? -1 : 1);
			vel *= ((side == 0) ? 1 : -1);
			cy = Negpos.negpos(vel) * Rflex.gridSize;
			cx = pos * Rflex.gridSize;
			w = Rflex.gridSize * 0.6f;
			h = 1.1f * Rflex.gridSize;
			cy += (windowMinDimension + h * 0.95f) * -Negpos.negpos(vel);
			this.velocity = new Vector2(0.0f, vel);
		} else {
			pos *= ((side == 3) ? -1 : 1);
			vel *= ((side == 1) ? 1 : -1);
			cx = Negpos.negpos(vel) * Rflex.gridSize;
			cy = pos * Rflex.gridSize;
			h = Rflex.gridSize * 0.6f;
			w = 1.1f * Rflex.gridSize;
			cx += (windowMinDimension + w * 0.95f) * -Negpos.negpos(vel);
			this.velocity = new Vector2(vel, 0.0f);
		}

		cx += center.x;
		cy += center.y;

		this.bounds = new Rectangle(cx - w / 2.0f, cy - h / 2.0f, w, h);
		this.sourcePos = new Vector2(cx, cy);
		if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
			this.bounds.translate(progress * windowMinDimension * Negpos.negpos(velocity.x), 0);
			this.sourcePos.x = (this.sourcePos.x - center.x) * 2f + center.x;
		} else {
			this.bounds.translate(0, progress * windowMinDimension * Negpos.negpos(velocity.y));
			this.sourcePos.y = (this.sourcePos.y - center.y) * 2f + center.y;
		}
	}

	public boolean isOutOfWay() {
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);
		return (this.velocity.x < 0.0f && this.bounds.getX2() < center.x - Rflex.gridSize) || (this.velocity.y < 0.0f && this.bounds.getY2() < center.y - Rflex.gridSize) || (this.velocity.x > 0.0f && this.bounds.getX() > center.x + Rflex.gridSize) || (this.velocity.y > 0.0f && this.bounds.getY() > center.y + Rflex.gridSize);
	}

	@Override
	public void wipe() {
		this.wiped = true;
	}

	@Override
	public boolean wiped() {
		return this.wiped;
	}

	public boolean nerfed = false;

	public void nerf() {
		nerfed = true;
	}

	public static class BlockData {
		public int pos;
		public int side;

		public BlockData(final int pos, final int side) {
			this.pos = pos;
			this.side = side;
		}
	}
}
