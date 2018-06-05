package net.writeboard.flikr;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


enum DownloadStatus { IDLE, PROCESSING, NOT_INITILISED, FAILD_OR_EMPTY, OK }

class GetRawData extends AsyncTask<String, Void, String> {

    private static final String TAG = "GetRawData";
    private DownloadStatus mDownloadStatus; // m - starts for member variable (Common Convention)
    private final OnDownloadComplete mCallBack;

    // Defines an Interface
    interface OnDownloadComplete {
        void onDownloadComplete(String data, DownloadStatus status);
    }

    public GetRawData(OnDownloadComplete callBack){
        this.mDownloadStatus = DownloadStatus.IDLE;
        mCallBack = callBack;
    }

    void runInSameThread(String s){
        Log.d(TAG, "runInSameThread: Starts");

//        onPostExecute(doInBackground(s));
        if(mCallBack != null) {
            String result = doInBackground(s);
            mCallBack.onDownloadComplete(result, mDownloadStatus);
        }

        Log.d(TAG, "runInSameThread: Ends");
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: Parameter = " + s);
        if(mCallBack != null){
            mCallBack.onDownloadComplete(s, mDownloadStatus);
        }
        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        if(strings == null){
            mDownloadStatus = DownloadStatus.NOT_INITILISED;
            return null;
        }

        try {
            mDownloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(strings[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: The response code is " + response);


            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

//            String line;
//            while (null != (line = reader.readLine())) {
            for(String line = reader.readLine(); line != null; line = reader.readLine()){
                result.append(line).append("\n");
            }
            mDownloadStatus = DownloadStatus.OK;
            return result.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO Exception " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Needs Permission" + e.getMessage());
        } finally { // Gauranteed to be executed wether an exception is thrown or not!
            if(connection != null){
                connection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing " + e.getMessage() );
                }
            }
        }
        mDownloadStatus = DownloadStatus.FAILD_OR_EMPTY;
        return null;
    }
}
