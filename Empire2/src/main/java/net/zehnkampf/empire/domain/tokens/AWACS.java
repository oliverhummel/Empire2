package net.zehnkampf.empire.domain.tokens;

import net.zehnkampf.empire.R;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

public class AWACS extends Token {

    public static int buildTime = 16;
    public static String name = "AWACS";
    
    public AWACS() {
    	this(-1, -1);
    }
    
	public AWACS(int x, int y) {
		super(x, y);
		super.name = name;
        super.buildTime = buildTime;

		viewRadius = 7;
		attackStrength = 0;
		shortHandPosition = 1;
		
		speed = 5;
		range = 20;
		remainingRange = range;
		remainingRoundRange = speed;
		
        canFly = true;
        canSwim = false;
        
        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.awacs);
	}

	public void draw(Paint paint) {
		// TODO Auto-generated method stub
	}

	public int getProductId() {
		return 6;
	}
	
}
