package net.zehnkampf.empire.domain;

import java.io.Serializable;
import java.util.ArrayList;

import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.map.CityTile;
import net.zehnkampf.empire.domain.map.RadarService;
import net.zehnkampf.empire.domain.map.WorldMap;
import net.zehnkampf.empire.domain.tokens.Token;
import android.util.Log;

public class Player implements Serializable {

	private boolean human;
	private int color;

	private String name;
	private char shorthand;

	protected boolean[][] worldView, radarView;

	protected ArrayList<CityTile> ownCities = new ArrayList<CityTile>();
	protected ArrayList<CityTile> cities = new ArrayList<CityTile>();
	protected int tokenCounter = 0;
	protected int worldExplored = 0;
	
	public Player() {
		
	}

	public Player(String name, boolean human, boolean[][] worldView) {
		this.name = name;
		this.human = human;
		this.shorthand = name.charAt(name.length() - 1);

		this.worldView = worldView;
	}

	public boolean isHuman() {
		return human;
	}

	public String getName() {
		return name;
	}

	public void addCity(CityTile city) {
		ownCities.add(city);
	}

	public void removeCity(CityTile city) {
		ownCities.remove(city);
	}

	public boolean isKnowing(int x, int y) {
		return worldView[x][y];
	}

	public boolean[][] getView() {
		return worldView;
	}

	public int getCityCount() {
		return ownCities.size();
	}

	public void increaseTokenCount() {
		tokenCounter++;
	}

	public void decreaseTokenCount() {
		tokenCounter--;
	}

	public int getTokenCount() {
		return tokenCounter;
	}

	public char getShorthand() {
		return shorthand;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public float getWorldExplored() {
		Log.v("Player getWorldExplored", "" + worldExplored);
		return ((float)worldExplored) / (worldView.length * worldView[0].length) * 100;
	}

	public void addWorldExplored(int numberOfTiles) {
		this.worldExplored += numberOfTiles;
	}

	public boolean[][] updateRadarView(ArrayList<Token> tokens) {
		WorldMap map = WarGame.map;
		radarView = new boolean[map.getWidth()][map.getHeight()];

		for (Token token : tokens) {
			if (token.getOwner() == this && !token.isLoad() && token.isAlive())
				RadarService.makeVisible(radarView, token.getX(), token.getY(), token.getViewRadius());
		}
		for (CityTile city : ownCities) {
			if (city.getOwner() == this)
				RadarService.makeVisible(radarView, city.getX(), city.getY(), 2); //TODO 2 is not nice here...
		}

		cities.clear();
		for (CityTile city : WarGame.map.getCities()) {
			if (radarView[city.getX()][city.getY()])
				cities.add(city);
		}

		return radarView;
	}

	public boolean isRadarVisible(MapPosition pos) {
		int x,y;
		x = pos.getX();
		y = pos.getY();
		
		// TODO find a more elegant solution for this -1 business...
		if (x == -1 || y == -1)
			return false;
				
		return radarView[x][y];
	}

	public CityTile findClosestCity(Token token, boolean friendly) {
		CityTile closest = null;
		int distance = Integer.MAX_VALUE;

		// TODO check whether it is reachable at all...
		for (CityTile city : cities) {
			if (friendly && token.getOwner() == city.getOwner() || !friendly && token.getOwner() != city.getOwner()) {
				int newDistance = WarGame.map.getDistance(city, token);
				if (newDistance < distance) {
					distance = newDistance;
					closest = city;
				}
			} // friendly or not
		} // for

		return closest;
	}

} // eof
