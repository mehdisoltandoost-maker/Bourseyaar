package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(30, 40, 30, 30);

        TextView title = new TextView(this);
        title.setText("بورس‌یار");
        title.setTextSize(32);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);

        main.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("دستیار تحلیل بورس ایران");
        subtitle.setTextSize(18);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 10, 0, 30);

        main.addView(subtitle);

        String[] menus = {
            "اطلاعات کلی بورس ایران",
            "ورود و خروج پول",
            "پول هوشمند",
            "تحلیل تکنیکال",
            "تحلیل بنیادی",
            "سهم‌های ارزنده",
            "بررسی سهام من"
        };

        for (String menu : menus) {
            Button button = new Button(this);
            button.setText(menu);
            button.setTextSize(16);
            button.setAllCaps(false);

            main.addView(button);
        }

        setContentView(main);
    }
}
