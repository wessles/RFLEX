package com.wessles.rflex.game;

import com.wessles.mercury.math.RandomUtil;
import com.wessles.mercury.utilities.WipingArrayList;
import com.wessles.mercury.utilities.misc.Renderable;
import com.wessles.mercury.utilities.misc.Updatable;
import com.wessles.rflex.Rflex;
import com.wessles.rflex.level.Level;

public class Manager implements Updatable, Renderable {
	public WipingArrayList<Block> blocks;

	public Manager() {
		this.blocks = new WipingArrayList<Block>();
	}

	public Level.Frame lastFrame = null;

	public void parseFrame(final Level.Frame frame) {
		if (frame == null)
			return;

		lastFrame = frame;

		if (frame.blockData != null)
			for (final Block.BlockData b : frame.blockData)
				if (b != null)
					this.blocks.add(new Block(b, RandomUtil.chance(Rflex.levelSelect.getCurrentLevel().rotatingChance)));
	}

	Level.Pattern currentPattern;

	@Override
	public void update(double delta) {
		Level level = Rflex.levelSelect.getCurrentLevel();

		if (currentPattern == null)
			currentPattern = level.next();

		boolean inEditor = Rflex.self.getCurrentState() == Rflex.editor;

		if (!Rflex.game.player.isDead() || inEditor || Rflex.game.previewMode) {
			if (level != null && !inEditor) {
				boolean allBlocksOutOfWay = true;
				for (Block block : blocks)
					if (!block.isOutOfWay()) {
						allBlocksOutOfWay = false;
						break;
					}

				if (allBlocksOutOfWay && Rflex.game.time > 1500f * delta) {
					Level.Frame nextFrame = currentPattern.next();
					if (nextFrame == null)
						nextFrame = (currentPattern = level.next().mix()).next();

					Rflex.game.screenShake(Rflex.levelSelect.getCurrentLevel().rotKick);

					parseFrame(nextFrame);
				}
			}
			for (final Block block2 : this.blocks)
				block2.update(delta);

			this.blocks.sweep();
		}
	}

	@Override
	public void render() {
		for (final Block block : this.blocks)
			block.renderLines();
		for (final Block block : this.blocks)
			block.render();
	}

	public void reset() {
		this.blocks.clear();
		currentPattern.reset();
		currentPattern = Rflex.levelSelect.getCurrentLevel().next();
	}
}
