package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {

    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMainPage();
    }

    private TextView title(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(24);
        t.setTextColor(Color.WHITE);
        t.setGravity(Gravity.CENTER);
        t.setPadding(10, 25, 10, 25);
        return t;
    }

    private Button menuButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(17);
        b.setAllCaps(false);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        p.setMargins(0, 8, 0, 8);
        b.setLayoutParams(p);

        return b;
    }

    private void showMainPage() {

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        TextView header = title("بورس‌یار");
        header.setBackgroundColor(Color.rgb(30, 100, 180));
        layout.addView(header);

        Button market = menuButton("📊 اطلاعات کلی بورس ایران");
        Button fundamental = menuButton("💰 بهترین نمادها از نظر بنیادی");
        Button technical = menuButton("📈 تحلیل تکنیکال");
        Button smartMoney = menuButton("💵 پول هوشمند");
        Button flow = menuButton("🔄 ورود و خروج پول");
        Button valuable = menuButton("⭐ سهم‌های ارزنده");
        Button portfolio = menuButton("📁 بررسی سهام‌های من");

        layout.addView(market);
        layout.addView(fundamental);
        layout.addView(technical);
        layout.addView(smartMoney);
        layout.addView(flow);
        layout.addView(valuable);
        layout.addView(portfolio);

        market.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMarketPage();
            }
        });

        fundamental.setOnClickListener(v ->
                showPage("بهترین نمادها از نظر بنیادی",
                        "این بخش در مرحله بعد به اطلاعات بنیادی متصل می‌شود."));

        technical.setOnClickListener(v ->
                showPage("تحلیل تکنیکال",
                        "RSI\n\nMACD\n\nمیانگین متحرک\n\nحمایت و مقاومت"));

        smartMoney.setOnClickListener(v ->
                showPage("پول هوشمند",
                        "این بخش در مرحله بعد به داده‌های بازار متصل می‌شود."));

        flow.setOnClickListener(v ->
                showPage("ورود و خروج پول",
                        "این بخش در مرحله بعد به داده‌های بازار متصل می‌شود."));

        valuable.setOnClickListener(v ->
                showPage("سهم‌های ارزنده",
                        "ترکیب تحلیل بنیادی و تکنیکال در مرحله بعد اضافه می‌شود."));

        portfolio.setOnClickListener(v ->
                showPage("سهام‌های من",
                        "بررسی سبد سهام در مرحله بعد اضافه می‌شود."));

        setContentView(layout);
    }

    private void showMarketPage() {

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(20, 20, 20, 20);

        TextView header = title("اطلاعات کلی بورس ایران");
        header.setBackgroundColor(Color.rgb(30, 100, 180));
        page.addView(header);

        TextView info = new TextView(this);
        info.setText(
                "اتصال اینترنت برقرار است.\n\n" +
                "برای آزمایش دسترسی به اطلاعات بورس، " +
                "دکمه زیر را بزنید:"
        );
        info.setTextSize(18);
        info.setPadding(15, 30, 15, 30);
        page.addView(info);

        Button tsetmc = new Button(this);
        tsetmc.setText("🌐 باز کردن TSETMC");
        tsetmc.setTextSize(17);
        tsetmc.setAllCaps(false);
        page.addView(tsetmc);

        Button back = new Button(this);
        back.setText("⬅ بازگشت");
        back.setAllCaps(false);
        page.addView(back);

        tsetmc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.tsetmc.com/")
                    );
                    startActivity(intent);
                } catch (Exception e) {
                    info.setText(
                            "❌ باز کردن سایت انجام نشد.\n\n" +
                            "لطفاً مرورگر گوشی را بررسی کنید."
                    );
                }
            }
        });

        back.setOnClickListener(v -> showMainPage());

        setContentView(page);
    }

    private void showPage(String pageTitle, String text) {

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(20, 20, 20, 20);

        TextView header = title(pageTitle);
        header.setBackgroundColor(Color.rgb(30, 100, 180));
        page.addView(header);

        TextView content = new TextView(this);
        content.setText(text);
        content.setTextSize(18);
        content.setPadding(15, 35, 15, 35);
        page.addView(content);

        Button back = new Button(this);
        back.setText("⬅ بازگشت");
        back.setAllCaps(false);
        page.addView(back);

        back.setOnClickListener(v -> showMainPage());

        setContentView(page);
    }
}
