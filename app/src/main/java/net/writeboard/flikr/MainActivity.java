package net.writeboard.flikr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements GetFlikrJsonData.OnDataAvailable,
                            RecyclerItemClickListener.OnRecyclerClickListener {

    private static final String TAG = "MainActivity";
    private FlikrRecyclerViewAdapter mFlikrRecyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: CHRIS: View Created starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activateToolbar(false);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, this));

        mFlikrRecyclerViewAdapter = new FlikrRecyclerViewAdapter(new ArrayList<Photo>(), this );
        recyclerView.setAdapter(mFlikrRecyclerViewAdapter);

//        GetRawData getRawData = new GetRawData(this);
//        getRawData.execute("https://api.flickr.com/services/feeds/photos_public.gne/?tags=android,nougat&format=json&nojsoncallback=1");


        Log.d(TAG, "onCreate: CHRIS: View created ends");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Starts");
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Checks to see if a user prefernece was saved from a query
        String queryResult = sharedPreferences.getString(FLICKR_QUERY, ""); // Sets the user preference to a string that is going to be used to build the url string

        if(queryResult.length() > 0) { // Checks if a user preference was created to build a filtered url
            GetFlikrJsonData getFlikrJsonData = new GetFlikrJsonData("https://api.flickr.com/services/feeds/photos_public.gne/", "en-us", true, this);
            getFlikrJsonData.execute(queryResult);
        }

        Log.d(TAG, "onResume: Ends");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d(TAG, "onCreateOptionsMenu() returned: " + true);
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

        if(id == R.id.action_search) {
            // Segues to the search View by invocing the SearchActivity class on push
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        }

        Log.d(TAG, "onOptionsItemSelected() returned: returned");
        return super.onOptionsItemSelected(item);
    }

    public void onDataAvailable(List<Photo> data, DownloadStatus status) {
        if (status == DownloadStatus.OK){
            mFlikrRecyclerViewAdapter.loadNewData(data);
        } else {
            // Failed download
            Log.e(TAG, "onDownloadComplete: Failed status " + status);
        }
        Log.d(TAG, "onDataAvailable: Ends");
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick: Starts");
        Toast.makeText(MainActivity.this, "Normal Tap at position " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Log.d(TAG, "onItemLongClick: starts");
        Intent intent = new Intent(this, PhotoDetailActivity.class);
        // This is how to segue while passing data
        intent.putExtra(PHOTO_TRANSFER, mFlikrRecyclerViewAdapter.getPhoto(position));
        startActivity(intent);
    }
}
