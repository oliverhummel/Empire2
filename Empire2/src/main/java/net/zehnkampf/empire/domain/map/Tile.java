package net.zehnkampf.empire.domain.map;

import java.io.Serializable;

import net.zehnkampf.empire.domain.MapPosition;
import net.zehnkampf.empire.domain.Player;
import net.zehnkampf.empire.domain.tokens.Token;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public abstract class Tile implements MapPosition, Serializable {

	final protected int x, y;
	protected Token token;
	private boolean showExplosion, showBoom;

	protected int color = Color.BLUE;

	public Tile(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void draw(Paint paint) {
		paint.setColor(color);
	}

	public void addToken(Token token) {
		Log.v("add Token at ", this.x + " " + this.y);
		this.token = token;
	}

	// parameter required for CityTile
	public void removeToken(Token token) {
		Log.v("remove Token at ", this.x + " " + this.y + " " + token.getName() + " " + token.isLoad());

		if (!token.isLoad())
			this.token = null;
		else {
			if (this.token != null)
				this.token.removeToken(token);
		} // else
	}

	public Token getToken() {
		return token;
	}

	public boolean hasToken() {
		return token != null;	
	}

	public abstract Player getOwner();

	public abstract String getType();

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isShowExplosion() {
		return showExplosion;
	}

	public void setShowExplosion(boolean showExplosion) {
		this.showExplosion = showExplosion;
	}

	public boolean isShowBoom() {
		return showBoom;
	}

	public void setShowBoom(boolean showBoom) {
		this.showBoom = showBoom;
	}

} // eof
