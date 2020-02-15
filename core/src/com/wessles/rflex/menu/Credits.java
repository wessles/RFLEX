package com.wessles.rflex.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.wessles.mercury.framework.GameState;
import com.wessles.rflex.MultiInput;
import com.wessles.rflex.Rflex;

public class Credits extends GameState {

	private PolyButton left, right;

	private CreditPage[] creditPages;
	private int currentPage = 0;

	public Credits() {
		this.left = new PolyButton("<") {
			@Override
			public void doAction() {
				Menu.moveCursor.play(Rflex.SFX_VOLUME);
				currentPage = Math.max(0, currentPage - 1);
			}
		};

		this.right = new PolyButton(">") {
			@Override
			public void doAction() {
				Menu.moveCursor.play(Rflex.SFX_VOLUME);
				currentPage = Math.min(creditPages.length - 1, currentPage + 1);
			}
		};

		CreditPage creditPage0 = new CreditPage("A game by", "Wesley \"wessles\" LaFerriere\n \n\n[#FFFFFF66]System Void Games[] \n[#FFFFFF66]systemvoidgames.com[] ");
		CreditPage creditPage1 = new CreditPage("With music/SFX by",
				"RollingFacade\n[#FFFFFF25]Song 2, 3[] \n" +
						"Adhenoid\n[#FFFFFF25]Song 4, 5 + SFX[] \n" +
						"ForeverBound\n[#FFFFFF25]Song 1[] \n" +
						"Wessles\n[#FFFFFF25]SFX[] \n"
		);
		CreditPage creditPage2 = new CreditPage("Testing from", "Rayvolution, Agro,\nNegativeZero, Ra4king,\nWaratte, Jervac,\nDrenius, Noctarius,\nSoulfoam");
		CreditPage creditPage3 = new CreditPage("Special thanks to", "Terry Cavanagh\nFluke Dude\nRaymond \"Rayvolution\" Doerr\nLostWarrior");
		creditPages = new CreditPage[]{creditPage0, creditPage1, creditPage2, creditPage3};
	}

	@Override
	public void onEnter() {
		currentPage = 0;
		LevelSelect.sober = true;
	}

	@Override
	public void update(double delta) {
		if (MultiInput.backed()) {
			Menu.exit.play(Rflex.SFX_VOLUME);
			Rflex.self.switchGameState(Rflex.mainMenu);
		}

		left.update(delta);
		right.update(delta);

		super.update(delta);
	}

	@Override
	public void render() {
		Vector2 dimension = new Vector2(Rflex.viewport.getWorldWidth(), Rflex.viewport.getWorldHeight());
		Vector2 center = new Vector2(Rflex.viewport.getWorldWidth() / 2f, Rflex.viewport.getWorldHeight() / 2f);

		Rflex.bg.brightness = 0.1f;
		Rflex.bg.render();
		Rflex.bg.brightness = 1f;

		Rflex.batch.begin();
		Rflex.font_squares_xxx.setColor(Rflex.bg.targetColor);
		Rflex.font_squares_xxx.draw(Rflex.batch, creditPages[currentPage].title, 0, dimension.y - Rflex.font_squares_xxx.getLineHeight() / 2f, dimension.x, Align.center, false);
		Rflex.batch.setColor(Color.WHITE);
		Rflex.font_squares_xx.draw(Rflex.batch, creditPages[currentPage].credits, 0, dimension.y - Rflex.font_squares_xxx.getLineHeight() * 2f, dimension.x, Align.center, false);
		Rflex.batch.end();

		left.render(center.y * 0.5f / 3, center.y * 0.5f / 3, center.y / 3);
		right.render(dimension.x - center.y * 0.5f / 3, center.y * 0.5f / 3, center.y / 3f);
	}

	public static class CreditPage {
		String title, credits;

		public CreditPage(String title, String credits) {
			this.title = title;
			this.credits = credits;
		}
	}
}
