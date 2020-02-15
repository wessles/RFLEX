package com.wessles.rflex.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Base64Coder;
import com.wessles.crypto.Validation;
import com.wessles.mercury.math.RandomUtil;
import com.wessles.mercury.utilities.logging.Logger;
import com.wessles.rflex.Rflex;
import com.wessles.rflex.game.Block;
import com.wessles.rflex.game.Song;
import com.wessles.rflex.menu.LevelSelect;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Level {
	public String name;

	public long deaths, totalTime, lastTime, bestTime, bestDrunkTime;

	public long getBestTime() {
		return LevelSelect.sober ? bestTime : bestDrunkTime;
	}

	public long setBestTime(long newBestTime) {
		if (LevelSelect.sober)
			bestTime = newBestTime;
		else
			bestDrunkTime = newBestTime;
		return getBestTime();
	}

	public long getBestNormalTime() {
		return bestTime;
	}

	public long getBestDrunkTime() {
		return bestDrunkTime;
	}

	public Song song;
	public Color backgroundColor;
	public float rotKick;
	public float swayAmount;
	public float swayTime;
	public float blockSpeed;
	public float finalBlockSpeed;
	public float completionistSpeed;
	public float rotatingChance;
	public boolean flip;
	public boolean flash;

	public HashMap<String, String> values;

	public ArrayList<Pattern> patterns;

	public Level(String name, HashMap<String, String> values, long deaths, long totalTime, long bestTime, long bestDrunkTime, long lastTime, ArrayList<Pattern> patterns) {
		this.name = name;

		this.deaths = deaths;
		this.totalTime = totalTime;
		this.bestTime = bestTime;
		this.bestDrunkTime = bestDrunkTime;
		this.lastTime = lastTime;

		if (!values.containsKey("song"))
			values.put("song", "Handicapped");
		if (!values.containsKey("backgroundColor"))
			values.put("backgroundColor", "0 0 0");

		if (!values.containsKey("rotKick"))
			values.put("rotKick", "0");
		if (!values.containsKey("swayAmount"))
			values.put("swayAmount", "0");
		if (!values.containsKey("swayTime"))
			values.put("swayTime", "0");

		if (!values.containsKey("blockSpeed"))
			values.put("blockSpeed", "0");
		if (!values.containsKey("finalBlockSpeed"))
			values.put("finalBlockSpeed", "0");
		if (!values.containsKey("completionistSpeed"))
			values.put("completionistSpeed", "0");
		if (!values.containsKey("rotatingChance"))
			values.put("rotatingChance", "0");

		if (!values.containsKey("flip"))
			values.put("flip", "false");
		if (!values.containsKey("flash"))
			values.put("flash", "false");

		this.song = new Song(values.get("song"));
		String[] bg = values.get("backgroundColor").split(" ");
		this.backgroundColor = new Color(Integer.valueOf(bg[0]) / 255f, Integer.valueOf(bg[1]) / 255f, Integer.valueOf(bg[2]) / 255f, 1f);

		this.rotKick = Float.valueOf(values.get("rotKick"));
		this.swayAmount = Float.valueOf(values.get("swayAmount"));
		this.swayTime = Float.valueOf(values.get("swayTime"));

		this.blockSpeed = Float.valueOf(values.get("blockSpeed"));
		this.finalBlockSpeed = Float.valueOf(values.get("finalBlockSpeed"));
		this.completionistSpeed = Float.valueOf(values.get("completionistSpeed"));
		this.rotatingChance = Float.valueOf(values.get("rotatingChance"));

		this.flip = Boolean.valueOf(values.get("flip"));
		this.flash = Boolean.valueOf(values.get("flash"));

		this.values = values;

		this.patterns = patterns;
	}

	int currentPattern = -1;
	boolean recurred = false;

	public Pattern next() {
		if (this.patterns.size() <= 0)
			return null;

		if (Rflex.self.getCurrentState() != Rflex.editor) {
			int lastPattern = currentPattern;
			currentPattern = (int) RandomUtil.random(0, this.patterns.size() - 1);
			if (currentPattern == lastPattern)
				if (!recurred)
					recurred = true;
				else
					while (currentPattern == lastPattern)
						currentPattern = (int) RandomUtil.random(0, this.patterns.size() - 1);
			else
				recurred = false;
		}

		return this.patterns.get(Math.min(patterns.size() - 1, currentPattern));
	}

	public void save() {
		saveLevel(this);
	}

	public static int levelID = -1;

	public static Level loadLevel(String levelName, long deaths, long totalTime, long bestTime, long bestDrunkTime, long lastTime) {
		levelID++;

		Logger.log(levelName);

		final FileHandle fileHandle = Gdx.files.internal("assets/levels/" + levelName + ".rflex");
		if (!fileHandle.exists())
			try {
				fileHandle.file().createNewFile();
				Gdx.app.exit();
			} catch (IOException e) {
				e.printStackTrace();
			}

		String levelFileString = fileHandle.readString();
		// TODO fix
		// verify
		if (Validation.validate(Gdx.files.internal("assets/audio/music/." + levelName + ".rflex.signature").readString(), levelFileString)) {
			Logger.log("level " + levelName + " is valid.");
		} else
			try {
				LevelSelect.reportToNaughtyList("levelmod");
				throw new SignatureException(levelName + " is invalid; was it modified?");
			} catch (SignatureException e) {
				e.printStackTrace();
				Gdx.app.exit();
			}
		// TODO fix
		levelFileString = Base64Coder.decodeString(new String(levelFileString.getBytes(StandardCharsets.UTF_8)));
		String[] lines = levelFileString.split("\n");
		int currentLine = 0;

		HashMap<String, String> values = new HashMap<String, String>();

		String line;
		while (!(line = lines[currentLine++]).equals("")) {
			String[] vals = line.split("=");
			values.put(vals[0], vals[1]);
		}

		boolean niceTry = false;

		if (levelID == 0 && !values.get("song").equalsIgnoreCase("reignite"))
			niceTry = true;
		else if (levelID == 1 && !values.get("song").equalsIgnoreCase("handicapped"))
			niceTry = true;
		else if (levelID == 2 && !values.get("song").equalsIgnoreCase("laserdragon"))
			niceTry = true;
		else if (levelID == 3 && !values.get("song").equalsIgnoreCase("steppingintomadness"))
			niceTry = true;
		else if (levelID == 4 && !values.get("song").equalsIgnoreCase("machinarium"))
			niceTry = true;

//		if (niceTry)
//			try {
//				throw new Exception("nice try, " + levelID + "!");
//			} catch (Exception e) {
//				e.printStackTrace();
//				Gdx.app.exit();
//			}

		ArrayList<Pattern> patterns = new ArrayList<Pattern>();
		ArrayList<Frame> frames = new ArrayList<Frame>();

		while (currentLine < lines.length) {
			final ArrayList<Block.BlockData> frameBlocks = new ArrayList<Block.BlockData>();

			final String s = lines[currentLine++];

			if (s.equals(""))
				frames.add(new Frame(frameBlocks));
			else if (s.startsWith("~"))
				frames.add(new Frame(frameBlocks, s.substring(1)));
			else if (s.equals("=")) {
				Frame[] frameArray = new Frame[frames.size()];
				frameArray = frames.toArray(frameArray);
				patterns.add(new Pattern(frameArray));
				frames.clear();
			} else {
				final char[] sa = s.toCharArray();
				for (int i0 = 0; i0 < sa.length; ++i0) {
					final char c = sa[i0];
					if (sa[i0] == 'b' || sa[i0] == 'l' || sa[i0] == 'n') {
						i0 += 2;
						String argsStr = "";
						char otherC;
						while ((otherC = sa[i0++]) != ')') {
							argsStr += otherC;
						}
						final String[] args = argsStr.split(" ");
						if (c == 'b') {
							final int side = Integer.valueOf(args[0]);
							final int pos = Integer.valueOf(args[1]);
							frameBlocks.add(new Block.BlockData(pos, side));
						}
					}
				}
				frames.add(new Frame(frameBlocks));
			}
		}

		if (patterns.size() == 0) {
			Frame[] frameArray = new Frame[frames.size()];
			frameArray = frames.toArray(frameArray);
			patterns.add(new Pattern(frameArray));
			frames.clear();
		}

		return new Level(levelName, values, deaths, totalTime, bestTime, bestDrunkTime, lastTime, patterns);
	}

	public static void saveLevel(final Level level) {
		level.values.put("song", level.song.name);

		level.values.put("backgroundColor", (int) (level.backgroundColor.r * 255) + " " + (int) (level.backgroundColor.g * 255) + " " + (int) (level.backgroundColor.b * 255) + " ");

		level.values.put("rotKick", String.valueOf(level.rotKick));
		level.values.put("swayAmount", String.valueOf(level.swayAmount));
		level.values.put("swayTime", String.valueOf(level.swayTime));

		level.values.put("blockSpeed", String.valueOf(level.blockSpeed));
		level.values.put("finalBlockSpeed", String.valueOf(level.finalBlockSpeed));
		level.values.put("completionistSpeed", String.valueOf(level.completionistSpeed));
		level.values.put("rotatingChance", String.valueOf(level.rotatingChance));

		level.values.put("flip", String.valueOf(level.flip));
		level.values.put("flash", String.valueOf(level.flash));

		String write = "";

		for (String value : level.values.keySet())
			write += value + "=" + level.values.get(value) + "\n";

		write += "\n";

		for (Pattern pattern : level.patterns) {
			for (final Frame frame : pattern.frames) {
				if (frame.blockData.size() == 0)
					write += "-" + "\n";
				else {
					for (final Block.BlockData block : frame.blockData) {
						write += "b(";
						write += block.side + " ";
						write += block.pos + ") ";
					}
					write += "\n";
				}
			}
			write += "=" + "\n";
		}

		final FileHandle fileHandle = Gdx.files.internal("assets/levels/" + level.name + ".rflex");
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileHandle.file());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		// TODO fix
		writer.write(Base64Coder.encodeString(new String(write.getBytes(StandardCharsets.UTF_8))));
//		writer.write(write);
		writer.close();
	}

	public static class Pattern {
		public ArrayList<Frame> frames;

		public Pattern(Frame... frames) {
			this.frames = new ArrayList<>();
			Collections.addAll(this.frames, frames);
		}

		private int currentFrame = -1;

		public Frame next() {
			currentFrame++;
			if (currentFrame < frames.size()) return frames.get(currentFrame);
			else {
				reset();
				return null;
			}
		}

		public void reset() {
			currentFrame = -1;
		}

		public Pattern mix() {
			for (Frame frame : frames)
				frame.mix();

			return this;
		}
	}

	public static class Frame {
		public ArrayList<Block.BlockData> blockData;
		public String tutorial;

		public Frame(final ArrayList<Block.BlockData> blockData) {
			this(blockData, "");
		}

		public Frame(final ArrayList<Block.BlockData> blockData, final String tutorial) {
			this.blockData = blockData;
			this.tutorial = tutorial;
		}

		public Frame mix() {
			for (final Block.BlockData blockData2 : blockData) {
				blockData2.side += 1;
				if (blockData2.side > 3)
					blockData2.side %= 4;
			}

			return this;
		}
	}
}
