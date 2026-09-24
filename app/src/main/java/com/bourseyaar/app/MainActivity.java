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

    private LinearLayout mainLayout;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(Looper.getMainLooper());

        showMainPage();
    }

    // =========================================================
    // صفحه اصلی
    // =========================================================

    private void showMainPage() {

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dp(16), dp(20), dp(16), dp(20));
        mainLayout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("📊 بورس‌یار");
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.rgb(20, 60, 100));

        mainLayout.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        TextView subtitle = new TextView(this);
        subtitle.setText("دستیار تحلیل بازار بورس ایران");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(Color.DKGRAY);

        mainLayout.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                )
        );

        // -----------------------------------------------------
        // دکمه 1
        // -----------------------------------------------------

        Button market = makeButton("📊 اطلاعات کلی بورس ایران");

        market.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMarketPage();
            }
        });

        mainLayout.addView(market);

        // -----------------------------------------------------
        // دکمه 2
        // -----------------------------------------------------

        Button smartMoney = makeButton("💵 پول هوشمند");

        smartMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "💵 پول هوشمند",
                        "در این بخش ورود پول هوشمند به نمادها بررسی می‌شود.\n\n" +
                        "نسخه فعلی در حال آماده‌سازی اتصال به داده‌های واقعی بازار است.\n\n" +
                        "پس از فعال شدن دریافت اطلاعات، نمادها بر اساس حجم معاملات، ارزش معاملات و ورود پول حقیقی بررسی خواهند شد."
                );
            }
        });

        mainLayout.addView(smartMoney);

        // -----------------------------------------------------
        // دکمه 3
        // -----------------------------------------------------

        Button moneyFlow = makeButton("🔄 ورود و خروج پول");

        moneyFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "🔄 ورود و خروج پول",
                        "در این بخش ورود و خروج پول حقیقی بررسی می‌شود.\n\n" +
                        "اطلاعات مورد نیاز:\n" +
                        "• خرید حقیقی\n" +
                        "• فروش حقیقی\n" +
                        "• ورود پول\n" +
                        "• خروج پول\n" +
                        "• ارزش معاملات"
                );
            }
        });

        mainLayout.addView(moneyFlow);

        // -----------------------------------------------------
        // دکمه 4
        // -----------------------------------------------------

        Button fundamental = makeButton("💰 تحلیل بنیادی");

        fundamental.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "💰 تحلیل بنیادی",
                        "تحلیل بنیادی بورس‌یار شامل بررسی موارد زیر خواهد بود:\n\n" +
                        "• سود هر سهم\n" +
                        "• نسبت P/E\n" +
                        "• فروش شرکت\n" +
                        "• سود خالص\n" +
                        "• رشد سودآوری\n" +
                        "• وضعیت دارایی‌ها و بدهی‌ها\n\n" +
                        "منبع اطلاعات بنیادی: کدال"
                );
            }
        });

        mainLayout.addView(fundamental);

        // -----------------------------------------------------
        // دکمه 5
        // -----------------------------------------------------

        Button technical = makeButton("📈 تحلیل تکنیکال");

        technical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "📈 تحلیل تکنیکال",
                        "در این بخش تحلیل تکنیکال نمادها انجام خواهد شد.\n\n" +
                        "اندیکاتورهای مورد نظر:\n" +
                        "• RSI\n" +
                        "• MACD\n" +
                        "• میانگین متحرک\n" +
                        "• حجم معاملات\n" +
                        "• حمایت و مقاومت\n" +
                        "• روند قیمت"
                );
            }
        });

        mainLayout.addView(technical);

        // -----------------------------------------------------
        // دکمه 6
        // -----------------------------------------------------

        Button symbols = makeButton("🔎 بررسی نمادها");

        symbols.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "🔎 بررسی نمادها",
                        "نام نماد را می‌توان در این بخش بررسی کرد.\n\n" +
                        "اطلاعات قابل نمایش:\n" +
                        "• قیمت\n" +
                        "• حجم\n" +
                        "• ارزش معاملات\n" +
                        "• خرید و فروش حقیقی\n" +
                        "• وضعیت تکنیکال\n" +
                        "• وضعیت بنیادی"
                );
            }
        });

        mainLayout.addView(symbols);

        // -----------------------------------------------------
        // دکمه 7
        // -----------------------------------------------------

        Button valuable = makeButton("⭐ سهم‌های ارزنده");

        valuable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "⭐ سهم‌های ارزنده",
                        "بورس‌یار در این قسمت نمادها را بر اساس ترکیبی از عوامل بنیادی و تکنیکال بررسی خواهد کرد.\n\n" +
                        "هدف، پیدا کردن نمادهایی است که شرایط مناسبی از نظر داده‌های بازار داشته باشند."
                );
            }
        });

        mainLayout.addView(valuable);

        // -----------------------------------------------------
        // دکمه 8
        // -----------------------------------------------------

        Button portfolio = makeButton("📁 بررسی سهام‌های من");

        portfolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "📁 بررسی سهام‌های من",
                        "در این بخش می‌توان نمادهای موجود در سبد سهام را بررسی کرد.\n\n" +
                        "اطلاعات مورد نظر:\n" +
                        "• قیمت خرید\n" +
                        "• قیمت فعلی\n" +
                        "• سود و زیان\n" +
                        "• وضعیت تکنیکال\n" +
                        "• وضعیت بنیادی"
                );
            }
        });

        mainLayout.addView(portfolio);

        // -----------------------------------------------------
        // دکمه 9
        // -----------------------------------------------------

        Button suggestions = makeButton("💡 پیشنهادهای معاملاتی");

        suggestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoPage(
                        "💡 پیشنهادهای معاملاتی",
                        "این بخش برای جمع‌بندی اطلاعات بازار طراحی شده است.\n\n" +
                        "پس از فعال شدن داده‌های واقعی، بورس‌یار می‌تواند نمادها را از نظر:\n\n" +
                        "• روند\n" +
                        "• حجم\n" +
                        "• ورود پول\n" +
                        "• وضعیت بنیادی\n" +
                        "• وضعیت تکنیکال\n\n" +
                        "بررسی کند."
                );
            }
        });

        mainLayout.addView(suggestions);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(mainLayout);

        setContentView(scrollView);
    }

    // =========================================================
    // ساخت دکمه
    // =========================================================

    private Button makeButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(16);
        button.setGravity(Gravity.CENTER);
        button.setAllCaps(false);

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

    // =========================================================
    // صفحه اطلاعات ساده
    // =========================================================

    private void showInfoPage(String title, String text) {

        LinearLayout page = createPage();

        TextView titleView = new TextView(this);

        titleView.setText(title);
        titleView.setTextSize(24);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTextColor(Color.rgb(20, 60, 100));
        titleView.setPadding(0, dp(15), 0, dp(20));

        page.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        TextView content = new TextView(this);

        content.setText(text);
        content.setTextSize(17);
        content.setTextColor(Color.DKGRAY);
        content.setGravity(Gravity.RIGHT);
        content.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(20)
        );

        page.addView(
                content,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        addBackButton(page);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(page);

        setContentView(scrollView);
    }

    // =========================================================
    // صفحه اطلاعات کلی بازار
    // =========================================================

    private void showMarketPage() {

        final LinearLayout page = createPage();

        TextView title = new TextView(this);

        title.setText("📊 اطلاعات کلی بورس ایران");
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.rgb(20, 60, 100));

        page.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        final TextView result = new TextView(this);

        result.setText(
                "در حال دریافت اطلاعات بازار...\n\n" +
                "لطفاً چند لحظه صبر کنید."
        );

        result.setTextSize(17);
        result.setTextColor(Color.DKGRAY);
        result.setGravity(Gravity.RIGHT);
        result.setPadding(
                dp(10),
                dp(15),
                dp(10),
                dp(15)
        );

        page.addView(
                result,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        Button refresh = makeButton("🔄 دریافت دوباره اطلاعات");

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                result.setText(
                        "در حال دریافت اطلاعات بازار...\n\n" +
                        "لطفاً صبر کنید."
                );

                loadMarketData(result);
            }
        });

        page.addView(refresh);

        addBackButton(page);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(page);

        setContentView(scrollView);

        loadMarketData(result);
    }

    // =========================================================
    // دریافت اطلاعات بازار
    // =========================================================

    private void loadMarketData(final TextView result) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                HttpURLConnection connection = null;

                try {

                    URL url = new URL(
                            "https://old.tsetmc.com/tsev2/data/MarketWatchInit.aspx"
                    );

                    connection =
                            (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");

                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(20000);

                    connection.setUseCaches(false);

                    connection.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0"
                    );

                    connection.setRequestProperty(
                            "Accept",
                            "*/*"
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
                    }

                    if (inputStream == null) {

                        throw new Exception(
                                "پاسخی از سرور دریافت نشد"
                        );
                    }

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            inputStream,
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

                    final String data =
                            builder.toString();

                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            if (data.length() == 0) {

                                result.setText(
                                        "❌ اطلاعاتی از سرور دریافت نشد."
                                );

                            } else {

                                String display =
                                        "✅ اتصال به سرور برقرار شد\n\n" +
                                        "حجم اطلاعات دریافتی: " +
                                        data.length() +
                                        " کاراکتر\n\n";

                                try {

                                    String[] parts =
                                            data.split(";");

                                    display +=
                                            "تعداد بخش‌های اطلاعات: " +
                                            parts.length +
                                            "\n\n";

                                    display +=
                                            "اتصال TSETMC فعال است.\n\n";

                                    display +=
                                            "مرحله بعدی: تبدیل اطلاعات خام بازار به شاخص‌ها و نمادهای قابل نمایش در بورس‌یار.";

                                } catch (Exception e) {

                                    display +=
                                            "داده دریافت شد ولی پردازش آن نیاز به اصلاح دارد.";
                                }

                                result.setText(display);
                            }
                        }
                    });

                } catch (final Exception e) {

                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            String message =
                                    e.getClass().getSimpleName();

                            if (e.getMessage() != null) {

                                message +=
                                        "\n\n" +
                                        e.getMessage();
                            }

                            result.setText(
                                    "❌ دریافت اطلاعات بازار انجام نشد.\n\n" +
                                    "خطای اتصال:\n" +
                                    message +
                                    "\n\n" +
                                    "اگر اینترنت گوشی برقرار است، احتمالاً اتصال HTTPS/TSETMC نیاز به اصلاح دارد."
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

    // =========================================================
    // ساخت صفحه
    // =========================================================

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

    // =========================================================
    // دکمه بازگشت
    // =========================================================

    private void addBackButton(
            LinearLayout page
    ) {

        Button back =
                makeButton("⬅️ بازگشت به صفحه اصلی");

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

    // =========================================================
    // دکمه Back گوشی
    // =========================================================

    @Override
    public void onBackPressed() {

        showMainPage();
    }

    // =========================================================
    // تبدیل dp
    // =========================================================

    private int dp(int value) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return (int) (
                value * density + 0.5f
        );
    }
}
