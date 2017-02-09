package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;


public class NewLoginActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    Button login;
    String emailString;
    String passwordString;
    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_login);

        //Toolbar
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle("Login");
        }

        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        login = (Button)findViewById(R.id.Login_btn);

        Button registerBtn = (Button) findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewLoginActivity.this, RegisterActivity.class);
                NewLoginActivity.this.startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailString = email.getText().toString();
                passwordString = password.getText().toString();
                String ipAdresse = GlobalClass.getInstance().getIpAddresse();

                new PostDataTask().execute(ipAdresse + "/profiles");
            }
        });
    }

    class PostDataTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(NewLoginActivity.this);
            progressDialog.setMessage("Inserting data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return postData(params[0]);
            } catch (IOException ex) {
                return "Network error!";
            } catch (JSONException ex) {
                return "Data Invalid!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        private String postData(String urlPath) throws IOException, JSONException {

            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

/*            inputTitle = (EditText) findViewById(R.id.titelET);
            inputBeschreibung = (EditText) findViewById(R.id.beschreibungET);
            //final EditText inputDate = (EditText) findViewById(R.id.datumET);
            inputDate = (TextView) findViewById((R.id.dateTV));
            inputTime = (EditText) findViewById(R.id.timeET);
            inputDestination = (EditText) findViewById(R.id.zielET);*/
            int inputID = 0;

            try {
                //data erstellen
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("email", emailString);
                dataToSend.put("login", passwordString);

                //connect zum server
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(10000 /* milliseconds */);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true); //enable output (body data)
                urlConnection.setRequestProperty("Content-Type", "application/json");// set header
                urlConnection.connect();

                //Write data
                OutputStream outputStream = urlConnection.getOutputStream();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(dataToSend.toString());
                bufferedWriter.flush();

                //Read data
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                    Log.d("json", line.toString());
                }

                try
                {
                    JSONObject jsonObj = new JSONObject(result.toString());
                    if(jsonObj != null) {
                        try {
                            String jsonstr = jsonObj.getString("response");
                            if (jsonObj.getBoolean("res")) {
                                String token = jsonObj.getString("token");
                                String grav = jsonObj.getString("grav");
                                SharedPreferences.Editor edit = pref.edit();
                                //Speicher Data in SharedPreferences
                                edit.putString("token", token);
                                edit.putString("grav", grav);
                                edit.commit();
                                Intent loginIntent = new Intent(NewLoginActivity.this, MainmenuActivity.class);

                                startActivity(loginIntent);
                                finish();
                            }

                            Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }catch (final JSONException e) {
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
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }

            }

            Log.d("json", result.toString());
            return result.toString();
        }
    }
}
