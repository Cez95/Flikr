package net.writeboard.flikr;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.SearchView;
import android.util.Log;
import android.view.Menu;

public class SearchActivity extends BaseActivity {
    private static final String TAG = "SearchActivity";
    private SearchView mSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        activateToolbar(true);
        Log.d(TAG, "onCreate: Ends");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: Starts");
        // This is how to inflate a search menu
        // Inflating just means to take an XML layout and create it on the view
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // This is how to instantiate the search manager for querying searches
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        mSearchView.setSearchableInfo(searchableInfo); // This is the placeholder text but
        mSearchView.setIconified(false); // Setting this true will require user to tap button to open search
                                        // Setting false will open search as soon as the view appears

        // Handles querying the tags for text when entered and when text changes
        // Standard pattern for adding listeners
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: called");

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPreferences.edit().putString(FLICKR_QUERY, query).apply(); // Stores the tag from the search

                mSearchView.clearFocus(); // This is what dismiss the keyboard
                finish(); // Closes the activity and goes back to previous view. Like dismiss in swift
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG, "onClose: called");
                mSearchView.clearFocus();
                finish();
                return false;
            }
        });

        Log.d(TAG, "onCreateOptionsMenu: Return True");
        return true;
    }
}
