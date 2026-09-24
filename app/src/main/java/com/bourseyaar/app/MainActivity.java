package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int blue = Color.rgb(25, 90, 160);
    private int darkBlue = Color.rgb(15, 55, 105);
    private int lightBlue = Color.rgb(235, 243, 252);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(darkBlue);

        showMainPage();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    private TextView makeTitle(String text) {

        TextView title = new TextView(this);

        title.setText(text);
        title.setTextSize(23);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(dp(10), dp(18), dp(10), dp(18));
        title.setBackgroundColor(blue);

        return title;
    }

    private TextView makeText(String text) {

        TextView tv = new TextView(this);

        tv.setText(text);
        tv.setTextSize(17);
        tv.setTextColor(Color.rgb(35, 35, 35));
        tv.setPadding(dp(16), dp(18), dp(16), dp(18));

        return tv;
    }

    private Button makeButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setTextColor(Color.rgb(30, 30, 30));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, dp(5), 0, dp(5));

        button.setLayoutParams(params);

        return button;
    }

    private LinearLayout makePageLayout() {

        LinearLayout page = new LinearLayout(this);

        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(12), dp(12), dp(12), dp(20));
        page.setBackgroundColor(Color.WHITE);

        return page;
    }

    private ScrollView makeScrollView(LinearLayout page) {

        ScrollView scroll = new ScrollView(this);

        scroll.setFillViewport(true);
        scroll.addView(page);

        return scroll;
    }

    private void addSpace(LinearLayout layout, int height) {

        TextView space = new TextView(this);

        space.setText("");
        space.setHeight(dp(height));

        layout.addView(space);
    }

    private void showMainPage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("بورس‌یار"));

        TextView intro = makeText(
                "دستیار تحلیل بازار سرمایه ایران\n\n" +
                "اطلاعات بازار، پول هوشمند، ورود و خروج پول، " +
                "تحلیل بنیادی و تکنیکال"
        );

        intro.setGravity(Gravity.CENTER);
        page.addView(intro);

        Button market =
                makeButton("📊 اطلاعات کلی بورس ایران");

        Button smartMoney =
                makeButton("💵 پول هوشمند");

        Button moneyFlow =
                makeButton("🔄 ورود و خروج پول");

        Button fundamental =
                makeButton("💰 تحلیل بنیادی");

        Button technical =
                makeButton("📈 تحلیل تکنیکال");

        Button symbols =
                makeButton("🔎 بررسی نمادها");

        Button valuable =
                makeButton("⭐ سهم‌های ارزنده");

        Button portfolio =
                makeButton("📁 بررسی سهام‌های من");

        Button suggestions =
                makeButton("💡 پیشنهادهای معاملاتی");

        page.addView(market);
        page.addView(smartMoney);
        page.addView(moneyFlow);
        page.addView(fundamental);
        page.addView(technical);
        page.addView(symbols);
        page.addView(valuable);
        page.addView(portfolio);
        page.addView(suggestions);

        market.setOnClickListener(v -> showMarketPage());

        smartMoney.setOnClickListener(v -> showSmartMoneyPage());

        moneyFlow.setOnClickListener(v -> showMoneyFlowPage());

        fundamental.setOnClickListener(v -> showFundamentalPage());

        technical.setOnClickListener(v -> showTechnicalPage());

        symbols.setOnClickListener(v -> showSymbolsPage());

        valuable.setOnClickListener(v -> showValuablePage());

        portfolio.setOnClickListener(v -> showPortfolioPage());

        suggestions.setOnClickListener(v -> showSuggestionsPage());

        setContentView(makeScrollView(page));
    }

    private void showMarketPage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("📊 اطلاعات کلی بورس ایران"));

        final TextView status =
                makeText("⏳ در حال دریافت اطلاعات بازار...");

        status.setBackgroundColor(lightBlue);

        page.addView(status);

        Button refresh =
                makeButton("🔄 دریافت مجدد اطلاعات");

        Button back =
                makeButton("⬅ بازگشت");

        page.addView(refresh);
        page.addView(back);

        refresh.setOnClickListener(v -> {
            status.setText("⏳ در حال دریافت اطلاعات از TSETMC...");
            loadMarketData(status);
        });

        back.setOnClickListener(v -> showMainPage());

        setContentView(makeScrollView(page));

        loadMarketData(status);
    }

    private void loadMarketData(final TextView status) {

        new Thread(() -> {

            String result;

            HttpURLConnection connection = null;

            try {

                URL url = new URL(
                        "https://cdn.tsetmc.com/api/Index/GetIndexB1LastAll/SelectedIndexes/1"
                );

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setUseCaches(false);

                connection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                int responseCode =
                        connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    inputStream =
                            connection.getInputStream();

                } else {

                    inputStream =
                            connection.getErrorStream();

                    String errorText =
                            readStream(inputStream);

                    throw new Exception(
                            "HTTP " +
                            responseCode +
                            "\n" +
                            errorText
                    );
                }

                String json =
                        readStream(inputStream);

                if (json == null ||
                        json.trim().length() == 0) {

                    throw new Exception(
                            "پاسخ خالی از سرور دریافت شد."
                    );
                }

                result =
                        parseMarketData(json);

            } catch (Exception e) {

                String message =
                        e.getMessage();

                if (message == null ||
                        message.length() == 0) {

                    message =
                            e.getClass().getSimpleName();
                }

                result =
                        "❌ دریافت اطلاعات انجام نشد.\n\n" +
                        "علت:\n" +
                        message +
                        "\n\n" +
                        "اگر اینترنت یا سرویس TSETMC " +
                        "در دسترس نباشد، برنامه همچنان " +
                        "قابل استفاده است.";
            }

            final String finalResult = result;

            mainHandler.post(() ->
                    status.setText(finalResult)
            );

            if (connection != null) {
                connection.disconnect();
            }

        }).start();
    }

    private String readStream(InputStream stream)
            throws Exception {

        if (stream == null) {
            return "";
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                "UTF-8"
                        )
                );

        StringBuilder builder =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        reader.close();

        return builder.toString();
    }

    private String parseMarketData(String json)
            throws Exception {

        JSONObject root =
                new JSONObject(json);

        JSONArray indexes =
                root.optJSONArray("indexB1");

        if (indexes == null ||
                indexes.length() == 0) {

            return "⚠️ پاسخ دریافت شد اما " +
                    "اطلاعات شاخص در آن پیدا نشد.";
        }

        StringBuilder result =
                new StringBuilder();

        result.append("📊 اطلاعات بازار\n\n");

        boolean found = false;

        for (int i = 0;
             i < indexes.length();
             i++) {

            JSONObject item =
                    indexes.optJSONObject(i);

            if (item == null) {
                continue;
            }

            String name =
                    item.optString(
                            "lVal30",
                            ""
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
                    name.contains("هم وزن") ||
                    name.contains("هم‌وزن")) {

                found = true;

                result.append("📈 ")
                        .append(name)
                        .append("\n");

                if (value.length() > 0) {

                    result.append("مقدار: ")
                            .append(value)
                            .append("\n");
                }

                if (change.length() > 0) {

                    result.append("تغییر: ")
                            .append(change)
                            .append("\n");
                }

                result.append("\n");
            }
        }

        if (!found) {

            result.append(
                    "داده بازار دریافت شد، " +
                    "اما شاخص‌های اصلی " +
                    "قابل شناسایی نبودند."
            );
        }

        return result.toString();
    }

    private void showSmartMoneyPage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("💵 پول هوشمند"));

        page.addView(makeText(
                "پول هوشمند\n\n" +
                "در این بخش نمادهایی که احتمال ورود " +
                "نقدینگی غیرعادی دارند بررسی می‌شوند.\n\n" +
                "معیارها:\n\n" +
                "• قدرت خریدار حقیقی\n" +
                "• حجم معاملات\n" +
                "• نسبت حجم به میانگین حجم\n" +
                "• سرانه خرید و فروش\n" +
                "• ارزش معاملات\n" +
                "• ورود نقدینگی حقیقی\n\n" +
                "این بخش در مرحله بعد به داده واقعی بازار متصل می‌شود."
        ));

        addBackButton(page);
    }

    private void showMoneyFlowPage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("🔄 ورود و خروج پول"));

        page.addView(makeText(
                "ورود و خروج پول حقیقی\n\n" +
                "موارد قابل بررسی:\n\n" +
                "🟢 ورود پول حقیقی\n" +
                "🔴 خروج پول حقیقی\n" +
                "📊 ارزش معاملات\n" +
                "👤 سرانه خرید\n" +
                "👤 سرانه فروش\n" +
                "⚖ قدرت خریدار به فروشنده\n\n" +
                "داده واقعی بازار در مرحله بعد اضافه می‌شود."
        ));

        addBackButton(page);
    }

    private void showFundamentalPage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("💰 تحلیل بنیادی"));

        page.addView(makeText(
                "تحلیل بنیادی نمادها\n\n" +
                "معیارهای اصلی:\n\n" +
                "• EPS\n" +
                "• P/E\n" +
                "• رشد سودآوری\n" +
                "• فروش شرکت\n" +
                "• حاشیه سود\n" +
                "• ارزش بازار\n" +
                "• نسبت قیمت به ارزش دفتری\n" +
                "• وضعیت صنعت\n\n" +
                "اطلاعات بنیادی از گزارش‌های رسمی کدال استخراج خواهد شد."
        ));

        addBackButton(page);
    }

    private void showTechnicalPage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("📈 تحلیل تکنیکال"));

        page.addView(makeText(
                "تحلیل تکنیکال\n\n" +
                "اندیکاتورها:\n\n" +
                "• RSI\n" +
                "• MACD\n" +
                "• میانگین متحرک\n" +
                "• حمایت و مقاومت\n" +
                "• حجم معاملات\n" +
                "• روند قیمت\n\n" +
                "این بخش در مرحله بعد به داده قیمت و حجم متصل می‌شود."
        ));

        addBackButton(page);
    }

    private void showSymbolsPage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("🔎 بررسی نمادها"));

        page.addView(makeText(
                "بررسی نماد\n\n" +
                "در این قسمت می‌توان نماد شرکت را بررسی کرد.\n\n" +
                "اطلاعات مورد نظر:\n\n" +
                "قیمت\n" +
                "درصد تغییر\n" +
                "حجم\n" +
                "ارزش معاملات\n" +
                "قدرت خریدار\n" +
                "ورود و خروج پول\n" +
                "تحلیل تکنیکال\n" +
                "تحلیل بنیادی"
        ));

        addBackButton(page);
    }

    private void showValuablePage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("⭐ سهم‌های ارزنده"));

        page.addView(makeText(
                "سهم‌های ارزنده\n\n" +
                "معیارهای بررسی:\n\n" +
                "۱. وضعیت بنیادی\n" +
                "۲. وضعیت تکنیکال\n" +
                "۳. حجم معاملات\n" +
                "۴. ورود پول حقیقی\n" +
                "۵. قدرت خریدار\n" +
                "۶. روند قیمت\n\n" +
                "در مرحله بعد این اطلاعات به صورت خودکار از داده‌های بازار محاسبه می‌شوند."
        ));

        addBackButton(page);
    }

    private void showPortfolioPage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("📁 بررسی سهام‌های من"));

        page.addView(makeText(
                "سبد سهام من\n\n" +
                "برای هر نماد:\n\n" +
                "• قیمت خرید\n" +
                "• قیمت فعلی\n" +
                "• سود یا زیان\n" +
                "• درصد سود یا زیان\n" +
                "• وضعیت تکنیکال\n" +
                "• وضعیت بنیادی\n" +
                "• ورود و خروج پول\n\n" +
                "امکان ذخیره سبد در مرحله بعد اضافه می‌شود."
        ));

        addBackButton(page);
    }

    private void showSuggestionsPage() {

        LinearLayout page = makePageLayout();

        page.addView(makeTitle("💡 پیشنهادهای معاملاتی"));

        page.addView(makeText(
                "پیشنهادهای معاملاتی\n\n" +
                "نمادها براساس چند معیار بررسی خواهند شد:\n\n" +
                "🟢 روند مثبت\n" +
                "🟢 حجم مناسب\n" +
                "🟢 قدرت خریدار\n" +
                "🟢 ورود پول حقیقی\n" +
                "🟢 وضعیت بنیادی\n" +
                "🟢 وضعیت تکنیکال\n\n" +
                "⚠️ این اطلاعات تضمین‌کننده سود نیستند."
        ));

        addBackButton(page);
    }

    private void addBackButton(LinearLayout page) {

        addSpace(page, 10);

        Button back =
                makeButton("⬅ بازگشت به صفحه اصلی");

        page.addView(back);

        back.setOnClickListener(v -> showMainPage());
    }

    @Override
    public void onBackPressed() {
        showMainPage();
    }
}
