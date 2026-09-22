package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    LinearLayout layout;

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
        t.setPadding(10, 30, 10, 30);
        return t;
    }

    private Button menuButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(17);
        b.setAllCaps(false);
        b.setPadding(10, 15, 10, 15);
        return b;
    }

    private void showMainPage() {

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

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

        market.setOnClickListener(v ->
                showPage("اطلاعات کلی بورس ایران",
                        "شاخص کل\nشاخص هم‌وزن\nارزش معاملات\nحجم معاملات"));

        fundamental.setOnClickListener(v ->
                showPage("بهترین نمادها از نظر بنیادی",
                        "در این بخش نمادها بر اساس اطلاعات بنیادی بررسی می‌شوند."));

        technical.setOnClickListener(v ->
                showPage("تحلیل تکنیکال",
                        "RSI\nMACD\nمیانگین متحرک\nحمایت و مقاومت"));

        smartMoney.setOnClickListener(v ->
                showPage("پول هوشمند",
                        "بررسی ورود پول هوشمند به نمادها."));

        flow.setOnClickListener(v ->
                showPage("ورود و خروج پول",
                        "بررسی ورود و خروج پول حقیقی."));

        valuable.setOnClickListener(v ->
                showPage("سهم‌های ارزنده",
                        "ترکیب تحلیل بنیادی و تکنیکال برای شناسایی نمادهای ارزنده."));

        portfolio.setOnClickListener(v ->
                showPage("سهام‌های من",
                        "در این بخش می‌توان سهام‌های موجود در سبد شما را بررسی کرد."));

        setContentView(layout);
    }

    private void showPage(String pageTitle, String text) {

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(25, 25, 25, 25);

        TextView header = title(pageTitle);
        header.setBackgroundColor(Color.rgb(30, 100, 180));

        page.addView(header);

        TextView content = new TextView(this);
        content.setText(text);
        content.setTextSize(18);
        content.setPadding(15, 40, 15, 40);

        page.addView(content);

        Button back = new Button(this);
        back.setText("⬅ بازگشت");
        back.setTextSize(17);
        back.setAllCaps(false);

        page.addView(back);

        back.setOnClickListener(v -> showMainPage());

        setContentView(page);
    }
}
