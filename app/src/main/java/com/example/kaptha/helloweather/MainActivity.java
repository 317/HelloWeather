package com.example.kaptha.helloweather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Boolean search_done = false;
    private LinearLayout resultLayout;
    private RetrieveFeedTask retrieveTask;
    private  EditText et_input_ville;
    private InputMethodManager inputMethodManager;

    protected String city_to_look_for;
    protected TextView resultVille;
    protected TextView resultTemp;
    protected TextView resultWind;
    protected ImageView resultIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Code nécessaire pour la requête HTTP.
        //Source : http://stackoverflow.com/questions/22395417/error-strictmodeandroidblockguardpolicy-onnetwork
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //Allez hop on affiche la vue histoire de pouvoir récupérer les éléments dans des variables après
        setContentView(R.layout.activity_main);
        //On remplit les variables
        this.resultLayout = (LinearLayout)findViewById(R.id.result_layout);
        this.resultVille = (TextView) findViewById(R.id.result_ville);
        this.et_input_ville = (EditText) findViewById(R.id.input_ville);
        this.resultTemp = (TextView) findViewById(R.id.result_temp);
        this.resultWind = (TextView) findViewById(R.id.result_wind);
        this.resultIcon = (ImageView) findViewById(R.id.result_icon);
        //On récupère le clavier
        this.inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * Si on a déjà fait une recherche, un tap sur l'input va l'éffacer.
     * On passe par le param View histoire de respecter au moins une bonne pratique dans sa vie.
     * @param view la vue lol
     */
    public void clearInput(View view){
        if(this.search_done == true){
            ((EditText)view).setText("");
        }
    }

    public void requestWeather(View view){
        //On signale qu'une recherche a été demandée
        this.search_done = true;
        //On vire le clavier pour avoir un écran tout joli
        this.inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        //On récupère la donnée dans l'input
        this.city_to_look_for = this.et_input_ville.getText().toString();
        //Et hop une nouvelle tâche asynchrone !
        retrieveTask = new RetrieveFeedTask();
        //On lance la tâche asynchrone qui va aller chercher les données sur l'API
        retrieveTask.execute();
    }



    /**
     * Requête de base :
     * "http://api.openweathermap.org/data/2.5/weather?q=[VILLE_A_RENSEIGNER]&units=metric&APPID=b8de8503e7012ee121a886385eddf0c1";
     * "http://openweathermap.org/img/w/"+pictureName+".png"
     */
    private class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;
        private Bitmap picture_data;
        protected void onPreExecute() {
        }

        protected void prepareRequest(String ville){
            //TODO: un peu de validation
        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+city_to_look_for+"&units=metric&APPID=b8de8503e7012ee121a886385eddf0c1");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();

                    return stringBuilder.toString();
                }

                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                Log.e("ERROR", "LA REPONSE EST NULLE");
            }
            Log.i("INFO", response);
            JSONObject weather_report = null;
            try {
                weather_report = new JSONObject(response);
                Log.i("PARSED OBJECT", weather_report.get("weather").toString());
                resultVille.setText(weather_report.getString("name"));
                resultTemp.setText(weather_report.getJSONObject("main").getString("temp")+"°");
                resultWind.setText("Vent à "+weather_report.getJSONObject("wind").getString("speed")+" km/h");
                URL url2 = new URL("http://openweathermap.org/img/w/"+weather_report.getJSONArray("weather").getJSONObject(0).getString("icon")+".png");
                this.picture_data = BitmapFactory.decodeStream(url2.openConnection().getInputStream());
                resultIcon.setImageBitmap(this.picture_data);
                //On ne met visible que si ça a marché
                resultLayout.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //TextView txt = (TextView) findViewById(R.id.result_ville);
            //responseView.setText(response);
        }
    }
}
