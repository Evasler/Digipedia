package com.evasler.digipediamasteredition;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Random;

public class DailyDigimon {

    DailyDigimon() { }

    static void generateDailyDigimon(Context context) {
        List<String> digimonNames = AppDatabase.getDatabase(context).myDao().getAllDigimonNames();

        Random rand = new Random();
        int dailyDigimonIndex = rand.nextInt(digimonNames.size());

        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);
        prefs.edit().putString(context.getString(R.string.daily_digimon), digimonNames.get(dailyDigimonIndex)).apply();
    }

    public static String getDailyDigimon(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);
        return prefs.getString(context.getString(R.string.daily_digimon), "");
    }
}
