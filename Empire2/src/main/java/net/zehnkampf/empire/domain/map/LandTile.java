package net.zehnkampf.empire.domain.map;

import java.io.Serializable;

import net.zehnkampf.empire.domain.Player;
import android.graphics.Color;

public class LandTile extends Tile implements Serializable {

	public LandTile(int x, int y) {
		super(x, y);

		color = Color.argb(255, 34, 139, 34);
	}

	public String getType() {
		return "land";
	}

	@Override
	public Player getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

}
