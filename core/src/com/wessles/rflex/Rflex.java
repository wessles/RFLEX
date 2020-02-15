package com.wessles.rflex;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.codedisaster.steamworks.*;
import com.wessles.mercury.framework.GameState;
import com.wessles.mercury.utilities.logging.Logger;
import com.wessles.rflex.game.Background;
import com.wessles.rflex.game.Game;
import com.wessles.rflex.game.Player;
import com.wessles.rflex.level.editor.Editor;
import com.wessles.rflex.menu.*;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Rflex extends ApplicationAdapter {
    public static String VERSION;

    public static Preferences prefs;

    public static int DEFAULT_WIDTH = 1280, DEFAULT_HEIGHT = 720;
    public static int PRESUMED_WIDTH = 2560, PRESUMED_HEIGHT = 1440;

    public static float brightness = 1f;

    public static float SFX_VOLUME = 1f, MUSIC_VOLUME = 1f;

    public static boolean uploadScores = true;

    public static boolean cinematic = false;

    public static Rflex self;

    public static InputMultiplexer multiplexer;

    public static Viewport viewport;
    public static OrthographicCamera cam, unscaledCam;
    public static SpriteBatch batch;
    public static ShapeRenderer shapeRenderer;
    public static PolygonSpriteBatch polyBatch;

    public static float sizeY;
    public static float gridSize;

    public static ShaderProgram shader;
    public static ShaderProgram glitchShader;
    public static FrameBuffer frameBuffer;

    public static TitleScreen titleScreen;
    public static MainMenu mainMenu;
    public static OptionsMenu optionsMenu;
    public static Credits credits;
    public static LevelSelect levelSelect;
    public static Game game;
    public static Editor editor;
    public static BindControls bindControls;

    public static FreeTypeFontGenerator squaresGenerator;
    public static FreeTypeFontGenerator.FreeTypeFontParameter squaresParameter;

    public static BitmapFont font_squares_xxxx;
    public static BitmapFont font_squares_xxx;
    public static BitmapFont font_squares_xx;
    public static BitmapFont font_squares_x;
    public static BitmapFont font_squares_o;
    public static Background bg;

    // STEAM STUFF
    public static SteamUser steamUser;
    public static SteamFriends steamFriends;
    public static SteamUserStats steamUserStats;
    public static SteamRemoteStorage steamRemoteStorage;
    public static SteamUtils steamUtils;

    public static int currentLeaderBoardToFind = 0;

    public static class Leaderboard {
        public SteamLeaderboardHandle leaderboardHandle;

        public Leaderboard(SteamLeaderboardHandle leaderboardHandle) {
            this.leaderboardHandle = leaderboardHandle;
        }

        public void update(long score) {
            if (!Rflex.uploadScores)
                return;

            updateScoreRequestQueue.add(new UpdateScoreRequest(leaderboardHandle, score));
        }

        public static class UpdateScoreRequest {
            public SteamLeaderboardHandle leaderboardHandle;
            public long score;

            public UpdateScoreRequest(SteamLeaderboardHandle leaderboardHandle, long score) {
                this.leaderboardHandle = leaderboardHandle;
                this.score = score;
            }

            public void update() {
                steamUserStats.uploadLeaderboardScore(leaderboardHandle, SteamUserStats.LeaderboardUploadScoreMethod.ForceUpdate, (int) score);
                Logger.log("requested " + steamUserStats.getLeaderboardName(leaderboardHandle));
            }
        }

        public static boolean updateQueue;
        public static Queue<UpdateScoreRequest> updateScoreRequestQueue = new LinkedList<>();

        public enum ReloadTypes {
            top500, aroundYou, friends
        }

        public ReloadTypes reloadType = ReloadTypes.top500;

        public void reload(ReloadTypes reload) {
            entries.clear();

            reloadType = reload;

            if (reload == ReloadTypes.top500)
                steamUserStats.downloadLeaderboardEntries(leaderboardHandle, SteamUserStats.LeaderboardDataRequest.Global, 0, 500);
            else if (reload == ReloadTypes.aroundYou)
                steamUserStats.downloadLeaderboardEntries(leaderboardHandle, SteamUserStats.LeaderboardDataRequest.GlobalAroundUser, 0, 500);
            else if (reload == ReloadTypes.friends)
                steamUserStats.downloadLeaderboardEntries(leaderboardHandle, SteamUserStats.LeaderboardDataRequest.Friends, 0, 500);
        }

        public ArrayList<SteamLeaderboardEntry> entries = new ArrayList<>();

        int myPlace = 0;

        public String[] getBoardLeftColumn() {
            if (entries.size() == 0)
                return new String[]{"-"};

            String[] lines = new String[entries.size()];

            int index = 0;
            for (SteamLeaderboardEntry entry : entries) {
                if (entry.getSteamIDUser().getAccountID() == steamUser.getSteamID().getAccountID())
                    myPlace = index;

                lines[index] = "#" + entry.getGlobalRank();

                index++;
            }

            return lines;
        }

        public String[] getBoardMidColumn() {
            if (entries.size() == 0)
                return new String[]{"Loading..."};

            String[] lines = new String[entries.size()];

            int index = 0;
            for (SteamLeaderboardEntry entry : entries) {
                String name;
                if (steamFriends.requestUserInformation(entry.getSteamIDUser(), true))
                    name = "Requesting...";
                else
                    name = steamFriends.getFriendPersonaName(entry.getSteamIDUser());

                if (entry.getSteamIDUser().getAccountID() == steamUser.getSteamID().getAccountID()) {
                    name = "[#BBBBFFFF]" + name + "[] ";
                    myPlace = index;
                }

                lines[index] = name;

                index++;
            }

            return lines;
        }

        public String[] getBoardRightColumn() {
            if (entries.size() == 0)
                return new String[]{"-"};

            String[] lines = new String[entries.size()];

            int index = 0;
            for (SteamLeaderboardEntry entry : entries) {
                if (entry.getSteamIDUser().getAccountID() == steamUser.getSteamID().getAccountID())
                    myPlace = index;

                lines[index] = new DecimalFormat("#.##").format(entry.getScore() / 1000f) + " sec.";

                index++;
            }

            return lines;
        }

        public int getMyPlaceOnList() {
            return myPlace;
        }
    }

    public static HashMap<SteamLeaderboardHandle, Leaderboard> leaderboardMap = new HashMap<>();
    public static HashMap<Integer, SteamLeaderboardHandle> leaderboardHandleMap = new HashMap<>();

    @Override
    public void create() {
        VERSION = Gdx.files.internal("version").readString().trim();

        // no cursor
        FileHandle fh = Gdx.files.internal("assets/cursor.png");
        Pixmap pm = new Pixmap(fh);
        pm.setColor(0,0,0,0);
        Gdx.input.setCursorImage(pm, 0, 0);
        pm.dispose();

        // STEAM API TODO REMOVE FOR DRM FREE RELEASE
        if (!SteamAPI.init()) {
                Logger.warn("Steam API failed to initialize.\nIf you don't have steam turned on while playing the steam version of RFLEX,\nyou're gonna have a bad time.");
        } else {
            steamUser = new SteamUser();

            steamFriends = new SteamFriends(new SteamFriendsCallback() {
                @Override
                public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange change) {
                }
            });

            steamUserStats = new SteamUserStats(new SteamUserStatsCallback() {
                @Override
                public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result) {
                    System.out.println("User stats received: gameId=" + gameId + ", userId=" + steamIDUser.getAccountID() + ", result=" + result.toString());

//					int numAchievements = steamUserStats.getNumAchievements();
//					System.out.println("Num of achievements: " + numAchievements);

//					Rflex.steamUserStats.resetAllStats(true);

//					for (int i = 0; i < numAchievements; i++) {
//						String name = steamUserStats.getAchievementName(i);
//						boolean achieved = steamUserStats.isAchieved(name, false);
//						System.out.println("# " + i + " : name=" + name + ", achieved=" + (achieved ? "yes" : "no"));
//					}
                }

                @Override
                public void onUserStatsStored(long gameId, SteamResult result) {

                }

                @Override
                public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found) {
                    if (!found) {
                        Logger.log("failed to find leaderboard " + "best_times_level_" + currentLeaderBoardToFind);
                        return;
                    }

                    int bestTime;
                    if (currentLeaderBoardToFind >= levelSelect.levels.size())
                        bestTime = (int) levelSelect.levels.get(currentLeaderBoardToFind % levelSelect.levels.size()).bestDrunkTime;
                    else
                        bestTime = (int) levelSelect.levels.get(currentLeaderBoardToFind % levelSelect.levels.size()).bestTime;

                    Leaderboard wrappedLeaderboard = new Leaderboard(leaderboard);
                    wrappedLeaderboard.update(bestTime);

                    leaderboardHandleMap.put(currentLeaderBoardToFind, leaderboard);
                    leaderboardMap.put(leaderboard, wrappedLeaderboard);

                    currentLeaderBoardToFind++;

                    if (currentLeaderBoardToFind < levelSelect.levels.size() * 2)
                        steamUserStats.findLeaderboard("best_times_level_" + currentLeaderBoardToFind);
                    else
                        Leaderboard.updateQueue = true;
                }

                @Override
                public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard, SteamLeaderboardEntriesHandle entries, int numEntries) {
                    System.out.println("Leaderboard scores downloaded: handle=" + leaderboard.toString() + ", entries=" + entries.toString() + ", count=" + numEntries + ", name=" + steamUserStats.getLeaderboardName(leaderboard));

                    for (int i = 0; i < numEntries; i++) {
                        SteamLeaderboardEntry entry = new SteamLeaderboardEntry();
                        if (steamUserStats.getDownloadedLeaderboardEntry(entries, i, entry)) {

//							System.out.println("\tLeaderboard entry #" + i +
//									": steamIDUser=" + entry.getSteamIDUser().getAccountID() +
//									", globalRank=" + entry.getGlobalRank() +
//									", score=" + entry.getScore());

                            // fetch name and query
                            steamFriends.requestUserInformation(entry.getSteamIDUser(), false);

                            leaderboardMap.get(leaderboard).entries.add(entry);

                        }
                    }
                }

                @Override
                public void onLeaderboardScoreUploaded(boolean success, SteamLeaderboardHandle leaderboard, int score, boolean scoreChanged, int globalRankNew, int globalRankPrevious) {
                    System.out.println("Leaderboard scores uploaded: success=" + success + ", board=" + steamUserStats.getLeaderboardName(leaderboard) + ", score=" + score);
                    Leaderboard.updateQueue = true;
                }
            });

            steamRemoteStorage = new SteamRemoteStorage(new SteamRemoteStorageCallback() {
                @Override
                public void onFileShareResult(SteamUGCHandle fileHandle, String fileName, SteamResult result) {

                }

                @Override
                public void onDownloadUGCResult(SteamUGCHandle fileHandle, SteamResult result) {

                }

                @Override
                public void onPublishFileResult(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result) {

                }

                @Override
                public void onUpdatePublishedFileResult(SteamPublishedFileID publishedFileID, boolean needsToAcceptWLA, SteamResult result) {

                }
            });

            steamUtils = new SteamUtils();

            System.out.println("Local user account ID: " + steamUser.getSteamID().getAccountID());
            System.out.println("App ID: " + steamUtils.getAppID());

            Logger.log("requesting stats: " + steamUserStats.requestCurrentStats());

            Logger.log("beginning series of leaderboard requests: " + steamUserStats.findLeaderboard("best_times_level_" + currentLeaderBoardToFind), "\n");
        }

        try {
            String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

            FileHandle fosFileOut = Gdx.files.internal("log/out/rflex_sysout_" + date + ".log");
            if (!fosFileOut.exists()) {
                fosFileOut.parent().file().mkdirs();
                fosFileOut.file().createNewFile();
            }
            FileOutputStream fosOut = new FileOutputStream(fosFileOut.file());
            TeeOutputStream myFosOut = new TeeOutputStream(System.out, fosOut);
            PrintStream psOut = new PrintStream(myFosOut);
            System.setOut(psOut);

            FileHandle fosFileErr = Gdx.files.internal("log/error/rflex_syserr_" + date + ".log");
            if (!fosFileErr.exists()) {
                fosFileErr.parent().file().mkdirs();
                fosFileErr.file().createNewFile();
            }
            FileOutputStream fosErr = new FileOutputStream(fosFileErr.file());
            TeeOutputStream myFosErr = new TeeOutputStream(System.out, fosErr);
            PrintStream psErr = new PrintStream(myFosErr);
            System.setErr(psErr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        self = this;

        prefs = Gdx.app.getPreferences("rflex.prefs");
        loadPreferences();

        Gdx.input.setCatchBackKey(true);
        multiplexer = new InputMultiplexer();
        Controllers.addListener(new ControllerListener() {
            @Override
            public void connected(Controller controller) {
            }

            @Override
            public void disconnected(Controller controller) {

            }

            boolean[] buttonsUsed = new boolean[256];

            @Override
            public boolean buttonDown(Controller controller, int buttonCode) {
                if (!buttonsUsed[buttonCode])
                    BindControls.setLastPressed(buttonCode);

                buttonsUsed[buttonCode] = true;

                if (buttonCode == BindControls.BUTTON_X)
                    MultiInput.cCtrled = true;
                if (buttonCode == BindControls.BUTTON_A)
                    MultiInput.cAccept = true;
                if (buttonCode == BindControls.BUTTON_B)
                    MultiInput.cBack = true;
                if (buttonCode == BindControls.BUTTON_Y)
                    MultiInput.cAlt = true;

                return false;
            }

            @Override
            public boolean buttonUp(Controller controller, int buttonCode) {
                buttonsUsed[buttonCode] = false;
                return false;
            }

            boolean[] lastValue = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};

            public float sign(float number) {
                return ((number != 0) ? (number < 0 ? -1 : 1) : 0);
            }

            @Override
            public boolean axisMoved(Controller controller, int axisCode, float value) {
                if (Math.abs(value) < 0.1f) {
                    value = 0f;
                    lastValue[axisCode] = false;
                }

                if (Math.abs(value) == 1f && axisCode < 2)
                    BindControls.setLastAxis(axisCode, Math.round((value) / Math.abs(value)));

                MultiInput.cUp = MultiInput.cDown = MultiInput.cLeft = MultiInput.cRight = false;

                if (Math.abs(controller.getAxis(BindControls.AXIS_LEFT)) < 0.2f)
                    MultiInput.cLefted = MultiInput.cRighted = false;
                if (Math.abs(controller.getAxis(BindControls.AXIS_UP)) < 0.2f)
                    MultiInput.cUpped = MultiInput.cDowned = false;

                if (Math.abs(controller.getAxis(Math.abs(BindControls.AXIS_RIGHT))) > 0.6f)
                    if (BindControls.RIGHT_POLARITY == sign(controller.getAxis(Math.abs(BindControls.AXIS_RIGHT))))
                        MultiInput.cRight = true;
                    else
                        MultiInput.cLeft = true;

                if (Math.abs(controller.getAxis(Math.abs(BindControls.AXIS_UP))) > 0.6f)
                    if (BindControls.UP_POLARITY == sign(controller.getAxis(Math.abs(BindControls.AXIS_UP))))
                        MultiInput.cUp = true;
                    else
                        MultiInput.cDown = true;

                return false;
            }

            @Override
            public boolean povMoved(Controller controller, int povCode, PovDirection value) {
                return false;
            }

            @Override
            public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
                return false;
            }

            @Override
            public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
                return false;
            }

            @Override
            public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
        MultiInput.create();

        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.setToOrtho(false);
        cam.update();
        unscaledCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        unscaledCam.setToOrtho(false);
        unscaledCam.update();
        viewport = new ScreenViewport(cam);
        viewport.apply();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        polyBatch = new PolygonSpriteBatch();

        resizeRatios(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        squaresGenerator = new FreeTypeFontGenerator(Gdx.files.internal("assets/gfx/fonts/squares2/Squares Bold Free.otf"));
        squaresParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        reloadFonts();

        shader = new ShaderProgram(Gdx.files.internal("assets/gfx/shaders/shader.vert"), Gdx.files.internal("assets/gfx/shaders/shader.frag"));
        Logger.log(shader.getLog() + "\n" + (shader.isCompiled() ? "compiled" : "not compiled"));
        glitchShader = new ShaderProgram(Gdx.files.internal("assets/gfx/shaders/shader.vert"), Gdx.files.internal("assets/gfx/shaders/glitch.frag"));
        Logger.log(glitchShader.getLog() + "\n" + (glitchShader.isCompiled() ? "compiled" : "not compiled"));
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        bg = new Background();

        titleScreen = new TitleScreen();
        mainMenu = new MainMenu();
        optionsMenu = new OptionsMenu();
        credits = new Credits();
        levelSelect = new LevelSelect();
        game = new Game();
        editor = new Editor();
        bindControls = new BindControls();
        switchGameState(titleScreen);
    }

    public void loadPreferences() {
        if (Rflex.optionsMenu == null || Rflex.optionsMenu.fullscreenChanged)
            if (prefs.getBoolean("fullscreen"))
                Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode());
            else
                Gdx.graphics.setDisplayMode(DEFAULT_WIDTH, DEFAULT_HEIGHT, false);

        if (prefs.contains("sfx_volume"))
            Rflex.SFX_VOLUME = prefs.getFloat("sfx_volume");

        if (prefs.contains("brightness"))
            Rflex.brightness = prefs.getFloat("brightness");

        if (prefs.contains("music_volume"))
            Rflex.MUSIC_VOLUME = prefs.getFloat("music_volume");

        if (prefs.contains("is_epileptic"))
            Background.isEpileptic = prefs.getBoolean("is_epileptic");

        if (prefs.contains("upload_scores"))
            Rflex.uploadScores = prefs.getBoolean("upload_scores");

        if (prefs.contains("screenCurve"))
            Background.screenCurve = prefs.getFloat("screenCurve");

        if (prefs.contains("variable_gamepad_control"))
            Player.VARIABLE_GAMEPAD_CONTROL = prefs.getBoolean("variable_gamepad_control");

        if (prefs.contains("cinematic"))
            Rflex.cinematic = prefs.getBoolean("cinematic");

        BindControls.loadPrefs();
    }

    public static double time = 0;

    public void update(double delta) {
        time += delta;
        updateState(delta);

        // update leaderboard queue
        if (Leaderboard.updateQueue) {
            if (Leaderboard.updateScoreRequestQueue.size() > 0)
                Leaderboard.updateScoreRequestQueue.poll().update();
            Leaderboard.updateQueue = false;
        }
    }

    public static double getDelta() {
        double DELTA_AVERAGE = 0.016718244;
        return Gdx.graphics.getDeltaTime() / DELTA_AVERAGE;
    }

    @Override
    public void render() {
        Vector2 center = new Vector2(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f);

        // STEAM API TODO REMOVE FOR DRM FREE RELEASE
        if (SteamAPI.isSteamRunning())
            SteamAPI.runCallbacks();

        update(getDelta());

        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        frameBuffer.begin();
        {
            MultiInput.clear();

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            if (!inGame()) {
                cam.position.set(center.x, center.y, 0);
                Rflex.cam.zoom = 1f;
                Rflex.cam.up.set(0, 1, 0);
                Rflex.cam.direction.set(0, 0, -1);
            }

            cam.update();
            batch.setProjectionMatrix(cam.combined);
            shapeRenderer.setProjectionMatrix(cam.combined);
            polyBatch.setProjectionMatrix(cam.combined);

            bg.render();
            renderState();
        }
        frameBuffer.end();

        batch.begin();
        batch.setShader(shader);
        if (!LevelSelect.sober) {
            screenOffsetX += ((float) (15 * (float) Math.cos(Rflex.currentTimeMillis() / 670D) + -game.screenShake * Math.cos(Rflex.currentTimeMillis() / 20D * (game.screenShake / 20f))) - screenOffsetX) / 10f;
            screenOffsetY += ((float) (15 * Math.cos(Rflex.currentTimeMillis() / 600D) + -game.screenShake * Math.sin(Rflex.currentTimeMillis() / 20D * (game.screenShake / 20f))) - screenOffsetY) / 10f;
        } else {
            screenOffsetX += (-screenOffsetX) / 10f;
            screenOffsetY += (-screenOffsetY) / 10f;
            Background.screenCurveX += (Background.screenCurve - Background.screenCurveX) / 3f;
            Background.screenCurveY += (Background.screenCurve - Background.screenCurveY) / 3f;
        }
        shader.setUniformf("CRT_CURVE_AMNTx", Background.screenCurveX);
        shader.setUniformf("CRT_CURVE_AMNTy", Background.screenCurveY);
        brightness = Math.max(brightness, 0.02f);
        batch.setColor(Rflex.brightness, Rflex.brightness, Rflex.brightness, 1f);
        batch.draw(frameBuffer.getColorBufferTexture(), screenOffsetX, screenOffsetY + frameBuffer.getColorBufferTexture().getHeight(), frameBuffer.getColorBufferTexture().getWidth(), -frameBuffer.getColorBufferTexture().getHeight());
        batch.end();
        batch.setShader(null);
//		batch.begin();
//		Rflex.font_squares_xx.draw(Rflex.batch, Gdx.graphics.getFramesPerSecond() + "", 0, 100);
//		batch.end();

        MultiInput.cCtrled = false;
        MultiInput.cAccept = false;
        MultiInput.cBack = false;
        MultiInput.cAlt = false;
    }

    public float screenOffsetX = 0, screenOffsetY = 0;

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        unscaledCam = new OrthographicCamera(width, height);
        unscaledCam.setToOrtho(false);
        unscaledCam.update();

        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        game.resizeFrameBuffers(width, height);
        levelSelect.resizeFrameBuffers(width, height);

        resizeRatios(width, height);
        reloadFonts();

        Rflex.game.player.updateBounds(0f);
    }

    public void resizeRatios(int width, int height) {
        sizeY = (float) height / (float) PRESUMED_HEIGHT;
        sizeY = Math.min(sizeY, (float) width / (float) PRESUMED_WIDTH);
        gridSize = sizeY * 175.0f;
    }

    public void reloadFonts() {
        squaresParameter.size = (int) (340f * sizeY);
        font_squares_xxxx = squaresGenerator.generateFont(squaresParameter);
        font_squares_xxxx.getData().markupEnabled = true;
        squaresParameter.size = (int) (190f * sizeY);
        font_squares_xxx = squaresGenerator.generateFont(squaresParameter);
        font_squares_xxx.getData().markupEnabled = true;
        squaresParameter.size = (int) (100 * sizeY);
        font_squares_xx = squaresGenerator.generateFont(squaresParameter);
        font_squares_xx.getData().markupEnabled = true;
        squaresParameter.size = (int) (60 * sizeY);
        font_squares_x = squaresGenerator.generateFont(squaresParameter);
        font_squares_x.getData().markupEnabled = true;
        squaresParameter.size = (int) (30 * sizeY);
        font_squares_o = squaresGenerator.generateFont(squaresParameter);
        font_squares_o.getData().markupEnabled = true;
    }

    @Override
    public void dispose() {
        getCurrentState().onLeave();
        levelSelect.saveLevelData();

        batch.dispose();
        shapeRenderer.dispose();
        polyBatch.dispose();

        prefs.flush();

        // STEAM API TODO REMOVE FOR DRM FREE RELEASE
        SteamAPI.shutdown();
    }

    // states

    private final GameState defaultGameState = new GameState();
    private GameState currentGameState = defaultGameState;

    public void updateState(double delta) {
        if (currentGameState != null)
            currentGameState.update(delta);
    }

    public void renderState() {
        if (currentGameState != null)
            currentGameState.render();
    }

    public void switchGameState(GameState currentGameState) {
        this.currentGameState.onLeave();
        currentGameState.onEnter();

        this.currentGameState = null;
        this.currentGameState = currentGameState;
    }

    public GameState getCurrentState() {
        return currentGameState;
    }

    public static boolean inGame() {
        return self.getCurrentState() == game || self.getCurrentState() == levelSelect || self.getCurrentState() == editor;
    }

    public static long currentTimeMillis() {
        return System.nanoTime() / 1000000;
    }
}
