package net.zehnkampf.empire.domain.tokens;

import java.util.ArrayList;

import net.zehnkampf.empire.R;
import android.graphics.BitmapFactory;
import android.graphics.Paint;


public class Carrier extends TransporterToken {

    public static int buildTime = 32;
    public static String name = "Carrier";
    
    private int maxHelicopters = 3;
    private int maxFighters = 10;
    private int noOfHelicopters = 0;
    private int noOfFighters = 0;
	final private ArrayList<Token> tokens = new ArrayList<Token>();
    
    public Carrier() {
    	this(-1, -1);
    }
    
    public Carrier(int i, int j) {
        super(i, j);
        super.name = name;
        super.buildTime = buildTime;
        
        attackStrength = 1;
        defenseStrength = 3;
        
        viewRadius = 3;
//        shortHandPosition = 5;
        
        speed = 2;
        range = 250;
		remainingRange = range;
        remainingRoundRange = speed;

        canFly = false;
        canSwim = true;

        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.carrier);
	}
	
    public boolean canLoad(Token otherToken, int oldX, int oldY) {
    	if (otherToken instanceof Helicopter && noOfHelicopters < maxHelicopters)
    		return true;
    	
    	if (otherToken instanceof Fighter && noOfFighters < maxFighters)
    		return true;
    		
    	return false;
    }

    public void draw(Paint paint) {
    }

    public int getProductId() {
        return 8;
    }

	@Override
	public void increaseCount(Token token) {
		if (token instanceof Helicopter)
			noOfHelicopters++;
		else if (token instanceof Fighter)
			noOfFighters++;
	}

	@Override
	public void decreaseCount(Token token) {
		if (token instanceof Helicopter)
			noOfHelicopters--;
		else if (token instanceof Fighter)
			noOfFighters--;
	}

}
