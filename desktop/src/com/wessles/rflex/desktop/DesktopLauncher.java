package com.wessles.rflex.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.wessles.rflex.Rflex;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = Rflex.DEFAULT_WIDTH;
		config.height = Rflex.DEFAULT_HEIGHT;
		config.resizable = true;
		config.addIcon("assets/gfx/textures/icon32.png", Files.FileType.Internal);
		config.addIcon("assets/gfx/textures/icon128.png", Files.FileType.Internal);
		config.addIcon("assets/gfx/textures/icon16.png", Files.FileType.Internal);
		config.samples = 2;
		config.vSyncEnabled = true;
		config.title = "RFLEX";

		new LwjglApplication(Rflex.self = new Rflex(), config);
	}
}
