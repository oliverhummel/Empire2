package net.zehnkampf.empire.domain.tokens;

import java.util.ArrayList;

import net.zehnkampf.empire.R;
import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.map.CityTile;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.util.Log;


public class TransporterPlane extends TransporterToken {

	public static int buildTime = 12;
	public static String name = "Transporter Plane";

	private int maxTanks = 2;
	private int noOfTanks = 0;
	final private ArrayList<Token> tokens = new ArrayList<Token>();

	public TransporterPlane() {
		this(-1, -1);
	}

	public TransporterPlane(int i, int j) {
		super(i, j);
		super.name = name;
		super.buildTime = buildTime;

		attackStrength = 0;
		defenseStrength = 0;

		viewRadius = 1;

		speed = 5;
		range = 24;
		remainingRange = range;
		remainingRoundRange = speed;

		canFly = true;
		canSwim = false;
		
		shortHandPosition = 12;
		
        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.tplane);
	}

	public boolean canLoad(Token otherToken, int oldX, int oldY) {
		Log.v("TransporterPlane canLoad", otherToken + " " + noOfTanks  +" " + maxTanks);
		Log.v("canload", otherToken.getX() + " " + otherToken.getY());
		return otherToken instanceof TankToken && noOfTanks < maxTanks 
								&& WarGame.map.getTile(oldX, oldY) instanceof CityTile;
	}

	public void draw(Paint paint) {
	}

	public int getProductId() {
		return 5;
	}

	@Override
	public void increaseCount(Token token) {
		if (token instanceof TankToken)
			noOfTanks++;
	}

	@Override
	public void decreaseCount(Token token) {
		if (token instanceof TankToken)
			noOfTanks--;
	}

} // eof
