package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMainPage();
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        b.setAllCaps(false);
        return b;
    }

    private void showMainPage() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 40, 30, 30);

        TextView title = new TextView(this);
        title.setText("بورس‌یار");
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView sub = new TextView(this);
        sub.setText("دستیار تحلیل بورس ایران");
        sub.setTextSize(18);
        sub.setGravity(Gravity.CENTER);
        layout.addView(sub);

        String[] names = {
            "اطلاعات کلی بورس ایران",
            "ورود و خروج پول",
            "پول هوشمند",
            "تحلیل تکنیکال",
            "تحلیل بنیادی",
            "سهم‌های ارزنده",
            "بررسی سهام من"
        };

        for (final String name : names) {

            Button b = makeButton(name);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPage(name);
                }
            });

            layout.addView(b);
        }

        setContentView(layout);
    }

    private void showPage(String name) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 40, 30, 30);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView text = new TextView(this);
        text.setText("صفحه «" + name + "» با موفقیت باز شد.");
        text.setTextSize(20);
        text.setTextColor(Color.DKGRAY);
        text.setGravity(Gravity.CENTER);
        text.setPadding(10, 50, 10, 50);
        layout.addView(text);

        Button back = makeButton("بازگشت به صفحه اصلی");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainPage();
            }
        });

        layout.addView(back);

        setContentView(layout);
    }
}
