package com.mdeiml.richard

import android.database.sqlite.*
import android.database.Cursor
import android.content.Context
import android.content.ContentValues
import android.util.Log

class SavedMatchesDbHelper(context: Context) : SQLiteOpenHelper(context, SavedMatchesDbHelper.DATABASE_NAME, null, SavedMatchesDbHelper.DATABASE_VERSION) {

    val context = context

    companion object {
        val DATABASE_VERSION = 3
        val DATABASE_NAME = "SavedMatches.db"
    }

	override fun onCreate(db: SQLiteDatabase) {
		val createScript = context.resources.getStringArray(R.array.sql_create_database)
		for (sql in createScript) {
			db.execSQL(sql)
		}
	}

    // TODO
	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		val deleteScript = context.resources.getStringArray(R.array.sql_delete_database)
		for (sql in deleteScript) {
			db.execSQL(sql)
		}
        onCreate(db)
	}

	override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
	}

	fun saveMatch(match: Match) {
		val db = getWritableDatabase()
		val matchValues = ContentValues()
        val id = match.matchId
		if (id != null) {
            deleteMatch(id)
			matchValues.put("match_id", id)
		}
        matchValues.put("match_start", match.startTime)
        matchValues.put("match_numsets", match.numSets)
        matchValues.put("match_matchtiebreak", match.matchTiebreak)
		matchValues.put("match_player1", match.player1)
		matchValues.put("match_player2", match.player2)
		matchValues.put("match_winner", match.winner?.toInt() ?: 0)
		matchValues.put("match_p1", match.p1)
		matchValues.put("match_p2", match.p2)
		val matchId = db.insert("matches", null, matchValues)
		match.matchId = matchId

		for ((i, set) in match.sets.withIndex()) {
			val setValues = ContentValues()
			setValues.put("set_match", matchId)
			setValues.put("set_nr", i)
			setValues.put("set_winner", set.winner?.toInt() ?: 0)
			setValues.put("set_tiebreak", set.tiebreak)
			db.insert("sets", null, setValues)
			for ((j, game) in set.games.withIndex()) {
                Log.i("SavedMatchesDbHelper", "$j, ${game.winner}")
				val gameValues = ContentValues()
				gameValues.put("game_match", matchId)
				gameValues.put("game_set", i)
				gameValues.put("game_nr", j)
				gameValues.put("game_winner", game.winner?.toInt() ?: 0)
				gameValues.put("game_tiebreak", game.tiebreak.ordinal)
				db.insert("games", null, gameValues)
				for ((k, point) in game.points.withIndex()) {
					val pointValues = ContentValues()
					pointValues.put("point_match", matchId)
					pointValues.put("point_set", i)
					pointValues.put("point_game", j)
					pointValues.put("point_nr", k)
					pointValues.put("point_winner", point.winner?.toInt() ?: 0)
					pointValues.put("point_server", point.server.toInt())
					pointValues.put("point_winprob", point.winProb)
					pointValues.put("point_importance", point.importance)
					db.insert("points", null, pointValues)
				}
			}
		}
	}
    
    fun deleteMatch(matchId: Long) {
        val db = getWritableDatabase()
        db.delete("matches", "match_id = ?", arrayOf(matchId.toString()))
        db.delete("sets", "set_match = ?", arrayOf(matchId.toString()))
        db.delete("games", "game_match = ?", arrayOf(matchId.toString()))
        db.delete("points", "point_match = ?", arrayOf(matchId.toString()))
    }

	fun loadMatch(matchId: Long): Match? {
		val db = getReadableDatabase()
		val matchCursor = db.query(
			"matches",
			arrayOf("match_player1", "match_player2", "match_p1", "match_p2", "match_start", "match_numsets", "match_matchtiebreak"),
			"match_id = ?",
			arrayOf(matchId.toString()),
			null, null, null)
		val player1Index = matchCursor.getColumnIndex("match_player1")
		val player2Index = matchCursor.getColumnIndex("match_player2")
		val p1Index = matchCursor.getColumnIndex("match_p1")
		val p2Index = matchCursor.getColumnIndex("match_p2")
		val startIndex = matchCursor.getColumnIndex("match_p2")
        val numSetsIndex = matchCursor.getColumnIndex("match_numsets")
        val matchTiebreakIndex = matchCursor.getColumnIndex("match_matchtiebreak")
		if(matchCursor.moveToNext()) {
			val match = Match(
				matchCursor.getString(player1Index),
				matchCursor.getString(player2Index),
				matchCursor.getDouble(p1Index),
				matchCursor.getDouble(p2Index),
                matchCursor.getLong(startIndex),
                matchCursor.getInt(numSetsIndex),
                matchCursor.getInt(matchTiebreakIndex) != 0,
				matchId
			)
            matchCursor.close()
			match.sets.clear()

			val setCursor = db.query("sets",
				arrayOf("set_winner", "set_tiebreak"),
				"set_match = ?",
				arrayOf(matchId.toString()),
				null, null, "set_nr ASC")
			val setWinnerIndex = setCursor.getColumnIndex("set_winner")
			val setTiebreakIndex = setCursor.getColumnIndex("set_tiebreak")

			while (setCursor.moveToNext()) {
				val set = Match.Set(match.matrixSet, setCursor.getInt(setTiebreakIndex) != 0)
				set.winner = setCursor.getInt(setWinnerIndex).toPlayer()
				match.sets.add(set)
			}
            setCursor.close()

			val gameCursor = db.query("games",
				arrayOf("game_set", "game_winner", "game_tiebreak"),
				"game_match = ?",
				arrayOf(matchId.toString()),
				null, null, "game_set, game_nr ASC")
			val gameSetIndex = gameCursor.getColumnIndex("game_set")
			val gameWinnerIndex = gameCursor.getColumnIndex("game_winner")
			val gameTiebreakIndex = gameCursor.getColumnIndex("game_tiebreak")

			while (gameCursor.moveToNext()) {
				val game = Match.Game(match.matrixSet, Match.Tiebreak.values()[gameCursor.getInt(gameTiebreakIndex)])
				game.winner = gameCursor.getInt(gameWinnerIndex).toPlayer()
				match.sets[gameCursor.getInt(gameSetIndex)].games.add(game)
			}
            gameCursor.close()

			val pointCursor = db.query("points",
				arrayOf("point_set", "point_game", "point_winner", "point_server", "point_winprob", "point_importance"),
				"point_match = ?",
				arrayOf(matchId.toString()),
				null, null, "point_set, point_game, point_nr ASC")
			val pointSetIndex = pointCursor.getColumnIndex("point_set")
			val pointGameIndex = pointCursor.getColumnIndex("point_game")
			val pointWinnerIndex = pointCursor.getColumnIndex("point_winner")
			val pointServerIndex = pointCursor.getColumnIndex("point_server")
			val pointWinprobIndex = pointCursor.getColumnIndex("point_winprob")
			val pointImportanceIndex = pointCursor.getColumnIndex("point_importance")

			while (pointCursor.moveToNext()) {
				val point = Match.Point(pointCursor.getInt(pointServerIndex).toPlayer()!!)
				point.winner = pointCursor.getInt(pointWinnerIndex).toPlayer()
				point.winProb = pointCursor.getDouble(pointWinprobIndex).toFloat()
				point.importance = pointCursor.getDouble(pointImportanceIndex).toFloat()
				match.sets[pointCursor.getInt(pointSetIndex)].games[pointCursor.getInt(pointGameIndex)].points.add(point)
			}
            pointCursor.close()

			return match
		}
        matchCursor.close()
		return null
	}

}
