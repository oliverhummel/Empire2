package net.zehnkampf.empire.domain.tokens;

import net.zehnkampf.empire.R;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

public class Destroyer extends TransporterToken {

    public static int buildTime = 20;
    public static String name = "Destroyer";
    
    private int maxHelicopters = 1;
    private int noOfHelicopters = 0;
    
    public Destroyer() {
    	this(-1, -1);
    }
    
    public Destroyer(int i, int j) {
        super(i, j);
        super.name = name;
        super.buildTime = buildTime;
        
        attackStrength = 3;
        defenseStrength = 2;
        
        viewRadius = 3;
        
        speed = 3;
        range = 90;
		remainingRange = range;
        remainingRoundRange = speed;

        canFly = false;
        canSwim = true;
        
        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.destroyer);
	}
    
    public boolean canLoad(Token otherToken, int oldX, int oldY) {
    	return (otherToken instanceof Helicopter && noOfHelicopters < maxHelicopters);
    }
    
    public void draw(Paint paint) {
    }

    public int getProductId() {
        return 10;
    }

	@Override
	public void increaseCount(Token token) {
		if (token instanceof Helicopter)
			noOfHelicopters++;
	}

	@Override
	public void decreaseCount(Token token) {
		if (token instanceof Helicopter)
			noOfHelicopters--;
	}

} // eof
