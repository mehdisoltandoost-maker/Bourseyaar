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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(Looper.getMainLooper());

        showMainPage();
    }

    // =====================================================
    // صفحه اصلی
    // =====================================================

    private void showMainPage() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(18), dp(16), dp(20));
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("📊 بورس‌یار");
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.rgb(20, 60, 100));

        layout.addView(
                title,
                new LinearLayout.LayoutParams(-1, dp(65))
        );

        TextView subtitle = new TextView(this);
        subtitle.setText("دستیار تحلیل بازار بورس ایران");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(Color.DKGRAY);

        layout.addView(
                subtitle,
                new LinearLayout.LayoutParams(-1, dp(45))
        );

        Button market = makeButton("📊 اطلاعات کلی بورس ایران");
        market.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMarketPage();
            }
        });
        layout.addView(market);

        Button smart = makeButton("💵 پول هوشمند");
        smart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "💵 پول هوشمند",
                        "در این بخش ورود پول هوشمند به نمادها بررسی خواهد شد.\n\n" +
                        "اطلاعات مورد نیاز از داده‌های معاملات بازار استخراج می‌شود."
                );
            }
        });
        layout.addView(smart);

        Button money = makeButton("🔄 ورود و خروج پول");
        money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "🔄 ورود و خروج پول",
                        "در این بخش ورود و خروج پول حقیقی بررسی خواهد شد.\n\n" +
                        "خرید حقیقی\n" +
                        "فروش حقیقی\n" +
                        "ورود پول\n" +
                        "خروج پول\n" +
                        "ارزش معاملات"
                );
            }
        });
        layout.addView(money);

        Button fundamental = makeButton("💰 تحلیل بنیادی");
        fundamental.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "💰 تحلیل بنیادی",
                        "بررسی سودآوری، فروش، سود هر سهم، P/E، دارایی‌ها و اطلاعات کدال."
                );
            }
        });
        layout.addView(fundamental);

        Button technical = makeButton("📈 تحلیل تکنیکال");
        technical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "📈 تحلیل تکنیکال",
                        "RSI\nMACD\nمیانگین متحرک\nحجم معاملات\nحمایت و مقاومت\nروند قیمت"
                );
            }
        });
        layout.addView(technical);

        Button symbols = makeButton("🔎 بررسی نمادها");
        symbols.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "🔎 بررسی نمادها",
                        "در این قسمت اطلاعات نمادهای بورس بررسی خواهد شد."
                );
            }
        });
        layout.addView(symbols);

        Button valuable = makeButton("⭐ سهم‌های ارزنده");
        valuable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "⭐ سهم‌های ارزنده",
                        "نمادها بر اساس اطلاعات بنیادی و تکنیکال بررسی خواهند شد."
                );
            }
        });
        layout.addView(valuable);

        Button portfolio = makeButton("📁 بررسی سهام‌های من");
        portfolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "📁 بررسی سهام‌های من",
                        "در این بخش سبد سهام شما بررسی خواهد شد."
                );
            }
        });
        layout.addView(portfolio);

        Button suggestions = makeButton("💡 پیشنهادهای معاملاتی");
        suggestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "💡 پیشنهادهای معاملاتی",
                        "پس از دریافت اطلاعات واقعی بازار، این قسمت برای جمع‌بندی شرایط نمادها استفاده خواهد شد."
                );
            }
        });
        layout.addView(suggestions);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(layout);

        setContentView(scroll);
    }

    // =====================================================
    // ساخت دکمه
    // =====================================================

    private Button makeButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setEnabled(true);
        button.setClickable(true);
        button.setFocusable(true);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        params.setMargins(
                0,
                dp(5),
                0,
                dp(5)
        );

        button.setLayoutParams(params);

        return button;
    }

    // =====================================================
    // صفحه اطلاعات
    // =====================================================

    private void showInfoPage(
            String title,
            String message
    ) {

        LinearLayout page = createPage();

        TextView titleView = new TextView(this);

        titleView.setText(title);
        titleView.setTextSize(24);
        titleView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        titleView.setGravity(Gravity.CENTER);
        titleView.setTextColor(
                Color.rgb(20, 60, 100)
        );

        page.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        TextView text = new TextView(this);

        text.setText(message);
        text.setTextSize(17);
        text.setTextColor(Color.DKGRAY);
        text.setGravity(Gravity.RIGHT);
        text.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(20)
        );

        page.addView(
                text,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        addBackButton(page);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(page);

        setContentView(scroll);
    }

    // =====================================================
    // صفحه بازار
    // =====================================================

    private void showMarketPage() {

        final LinearLayout page = createPage();

        TextView title = new TextView(this);

        title.setText("📊 اطلاعات کلی بورس ایران");
        title.setTextSize(24);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        title.setGravity(Gravity.CENTER);
        title.setTextColor(
                Color.rgb(20, 60, 100)
        );

        page.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        final TextView result = new TextView(this);

        result.setText(
                "⏳ در حال اتصال به TSETMC...\n\n" +
                "لطفاً چند لحظه صبر کنید."
        );

        result.setTextSize(17);
        result.setTextColor(Color.DKGRAY);
        result.setGravity(Gravity.RIGHT);
        result.setPadding(
                dp(10),
                dp(15),
                dp(10),
                dp(20)
        );

        page.addView(
                result,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        Button refresh =
                makeButton("🔄 دریافت دوباره اطلاعات");

        refresh.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        result.setText(
                                "⏳ در حال اتصال به TSETMC..."
                        );

                        loadTsetmcData(result);
                    }
                }
        );

        page.addView(refresh);

        addBackButton(page);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(page);

        setContentView(scroll);

        loadTsetmcData(result);
    }

    // =====================================================
    // اتصال جدید TSETMC
    // =====================================================

    private void loadTsetmcData(
            final TextView result
    ) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                HttpURLConnection connection = null;

                try {

                    String address =
                            "https://cdn.tsetmc.com/api/ClosingPrice/GetMarketWatch"
                            + "?market=0"
                            + "&paperTypes%5B0%5D=1"
                            + "&paperTypes%5B1%5D=2"
                            + "&paperTypes%5B2%5D=3"
                            + "&paperTypes%5B3%5D=4"
                            + "&paperTypes%5B4%5D=5"
                            + "&paperTypes%5B5%5D=6"
                            + "&paperTypes%5B6%5D=7"
                            + "&paperTypes%5B7%5D=8"
                            + "&paperTypes%5B8%5D=9"
                            + "&withBestLimits=false"
                            + "&hEven=0"
                            + "&RefID=0";

                    URL url = new URL(address);

                    connection =
                            (HttpURLConnection)
                                    url.openConnection();

                    connection.setRequestMethod("GET");

                    connection.setConnectTimeout(20000);

                    connection.setReadTimeout(25000);

                    connection.setUseCaches(false);

                    connection.setDoInput(true);

                    connection.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36"
                    );

                    connection.setRequestProperty(
                            "Accept",
                            "application/json, text/plain, */*"
                    );

                    connection.setRequestProperty(
                            "Referer",
                            "https://www.tsetmc.com/"
                    );

                    connection.setRequestProperty(
                            "Connection",
                            "close"
                    );

                    int code =
                            connection.getResponseCode();

                    InputStream stream;

                    if (code >= 200 && code < 300) {

                        stream =
                                connection.getInputStream();

                    } else {

                        stream =
                                connection.getErrorStream();
                    }

                    if (stream == null) {

                        throw new Exception(
                                "سرور هیچ پاسخی ارسال نکرد."
                        );
                    }

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            stream,
                                            "UTF-8"
                                    )
                            );

                    StringBuilder data =
                            new StringBuilder();

                    String line;

                    while (
                            (line = reader.readLine())
                                    != null
                    ) {

                        data.append(line);
                    }

                    reader.close();

                    final int finalCode = code;

                    final String finalData =
                            data.toString();

                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            if (finalCode >= 200 &&
                                    finalCode < 300 &&
                                    finalData.length() > 0) {

                                String preview =
                                        finalData;

                                if (preview.length() > 3000) {

                                    preview =
                                            preview.substring(
                                                    0,
                                                    3000
                                            );
                                }

                                result.setText(
                                        "✅ اتصال به TSETMC برقرار شد\n\n" +
                                        "کد پاسخ سرور: " +
                                        finalCode +
                                        "\n\n" +
                                        "حجم اطلاعات: " +
                                        finalData.length() +
                                        " کاراکتر\n\n" +
                                        "نمونه اطلاعات دریافتی:\n\n" +
                                        preview
                                );

                            } else {

                                result.setText(
                                        "❌ اتصال به TSETMC موفق نبود.\n\n" +
                                        "کد پاسخ سرور: " +
                                        finalCode +
                                        "\n\n" +
                                        "پاسخ سرور:\n\n" +
                                        finalData
                                );
                            }
                        }
                    });

                } catch (final Exception e) {

                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            String error =
                                    e.getClass()
                                            .getSimpleName();

                            String detail =
                                    e.getMessage();

                            if (detail == null) {
                                detail = "";
                            }

                            result.setText(
                                    "❌ اتصال به TSETMC برقرار نشد.\n\n" +
                                    "نوع خطا:\n" +
                                    error +
                                    "\n\n" +
                                    "جزئیات:\n" +
                                    detail +
                                    "\n\n" +
                                    "این متن برای تشخیص دقیق مشکل اتصال نمایش داده شده است."
                            );
                        }
                    });

                } finally {

                    if (connection != null) {

                        connection.disconnect();
                    }
                }
            }

        }).start();
    }

    // =====================================================
    // صفحه پایه
    // =====================================================

    private LinearLayout createPage() {

        LinearLayout page =
                new LinearLayout(this);

        page.setOrientation(
                LinearLayout.VERTICAL
        );

        page.setPadding(
                dp(16),
                dp(15),
                dp(16),
                dp(20)
        );

        page.setBackgroundColor(
                Color.WHITE
        );

        return page;
    }

    // =====================================================
    // دکمه بازگشت
    // =====================================================

    private void addBackButton(
            LinearLayout page
    ) {

        Button back =
                makeButton(
                        "⬅️ بازگشت به صفحه اصلی"
                );

        back.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        showMainPage();
                    }
                }
        );

        page.addView(back);
    }

    // =====================================================
    // Back گوشی
    // =====================================================

    @Override
    public void onBackPressed() {

        showMainPage();
    }

    // =====================================================
    // تبدیل dp
    // =====================================================

    private int dp(int value) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return (int)
                (value * density + 0.5f);
    }
}
