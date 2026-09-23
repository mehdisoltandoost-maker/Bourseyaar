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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

        TextView status = new TextView(this);
        status.setText("⏳ در حال دریافت شاخص‌های بورس...");
        status.setTextSize(18);
        status.setPadding(15, 30, 15, 30);
        page.addView(status);

        Button refresh = new Button(this);
        refresh.setText("🔄 دریافت اطلاعات");
        refresh.setAllCaps(false);
        page.addView(refresh);

        Button back = new Button(this);
        back.setText("⬅ بازگشت");
        back.setAllCaps(false);
        page.addView(back);

        refresh.setOnClickListener(v -> loadMarketData(status));

        back.setOnClickListener(v -> showMainPage());

        setContentView(page);

        loadMarketData(status);
    }

    private void loadMarketData(TextView status) {

        status.setText("⏳ در حال دریافت شاخص‌های بورس...");

        new Thread(new Runnable() {
            @Override
            public void run() {

                String result;

                try {

                    URL url = new URL(
                            "https://cdn.tsetmc.com/api/Index/GetIndexB1LastAll/SelectedIndexes/1"
                    );

                    HttpURLConnection connection =
                            (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0"
                    );

                    int responseCode = connection.getResponseCode();

                    if (responseCode < 200 || responseCode >= 300) {
                        throw new Exception(
                                "HTTP " + responseCode
                        );
                    }

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            connection.getInputStream()
                                    )
                            );

                    StringBuilder builder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    reader.close();
                    connection.disconnect();

                    JSONObject root =
                            new JSONObject(builder.toString());

                    JSONArray indexes =
                            root.optJSONArray("indexB1");

                    if (indexes == null || indexes.length() == 0) {
                        throw new Exception("داده‌ای دریافت نشد");
                    }

                    StringBuilder text =
                            new StringBuilder();

                    text.append("📊 اطلاعات بازار\n\n");

                    for (int i = 0; i < indexes.length(); i++) {

                        JSONObject item =
                                indexes.getJSONObject(i);

                        String name =
                                item.optString(
                                        "lVal30",
                                        "شاخص"
                                );

                        String value =
                                item.optString(
                                        "xVal",
                                        ""
                                );

                        String change =
                                item.optString(
                                        "xVarIdx",
                                        ""
                                );

                        if (name.contains("کل") ||
                                name.contains("هم وزن")) {

                            text.append("📈 ")
                                    .append(name)
                                    .append("\n");

                            text.append("مقدار: ")
                                    .append(value)
                                    .append("\n");

                            text.append("تغییر: ")
                                    .append(change)
                                    .append("\n\n");
                        }
                    }

                    if (text.toString().equals(
                            "📊 اطلاعات بازار\n\n")) {

                        text.append(
                                "داده دریافت شد، " +
                                "اما نام شاخص‌ها قابل شناسایی نبود."
                        );
                    }

                    result = text.toString();

                } catch (Exception e) {

                    result =
                            "❌ دریافت اطلاعات بورس انجام نشد.\n\n" +
                            "ممکن است سرویس TSETMC از این اتصال " +
                            "قابل دسترسی نباشد.\n\n" +
                            "خطا: " + e.getMessage();
                }

                final String finalResult = result;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        status.setText(finalResult);
                    }
                });
            }
        }).start();
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
