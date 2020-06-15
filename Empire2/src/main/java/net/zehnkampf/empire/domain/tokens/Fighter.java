package net.zehnkampf.empire.domain.tokens;

import net.zehnkampf.empire.R;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

public class Fighter extends Token {

    public static int buildTime = 8;
    public static String name = "Fighter";
    
    public Fighter() {
    	this(-1, -1);
    }
    
	public Fighter(int x, int y) {
		super(x, y);
		super.name = name;
        super.buildTime = buildTime;

		viewRadius = 3;
		attackStrength = 3;
		
		speed = 9;
		range = 22;
		remainingRange = range;
		remainingRoundRange = speed;
		
        canFly = true;
        canSwim = false;
        
        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.fighter);
	}

	@Override
	public void draw(Paint paint) {
		// TODO Auto-generated method stub
	}

	public int getProductId() {
		return 1;
	}
	
}
