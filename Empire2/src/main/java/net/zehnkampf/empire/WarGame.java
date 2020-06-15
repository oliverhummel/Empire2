package net.zehnkampf.empire;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import net.zehnkampf.empire.domain.ComputerPlayer;
import net.zehnkampf.empire.domain.Fighting;
import net.zehnkampf.empire.domain.MapPosition;
import net.zehnkampf.empire.domain.Player;
import net.zehnkampf.empire.domain.map.CityTile;
import net.zehnkampf.empire.domain.map.RadarService;
import net.zehnkampf.empire.domain.map.Tile;
import net.zehnkampf.empire.domain.map.WorldMap;
import net.zehnkampf.empire.domain.tokens.Army;
import net.zehnkampf.empire.domain.tokens.ImpossibleMoveException;
import net.zehnkampf.empire.domain.tokens.Missile;
import net.zehnkampf.empire.domain.tokens.Radar;
import net.zehnkampf.empire.domain.tokens.Submarine;
import net.zehnkampf.empire.domain.tokens.TankToken;
import net.zehnkampf.empire.domain.tokens.Token;
import net.zehnkampf.empire.domain.tokens.WaitForLoadException;
import net.zehnkampf.empire.view.ScrollViewListener;
import net.zehnkampf.empire.view.TwoDScrollView;
import net.zehnkampf.empire.view.WorldPreview;
import net.zehnkampf.empire.view.WorldView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

public class WarGame extends Activity implements OnTouchListener, ScrollViewListener {

	public static int TILE_SIZE = 40; 
	public static WorldMap map = null;

	private int currentTokenNumber = 0;
	private Token currentToken;
	private int round;
	private volatile boolean waitOnUI = false;

	private WorldView wv;
	private WorldPreview wp;
	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Token> tokens = new ArrayList<Token>();
	private ArrayList<Token> newTokens;
	private Player human;

	private GestureDetector gd;
	private ScaleGestureDetector sd;

	private TwoDScrollView sv;
	private RelativeLayout rl1, rl2;

	private int screenWidth, screenHeight;

	private static Vibrator vibrator = null;
	private final String filename = "/EmpireGame.ser";
	private boolean saveGame = true;

	//	private boolean doubleTapWorkAround = false;

	// ------------------------------------------


	@Override
	protected void onStop() {
	    super.onStop();
	    
	    if (saveGame)
			saveGame();
	}
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		vibrator = (Vibrator)getSystemService("vibrator");

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		TILE_SIZE = dm.densityDpi / 6;
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		sv = new TwoDScrollView(this);
		sv.setHorizontalScrollBarEnabled(true);
		sv.setVerticalScrollBarEnabled(true);
		sv.setScrollViewListener(this);

		Token.view = sv;
		if (!loadGame())
			setupGame();

		wv = new WorldView(this, players.get(0), tokens, map.getCities());
		wp = new WorldPreview(this, map, sv, players.get(0));

		rl1 = new RelativeLayout(this);
		rl2 = new RelativeLayout(this);

		//		wv.getLayoutParams(). setGravity(Gravity.CENTER_HORIZONTAL); // TODO Center this...
		rl2.addView(wv);

		sv.addView(rl2);
		rl1.addView(sv);
		rl1.addView(wp);

		wv.setOnTouchListener(this);
		gd = new GestureDetector(this, new GestureListener());
		sd = new ScaleGestureDetector(this, new ScaleListener());

		setContentView(rl1);

		updateTitle(currentToken);
		showPosition(currentToken);

		if (currentToken.getOwner() != human)
			nextToken();
	} // onCreate

	private void setupGame() {
		Log.v("WarGame setupGame", "hier");

		map = new WorldMap(100, 100);
		map.createCities();

		synchronized(WarGame.map) {
			players.clear();
			tokens.clear();

			Player player = new Player("Human", true, map.generateVisibilityMap());
			player.setColor(Color.LTGRAY);
			players.add(player);

			player = new ComputerPlayer("Computer1", false, map.generateVisibilityMap());
			player.setColor(Color.YELLOW);
			players.add(player);

			player = new ComputerPlayer("Computer2", false, map.generateVisibilityMap());
			player.setColor(Color.DKGRAY);
			players.add(player);

			player = new ComputerPlayer("Computer3", false, map.generateVisibilityMap());
			player.setColor(Color.MAGENTA);
			players.add(player);

			player = new ComputerPlayer("Computer4", false, map.generateVisibilityMap());
			player.setColor(Color.CYAN);
			players.add(player);

			for (int i = 0; i < players.size(); i++) {
				CityTile city = map.getCity(i);
				city.setOwner(players.get(i));

				Radar radar = new Radar(city.getX(), city.getY());
				radar.setOwner(players.get(i));
				radar.setAlive(true);
				radar.setLastVisitedCity(city);
				tokens.add(radar);

				//												TransporterPlane transporter = new TransporterPlane(city.getX(), city.getY());
				//												transporter.setOwner(players.get(i));
				//												transporter.setAlive(true);
				//												transporter.setLastVisitedCity(city);
				//												tokens.add(transporter);

				//			Carrier carrier = new Carrier(city.getX(), city.getY());
				//			carrier.setOwner(players.get(i));
				//			carrier.setAlive(true);
				//			tokens.add(carrier);

				//								Fighter fighter = new Fighter(city.getX(), city.getY());
				//								fighter.setOwner(players.get(i));
				//								fighter.setAlive(true);
				//								fighter.setLastVisitedCity(city);
				//								tokens.add(fighter);

				//				AWACS awacs = new AWACS(city.getX(), city.getY());
				//				awacs.setOwner(players.get(i));
				//				awacs.setAlive(true);
				//				awacs.setLastVisitedCity(city);
				//				tokens.add(awacs);

				Army army = new Army(city.getX(), city.getY());
				army.setOwner(players.get(i));
				army.setAlive(true);
				army.setLastVisitedCity(city);
				tokens.add(army);

				//			Battleship battleship = new Battleship(city.getX(), city.getY());
				//			battleship.setOwner((Player)players.get(i));
				//			tokens.add(battleship);

				//				Submarine battle = new Submarine(city.getX(), city.getY());
				//				battle.setOwner(players.get(i));
				//				battle.setAlive(true);
				//				battle.setLastVisitedCity(city);
				//				tokens.add(battle);

				//				Missile missile = new Missile(city.getX(), city.getY());
				//				missile.setOwner(players.get(i));
				//				missile.setAlive(true);
				//				missile.setLastVisitedCity(city);
				//				tokens.add(missile);

				//			Destroyer destr = new Destroyer(city.getX(), city.getY());
				//			destr.setOwner(players.get(i));
				//			destr.setAlive(true);
				//			tokens.add(destr);
				//
				//			Helicopter heli = new Helicopter(city.getX(), city.getY());
				//			heli.setOwner(players.get(i));
				//			heli.setAlive(true);
				//			tokens.add(heli);

				// TODO remove later
				//				if (i != 0) {
				//					for (int j = 0; j < 42; j ++) {
				//						int x = (int)(Math.random() * 100);
				//						int y = (int)(Math.random() * 100);
				//
				//						if (map.getTile(x, y) instanceof LandTile && j % 4 != 0) {
				//							army = new Army(x, y);
				//							army.setOwner(players.get(i));
				//							army.setAlive(true);
				//							army.setLastVisitedCity(city);
				//							tokens.add(army);
				//						}
				//					}
				//				}

			}  // creating player tokens
		}

		round = 1;
		currentTokenNumber = 0;
		newTokens = (ArrayList<Token>)tokens.clone();

		setupBasics();

	} // setupGame

	private void setupBasics() {
		human = players.get(0);

		currentToken = tokens.get(currentTokenNumber);
		currentToken.setCurrent(true);
	}

	private void showToken(final Token token) {
		Log.v("WarGame showToken", "showToken " + token + " " + token.getOwner());

		// TODO added as precaution, is it necessary?
		if (human.isRadarVisible(token)) {
			token.setCurrent(true);
			showPosition(token);

			updateTitle(token);
			wv.invalidate();
		}
	}

	private void showPosition(final MapPosition token) {
		sv.post(new Runnable() {
			@Override
			public void run() {
				int x = token.getX() * TILE_SIZE;
				int y = token.getY() * TILE_SIZE;

				Log.v("WarGame showPosition token", x + " " + y);
				//Log.v("WarGame showPosition view", sv.getScrollX() + " xxx " + sv.getScrollY() + " " + screenWidth + " " + screenHeight);

				if ( !(x > sv.getScrollX() && y > sv.getScrollY() && x <sv.getScrollX() + screenWidth - TILE_SIZE && y < sv.getScrollY() + screenHeight - 120)) {
					x = Math.max(token.getX() - screenWidth / TILE_SIZE / 2, 0) * TILE_SIZE;
					y = Math.max(token.getY() - screenHeight / TILE_SIZE / 2, 0) * TILE_SIZE;
					sv.scrollTo(x, y);
				}
			} 
		});
	}

	private void updateView() {
		Log.v("WarGame", "updateView");
		updateTitle(currentToken);
		wv.invalidate();
		wp.invalidate();
	}

	private void updateTitle(Token token) {
		if (token.getHealth() > 0)
			if (token.getOwner() == human)
				setTitle(token.getName() + ": " + token.getRemainingRoundRange() + "|" + token.getRemainingRange() + " - H: "
						+ token.getHealth() + "% (R:" + round + ")" );
			else
				setTitle(token.getOwner().getName() + "'s " + token.getName() + " (R:" + round + ")" );
	}

	// ===========================================

	private void jumpToNextToken(int millis) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				nextToken();
			}
		}, millis);
	}

	private void nextToken()  {
		Thread t = new Thread() {
			public void run() {
				wv.setOnTouchListener(null);

				boolean loadContinue; // if token is load and can't move...
				do {

					while (waitOnUI) {
						try {
							Thread.sleep(500);
							Log.v("dumdidum", " waitonUI " + waitOnUI);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					Log.v("next token", "entry");
					loadContinue = false;
					currentToken.setCurrent(false);
					currentTokenNumber++;

					if(currentTokenNumber >= tokens.size() || currentTokenNumber < 0)
						nextRound();

					currentToken = tokens.get(currentTokenNumber);
					// just as a precaution
					if (currentToken.getRemainingRange() <= 0 || !currentToken.isAlive()) {
						deleteToken(currentToken);
						continue;
					}

					Log.v("WarGame current token", "" + currentToken.getName() + " " + currentToken.getRemainingRoundRange() + " " 
																								+ currentToken.getOwner().getName());
					if (currentToken.isOnGuard() && currentToken.getOwner() == human) {
						//						Log.v("WarGame next token", "on guard: continue");
						continue;
					}
					if (currentToken.isLoad() && !currentToken.canMoveAnywhere()) {
						Log.v("WarGame next token", "is load: continue");
						loadContinue = true;
						continue;
					}

					boolean showIt = false;
					if (human.isRadarVisible(currentToken) && currentToken.getRemainingRoundRange() > 0
							&& !(currentToken instanceof Submarine) || RadarService.isVisibleSubmarine(currentToken, human) || currentToken.getOwner() == human) {
						//												Log.v("WarGame moveToken", "radar visible: " + currentToken.toString());
						showIt = true;
						runOnUiThread(new Runnable() {
							public void run() {
								showToken(currentToken);
								wv.invalidate();
							}
						});

						try {
							int sleep = (currentToken.getOwner() == human) ? 1000 : 1500;
							Thread.sleep(sleep);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// move it
					if (!currentToken.hasReachedTarget() && currentToken.getRemainingRoundRange() > 0 || currentToken.getOwner() != human) {
						if (currentToken.getOwner() != human) {
							Log.v("WarGame nextToken", "computer move...");
							((ComputerPlayer)currentToken.getOwner()).moveToken(currentToken);
						}

						try {
							Log.v("auto mooooooooooove it", "auto here and now");
							moveToken(currentToken);
						}
						catch(ImpossibleMoveException ime) {
							currentToken.setTarget(-1, -1);
							Log.v("auto move", "auto exception");
							ime.printStackTrace();
						} catch(WaitForLoadException wfle) {
							Log.v("auto move", "wait for load exception " + currentToken.getOwner() + " " + currentToken.getName());
							moveTokenToEndOfList(currentToken);
						}
					}

					if (showIt) {
						runOnUiThread(new Runnable() {
							public void run() {
								showToken(currentToken);
								wv.invalidate();
							}
						});

						try {
							int sleep = (currentToken.getOwner() == human) ? 600 : 1200;
							Thread.sleep(sleep);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} // visible

					if (currentToken.getOwner() != human) {
						currentToken.setRemainingRange(currentToken.getRemainingRange() - currentToken.getRemainingRoundRange());
						checkForLostToken(currentToken);
					}
						

				} while(currentToken.getOwner() != human || currentToken.isOnGuard() || loadContinue || currentToken.getRemainingRoundRange() == 0 || !currentToken.isAlive());
				
				wv.setOnTouchListener(WarGame.this);

			} // run
		};
		t.start();

	}  // nextToken

	private void checkGameOver() {
		String message = null;
		if (human.getCityCount() == map.getCities().size() && human.getTokenCount() == tokens.size()) {
			message = "Congratulations!\n"
					+ "You have won the battle and achieved nothing less than world domination!\n\n"
					+ "We sincerely hope you enjoyed this game, but in reality - as an old Japanese proverb says - it's the generals who prevail and the soldiers who die...";
		} else if (human.getCityCount() == 0 && human.getTokenCount() == 0) {
			message = "Outch...\n"
					+ "That did not go too well: Game Over, you have lost all your cities and units." 
					+ "But you might want to try again and fail better this time.";
		}

		if (message != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(WarGame.this);
			builder.setTitle("Finally...");
			builder.setMessage(message);
			builder.setPositiveButton("OK", null);
			showDialog(builder);
		}
	}

	private void nextRound() {
		round++;
		Log.v("WarGame next round", "" + round);
		toastIt("Starting round number " + round);

		//		for (Token token : tokens) {
		//			token.newRound();
		//		}

		synchronized(WarGame.map) {
			tokens = new ArrayList<Token>();
			// bring loaded Units to the end of the list
			for (Token token : newTokens) {
				if (!token.isAlive())
					continue;
				token.newRound();
				if (!token.isLoad()) 
					tokens.add(token);	
			}
			for (Token token : newTokens) {
				if (!token.isAlive())
					continue;
				if (token.isLoad()) 
					tokens.add(token);	
			}

			//			tokens = newTokens;
			newTokens = (ArrayList<Token>)tokens.clone();
			wv.setTokens(tokens);
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean newTokenToastShown = false;
		for (CityTile city : map.getCities()) {
			final Token token = city.nextRound();
			Player owner = city.getOwner();

			if (null != token && owner != null) {
				synchronized(WarGame.map) {
					token.setAlive(true);
					tokens.add(token);
					newTokens.add(token);
				}

				if (owner == human) {
					waitOnUI = true;
					map.setNewUnitIntroMode(true);
					runOnUiThread(new Runnable() {
						public void run() {
							showToken(token);
						}
					});

					if (!newTokenToastShown) {
						toastIt("New unit(s) produced.");
						newTokenToastShown = true;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					waitOnUI = false;
					token.setCurrent(false);
				} else {
					city.setProduct(((ComputerPlayer)owner).choseProduction(city, false));
				}
			}
		}

		map.setNewUnitIntroMode(false);

		Log.v("tokens length", "" + tokens.size());
		currentTokenNumber = 0;
		checkGameOver();
	} // nextRound

	private void moveToken(final Token token) throws ImpossibleMoveException, WaitForLoadException {
		while (!(!token.mustMove() || !token.hasRangeLeft() || token.getTargetX() == -1)) {

			if (!token.move()) { // attacking something
				Tile tile = map.getTile(token.getTargetX(), token.getTargetY());
				if (tile instanceof CityTile) {
					Log.v("moveToken", "attack city with " + token.getName() + " " + token.getOwner());
					attackCity(token, (CityTile)tile);
				} else {
					attackToken(token, tile);
				}	
				//break;
			}  // attacking
		} // while
		Log.v("WarGame: end of moveToken", "" + map.getTile(token.getX(), token.getY()).getToken());

		if (human == token.getOwner()) {
			runOnUiThread(new Runnable() {
				public void run() {
					updateView();
					showPosition(token);
				}
			});
		}

		checkForLostToken(token);
	}

	private void checkForLostToken(Token token) {
		// check whether token ran out of fuel and needs to be removed

		synchronized(WarGame.map) {
			Tile tile = map.getTile(token.getX(), token.getY());
			Token transporter = tile.getToken();
			if (token.getRemainingRange() == 0 && newTokens.contains(token) && !(tile instanceof CityTile) 
					&& !(transporter != null && transporter.canLoad(token, token.getX(), token.getY()) && transporter.getOwner() == token.getOwner())) {  
				deleteToken(token);

				if (human == token.getOwner()) {
					toastIt(token.getName() + " lost!");
					vibrator.vibrate(100);
					tile.setShowBoom(true);
				}

				Log.v("WarGame checkforlosttoken", token.getName() + " of " + token.getOwner().getName() + " lost.");
			}
		} // synch
	}

	private void deleteToken(Token token) {
		synchronized(WarGame.map) {
			token.setRemainingRange(0);
			token.setAlive(false);

			if (token.isLoaded()) {
				for (Token load : token.getTokens()) {
					// deleteToken(load); This will cause an exception!
					load.setRemainingRange(0);
					load.setAlive(false);
					newTokens.remove(load);
					if (token.getOwner() != null)
						token.getOwner().decreaseTokenCount();
					//										map.getTile(load.getX(), load.getY()).removeToken(load);
				}
			}
			newTokens.remove(token);
			map.getTile(token.getX(), token.getY()).removeToken(token);

			if (token.getOwner() != null)
				token.getOwner().decreaseTokenCount();

			Log.v("WarGame deleteToken", "@ " + token.getX() + " " + token.getY());
		} // synch
	} // deleteToken

	private void showProduction(final CityTile city) {
		AlertDialog.Builder builder = new AlertDialog.Builder(WarGame.this);
		builder.setTitle("City's Production (H:" + city.getHealth() + ")");
		builder.setSingleChoiceItems(city.getProductOverview(), city.getCurrentProduct().getProductId(), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int k) {
				if (k != city.getCurrentProduct().getProductId())
					city.setProduct(k);
				Toast.makeText(WarGame.this, "City is producing a " + city.getProductOverview()[k], Toast.LENGTH_SHORT).show();
			}
		});
		showDialog(builder);
	}

	private byte fighting(Fighting attacker, Fighting defender) {
		// TODO: this should be in the UI methods...
		if (human.equals(attacker.getOwner()) || human.equals(defender.getOwner()) )
			vibrator.vibrate(new long[] {0,  20, 10, 20, 10, 200, 10, 200}, -1);

		byte result = 0;

		// does not matter, if one is totally destroyed
		attacker.decreaseHealth((int)(Math.random() * 10 * (10 - (attacker.getAttackStrength() - defender.getDefenseStrength()))) );
		defender.decreaseHealth((int)(Math.random() * 10 * (10 - (defender.getDefenseStrength() - attacker.getAttackStrength()))) );

		int balanceOfPower = ((int)(Math.random() * attacker.getAttackStrength() * attacker.getHealth()))
				- ((int)(Math.random() * defender.getDefenseStrength() * defender.getHealth())); 

		if (balanceOfPower <= -33) {
			result = -1;
		} else if (balanceOfPower >= 33) {
			result = 1;
		}

		if (defender.getHealth() < 0 && result != 1)
			defender.setHealth(1);

		if (attacker.getHealth() < 0 && result != -1)
			attacker.setHealth(1);

		if (result == 0 && attacker instanceof Missile)
			result = -1;

		Log.v("WarGame fighting, balance of power", result + " " + balanceOfPower + " " + attacker.getHealth() + " " + defender.getHealth());
		return result;
	}

	private void attackToken(final Token attacker, Tile tile)  {
		final Token defender = tile.getToken();
		Log.v("WarGame moveToken attack", attacker + " vs. " + defender);
		if (defender != null) {
			if (human.isRadarVisible(defender) && !(attacker instanceof Missile)) {
				map.getTile(defender.getX(), defender.getY()).setShowBoom(true);
				map.getTile(attacker.getX(), attacker.getY()).setShowBoom(true);
			}

			int result = fighting(attacker, defender);
			final String message;

			if (result == 0) {
				message = attacker.getOwner().getName() + "'s " + attacker.getName() + " was defended.";
				//				message += "\nHealth of " + attacker.getName() + ": " + attacker.getHealth() + " %.";
				attacker.setTarget(attacker.getX(), attacker.getY());
			} else if (result == 1) {
				message = defender.getOwner().getName() + "'s " + defender.getName() + " was destroyed.";
				deleteToken(defender);

				if (!(attacker instanceof Missile) && attacker.canMoveOnTile(attacker.getTargetX(), attacker.getTargetY(), 0, 0)) {
					synchronized(WarGame.map) {
						map.getTile(attacker.getX(), attacker.getY()).removeToken(attacker);
						attacker.setX(attacker.getTargetX());
						attacker.setY(attacker.getTargetY());
						map.getTile(attacker.getX(), attacker.getY()).addToken(attacker);
					}
					//					if (attacker.getOwner() == human)
					//						message += "\nHealth of " + attacker.getName() + ": " + attacker.getHealth() + " %.";
				} else if (attacker instanceof Missile) {
					//					message = "Missile detonated.\n" + message;
					map.getTile(defender.getX(), defender.getY()).setShowExplosion(true);
					deleteToken(attacker);
				}
			} else {
				message = attacker.getOwner().getName() + "'s " + attacker.getName() + " was destroyed.";
				deleteToken(attacker);
			}

			runOnUiThread(new Runnable() {
				public void run() {
					if (attacker.getOwner() == human || defender.getOwner() == human) {
						Toast.makeText(WarGame.this, message, Toast.LENGTH_SHORT).show();
						showPosition(attacker);
					}
					wv.invalidate();
				}
			});

		}
	} // attack token

	private void attackCity(final Token attacker, final CityTile city) {
		String message = null;
		city.setUnderAttack(round, attacker.getAttackStrength());

		if (human.isRadarVisible(city) && !(attacker instanceof Missile)) {
			map.getTile(city.getX(), city.getY()).setShowBoom(true);
			map.getTile(attacker.getX(), attacker.getY()).setShowBoom(true);
		}

		if (attacker instanceof Missile) {
			if (human.equals(attacker.getOwner()) || human.equals(city.getOwner()) )
				vibrator.vibrate(new long[] {0,  20, 10, 20, 10, 200, 10, 200}, -1);

			deleteToken(attacker);
			message = "Missile detonated.";
			city.setShowExplosion(true);

			if ((int)(Math.random() * 100) % 7 != 0) {
				city.decreaseHealth((int)(Math.random() * (10 + 2 * attacker.getAttackStrength()) ));
			}
		}
		else {
			byte result = fighting(attacker, city);
			Log.v("WarGame attackCity fight result", "" + result);

			if (result == -1 || attacker.getHealth() <= 0) {
				deleteToken(attacker);
				//				message = attacker.getName() + " was destroyed.";
			} else  {
				if (attacker instanceof TankToken && result == 1) {
					city.setOwner(attacker.getOwner());

					synchronized(WarGame.map) {
						map.getTile(attacker.getX(), attacker.getY()).removeToken(attacker);
						map.getTile(city.getX(), city.getY()).addToken(attacker);
						attacker.setX(city.getX());
						attacker.setY(city.getY());
						message = "City was conquered.";
					}

					if (attacker.isLoad())
						attacker.setLoad(false);

					if (human == attacker.getOwner()) {
						runOnUiThread(new Runnable() {
							public void run() {
								showProduction(city);
							}
						});
					}
					else if (attacker.getOwner() instanceof ComputerPlayer) 
						((ComputerPlayer)attacker.getOwner()).choseProduction(city, true);
				}
				else {
					attacker.setTarget(attacker.getX(), attacker.getY());
				}
			} 

		} // no missile

		//		if ((city.getOwner() == human || attacker.getOwner() == human) && message != null) {
		//			runOnUiThread(new Runnable() {
		//				public void run() {
		//					showToken(attacker, false);
		//				}
		//			});
		//			toastIt(message);
		//		}

		Log.v("WarGame attackCity end", message + " health: " + city.getHealth());
	} // attackCity

	private void moveTokenToEndOfList(Token token) {
		synchronized(WarGame.map) {
			tokens.remove(token);
			tokens.add(token);

			currentTokenNumber--;
		}
	}

	// -------------------------------------------

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 42, 0, "Find current Unit");
		menu.add(0, 45, 0, "Send Unit on Guard Duty");
		menu.add(0, 43, 0, "Status Report");
		menu.add(0, 41, 0, "Zoom Level (" + (int)(TILE_SIZE / 8) + ")");
		menu.add(0, 0, 0, "Help");
		menu.add(0, 46, 0, "Abandon Unit");
		menu.add(0, 4711, 0, "Save Game");
		menu.add(0, 44, 0, "New Game");
		return true;
	} 

	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(45);
		if (currentToken.canFly() || currentToken.canSwim() || map.getTile(currentToken.getX(), currentToken.getY()) instanceof CityTile || currentToken.isLoad())
			item.setEnabled(false);
		else
			item.setEnabled(true);

		item = menu.findItem(41);
		item.setTitle("Zoom Level (" + (int)(TILE_SIZE / 8) + ")");

		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected (MenuItem item){
		switch (item.getItemId()){
		case 42:
			showPosition(currentToken);	
			return true;

		case 43:
			showStatusReport();	
			return true;

		case 45:
			Toast.makeText(WarGame.this, currentToken.getName() + " sent on guard duty.", Toast.LENGTH_SHORT).show();
			currentToken.setOnGuard(true);
			wv.invalidate();
			showPosition(currentToken);

			jumpToNextToken(1000);
			return true;

		case 46:
			Toast.makeText(WarGame.this, currentToken.getName() + " abandoned.", Toast.LENGTH_SHORT).show();
			deleteToken(currentToken);
			wv.invalidate();
			showPosition(currentToken);

			jumpToNextToken(1000);
			return true;

		case 4711:
			saveGame();
			Toast.makeText(WarGame.this, "Game saved.", Toast.LENGTH_SHORT).show();
			return true;

		case 44: 
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						setupGame();
						wv.initialize(human, newTokens, map.getCities()); //= new WorldView(this, human, tokens, map.getCities());
						wp.initialize(map, sv, human);

						Toast.makeText(WarGame.this, "New game initialized.", Toast.LENGTH_SHORT).show();
						showPosition(currentToken);
						updateTitle(currentToken);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						Toast.makeText(WarGame.this, "Game continues...", Toast.LENGTH_SHORT).show();
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do your really want to end this game?").setPositiveButton("Yes, end it!", dialogClickListener).setNegativeButton("No, continue!", dialogClickListener);
			showDialog(builder);
			return true;

		case 0:
			showInfoDialog();
			return true;

		case 41:
			final AlertDialog.Builder alert = new AlertDialog.Builder(this); 
			alert.setTitle("Zoom Level"); 
			alert.setMessage("Slide to Change zoom."); 

			final SeekBar seek = new SeekBar(this); 
			seek.setMax(8);
			seek.setProgress((int)(TILE_SIZE / 8) - 2);
			alert.setView(seek); 

			alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() { 
				public void onClick(DialogInterface dialog,int id) { 
					TILE_SIZE = seek.getProgress() * 8 + 16;
					wv.setZoom(TILE_SIZE);
					wp.invalidate();
					showPosition(currentToken);
					Log.v("WarGame manual zoom", "" + TILE_SIZE);				
				} 
			}); 
			alert.setNegativeButton("Cancel", null);
			showDialog(alert);

		}
		return true;
	}

	// -------------------------------------------

	@Override
	public boolean onTouch(View v, MotionEvent me) {
		super.onTouchEvent(me);

		boolean res = gd.onTouchEvent(me);
		if (!res)
			res = sd.onTouchEvent(me);

		return res;
	}

	@Override
	public void onScrollChanged(TwoDScrollView scrollView, int x, int y, int oldx, int oldy) {
		wp.invalidate();
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			Log.v("x factor", "" + detector.getScaleFactor());

			if (detector.getScaleFactor() < 1 && TILE_SIZE > 16)
				TILE_SIZE -= 8;

			if (detector.getScaleFactor() > 1 && TILE_SIZE < 80)
				TILE_SIZE += 8;

			Log.v("WarGame zoom TS", "" + TILE_SIZE);
			wv.setZoom(TILE_SIZE);

			wp.invalidate();
			showPosition(currentToken);

			return true;
		}
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		public void onLongPress(final MotionEvent me) {
			//			if (doubleTapWorkAround) {
			//				doubleTapWorkAround = false;
			//				return;
			//			}

			final int x = (int)(me.getX() / WarGame.TILE_SIZE);
			final int y  = (int)(me.getY() / WarGame.TILE_SIZE);

			Log.v("LongPress", (new StringBuilder("Tapped at: (")).append(x).append(",").append(y).append(")").toString());
			Log.v("map field = ", "" + map.getTile(x, y).getToken());

			if (x != currentToken.getX() || y != currentToken.getY()) {
				WarGame.vibrator.vibrate(50L);
				try {
					currentToken.setTarget(x, y);
					moveToken(currentToken);
				} catch(ImpossibleMoveException ime) {
					WarGame.vibrator.vibrate(400);	
				} catch(WaitForLoadException wfle) {
					// should never occur here...
				}

				if(!currentToken.hasRangeLeft()) {
					Log.v("long press", "neeeeext toooooken");
					jumpToNextToken(1000);	
					updateView();
				}
			} // endif
		}

		public boolean onSingleTapConfirmed(MotionEvent me) {
			Log.v("single tab", "wait");

			if (currentToken != tokens.get(tokens.size() - 1)) 
				Toast.makeText(WarGame.this, currentToken.getName() + " skipped for now.", Toast.LENGTH_SHORT).show();
			else 
				Toast.makeText(WarGame.this, currentToken.getName() + " is the last unit for this round.", Toast.LENGTH_SHORT).show();

			moveTokenToEndOfList(currentToken);
			jumpToNextToken(1000);

			return true;
		}

		public boolean onDoubleTap(MotionEvent me) {
			//			doubleTapWorkAround = true;

			int x = (int) me.getX() / TILE_SIZE;
			int  y = (int) me.getY() / TILE_SIZE;
			Tile tile = map.getTile((int)x, (int)y);

			Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");

			// TODO: remove, as is for dev only...
			//			if (tile.getToken() != null)
			//				Log.v("WarGame double tap", "unit health: " + map.getTile(x, y).getToken().getHealth() );

			// city production
			if (tile instanceof CityTile && human.isKnowing(tile.getX(), tile.getY())) {
				final CityTile city = (CityTile)tile;
				Log.v("WarGame double tap", "city health: " + city.getHealth() + " producing: " + city.getCurrentProduct());

				Player owner = city.getOwner();
				if (human.equals(owner))
					showProduction(city);
				else 
					Toast.makeText(WarGame.this, city.getInfo(), Toast.LENGTH_SHORT).show();
			} // if

			else if (tile.getToken() != null) {
				Token token = tile.getToken();
				String message = token.getName() + " of " + token.getOwner().getName() + " @(" + token.getX() + "," + token.getY() +")";

				if (token.getOwner() == human) {
					message += "\nhealth: " + token.getHealth() + " %";
					message += "\nrange: " + token.getRemainingRange() + " squares";
					message += "\nmileage: " + token.getMileage() + " squares";

					if (token.isOnGuard()) {
						token.setOnGuard(false);
						message += "\n\nGuard duty ended.";
						wv.invalidate();
					} else {
						token.setTarget(token.getX(), token.getY());
						message += "\n\nOrders deleted.";
					}
				}
				else
					Log.v("Token info:" ,  token.getHealth() + "% - " + token.getRemainingRange() + " squares" );

				Toast.makeText(WarGame.this, message, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(WarGame.this, currentToken.getName() + " not moved.", Toast.LENGTH_SHORT).show();
				if (!(map.getTile(currentToken.getX(), currentToken.getY()) instanceof CityTile) ) {
					currentToken.setRemainingRange(currentToken.getRemainingRange() - currentToken.getRemainingRoundRange());
					checkForLostToken(currentToken);
				}
				jumpToNextToken(1000);
			}

			return true;
		}
	} // GestureListener

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		//		TILE_SIZE = dm.densityDpi / 6;
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		showPosition(currentToken);

		Log.v("WarGame onConfigurationChanged", "width: " + screenWidth + " height: " + screenHeight);
	}

	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle("Exit?")
		.setMessage("Do your really want to quit?")
		.setNeutralButton("No, continue game", null)
		//		.setNegativeButton("Yes, quit and do NOT save!", new OnClickListener() {
		//			public void onClick(DialogInterface arg0, int arg1) {
		//				saveGame = false;
		//				WarGame.super.onBackPressed();
		//			}
		//		})
		.setPositiveButton("Yes, save and quit!", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				WarGame.super.onBackPressed();
			}
		});
		showDialog(builder);
	}

	// ----------------------

	private void showStatusReport() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Empire: Status Report for " + human.getName());
		builder.setMessage(
				"Rounds played: " + (round-1)
				//	+ "\n  ->" + currentTokenNumber + " out of " + human.getTokenCount() + " Units played already this round." 
				+ "\nWorld explored: " + (human.getWorldExplored()) + " %"
				+ "\nUnits in Combat: " + human.getTokenCount()
				+ "\nCities: " + human.getCityCount()
				);
		builder.setPositiveButton("Ok", null);
		builder.create().show();
	}

	private void showInfoDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Empire (beta)");
		builder.setMessage("Welcome to this remake of a classic strategy game - explore and conquer your world!\n"
				+ "\nblue squares = sea\n"
				+ "green squares = land\n"
				+ "red squares = cities.\n\n"
				+ "Black areas in the map need to be explored, darker areas are currently not covered by radar.\n\n"

				+ "Cities can be conquered by armies. Make a long tap on the map to move a unit there, a short tap to let it wait; double"
				+ "tap somewhere in the map to not move a unit in the current round.\n\n"

				+ "Available units:\n"
				+ "Army (A)\n"
				+ "Battleship (B << H & 2 x M)\n"
				+ "Destroyer (D << H)\n"
				+ "Carrier (C << 10 x F & 3 x H)\n"
				+ "Fighter Plane (F)\n"
				+ "Combat Helicopter (H)\n"
				+ "Missile (M)\n"
				+ "Mobile Radar (R)\n"
				+ "Submarine (S << 2 x M)\n"
				+ "Transporter Ship (T << 5 x A & R)\n"

				+ "\nSome units can load other units as shown above. A missile can only be loaded on ships next to its initial position."
				+ " The same applies for armies to be loaded on a plane. They must be in a city. Once a missile is started from a ship or a city, it cannot land again.\n"

				+ "\nDouble tap a unit for info or a city to chose unit to be produced. A double tap on a unit also deletes its orders (guard duty or movement orders).\n"
				+ "Units a have range per round and a total range, as well as a health level (in percent). The white resp. grey squares around a unit indicate its range in the round "
				+ "and its remaining total range resp. the distance from which it can safely return to the current point (marked in green). \n"

				+ "\nA word on tactics, when you want to conquer a city, try it in a focused attack with a number of units, since cities can regenerate after an attack as well.\n"

				+ "\nPlease be aware that this is still a beta version, although it is fully playable the computer players are still somewhat dumb...\n"

				+ "\nApp (c) 2011-2013 by oliver@zehnkampf.net");
		builder.setPositiveButton("Ok", null);
		showDialog(builder);
	}

	private void saveGame() {
		Log.v("saveGame", "try it");

		waitOnUI = true;
		synchronized(WarGame.map) {
			FileOutputStream fos = null;
			ObjectOutputStream o = null;
			try {
				File file = new File(this.getFilesDir().getPath().toString() + filename);
				Log.v("file", file.getAbsolutePath());
				fos = new FileOutputStream(file);
				//			fos = openFileOutput(file, Context.MODE_PRIVATE);
				o = new ObjectOutputStream(fos); 
				o.writeInt(round);
				o.writeInt(currentTokenNumber);
				o.writeInt(TILE_SIZE);
				o.writeObject(players);
				o.writeObject(tokens);
				o.writeObject(newTokens);
				o.writeObject(map);
				o.flush();
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(WarGame.this, "Empire: Problem while saving game: " + e.toString(), Toast.LENGTH_LONG).show();
			} 
			finally { 
				try { 
					Log.v("saveGame", "finally successful");
					Toast.makeText(WarGame.this, "Empire: Game successfully saved!", Toast.LENGTH_LONG).show();
					o.close();
					fos.close(); 
				} catch (Exception e) { 
					e.printStackTrace();
					Toast.makeText(WarGame.this, "Empire: Problem while saving game: " + e.toString(), Toast.LENGTH_LONG).show();
				} 
			}
		} // synch
		waitOnUI = false;
	} // save

	private boolean loadGame() {
		Log.v("loadGame", "try it...");	
		FileInputStream fis = null; 
		try { 
			File file = new File(this.getFilesDir().getPath().toString() + filename);
			fis = new FileInputStream(file); 
			ObjectInputStream o = new ObjectInputStream(fis);
			round = o.readInt();
			currentTokenNumber = o.readInt();
			TILE_SIZE = o.readInt();
			players = (ArrayList<Player>) o.readObject();
			tokens = (ArrayList<Token>) o.readObject();
			newTokens = (ArrayList<Token>) o.readObject();
			map = (WorldMap) o.readObject();

			fis.close();
			setupBasics();
			Log.v("loadGame", "successful");
			return true;
		} 
		catch (IOException e) { 
			e.printStackTrace(); 
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace(); 
		}

		showInfoDialog();

		Log.v("loadGame", "not successful");
		return false;

	} // loadGame

	private void showDialog(AlertDialog.Builder builder) {
		AlertDialog ad = builder.create();
		ad.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				waitOnUI = false;
			}
		});
		ad.show();
		waitOnUI = true;
	}

	private void toastIt(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(WarGame.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

} // eof
