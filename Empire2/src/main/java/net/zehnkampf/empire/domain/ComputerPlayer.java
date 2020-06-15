package net.zehnkampf.empire.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.map.CityTile;
import net.zehnkampf.empire.domain.map.RadarService;
import net.zehnkampf.empire.domain.map.Tile;
import net.zehnkampf.empire.domain.tokens.Missile;
import net.zehnkampf.empire.domain.tokens.Token;
import net.zehnkampf.empire.domain.tokens.TransporterToken;
import android.util.Log;

public class ComputerPlayer extends Player implements Serializable {

	private transient MovementMode mode;
	private ArrayList<Tile> enemies;

	private Hashtable<Token, Integer> directions;

	private int direction = (int)(Math.random() * 8);

	public ComputerPlayer(String name, boolean human, boolean[][] worldView) {
		super(name, human, worldView);

		enemies = new ArrayList<Tile>();

		this.directions = new Hashtable<Token, Integer>();
	}

	public void moveToken(Token token) {
		if (directions.get(token) == null)
			directions.put(token, direction);

		CityTile enemyCity = findClosestCity(token, false);
		CityTile ownCity = findClosestCity(token, true);
		MapPosition enemy = RadarService.seesEnemy(token.getX(), token.getY(), token.getViewRadius(), token.getOwner());

		if (token.getTargetX() == -1 && enemy != null) {	// got stuck before? than try to attack something
			token.setTarget(enemy.getX(), enemy.getY());
		}
		else if (token.getRemainingRange() <= token.getRange() / 2 + 4 || token.getHealth() < 25) {
			CityTile myCity = RadarService.hasOwnCityAsNeighbor(token, this);
			if (myCity != null)
				token.setLastVisitedCity(myCity);

			int x, y;
			if (ownCity != null) {
				x = ownCity.getX();
				y = ownCity.getY();
			} else {
				x = token.getLastVisitedCity().getX();
				y = token.getLastVisitedCity().getY();
			}
			token.setTarget(x, y);
			Log.v("ComputerPlayer moveToken", token.getName() + " (" + token.getRemainingRange() + "/" + token.getHealth() + ") returns home to " + token.getTargetX() + " " + token.getTargetY() + " of " + token.getOwner().getName());
		}
		else if (enemyCity != null && token.getHealth() > 65 && token.getAttackStrength() >= 2) {
			token.setTarget(enemyCity.getX(), enemyCity.getY());
		}
		else if (enemy != null && token.getHealth() > 39 && token.getAttackStrength() >= 2) {
			token.setTarget(enemy.getX(), enemy.getY());
		}
		else {
			if (!(token instanceof Missile)) { 
				if (mode == null) {
					mode = new ExplorationMode();
				}
				mode.calculateTarget(token);
				direction++;
			}
		}

	} // moveToken

	public int choseProduction(CityTile city, boolean justConquered) {
		int product = 2; // missile

		if (!justConquered) {
			if (ownCities.size() == 1 && city.isIsland() && tokenCounter > 5)
				product = 10; // Destroyer

			if (tokenCounter > 4) 
				product = 3; // army

			if (tokenCounter > 6) 
				product = 5; // TransporterPlane

			if (tokenCounter > 7) 
				product = 3; // Helicopter

			if (tokenCounter > 10) 
				product = 1; // Fighter

			if (ownCities.size() > 5 || tokenCounter > 25) 
				product = (int)(Math.random() * city.getProductOverview().length);
		}
		
//		Log.v("pppppppppppppppppppppppppppppppppppppppppp"," " + product);

		return product;
	}

	// inner classes ============

	private abstract class MovementMode {
		public abstract void calculateTarget(Token token);	
	} // eoc

	private class ExplorationMode extends MovementMode {

		public void calculateTarget(Token token) {

			int tokenDirection = directions.get(token);
			int roundRange = token.getRemainingRoundRange();
			int range = token.getRemainingRange();

			int x = token.getX();
			int y = token.getY();
			int oldX = x;
			int oldY = y;

			int counter = 0;

			do {
				int deltaX = 0;
				int deltaY = 0;

				// TODO randomize somehow
				if (x >= WarGame.map.getWidth() - token.getViewRadius() && tokenDirection >= 1 && tokenDirection <= 3)
					tokenDirection = 4;
				if ( (x <= token.getViewRadius() && tokenDirection >= 5 && tokenDirection <=7))
					tokenDirection = 0;
				if (y >= WarGame.map.getHeight() - token.getViewRadius() && tokenDirection >= 3 && tokenDirection <= 5)
					tokenDirection = 2;
				if ( (y <= token.getViewRadius() && (tokenDirection == 7 || tokenDirection == 0 || tokenDirection == 1)))
					tokenDirection = 6;

				switch (tokenDirection) {
				case 0:
					deltaY = -1;
					break;
				case 1:
					deltaX = 1;
					deltaY = -1;
					break;
				case 2:
					deltaX = 1;
					break;
				case 3:
					deltaX = 1;
					deltaY = 1;
					break;
				case 4:
					deltaY = 1;
					break;
				case 5:
					deltaX = -1;
					deltaY = 1;
					break;
				case 6:
					deltaX = -1;
					break;
				case 7:
					deltaX = -1;
					deltaY = 1;
				} // switch

				if (deltaX + x < 0 || deltaX + x >= WarGame.map.getWidth())
					deltaX = 0;
				if (deltaY + y < 0 || deltaY + y >= WarGame.map.getHeight())
					deltaY = 0;

				counter++;
				if (deltaX == 0 && deltaY == 0 || !token.canMoveOnTile(x + deltaX, y + deltaY, oldX, oldY)) {
					tokenDirection = (tokenDirection + (int)(Math.random() * 8)) % 8;
					Log.v("ComputerPlayer calculateTarget", "direction changed for " + token);
					continue;
				}

				x += deltaX;
				y += deltaY;
				roundRange--;
				range--;
				counter = 0;

				if (token instanceof TransporterToken && WarGame.map.getTile(x, y) instanceof CityTile && !token.isLoaded()) {
					Log.v("ComputerPlayer moveToken", token.getName() + " returned " + range);
					break;
				}
			} while(roundRange > 0 && counter <= 8 && range > token.getRange() / 2 - 4);

			directions.put(token, tokenDirection);

			if (x < 0)
				x = 0;
			if (y < 0)
				y = 0;
			if (x > WarGame.map.getWidth() -1)
				x = WarGame.map.getWidth() -1;
			if (y > WarGame.map.getHeight() -1)
				y = WarGame.map.getWidth() -1;

			token.setTarget(x,  y);

			Log.v("ExplorationMode calculateTarget", x + " " + y +" for " + token.getName() + " @ " + token.getX() + " " + token.getY() + " " + tokenDirection);
		} // calculateTarget

	} // eoc

	private class AttackMode extends MovementMode {

		public void calculateTarget(Token token) {

		}
	} // eoc


} // eof
