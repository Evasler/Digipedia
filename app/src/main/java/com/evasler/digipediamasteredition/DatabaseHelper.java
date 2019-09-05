package com.evasler.digipediamasteredition;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class DatabaseHelper {

    private Context context;

    private static final String DB_NAME = "DigipediaMasterEdition.db";
    private static final int dbVersion = 0;

    DatabaseHelper(Context context) {
        setContext(context);
    }

    private void setContext(Context context) {
        this.context = context;
    }

    void checkDb() {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);

        if (prefs.getInt("dbVersion", -1) < dbVersion) {
            transferDatabase();
            prefs.edit().putInt("dbVersion", dbVersion).apply();
        }
    }

    private void transferDatabase() {

        final File dbPath = context.getDatabasePath(DB_NAME);

        if (!dbPath.getParentFile().exists()) {
            dbPath.getParentFile().mkdirs();
        } else if(dbPath.exists()) {
            deleteDbFile();
        }

        try {
            final InputStream inputStream = context.getAssets().open(DB_NAME);
            final OutputStream outputStream = new FileOutputStream(dbPath);

            byte[] buffer = new byte[2048];
            int length;

            while ((length = inputStream.read(buffer, 0, 2048)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteDbFile() {

        final File dbPath = context.getDatabasePath(DB_NAME);

        if (dbPath.exists()) {
            dbPath.delete();
        }
    }
}
