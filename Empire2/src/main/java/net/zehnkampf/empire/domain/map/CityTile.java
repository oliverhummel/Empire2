package net.zehnkampf.empire.domain.map;

import java.io.Serializable;
import java.util.ArrayList;

import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.Fighting;
import net.zehnkampf.empire.domain.Player;
import net.zehnkampf.empire.domain.tokens.AWACS;
import net.zehnkampf.empire.domain.tokens.Army;
import net.zehnkampf.empire.domain.tokens.Battleship;
import net.zehnkampf.empire.domain.tokens.Carrier;
import net.zehnkampf.empire.domain.tokens.Destroyer;
import net.zehnkampf.empire.domain.tokens.Fighter;
import net.zehnkampf.empire.domain.tokens.Helicopter;
import net.zehnkampf.empire.domain.tokens.Missile;
import net.zehnkampf.empire.domain.tokens.Radar;
import net.zehnkampf.empire.domain.tokens.Submarine;
import net.zehnkampf.empire.domain.tokens.Token;
import net.zehnkampf.empire.domain.tokens.TransporterPlane;
import net.zehnkampf.empire.domain.tokens.TransporterShip;
import net.zehnkampf.empire.domain.tokens.TransporterToken;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class CityTile extends Tile implements Fighting, Serializable {

	private int defenseStrength = 3;
	private int attackStrength = 2;
	private int underAttack = -1;

	private int health = 100;

	private Player owner;
	private Token product;
	private Class products[];
	private int remainingTime;

	// TODO: viewRadius = 3; + interface Radar or so...

	private boolean port = false;
	private boolean island = false;
	final protected ArrayList<Token> tokens = new ArrayList<Token>();

	public CityTile(int x, int y) {
		super(x, y);

		port = hasSeaOrLandAccess(WarGame.map, true);
		island = !hasSeaOrLandAccess(WarGame.map, false);

		product = new Army(x, y);
		remainingTime = product.getBuildTime();

		int noOfProducts = (port) ? 12 : 7;
		products = new Class[noOfProducts];

		products[0] = Army.class;
		products[1] = Fighter.class;
		products[2] = Missile.class;
		products[3] = Helicopter.class;
		products[4] = Radar.class;
		products[5] = TransporterPlane.class;
		products[6] = AWACS.class;

		if (port) {
			products[7] = Battleship.class;
			products[8] = Carrier.class;
			products[9] = TransporterShip.class;
			products[10] = Destroyer.class;
			products[11] = Submarine.class;
		}	
	}

	private boolean hasSeaOrLandAccess(WorldMap map, boolean sea) {
		int startCheckX = Math.max(0, x - 1);
		int startCheckY = Math.max(0,  y - 1);

		int endCheckX = Math.min(x + 1, map.getWidth() - 1);
		int endCheckY = Math.min(y + 1, map.getHeight() - 1);

		for (int i = startCheckX; i <= endCheckX; i++) {
			for (int j = startCheckY; j <= endCheckY; j++) {
				if (i != x || j != y) {
					if (map.getTile(i, j) instanceof SeaTile && sea || (map.getTile(i, j) instanceof LandTile && !sea)) 
						return true;
				}
			}
		}

		return false;
	}

	@Override
	public void draw(Paint paint) {
		paint.setColor(Color.argb(255, 180, 23, 0));
	}

	public Player getOwner() {
		return owner;
	}

	public int getAttackStrength() {
		return attackStrength;
	}

	public int getDefenseStrength() {
		return defenseStrength;
	}

	public void setOwner(Player owner) {
		if (this.owner != null) {
			this.owner.removeCity(this);
		}

		owner.addCity(this);
		this.owner = owner;

		synchronized(WarGame.map) {
			for (Token token : tokens) {
				token.setOwner(owner);
			} // tokens
		}
	} // setOwner

	public boolean isPort() {
		return port;
	}

	public boolean isIsland() {
		return island;
	}

	public void addToken(Token token) {
		synchronized(WarGame.map) {
			tokens.add(token);
		}
	}

	public void removeToken(Token token) {
		synchronized(WarGame.map) {

			if (!token.isLoad())
				tokens.remove(token);
			else {
				for (Token potTransporter : tokens) {
					if (!(potTransporter instanceof TransporterToken))
						continue;
					else {
						if (((TransporterToken)potTransporter).hasLoaded(token))
							potTransporter.removeToken(token);
					}

				} // for
			} // else
		} // synch
	}

	public int getNumberOfTokens() {
		return tokens.size();
	}

	public Token nextRound() {
		health = Math.min(health + 20,  100);

		if(owner == null) 
			return null;

		Token deliverable = null;

		remainingTime--;
		if(remainingTime == 0) {
			product.setOwner(owner);
			product.setX(x);
			product.setY(y);
			product.setTarget(x, y);
			product.setAlive(true);
			product.setLastVisitedCity(this);
			tokens.add(product);

			deliverable = product;
			Log.v("product creation", "new "  + product.getName() + " created");

			try {
				product = product.getClass().newInstance();
				product.setX(x);
				product.setY(y);
				remainingTime = product.getBuildTime();
			} catch (Exception e) {
				Log.v("product creation", "exception");
			}
		}

		return deliverable;
	} // nextRound


	public Token createProduct(Class product) {
		Token token = null;

		try {
			token = (Token)product.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return token;
	}

	public void setProduct(int i) {
		product = createProduct(products[i]);
		remainingTime = product.getBuildTime();
	}

	public String getType() {
		return "city";
	}

	public Token getCurrentProduct() {
		return product;
	}

	public String[] getProductOverview() {
		String[] overview = new String[products.length];
		for (int i = 0; i < products.length; i++) {
			try {
				overview[i] = (String)products[i].getField("name").get(null);

				if (this.product.getClass() == products[i])
					overview[i] += " (" + remainingTime + ")";
				else
					overview[i] += " (" + (Integer)products[i].getField("buildTime").get(null) + ")";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return overview;
	}

	public Token hasNewProduct() {
		remainingTime--;
		Token token;
		if(remainingTime == 0) {
			remainingTime = product.getBuildTime();
			token = product;
			token.setOwner(owner);
		} else {
			token = null;
		}
		return token;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		if (this.health <= 0)
			health = 1;
		this.health = health;
	}

	public void decreaseHealth(int health) {
		this.health -= health;
		if (this.health <= 0)
			this.health = 1;
	}

	public String getInfo() {
		if (owner == null)
			return "Neutral City";
		else
			return "City owned by: " + getOwner();
	}

	public int getUnderAttack() {
		return underAttack;
	}

	public void setUnderAttack(int underAttack, int attackStrength) {
		this.underAttack = underAttack;

		for (Token token : tokens) {
			token.decreaseHealth((int)(Math.random() * 10 * attackStrength));

			if (token.getHealth() == 0)
				token.setAlive(false);
		} // tokens
	}

} // eof
