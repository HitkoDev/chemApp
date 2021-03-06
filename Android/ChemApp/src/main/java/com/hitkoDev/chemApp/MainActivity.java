package com.hitkoDev.chemApp;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.hitkoDev.chemApp.data.Level;
import com.hitkoDev.chemApp.rest.LoadDataTask;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.hitkoDev.chemApp.fragment.ExamFragment;
import com.hitkoDev.chemApp.fragment.ExerciseFragment;
import com.hitkoDev.chemApp.fragment.HelpFragment;
import com.hitkoDev.chemApp.fragment.LessonsFragment;
import com.hitkoDev.chemApp.fragment.SectionsFragment;
import com.hitkoDev.chemApp.fragment.SettingsFragment;
import com.hitkoDev.chemApp.fragment.SettingsFragment.PreferenceAttachedListener;
import com.hitkoDev.chemApp.fragment.SplashFragment;
import com.hitkoDev.chemApp.helper.ExerciseProgressInterface;
import com.hitkoDev.chemApp.helper.FragmentActionsListener;
import com.hitkoDev.chemApp.helper.SyncObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author hitno
 */
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ExerciseProgressInterface,
        PreferenceAttachedListener,
        FragmentActionsListener {

    private final static int LESSONS = 1;
    private final static int EXERCISES = 2;
    private final static int EXAM = 3;

    private final static String FRAGMENT_EXAM = "ChemApp.Exam";
    private final static String FRAGMENT_EXERCISE = "ChemApp.Exercise";
    private final static String FRAGMENT_LESSONS = "ChemApp.Lessons";
    private final static String FRAGMENT_SECTIONS = "ChemApp.Sections";
    private final static String FRAGMENT_SPLASH = "ChemApp.Splash";
    private final static String FRAGMENT_SETTINGS = "ChemApp.Settings";
    private final static String FRAGMENT_HELP= "ChemApp.Help";

    private final static String SAVE_TAG = "ChemApp.Progress";

    private final ArrayList<Level> levels = new ArrayList();
    private NavigationView navigationView;
    private boolean lvl;
    private int level;
    private int section;
    private int action = -1;

    private TextView userName;
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    private boolean mExplicitSignOut = false;
    private boolean mInSignInFlow = false;
    private Snapshot snapshot = null;

    private FrameLayout mainFrame;
    private FrameLayout helperFrame;

    private SectionsFragment sectionsFragment;
    private LessonsFragment lessonsFragment;
    private ExerciseFragment exerciseFragment;
    private ExamFragment examFragment;
    private SplashFragment splashFragment;

    private final Set<Integer> unlocked = new HashSet();
    private SettingsFragment settingsFragment;
    private HelpFragment helpFragment;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        settings = getSharedPreferences(ChemApp.PREF_NAME, 0);
        settings.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("level")){
                    updateLevel(Integer.parseInt(settings.getString("level", "1")));
                }
            }
        });
        prefEditor = settings.edit();
        level = Integer.parseInt(settings.getString("level", "1"));
        section = settings.getInt("section", 0);
        mExplicitSignOut = !settings.getBoolean("autoLogin", false);

        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .build();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mainFrame = (FrameLayout) findViewById(R.id.main_frame);
        helperFrame = (FrameLayout) findViewById(R.id.helper_frame);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        lvl = navigationView.getMenu().findItem(R.id.nav_lessons) == null;
        userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name);

        if (splashFragment == null) {
            splashFragment = new SplashFragment();
        }
        if (getSupportFragmentManager().getFragments() == null || getSupportFragmentManager().getFragments().isEmpty()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, splashFragment, FRAGMENT_SPLASH).commit();
        }

        new LoadDataTask(this, new OnJSONResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray l = response.getJSONArray("results");
                    for (int i = 0; i < l.length(); i++) {
                        try {
                            levels.add(new Level(l.getJSONObject(i)));
                        } catch (Exception ex) {

                        }
                    }
                } catch (Exception ex) {
                }
                setLevel(level);
            }

            @Override
            public void onFail(String response) {
                System.out.println(response);
            }
        }).execute("level");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        userName.setText(Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName());
        mInSignInFlow = false;
        lvl = !lvl;
        loadSnapshot();
        updateDrawerMenu(null);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
                lvl = !lvl;
                updateDrawerMenu(null);
            }
        }
        mInSignInFlow = false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_failure);
                mInSignInFlow = false;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            showSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void updateLevel(int l){
        boolean invalidate = level != l;
        level = l;
        TextView tw = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView);
        tw.setText("");
        if (level > 0) {
            for (Level lv : levels) {
                if (lv.getId() == level) {
                    tw.setText(lv.getName());
                    break;
                }
            }
        }
        if (sectionsFragment != null) {
            sectionsFragment.loadSections(level);
        }
        if (invalidate) {
            int c = getSupportFragmentManager().getBackStackEntryCount();
            if (c > 0) {
                c--;
                switch (getSupportFragmentManager().getBackStackEntryAt(c).getName()) {
                    case FRAGMENT_LESSONS:
                    case FRAGMENT_EXERCISE:
                    case FRAGMENT_EXAM:
                        this.prefEditor.putInt("section", 0);
                        showSections();
                        break;
                }
            }
        }
    }

    private void setLevel(int l) {
        prefEditor.putString("level", l + "");
        prefEditor.commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        lvl = navigationView.getMenu().findItem(R.id.nav_lessons) == null;

        if (lvl) {

            switch (id) {
                case R.id.nav_log_in:
                    mSignInClicked = true;
                    mExplicitSignOut = false;
                    prefEditor.putBoolean("autoLogin", true);
                    prefEditor.commit();
                    mInSignInFlow = true;
                    mGoogleApiClient.connect();
                    break;
                case R.id.nav_log_out:
                    mSignInClicked = false;
                    mExplicitSignOut = true;
                    prefEditor.putBoolean("autoLogin", false);
                    prefEditor.commit();
                    mInSignInFlow = false;
                    userName.setText(R.string.not_logged_in);
                    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                        Games.signOut(mGoogleApiClient);
                        mGoogleApiClient.disconnect();
                    }
                    break;
                default:
                    setLevel(id);
                    break;
            }
            updateDrawerMenu(null);

        } else {

            switch (id) {
                // Handle the camera action
                case R.id.nav_sections:
                    showSections();
                    break;
                case R.id.nav_lessons:
                    action = LESSONS;
                    if (section > 0) {
                        showLessons();
                    } else {
                        showSections();
                    }
                    break;
                case R.id.nav_exercises:
                    action = EXERCISES;
                    if (section > 0) {
                        showExercises();
                    } else {
                        showSections();
                    }
                    break;
                case R.id.nav_exam:
                    showExam();
                    break;
                case R.id.nav_settings:
                    showSettings();
                    break;
                case R.id.nav_help:
                    showHelp();
                    break;
                default:
                    break;
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSections() {

        if (sectionsFragment == null) {
            sectionsFragment = new SectionsFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(helperFrame == null ? R.id.main_frame : R.id.helper_frame, sectionsFragment, FRAGMENT_SECTIONS).addToBackStack(FRAGMENT_SECTIONS).commit();
        if (helperFrame != null) {
            helperFrame.setVisibility(View.VISIBLE);
        }

    }

    private void showLessons() {

        if (lessonsFragment == null) {
            lessonsFragment = new LessonsFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, lessonsFragment, FRAGMENT_LESSONS).addToBackStack(FRAGMENT_LESSONS).commit();

    }

    private void showExercises() {

        if (exerciseFragment == null) {
            exerciseFragment = new ExerciseFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, exerciseFragment, FRAGMENT_EXERCISE).addToBackStack(FRAGMENT_EXERCISE).commit();

    }

    private void showExam() {

        if (examFragment == null) {
            examFragment = new ExamFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, examFragment, FRAGMENT_EXAM).addToBackStack(FRAGMENT_EXAM).commit();
    }

    private void showHelp() {

        if (helpFragment == null) {
            helpFragment = new HelpFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, helpFragment, FRAGMENT_HELP).addToBackStack(FRAGMENT_HELP).commit();
    }

    private void showSettings() {

        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, settingsFragment, FRAGMENT_SETTINGS).addToBackStack(FRAGMENT_SETTINGS).commit();
    }

    public void updateDrawerMenu(View v) {
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_lessons);
        lvl = !lvl;
        if (lvl) {
            Menu m = navigationView.getMenu();
            if (item != null) {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.drawer_select_level);
                int i = 5;
                for (Level l : levels) {
                    m.add(R.id.nav_levels, l.getId(), i++, l.getName()).setChecked(l.getId() == level);
                }
            } else {
                int i = 5;
                for (Level l : levels) {
                    if (m.findItem(l.getId()) == null) {
                        m.add(R.id.nav_levels, l.getId(), i++, l.getName()).setChecked(l.getId() == level);
                    }
                }
            }
            boolean showLogin = !mInSignInFlow && !isLogged();
            m.findItem(R.id.nav_log_in).setVisible(showLogin);
            m.findItem(R.id.nav_log_out).setVisible(!showLogin);
        } else if (item == null) {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main_drawer);
        }

        ImageButton ib = (ImageButton) navigationView.getHeaderView(0).findViewById(R.id.toggle_levels);
        ib.setImageResource(lvl ? R.drawable.ic_collapse : R.drawable.ic_expand);
    }

    public boolean isLogged() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    @Override
    public void onSectionSelected(int s) {
        section = s;
        prefEditor.putInt("section", section);
        prefEditor.commit();
        MenuItem m;
        switch (action) {
            case EXERCISES:
                showExercises();
                m = navigationView.getMenu().findItem(R.id.nav_exercises);
                if (m != null) {
                    m.setChecked(true);
                }
                break;
            case LESSONS:
            default:
                showLessons();
                m = navigationView.getMenu().findItem(R.id.nav_lessons);
                if (m != null) {
                    m.setChecked(true);
                }
                break;
        }
    }

    @Override
    public void setFragmentDescription(ActivityManager.TaskDescription td, String title) {
        setTaskDescription(td);
        getSupportActionBar().setTitle(title);
    }

    public void continueLearning(View v) {
        showLessons();
    }

    public void takeExam(View v) {
        showExam();
    }

    public void newSection(View v) {
        showSections();
    }

    @Override
    public void setFragmentDescription(String title) {
        getSupportActionBar().setTitle(title);

        Resources.Theme th = getTheme();
        TypedValue val = new TypedValue();
        th.resolveAttribute(R.attr.colorPrimary, val, true);

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_desc);

        setTaskDescription(new ActivityManager.TaskDescription(title, b, val.data));

    }

    private PendingResult<Snapshots.CommitSnapshotResult> writeSnapshot() {

        // Set the data payload for the snapshot
        SyncObject data = new SyncObject();
        data.level = level;
        data.section = section;
        data.unlocked = getUnlocked();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(bos).writeObject(data);
        } catch (Exception ex) {
        }
        if (snapshot != null && snapshot.getSnapshotContents() != null) {
            snapshot.getSnapshotContents().writeBytes(bos.toByteArray());
        }

        // Create the change operation
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setCoverImage(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setDescription(SAVE_TAG)
                .build();

        // Commit the operation
        return Games.Snapshots.commitAndClose(mGoogleApiClient, snapshot, metadataChange);
    }

    private void loadSnapshot() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {

                Snapshots.OpenSnapshotResult result = Games.Snapshots.open(mGoogleApiClient, SAVE_TAG, true).await();
                if (result.getStatus().isSuccess()) {
                    snapshot = result.getSnapshot();

                    SyncObject d1;
                    try {
                        d1 = (SyncObject) new ObjectInputStream(new ByteArrayInputStream(snapshot.getSnapshotContents().readFully())).readObject();
                    } catch (Exception ex) {
                        d1 = new SyncObject();
                    }
                    final SyncObject data = d1;
                    unlocked.clear();
                    for (int i : data.unlocked) {
                        unlocked.add(i);
                    }
                    section = data.section;
                    prefEditor.putInt("section", section);
                    prefEditor.commit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setLevel(data.level);
                        }
                    });

                } else {
                    snapshot = null;
                }
                return result.getStatus().getStatusCode();
            }

            @Override
            protected void onPostExecute(Integer status) {
                System.out.println("Snapshot status: " + status);
            }
        }.execute();
    }

    @Override
    public void setUnlocked(int sectionId) {
        unlocked.add(sectionId);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            writeSnapshot();
        }
    }

    @Override
    public boolean isUnlocked(int sectionId) {
        return unlocked.contains(sectionId);
    }

    @Override
    public int[] getUnlocked() {
        Integer[] ul = unlocked.toArray(new Integer[0]);
        int[] unl = new int[ul.length];
        for (int i = 0; i < unl.length; i++) {
            unl[i] = ul[i];
        }
        return unl;
    }

    @Override
    public ArrayList<Level> getLevels() {
        return levels;
    }

    @Override
    public int getProgress() {
        return unlocked.size();
    }

    @Override
    public void resetProgress() {
        unlocked.clear();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            writeSnapshot();
        }
    }

}
