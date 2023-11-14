package climaApp.weatherviewer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Weather> weatherList = new ArrayList<>();
    // ArrayAdapter for binding Weather objects to a ListVies
    private WeatherArrayAdapter weatherArrayAdapter;
    private ListView weatherListView; // displays weather info


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        weatherListView = (ListView) findViewById(R.id.weatherListView);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);


        FloatingActionButton fab=
                (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view) {


            EditText locationEditText = (EditText) findViewById(R.id.locationEditText);
            URL url = createURL(locationEditText.getText().toString());



            if (url != null){
                dismissKeyboard(locationEditText);
                GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                getLocalWeatherTask.execute(url);
            }
            else{
                Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url, Snackbar.LENGTH_LONG).show();

            }
        }
        });
}

    // programmatically dismiss keyboard when user touches FAB
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

// create openweathermap.org web service URL using city
        private URL createURL(String city) {
            String apiKey = getString(R.string.api_key);
            String baseUrl = getString(R.string.web_service_url);

            //http://api.openweathermap.org/data/2.5/forecast?id=524901& appid={API key}
            try {

                String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") +
                        "&units=imperial&cnt=16&APPID=" + apiKey;
                return new URL(urlString);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
                return null; // URL was malformed
    }

    // makes the REST web service call to get weather data and
// saves the data to a local HTML file
    private class GetWeatherTask
            extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground (URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                    catch (IOException e) {
                            Snackbar.make(findViewById(R.id.coordinatorLayout),
                                    R.string.read_error, Snackbar.LENGTH_LONG).show();
                            e.printStackTrace();

                    }
                    return new JSONObject(builder.toString());
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            }
                    catch (Exception e) {
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                R.string.connect_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    finally {
                        connection.disconnect(); // close the HttpURLConnection
                    }
                        return null;
                    }
                    // process 350N response and update ListView
                    @Override
                    protected void onPostExecute(JSONObject weather) {
                        convertJSONtoArrayList(weather); // repopulate weatherList
                  weatherArrayAdapter.notifyDataSetChanged (); // rebind to ListView
                  weatherListView.smoothScrollToPosition(0); // scroll to top
                    }
    }

    // create Weather objects from 35UNObject containing the forecast
    private void convertJSONtoArrayList(JSONObject forecast) {
        weatherList.clear();    // clear old weather data

        try {
            // read the "list" of weather forecast - JSONArray
            JSONArray list = forecast.getJSONArray("list");

            // transform each list element into a Weather object
            for (int i = 0; i < list.length(); ++i) {
                JSONObject day = list.getJSONObject(i); // read data concerning one day

                // read data concerning temperature in that day from JSONObject
                JSONObject temperatures = day.getJSONObject("main");

                // read description and weather icon from JSONObject
                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                // add new Weather object to weatherList
                weatherList.add(new Weather(
                        day.getLong("dt"),  // timestamp
                        temperatures.getDouble("temp_min"),  // minimal temperature
                        temperatures.getDouble("temp_max"),  // maximal temperature
                        temperatures.getDouble("humidity"),  // humidity
                        weather.getString("description"), // weather conditions
                        weather.getString("icon")));    // icon name
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}