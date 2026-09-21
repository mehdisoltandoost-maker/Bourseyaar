package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView text = new TextView(this);
        text.setText("بورس‌یار");
        text.setTextSize(28);
        text.setTextColor(Color.BLACK);
        text.setGravity(Gravity.CENTER);

        setContentView(text);
    }
}
