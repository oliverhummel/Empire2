package net.zehnkampf.empire.domain.tokens;

import net.zehnkampf.empire.R;
import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.map.CityTile;
import android.graphics.BitmapFactory;
import android.graphics.Paint;


public class Battleship extends TransporterToken {

    public static int buildTime = 28;
    public static String name = "Battleship";
    
    private int maxHelicopters = 1;
    private int maxMissiles = 2;
    private int noOfHelicopters = 0;
    private int noOfMissiles = 0;
    
    public Battleship() {
    	this(-1, -1);
    }
    
    public Battleship(int i, int j) {
        super(i, j);
        super.name = name;
        super.buildTime = buildTime;
        
        attackStrength = 7;
        defenseStrength = 8;
        
        viewRadius = 2;
        
        speed = 2;
        range = 150;
		remainingRange = range;
        remainingRoundRange = speed;

        canFly = false;
        canSwim = true;
        
        loadSprite();
	}

	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.battleship);
	}
    
    public boolean canLoad(Token otherToken, int oldX, int oldY) {
    	if (otherToken instanceof Helicopter && noOfHelicopters < maxHelicopters)
    		return true;
    	
    	if (otherToken instanceof Missile && noOfMissiles < maxMissiles 
    			&& WarGame.map.getTile(oldX, oldY) instanceof CityTile)
    		return true;
    		
    	return false;
    }
    
    public void draw(Paint paint) {
    }

    public int getProductId() {
        return 7;
    }

	@Override
	public void increaseCount(Token token) {
		if (token instanceof Helicopter)
			noOfHelicopters++;
		else if (token instanceof Missile)
			noOfMissiles++;
	}

	@Override
	public void decreaseCount(Token token) {
		if (token instanceof Helicopter)
			noOfHelicopters--;
		else if (token instanceof Missile)
			noOfMissiles--;
	}

}
