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

    private LinearLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMainPage();
    }

    private TextView makeTitle(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setPadding(10, 20, 10, 20);
        return title;
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setPadding(10, 10, 10, 10);
        return button;
    }

    private void showMainPage() {

        main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(30, 40, 30, 30);

        main.addView(makeTitle("بورس‌یار"));

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

        for (final String menu : menus) {
            Button button = makeButton(menu);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPage(menu);
                }
            });

            main.addView(button);
        }

        setContentView(main);
    }

    private void showPage(String pageTitle) {

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(30, 40, 30, 30);

        page.addView(makeTitle(pageTitle));

        TextView info = new TextView(this);
        info.setText("این بخش از بورس‌یار آماده است.\n\n"
                + "در مرحله بعد اطلاعات واقعی بازار، "
                + "تحلیل تکنیکال و بنیادی به آن اضافه می‌شود.");
        info.setTextSize(18);
        info.setTextColor(Color.DKGRAY);
        info.setGravity(Gravity.CENTER);
        info.setPadding(10, 30, 10, 30);
        page.addView(info);

        Button backButton = makeButton("بازگشت به صفحه اصلی");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainPage();
            }
        });

        page.addView(backButton);

        setContentView(page);
    }
}
