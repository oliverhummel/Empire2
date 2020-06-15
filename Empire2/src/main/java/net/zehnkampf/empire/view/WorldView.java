package net.zehnkampf.empire.view;

import java.util.ArrayList;

import net.zehnkampf.empire.R;
import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.ComputerPlayer;
import net.zehnkampf.empire.domain.Player;
import net.zehnkampf.empire.domain.map.CityTile;
import net.zehnkampf.empire.domain.map.RadarService;
import net.zehnkampf.empire.domain.map.Tile;
import net.zehnkampf.empire.domain.map.WorldMap;
import net.zehnkampf.empire.domain.tokens.Missile;
import net.zehnkampf.empire.domain.tokens.Submarine;
import net.zehnkampf.empire.domain.tokens.Token;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class WorldView extends View {

	private WorldMap map;
	private Player player;

	private int TS = WarGame.TILE_SIZE;

	private ArrayList<Token> tokens;
	//	private ArrayList<CityTile> cities;

	private Bitmap tent, mushroom, boom;

	public WorldView(Context context, Player player, ArrayList<Token> tokens, ArrayList<CityTile> cities) {
		super(context);

		tent = BitmapFactory.decodeResource(this.getResources(), R.drawable.tent); // TODO: get rid of this...
		mushroom = BitmapFactory.decodeResource(this.getResources(), R.drawable.mushroom);
		boom = BitmapFactory.decodeResource(this.getResources(), R.drawable.boom );

		initialize(player, tokens, cities);
	}

	public void initialize(Player player, ArrayList<Token> tokens, ArrayList<CityTile> cities) {
		this.map = WarGame.map;

		this.player = player;
		this.tokens = tokens;
		//		this.cities = cities;

		setMinimumHeight(map.getWidth() * TS);
		setMinimumWidth(map.getHeight() * TS);

		this.invalidate();
	}

	public void setZoom(int size) {
		TS = size;
		ViewGroup.LayoutParams params = this.getLayoutParams();
		params.width = map.getWidth() * TS;
		params.height = map.getHeight() * TS;
		this.setLayoutParams(params);
		invalidate();
	}

	public void setTokens(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//		Log.v("WorldView", "onDraw");

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);

		final boolean[][] radarView = player.updateRadarView(tokens);
		float size = paint.getTextSize();

		drawMap(canvas, paint, radarView);	
		drawTokens(canvas, paint, size, radarView);

	} // onDraw

	private void drawMap(Canvas canvas, Paint paint, final boolean[][] radarView) {
		synchronized(WarGame.map) { 
			for (int x = 0; x < map.getWidth(); x++) {
				for (int y = 0; y < map.getHeight(); y++) {

					Tile tile = map.getTile(x, y);

					if (player.isKnowing(x, y)) {
						tile.draw(paint);
					} else
						paint.setColor(Color.BLACK);
					canvas.drawRect(x * TS, y * TS, (x + 1) * TS, (y + 1) * TS, paint);

					// ---

					paint.setColor(Color.DKGRAY);
					canvas.drawRect(x * TS -1, y * TS -1, x * TS, y * TS, paint);

					if ((x == 0 || x == map.getWidth() - 1) && y % 5 == 0) 
						canvas.drawText("" + y, 2, y * TS + TS / 2, paint);
					if ((y == 0 || y == map.getHeight() - 1) && x % 5 == 0) 
						canvas.drawText("" + x, x * TS + 2, y * TS + TS / 2, paint);

					// do not move up, otherwise city will be in player color
					if (player.isKnowing(x, y)) {
						if (tile instanceof CityTile) {
							Player owner = ((CityTile)tile).getOwner();
							if (null != owner) {
								paint.setColor(owner.getColor());
								paint.setStyle(Paint.Style.STROKE);
								paint.setStrokeWidth(3);

								canvas.drawRect(x * TS, y * TS, (x +1) * TS+1, (y+1) * TS+1, paint);
								if (tile instanceof CityTile && ((CityTile)tile).getNumberOfTokens() > 0) {
									//								text += ".";
									canvas.drawText(".", x * TS + 3, y * TS + 6, paint); 
								}

								paint.setStyle(Paint.Style.FILL);
							}
						} // endif CityTile

						if (!radarView[x][y]) {
							paint.setARGB(140, 0, 0, 0);	
							canvas.drawRect(x * TS, y * TS, (x + 1) * TS, (y + 1) * TS, paint);
						}

						if (map.getTile(x, y).isShowExplosion()) {
							ColorFilter filter = new LightingColorFilter(0, Color.LTGRAY);
							paint.setColorFilter(filter);
							map.getTile(x, y).setShowExplosion(false);
							Bitmap d = Bitmap.createScaledBitmap(mushroom, TS, TS, true);
							canvas.drawBitmap(d, x * TS, y * TS , paint);
							Log.v("bomb", "yeeehaaa!");
						}
						if (map.getTile(x, y).isShowBoom()) {
							ColorFilter filter = new LightingColorFilter(0, Color.LTGRAY);
							paint.setColorFilter(filter);
							map.getTile(x, y).setShowBoom(false);
							Bitmap d = Bitmap.createScaledBitmap(boom, TS, TS, true);
							canvas.drawBitmap(d, x * TS, y * TS , paint);
							this.invalidate();
							Log.v("boom", "yeeehaaa!");
						}

						paint.setColorFilter(null);
					} // endif

				} // for y
			} // for x
		} // synch
	} // drawMap


	private void drawTokens(Canvas canvas, Paint paint, float size, final boolean[][] radarView) {
		paint.setTextSize(TS / 1.5f);

		synchronized(WarGame.map){ 
			
//			Log.v("ssssiiiiiize", " " +map.getCities().size());
//			for (CityTile city : map.getCities()) {
//				if (city.getOwner() != null)
//				Log.v("ccccccccc", city.getX() + " " + city.getY() + " " + city.getOwner().getName() + " " + city.getCurrentProduct().getName());
//			}
			
			for (Token token : tokens) {
				paint.setColor(token.getOwner().getColor());

				int x = token.getX();
				int y = token.getY();

				// TODO this is just because otherwise radarView might cause a crash with -1, -1 units... clean up!
				if (x == -1 || y == -1 || !token.isAlive())
					continue;

				// TODO clean up if
				if (player == token.getOwner() || radarView[x][y]) {
					if ( (!(map.getTile(x, y) instanceof CityTile || token.isLoad()) || token.isCurrent()) &&  token.isAlive() && !token.loadIsCurrentToken()) {
						Bitmap d = null;		
						ColorFilter filter = new LightingColorFilter(0, token.getOwner().getColor());
						paint.setColorFilter(filter);

						if (token.isOnGuard()) {
							d = Bitmap.createScaledBitmap(tent, TS, TS, true);
						} else
							d = token.getSprite(TS);

						if (!(token instanceof Submarine) || !(token.getOwner() instanceof ComputerPlayer) || RadarService.isVisibleSubmarine(token, player)) {
							canvas.drawBitmap(d, x * TS, y * TS , paint);
//							Log.v("xxxxxxxxxxxxxxxxxxx", "draw it: " + token.getName() + " " + token.getOwner().getName() + " " + token.getX() + " " + token.getY() +" " 
//							 + token.isAlive() + " " + token.isLoad() + " " + token.getRemainingRange() + " " + map.getTile(token.getX(), token.getY()) + " " 
//									+ token.getLastVisitedCity().getX() + " " + token.getLastVisitedCity().getY() );
							
						}

						if (token.isLoaded()) {
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < token.getTokens().size(); i++) {
								sb.append(".");
							}
							canvas.drawText(sb.toString(), x * TS + 2, y * TS + TS - 4, paint); 
						}
						else if (token.isLoad())
							canvas.drawText("___", x * TS + 2, y * TS + TS - 4, paint); 

						paint.setColorFilter(null);

						if (token.isCurrent()) {
							paint.setStyle(Paint.Style.STROKE);
							paint.setStrokeWidth(2);

							if (!(token.getOwner() instanceof ComputerPlayer)) {
								if (!map.isInNewUnitIntroMode()) {
									paint.setColor(Color.argb(200, 220, 220, 220));
									int range = token.getRemainingRoundRange();
									canvas.drawRect((x - range) * TS, (y - range) * TS, (x + range +1 ) * TS, (y + range+1) * TS, paint);

									range = token.getRemainingRange();
									
									paint.setAlpha(60);
									if (!token.isLoad() && (token.getTargetX() != token.getX() || token.getTargetY() != token.getY()) && token.getTargetX() != -1) {
										canvas.drawLine(token.getX() * TS + TS/2, token.getY() * TS + TS/2, token.getTargetX() * TS + TS/2, token.getTargetY() * TS + TS/2, paint);
									}
									
									if (range < 100) {
										canvas.drawRect((x - range) * TS, (y - range) * TS, (x + range +1 ) * TS, (y + range+1) * TS, paint);

										if (token.getRemainingRange() == token.getRange() && !(token instanceof Missile)) {
											range /=2;
											paint.setAlpha(120);
											paint.setColor(Color.GREEN);
											canvas.drawRect((x - range) * TS, (y - range) * TS, (x + range +1 ) * TS, (y + range+1) * TS, paint);
											paint.setColor(token.getOwner().getColor());
										}
									} // range < 100
									paint.setAlpha(255);
								}

								if ((token.getHealth() <= 20 || token.getRemainingRange() <= 6) && !(map.getTile(x, y) instanceof CityTile) )
									paint.setColor(Color.RED);
								else if(map.isInNewUnitIntroMode())
									paint.setColor(Color.GREEN);
							} // ! ComputerPlayer
							canvas.drawCircle(x * TS + TS / 2, y * TS + TS /2, TS -3, paint);
							paint.setColor(token.getOwner().getColor());

						} // current
					}
				} /*else if (token.isAlive()) {
					// TODO remove for final version
					//				Log.v("draw", "enemy token " + token.getShorthand() + " " + token.getX() + " " + token.getY());
					//				if (!(map.getTile(x, y) instanceof CityTile) && player.isRadarVisible(x, y)) {
					canvas.drawText("" + token.getShorthand(), token.getX() * TS + 15, token.getY() * TS + (int)(TS *0.7), paint);
					//				} // endif
				} */
			}
		} // synch
		paint.setTextSize(size);
	} // drawTokens
	
} // eof
