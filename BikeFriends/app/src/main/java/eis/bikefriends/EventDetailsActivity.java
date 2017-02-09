package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    String eventID, userID;
    private TextView eventIDTv, event_titleTv, event_startTv, event_destTv, event_timeTv, event_dateTv, event_descriptTv, event_organiser;
    String title, start, destination, time, date, description, organiser;
    SharedPreferences pref;
    Button teilnehmenbtn;
    ArrayList<HashMap<String, String>> resultsList;
    private ListView resultsLV;
    public final static String intentUserID = "eis.bikefriends.EventsActivity_intentUserID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        resultsList = new ArrayList<>();
        resultsLV = (ListView) findViewById(R.id.participantsLV);

        //Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Veranstaltungens Name");
        }
        event_titleTv = (TextView) findViewById(R.id.event_titleTv);
        event_startTv = (TextView) findViewById(R.id.event_startTv);
        event_destTv = (TextView) findViewById(R.id.event_destTv);
        event_timeTv = (TextView) findViewById(R.id.event_timeTv);
        event_dateTv = (TextView) findViewById(R.id.event_dateTv);
        event_descriptTv = (TextView) findViewById(R.id.event_descriptTv);
        eventIDTv = (TextView) findViewById(R.id.eventIDTv);
        event_organiser = (TextView) findViewById(R.id.event_organizerTV);

        //Shared Preferences aufrufen
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        final String ipAdresse = GlobalClass.getInstance().getIpAddresse();
        eventID = getIntent().getStringExtra(EventsActivity.eventID);


        //Benutzer trägt sich als Teilnehmer ein per Button
        teilnehmenbtn = (Button) findViewById(R.id.teilnehmenbtn);
        teilnehmenbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PutTeilnehmerTask().execute(ipAdresse + "/events/" + eventID + "/teilnehmer");
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });


        //Veranstaltungen vom Server laden und anzeigen
        new GetVeranstaltungTask().execute(ipAdresse + "/events/" + eventID);
    }

    //Toolbar back Button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    //Veranstaltungen vom Server laden und anzeigen
    class GetVeranstaltungTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progressDialog = new ProgressDialog(EventDetailsActivity.this);
            progressDialog.setMessage("Lade Veranstaltung...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return getVeranstaltung(params[0]);
            } catch (IOException ex) {
                return "Network error!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            //Prüfen, ob der Betrachter der Veranstalter ist. Wenn wahr, teilnehmenbtn verbergen


            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            //zuweisung der Strings zu den jeweiligen TextViews
            eventIDTv.setText("Event ID: " + eventID);
            event_titleTv.setText(title);
            event_startTv.setText("Start: " + start);
            event_destTv.setText("Ziel: " + destination);
            event_timeTv.setText("Zeit: " + time);
            event_dateTv.setText("Datum: " + date);
            event_descriptTv.setText("Beschreibung: " + description);
            event_organiser.setText("Veranstalter" + organiser);

            String pref_organiser = pref.getString("userID", null);

            /*if (organiser.equals(pref_organiser)){
                teilnehmenbtn.setVisibility(View.GONE);
            }*/

            if(getSupportActionBar()!=null){
                getSupportActionBar().setTitle(title);
            }


            //Arraylist Daten werden dem ListView zugewiesen
            ListAdapter adapter = new SimpleAdapter(
                    EventDetailsActivity.this, resultsList,
                    R.layout.participants_list_item, new String[]{"participant_username", "participant_userID"},
                    new int[]{R.id.teilnehmerName, R.id.userIDTV});

            resultsLV.setAdapter(adapter);

            //OnItemClick getEventID
            //Intent mit EventID auf MyProfileActivty
            resultsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent itemInten = new Intent(EventDetailsActivity.this, MyProfileActivity.class);
                    HashMap<String, String> selectEvent = new HashMap<>();
                    selectEvent = resultsList.get((int) id);

                    String uID = (String)selectEvent.get("participant_userID");
                    itemInten.putExtra(intentUserID, uID);
                    startActivity(itemInten);
                }
            });

        }


        private String getVeranstaltung(String urlPath) throws IOException {
            StringBuilder result = new StringBuilder();
            BufferedReader bufferedReader = null;

            try {
                //connect zum server
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(10000 /* milliseconds */);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");// set header
                urlConnection.connect();

                //Read data response
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                    Log.d("json", line);
                }

                try {

                    //Json Object auslesen
                    JSONObject jsonObj = new JSONObject(result.toString());
                    JSONObject event = jsonObj.getJSONObject("results");

                    title = event.getString("title");
                    start = event.getString("start");
                    destination = event.getString("destination");
                    date = event.getString("date");
                    time = event.getString("time");
                    description = event.getString("description");
                    organiser = event.getString("organiser");
                    Log.d("json", jsonObj.toString());
                    Log.d("jsonarray", "test");
                    JSONArray results = event.getJSONArray("participants");

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject r = results.getJSONObject(i);

                        String name = r.getString("participant_username");
                        String userid = r.getString("participant_userID");

                        //Teilnehmer werden einer HashMap zugewiesen
                        HashMap<String, String> teilnehmer = new HashMap<>();

                        teilnehmer.put("participant_userID", userid);
                        teilnehmer.put("participant_username", name);

                        resultsList.add(teilnehmer);
                        Log.d("jsonarray", name.toString());
                    }


                } catch (final JSONException e) {
                    Log.e("parsingError", "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            Log.d("json", result.toString());


            return result.toString();
        }
    }


    //Teilnehmer einem Event hinzufügen
    class PutTeilnehmerTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(EventDetailsActivity.this);
            progressDialog.setMessage("Updating data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return putTeilnehmer(params[0]);
            } catch (IOException ex) {
                return "Network error !";
            } catch (JSONException ex) {
                return "Data invalid !";
            }


        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //mResult.setText(result);

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        private String putTeilnehmer(String urlPath) throws IOException, JSONException {
            BufferedWriter bufferedWriter = null;
            String result = null;

            try {
                // create data to update
                String userID = pref.getString("userID", null);
                String userName = pref.getString("username", null);


                JSONObject dataToSend = new JSONObject();
                dataToSend.put("participant_userID", userID);
                dataToSend.put("participant_username",userName);
                Log.d("json to send",dataToSend.toString());
                //Initialize and config request, then connect to server
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(10000 /* milliseconds */);
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoOutput(true); //enable output (body data)
                urlConnection.setRequestProperty("Content-Type", "application/json");// set header
                urlConnection.connect();

                //write data into server
                OutputStream outputStream = urlConnection.getOutputStream();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(dataToSend.toString());
                bufferedWriter.flush();

                // check update successful or not
                if (urlConnection.getResponseCode() == 200) {
                    return "Anmeldung erfolgreich!";
                } else {
                    return "Anmeldung fehlgeschlagen!";
                }

            } finally {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }

        }

    }


}
