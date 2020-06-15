package net.zehnkampf.empire.domain.tokens;

import net.zehnkampf.empire.R;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

public class Radar extends TankToken {

    public static int buildTime = 5;
    public static String name = "Mobile Radar";
    
    public Radar() {
    	this(-1, -1);
    }
    
	public Radar(int x, int y) {
		super(x, y);
        super.name = name;
        super.buildTime = buildTime;

		attackStrength = 1;
		defenseStrength = 0;
		viewRadius = 4; // 100;
		shortHandPosition = 7;
		
		range = 60;
		remainingRange = range;
		remainingRoundRange = speed;
		
        canFly = false;
        canSwim = false;
	
        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.radar);
	}
	
	@Override
	public void draw(Paint paint) {
		// TODO Auto-generated method stub

	}

	public int getProductId() {
		return 4;
	}

}
