package www.a2adesign.net.sindikatzdravstva.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import www.a2adesign.net.sindikatzdravstva.R;
import www.a2adesign.net.sindikatzdravstva.activity.MainActivity;

public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread splashScreenThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        splashScreenThread.start();
    }
}
