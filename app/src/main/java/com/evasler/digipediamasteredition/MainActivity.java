package com.evasler.digipediamasteredition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import com.google.ads.consent.*;
import com.google.firebase.analytics.FirebaseAnalytics;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.ContentValues.TAG;

public class MainActivity extends Activity
        implements Results.OnFragmentInteractionListener, DigimonProfile.OnFragmentInteractionListener, Settings.OnFragmentInteractionListener {

    private ConsentForm form;

    private TransitionAnimators transitionAnimators;

    private List<TextView> backgroundNumbers;

    private List<LinearLayout> resultsRows;
    private List<ImageView> resultsIcons;
    private List<TextView> resultsNames;

    private List<TextView> priorForms;
    private List<TextView> nextForms;

    private List<LinearLayout> rows;

    private int width, height, statusBarHeight, start, end, total;

    private boolean backBlocked, loading, consentStageFinished, mainWindowOpened;

    public MainActivity() {}

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backBlocked = false;
        loading = false;
        consentStageFinished = !isNetworkAvailable();
        mainWindowOpened = false;
        start = 0;
        end = 0;
        total = 0;

        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        helper.checkDb();
        initializeNotification();

        initializeViewHolders();
        setScreenDimensions();
        populateNumbers();
        populateLists();
        populateFeaturedDigimon();
        bannerInitialization();
        setTypeSpinnerWidth();
        collapseAllWindows();
        populateFragments();
    }

    @Override
    protected void onResume() {
        super.onResume();

        whenOnlineCheckForConsent();
        applicationOpeningAnimation();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    void requestConsent() {

        URL privacyUrl = null;
        try {
            // TODO: Replace with your app's privacy policy URL.
            privacyUrl = new URL("https://sites.google.com/view/digipediaprivacypolicy/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }
        form = new ConsentForm.Builder(MainActivity.this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        Log.d(TAG, "Requesting Consent: onConsentFormLoaded");
                        showForm();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                        Log.d(TAG, "Requesting Consent: onConsentFormOpened");
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.d(TAG, "Requesting Consent: onConsentFormClosed");
                        if (userPrefersAdFree) {
                            // Buy or Subscribe
                            Log.d(TAG, "Requesting Consent: User prefers AdFree");
                        } else {
                            Log.d(TAG, "Requesting Consent: Requesting consent again");
                            switch (consentStatus) {
                                case PERSONALIZED:
                                    FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(true);
                                    showPersonalizedAds();
                                    break;
                                case NON_PERSONALIZED:
                                    FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(false);
                                    showNonPersonalizedAds();
                                    break;
                                case UNKNOWN:
                                    FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(false);
                                    showNonPersonalizedAds();
                                    break;
                            }

                        }
                        // Consent form was closed.
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d(TAG, "Requesting Consent: onConsentFormError. Error - " + errorDescription);
                        // Consent form error.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }

    void waitMainWindowOpened() {
        while (!mainWindowOpened) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void showPersonalizedAds() {

        new Thread(() -> {
            ConsentInformation.getInstance(this).setConsentStatus(ConsentStatus.PERSONALIZED);

            AdView adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice("C685DD0978FBE613FA47C90071980B35")
                    .build();

            consentStageFinished = true;

            waitMainWindowOpened();

            runOnUiThread(() -> adView.loadAd(adRequest));
        }).start();
    }

    private void showNonPersonalizedAds() {

        new Thread(() -> {
            ConsentInformation.getInstance(this).setConsentStatus(ConsentStatus.NON_PERSONALIZED);

            AdView adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice("C685DD0978FBE613FA47C90071980B35")
                    .addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle())
                    .build();

            consentStageFinished = true;

            waitMainWindowOpened();

            runOnUiThread(() -> adView.loadAd(adRequest));
        }).start();
    }

    public Bundle getNonPersonalizedAdsBundle() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");

        return extras;
    }

    private void showForm() {
        if (form == null) {
            Log.d(TAG, "Consent form is null");
        }
        if (form != null && !this.isFinishing()) {
            Log.d(TAG, "Showing consent form");
            form.show();
        } else {
            Log.d(TAG, "Not Showing consent form");
        }
    }

    /* ===========================FOR checkForConsent TESTING===============================

        ConsentInformation.getInstance(this).addTestDevice("C685DD0978FBE613FA47C90071980B35");

        // Geography appears as in EEA for test devices.
        ConsentInformation.getInstance(this).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        // Geography appears as not in EEA for debug devices.
        ConsentInformation.getInstance(this).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA);

        =====================================================================================*/

    void checkForConsent() {
        ConsentInformation consentInformation = ConsentInformation.getInstance(getApplicationContext());

        String[] publisherIds = {"pub-2026622836756295"};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.
                switch (consentStatus) {
                    case PERSONALIZED:
                        Log.d(TAG, "Showing Personalized ads");
                        FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(true);
                        showPersonalizedAds();
                        break;
                    case NON_PERSONALIZED:
                        Log.d(TAG, "Showing Non-Personalized ads");
                        FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(false);
                        showNonPersonalizedAds();
                        break;
                    case UNKNOWN:
                        Log.d(TAG, "Requesting Consent");
                        if (ConsentInformation.getInstance(getBaseContext())
                                .isRequestLocationInEeaOrUnknown()) {
                            requestConsent();
                        } else {
                            FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(true);
                            showPersonalizedAds();
                        }
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
            }
        });
    }

    void bannerInitialization() {

        new Thread(() -> {

            int minHeight;
            int screenHeightDp = (int) (height / getResources().getDisplayMetrics().density);

            if (screenHeightDp <= 400) {
                minHeight = (int) (32 * getResources().getDisplayMetrics().density);
            } else if (screenHeightDp <= 720) {
                minHeight = (int) (50 * getResources().getDisplayMetrics().density);
            } else {
                minHeight = (int) (90 * getResources().getDisplayMetrics().density);
            }

            AdView adView = findViewById(R.id.adView);
            runOnUiThread(() -> {
                adView.setMinimumHeight(minHeight);
            });
        }).start();

    }

    void populateNotificationCheckBox() {

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        CheckBox checkBox = findViewById(R.id.notificationCheckbox);
        checkBox.setChecked(Objects.requireNonNull(prefs.getString(getString(R.string.notification_status), "empty")).equals("enabled"));
    }

    void initializeNotification() {

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        Intent notifyIntent = new Intent(this, MyReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), FLAG_UPDATE_CURRENT, notifyIntent, FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if(calendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis())
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        if (Objects.requireNonNull(prefs.getString(getString(R.string.notification_status), "empty")).equals("empty")) {

            prefs.edit().putString(getString(R.string.notification_status), "enabled").apply();

            DailyDigimon.generateDailyDigimon(getApplicationContext());
        }
    }

    void collapseAllWindows() {
        setScale(R.id.mainWindow, 0f);
        setScale(R.id.resultsWindowContainer, 0f);
        setScale(R.id.profileWindowContainer, 0f);
        setScale(R.id.settingsWindowContainer, 0f);
    }

    void populateFeaturedDigimon() {

        String description = AppDatabase.getDatabase(getApplicationContext()).myDao().getDigimonDescription(DailyDigimon.getDailyDigimon(getApplicationContext()));
        findViewById(R.id.featuredDigimonContainer).setContentDescription(DailyDigimon.getDailyDigimon(getApplicationContext()) + ", from_main->to_profile");
        ((TextView) findViewById(R.id.dailyDigimonName)).setText(DailyDigimon.getDailyDigimon(getApplicationContext()).replace("plusSign", "+"));
        ((TextView) findViewById(R.id.dailyDigimonDescription)).setText(description);
        ((ImageView) findViewById(R.id.dailyDigimonImage)).setImageResource(getImageId(getDigimonImageName(DailyDigimon.getDailyDigimon(getApplicationContext()))));
    }

    String getDigimonImageName(String digimonName) {
        return digimonName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }

    void initializeViewHolders() {

        backgroundNumbers = new ArrayList<>();

        resultsRows = new ArrayList<>();
        resultsIcons = new ArrayList<>();
        resultsNames = new ArrayList<>();

        priorForms = new ArrayList<>();
        nextForms = new ArrayList<>();

        rows = new ArrayList<>();
    }

    void setTypeSpinnerWidth() {

        TextView test = new TextView(this);
        test.setTextSize(16);
        test.setText("Ancient Aquatic Beast Man");
        test.measure(0, 0);
        findViewById(R.id.typeSpinner).setLayoutParams(new TableRow.LayoutParams((int) (test.getMeasuredWidth() + 16 * getResources().getDisplayMetrics().density), TableRow.LayoutParams.WRAP_CONTENT));
    }

    void applicationOpeningAnimation() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        new Thread(() -> {
            transitionAnimators = new TransitionAnimators(this);
            transitionAnimators.initializeScaleInAnimator(R.id.mainWindow);
            while(!consentStageFinished) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> transitionAnimators.startScaleIn());

            while (transitionAnimators.getScaleIn().isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mainWindowOpened = true;
        }).start();
    }

    void populateFragments() {

        FragmentManager fragmentManager = getFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.resultsFragment, new Results());
        transaction.replace(R.id.profileFragment, new DigimonProfile());
        transaction.replace(R.id.settingsFragment, new Settings());
        transaction.commit();
    }

    void setScreenDimensions() {
        Display display = getWindowManager().getDefaultDisplay();

        statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y - statusBarHeight;
    }

    void populateNumbers() {

        Random rg = new Random();
        String number = "0000000000";

        LinearLayout backgroundNumbersContainer = findViewById(R.id.backgroundNumbersContainer);

        TextView tempView = new TextView(this);
        tempView.setText(number);
        tempView.setTextSize(14);
        tempView.measure(0, 0);

        int horizontalRepeats = (int) Math.ceil((double) width / tempView.getMeasuredWidth()) + 1;
        int verticalRepeats = (int) Math.ceil((double) height / tempView.getMeasuredHeight());

        ArrayList<LinearLayout> rows = new ArrayList<>();

        for (int i = 0; i < verticalRepeats; i++) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            int offset = rg.nextInt(30) * -1;

            for (int j = 0; j < horizontalRepeats; j++) {

                tempView = new TextView(this);
                tempView.setText(getTenDigitBinary());
                tempView.measure(0, 0);
                tempView.setTextSize(14);
                tempView.setTextColor(Color.parseColor("#2fac2f"));
                tempView.setSingleLine(true);
                tempView.setX(offset);
                backgroundNumbers.add(tempView);
                row.addView(backgroundNumbers.get(backgroundNumbers.size() - 1));
            }

            rows.add(row);
            backgroundNumbersContainer.addView(rows.get(rows.size() - 1));
        }

        updateNumbers(horizontalRepeats);
    }

    void updateNumbers(int horizontalRepeats) {

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                handler.postDelayed(this, 30);
                int movement = 1;
                int endOfRowX = (int) Math.floor(horizontalRepeats * backgroundNumbers.get(0).getMeasuredWidth());

                for (int i = 0; i < backgroundNumbers.size(); i++) {

                    if ((i/horizontalRepeats)%2==0) {

                        if (backgroundNumbers.get(i).getX() + backgroundNumbers.get(i).getMeasuredWidth() < 0) {

                            backgroundNumbers.get(i).setX(backgroundNumbers.get(i).getX() + endOfRowX);
                            backgroundNumbers.get(i).setText(getTenDigitBinary());
                        } else {
                            backgroundNumbers.get(i).setX(backgroundNumbers.get(i).getX() - movement);
                        }
                    } else {
                        if (backgroundNumbers.get(i).getX()  > width) {

                            backgroundNumbers.get(i).setX(backgroundNumbers.get(i).getX() - endOfRowX);
                            backgroundNumbers.get(i).setText(getTenDigitBinary());
                        } else {
                            backgroundNumbers.get(i).setX(backgroundNumbers.get(i).getX() + movement);
                        }
                    }
                }
            }
        }, 0);
    }

    String getTenDigitBinary() {

        Random rg = new Random();
        StringBuilder number = new StringBuilder(Integer.toBinaryString(rg.nextInt(1024)));

        while (number.length() < 10) {
            number.insert(0, "0");
        }

        return number.toString();
    }

    void populateLists() {
        new Thread(() -> {
            List<Attribute> attributeList = new ArrayList<>(AppDatabase.getDatabase(getApplicationContext()).myDao().getAllAttributes());
            List<String> stringList = new ArrayList<>();
            stringList.add("All");
            for (Attribute attribute : attributeList) {
                stringList.add(attribute.getDescription());
            }
            Spinner attributeSpinner = findViewById(R.id.attributeSpinner);
            ArrayAdapter<String> attributeAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner, stringList);

            runOnUiThread(() -> attributeSpinner.setAdapter(attributeAdapter));
        }).start();

        new Thread(() -> {
            List<Family> familyList = new ArrayList<>(AppDatabase.getDatabase(getApplicationContext()).myDao().getAllFamilies());
            List<String> stringList = new ArrayList<>();
            stringList.add("All");
            for (Family family : familyList) {
                stringList.add(family.getDescription());
            }
            Spinner familySpinner = findViewById(R.id.familySpinner);
            ArrayAdapter<String> familyAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner, stringList);

            runOnUiThread(() -> familySpinner.setAdapter(familyAdapter));
        }).start();

        new Thread(() -> {
            List<Level> levelList = new ArrayList<>(AppDatabase.getDatabase(getApplicationContext()).myDao().getAllLevels());
            List<String> stringList = new ArrayList<>();
            stringList.add("All");
            for (Level level : levelList) {
                stringList.add(level.getDescription());
            }
            Spinner levelSpinner = findViewById(R.id.levelSpinner);
            ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner, stringList);

            runOnUiThread(() -> levelSpinner.setAdapter(levelAdapter));
        }).start();
        new Thread(() -> {
            List<Type> typeList = new ArrayList<>(AppDatabase.getDatabase(getApplicationContext()).myDao().getAllTypes());
            List<String> stringList = new ArrayList<>();
            stringList.add("All");
            for (Type type : typeList) {
                stringList.add(type.getDescription());
            }

            Spinner typeSpinner = findViewById(R.id.typeSpinner);
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.custom_spinner, stringList);
            runOnUiThread(() -> typeSpinner.setAdapter(typeAdapter));
        }).start();

    }

    public void onTransition(View view) {   //Content Description format: "digimonName, currentWindow-> nextWindow" || "search"

        String contentDescription = view.getContentDescription().toString();

        if (contentDescription.contains("from_main->to_profile")) {
            onTransition(contentDescription, R.id.mainWindow, R.id.profileWindowContainer);
        } else if (contentDescription.contains("from_results->to_profile")) {
            onTransition(contentDescription, R.id.resultsWindowContainer, R.id.profileWindowContainer);
        } else if (contentDescription.contains("from_profile->to_profile")) {
            onTransition(contentDescription, R.id.profileWindowContainer, R.id.profileWindowContainer);
        } else if (contentDescription.equals("from_main->to_results")) {
            onTransition(contentDescription, R.id.mainWindow, R.id.resultsWindowContainer);
        } else if (contentDescription.equals("from_main->to_settings")) {
            onTransition(contentDescription, R.id.mainWindow, R.id.settingsWindowContainer);
        }
    }

    void onTransition(String transitionInfo, int layoutFromId, int layoutToId) {

        transitionAnimators.initializeScaleOutAnimator(layoutFromId);
        transitionAnimators.initializeScaleInAnimator(layoutToId);

        hideKeyboard(this);
        transitionAnimators.startScaleOut();

        if (transitionInfo.contains("from_main->to_results")) {
            generateResults();
        } else if (transitionInfo.contains("from_main->to_profile") ||
                    transitionInfo.contains("from_results->to_profile")) {
            populateDigimonProfile(transitionInfo.substring(0, transitionInfo.indexOf(",")));
        } else if (transitionInfo.contains("from_profile->to_profile")) {
            populateDigimonProfile(transitionInfo.substring(0, transitionInfo.indexOf(",")));
        } else if (transitionInfo.equals("from_results->to_main")) {
            clearResults();
        } else if (transitionInfo.equals("from_main->to_settings")) {
            populateNotificationCheckBox();
        }

        new Thread(() -> {
            while(transitionAnimators.getScaleOut().isRunning()) {
                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> {
                scrollViewsToTop();
                transitionAnimators.startScaleIn();
            });
        }).start();
    }

    void scrollViewsToTop() {

        if (findViewById(R.id.dailyDigimonDescriptionScroll) != null)
            findViewById(R.id.dailyDigimonDescriptionScroll).scrollTo(0, 0);
        if (findViewById(R.id.digimonDescriptionScroll) != null)
            findViewById(R.id.digimonDescriptionScroll).scrollTo(0, 0);
        if (findViewById(R.id.digimonAttacksScroll) != null)
            findViewById(R.id.digimonAttacksScroll).scrollTo(0, 0);
        if (findViewById(R.id.priorFormsScroll) != null)
            findViewById(R.id.priorFormsScroll).scrollTo(0, 0);
        if (findViewById(R.id.nextFormsScroll) != null)
            findViewById(R.id.nextFormsScroll).scrollTo(0, 0);
    }

    void clearFormsTables() {
        for (LinearLayout row : rows) {
            row.removeAllViews();
        }
        rows.clear();
        ((TableLayout) findViewById(R.id.priorFormsContainer)).removeAllViews();
        priorForms.clear();
        ((LinearLayout) findViewById(R.id.nextFormsContainer)).removeAllViews();
        nextForms.clear();
    }

    void clearResults() {
        new Thread(() -> {
            LinearLayout resultsContainer = findViewById(R.id.resultsContainer);
            if (resultsContainer.getChildCount() > 0) {

                while(findViewById(R.id.resultsWindowContainer).getVisibility() != View.GONE) {
                    try {
                        Thread.sleep(transitionAnimators.getScaleOut().getDuration()/2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                start = 0;
                end = 0;
                total = 0;

                runOnUiThread(() -> {
                    resultsContainer.removeAllViews();
                    ((TextView)findViewById(R.id.currentResults)).setText("");
                    ((TextView)findViewById(R.id.totalResults)).setText("No results found");
                    ((ScrollView) findViewById(R.id.resultsScroll)).fullScroll(ScrollView.FOCUS_UP);
                });
                resultsRows.clear();
                resultsIcons.clear();
                resultsNames.clear();
            }
        }).start();
    }

    void generateResults() {

        loading = true;
        disableClicksTillLoad();

        SimpleSQLiteQuery filtersQuery = new SimpleSQLiteQuery(buildSearchString());
        List<String> digimonNames = new ArrayList<>(AppDatabase.getDatabase(getApplicationContext()).myDao().getFilteredDigimon(filtersQuery));

        updateTotalResults(digimonNames.size());

        if (digimonNames.size() > 0) {

            total = digimonNames.size() - 1;
            start = 0;
            end = digimonNames.size() > 14 ? 14 : total;

            populateResultsRange(start, end, digimonNames);
        } else {
            start = -1;
            end = -1;
        }

        updateCurrentResults(start, end);
        configurePreviousNextResultButtons();
        loading = false;
    }

    void disableClicksTillLoad() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        new Thread(() -> {
            while(loading) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE));
        }).start();
    }

    void configurePreviousNextResultButtons() {

            findViewById(R.id.previousResultsButton).setEnabled(start > 0);
            findViewById(R.id.nextResultsButton).setEnabled(end != total && end > 0);
    }

    public void getPreviousResults(View view) {

        loading = true;
        disableClicksTillLoad();

        ((ScrollView) findViewById(R.id.resultsScroll)).fullScroll(ScrollView.FOCUS_UP);

        end = start - 1;
        start = start - 15;

        ((LinearLayout) findViewById(R.id.resultsContainer)).removeAllViews();
        resultsRows.clear();
        resultsIcons.clear();
        resultsNames.clear();

        SimpleSQLiteQuery filtersQuery = new SimpleSQLiteQuery(buildSearchString());
        List<String> digimonNames = new ArrayList<>(AppDatabase.getDatabase(getApplicationContext()).myDao().getFilteredDigimon(filtersQuery));

        updateCurrentResults(start, end);
        populateResultsRange(start, end, digimonNames);
        configurePreviousNextResultButtons();
        loading = false;
    }

    public void getNextResults(View view) {

        loading = true;
        disableClicksTillLoad();

        ((ScrollView) findViewById(R.id.resultsScroll)).fullScroll(ScrollView.FOCUS_UP);

        start = end + 1;
        end = end + 15 > total ? total : end + 15;

        ((LinearLayout) findViewById(R.id.resultsContainer)).removeAllViews();
        resultsRows.clear();
        resultsIcons.clear();
        resultsNames.clear();

        SimpleSQLiteQuery filtersQuery = new SimpleSQLiteQuery(buildSearchString());
        List<String> digimonNames = new ArrayList<>(AppDatabase.getDatabase(getApplicationContext()).myDao().getFilteredDigimon(filtersQuery));

        updateCurrentResults(start, end);
        populateResultsRange(start, end, digimonNames);
        configurePreviousNextResultButtons();
        loading = false;
    }

    void populateResultsRange(int start, int end, List<String> digimonNames) {

        LinearLayout resultsContainer = findViewById(R.id.resultsContainer);

        for (int i = start; i <= end; i++) {
            populateResult(digimonNames.get(i), i == end);
            resultsContainer.addView(resultsRows.get(resultsRows.size() - 1));
        }
    }

    void populateResult(String digimon, boolean isLast) {
        ImageView tempIcon = formatResultIcon(digimon);
        TextView tempName = formatResultName(digimon);

        resultsIcons.add(tempIcon);
        resultsNames.add(tempName);

        LinearLayout tempRow = formatResultRow(digimon, isLast);
        resultsRows.add(tempRow);

        resultsRows.get(resultsRows.size() - 1).addView(resultsIcons.get(resultsIcons.size() - 1));
        resultsRows.get(resultsRows.size() - 1).addView(resultsNames.get(resultsNames.size() - 1));
    }

    LinearLayout formatResultRow(String digimon, boolean isLast) {

        int bottomMargin = isLast ? (int) (10 * getResources().getDisplayMetrics().density) : 0;

        LinearLayout resultRow = new LinearLayout(this);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, (int) (10 * getResources().getDisplayMetrics().density), 0, bottomMargin);
        resultRow.setLayoutParams(rowParams);
        resultRow.setOnClickListener(this::onTransition);
        resultRow.setContentDescription(digimon + ", from_results->to_profile");

        return resultRow;
    }

    ImageView formatResultIcon(String digimon) {

        ImageView resultIcon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams((int) (78 * getResources().getDisplayMetrics().density), (int) (78 * getResources().getDisplayMetrics().density));
        resultIcon.setLayoutParams(iconParams);
        resultIcon.setBackgroundResource(getImageId("top_left_bottom_borders"));
        resultIcon.setAdjustViewBounds(true);
        resultIcon.setImageResource(getImageId(getDigimonImageName(digimon)));

        return resultIcon;
    }

    TextView formatResultName(String digimon) {

        TextView resultName = new TextView(this);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams((int) (234 * getResources().getDisplayMetrics().density), (int) (78 * getResources().getDisplayMetrics().density));
        resultName.setLayoutParams(nameParams);
        resultName.setTextSize(20);
        resultName.setTextColor(Color.WHITE);
        resultName.setGravity(Gravity.CENTER);
        resultName.setBackgroundResource(getImageId("all_borders"));
        resultName.setText(digimon.replace("plusSign", "+"));

        return  resultName;
    }

    void updateCurrentResults(int start, int end) {

        String currentResults;

        if (end < 0) {
            currentResults = "0-0";
        } else {
            currentResults = (start + 1) + "-" + (end + 1);
        }

        ((TextView) findViewById(R.id.currentResults)).setText(currentResults);
    }

    void updateTotalResults(int amount) {
        ((TextView) findViewById(R.id.totalResults)).setText("/" + amount);
    }

    public int getImageId(String imageName) {
        return this.getResources().getIdentifier("drawable/" + imageName, null, this.getPackageName());
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    String buildSearchString() {

        List<String> queryFilters = new ArrayList<>();

        String name = ((EditText) findViewById(R.id.searchName)).getText().toString().replaceAll("[^\\dA-Za-z\\-()]","");
        String attribute = ((Spinner) findViewById(R.id.attributeSpinner)).getSelectedItem().toString().replaceAll("'", "''");
        String level = ((Spinner) findViewById(R.id.levelSpinner)).getSelectedItem().toString().replaceAll("'", "''");
        String family = ((Spinner) findViewById(R.id.familySpinner)).getSelectedItem().toString().replaceAll("'", "''");
        String type = ((Spinner) findViewById(R.id.typeSpinner)).getSelectedItem().toString().replaceAll("'", "''");

        if (!name.equals(""))
            queryFilters.add("LOWER(name) LIKE LOWER('%" + name + "%')");
        if (!attribute.equals("All"))
            queryFilters.add("LOWER(attribute) = LOWER('" + attribute + "')");
        if (!level.equals("All"))
            queryFilters.add("LOWER(level) = LOWER('" + level + "')");
        if (!family.equals("All"))
            queryFilters.add("LOWER(family) LIKE LOWER('%" + family + "%')");
        if (!type.equals("All"))
            queryFilters.add("LOWER(type) = LOWER('" + type + "')");

        StringBuilder searchQuery = new StringBuilder();
        searchQuery.append("SELECT name FROM Digimon");

        if (queryFilters.size() > 0)
            searchQuery.append(" WHERE ");

        for (int i = 0; i < queryFilters.size(); i++) {

            if (i != 0)
                searchQuery.append(" AND ");

            searchQuery.append(queryFilters.get(i));
        }

        Log.d("searchQuery", searchQuery.toString());

        return searchQuery.toString();
    }

    void clearProfile() {
        ((TextView) findViewById(R.id.digimonName)).setText("");
        recreateDigimonImage();
        ((TextView) findViewById(R.id.digimonAttribute)).setText("");
        ((TextView) findViewById(R.id.digimonLevel)).setText("");
        ((TextView) findViewById(R.id.digimonFamily)).setText("");
        ((TextView) findViewById(R.id.digimonType)).setText("");
        ((TextView) findViewById(R.id.digimonDescription)).setText("");
        ((TextView) findViewById(R.id.digimonAttacks)).setText("");
        clearFormsTables();
        initializeForms();
    }

    void recreateDigimonImage() {

        ImageView old = findViewById(R.id.digimonImage);
        int id = old.getId();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        ImageView recreated = new ImageView(this);
        recreated.setId(id);
        recreated.setLayoutParams(layoutParams);
        recreated.setAdjustViewBounds(true);
        recreated.setBackgroundResource(getImageId("all_borders_profile_image"));

        ((RelativeLayout) findViewById(R.id.digimonImageContainer)).removeAllViews();
        ((RelativeLayout) findViewById(R.id.digimonImageContainer)).addView(recreated);
    }

    void populateDigimonProfile(String digimonName) {

        Digimon digimon = AppDatabase.getDatabase(getApplicationContext()).myDao().getSpecificDigimon(digimonName);

        new Thread(() -> {
            while(findViewById(R.id.profileWindowContainer).getVisibility() != View.GONE) {
                try {
                    Thread.sleep(transitionAnimators.getScaleOut().getDuration() / 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> {
                scrollViewsToTop();
                clearProfile();
                ((TextView) findViewById(R.id.digimonName)).setText(digimonName.replace("plusSign", "+"));
                ((ImageView) findViewById(R.id.digimonImage)).setImageResource(getImageId(getDigimonImageName(digimonName)));
                setupScalableDigimonImage();
                findViewById(R.id.digimonImage).setTag(getImageId(getDigimonImageName(digimonName)));
                ((TextView) findViewById(R.id.digimonAttribute)).setText(digimon.getAttribute().replace(",", "\n"));
                ((TextView) findViewById(R.id.digimonLevel)).setText(digimon.getLevel().replace(",", "\n"));
                ((TextView) findViewById(R.id.digimonFamily)).setText(digimon.getFamily().replace(",", "\n"));
                ((TextView) findViewById(R.id.digimonType)).setText(digimon.getType().replace(",", "\n"));
                ((TextView) findViewById(R.id.digimonDescription)).setText(digimon.getDescription());
                ((TextView) findViewById(R.id.digimonAttacks)).setText(getFormatedAttacks(digimon.getAttacks()), TextView.BufferType.SPANNABLE);
                populateForms(digimon.getPrior_forms(), digimon.getNext_forms());
            });
        }).start();
    }

    void initializeForms() {
        findViewById(R.id.priorFormsPanel).setScaleX(0f);
        findViewById(R.id.priorFormsPanel).setVisibility(View.INVISIBLE);
        findViewById(R.id.nextFormsPanel).setScaleX(0f);
        findViewById(R.id.nextFormsPanel).setVisibility(View.INVISIBLE);
        findViewById(R.id.digimonDescriptionContainer).setScaleX(1f);
        findViewById(R.id.digimonDescriptionContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.digimonAttacksContainer).setScaleX(1f);
        findViewById(R.id.digimonAttacksContainer).setVisibility(View.VISIBLE);

        ((Button) findViewById(R.id.formsButton)).setText("Evolution Branch");
    }

    void setScale(int viewId, float scale) {
        findViewById(viewId).setScaleX(scale);
        findViewById(viewId).setScaleY(scale);
        findViewById(viewId).setVisibility(scale > 0 ? View.VISIBLE : View.GONE);
    }

    void populateForms(String priorFormsString, String nextFormsString) {

        String[] priorFormsList = priorFormsString.split(",");
        String[] nextFormsList = nextFormsString.split(",");

        TextView temp;

        TableLayout priorFormsContainer = findViewById(R.id.priorFormsContainer);
        LinearLayout nextFormsContainer = findViewById(R.id.nextFormsContainer);

        for (int i = 0; i < priorFormsList.length; i++) {

            if (!priorFormsList[i].contains("+")) {

                temp = formatForms(priorFormsList[i]);
                priorForms.add(temp);
                priorFormsContainer.addView(priorForms.get(priorForms.size() - 1));
            } else {
                formatPlus(priorFormsList[i]);
            }
        }

        for (int i = 0; i < nextFormsList.length; i++) {

            if (!nextFormsList[i].contains("*(w/")) {
                temp = formatForms(nextFormsList[i]);
                nextForms.add(temp);
                addLatestFormView(nextFormsContainer);
            } else {
                formatWith(nextFormsList[i]);
            }
        }
    }

    void formatPlus(String priorFormInfo) {

        LinearLayout container = findViewById(R.id.priorFormsContainer);

        String[] materialsArray = priorFormInfo.split("\\+");

        int rowsStart = rows.size();

        rows.add(new LinearLayout(this));

        //nextForms.add(with);
        //rows.get(rows.size() - 1).addView(with);

        LinearLayout currentRow;
        TextView currentForm;

        TextView plus = addTextView(" + ");
        plus.measure(0, 0);

        int widthLimit = (int) (width - (10 + 6 + 5 + 4 + 4 + 5 + 6 + 10 + plus.getMeasuredWidth()) * getResources().getDisplayMetrics().density);

        for (String material : materialsArray) {
            priorForms.add(formatForms(material));

            currentRow = rows.get(rows.size() - 1);
            currentForm = priorForms.get(priorForms.size() - 1);

            currentRow.measure(0, 0);
            currentForm.measure(0, 0);

            if (currentRow.getMeasuredWidth() + currentForm.getMeasuredWidth() > widthLimit) {
                rows.add(new LinearLayout(this));
                currentRow = rows.get(rows.size() - 1);
                currentRow.measure(0, 0);
            }

            currentRow.addView(priorForms.get(priorForms.size() - 1));

            if (!currentForm.getText().equals(materialsArray[materialsArray.length - 1])) {
                priorForms.add(addTextView(" + "));
                currentRow.addView(priorForms.get(priorForms.size() - 1));
            }

        }

        for (int i = rowsStart; i < rows.size(); i++) {
            container.addView(rows.get(i));
        }
    }

    void formatWith(String nextFormInfo) {

        LinearLayout container = findViewById(R.id.nextFormsContainer);

        String nextForm = nextFormInfo.substring(0, nextFormInfo.indexOf("*(w/"));
        String materials = nextFormInfo.substring(nextFormInfo.indexOf("*(w/") + 4, nextFormInfo.length() - 1);
        String[] materialsArray = materials.split("\\|");


        int rowsStart = rows.size();

        rows.add(new LinearLayout(this));

        nextForms.add(formatForms(nextForm));
        rows.get(rows.size() - 1).addView(nextForms.get(nextForms.size() - 1));
        nextForms.add(addTextView(" (with: "));
        rows.get(rows.size() - 1).addView(nextForms.get(nextForms.size() - 1));

        LinearLayout currentRow;
        TextView currentForm;

        TextView comma = addTextView(", ");
        comma.measure(0, 0);

        int widthLimit = (int) (width - (10 + 6 + 5 + 4 + 4 + 5 + 6 + 10 + comma.getMeasuredWidth()) * getResources().getDisplayMetrics().density);

        for (String material : materialsArray) {
            nextForms.add(formatForms(material));

            currentRow = rows.get(rows.size() - 1);
            currentForm = nextForms.get(nextForms.size() - 1);

            currentRow.measure(0, 0);
            currentForm.measure(0, 0);

            if (currentRow.getMeasuredWidth() + currentForm.getMeasuredWidth() > widthLimit) {
                rows.add(new LinearLayout(this));
                currentRow = rows.get(rows.size() - 1);
                currentRow.measure(0, 0);
            }

            currentRow.addView(nextForms.get(nextForms.size() - 1));

            if (!currentForm.getText().equals(materialsArray[materialsArray.length - 1])) {
                nextForms.add(addTextView(", "));
                currentRow.addView(nextForms.get(nextForms.size() - 1));
            } else {
                nextForms.add(addTextView(")"));
                currentRow.addView(nextForms.get(nextForms.size() - 1));
            }

        }

        for (int i = rowsStart; i < rows.size(); i++) {
            container.addView(rows.get(i));
        }
    }

    TextView addTextView(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.WHITE);
        view.setTextSize(14);

        return view;
    }

    void addLatestFormView(LinearLayout container) {
        container.addView(nextForms.get(nextForms.size() - 1));
    }

    TextView formatForms(String formString) {

        Digimon digimon = AppDatabase.getDatabase(getApplicationContext()).myDao().getSpecificDigimon(formString);

        TextView form = new TextView(this);
        form.setTextSize(14);
        form.setText(formString);

        if (digimon != null) {
            form.setTextColor(Color.parseColor("#4cffff"));
            form.setContentDescription(formString + ", from_profile->to_profile");
            form.setOnClickListener(this::onTransition);
        } else {
            form.setTextColor(Color.parseColor("#ff4c4c"));
        }

        return form;
    }

    Spannable getFormatedAttacks(String attacks) {

        String cleanAttacks = attacks.replaceFirst("<", "").replaceAll(">", "\n");
        cleanAttacks = cleanAttacks.replaceAll("<", "\n");
        Spannable spannable = new SpannableString(cleanAttacks);

        Pattern pattern = Pattern.compile("[^<]*<");
        Matcher matcher = pattern.matcher(attacks);
        int count = 0;
        while (matcher.find())
            count++;

        for (int i = 0; i < count; i++) {
            int start = attacks.indexOf("<") + i;
            int end = attacks.indexOf(">") + i;

            attacks = attacks.replaceFirst("<", "").replaceFirst(">", "\n");

            spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#f7b81e")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }

    public void setBackBlocked(boolean backBlocked) {
        this.backBlocked = backBlocked;
    }

    @Override
    public void onBackPressed() {

        if (backBlocked) {
            return;
        }

        if (findViewById(R.id.scalableDigimonImage).getContentDescription().equals("scaled")) {

            imageScale(findViewById(R.id.scalableDigimonImage));
        } else if (findViewById(R.id.profileWindowContainer).getVisibility() == View.VISIBLE
                && resultsRows.size() > 0) {

            onTransition("from_profile->to_results", R.id.profileWindowContainer, R.id.resultsWindowContainer);
        } else if (findViewById(R.id.profileWindowContainer).getVisibility() == View.VISIBLE
                && resultsRows.size() == 0){

            onTransition("from_profile->to_main", R.id.profileWindowContainer, R.id.mainWindow);
        } else if (findViewById(R.id.resultsWindowContainer).getVisibility() == View.VISIBLE) {

            onTransition("from_results->to_main", R.id.resultsWindowContainer, R.id.mainWindow);
        } else if (findViewById(R.id.settingsWindowContainer).getVisibility() == View.VISIBLE) {

            onTransition("from_settings->to_main", R.id.settingsWindowContainer, R.id.mainWindow);
        } else {
            transitionAnimators.initializeScaleOutAnimator(R.id.mainWindow);
            transitionAnimators.startScaleOut();

            new Thread(() -> {
                while (transitionAnimators.getScaleOut().isRunning()) {
                    try {
                        Thread.sleep(transitionAnimators.getScaleOut().getDuration() / 5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(super::onBackPressed);
            }).start();
        }
    }

    boolean animationsExecuting() {

        boolean scaleInExecuting = transitionAnimators.getScaleIn().isRunning();
        boolean scaleOutExecuting = transitionAnimators.getScaleOut().isRunning();
        boolean scaleDigimonImageExecution = transitionAnimators.getScaleDigimonImage().isRunning();
        return scaleInExecuting || scaleOutExecuting || scaleDigimonImageExecution;
    }

    void setupScalableDigimonImage() {

        new Thread(() -> {
            ImageView baseDigimonImage = findViewById(R.id.digimonImage);
            ImageView scalableDigimonImage = findViewById(R.id.scalableDigimonImage);
            runOnUiThread(() -> {
                scalableDigimonImage.getLayoutParams().width = baseDigimonImage.getWidth();
                scalableDigimonImage.getLayoutParams().height = baseDigimonImage.getHeight();
                scalableDigimonImage.requestLayout();
                baseDigimonImage.setVisibility(View.GONE);
                RelativeLayout profileContainer = findViewById(R.id.profileContainer);
                RelativeLayout digimonImageContainer = findViewById(R.id.digimonImageContainer);
                digimonImageContainer.measure(0,0);
                profileContainer.measure(0,0);

                scalableDigimonImage.setX((10 + 8 + 5) * getResources().getDisplayMetrics().density + ((digimonImageContainer.getWidth() - baseDigimonImage.getWidth()) >> 1));
                scalableDigimonImage.setY((float) ((10 + 6 + 2.5) * getResources().getDisplayMetrics().density + profileContainer.getHeight() + ((digimonImageContainer.getHeight() - baseDigimonImage.getHeight()) >> 1)));

                scalableDigimonImage.setImageResource((Integer) baseDigimonImage.getTag());
            });
        }).start();

    }

    public void formsAnimation(View view) {
        TransitionAnimators transitionAnimators1 = new TransitionAnimators(this);
        TransitionAnimators transitionAnimators2 = new TransitionAnimators(this);

        boolean formsVisible = findViewById(R.id.nextFormsPanel).getScaleX() == 1f;

        int scaleOutViewId1 = formsVisible ? R.id.priorFormsPanel : R.id.digimonDescriptionContainer;
        int scaleOutViewId2 = formsVisible ? R.id.nextFormsPanel : R.id.digimonAttacksContainer;
        int scaleInViewId1 = formsVisible ? R.id.digimonDescriptionContainer : R.id.priorFormsPanel;
        int scaleInViewId2 = formsVisible ? R.id.digimonAttacksContainer : R.id.nextFormsPanel;

        transitionAnimators1.initializeScaleOutLeftAnimator(scaleOutViewId1);
        transitionAnimators1.startScaleOutLeft();
        transitionAnimators2.initializeScaleOutLeftAnimator(scaleOutViewId2);
        transitionAnimators2.startScaleOutLeft();

        new Thread(() -> {
            while(transitionAnimators1.getScaleOutLeft().isRunning() || transitionAnimators2.getScaleOutLeft().isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> {
                transitionAnimators1.initializeScaleInRightAnimator(scaleInViewId1);
                transitionAnimators1.startScaleInRight();
                transitionAnimators2.initializeScaleInRightAnimator(scaleInViewId2);
                transitionAnimators2.startScaleInRight();
            });
        }).start();

        Button formsButton = findViewById(R.id.formsButton);
        formsButton.setText(formsButton.getText().equals("Evolution Branch") ? "Information" : "Evolution Branch");
    }

    public void imageScale(View view) {

        transitionAnimators.initializeScaleDigimonImageAnimator();
        transitionAnimators.startScaleDigimonImage();
    }

    public void enableDisableNotification(View view) {

        SharedPreferences prefs = getSharedPreferences(getString(R.string.preferences), Context.MODE_PRIVATE);

        if (((CheckBox) view).isChecked()) {
            prefs.edit().putString(getString(R.string.notification_status), "enabled").apply();
        } else {
            prefs.edit().putString(getString(R.string.notification_status), "disabled").apply();
        }
    }

    void whenOnlineCheckForConsent() {

        if (!isNetworkAvailable()) {
            new Thread(() -> {
                while(!isNetworkAvailable()) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isNetworkAvailable()) {
                        checkForConsent();
                    }
                }
            }).start();
        } else {
            checkForConsent();
        }
    }

    public void openFandomWiki(View view) {
        visitURL("https://digimon.fandom.com/wiki/Digimon_Wiki");
    }

    public void openWikimon(View view) {
        visitURL("https://wikimon.net/Main_Page");
    }

    public void openPrivacy(View view) {
        visitURL("https://sites.google.com/view/digipediaprivacypolicy/");
    }

    public void visitURL(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
