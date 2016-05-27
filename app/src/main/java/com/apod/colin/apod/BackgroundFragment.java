package com.background.colin.background;
/*
 * Copyright (C) Colin Smith April 2016
 */
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.apod.colin.apod.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Background
        dFragment extends Fragment {

    private ArrayAdapter<String> mBackgroundAdapter;
//    private Drawable[] drawables = null;
//    private Drawable drawable;


    public BackgroundFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

//        drawables = new Drawable[] {
//                ResourcesCompat.getDrawable(getResources(), R.drawable.mercury_transit, null),
//                ResourcesCompat.getDrawable(getResources(), R.drawable.the_iris_nebula, null),
//                ResourcesCompat.getDrawable(getResources(), R.drawable.three_worlds, null)
//        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backgroundfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchBackgroundTask backgroundTask = new FetchBackgroundTask();
            backgroundTask.execute("94043");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String[] data = {
                "Today - Sunny - 90/63",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain - 65/56",
                "Sat - weather - 60/51",
                "Sun - Sunny - 80/68"
        };

        List<String> background = new ArrayList<>(Arrays.asList(data));

        //ArrayAdapter takes data from a source and puts it in the ListView.
        mBackgroundAdapter =
                new ArrayAdapter<>(
                        getActivity(), // The current context
                        R.layout.list_item_background, // ID of list item layout
                        R.id.list_item_background_TextView, // ID of the TextView to put data in
                        background); // Background Data
        View rootView = inflater.inflate(R.layout.content_background, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.ListView_background);
        listView.setAdapter(mBackgroundAdapter);

        return rootView;
    }

    public class FetchBackgroundTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchBackgroundTask.class.getSimpleName();

        /**
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getBackgroundDataFromJson(String backgroundJsonStr, int numData)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String NASA_DATE = "date";
            final String NASA_TITLE = "title";
            final String NASA_DESCRIPTION = "explanation";

            JSONObject backgroundJson = new JSONObject(backgroundJsonStr);
            JSONArray backgroundArray = backgroundJson.getJSONArray(NASA_DATE);

            String[] resultStrs = new String[numData];
            for(int i = 0; i < backgroundArray.length(); i++) {
                String description;

                // Get the JSON object representing the day
                JSONObject dayBackground = backgroundArray.getJSONObject(i);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayBackground.getJSONArray(NASA_TITLE).getJSONObject(0);
                description = weatherObject.getString(NASA_DESCRIPTION);

                resultStrs[i] =  description;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Background entry: " + s);
            }
            return resultStrs;

        }
        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String backgroundJsonStr = null;

//            String format = "json";
//            String units = "metric";
            String api_key = "cMei2Vz7UUWgE9yJ77cbyeJO5pvOgkgJGQr5y8CL";
            int numData = 1;

            try {
                // Construct the URL for the NASA query
                final String BACKGROUND_BASE_URL =
                        "https://api.nasa.gov/planetary/apod?";
                final String QUERY_PARAM = "api_key";
//                final String FORMAT_PARAM = "mode";
//                final String UNITS_PARAM = "units";
//                final String DAYS_PARAM = "cnt";
//                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(BACKGROUND_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, api_key)
//                        .appendQueryParameter(FORMAT_PARAM, format)
//                        .appendQueryParameter(UNITS_PARAM, units)
//                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numData))
//                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_BACKGROUND_MAP_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    builder.append(line);
                }

                if (builder.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                backgroundJsonStr = builder.toString();

                Log.v(LOG_TAG, "Background string: " + backgroundJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getBackgroundDataFromJson(backgroundJsonStr, numData);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the data.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mBackgroundAdapter.clear();
                for(String dayBackgroundStr : result) {
                    mBackgroundAdapter.add(dayBackgroundStr);
                }
            }
        }
    }
}
