package com.mdeiml.richard;

import android.content.ContentProvider;
import android.content.UriMatcher;
import android.net.Uri;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;


public class MatchProvider extends ContentProvider {
    
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int MATCHES = 1;
    private static final int SETS = 3;
    private static final int GAMES = 5;
    private static final int POINTS = 7;
    private static final int MATCHES_OVERVIEW = 8;

    private SavedMatchesDbHelper dbHelper;

    static {
        uriMatcher.addURI("com.mdeiml.richard.provider", "matches", MATCHES);
        uriMatcher.addURI("com.mdeiml.richard.provider", "matches/#/sets", SETS);
        uriMatcher.addURI("com.mdeiml.richard.provider", "matches/#/sets/#/games", GAMES);
        uriMatcher.addURI("com.mdeiml.richard.provider", "matches/#/sets/#/games/#/points", POINTS);
        uriMatcher.addURI("com.mdeiml.richard.provider", "matches_overview", MATCHES_OVERVIEW);
    }

    public boolean onCreate() {
        dbHelper = new SavedMatchesDbHelper(getContext());
        return true;
    }
    
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = null;
        List<String> pathSegments = uri.getPathSegments();

        switch(uriMatcher.match(uri)) {
            case MATCHES:
                table = "matches";
                break;
            case SETS:
                table = "sets";
                selection = selection + " set_match = " + pathSegments.get(1);
                break;
            case GAMES:
                table = "games";
                selection = selection + " game_match = " + pathSegments.get(1) + " game_set = " + pathSegments.get(3);
                break;
            case POINTS:
                table = "points";
                selection = selection + " point_match = " + pathSegments.get(1) + " point_set = " + pathSegments.get(3) + " point_game = " + pathSegments.get(5);
                break;
        }

        return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String table = "";
        List<String> pathSegments = uri.getPathSegments();

        switch(uriMatcher.match(uri)) {
            case MATCHES:
                table = "matches";
                break;
            case SETS:
                table = "sets";
                values.put("set_match", Long.parseLong(pathSegments.get(1)));
                break;
            case GAMES:
                table = "games";
                values.put("game_match", Long.parseLong(pathSegments.get(1)));
                values.put("game_set", Long.parseLong(pathSegments.get(3)));
                break;
            case POINTS:
                table = "points";
                values.put("point_match", Long.parseLong(pathSegments.get(1)));
                values.put("point_set", Long.parseLong(pathSegments.get(3)));
                values.put("point_game", Long.parseLong(pathSegments.get(5)));
                break;
        }

        db.insertOrThrow(table, null, values);

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String table = null;
        List<String> pathSegments = uri.getPathSegments();

        switch(uriMatcher.match(uri)) {
            case MATCHES:
                table = "matches";
                break;
            case SETS:
                table = "sets";
                selection = selection + " set_match = " + pathSegments.get(1);
                break;
            case GAMES:
                table = "games";
                selection = selection + " game_match = " + pathSegments.get(1) + " game_set = " + pathSegments.get(3);
                break;
            case POINTS:
                table = "points";
                selection = selection + " point_match = " + pathSegments.get(1) + " point_set = " + pathSegments.get(3) + " point_game = " + pathSegments.get(5);
                break;
        }

        return db.update(table, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String table = null;
        List<String> pathSegments = uri.getPathSegments();

        switch(uriMatcher.match(uri)) {
            case MATCHES:
                table = "matches";
                break;
            case SETS:
                table = "sets";
                selection = selection + " set_match = " + pathSegments.get(1);
                break;
            case GAMES:
                table = "games";
                selection = selection + " game_match = " + pathSegments.get(1) + " game_set = " + pathSegments.get(3);
                break;
            case POINTS:
                table = "points";
                selection = selection + " point_match = " + pathSegments.get(1) + " point_set = " + pathSegments.get(3) + " point_game = " + pathSegments.get(5);
                break;
        }

        return db.delete(table, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        switch(uriMatcher.match(uri)) {
            case MATCHES:
                return "vnd.android.cursor.dir/vnd.com.mdeiml.provider.match";
            case SETS:
                return "vnd.android.cursor.dir/vnd.com.mdeiml.provider.set";
            case GAMES:
                return "vnd.android.cursor.dir/vnd.com.mdeiml.provider.game";
            case POINTS:
                return "vnd.android.cursor.dir/vnd.com.mdeiml.provider.point";
            default:
                return null;
        }
    }

}
