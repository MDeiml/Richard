package com.mdeiml.richard;

import android.database.sqlite.*;
import android.content.Context;
import android.content.ContentValues;
import java.util.ArrayList;

public class SavedMatchesDbHelper extends SQLiteOpenHelper {

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "SavedMatches.db";

	private Context context;

	public SavedMatchesDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(context.getResources().getString(R.string.sql_create_database));
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void saveMatch(History h) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues matchValues = new ContentValues();
		matchValues.put("match_player1", h.player1);
		matchValues.put("match_player2", h.player2);
		matchValues.put("match_winner", h.getWinner());
		matchValues.put("match_p1", h.getP1());
		matchValues.put("match_p2", h.getP2());
		long matchId = db.insert("matches", null, matchValues);

		for(int i = 0; i < h.sets.size(); i++) {
			History.Set set = h.sets.get(i);
			ContentValues setValues = new ContentValues();
			setValues.put("set_match", matchId);
			setValues.put("set_nr", i);
			setValues.put("set_winner", set.getWinner());
			setValues.put("set_isTiebreak", set.tiebreak);
			db.insert("sets", null, setValues);
			for(int j = 0; j < set.games.size(); j++) {
				History.Game game = set.games.get(j);
				ContentValues gameValues = new ContentValues();
				gameValues.put("game_match", matchId);
				gameValues.put("game_set", i);
				gameValues.put("game_nr", j);
				gameValues.put("game_winner", game.getWinner());
				gameValues.put("game_isTiebreak", game.tiebreak);
				db.insert("games", null, gameValues);
				for(int k = 0; k < game.points.size(); k++) {
					History.Point point = game.points.get(k);
					ContentValues pointValues = new ContentValues();
					pointValues.put("point_match", matchId);
					pointValues.put("point_set", i);
					pointValues.put("point_game", j);
					pointValues.put("point_nr", k);
					pointValues.put("point_winner", point.winner);
					pointValues.put("point_server", point.server);
					db.insert("points", null, pointValues);
				}
			}
		}
	}

}