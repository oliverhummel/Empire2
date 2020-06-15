package net.zehnkampf.empire.domain.tokens;

import net.zehnkampf.empire.R;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

// Referenced classes of package net.zehnkampf.empire.domain.tokens:
//            Token

public class Missile extends Token {

    public static int buildTime = 3;
    public static String name = "Missile";
    
    public Missile() {
    	this(-1, -1);
    }
    
    public Missile(int i, int j) {
        super(i, j);
        super.name = name;
        super.buildTime = buildTime;
        
        attackStrength = 10;
        defenseStrength = 0;
        
        viewRadius = 1;
        speed = 12;
        range = 30;
		remainingRange = range;
        remainingRoundRange = speed;
        
        canFly = true;
        canSwim = false;
        
        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.missile);
	}
	
    public void draw(Paint paint) {
    }

    public int getProductId() {
        return 2;
    }


}
