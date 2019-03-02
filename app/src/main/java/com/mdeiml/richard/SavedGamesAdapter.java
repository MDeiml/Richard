package com.mdeiml.richard;

import android.graphics.Typeface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.TableRow;
import android.util.TypedValue;
import java.util.Locale;

public class SavedGamesAdapter extends CursorAdapter {
    
    private final int idIndex;
    private final int player1Index;
    private final int player2Index;
    private final int winnerIndex;
    private SavedMatchesDbHelper dbHelper;
    private int runningHeaderIndex;
    private int overHeaderIndex;

    public SavedGamesAdapter(Context context, SavedMatchesDbHelper dbHelper) {
        super(context, createCursor(dbHelper.getReadableDatabase(), context), false);
        this.idIndex = getCursor().getColumnIndexOrThrow("_id");
        this.player1Index = getCursor().getColumnIndexOrThrow("match_player1");
        this.player2Index = getCursor().getColumnIndexOrThrow("match_player2");
        this.winnerIndex = getCursor().getColumnIndexOrThrow("match_winner");
        this.dbHelper = dbHelper;
        this.runningHeaderIndex = -1;
        this.overHeaderIndex = -1;
    }

    private static Cursor createCursor(SQLiteDatabase db, Context context) {
        return db.rawQuery(context.getString(R.string.sql_saved_matches), new String[0]);
    }

    public int getViewTypeCount() {
        return 2;
    }

    public int getItemViewType(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        int winner = cursor.getInt(winnerIndex);
        if (position == runningHeaderIndex || position == overHeaderIndex) {
            return 0;
        } else if (winner == -1) {
            runningHeaderIndex = position;
            return 0;
        } else if (winner == 1) {
            overHeaderIndex = position;
            return 0;
        } else {
            return 1;
        }
    }

    public void bindView(View view, Context context, Cursor cursor) {
        int winner = cursor.getInt(winnerIndex);
        if (winner == -1) {
            ((TextView) view).setText(context.getString(R.string.game_running_heading));
        } else if (winner == 1) {
            ((TextView) view).setText(context.getString(R.string.game_over_heading));
        } else {
            final long matchId = cursor.getLong(idIndex);
            final int position = cursor.getPosition();
            TextView sgPlayer1 = (TextView)view.findViewById(R.id.saved_game_player1);
            TextView sgPlayer2 = (TextView)view.findViewById(R.id.saved_game_player2);
            TextView sgPoints1 = view.findViewById(R.id.saved_game_points1);
            TextView sgPoints2 = view.findViewById(R.id.saved_game_points2);
            TableRow sgSets1 = (TableRow)view.findViewById(R.id.saved_game_sets1);
            TableRow sgSets2 = (TableRow)view.findViewById(R.id.saved_game_sets2);

            Match match = dbHelper.loadMatch(matchId);
            sgPlayer1.setText(match.player1);
            sgPlayer2.setText(match.player2);

            int numSets = match.getCurrentSetNr()+1;
            byte[][] games = match.getGames();
            for (int i = 0; i < numSets; i++) {
                TextView tv0 = (TextView) sgSets1.getChildAt(i + 1);
                tv0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                tv0.setText(String.format(Locale.getDefault(), "%d", games[i][0]));
                tv0.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                tv0.setVisibility(View.VISIBLE);
                
                TextView tv1 = (TextView) sgSets2.getChildAt(i + 1);
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                tv1.setText(String.format(Locale.getDefault(), "%d", games[i][1]));
                tv1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                tv1.setVisibility(View.VISIBLE);
            }

            for (int i = numSets; i < 3; i++) {
                sgSets1.getChildAt(i + 1).setVisibility(View.INVISIBLE);
                sgSets2.getChildAt(i + 1).setVisibility(View.INVISIBLE);
            }

            String[] scores = match.getCurrentSet().getCurrentGame().stringScores();
            if(scores[0] != "0" || scores[1] != "0") {
                sgPoints1.setText(scores[0]);
                sgPoints2.setText(scores[1]);
            }
        }
    }
    
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int winner = cursor.getInt(winnerIndex);
        if (winner == -1 || winner == 1) {
            TextView heading = new TextView(context);
            heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            heading.setTextColor(0xff707070);
            heading.setTypeface(null, Typeface.BOLD);
            return heading;
        } else {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.saved_game, parent, false);
        }
    }
    
    public void updateCursor(Context context) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = createCursor(db, context);
        changeCursor(cursor);
        runningHeaderIndex = -1;
        overHeaderIndex = -1;
    }

    public boolean isHeader(int position) {
        return position != -1 && (position == runningHeaderIndex || position == overHeaderIndex);
    }
}
