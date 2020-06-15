package net.zehnkampf.empire.domain.map;

import java.io.Serializable;

import net.zehnkampf.empire.domain.Player;
import android.graphics.Color;

public class SeaTile extends Tile implements Serializable {

	public SeaTile(int x, int y) {
		super(x, y);

		color = Color.argb(255, 0, 0, 180);
	}

	public String getType() {
		return "sea";
	}

	@Override
	public Player getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

}
