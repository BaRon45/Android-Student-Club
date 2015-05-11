package example.com.studentclub;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import example.com.studentclub.R;

/**
 * @author: Tareq Si Salem
 */
public class MainActivity extends ActionBarActivity {

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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            try {
                Log.i("Tareq", rootView.toString());
                Log.i("Tareq", getActivity().toString());

                ArrayList<String> offline_dummy_data = new ArrayList<>();
                offline_dummy_data.addAll(Arrays.asList("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7"));
                arrayAdapter = new ArrayAdapter<String>(rootView.getContext(), R.layout.list_item_forcast, R.id.list_item_forcast_textView, offline_dummy_data);
                ListView listView = (ListView) rootView.findViewById(R.id.list_view_forcast);
                listView.setAdapter(arrayAdapter);
                CustomAsyncTask customAsyncTask = new CustomAsyncTask();
                customAsyncTask.execute("Boumerdes");
            } catch (Exception e) {
                Log.i("Tareq", e.toString());

            }
            return rootView;
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
                    // Thread Started Here.
                    Log.i("Tareq", "Thread started");

                    // Build our uri example format "http://api.openweathermap.org/data/2.5/forecast/daily?q=Boumerdes,dz&mode=json&cnt=7&units=metric"
                    Uri uri = Uri.parse(FORCAST_BASE_URL).buildUpon()
                            .appendQueryParameter("q", params[0] + ",dz")
                            .appendQueryParameter("mode", "json")
                            .appendQueryParameter("cnt", days + "")
                            .appendQueryParameter("units", "metric").build();
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
                        Log.i("Tareq", line);
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