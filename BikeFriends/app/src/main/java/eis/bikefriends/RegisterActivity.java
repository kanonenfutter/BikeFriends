package eis.bikefriends;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    Button signUpbtn;
    EditText usernameET, emailET, passwordET, bdateET, residenceET;
    RadioButton maleRB, femaleRB;
    RadioGroup genderRG;
    String bdateISO;
    Calendar calendar;
    DatePickerDialog.OnDateSetListener date;
    String ipAdresse, response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        calendar = Calendar.getInstance();

        usernameET = (EditText)findViewById(R.id.usernameET);
        emailET = (EditText)findViewById(R.id.emailET);
        passwordET = (EditText)findViewById(R.id.passwordET);
        bdateET = (EditText)findViewById(R.id.bdateET);
        bdateET.setOnClickListener(this);
        residenceET = (EditText)findViewById(R.id.residenceET);

        maleRB = (RadioButton)findViewById(R.id.maleRB);

        femaleRB = (RadioButton)findViewById(R.id.femaleRB);

        genderRG = (RadioGroup)findViewById(R.id.genderRG);

        signUpbtn = (Button)findViewById(R.id.signUpbtn);
        signUpbtn.setOnClickListener(this);

        ipAdresse = GlobalClass.getInstance().getIpAddresse();


        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };


        //Toolbar back
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Registrieren");
        }
    }

    //Toolbar back
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }



    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signUpbtn:
                new RegistrationTask().execute(ipAdresse + "/register");
                break;
            case R.id.bdateET:
                DatePickerDialog dialog = new DatePickerDialog(RegisterActivity.this, date,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
                break;
        }
    }
    private void updateLabel() {
        SimpleDateFormat sdf_iso = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'", Locale.getDefault());
        SimpleDateFormat sdf_display = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        bdateISO = sdf_iso.format(calendar.getTime());
        bdateET.setText(sdf_display.format(calendar.getTime()), TextView.BufferType.EDITABLE);
    }


    class RegistrationTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setMessage("Registriere neuen Benutzer...");
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
            Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_SHORT).show();
            if(response.equals("Sucessfully registered")){
                finish();
            }
        }

        private String postData(String urlPath) throws IOException, JSONException {

            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {
                //data erstellen
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("username", usernameET.getText().toString().trim());
                dataToSend.put("email", emailET.getText().toString().trim());
                dataToSend.put("password", passwordET.getText().toString().trim());
                dataToSend.put("bdate", bdateISO.toString());
                dataToSend.put("residence", residenceET.getText().toString().trim());
                Button temp = (Button)findViewById(genderRG.getCheckedRadioButtonId());
                dataToSend.put("gender", temp.getText().toString().trim());

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
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }

            }
            JSONObject json = new JSONObject(result.toString());
            Log.d("json", json.getString("response"));
            response = json.getString("response");
            Log.d("json", result.toString());
            /*if(response.equals("Sucessfully registered")){
                finish();
            }*/
            return result.toString();
        }
    }
}
