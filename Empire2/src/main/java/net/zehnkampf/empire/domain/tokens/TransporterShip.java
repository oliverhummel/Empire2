package net.zehnkampf.empire.domain.tokens;

import java.util.ArrayList;

import net.zehnkampf.empire.R;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.util.Log;


public class TransporterShip extends TransporterToken {

	public static int buildTime = 15;
	public static String name = "Transporter Ship";

	private int maxTanks = 5;
	private int noOfTanks = 0;
	final private ArrayList<Token> tokens = new ArrayList<Token>();

	public TransporterShip() {
		this(-1, -1);
	}

	public TransporterShip(int i, int j) {
		super(i, j);
		super.name = name;
		super.buildTime = buildTime;

		attackStrength = 0;
		defenseStrength = 1;

		viewRadius = 1;

		speed = 2;
		range = 45;
		remainingRange = range;
		remainingRoundRange = speed;

		canFly = false;
		canSwim = true;

        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.transporter);
	}

	public boolean canLoad(Token otherToken, int oldX, int oldY) {
		Log.v("TransporterShip canLoad", otherToken + " " + noOfTanks  +" " + maxTanks);
		return otherToken instanceof TankToken && noOfTanks < maxTanks;
	}

	public void draw(Paint paint) {
	}

	public int getProductId() {
		return 9;
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
