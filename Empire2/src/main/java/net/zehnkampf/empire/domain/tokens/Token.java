package net.zehnkampf.empire.domain.tokens;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.ComputerPlayer;
import net.zehnkampf.empire.domain.Fighting;
import net.zehnkampf.empire.domain.MapPosition;
import net.zehnkampf.empire.domain.Player;
import net.zehnkampf.empire.domain.map.CityTile;
import net.zehnkampf.empire.domain.map.LandTile;
import net.zehnkampf.empire.domain.map.RadarService;
import net.zehnkampf.empire.domain.map.SeaTile;
import net.zehnkampf.empire.domain.map.Tile;
import net.zehnkampf.empire.domain.map.WorldMap;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public abstract class Token implements MapPosition, Fighting, Serializable {

	protected int attackStrength;
	protected int buildTime;

	protected boolean canFly;
	protected boolean canSwim;
	protected boolean current;
	protected boolean alive; // needed for ignoring destroyed tokens when drawing map
	protected boolean load; // also needed for drawing map

	protected int defenseStrength;
	protected int health;

	protected int shortHandPosition = 0;
	protected String name;
	protected Player owner;
	protected int range;
	protected int remainingRange;
	protected int remainingRoundRange;
	protected int age;
	protected int mileage;

	protected int speed;
	protected int viewRadius;

	protected int targetX;
	protected int targetY;
	protected CityTile lastVisitedCity;

	protected int x;
	protected int y;

	protected boolean onGuard;
	private boolean enemyWarning = false; 

	// ---------------------------------

	public transient static View view;
	protected transient Bitmap originalSprite;
	protected transient Hashtable<Integer, Bitmap> sprites;

	// ---------------------------------

	public Token() {}

	public Token(int x, int y) {		
		this.x = targetX = x;
		this.y = targetY = y;

		if (x != -1)
			WarGame.map.getTile(x, y).addToken(this);

		viewRadius = 1;
		speed = 1;
		remainingRoundRange = speed;
		range = Integer.MAX_VALUE;
		alive = false;
		load = false;

		health = 100;
		buildTime = 4;

		attackStrength = 1;
		defenseStrength = 1;
		age = 0;

		sprites = new Hashtable<Integer, Bitmap>();
	}

	public void newRound() {
		if (x != -1 && alive && WarGame.map.getTile(x, y) instanceof CityTile || load && WarGame.map.getTile(x, y) instanceof SeaTile) {
			health = Math.min(health + 200 / buildTime + 1, 100);
			remainingRange = range;
		}

		remainingRoundRange = Math.min(speed, remainingRange);
		age++;
	}

	public int getViewRadius() {
		return viewRadius;
	}
	public int getSpeed() {
		return speed;
	}
	public int getRange() {
		return range;
	}

	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		this.health = health;
	}

	public void decreaseHealth(int health) {
		this.health -= health;

		//		if (this.health < 5)
		//			this.health = 5;
	}

	public int getBuildTime() {
		return buildTime;
	}
	public int getAttackStrength() {
		return attackStrength;
	}
	public int getDefenseStrength() {
		return defenseStrength;
	}

	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}

	public boolean isOnGuard() {
		return onGuard;
	}
	public void setOnGuard(boolean onGuard) {
		this.onGuard = onGuard;
	}

	public int getTargetX() {
		return targetX;
	}

	public int getTargetY() {
		return targetY;
	}

	public Player getOwner() {
		return owner;
	}
	public void setOwner(Player owner) {
		this.owner = owner;

		if (owner != null) {
			owner.increaseTokenCount();
			owner.addWorldExplored(RadarService.makeVisible(owner.getView(), x, y, viewRadius));
		}
	}

	public void setTarget(int x, int y) {
		targetX = x;
		targetY = y;
	}

	public boolean hasReachedTarget() {
		//		Log.v(" target?", "auto " + (x == targetX && y == targetY));
		return x == targetX && y == targetY || load;
	}

	public boolean mustMove() {
		return (targetX != x || targetY != y);
	}

	public boolean hasRangeLeft() {
		return remainingRoundRange > 0 && remainingRange > 0;
	}

	public boolean canFly() {
		return canFly;
	}

	public boolean canMoveOnTile(int x, int y, int oldX, int oldY) {
		Tile tile = WarGame.map.getTile(x, y);
		Token token = tile.getToken();

		if (token != null)
			Log.v("Token: canMoveOnTile", token.getName() + " " + x + " " + y);

		boolean result = (token == null) && ((tile instanceof SeaTile && (canSwim || canFly)) || (tile instanceof LandTile) && !canSwim);
		Log.v("Token canMoveonTile result1", "" + result);
		result = result || (tile instanceof CityTile) ;
		Log.v("Token canMoveonTile result2", "" + result);
		result = result && (!(this instanceof Missile && tile instanceof CityTile && tile.getOwner() == this.getOwner()))
				|| (token != null && token.getOwner() == this.owner && token.canLoad(this, oldX, oldY) );
		Log.v("Token canMoveonTile result3", "" + result + " " + token + " " + WarGame.map.getTile(oldX, oldY) + " " + x + " " + y + " " + this.x + " " + this.y);

		MapPosition enemy = RadarService.hasDirectEnemyContact(oldX, oldY, owner);
		if (enemy != null && !enemyWarning && WorldMap.calculateDistance(oldX, oldY, enemy.getX(), enemy.getY()) > 1) {
			enemyWarning = true;
			result = false;
		} else if (enemyWarning && enemy == null)
			enemyWarning = false;

		Log.v("Token canMoveon Tile final", "" + result);

		return result;
	}

	public boolean canSwim() {
		return canSwim;
	}

	public void setRemainingRange(int remainingRange) {
		this.remainingRange = remainingRange;
	}

	public boolean canLoad(Token otherToken, int oldX, int oldY) {
		return false;
	}

	public boolean move() throws ImpossibleMoveException, WaitForLoadException {
		Log.v("Token move target", "target: " + targetX + " " + targetY);
		//		if (targetX == -1) {
		//			setTarget(x, y);
		//			return true;
		//		}

		WorldMap map = WarGame.map;

		int oldX = x;
		int oldY = y;
		int deltaX = (x - targetX) * -1;
		int deltaY = (y - targetY) * -1;

		if (deltaX > 0)
			x++;
		else if (deltaX < 0)
			x--;

		if (deltaY > 0)
			y++;
		else if (deltaY < 0)
			y--;

		// TODO find a more elegant solution
		Token potentialEnemy = map.getTile(x, y).getToken();
		if(!canMoveOnTile(x, y, oldX, oldY) && !(potentialEnemy != null && potentialEnemy.getOwner() != owner) 
													&& !(Math.abs(oldX-targetX) <= 1 && Math.abs(oldY-targetY) <= 1)) {
			if(deltaX == 0) {
				x++;
				if(!canMoveOnTile(x, y, oldX, oldY))
					x-=2;
			}
			else if (deltaY == 0) {
				y++;
				if(!canMoveOnTile(x, y, oldX, oldY))
					y -=2;
			}
			else if (deltaX <=1 && deltaY <= 1) {
				setTarget(oldX, oldY);
			}
			else if (deltaX > 0 && deltaY > 0 || deltaX > 0 && deltaY < 0) {
				x = oldX;
				if(!canMoveOnTile(x, y, oldX, oldY)) {
					x++;
					y = oldY;
				}	
			}
			else if (deltaX < 0 && deltaY < 0 || deltaX < 0 && deltaY > 0) {
				x = oldX;
				if(!canMoveOnTile(x, y, oldX, oldY)) {
					x--;
					y = oldY;
				}	
			}

//			if(!canMoveOnTile(x, y, oldX, oldY)) {
				//				setTarget(x,y);
//				x = oldX;
//				y = oldY;
//			}


			Log.v("Token move", "tried it " + x + " " + y);

		} // endif first can move?

		if(canMoveOnTile(x, y, oldX, oldY)) {
			Log.v("Token.move()", x + " " + y);

			remainingRoundRange--;
			remainingRange--;
			owner.addWorldExplored(RadarService.makeVisible(owner.getView(), x, y, viewRadius));

			//			if (RadarService.hasDirectEnemyContact(x, y,viewRadius, owner) != null
			//					&& WorldMap.calculateDistance(x, y, targetX, targetX) > 1) {
			//				targetX = x;
			//				targetY = y;
			//				
			//				return true;
			//			}

			if(map.getTile(x, y) instanceof CityTile) {
				CityTile city = (CityTile) map.getTile(x, y);
				targetX = x;
				targetY = y;

				if (city.getOwner() == owner) {
					remainingRoundRange = 0;
					lastVisitedCity = city;
				//	map.getTile(oldX, oldY).removeToken(this);
				} else {
					// special move for attack
					x = oldX;
					y = oldY;

					return false;
				}
			} 

			if (map.getTile(x, y).getToken() != null && map.getTile(x, y).getToken().canLoad(this, oldX, oldY)) {
				if (!(this instanceof Missile) || (map.getTile(oldX, oldY) instanceof CityTile)) {
					Log.v("Token.move()", "load, removing from " + oldX + " " + oldY);
					map.getTile(oldX,  oldY).removeToken(this);
					map.getTile(x, y).getToken().addToken(this);
					remainingRoundRange = 0;
					targetX = -1;
					targetY = -1;
				}
			} else {
				map.getTile(oldX, oldY).removeToken(this);
				if (load) {
					this.load = false;
				}
				map.getTile(x, y).addToken(this);
			}
		} else if (map.getTile(x, y).getToken() != null && map.getTile(x, y).getToken().getOwner() != this.owner && WorldMap.calculateDistance(x, y, targetX, targetY) == 0) {
			// attack
			targetX = x;
			targetY = y;

			x = oldX;
			y = oldY;

			remainingRoundRange--;
			remainingRange--;

			Log.v("move token", "return false -> attack!");
			return false;

		} else {
			noPossibleMove(oldX, oldY);	
			throw new ImpossibleMoveException();
		}

		mileage += Math.max(Math.abs(x-oldX), Math.abs(y-oldY)); 
		
		if (this instanceof TransporterToken && owner instanceof ComputerPlayer && this.remainingRange > 5 && !this.isLoaded()
				&& map.getTile(oldX, oldY) instanceof CityTile && ((CityTile)map.getTile(oldX, oldY)).getOwner() == owner) {
			throw new WaitForLoadException();
		}
			

		return true;
	}

	private void noPossibleMove(int oldX, int oldY) {
		Log.v("Token: move not possible", "ha ha");
		x = oldX;
		y = oldY;
		targetX = oldX;
		targetY = oldY;
	}

	public void addToken(Token token) {
		// do nothing
	}

	public void removeToken(Token token) {
		// do nothing
	}

	public abstract void draw(Paint paint);

	public String getName() {
		return name;
	}

	public abstract int getProductId();


	public int getRemainingRange() {
		return remainingRange;
	}

	public char getShorthand() {
		// TODO find better solution
		return name.toUpperCase().charAt(shortHandPosition);
	}

	public int getRemainingRoundRange() {
		return remainingRoundRange;
	}


	public int getMileage() {
		return mileage;
	}

	public int getAge() {
		return age;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public CityTile getLastVisitedCity() {
		return lastVisitedCity;
	}

	public void setLastVisitedCity(CityTile lastVisitedCity) {
		this.lastVisitedCity = lastVisitedCity;
	}

	public boolean isLoad() {
		return load;
	}

	public void setLoad(boolean loaded) {
		this.load = loaded;
	}

	public boolean isLoaded() {
		return false;
	}

	public boolean loadIsCurrentToken() {
		return false;
	}

	public ArrayList<Token> getTokens() {
		return null;
	}

	public boolean isStronger(Token enemy) {
		return enemy.defenseStrength > attackStrength;
	}

	public boolean canMoveAnywhere() {
		int startCheckX = Math.max(0, x - 1);
		int startCheckY = Math.max(0,  y - 1);

		int endCheckX = Math.min(x + 1, WarGame.map.getWidth() - 1);
		int endCheckY = Math.min(y + 1, WarGame.map.getHeight() - 1);

		for (int i = startCheckX; i <= endCheckX; i++) {
			for (int j = startCheckY; j <= endCheckY; j++) {
				if (i != x || j != y) {
					if (canMoveOnTile(i,j, x, y)) {
						Log.v("Token can Move anywhere?", "yes, on: " + i + " " + j);
						return true;
					}
				}
			}
		}
		return false;
	}

	public Bitmap getSprite(int tileSize) {	
		if (sprites == null) {                     // cannot be serialized and thus must be loaded separately when a game was saved
			sprites = new Hashtable<Integer, Bitmap>();	
			loadSprite();
		}

		Bitmap sprite = sprites.get(tileSize);
		if (sprite == null) {
			sprite = Bitmap.createScaledBitmap(originalSprite, tileSize, tileSize, true);
			sprites.put(tileSize, sprite);
		}

		return sprite;
	}

	public abstract void loadSprite();
} // eof
