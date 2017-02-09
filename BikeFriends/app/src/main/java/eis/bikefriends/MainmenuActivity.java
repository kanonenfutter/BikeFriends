package eis.bikefriends;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainmenuActivity extends AppCompatActivity implements View.OnClickListener{
//http://stackoverflow.com/questions/25905086/multiple-buttons-onclicklistener-android

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        Button LaunchEventsActivitybtn = (Button) findViewById(R.id.menuEvents);
        Button LaunchProfileActivitybtn = (Button) findViewById(R.id.menuMyProfile);
        Button LaunchMatchingActivitybtn = (Button) findViewById(R.id.menuMatching);
        Button LaunchSettingsActivitybtn = (Button) findViewById(R.id.menuSettings);
        Button LaunchCalibrationActivitybtn = (Button) findViewById(R.id.menuCalibration);
        Button LaunchDebug1Activitybtn = (Button) findViewById(R.id.menuDebug1);

        LaunchEventsActivitybtn.setOnClickListener(this);
        LaunchProfileActivitybtn.setOnClickListener(this);
        LaunchMatchingActivitybtn.setOnClickListener(this);
        LaunchSettingsActivitybtn.setOnClickListener(this);
        LaunchCalibrationActivitybtn.setOnClickListener(this);
        LaunchDebug1Activitybtn.setOnClickListener(this);

    }
    @Override
    public void onBackPressed() {
        return;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.menuMyProfile:
                Intent profileintent = new Intent(this, MyProfileActivity.class);
                MainmenuActivity.this.startActivity(profileintent);
                break;

            case R.id.menuEvents:
                Intent eventsintent = new Intent(this, EventsActivity.class);
                MainmenuActivity.this.startActivity(eventsintent);
                break;

            case R.id.menuMatching:
                Intent matchingintent = new Intent(this, MatchingActivity.class);
                MainmenuActivity.this.startActivity(matchingintent);
                break;
            case R.id.menuCalibration:
                Intent calibrationintent = new Intent(this, CalibrationActivity.class);
                MainmenuActivity.this.startActivity(calibrationintent);
                break;
            case R.id.menuSettings:
 /*               Intent settingsintent = new Intent(this, SettingsActivity.class);
                MainmenuActivity.this.startActivity(settingsintent);*/
                break;

            default:
                break;
        }
    }
}
