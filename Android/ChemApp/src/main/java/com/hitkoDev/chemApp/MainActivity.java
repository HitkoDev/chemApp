package com.hitkoDev.chemApp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

/**
 *
 * @author hitno
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    
    private ArrayList<Level> levels = new ArrayList();
    private NavigationView navigationView;
    private boolean lvl;
    private int level;
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
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
        lvl = true;
        toggleDrawerMenu(null);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        lvl = navigationView.getMenu().findItem(R.id.nav_lessons) == null;
        
        if(lvl){
            
            setLevel(id);
            
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
            if(item != null){
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.drawer_select_level);
                Menu m = navigationView.getMenu();
                for(Level l : levels) m.add(R.id.nav_levels, l.getId(), 0, l.getName());
            } else {
                Menu m = navigationView.getMenu();
                for(Level l : levels) if(m.findItem(l.getId()) == null) m.add(R.id.nav_levels, l.getId(), 0, l.getName());
            }
        } else {
            if(item == null){
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.activity_main_drawer);
            }
        }
        
        ImageButton ib = (ImageButton) navigationView.getHeaderView(0).findViewById(R.id.toggle_levels);
        ib.setImageResource(lvl ? R.drawable.ic_collapse : R.drawable.ic_expand);
    }
    
}
