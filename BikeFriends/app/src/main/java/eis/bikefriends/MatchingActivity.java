package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MatchingActivity extends AppCompatActivity   {
    ListView resultsLV;
    ArrayList<HashMap<String, String>> resultsList;
    SharedPreferences pref;
    String ipAdresse, userID;
    public final static String intentMatchingID = "eis.bikefriends.MatchingActivity_matchID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);
        resultsList = new ArrayList<>();
        resultsLV = (ListView) findViewById(R.id.lvItems);
        ipAdresse = GlobalClass.getInstance().getIpAddresse();
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        userID = pref.getString("userID", null);

        new GetMatchesTask().execute(ipAdresse + "/profiles/" + userID + "/matches");
    }

    class GetMatchesTask extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progressDialog = new ProgressDialog(MatchingActivity.this);
            progressDialog.setMessage("Lade Matches...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return getMatchesTask(params[0]);
            } catch (IOException ex) {
                return "Network error!";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //TODO: resultslist
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            ListAdapter adapter = new SimpleAdapter(
                    MatchingActivity.this, resultsList,
                    R.layout.matchinglist_item, new String[]{"username", "age_gender", "residence", "id"},
                    new int[]{R.id.mNameTV, R.id.mAge_GenderTV, R.id.mResidenceTV});

            resultsLV.setAdapter(adapter);

            //OnItemClick getEventID
            //Intent mit EventID auf EventDetailsActivity
            resultsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent itemIntent = new Intent(MatchingActivity.this, MyProfileActivity.class);
                    HashMap<String, String> selectEvent = new HashMap<>();
                    selectEvent = resultsList.get((int) id);

                    String eID = (String)selectEvent.get("id");
                    Log.d("Match User ID:", eID);
                    itemIntent.putExtra(intentMatchingID, eID);
                    startActivity(itemIntent);
                }
            });
        }

        public String getMatchesTask(String urlPath) throws IOException {
            StringBuilder result = new StringBuilder();
            BufferedReader bufferedReader = null;

            try {
                //connection zum server
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

                    JSONObject jsonObj = new JSONObject(result.toString());

                    JSONArray results = jsonObj.getJSONArray("results");

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject r = results.getJSONObject(i);

                        String e_id = r.getString("_id");
                        String username = r.getString("username");

                        String birthdate = r.getString("birthdate");
                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                        Date bdate = dateformat.parse(birthdate);
                        int age = getAge(bdate);

                        String gender = r.getString("gender");
                        String age_gender =  age + ", " + gender;
                        String residence = r.getString("residence");

                        HashMap<String, String> match = new HashMap<>();

                        match.put("id", e_id);
                        match.put("username", username);
                        match.put("age_gender", age_gender);
                        match.put("residence", residence);

                        resultsList.add(match);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
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
    private int getAge(Date bdate){
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.setTime(bdate);
        int age = currentYear - calendar.get(Calendar.YEAR);
        if(age < 0)
            throw new IllegalArgumentException("Ungültiges Alter: age < 0");
        return age;
    }
}
