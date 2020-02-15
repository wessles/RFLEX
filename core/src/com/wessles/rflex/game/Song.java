package com.wessles.rflex.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.wessles.rflex.Rflex;

import java.util.ArrayList;

public class Song {
	public Music audio;
	public String name;

	public Song(final String audioName) {
		this.audio = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/music/" + audioName + ".ogg"));
		this.name = audioName;
		audio.setLooping(true);
	}

	public void pause() {
		audio.pause();
	}

	public void play() {
		audio.setVolume(Rflex.MUSIC_VOLUME);
		audio.play();
	}
}
