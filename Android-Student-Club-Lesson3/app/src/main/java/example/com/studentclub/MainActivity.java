package example.com.studentclub;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * @author: Tareq Si Salem
 */
public class MainActivity extends ActionBarActivity {

    // TIPS: Just to see the activity life cycle.
    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Tareq", "pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Tareq", "resume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Tareq", "start");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Tareq", "stop");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        ArrayAdapter<String> arrayAdapter;

        public PlaceholderFragment() {
        }

        @Override
        public void onStart() {
            super.onStart();
            updateWeather();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            try {

                // Declare an empty arrayList.
                ArrayList<String> offline_dummy_data = new ArrayList<>();

                // We don't need the dummy content anymore.
                // offline_dummy_data.addAll(Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7"));
                arrayAdapter = new ArrayAdapter<String>(rootView.getContext(), R.layout.list_item_forcast, R.id.list_item_forcast_textView, offline_dummy_data);
                ListView listView = (ListView) rootView.findViewById(R.id.list_view_forcast);
                listView.setAdapter(arrayAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // start new activity and send a text content inside an Explicit intent.
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT, arrayAdapter.getItem(position).toString());
                        startActivity(intent);
                    }
                });
            } catch (Exception e) {
                Log.i("Tareq", e.toString());
            }
            return rootView;
        }

        private void updateWeather() {
            CustomAsyncTask customAsyncTask = new CustomAsyncTask();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = preferences.getString(getString(R.string.location_key), getString(R.string.pref_default_location_name));
            String units = preferences.getString(getString(R.string.units_key), getString(R.string.pref_default_unit));
            customAsyncTask.execute(location, units);
        }


        class CustomAsyncTask extends AsyncTask<String, Void, String[]> {

            // Query parameters to be Added to the Uri Builder.
            String FORCAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            String OBJ_LIST = "list";
            String OBJ_WEATHER = "weather";
            String OBJ_TEMP = "temp";
            String OBJ_MAX = "max";
            String OBJ_MIN = "min";
            String OBJ_DATETIME = "dt";
            String OBJ_DESCRIPTION = "main";
            int days = 7;
            // end of Declaration.

            // List items text content to be displayed on the List View
            String[] items = new String[days];

            @Override
            protected String[] doInBackground(String... params) {
                try {
                    Log.i("Tareq", "Thread Started");
                    // Thread Started Here.
                    // Build our uri example format "http://api.openweathermap.org/data/2.5/forecast/daily?q=Boumerdes,dz&mode=json&cnt=7&units=metric"
                    Uri uri = Uri.parse(FORCAST_BASE_URL).buildUpon()
                            .appendQueryParameter("q", params[0] + ",dz")
                            .appendQueryParameter("mode", "json")
                            .appendQueryParameter("cnt", days + "")
                            .appendQueryParameter("units", params[1]).build();
                    //Uri building finished.


                    // Establishing http connection.
                    URL url = new URL(uri.toString());
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();
                    // Finished Establishing Http Connection with GET request method.

                    // Read Encoded bytes from the Input Stream, Encoded means that the data are not in binary "characters".
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    // result was saved in the stringBuilder object.


                    // Parse the JSON result.

                    //main object instance
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());

                    //the array "list" inside the object
                    JSONArray list = jsonObject.getJSONArray(OBJ_LIST);

                    /**
                     *  String objects are special for each time It's appended a new string object is created **BE CAREFUL**.
                     *  For many appending operations use StringBuilder.
                     */
                    String item = "";

                    // looping through the array
                    for (int i = 0; i < days; i++) {
                        // Element of the array
                        JSONObject dayInfo = list.getJSONObject(i);

                        /** Creating the date from the dt integer. dt = day time. in the Format*/
                        /**
                         * E day of week
                         *     {@code E}/{@code EE}/{@code EEE}:Tue, {@code EEEE}:Tuesday, {@code EEEEE}:T
                         */

                        /**
                         *  @code M month in year
                         *  {@code M}:1 {@code MM}:01 {@code MMM}:Jan {@code MMMM}:January {@code MMMMM}:J
                         *
                         */
                        /**
                         *@code d day in month
                         * (Number)
                         */
                        /**
                         * Refer to SimpleDateFormat Sourcecode for more details, you know java is open source :).
                         */
                        SimpleDateFormat format = new SimpleDateFormat("E, MM d");
                        String date = format.format(new Date((dayInfo.getLong(OBJ_DATETIME)) * 1000)).toString();

                        //appending space
                        item += date + " ";

                        // We add the description of the weather Rainy cloudy .. etc.
                        item += dayInfo.getJSONArray(OBJ_WEATHER).getJSONObject(0).getString(OBJ_DESCRIPTION) + " ";

                        // parsing the temp object.
                        JSONObject tempObj = dayInfo.getJSONObject(OBJ_TEMP);

                        // appending the minimum temperature.
                        item += (int) tempObj.getDouble(OBJ_MIN) + "/";

                        // appending the maximum temperature.
                        item += (int) tempObj.getDouble(OBJ_MAX) + " ";

                        // assign the item to the array at the specific index.
                        items[i] = item;
                        item = "";
                    }
                    Log.i("Tareq", "Result fetched !");

                } catch (Exception ex) {
                    // in case of error in parsing or establishing Connection.
                    Log.i("Tareq", ex.toString());
                }
                return items;
            }

            @Override
            protected void onPostExecute(String[] results) {
                // check if all results are received.
                for (int i = 0; i < results.length; i++) {
                    if (results[i] == null)
                        return;
                }
                // if reached to this line clear dummy offline data
                arrayAdapter.clear();
                // start adding results
                for (int i = 0; i < results.length; i++) {
                    arrayAdapter.add(results[i]);
                }
            }
        }
    }


}