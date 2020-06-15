package net.zehnkampf.empire.domain.tokens;

import net.zehnkampf.empire.R;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

public class Army extends TankToken {

    public static int buildTime = 4;
    public static String name = "Army";
    
    public Army() {
    	this(-1, -1);
    }
    
	public Army(int x, int y) {
		super(x, y);
        super.name = name;
        super.buildTime = buildTime;

		attackStrength = 2;
		defenseStrength = 2;
		
		range = 101;
		remainingRange = range;
		remainingRoundRange = speed;
		
        canFly = false;
        canSwim = false;
        
        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.tank);
	}

	@Override
	public void draw(Paint paint) {
		// TODO Auto-generated method stub

	}

	public int getProductId() {
		return 0;
	}
		
}
