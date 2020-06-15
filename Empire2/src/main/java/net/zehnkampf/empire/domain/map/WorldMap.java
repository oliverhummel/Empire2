package net.zehnkampf.empire.domain.map;

import java.io.Serializable;
import java.util.ArrayList;

import net.zehnkampf.empire.domain.MapPosition;
import android.util.Log;

public class WorldMap implements Serializable {

	private final int width;
	private final int height;
	private transient boolean newUnitIntroMode = false;

	private Tile[][] map;
	private ArrayList<CityTile> cities = new ArrayList<CityTile>();

	public WorldMap(int width, int height) {
		// create map
		this.width = width;
		this.height = height;
		map = new Tile[width][height];

		// create some islands
		int islands = (int)(Math.random() * (width * height / 501)) + 5;
		Log.v("islands", "" + islands);
		for (int i = 0; i < islands; i++) {
			int x = (int)(Math.random() * width);
			int y = (int)(Math.random() * height);
			map[x][y] = new LandTile(x, y);

			// add land
			int size = (int)(Math.random() * (width * height / 11)) + 25;
			int xNew = x; int yNew = y;
			for (int j = 0; j < size; j ++) {
				xNew = Math.abs( ((int)(Math.random() * 3)) -1 + xNew) % width;
				yNew = Math.abs( ((int)(Math.random() * 3)) -1 + yNew) % height;
				map[xNew][yNew] = new LandTile(x, y);	
			}
		} // for i


		// finally fill with sea...
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map.length; y++) {
				if (map[x][y] == null)
					map[x][y] = new SeaTile(x, y);
			} // for j
		} // for i
	} // constructor

	// TODO unify distance methods?
	public int getDistance(MapPosition pos1, MapPosition pos2) {
		return Math.max(Math.abs(pos1.getX() - pos2.getX()), Math.abs(pos1.getY() - pos2.getY()) );
	}
	
	public static int calculateDistance(int x1, int y1, int x2, int y2) {
		Log.v("WorldMap calculateDistance", "sch... " + x1 + " " + y1 + " " + x2 + " " + y2);
		return Math.max(Math.abs(x1-x2), Math.abs(y1-y2));
	}

	public void createCities() {
		// create cities
		int cityCount = (int)(Math.random() * (width * height / 550)) + 42;
		Log.v("cities", "" + cityCount);
		for (int i = 0; i < cityCount; i++) {
			int x, y;
			do {
				x = (int)(Math.random() * width);
				y = (int)(Math.random() * height);
				Log.v("repeat cityyy", "" + i);
			} while((i % 10 != 1 && map[x][y] instanceof SeaTile) || hasCityAsNeighbor(x, y) || map[x][y] instanceof CityTile);  // avoid too many lonely cities in the sea and "twin cities"...

			CityTile city =  new CityTile(x, y);
			map[x][y] = city;
			cities.add(city);
		} // for i
	}

	private boolean hasCityAsNeighbor(int x, int y) {
		int startCheckX = Math.max(0, x - 1);
		int startCheckY = Math.max(0,  y - 1);

		int endCheckX = Math.min(x + 1, width - 1);
		int endCheckY = Math.min(y + 1, height - 1);

		for (int i = startCheckX; i <= endCheckX; i++) {
			for (int j = startCheckY; j <= endCheckY; j++) {
				if (i != x || j != y) {
					if (map[i][j] instanceof CityTile) 
						return true;
				}
			}
		}

		return false;
	}

	public Tile getTile(int x, int y) {
		// TODO check this...
		if (x == -1 || y == -1)
			return null;
		return map[x][y];
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public CityTile getCity(int no) {
		return cities.get(no);
	}

	public ArrayList<CityTile> getCities() {
		return cities;
	}

	public boolean[][] generateVisibilityMap() {
		return new boolean[width][height];
	}

	// TODO: remove
	public void removeCity(CityTile city) {
		int x = city.getX();
		int y = city.getY();
		map[x][y] = new LandTile(x, y);

		cities.remove(city);
	}

	public boolean isInNewUnitIntroMode() {
		return newUnitIntroMode;
	}

	public void setNewUnitIntroMode(boolean newUnitIntroMode) {
		this.newUnitIntroMode = newUnitIntroMode;
	}
	
} // eof
