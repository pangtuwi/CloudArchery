package com.cloudarchery.archersapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cloudarchery.slidingmenu.adapter.NavDrawerListAdapter;
import com.cloudarchery.slidingmenu.model.NavDrawerItem;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private String[] navPageTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    MyApp myAppState;
    SharedPreferences mSharedPreferences;
    boolean myUserSyncOn = false;
    boolean myUserFirstRun = false;
    String userID = "";
    String myUserClubID = "";
    int REQUEST_CODE_LOGIN = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ToDo : (RELEASE > 2) : Main Page needs a "No rounds have been loaded yet" warning for when a sync has not yet happened.
        myAppState = ((MyApp)getApplicationContext());

        //Load Stored Config Data (Shared Preferences)
        mSharedPreferences = this.getSharedPreferences(getString(R.string.PREFS), this.MODE_PRIVATE);

        //Temporary code to clear Shared Preferences for testing purposes
        Boolean clearSharedPreferences = false;
        if (clearSharedPreferences) {
            SharedPreferences.Editor e = mSharedPreferences.edit();
            e.clear();
            e.commit();
        }
        //Temporary code to clear Database for testing purposes
        Boolean clearLDS = false;
        if (clearLDS) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Data Deletion");
            alert.setMessage("Are you sure you want to delete all the records from your device database?");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    SQLiteRounds db = new SQLiteRounds(getBaseContext());
                    db.deleteAllData();
                    SQLiteFirebaseQueue dbQ = new SQLiteFirebaseQueue(getBaseContext());
                    dbQ.deleteAllData();
                } //OnClick (dialog)
            }); //OnClickListener

            // Make a "Cancel" button that simply dismisses the alert
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
        } //clearLDS


        mTitle = mDrawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navPageTitles = getResources().getStringArray(R.array.nav_drawer_page_titles);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array

        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), true, "-"));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, myUserClubID));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));

        // Recycle the typed array
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                navDrawerItems.get(0).setCount("" + myAppState.getNumRounds());
                if (myAppState.isConnected()){
                    navDrawerItems.get(3).setCount(myAppState.CDS.clubID);
                } else  {
                    navDrawerItems.get(3).setCount("DISCONNECTED");
                }
                    adapter.notifyDataSetChanged();
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
        if (mSharedPreferences.contains(getString(R.string.PREF_FIRSTRUN))) {
            //Contains a value for firstrun, therefore not the first time the app is run
            myUserSyncOn = mSharedPreferences.getBoolean(getString(R.string.PREF_SYNC), false);
            userID = mSharedPreferences.getString(getString(R.string.PREF_USERID), "");
            myUserClubID = mSharedPreferences.getString(getString(R.string.PREF_CLUBID), "");
            myUserFirstRun = mSharedPreferences.getBoolean(getString(R.string.PREF_FIRSTRUN), false);
        } else {
            //first time the app is run
            SharedPreferences.Editor e = mSharedPreferences.edit();
            e.putBoolean(getString(R.string.PREF_FIRSTRUN), false);

            //ToDo : (RELEASE > 2) - put some RoundTypes in Database for first startup

            //Generate a unique userID to use until linked to a userID in the CDS
            UUID uuid = UUID.randomUUID();
            final String id = uuid.toString();
            e.putString(getString(R.string.PREF_USERID), id);
            e.commit();
            //Set userID to new one
            myAppState.CDS.userID = id;
            //Log.d ("CloudArchery", "Created new user ID : "+ myAppState.CDS.userID);

            myAppState.CDS.startRoundTypeUpdate();

            Bundle args = new Bundle();
            args.putBoolean("linked", false);
            args.putBoolean("newloginverified", false);
            Fragment fragment = new Login();
            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack("Login")
                        .commit();
            }

        }

    }

    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
         /*   case R.id.action_settings:
                return true; */
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /* *
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
     //   menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }


    private void displayView(int position) {
        //Displaying fragment view for selected nav drawer list item
        // update the main content by replacing fragments
        Fragment fragment = null;
        String fragmentName = "";
        switch (position) {
            case 0:
                fragment = new RoundsList();
                fragmentName = "RoundsList";
                break;
            case 1:
                fragment = new NewRound();
                fragmentName = "NewRound";
                break;
            case 2:
                fragment = new JoinRound();
                fragmentName = "JoinRound";
                break;
            case 3:
                fragment = new MyClub();
                fragmentName = "MyClub";
                break;
            case 4:
                fragment = new Statistics();
                fragmentName = "MyStats";
                break;
            case 5:
                fragment = new Settings();
                fragmentName = "Settings";
                break;

            default:
                break;
        }
        Bundle args = new Bundle();
        args.putString("userID", userID);
        fragment.setArguments(args);
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentName.equals("RoundsList")) {
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack(fragmentName)
                        .commit();
            }

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navPageTitles[position]);
            //ToDo : Bug : occasionally wrong title appears after being on sub page.  could not replicate
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_LOGIN) {
            RoundsList fragment = (RoundsList) getFragmentManager().findFragmentById(R.id.frame_container);
        }
    } //onActivityResult

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.activityPaused();
    }

}
