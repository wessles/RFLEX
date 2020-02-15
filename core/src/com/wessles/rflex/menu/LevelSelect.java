package com.wessles.rflex.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Base64Coder;
import com.codedisaster.steamworks.SteamAPI;
import com.wessles.mercury.framework.GameState;
import com.wessles.mercury.math.geometry.Polygon;
import com.wessles.mercury.math.geometry.Rectangle;
import com.wessles.mercury.utilities.logging.Logger;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;
import com.wessles.rflex.game.Background;
import com.wessles.rflex.level.Level;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class LevelSelect extends GameState {
    public ArrayList<Level> levels = new ArrayList<Level>();
    public int currentLevel = 0;

    ShaderProgram stencilShader;
    FrameBuffer maskFrameBuffer, textFrameBuffer;

    private PolyButton left, right;

    public String saveFile = "score.rflexsav";

    Rflex.Leaderboard.ReloadTypes currentReloadtype = Rflex.Leaderboard.ReloadTypes.aroundYou;

    private static String xorMessage(String message, String key) {
        if (message == null || key == null) return null;

        char[] keys = key.toCharArray();
        char[] msg = message.toCharArray();

        int ml = msg.length;
        int kl = keys.length;
        char[] newMsg = new char[ml];

        for (int i = 0; i < ml; i++)
            newMsg[i] = (char) (msg[i] ^ keys[i % kl]);
        return new String(newMsg);
    }

    String[] leftLines, midLines, rightLines;

    public LevelSelect() {
        // Load levels
        Scanner levelListScanner = new Scanner(Gdx.files.internal("assets/levels/levellist").read());
        ArrayList<String> levelNames = new ArrayList<String>();
        while (levelListScanner.hasNextLine())
            levelNames.add(levelListScanner.nextLine());
        levelListScanner.close();

        FileHandle rflexSave = Gdx.files.internal(saveFile);

        // If it doesnt exist, make a default file
        if (!rflexSave.exists()) {
            Logger.log("Save file not found; making new one");
            saveDefaultFile(rflexSave, levelNames);
        }

        // check if hash exists
        FileHandle saveHash = Gdx.files.internal("." + saveFile + ".hash");
        if (!saveHash.exists()) {
            Logger.warn("Hash was destroyed; wiping scores. This incident will be reported.");
            reportToNaughtyList("hashdestroy");
        } else {
            // else, check against hash
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return;
            }

            String thisHash = new String(md.digest(rflexSave.readString().getBytes()));
            if (!thisHash.equals(saveHash.readString())) {
                Logger.warn("Save was modified; wiping scores. This incident will be reported.");
                reportToNaughtyList("savemod");
            }
        }

        String fileString = rflexSave.readString();
        fileString = new String(fileString.getBytes(), StandardCharsets.UTF_8);

        Logger.log("\n\"\n"+fileString+"\n\"\n");

        String rawSave = Base64Coder.decodeString(fileString);
        // decode
        rawSave = Base64Coder.decodeString(new String(rawSave.getBytes(), StandardCharsets.UTF_8));
        // 'decrypt'
        rawSave = xorMessage(rawSave, key1.substring(9));
        // decode
        rawSave = Base64Coder.decodeString(new String(rawSave.getBytes(), StandardCharsets.UTF_8));
        // 'decrypt'
        rawSave = xorMessage(rawSave, key0.substring(9));

        String decodedSave = rawSave;
        String[] decodedSaveLines = decodedSave.split("\n");
        int currentSaveLine = 0;

        while (levels.size() < levelNames.size()) {
			if (currentSaveLine < decodedSaveLines.length) {
            String[] line = decodedSaveLines[currentSaveLine].split(" ");
            levels.add(Level.loadLevel(levelNames.get(levels.size()), Long.valueOf(line[1]), Long.valueOf(line[2]), Long.valueOf(line[3]), Long.valueOf(line[4]), Long.valueOf(line[5])));
            currentSaveLine++;
            Logger.log(levels.size());
			} else
				levels.add(Level.loadLevel(levelNames.get(levels.size()), 0, 0, 0, 0, 0));
        }

        stencilShader = new ShaderProgram(Gdx.files.internal("assets/gfx/shaders/shader.vert"), Gdx.files.internal("assets/gfx/shaders/stencil.frag"));
        Logger.log(stencilShader.getLog() + "\n" + (stencilShader.isCompiled() ? "compiled" : "not compiled"));

        resizeFrameBuffers(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.left = new PolyButton("<") {
            @Override
            public void doAction() {
                Menu.moveCursor.play(Rflex.SFX_VOLUME);
                --currentLevel;
                if (currentLevel < 0)
                    currentLevel = levels.size() - 1;
                else if (currentLevel >= levels.size())
                    currentLevel = 0;
                Rflex.game.manager.reset();

                if (SteamAPI.isSteamRunning() && Rflex.leaderboardMap.size() >= 5) {
                    leftLines = new String[]{""};
                    midLines = new String[]{"Loading..."};
                    rightLines = new String[]{""};

                    Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(sober ? currentLevel : currentLevel + levels.size())).reload(currentReloadtype);
                }

                scrolling = 0;
            }
        };

        this.right = new PolyButton(">") {
            @Override
            public void doAction() {
                Menu.moveCursor.play(Rflex.SFX_VOLUME);
                ++currentLevel;
                if (currentLevel < 0)
                    currentLevel = levels.size() - 1;
                else if (currentLevel >= levels.size())
                    currentLevel = 0;
                Rflex.game.manager.reset();

                if (SteamAPI.isSteamRunning() && Rflex.leaderboardMap.size() >= 5) {
                    leftLines = new String[]{""};
                    midLines = new String[]{"Loading..."};
                    rightLines = new String[]{""};

                    Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(sober ? currentLevel : currentLevel + levels.size())).reload(currentReloadtype);
                }

                scrolling = 0;
            }
        };
    }

    public static void reportToNaughtyList(String reason) {
        String hostName = "rflex-game.com";
        int portNumber = 25565;

        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true)
        ) {
            out.println(reason + ":" + Rflex.steamUser.getSteamID() + "-" + Rflex.steamUser.getSteamID().getAccountID() + "-" + Rflex.steamFriends.getFriendPersonaName(Rflex.steamUser.getSteamID()));
            out.flush();
            out.close();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
        }
    }

    public void saveDefaultFile(FileHandle rflexSave, ArrayList<String> levelNames) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(rflexSave.file());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        // normal text
        String write = "";
        for (String levelName : levelNames)
            write += levelName + " 0 0 0 0 0\n";
        // 'encrypt'
        write = xorMessage(write, key0.substring(9));
        // encode
        write = Base64Coder.encodeString(new String(write.getBytes(), StandardCharsets.UTF_8));
        // 'encrypt'
        write = xorMessage(write, key1.substring(9));
        // encode
        write = Base64Coder.encodeString(new String(write.getBytes(), StandardCharsets.UTF_8));
        // write
        writer.write(Base64Coder.encodeString(new String(write.getBytes(), StandardCharsets.UTF_8)));
        writer.close();

        makeAndWriteHash(rflexSave);
    }

    public void makeAndWriteHash(FileHandle fileHandle) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(Gdx.files.internal("." + saveFile + ".hash").file());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }

        writer.write(new String(md.digest(fileHandle.readString().getBytes())));
        writer.close();
    }

    public void resizeFrameBuffers(int width, int height) {
        maskFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        textFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }

    private static String key0 = "ve89nvnsdjklhxzuiouhnr89bvjsldajiopuaojriaorj498vo98a0pe9faomn9vpfduvojcxlernmfwef9pjdsvozvfja8ofenvxnzjkalfsf";
    private static String key1 = "jioajfvdvnsaffr3qur93rofnj2u859235u3qtnweanj348q9tro34jt3ij45tlh34utkbth3yutyhaosdv90vujsaiewf8afedasdfASDfdjf";

    public void saveLevelData() {
        final FileHandle fileHandle = Gdx.files.internal(saveFile);
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileHandle.file());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // normal text
        String print = "";
        for (Level level : levels) {
            print += level.name + " " + level.deaths + " " + level.totalTime + " " + level.bestTime + " " + level.bestDrunkTime + " " + level.lastTime + "\n";
//			level.save();
        }

        // 'encrypt'
        print = xorMessage(print, key0.substring(9));
        // encode
        print = Base64Coder.encodeString(new String(print.getBytes(), StandardCharsets.UTF_8));
        // 'encrypt'
        print = xorMessage(print, key1.substring(9));
        // encode
        print = Base64Coder.encodeString(new String(print.getBytes(), StandardCharsets.UTF_8));
        print = Base64Coder.encodeString(new String(print.getBytes(), StandardCharsets.UTF_8));
        // write
        writer.write(print);
        writer.close();

        // store hash
        makeAndWriteHash(fileHandle);
    }

    @Override
    public void onEnter() {
    }

    @Override
    public void onLeave() {
        leaderBoardTransition = 0;
        inLeaderBoard = false;
        transitionProgress = 0f;
    }

    public float scrolling = 0;

    @Override
    public void update(double delta) {
        if (transitionProgress == 0) {
            left.update(delta);
            right.update(delta);
        }

        if (MultiInput.accepted()) {
            scrolling = 0;
            if (leaderBoardTransition <= 0.1f) {
                Rflex.game.player.resurrectionNoise.play(Rflex.SFX_VOLUME);
                transitionProgress += 0.00001f;
                Rflex.game.previewMode = false;
                Rflex.game.manager.reset();
                Rflex.game.start = Rflex.currentTimeMillis();
                Rflex.game.time = 0;
            } else {
                Menu.enter.play(Rflex.SFX_VOLUME);

                if (currentReloadtype == Rflex.Leaderboard.ReloadTypes.top500)
                    currentReloadtype = Rflex.Leaderboard.ReloadTypes.aroundYou;
                else if (currentReloadtype == Rflex.Leaderboard.ReloadTypes.aroundYou)
                    currentReloadtype = Rflex.Leaderboard.ReloadTypes.friends;
                else
                    currentReloadtype = Rflex.Leaderboard.ReloadTypes.top500;

                if (SteamAPI.isSteamRunning() && Rflex.leaderboardMap.size() >= 5) {
                    leftLines = new String[]{""};
                    midLines = new String[]{"Loading..."};
                    rightLines = new String[]{""};

                    Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(sober ? currentLevel : currentLevel + levels.size())).reload(currentReloadtype);
                }
            }
        } else if (MultiInput.backed() && transitionProgress <= 0f) {
            Menu.exit.play(Rflex.SFX_VOLUME);
            scrolling = 0;
            if (!inLeaderBoard) {
                sober = true;
                Rflex.self.switchGameState(Rflex.mainMenu);
            } else
                inLeaderBoard = false;
        } else if (MultiInput.ctrled()) {
            sober = !sober;
            if (SteamAPI.isSteamRunning() && Rflex.leaderboardMap.size() >= 5) {
                leftLines = new String[]{""};
                midLines = new String[]{"Loading..."};
                rightLines = new String[]{""};

                Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(sober ? currentLevel : currentLevel + levels.size())).reload(currentReloadtype);
            }
            scrolling = 0;
        } else if (MultiInput.alt()) {
            if (transitionProgress <= 0) {
                if (SteamAPI.isSteamRunning() && Rflex.leaderboardMap.size() >= 5) {
                    leftLines = new String[]{""};
                    midLines = new String[]{"Loading..."};
                    rightLines = new String[]{""};

                    Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(sober ? currentLevel : currentLevel + levels.size())).reload(currentReloadtype);
                }
                scrolling = 0;
                inLeaderBoard = !inLeaderBoard;
                if (!inLeaderBoard)
                    Menu.exit.play(Rflex.SFX_VOLUME);
                else
                    Menu.enter.play(Rflex.SFX_VOLUME);
            }
        }

        currentLevel = Math.max(0, Math.min(levels.size() - 1, currentLevel));

        Rflex.game.update(delta);

        if (transitionProgress > 0) {
            transitionProgress += (1f - transitionProgress) / 3.5f;
            if (transitionProgress + 0.00001f >= 1f)
                Rflex.self.switchGameState(Rflex.game);
        }

        if (MultiInput.up())
            scrolling += 0.03f;
        if (MultiInput.down())
            scrolling -= 0.03f;
    }

    public float transitionProgress = 0f;
    float shapeRotation = 0f;

    public static boolean sober = true;
    public static boolean inLeaderBoard = false;
    public static float leaderBoardTransition = 0;

    @Override
    public void render() {
        Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
        Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

        if (inLeaderBoard)
            leaderBoardTransition += (1 - leaderBoardTransition) / 5f;
        else
            leaderBoardTransition += -leaderBoardTransition / 5f;

        if (leaderBoardTransition - 0.0001f <= 0)
            leaderBoardTransition = 0;

        Rflex.game.render();

        maskFrameBuffer.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Rflex.shapeRenderer.setColor(Color.WHITE);
        if (MultiInput.accepted())
            Menu.depression += (-0.02f - Menu.depression) / 1.5f;
        else
            Menu.depression += -Menu.depression / 1.5f;
        Polygon levelPreviewBounds = (Polygon) new Polygon(center.x, center.y, Math.min(center.x, center.y) * 0.55f, 5).rotate((float) (Rflex.time / 50D));
        levelPreviewBounds.translate((center.x - levelPreviewBounds.getCenter().x) * transitionProgress, (center.y - levelPreviewBounds.getCenter().y - center.y) * transitionProgress);
        levelPreviewBounds.dilate(1f + 8f * transitionProgress);
        levelPreviewBounds.dilate(1f - leaderBoardTransition);
        levelPreviewBounds.render(Rflex.shapeRenderer);
        Rflex.shapeRenderer.end();
        maskFrameBuffer.end();

        Rflex.frameBuffer.begin();
        Rflex.bg.brightness = 0.1f;
        Rflex.bg.render();
        Rflex.bg.brightness = 1f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Rflex.shapeRenderer.setColor(new Color(1f, 1f, 1f, 0.1f));
        shapeRotation += (currentLevel * 6.28f / 7 - shapeRotation) / 3f;
        new Polygon(center.x, -center.y * 2 / 3f, (1f + (float) Math.cos(Rflex.time / 20D) * 0.005f + Menu.depression) * dimension.y * 4 / 5f - dimension.y * leaderBoardTransition, 7).rotate(shapeRotation).render(Rflex.shapeRenderer);
        Rflex.shapeRenderer.end();
        if (transitionProgress > 0f) {
            Rflex.batch.begin();
            Rflex.font_squares_xxx.setColor(Color.WHITE);
            Rflex.font_squares_xxx.draw(Rflex.batch, getName(currentLevel), center.x, dimension.y * 1f / 8f, Align.center, Align.center, false);
            Rflex.batch.end();
        }

        Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Rflex.shapeRenderer.setColor(Color.BLACK);
        levelPreviewBounds.dilate(1.01f).render(Rflex.shapeRenderer);
        Rflex.shapeRenderer.end();

        Rflex.batch.begin();
        Rflex.batch.setShader(stencilShader);
        stencilShader.setUniformi("u_texture", 0);
        stencilShader.setUniformi("u_maskTexture", 1);
        maskFrameBuffer.getColorBufferTexture().bind(1);
        Rflex.game.glitchFrameBuffer.getColorBufferTexture().bind(0);
        Rflex.batch.draw(Rflex.game.glitchFrameBuffer.getColorBufferTexture(), 0, Rflex.game.glitchFrameBuffer.getHeight(), Rflex.game.glitchFrameBuffer.getWidth(), -Rflex.game.glitchFrameBuffer.getHeight());
        Rflex.batch.setShader(null);
        Rflex.batch.end();

        textFrameBuffer.begin();

        Gdx.gl.glClearColor(0, 0, 0, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (inLeaderBoard) {
            if (leftLines != null) {
                if (!leftLines[0].contains("#"))
                    if (SteamAPI.isSteamRunning() && Rflex.leaderboardMap.size() >= 5) {
                        leftLines = Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(sober ? currentLevel : currentLevel + levels.size())).getBoardLeftColumn();
                        midLines = Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(sober ? currentLevel : currentLevel + levels.size())).getBoardMidColumn();
                        rightLines = Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(sober ? currentLevel : currentLevel + levels.size())).getBoardRightColumn();
                    }
            }

            Rflex.batch.begin();

            float y;

            if (currentReloadtype == Rflex.Leaderboard.ReloadTypes.aroundYou || currentReloadtype == Rflex.Leaderboard.ReloadTypes.friends) {
                if (SteamAPI.isSteamRunning() && Rflex.leaderboardMap.size() >= 5)
                    y = center.y + Rflex.font_squares_x.getLineHeight() * Rflex.leaderboardMap.get(Rflex.leaderboardHandleMap.get(sober ? currentLevel : currentLevel + levels.size())).getMyPlaceOnList() + center.y * -scrolling;
                else
                    y = center.y + center.y * -scrolling;
            } else
                y = center.y + center.y * -scrolling;

            float maxWidth = 0;

            int lineY = 0;
            if (midLines != null)
                for (String s : midLines) {
                    maxWidth = Math.max(maxWidth, new GlyphLayout(Rflex.font_squares_x, s).width);
                    if (y + (-lineY * Rflex.font_squares_x.getLineHeight()) > 0 && y + (-lineY * Rflex.font_squares_x.getLineHeight()) < dimension.y)
                        Rflex.font_squares_x.draw(Rflex.batch, s, center.x, y + -lineY * Rflex.font_squares_x.getLineHeight(), Align.center, Align.center, false);
                    lineY += 1;
                }
            else
                Rflex.font_squares_x.draw(Rflex.batch, "Failed.", center.x, y + -lineY * Rflex.font_squares_x.getLineHeight(), Align.center, Align.center, false);
            lineY = 0;
            if (leftLines != null && rightLines != null)
                for (int line = 0; line < midLines.length; line++) {
                    if (y + (-lineY * Rflex.font_squares_x.getLineHeight()) > 0 && y + (-lineY * Rflex.font_squares_x.getLineHeight()) < dimension.y) {
                        Rflex.font_squares_x.draw(Rflex.batch, leftLines[line], Math.max(center.x / 5f, center.x - maxWidth * 1.5f), y + -lineY * Rflex.font_squares_x.getLineHeight(), Align.left, Align.left, false);
                        Rflex.font_squares_x.draw(Rflex.batch, rightLines[line], Math.min(dimension.x - center.x / 5f, center.x + maxWidth * 1.5f), y + -lineY * Rflex.font_squares_x.getLineHeight(), Align.right, Align.right, false);
                    }
                    lineY += 1;
                }

            Rflex.batch.end();
        }

        Rflex.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Rflex.shapeRenderer.setColor(Rflex.bg.targetColor.cpy());
        Rflex.shapeRenderer.getColor().mul(0.1f, 0.1f, 0.1f, 0.85f);
        new Rectangle(0, dimension.y - leaderBoardTransition * Rflex.font_squares_xx.getLineHeight() * 2f, dimension.x, Rflex.font_squares_xx.getLineHeight() * 2f).render(Rflex.shapeRenderer);
        new Rectangle(0, leaderBoardTransition * Rflex.font_squares_xx.getLineHeight() * 2f, dimension.x, -Rflex.font_squares_xx.getLineHeight() * 2f).render(Rflex.shapeRenderer);
        Rflex.shapeRenderer.end();

        Rflex.batch.begin();

        if (transitionProgress <= 0f) {
            Rflex.font_squares_xxx.setColor(Color.WHITE);
            Rflex.font_squares_xxx.draw(Rflex.batch, getName(currentLevel), center.x, Math.max(Rflex.font_squares_xxx.getLineHeight() * 4 / 5f, (1f - leaderBoardTransition) * dimension.y * 1.2f / 6f), Align.center, Align.center, false);

            String grade = Rflex.game.getOverallGrade();
            String gradeString = " Grade:  " + grade;
            Rflex.font_squares_xx.draw(Rflex.batch, gradeString, 0, dimension.y - Rflex.font_squares_xx.getLineHeight() / 3f);
            String best = "Best:  " + grade.substring(0, grade.indexOf(']') + 1) + new DecimalFormat("#.##").format(getCurrentLevel().getBestTime() / 1000f) + "   ";
            Rflex.font_squares_xx.draw(Rflex.batch, best, dimension.x, dimension.y - Rflex.font_squares_xx.getLineHeight() / 3f, Align.bottomRight, Align.bottomRight, false);

            Rflex.font_squares_x.draw(Rflex.batch, "    [#FFFFFF99]" + (sober ? (Controllers.getControllers().size > 0 ? "<X>" : "<ctrl>") + " drunk mode" : (Controllers.getControllers().size > 0 ? "<X>" : "<ctrl>") + " normal mode"), 0, dimension.y - Rflex.font_squares_xx.getLineHeight() * 5 / 4f, Align.center, Align.left, false);
            Rflex.font_squares_x.draw(Rflex.batch, "[#FFFFFF99]" + (!inLeaderBoard ? "leaderboard " + (Controllers.getControllers().size > 0 ? "<Y>" : "<tab>") : "level select " + (Controllers.getControllers().size > 0 ? "<Y>" : "<tab>") + " ") + "    ", dimension.x, dimension.y - Rflex.font_squares_xx.getLineHeight() * 5 / 4f, Align.center, Align.right, false);

            if (inLeaderBoard) {
                String title = "";
                if (currentReloadtype == Rflex.Leaderboard.ReloadTypes.top500)
                    title += "Global Top 500";
                else if (currentReloadtype == Rflex.Leaderboard.ReloadTypes.aroundYou)
                    title += "500 Around You";
                else
                    title += "Friends";

                Rflex.font_squares_x.draw(Rflex.batch, title + "\n [#FFFFFF99]" + (Controllers.getControllers().size > 0 ? "<a>" : "<space>") + " change board", center.x, dimension.y - Rflex.font_squares_x.getLineHeight() + Rflex.font_squares_x.getLineHeight() * (1f - leaderBoardTransition), Align.center, Align.center, false);
            }
        }

        if (!LevelSelect.sober) {
            Background.screenCurveX += (Background.screenCurve * (1f + 0.25f * (float) Math.cos(Rflex.currentTimeMillis() / 400D)) - Background.screenCurveX) / 15f;
            Background.screenCurveY += (Background.screenCurve * (1f + 0.25f * (float) Math.cos(Rflex.currentTimeMillis() / 500D)) - Background.screenCurveY) / 15f;
        }

        Rflex.batch.end();
        textFrameBuffer.end();
        Rflex.frameBuffer.begin();
        Rflex.batch.begin();

        Sprite textFrameBufferSprite = new Sprite(textFrameBuffer.getColorBufferTexture());
        textFrameBufferSprite.setBounds(0, textFrameBufferSprite.getHeight(), textFrameBufferSprite.getWidth(), -textFrameBufferSprite.getHeight());

        if (!LevelSelect.sober) {
            textFrameBufferSprite.setAlpha(0.1f);
            textFrameBufferSprite.setBounds((float) Math.cos(Rflex.currentTimeMillis() / 200D) * 10 * Rflex.sizeY, textFrameBuffer.getHeight() + (float) Math.sin(Rflex.currentTimeMillis() / 500D) * 10 * Rflex.sizeY, textFrameBuffer.getWidth(), -textFrameBuffer.getHeight());
            textFrameBufferSprite.draw(Rflex.batch);

            textFrameBufferSprite.setAlpha(0.1f);
            textFrameBufferSprite.setBounds((float) Math.cos(Rflex.currentTimeMillis() / 400D) * 20 * Rflex.sizeY, textFrameBuffer.getHeight() + (float) Math.sin(Rflex.currentTimeMillis() / 550D) * 20 * Rflex.sizeY, textFrameBuffer.getWidth(), -textFrameBuffer.getHeight());
            textFrameBufferSprite.draw(Rflex.batch);

            textFrameBufferSprite.setBounds((float) Math.cos(Rflex.currentTimeMillis() / 300D) * 5 * Rflex.sizeY, textFrameBuffer.getHeight() + (float) Math.sin(Rflex.currentTimeMillis() / 450D) * 15 * Rflex.sizeY, textFrameBuffer.getWidth(), -textFrameBuffer.getHeight());
        }

        textFrameBufferSprite.setAlpha(1f);
        textFrameBufferSprite.draw(Rflex.batch);

        Rflex.batch.end();

        if (transitionProgress <= 0f) {
            left.render(center.y * 0.5f / 3, center.y * 0.5f / 3, center.y / 3);
            right.render(dimension.x - center.y * 0.5f / 3, center.y * 0.5f / 3, center.y / 3f);
        }
    }

    public String getName(int index) {
        if (index == 0)
            return "amusement";
        else if (index == 1)
            return "challenge";
        else if (index == 2)
            return "addiction";
        else if (index == 3)
            return "lifestyle";
        else if (index == 4)
            return "[#FF1111FF]death-wish";
        else
            return "no name bug!";
    }

    public Level getCurrentLevel() {
        if (levels.size() == 0)
            return null;
        return levels.get(currentLevel);
    }
}
