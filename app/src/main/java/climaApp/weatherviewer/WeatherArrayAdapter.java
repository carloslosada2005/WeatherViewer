package climaApp.weatherviewer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class WeatherArrayAdapter extends ArrayAdapter<Weather>{
    // class for reusing views as list items scroll off and onto the screen
    private static class ViewHolder {
            ImageView conditionImageView;
            TextView dayTextView;
            TextView lowTextView;
            TextView hiTextView;
            TextView humidityTextView;
    }

    private Map<String, Bitmap> bitmaps = new HashMap<>();

    public WeatherArrayAdapter(Context context, List<Weather> forecast) {
        super(context, -1, forecast);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Weather day = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView =
                    inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.conditionImageView =
                    (ImageView) convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView =
                    (TextView) convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView =
                    (TextView) convertView.findViewById(R.id.lowTextView);
            viewHolder.hiTextView =
                    (TextView) convertView.findViewById(R.id.hiTextView);
            viewHolder.humidityTextView =
                    (TextView) convertView.findViewById(R.id.humidityTextView);
            convertView.setTag(viewHolder);
        }
        else { // reuse existing ViewHolder stored as the list item's tag
             viewHolder= (ViewHolder) convertView.getTag();
        }

        if (bitmaps.containsKey (day.iconURL)) {
            viewHolder.conditionImageView.setImageBitmap(
                    bitmaps.get(day.iconURL));
        }
        else {
        //download and display seather conditioninge
            new LoadImageTask(viewHolder.conditionImageView).execute(day.iconURL);
        }
        // get other data from Weather object and pla
        Context context = getContext(); //for loading String resources
        viewHolder.dayTextView.setText(context.getString( R.string.day_description, day.dayOfWeek, day.description));
                viewHolder.lowTextView.setText( context.getString(R.string.low_temp, day.minTemp));

        viewHolder.hiTextView.setText(
                context.getString(R.string.high_temp, day.maxTemp));
        viewHolder.humidityTextView.setText( context.getString(R.string.humidity, day.humidity));
        return convertView; // return completed list item to display
    }

    // AsyncTask to load weather condition icons in a separate thread
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;
        public LoadImageTask(ImageView imageView) {

            this.imageView = imageView;
        }
            @Override
            protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

            try (InputStream inputStream = connection.getInputStream()) {
                bitmap= BitmapFactory.decodeStream(inputStream);
                bitmaps.put(params[0], bitmap); // cache for Inter use
            }

            catch (Exception e) {
                e.printStackTrace();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally { connection.disconnect(); // close the Http//AL.Connection
                }
            return bitmap;
            }
            @Override
                protected void onPostExecute(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
                }
            }
        }
