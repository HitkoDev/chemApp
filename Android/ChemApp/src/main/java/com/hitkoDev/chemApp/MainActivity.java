package com.hitkoDev.chemApp;

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
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.SharedPreferences;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

/**
 *
 * @author hitno
 */
public class MainActivity extends AppCompatActivity implements 
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    
    private ArrayList<Level> levels = new ArrayList();
    private NavigationView navigationView;
    private boolean lvl;
    private int level;
    
    private TextView userName;
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    private boolean mExplicitSignOut = false;
    private boolean mInSignInFlow = false;
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        settings = getSharedPreferences(ChemApp.PREF_NAME, 0);
        prefEditor = settings.edit();
        level = settings.getInt("level", 0);
        mExplicitSignOut = !settings.getBoolean("autoLogin", true);
        
        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        
        setContentView(R.layout.activity_main);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        lvl = navigationView.getMenu().findItem(R.id.nav_lessons) == null;
        userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name);
        
        new LoadDataTask(this, new OnJSONResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray l = response.getJSONArray("results");
                    for(int i = 0; i < l.length(); i++){
                        try {
                            levels.add(new Level(l.getJSONObject(i)));
                        } catch (JSONException ex) {
                            Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
                setLevel(level);
            }

            @Override
            public void onFail(String response) {
                System.out.println(response);
            }
        }).executeCached("level");
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
    }
    
    @Override
    public void onConnected(Bundle connectionHint) {
        userName.setText(Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName());
        mInSignInFlow = false;
        lvl = !lvl;
        updateDrawerMenu(null);
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) return;

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void setLevel(int l){
        level = l;
        prefEditor.putInt("level", l);
        prefEditor.commit();
        TextView tw = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView);
        tw.setText("");
        if(level > 0){
            for(Level lv : levels) if(lv.getId() == level){
                tw.setText(lv.getName());
                break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        lvl = navigationView.getMenu().findItem(R.id.nav_lessons) == null;
        
        if(lvl){
            
            switch(id){
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
                case R.id.nav_lessons:
                    break;
                case R.id.nav_exercises:
                    break;
                case R.id.nav_exam:
                    break;
                case R.id.nav_settings:
                    break;
                default:
                    break;
            }
            
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    
    public void updateDrawerMenu(View v){
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_lessons);
        lvl = !lvl;
        if(lvl){
            Menu m = navigationView.getMenu();
            if(item != null){
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.drawer_select_level);
                int i = 5;
                for(Level l : levels) m.add(R.id.nav_levels, l.getId(), i++, l.getName()).setChecked(l.getId() == level);
            } else {
                int i = 5;
                for(Level l : levels) if(m.findItem(l.getId()) == null) m.add(R.id.nav_levels, l.getId(), i++, l.getName()).setChecked(l.getId() == level);
            }
            boolean showLogin = !mInSignInFlow && !isLogged();
            m.findItem(R.id.nav_log_in).setVisible(showLogin);
            m.findItem(R.id.nav_log_out).setVisible(!showLogin);
        } else {
            if(item == null){
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.activity_main_drawer);
            }
        }
        
        ImageButton ib = (ImageButton) navigationView.getHeaderView(0).findViewById(R.id.toggle_levels);
        ib.setImageResource(lvl ? R.drawable.ic_collapse : R.drawable.ic_expand);
    }
    
    public boolean isLogged(){
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }
    
}
