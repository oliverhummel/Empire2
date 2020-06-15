package net.zehnkampf.empire.domain.tokens;

import net.zehnkampf.empire.R;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

public class Helicopter extends Token {

    public static int buildTime = 5;
    public static String name = "Combat Helicopter";
    
    
    public Helicopter() {
    	this(-1, -1);
    }
    
	public Helicopter(int x, int y) {
		super(x, y);
		super.name = name;
        super.buildTime = buildTime;

		viewRadius = 3;
		attackStrength = 3;
		defenseStrength = 2;
		
		speed = 5;
		range = 16;
		remainingRange = range;
		remainingRoundRange = speed;
		
        canFly = true;
        canSwim = false;
        
        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.helicopter);
	}
	
	@Override
	public void draw(Paint paint) {
		// TODO Auto-generated method stub
	}

	public int getProductId() {
		return 3;
	}
	
}
