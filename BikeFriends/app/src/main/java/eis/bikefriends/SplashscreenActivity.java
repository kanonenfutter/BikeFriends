package eis.bikefriends;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashscreenActivity extends AppCompatActivity {
    private static int splash_time = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                Intent mainIntent = new Intent(SplashscreenActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, splash_time);
    }

}
