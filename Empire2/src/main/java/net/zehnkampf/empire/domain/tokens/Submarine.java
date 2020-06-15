package net.zehnkampf.empire.domain.tokens;

import net.zehnkampf.empire.R;
import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.map.CityTile;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

public class Submarine extends TransporterToken {

    public static int buildTime = 18;
    public static String name = "Submarine";
    
    private int maxMissiles = 2;
    private int noOfMissiles = 0;
    
    public Submarine() {
    	this(-1, -1);
    }
    
    public Submarine(int i, int j) {
        super(i, j);
        super.name = name;
        super.buildTime = buildTime;
        
        attackStrength = 3;
        defenseStrength = 2;
        
        viewRadius = 2;
        
        speed = 2;
        range = 200;
		remainingRange = range;
        remainingRoundRange = speed;

        canFly = false;
        canSwim = true;
        
        loadSprite();
    }
    
	public void loadSprite() {
	     originalSprite = BitmapFactory.decodeResource(view.getResources(), R.drawable.submarine);
	}
	
    public boolean canLoad(Token otherToken, int oldX, int oldY) {
    	return otherToken instanceof Missile && noOfMissiles < maxMissiles
    			&& WarGame.map.getTile(oldX, oldY) instanceof CityTile;
    }
    
    public void draw(Paint paint) {
    }

    public int getProductId() {
        return 11;
    }

	@Override
	public void increaseCount(Token token) {
		if (token instanceof Missile)
			noOfMissiles++;
	}

	@Override
	public void decreaseCount(Token token) {
		if (token instanceof Missile)
			noOfMissiles--;
	}

} // eof
