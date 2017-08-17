package com.mdeiml.richard;

import android.database.sqlite.*;
import android.database.Cursor;
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
		String[] createScript = context.getResources().getStringArray(R.array.sql_create_database);
		for(String sql : createScript) {
			db.execSQL(sql);
		}
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void saveMatch(Match match) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues matchValues = new ContentValues();
		if(match.matchId != -1) {
			db.delete("matches", "match_id = ?", new String[] {match.matchId+""});
			matchValues.put("match_id", match.matchId);
		}
		matchValues.put("match_player1", match.player1);
		matchValues.put("match_player2", match.player2);
		matchValues.put("match_winner", match.getWinner());
		matchValues.put("match_p1", match.getP1());
		matchValues.put("match_p2", match.getP2());
		long matchId = db.insert("matches", null, matchValues);
		match.matchId = matchId;

		for(int i = 0; i < match.sets.size(); i++) {
			Match.Set set = match.sets.get(i);
			ContentValues setValues = new ContentValues();
			setValues.put("set_match", matchId);
			setValues.put("set_nr", i);
			setValues.put("set_winner", set.getWinner());
			setValues.put("set_tiebreak", set.tiebreak);
			db.insert("sets", null, setValues);
			for(int j = 0; j < set.games.size(); j++) {
				Match.Game game = set.games.get(j);
				ContentValues gameValues = new ContentValues();
				gameValues.put("game_match", matchId);
				gameValues.put("game_set", i);
				gameValues.put("game_nr", j);
				gameValues.put("game_winner", game.getWinner());
				gameValues.put("game_tiebreak", game.tiebreak);
				db.insert("games", null, gameValues);
				for(int k = 0; k < game.points.size(); k++) {
					Match.Point point = game.points.get(k);
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

	public Match loadMatch(long matchId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor matchCursor = db.query(
			"matches",
			new String[] {"match_player1", "match_player2", "match_p1", "match_p2"},
			"match_id = ?",
			new String[] {matchId+""},
			null, null, null);
		int player1Index = matchCursor.getColumnIndex("match_player1");
		int player2Index = matchCursor.getColumnIndex("match_player2");
		int p1Index = matchCursor.getColumnIndex("match_p1");
		int p2Index = matchCursor.getColumnIndex("match_p2");
		if(matchCursor.moveToNext()) {
			Match match = new Match(
				matchCursor.getString(player1Index),
				matchCursor.getString(player2Index),
				matchCursor.getDouble(p1Index),
				matchCursor.getDouble(p2Index),
				matchId
			);
			match.sets.clear();

			Cursor setCursor = db.query("sets",
				new String[] {"set_winner", "set_tiebreak"},
				"set_match = ?",
				new String[] {matchId+""},
				null, null, "set_nr ASC");
			int setWinnerIndex = setCursor.getColumnIndex("set_winner");
			int setTiebreakIndex = setCursor.getColumnIndex("set_tiebreak");

			while(setCursor.moveToNext()) {
				Match.Set set = new Match.Set((byte)setCursor.getInt(setTiebreakIndex));
				set.winner = (byte)setCursor.getInt(setWinnerIndex);
				match.sets.add(set);
			}

			Cursor gameCursor = db.query("games",
				new String[] {"game_set", "game_winner", "game_tiebreak"},
				"game_match = ?",
				new String[] {matchId+""},
				null, null, "game_set, game_nr ASC");
			int gameSetIndex = gameCursor.getColumnIndex("game_set");
			int gameWinnerIndex = gameCursor.getColumnIndex("game_winner");
			int gameTiebreakIndex = gameCursor.getColumnIndex("game_tiebreak");

			while(gameCursor.moveToNext()) {
				Match.Game game = new Match.Game((byte)gameCursor.getInt(gameTiebreakIndex));
				game.winner = (byte)gameCursor.getInt(gameWinnerIndex);
				match.sets.get(gameCursor.getInt(gameSetIndex)).games.add(game);
			}

			Cursor pointCursor = db.query("points",
				new String[] {"point_set", "point_game", "point_winner", "point_server"},
				"point_match = ?",
				new String[] {matchId+""},
				null, null, "point_set, point_game, point_nr ASC");
			int pointSetIndex = pointCursor.getColumnIndex("point_set");
			int pointGameIndex = pointCursor.getColumnIndex("point_game");
			int pointWinnerIndex = pointCursor.getColumnIndex("point_winner");
			int pointServerIndex = pointCursor.getColumnIndex("point_server");

			while(pointCursor.moveToNext()) {
				Match.Point point = new Match.Point((byte)pointCursor.getInt(pointServerIndex));
				point.winner = (byte)pointCursor.getInt(pointWinnerIndex);
				match.sets.get(pointCursor.getInt(pointSetIndex)).games.get(pointCursor.getInt(pointGameIndex)).points.add(point);
			}

			return match;
		}
		return null;
	}

}