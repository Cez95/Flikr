package net.writeboard.flikr;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class GetFlikrJsonData extends AsyncTask<String, Void, List<Photo>> implements GetRawData.OnDownloadComplete {

    private static final String TAG = "GetFlikrJsonData";

    private List<Photo> mPhotoList = null;
    private String mBaseURL;
    private String mLang;
    private boolean mMatchAll;

    private final OnDataAvailable mCallBack;
    private boolean runningOnSameThread = false;

    interface OnDataAvailable {
        void onDataAvailable(List<Photo> data, DownloadStatus status);
    }

    @Override
    protected List<Photo> doInBackground(String... params) {
        Log.d(TAG, "doInBackground: Starts");
        String destinationUri = creatUri(params[0], mLang, mMatchAll);

        GetRawData getRawData = new GetRawData(this);
        getRawData.runInSameThread(destinationUri);
        Log.d(TAG, "doInBackground: Ends");

        return mPhotoList;
    }

    @Override
    protected void onPostExecute(List<Photo> photos) {
        Log.d(TAG, "onPostExecute: Starts");
        if(mCallBack != null) {
            mCallBack.onDataAvailable(mPhotoList, DownloadStatus.OK);
        }
        Log.d(TAG, "onPostExecute: Ends");
    }

    public GetFlikrJsonData(String baseURL, String lang, boolean matchAll, OnDataAvailable callBack) {
        Log.d(TAG, "GetFlikrJsonData: GetFlikrData Called");
        mBaseURL = baseURL;
        mLang = lang;
        mMatchAll = matchAll;
        mCallBack = callBack;
    }

    // Called in main activity
    void executeOnSameThread(String searchCriteria){
        Log.d(TAG, "executeOnSameThread: Starts");
        runningOnSameThread = true;
        String destinationUri = creatUri(searchCriteria, mLang, mMatchAll);

        GetRawData getRawData = new GetRawData(this);
        getRawData.execute(destinationUri);

        Log.d(TAG, "executeOnSameThread: Ends");
    }

    // This is how to build a custom Uri.
    private String creatUri(String searchCriteria, String lang, boolean matchAll){
        Log.d(TAG, "creatUri: Starts");


        return Uri.parse(mBaseURL).buildUpon()
                .appendQueryParameter("tags", searchCriteria)
                .appendQueryParameter("tagmode", matchAll ? "ALL" : "ANY")
                .appendQueryParameter("lang", lang)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .build().toString();
    }

    // This is how to parse JSON Data
    // A Uri - A Uniform Resource Identifier (URI) is a string of characters designed for unambiguous identification of resources and extensibility via the URI scheme.
    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {
        Log.d(TAG, "onDownloadComplete: Starts: Stats = " + status);
        if (status == DownloadStatus.OK){
            mPhotoList = new ArrayList<>();
            // Only executes if we get on OK status from the GetRawData.OnDownloadComplete method
            try {
                // Adds the items array from the API to a new array we can loop through and parse
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("items");

                for (int i = 0; i<itemsArray.length(); i++){
                    JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                    String title = jsonPhoto.getString("title");
                    String author = jsonPhoto.getString("author");
                    String authorId = jsonPhoto.getString("author_id");
                    String tags = jsonPhoto.getString("tags");

                    // Pulls from another embeded distionary inside an array
                    JSONObject jsonMedia = jsonPhoto.getJSONObject("media");
                    String photoUrl = jsonMedia.getString("m");

                    String link = photoUrl.replaceFirst("_m.", "_b."); // Replaces the _m. in the url with a _b., linking to the bigger photo from the API

                    Photo photo = new Photo(title, author, authorId, link, tags, photoUrl);
                    mPhotoList.add(photo);
                    Log.d(TAG, "onDownloadComplete: Added " + photo.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "onDownloadComplete: Jason parse error " + e.getMessage());
                status = DownloadStatus.FAILD_OR_EMPTY;
            }
        }

        // Only calls if running on the same thread and mCallBack is not empty
        if (runningOnSameThread && mCallBack != null) {
            // Now Inform caller that the processing is complete or returning an error
            mCallBack.onDataAvailable(mPhotoList, status); // Passes the array of data objects and the status of completion(fail or success)
        }

        Log.d(TAG, "onDownloadComplete: ends");
    }
}
