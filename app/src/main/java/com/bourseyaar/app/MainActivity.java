package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSocketFactory;

import java.security.cert.X509Certificate;

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

        ScrollView scroll = new ScrollView(this);

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        TextView header = title("بورس‌یار");
        header.setBackgroundColor(Color.rgb(30, 100, 180));
        layout.addView(header);

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "دستیار تحلیل بازار بورس ایران\n" +
                "اطلاعات بازار، بنیادی، تکنیکال و پول هوشمند");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(10, 20, 10, 20);
        layout.addView(subtitle);

        Button market =
                menuButton("📊 اطلاعات کلی بورس ایران");

        Button fundamental =
                menuButton("💰 بهترین نمادها از نظر بنیادی");

        Button technical =
                menuButton("📈 تحلیل تکنیکال");

        Button smartMoney =
                menuButton("💵 پول هوشمند");

        Button flow =
                menuButton("🔄 ورود و خروج پول");

        Button valuable =
                menuButton("⭐ سهم‌های ارزنده");

        Button portfolio =
                menuButton("📁 بررسی سهام‌های من");

        layout.addView(market);
        layout.addView(fundamental);
        layout.addView(technical);
        layout.addView(smartMoney);
        layout.addView(flow);
        layout.addView(valuable);
        layout.addView(portfolio);

        market.setOnClickListener(
                v -> showMarketPage());

        fundamental.setOnClickListener(
                v -> showPage(
                        "بهترین نمادها از نظر بنیادی",
                        "در این بخش نمادها بر اساس اطلاعات بنیادی بررسی می‌شوند.\n\n" +
                        "EPS\n\n" +
                        "P/E\n\n" +
                        "سودآوری\n\n" +
                        "رشد فروش\n\n" +
                        "ارزش ذاتی"));

        technical.setOnClickListener(
                v -> showPage(
                        "تحلیل تکنیکال",
                        "ابزارهای تحلیل تکنیکال:\n\n" +
                        "RSI\n\n" +
                        "MACD\n\n" +
                        "میانگین متحرک\n\n" +
                        "حمایت و مقاومت\n\n" +
                        "روند سهم"));

        smartMoney.setOnClickListener(
                v -> showPage(
                        "پول هوشمند",
                        "در این بخش ورود پول هوشمند و افزایش حجم معاملات بررسی می‌شود.\n\n" +
                        "حجم معاملات\n\n" +
                        "قدرت خریدار\n\n" +
                        "سرانه خرید\n\n" +
                        "سرانه فروش"));

        flow.setOnClickListener(
                v -> showPage(
                        "ورود و خروج پول",
                        "بررسی جریان نقدینگی بازار و نمادها.\n\n" +
                        "ورود پول حقیقی\n\n" +
                        "خروج پول حقیقی\n\n" +
                        "تغییرات نقدینگی"));

        valuable.setOnClickListener(
                v -> showPage(
                        "سهم‌های ارزنده",
                        "ترکیب تحلیل بنیادی و تکنیکال برای شناسایی نمادهای مناسب.\n\n" +
                        "درصد رشد\n\n" +
                        "قدرت خریدار\n\n" +
                        "ارزش معاملات\n\n" +
                        "وضعیت بنیادی"));

        portfolio.setOnClickListener(
                v -> showPage(
                        "سهام‌های من",
                        "در این بخش می‌توان سبد سهام را بررسی کرد.\n\n" +
                        "قیمت خرید\n\n" +
                        "قیمت فعلی\n\n" +
                        "سود و زیان\n\n" +
                        "درصد بازدهی"));

        scroll.addView(layout);
        setContentView(scroll);
    }

    private void showMarketPage() {

        ScrollView scroll = new ScrollView(this);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(20, 20, 20, 20);

        TextView header =
                title("اطلاعات کلی بورس ایران");

        header.setBackgroundColor(
                Color.rgb(30, 100, 180));

        page.addView(header);

        TextView status =
                new TextView(this);

        status.setText(
                "⏳ در حال دریافت اطلاعات بازار...");

        status.setTextSize(18);
        status.setPadding(15, 30, 15, 30);

        page.addView(status);

        Button refresh =
                new Button(this);

        refresh.setText(
                "🔄 دریافت اطلاعات");

        refresh.setAllCaps(false);

        page.addView(refresh);

        Button back =
                new Button(this);

        back.setText("⬅ بازگشت");
        back.setAllCaps(false);

        page.addView(back);

        refresh.setOnClickListener(
                v -> loadMarketData(status));

        back.setOnClickListener(
                v -> showMainPage());

        scroll.addView(page);

        setContentView(scroll);

        loadMarketData(status);
    }

    private SSLSocketFactory createTrustAllSocketFactory()
            throws Exception {

        TrustManager[] trustAllCerts =
                new TrustManager[]{
                        new X509TrustManager() {

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }

                            @Override
                            public void checkClientTrusted(
                                    X509Certificate[] chain,
                                    String authType) {
                            }

                            @Override
                            public void checkServerTrusted(
                                    X509Certificate[] chain,
                                    String authType) {
                            }
                        }
                };

        SSLContext sslContext =
                SSLContext.getInstance("TLS");

        sslContext.init(
                null,
                trustAllCerts,
                new java.security.SecureRandom());

        return sslContext.getSocketFactory();
    }

    private String formatNumber(double value) {

        if (Double.isNaN(value)) {
            return "—";
        }

        DecimalFormatSymbols symbols =
                new DecimalFormatSymbols(Locale.US);

        DecimalFormat format =
                new DecimalFormat(
                        "#,##0.##",
                        symbols);

        return format.format(value);
    }

    private String getNumber(
            JSONObject item,
            String key) {

        if (!item.has(key) ||
                item.isNull(key)) {

            return "—";
        }

        double value =
                item.optDouble(
                        key,
                        Double.NaN);

        return formatNumber(value);
    }

    private String getPercent(
            JSONObject item,
            String key) {

        if (!item.has(key) ||
                item.isNull(key)) {

            return "—";
        }

        double value =
                item.optDouble(
                        key,
                        Double.NaN);

        if (Double.isNaN(value)) {
            return "—";
        }

        return formatNumber(value) + "%";
    }

    private String getUpdateTime(
            JSONObject item) {

        int time =
                item.optInt(
                        "hEven",
                        0);

        if (time <= 0) {
            return "—";
        }

        int hour =
                time / 10000;

        int minute =
                (time / 100) % 100;

        int second =
                time % 100;

        return String.format(
                Locale.US,
                "%02d:%02d:%02d",
                hour,
                minute,
                second);
    }

    private void loadMarketData(
            TextView status) {

        status.setText(
                "⏳ در حال دریافت اطلاعات بازار...");

        new Thread(() -> {

            String result;

            try {

                URL url =
                        new URL(
                                "https://cdn.tsetmc.com/api/Index/GetIndexB1LastAll/SelectedIndexes/1");

                HttpsURLConnection connection =
                        (HttpsURLConnection)
                                url.openConnection();

                connection.setSSLSocketFactory(
                        createTrustAllSocketFactory());

                connection.setHostnameVerifier(
                        (hostname, session) -> true);

                connection.setRequestMethod("GET");

                connection.setConnectTimeout(
                        15000);

                connection.setReadTimeout(
                        15000);

                connection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0");

                connection.setRequestProperty(
                        "Accept",
                        "application/json");

                int responseCode =
                        connection.getResponseCode();

                if (responseCode < 200 ||
                        responseCode >= 300) {

                    throw new Exception(
                            "HTTP " +
                            responseCode);
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream()));

                StringBuilder builder =
                        new StringBuilder();

                String line;

                while ((line =
                        reader.readLine()) != null) {

                    builder.append(line);
                }

                reader.close();
                connection.disconnect();

                JSONObject root =
                        new JSONObject(
                                builder.toString());

                JSONArray indexes =
                        root.optJSONArray(
                                "indexB1");

                if (indexes == null ||
                        indexes.length() == 0) {

                    throw new Exception(
                            "داده‌ای از TSETMC دریافت نشد");
                }

                StringBuilder text =
                        new StringBuilder();

                text.append(
                        "📊 وضعیت فعلی بازار\n\n");

                for (int i = 0;
                        i < indexes.length();
                        i++) {

                    JSONObject item =
                            indexes.getJSONObject(i);

                    String name =
                            item.optString(
                                    "lVal30",
                                    "شاخص");

                    String value =
                            getNumber(
                                    item,
                                    "xDrNivJIdx004");

                    String change =
                            getNumber(
                                    item,
                                    "indexChange");

                    String percent =
                            getPercent(
                                    item,
                                    "xVarIdxJRfV");

                    String time =
                            getUpdateTime(
                                    item);

                    text.append(
                            "━━━━━━━━━━━━━━━━━━\n");

                    text.append("📈 ")
                            .append(name)
                            .append("\n\n");

                    text.append(
                            "مقدار شاخص: ")
                            .append(value)
                            .append("\n");

                    text.append(
                            "تغییر: ")
                            .append(change)
                            .append("\n");

                    text.append(
                            "درصد تغییر: ")
                            .append(percent)
                            .append("\n");

                    text.append(
                            "آخرین بروزرسانی: ")
                            .append(time)
                            .append("\n\n");
                }

                text.append(
                        "━━━━━━━━━━━━━━━━━━\n\n");

                text.append(
                        "✅ اطلاعات با موفقیت از TSETMC دریافت شد.");

                result =
                        text.toString();

            } catch (Exception e) {

                result =
                        "❌ دریافت اطلاعات بورس انجام نشد.\n\n" +
                        "نوع خطا: " +
                        e.getClass()
                                .getSimpleName() +
                        "\n\n" +
                        "شرح خطا: " +
                        e.getMessage();
            }

            final String finalResult =
                    result;

            runOnUiThread(
                    () -> status.setText(
                            finalResult));

        }).start();
    }

    private void showPage(
            String pageTitle,
            String text) {

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout page =
                new LinearLayout(this);

        page.setOrientation(
                LinearLayout.VERTICAL);

        page.setPadding(
                20, 20, 20, 20);

        TextView header =
                title(pageTitle);

        header.setBackgroundColor(
                Color.rgb(30, 100, 180));

        page.addView(header);

        TextView content =
                new TextView(this);

        content.setText(text);
        content.setTextSize(18);
        content.setPadding(
                15, 35, 15, 35);

        page.addView(content);

        Button back =
                new Button(this);

        back.setText(
                "⬅ بازگشت");

        back.setAllCaps(false);

        page.addView(back);

        back.setOnClickListener(
                v -> showMainPage());

        scroll.addView(page);

        setContentView(scroll);
    }
}
