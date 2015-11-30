package com.hitkoDev.chemApp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
    private boolean showLogin = true;
    
    private TextView userName;
    
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();
        
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        
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
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    
    @Override
    public void onConnected(Bundle connectionHint) {
        userName.setText(Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName());
        lvl = !lvl;
        showLogin = false;
        this.toggleDrawerMenu(null);
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_failure);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void setLevel(int l){
        level = l;
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
                    mGoogleApiClient.connect();
                    break;
                case R.id.nav_log_out:
                    mSignInClicked = false;
                    Games.signOut(mGoogleApiClient);
                    userName.setText(R.string.not_logged_in);
                    showLogin = true;
                    break;
                default:
                    setLevel(id);
                    break;
            }
            toggleDrawerMenu(null);
            
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
    
    public void toggleDrawerMenu(View v){
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_lessons);
        lvl = !lvl;
        if(lvl){
            Menu m = navigationView.getMenu();
            if(item != null){
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.drawer_select_level);
                int i = 5;
                for(Level l : levels) m.add(R.id.nav_levels, l.getId(), i++, l.getName());
            } else {
                int i = 5;
                for(Level l : levels) if(m.findItem(l.getId()) == null) m.add(R.id.nav_levels, l.getId(), i++, l.getName());
            }
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
