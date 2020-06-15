package net.zehnkampf.empire.view;

import net.zehnkampf.empire.WarGame;
import net.zehnkampf.empire.domain.Player;
import net.zehnkampf.empire.domain.map.Tile;
import net.zehnkampf.empire.domain.map.WorldMap;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class WorldPreview extends View {
	
	private Paint paint;
	
	private WorldMap map;
	private View wv;
	private Player player;

	public WorldPreview(Context context, WorldMap map, View wv, Player player) {
		super(context);
		
		initialize(map, wv, player);
	}
	
	public void initialize(WorldMap map, View wv, Player player) {
		setMinimumHeight(map.getWidth() + 2);
		setMinimumWidth(map.getHeight() + 2);
		
		this.map = map;
		this.player = player;
		this.wv = wv;
		this.invalidate();
	}
	
	protected void onDraw(Canvas canvas) {
//		Log.v("WorldPreview onDraw", "do it");

		final int TS = WarGame.TILE_SIZE;
		
		paint = new Paint();
		paint.setAlpha(200);
		paint.setColor(Color.DKGRAY);
		paint.setStrokeWidth(1);

		canvas.drawRect(0, 0, map.getWidth() + 2, map.getHeight() + 2, paint);
		for (int x = 0; x < map.getWidth(); x++) {
			for (int y = 0; y < map.getHeight(); y++) {

				Tile tile = map.getTile(x, y);

				if (player.isKnowing(x, y)) {
					tile.draw(paint);
				} else
					paint.setColor(Color.BLACK);
				canvas.drawRect(x + 1, y + 1, (x + 2), (y + 2), paint);

			} // for y
		} // for x
		
		
		Rect rect = new Rect();
		wv.getDrawingRect(rect);
		
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(rect.left / TS, rect.top / TS, rect.right / TS, rect.bottom / TS, paint);
	} // onDraw

}
