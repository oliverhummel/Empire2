package net.zehnkampf.empire.domain.map;

import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.MapPosition;
import net.zehnkampf.empire.domain.Player;
import net.zehnkampf.empire.domain.tokens.Token;
import android.util.Log;

public class RadarService {

	// TODO refactor x,y to MapPosition
	public static MapPosition seesEnemy(int x, int y, int viewRange, Player owner) {
		int startCheckX = Math.max(0, x - viewRange);
		int startCheckY = Math.max(0, y - viewRange);

		int endCheckX = Math.min(x + viewRange, WarGame.map.getWidth() - 1);
		int endCheckY = Math.min(y + viewRange, WarGame.map.getHeight() - 1);
		
		Log.v("RadarService enemy" , x + " " + y + " " + startCheckX + " " + startCheckY + " " + endCheckX + " " + endCheckY);

		for (int i = startCheckX; i <= endCheckX; i++) {
			for (int j = startCheckY; j <= endCheckY; j++) {
				if (x != i || y != j) {
					Tile tile = WarGame.map.getTile(i,j);
					if (tile.hasToken() && tile.getToken().getOwner() != owner || tile instanceof CityTile && ((CityTile)tile).getOwner() != owner) {
						Log.v("RadarService seesEnemy?", "yes, on: " + i + " " + j);
						return WarGame.map.getTile(i,j).getToken();
					}
				}
			}
		}
		return null;
	}
	
	public static boolean isVisibleSubmarine(MapPosition sub, Player enemy) {
		int startCheckX = Math.max(0, -1 + sub.getX());
		int startCheckY = Math.max(0, -1 + sub.getY());

		int endCheckX = Math.min(1 + sub.getX(), WarGame.map.getWidth() - 1);
		int endCheckY = Math.min(1 + sub.getY(), WarGame.map.getHeight() - 1);

		for (int i = startCheckX; i <= endCheckX; i++) {
			for (int j = startCheckY; j <= endCheckY; j++) {
				Token token = null;
				if ((token = WarGame.map.getTile(i, j).getToken()) != null) {
					if (token.getOwner() == enemy)
					return true;
				}
			}
		}
		return false;
	} // visibleSubmarine

	public static MapPosition hasDirectEnemyContact(int x, int y, Player owner) {
		return seesEnemy(x, y, 1, owner);
	}

	public static int makeVisible(boolean[][] view, int x, int y, int viewRadius) {
		if (x == -1 || viewRadius == 7 && WarGame.map.getTile(x, y) instanceof CityTile)  // Landed AWACS plane
			return 0;
		
		int newCounter = 0;

		int startCheckX = Math.max(0, -viewRadius + x);
		int startCheckY = Math.max(0, -viewRadius + y);

		int endCheckX = Math.min(viewRadius + x, WarGame.map.getWidth() - 1);
		int endCheckY = Math.min(viewRadius + y, WarGame.map.getHeight() - 1);

		for (int i = startCheckX; i <= endCheckX; i++) {
			for (int j = startCheckY; j <= endCheckY; j++) {
				if (!view[i][j])
					newCounter++;
				view[i][j] = true;
			}
		}

		return newCounter;
	}
	
	public static CityTile hasEnemyCityAsNeighbor(MapPosition position, Player owner) {
		return hasCityAsNeighbor(position, owner, false);
	}

	public static CityTile hasOwnCityAsNeighbor(MapPosition position, Player owner) {
		return hasCityAsNeighbor(position, owner, true);
	}

	private static CityTile hasCityAsNeighbor(MapPosition position, Player owner, boolean friendWanted) {
		int x = position.getX();
		int y = position.getY();

		int startCheckX = Math.max(0, x - 1);
		int startCheckY = Math.max(0,  y - 1);

		int endCheckX = Math.min(x + 1, WarGame.map.getWidth() - 1);
		int endCheckY = Math.min(y + 1, WarGame.map.getHeight() - 1);

		for (int i = startCheckX; i <= endCheckX; i++) {
			for (int j = startCheckY; j <= endCheckY; j++) {
				if (i != x || j != y) {
					if (WarGame.map.getTile(i, j) instanceof CityTile && 
							(WarGame.map.getTile(i, j).getOwner() == owner && friendWanted 
							|| WarGame.map.getTile(i, j).getOwner() != owner && !friendWanted)) 
						return (CityTile)WarGame.map.getTile(i, j);
				}
			} // j
		} // i

		return null;
	}

} // eof
