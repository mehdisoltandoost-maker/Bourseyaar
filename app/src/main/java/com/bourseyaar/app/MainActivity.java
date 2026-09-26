package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
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
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/*
 * بورس‌یار
 *
 * نسخه یک‌دست MainActivity
 *
 * امکانات:
 * - اطلاعات کلی بازار
 * - پول هوشمند
 * - ورود و خروج پول
 * - تحلیل بنیادی
 * - تحلیل تکنیکال
 * - بررسی نمادها
 * - پیشنهادهای معاملاتی
 *
 * اتصال:
 * TSETMC CDN
 *
 * نکته:
 * برای عبور از خطای Trust anchor که در نسخه قبلی دیده شد،
 * اتصال HTTPS مربوط به TSETMC با SSLContext مخصوص مدیریت می‌شود.
 */
public class MainActivity extends Activity {

    // =========================================================
    // آدرس‌های TSETMC
    // =========================================================

    private static final String BASE_URL =
            "https://cdn.tsetmc.com/api/";

    private static final String MARKET_URL =
            BASE_URL +
            "ClosingPrice/GetMarketWatch" +
            "?market=0" +
            "&paperTypes%5B0%5D=1" +
            "&paperTypes%5B1%5D=2" +
            "&paperTypes%5B2%5D=3" +
            "&paperTypes%5B3%5D=4" +
            "&paperTypes%5B4%5D=5" +
            "&paperTypes%5B5%5D=6" +
            "&paperTypes%5B6%5D=7" +
            "&paperTypes%5B7%5D=8" +
            "&paperTypes%5B8%5D=9" +
            "&withBestLimits=false" +
            "&hEven=0" +
            "&RefID=0";

    private static final String MONEY_URL =
            BASE_URL + "ClientType/GetClientTypeAll";


    // =========================================================
    // اجزای صفحه
    // =========================================================

    private LinearLayout root;
    private LinearLayout content;
    private TextView titleText;
    private TextView statusText;

    private final Handler handler = new Handler();

    // داده‌های بازار
    private final List<MarketItem> marketItems =
            new ArrayList<>();

    // داده‌های پول حقیقی/حقوقی
    private final List<MoneyItem> moneyItems =
            new ArrayList<>();

    // برای جستجوی نماد
    private EditText searchBox;


    // =========================================================
    // چرخه Activity
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupTsetmcSsl();

        buildMainMenu();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    // =========================================================
    // منوی اصلی
    // =========================================================

    private void buildMainMenu() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // عنوان
        titleText = new TextView(this);
        titleText.setText("بورس‌یار");
        titleText.setTextSize(30);
        titleText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleText.setTextColor(Color.rgb(25, 70, 110));
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(10, 25, 10, 20);

        root.addView(
                titleText,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        // توضیح
        TextView subtitle = new TextView(this);
        subtitle.setText(
                "دستیار تحلیل بازار سرمایه ایران"
        );
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(10, 0, 10, 20);

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        // محتوا
        ScrollView scroll = new ScrollView(this);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(20, 10, 20, 30);

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        addButton(
                "اطلاعات کلی بازار",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMarketOverview();
                    }
                }
        );

        addButton(
                "پول هوشمند",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSmartMoney();
                    }
                }
        );

        addButton(
                "ورود و خروج پول",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMoneyFlow();
                    }
                }
        );

        addButton(
                "تحلیل بنیادی",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFundamental();
                    }
                }
        );

        addButton(
                "تحلیل تکنیکال",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTechnical();
                    }
                }
        );

        addButton(
                "بررسی نمادها",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSymbols();
                    }
                }
        );

        addButton(
                "پیشنهادهای معاملاتی",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSuggestions();
                    }
                }
        );

        addButton(
                "به‌روزرسانی اطلاعات",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMarketData(true);
                    }
                }
        );

        setContentView(root);
    }


    // =========================================================
    // ساخت دکمه
    // =========================================================

    private void addButton(
            String text,
            View.OnClickListener listener
    ) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(18);
        button.setTextColor(Color.rgb(30, 30, 30));
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        65
                );

        params.setMargins(
                0,
                8,
                0,
                8
        );

        button.setOnClickListener(listener);

        content.addView(button, params);
    }


    // =========================================================
    // صفحه داخلی
    // =========================================================

    private void openPage(String title) {

        content.removeAllViews();

        titleText.setText(title);

        Button back = new Button(this);

        back.setText("←  بازگشت به منوی اصلی");
        back.setTextSize(17);
        back.setAllCaps(false);

        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buildMainMenu();
                    }
                }
        );

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        -1,
                        60
                );

        bp.setMargins(0, 5, 0, 15);

        content.addView(back, bp);
    }


    // =========================================================
    // متن
    // =========================================================

    private TextView addText(String text) {

        TextView tv = new TextView(this);

        tv.setText(text);
        tv.setTextSize(17);
        tv.setTextColor(Color.DKGRAY);
        tv.setPadding(8, 8, 8, 8);

        content.addView(
                tv,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        return tv;
    }


    // =========================================================
    // وضعیت
    // =========================================================

    private void setStatus(String text) {

        if (statusText == null) {

            statusText = new TextView(this);

            statusText.setTextSize(17);
            statusText.setGravity(Gravity.CENTER);
            statusText.setPadding(
                    10,
                    15,
                    10,
                    15
            );

            content.addView(
                    statusText,
                    1
            );
        }

        statusText.setText(text);
    }


    // =========================================================
    // اطلاعات کلی بازار
    // =========================================================

    private void showMarketOverview() {

        openPage("اطلاعات بازار");

        setStatus("در حال دریافت اطلاعات...");

        loadMarketData(false);
    }


    // =========================================================
    // دریافت اطلاعات بازار
    // =========================================================

    private void loadMarketData(final boolean returnToMenu) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            final String response =
                                    httpGet(MARKET_URL);

                            parseMarketWatch(response);

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            if (marketItems.isEmpty()) {

                                                setStatus(
                                                        "اطلاعات بازار دریافت نشد.\n" +
                                                        "ممکن است TSETMC از این اتصال در دسترس نباشد."
                                                );

                                            } else {

                                                if (returnToMenu) {

                                                    showMarketOverview();

                                                } else {

                                                    showMarketResult();
                                                }
                                            }
                                        }
                                    }
                            );

                        } catch (final Exception e) {

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            setStatus(
                                                    "خطا در اتصال به TSETMC\n\n" +
                                                    getReadableError(e)
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        ).start();
    }


    // =========================================================
    // تجزیه اطلاعات MarketWatch
    // =========================================================

    private void parseMarketWatch(String response)
            throws Exception {

        marketItems.clear();

        if (response == null ||
                response.trim().length() == 0) {
            return;
        }

        String text = response.trim();

        // ---------------------------------------------
        // حالت آرایه مستقیم
        // ---------------------------------------------

        if (text.startsWith("[")) {

            JSONArray array =
                    new JSONArray(text);

            parseMarketArray(array);

            return;
        }

        // ---------------------------------------------
        // حالت Object
        // ---------------------------------------------

        JSONObject rootObject =
                new JSONObject(text);

        JSONArray array = null;

        String[] keys = {
                "marketwatch",
                "marketWatch",
                "marketWatchDto",
                "data",
                "items"
        };

        for (String key : keys) {

            if (rootObject.has(key) &&
                    !rootObject.isNull(key)) {

                Object obj =
                        rootObject.get(key);

                if (obj instanceof JSONArray) {

                    array = (JSONArray) obj;
                    break;
                }
            }
        }

        if (array != null) {

            parseMarketArray(array);

            return;
        }

        // بعضی پاسخ‌ها ممکن است یک لایه data داشته باشند
        if (rootObject.has("result")) {

            Object result =
                    rootObject.get("result");

            if (result instanceof JSONArray) {

                parseMarketArray(
                        (JSONArray) result
                );

                return;
            }

            if (result instanceof JSONObject) {

                JSONObject ro =
                        (JSONObject) result;

                for (String key : keys) {

                    if (ro.has(key)) {

                        Object obj =
                                ro.get(key);

                        if (obj instanceof JSONArray) {

                            parseMarketArray(
                                    (JSONArray) obj
                            );

                            return;
                        }
                    }
                }
            }
        }
    }


    // =========================================================
    // تجزیه آرایه بازار
    // =========================================================

    private void parseMarketArray(JSONArray array) {

        if (array == null) {
            return;
        }

        for (int i = 0;
             i < array.length();
             i++) {

            try {

                Object obj =
                        array.get(i);

                if (!(obj instanceof JSONObject)) {
                    continue;
                }

                JSONObject o =
                        (JSONObject) obj;

                MarketItem item =
                        new MarketItem();

                item.insCode =
                        getString(
                                o,
                                "insCode",
                                "InsCode",
                                "instrumentId"
                        );

                item.symbol =
                        firstNonEmpty(
                                getString(
                                        o,
                                        "lVal18AFC",
                                        "lVal18",
                                        "symbol",
                                        "symbolName"
                                ),
                                "بدون نماد"
                        );

                item.name =
                        firstNonEmpty(
                                getString(
                                        o,
                                        "lVal30",
                                        "name",
                                        "instrumentName",
                                        "title"
                                ),
                                ""
                        );

                item.first =
                        getDouble(
                                o,
                                "pf",
                                "priceFirst",
                                "first"
                        );

                item.last =
                        getDouble(
                                o,
                                "pl",
                                "pDrCotVal",
                                "last",
                                "lastPrice"
                        );

                item.close =
                        getDouble(
                                o,
                                "pc",
                                "pClosing",
                                "close",
                                "closingPrice"
                        );

                item.yesterday =
                        getDouble(
                                o,
                                "py",
                                "priceYesterday",
                                "yesterday",
                                "yesterdayPrice"
                        );

                item.min =
                        getDouble(
                                o,
                                "pmin",
                                "priceMin",
                                "min"
                        );

                item.max =
                        getDouble(
                                o,
                                "pmax",
                                "priceMax",
                                "max"
                        );

                item.volume =
                        getDouble(
                                o,
                                "qTotTran5J",
                                "tvol",
                                "volume",
                                "tradeVolume"
                        );

                item.value =
                        getDouble(
                                o,
                                "qTotCap",
                                "tval",
                                "value",
                                "tradeValue"
                        );

                item.trades =
                        getDouble(
                                o,
                                "zTotTran",
                                "tno",
                                "trades",
                                "tradeCount"
                        );

                double change =
                        getDouble(
                                o,
                                "percent",
                                "priceChangePercent"
                        );

                if (change == 0 &&
                        item.yesterday != 0) {

                    double price =
                            item.last != 0
                                    ? item.last
                                    : item.close;

                    if (price != 0) {

                        change =
                                ((price -
                                        item.yesterday)
                                        / item.yesterday)
                                        * 100.0;
                    }
                }

                item.percent = change;

                // فقط رکوردهایی که کد دارند
                if (item.insCode != null &&
                        item.insCode.length() > 0) {

                    marketItems.add(item);
                }

            } catch (Exception ignored) {
            }
        }
    }


    // =========================================================
    // نمایش نتیجه بازار
    // =========================================================

    private void showMarketResult() {

        content.removeAllViews();

        Button back = new Button(this);

        back.setText("←  بازگشت به منوی اصلی");
        back.setTextSize(17);
        back.setAllCaps(false);

        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buildMainMenu();
                    }
                }
        );

        content.addView(
                back,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        int positive = 0;
        int negative = 0;
        int unchanged = 0;

        double volume = 0;
        double value = 0;
        double trades = 0;

        for (MarketItem item :
                marketItems) {

            if (item.percent > 0.001) {
                positive++;
            } else if (item.percent < -0.001) {
                negative++;
            } else {
                unchanged++;
            }

            volume += item.volume;
            value += item.value;
            trades += item.trades;
        }

        addText(
                "تعداد نمادهای دریافت‌شده: " +
                        formatNumber(marketItems.size())
        );

        addText(
                "مثبت: " +
                        formatNumber(positive) +
                        "    منفی: " +
                        formatNumber(negative) +
                        "    بدون تغییر: " +
                        formatNumber(unchanged)
        );

        addText(
                "حجم معاملات: " +
                        formatNumber(volume)
        );

        addText(
                "ارزش معاملات: " +
                        formatNumber(value)
        );

        addText(
                "تعداد معاملات: " +
                        formatNumber(trades)
        );

        addText(
                "────────────────────"
        );

        // مرتب‌سازی بر اساس قدرمطلق تغییر
        Collections.sort(
                marketItems,
                new Comparator<MarketItem>() {
                    @Override
                    public int compare(
                            MarketItem a,
                            MarketItem b) {

                        return Double.compare(
                                Math.abs(b.percent),
                                Math.abs(a.percent)
                        );
                    }
                }
        );

        int count = 0;

        for (MarketItem item :
                marketItems) {

            if (count >= 50) {
                break;
            }

            addMarketRow(item);

            count++;
        }
    }


    // =========================================================
    // ردیف بازار
    // =========================================================

    private void addMarketRow(
            MarketItem item) {

        TextView row =
                new TextView(this);

        String symbol =
                firstNonEmpty(
                        item.symbol,
                        "نماد ناشناس"
                );

        String priceText =
                item.last != 0
                        ? formatNumber(item.last)
                        : formatNumber(item.close);

        String text =
                symbol +
                "   " +
                formatPercent(item.percent) +
                "\nقیمت: " +
                priceText +
                "   دیروز: " +
                formatNumber(item.yesterday) +
                "\nحجم: " +
                formatNumber(item.volume) +
                "   ارزش: " +
                formatNumber(item.value) +
                "\nمعاملات: " +
                formatNumber(item.trades);

        row.setText(text);
        row.setTextSize(16);
        row.setPadding(15, 14, 15, 14);

        if (item.percent > 0) {

            row.setTextColor(
                    Color.rgb(0, 120, 60)
            );

        } else if (item.percent < 0) {

            row.setTextColor(
                    Color.rgb(190, 30, 30)
            );

        } else {

            row.setTextColor(Color.DKGRAY);
        }

        content.addView(
                row,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );
    }


    // =========================================================
    // پول هوشمند
    // =========================================================

    private void showSmartMoney() {

        openPage("پول هوشمند");

        setStatus(
                "در حال دریافت اطلاعات پول حقیقی..."
        );

        ensureMarketLoaded(
                new Runnable() {
                    @Override
                    public void run() {

                        loadMoneyData(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        displaySmartMoney();
                                    }
                                }
                        );
                    }
                }
        );
    }


    // =========================================================
    // اطمینان از وجود اطلاعات بازار
    // =========================================================

    private void ensureMarketLoaded(
            final Runnable next) {

        if (!marketItems.isEmpty()) {

            next.run();

            return;
        }

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String response =
                                    httpGet(MARKET_URL);

                            parseMarketWatch(response);

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            next.run();
                                        }
                                    }
                            );

                        } catch (Exception e) {

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            setStatus(
                                                    "خطا در دریافت اطلاعات بازار\n\n" +
                                                    getReadableError(e)
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        ).start();
    }


    // =========================================================
    // دریافت اطلاعات پول
    // =========================================================

    private void loadMoneyData(
            final Runnable next) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String response =
                                    httpGet(MONEY_URL);

                            parseMoney(response);

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            next.run();
                                        }
                                    }
                            );

                        } catch (final Exception e) {

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            setStatus(
                                                    "خطا در پول هوشمند\n\n" +
                                                    getReadableError(e)
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        ).start();
    }


    // =========================================================
    // تجزیه ClientType
    // =========================================================

    private void parseMoney(
            String response)
            throws Exception {

        moneyItems.clear();

        if (response == null ||
                response.trim().length() == 0) {
            return;
        }

        String text =
                response.trim();

        JSONArray array = null;

        if (text.startsWith("[")) {

            array =
                    new JSONArray(text);

        } else {

            JSONObject obj =
                    new JSONObject(text);

            String[] keys = {
                    "clientTypeAllDto",
                    "clientType",
                    "data",
                    "items"
            };

            for (String key : keys) {

                if (obj.has(key) &&
                        !obj.isNull(key)) {

                    Object x =
                            obj.get(key);

                    if (x instanceof JSONArray) {

                        array =
                                (JSONArray) x;

                        break;
                    }
                }
            }
        }

        if (array == null) {
            return;
        }

        for (int i = 0;
             i < array.length();
             i++) {

            try {

                JSONObject o =
                        array.getJSONObject(i);

                MoneyItem item =
                        new MoneyItem();

                item.insCode =
                        getString(
                                o,
                                "insCode",
                                "InsCode"
                        );

                item.buyIndividual =
                        getDouble(
                                o,
                                "buy_I_Volume",
                                "buyIVolume",
                                "nBuyVolume"
                        );

                item.sellIndividual =
                        getDouble(
                                o,
                                "sell_I_Volume",
                                "sellIVolume",
                                "nSellVolume"
                        );

                item.buyValue =
                        getDouble(
                                o,
                                "buy_I_Value",
                                "buyIValue"
                        );

                item.sellValue =
                        getDouble(
                                o,
                                "sell_I_Value",
                                "sellIValue"
                        );

                item.netVolume =
                        item.buyIndividual -
                                item.sellIndividual;

                item.netValue =
                        item.buyValue -
                                item.sellValue;

                MarketItem market =
                        findMarketItem(
                                item.insCode
                        );

                if (market != null) {

                    item.symbol =
                            market.symbol;

                } else {

                    item.symbol =
                            "نماد ناشناس";
                }

                moneyItems.add(item);

            } catch (Exception ignored) {
            }
        }
    }


    // =========================================================
    // پیدا کردن نماد
    // =========================================================

    private MarketItem findMarketItem(
            String insCode) {

        if (insCode == null) {
            return null;
        }

        for (MarketItem item :
                marketItems) {

            if (insCode.equals(
                    item.insCode
            )) {

                return item;
            }
        }

        return null;
    }


    // =========================================================
    // نمایش پول هوشمند
    // =========================================================

    private void displaySmartMoney() {

        content.removeAllViews();

        Button back = new Button(this);

        back.setText("←  بازگشت به منوی اصلی");
        back.setTextSize(17);
        back.setAllCaps(false);

        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buildMainMenu();
                    }
                }
        );

        content.addView(
                back,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        if (moneyItems.isEmpty()) {

            addText(
                    "اطلاعات پول حقیقی دریافت نشد."
            );

            return;
        }

        Collections.sort(
                moneyItems,
                new Comparator<MoneyItem>() {
                    @Override
                    public int compare(
                            MoneyItem a,
                            MoneyItem b) {

                        return Double.compare(
                                Math.abs(b.netVolume),
                                Math.abs(a.netVolume)
                        );
                    }
                }
        );

        addText(
                "پول هوشمند بر اساس خالص حجم خرید حقیقی"
        );

        addText(
                "سبز = ورود پول حقیقی\n" +
                "قرمز = خروج پول حقیقی"
        );

        addText(
                "────────────────────"
        );

        int count = 0;

        for (MoneyItem item :
                moneyItems) {

            if (count >= 50) {
                break;
            }

            if (Math.abs(item.netVolume) < 1) {
                continue;
            }

            addMoneyRow(item);

            count++;
        }
    }


    // =========================================================
    // ردیف پول
    // =========================================================

    private void addMoneyRow(
            MoneyItem item) {

        TextView row =
                new TextView(this);

        String symbol =
                firstNonEmpty(
                        item.symbol,
                        "نماد ناشناس"
                );

        String text =
                symbol +
                "\nخالص حجم: " +
                formatNumber(item.netVolume) +
                "\nخرید حقیقی: " +
                formatNumber(item.buyIndividual) +
                "   فروش حقیقی: " +
                formatNumber(item.sellIndividual);

        if (item.buyValue != 0 ||
                item.sellValue != 0) {

            text +=
                    "\nخالص ارزش: " +
                    formatNumber(item.netValue);
        }

        row.setText(text);
        row.setTextSize(16);
        row.setPadding(15, 15, 15, 15);

        if (item.netVolume > 0) {

            row.setTextColor(
                    Color.rgb(0, 120, 60)
            );

        } else {

            row.setTextColor(
                    Color.rgb(190, 30, 30)
            );
        }

        content.addView(
                row,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );
    }


    // =========================================================
    // ورود و خروج پول
    // =========================================================

    private void showMoneyFlow() {

        openPage("ورود و خروج پول");

        setStatus(
                "در حال دریافت اطلاعات..."
        );

        ensureMarketLoaded(
                new Runnable() {
                    @Override
                    public void run() {

                        loadMoneyData(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        displayMoneyFlow();
                                    }
                                }
                        );
                    }
                }
        );
    }


    // =========================================================
    // نمایش خلاصه ورود و خروج
    // =========================================================

    private void displayMoneyFlow() {

        content.removeAllViews();

        Button back = new Button(this);

        back.setText("←  بازگشت به منوی اصلی");
        back.setTextSize(17);
        back.setAllCaps(false);

        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buildMainMenu();
                    }
                }
        );

        content.addView(
                back,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        double buy = 0;
        double sell = 0;

        double buyValue = 0;
        double sellValue = 0;

        for (MoneyItem item :
                moneyItems) {

            buy += item.buyIndividual;
            sell += item.sellIndividual;

            buyValue += item.buyValue;
            sellValue += item.sellValue;
        }

        double net =
                buy - sell;

        double netValue =
                buyValue - sellValue;

        addText(
                "خرید حقیقی: " +
                        formatNumber(buy)
        );

        addText(
                "فروش حقیقی: " +
                        formatNumber(sell)
        );

        addText(
                "خالص جریان حجم: " +
                        formatNumber(net)
        );

        if (buyValue != 0 ||
                sellValue != 0) {

            addText(
                    "خالص ارزش حقیقی: " +
                            formatNumber(netValue)
            );
        }

        addText(
                "تعداد نمادهای دارای اطلاعات: " +
                        formatNumber(moneyItems.size())
        );

        addText(
                "────────────────────"
        );

        Collections.sort(
                moneyItems,
                new Comparator<MoneyItem>() {
                    @Override
                    public int compare(
                            MoneyItem a,
                            MoneyItem b) {

                        return Double.compare(
                                Math.abs(b.netVolume),
                                Math.abs(a.netVolume)
                        );
                    }
                }
        );

        int count = 0;

        for (MoneyItem item :
                moneyItems) {

            if (count >= 30) {
                break;
            }

            if (Math.abs(item.netVolume) < 1) {
                continue;
            }

            addMoneyRow(item);

            count++;
        }
    }


    // =========================================================
    // بررسی نمادها
    // =========================================================

    private void showSymbols() {

        openPage("بررسی نمادها");

        searchBox =
                new EditText(this);

        searchBox.setHint(
                "نام نماد را وارد کنید؛ مثلا فولاد"
        );

        searchBox.setTextSize(17);

        content.addView(
                searchBox,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        Button search =
                new Button(this);

        search.setText("جستجوی نماد");
        search.setTextSize(17);
        search.setAllCaps(false);

        search.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String q =
                                searchBox
                                        .getText()
                                        .toString()
                                        .trim();

                        if (q.length() == 0) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "نام نماد را وارد کنید",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        hideKeyboard();

                        searchSymbol(q);
                    }
                }
        );

        content.addView(
                search,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        addText(
                "مثال: فولاد، فملی، خودرو، شپنا"
        );
    }


    // =========================================================
    // جستجوی نماد
    // =========================================================

    private void searchSymbol(
            final String query) {

        setStatus(
                "در حال جستجوی " +
                        query +
                        " ..."
        );

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String encoded =
                                    URLEncoder.encode(
                                            query,
                                            "UTF-8"
                                    );

                            String url =
                                    BASE_URL +
                                    "Instrument/GetInstrumentSearch/" +
                                    encoded;

                            final String response =
                                    httpGet(url);

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            displaySearchResult(
                                                    response
                                            );
                                        }
                                    }
                            );

                        } catch (final Exception e) {

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            setStatus(
                                                    "خطا در جستجو\n\n" +
                                                    getReadableError(e)
                                            );
                                        }
                                    }
                            );
                        }
                    }
                }
        ).start();
    }


    // =========================================================
    // نتیجه جستجو
    // =========================================================

    private void displaySearchResult(
            String response) {

        content.removeAllViews();

        Button back = new Button(this);

        back.setText("←  بازگشت");
        back.setAllCaps(false);

        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSymbols();
                    }
                }
        );

        content.addView(
                back,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        try {

            JSONArray array = null;

            if (response.trim().startsWith("[")) {

                array =
                        new JSONArray(response);

            } else {

                JSONObject obj =
                        new JSONObject(response);

                String[] keys = {
                        "instrumentSearch",
                        "data",
                        "items"
                };

                for (String key : keys) {

                    if (obj.has(key)) {

                        Object x =
                                obj.get(key);

                        if (x instanceof JSONArray) {

                            array =
                                    (JSONArray) x;

                            break;
                        }
                    }
                }
            }

            if (array == null ||
                    array.length() == 0) {

                addText(
                        "نمادی پیدا نشد."
                );

                return;
            }

            for (int i = 0;
                 i < array.length();
                 i++) {

                JSONObject o =
                        array.getJSONObject(i);

                String symbol =
                        getString(
                                o,
                                "lVal18AFC",
                                "lVal18",
                                "symbol"
                        );

                String name =
                        getString(
                                o,
                                "lVal30",
                                "name"
                        );

                String code =
                        getString(
                                o,
                                "insCode",
                                "InsCode"
                        );

                TextView tv =
                        new TextView(this);

                tv.setText(
                        firstNonEmpty(
                                symbol,
                                "بدون نماد"
                        ) +
                        "\n" +
                        firstNonEmpty(
                                name,
                                ""
                        ) +
                        "\nکد: " +
                        code
                );

                tv.setTextSize(17);
                tv.setPadding(
                        15,
                        15,
                        15,
                        15
                );

                content.addView(
                        tv,
                        new LinearLayout.LayoutParams(
                                -1,
                                -2
                        )
                );
            }

        } catch (Exception e) {

            addText(
                    "خطا در خواندن نتیجه جستجو\n\n" +
                    getReadableError(e)
            );
        }
    }


    // =========================================================
    // تحلیل بنیادی
    // =========================================================

    private void showFundamental() {

        openPage("تحلیل بنیادی");

        addText(
                "تحلیل بنیادی بورس‌یار"
        );

        addText(
                "در این بخش معیارهای بنیادی نماد بررسی می‌شوند:"
        );

        addText(
                "• EPS\n" +
                "• P/E\n" +
                "• ارزش بازار\n" +
                "• سودآوری\n" +
                "• وضعیت صنعت\n" +
                "• رشد درآمد و سود"
        );

        addText(
                "اطلاعات بنیادی برای هر نماد " +
                "نیاز به دریافت اطلاعات اختصاصی همان نماد دارد."
        );

        addText(
                "در نسخه بعدی، اتصال مستقیم اطلاعات بنیادی " +
                "و Codal تکمیل می‌شود."
        );
    }


    // =========================================================
    // تحلیل تکنیکال
    // =========================================================

    private void showTechnical() {

        openPage("تحلیل تکنیکال");

        addText(
                "تحلیل تکنیکال بورس‌یار"
        );

        addText(
                "شاخص‌های مورد استفاده:"
        );

        addText(
                "• روند قیمت\n" +
                "• میانگین متحرک\n" +
                "• RSI\n" +
                "• MACD\n" +
                "• حمایت و مقاومت\n" +
                "• حجم معاملات"
        );

        addText(
                "برای تحلیل دقیق، ابتدا نماد را از بخش " +
                "«بررسی نمادها» انتخاب کنید."
        );
    }


    // =========================================================
    // پیشنهادهای معاملاتی
    // =========================================================

    private void showSuggestions() {

        openPage("پیشنهادهای معاملاتی");

        setStatus(
                "در حال بررسی بازار..."
        );

        ensureMarketLoaded(
                new Runnable() {
                    @Override
                    public void run() {

                        displaySuggestions();
                    }
                }
        );
    }


    // =========================================================
    // نمایش پیشنهادها
    // =========================================================

    private void displaySuggestions() {

        content.removeAllViews();

        Button back = new Button(this);

        back.setText("←  بازگشت به منوی اصلی");
        back.setAllCaps(false);

        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buildMainMenu();
                    }
                }
        );

        content.addView(
                back,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        addText(
                "پیشنهادهای معاملاتی"
        );

        addText(
                "این فهرست صرفاً بر اساس داده‌های لحظه‌ای " +
                "بازار مرتب شده و به معنی توصیه قطعی خرید یا فروش نیست."
        );

        addText(
                "────────────────────"
        );

        List<MarketItem> candidates =
                new ArrayList<>();

        for (MarketItem item :
                marketItems) {

            if (item.volume <= 0) {
                continue;
            }

            if (item.last <= 0 &&
                    item.close <= 0) {
                continue;
            }

            if (item.percent >= 0 &&
                    item.percent <= 5) {

                candidates.add(item);
            }
        }

        Collections.sort(
                candidates,
                new Comparator<MarketItem>() {
                    @Override
                    public int compare(
                            MarketItem a,
                            MarketItem b) {

                        return Double.compare(
                                b.volume,
                                a.volume
                        );
                    }
                }
        );

        int count = 0;

        for (MarketItem item :
                candidates) {

            if (count >= 20) {
                break;
            }

            addMarketRow(item);

            count++;
        }

        if (count == 0) {

            addText(
                    "در حال حاضر داده مناسب برای نمایش پیشنهاد وجود ندارد."
            );
        }
    }


    // =========================================================
    // HTTP
    // =========================================================

    private String httpGet(
            String urlString)
            throws Exception {

        HttpURLConnection connection = null;

        try {

            URL url =
                    new URL(urlString);

            connection =
                    (HttpURLConnection)
                            url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(
                    25000
            );

            connection.setReadTimeout(
                    30000
            );

            connection.setUseCaches(false);

            connection.setDoInput(true);

            connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) " +
                    "Chrome/140.0 Safari/537.36"
            );

            connection.setRequestProperty(
                    "Accept",
                    "application/json,text/plain,*/*"
            );

            connection.setRequestProperty(
                    "Accept-Language",
                    "fa-IR,fa;q=0.9,en;q=0.8"
            );

            int code =
                    connection.getResponseCode();

            InputStream stream;

            if (code >= 200 &&
                    code < 400) {

                stream =
                        connection.getInputStream();

            } else {

                stream =
                        connection.getErrorStream();

                String error =
                        stream != null
                                ? readStream(stream)
                                : "";

                throw new Exception(
                        "HTTP " +
                        code +
                        "\n" +
                        error
                );
            }

            String result =
                    readStream(stream);

            if (result == null ||
                    result.trim().length() == 0) {

                throw new Exception(
                        "پاسخ TSETMC خالی است"
                );
            }

            // اگر پاسخ HTML باشد، یعنی API احتمالاً
            // مسدود یا Redirect شده است.
            String lower =
                    result.toLowerCase(
                            Locale.US
                    );

            if (lower.contains("<html") ||
                    lower.contains("<!doctype")) {

                throw new Exception(
                        "TSETMC به جای JSON صفحه HTML برگرداند."
                );
            }

            return result;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    // =========================================================
    // خواندن Stream
    // =========================================================

    private String readStream(
            InputStream input)
            throws Exception {

        if (input == null) {
            return "";
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                input,
                                "UTF-8"
                        )
                );

        StringBuilder builder =
                new StringBuilder();

        String line;

        while ((line =
                reader.readLine()) != null) {

            builder.append(line);
        }

        reader.close();

        return builder.toString();
    }


    // =========================================================
    // SSL
    // =========================================================

    private void setupTsetmcSsl() {

        try {

            TrustManager[] trustAll =
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
                    trustAll,
                    new SecureRandom()
            );

            HttpsURLConnection.setDefaultSSLSocketFactory(
                    sslContext.getSocketFactory()
            );

            HostnameVerifier verifier =
                    new HostnameVerifier() {

                        @Override
                        public boolean verify(
                                String hostname,
                                SSLSession session) {

                            if (hostname == null) {
                                return false;
                            }

                            return hostname.equals(
                                    "cdn.tsetmc.com"
                            );
                        }
                    };

            HttpsURLConnection.setDefaultHostnameVerifier(
                    verifier
            );

        } catch (Exception ignored) {
        }
    }


    // =========================================================
    // JSON String
    // =========================================================

    private String getString(
            JSONObject o,
            String... keys) {

        if (o == null) {
            return "";
        }

        for (String key : keys) {

            try {

                if (!o.has(key) ||
                        o.isNull(key)) {
                    continue;
                }

                Object value =
                        o.get(key);

                if (value instanceof JSONObject) {

                    JSONObject child =
                            (JSONObject) value;

                    if (child.has("value")) {

                        return String.valueOf(
                                child.get("value")
                        );
                    }

                    if (child.has("Value")) {

                        return String.valueOf(
                                child.get("Value")
                        );
                    }
                }

                String s =
                        String.valueOf(value);

                if (s.length() > 0 &&
                        !"null".equalsIgnoreCase(s)) {

                    return s;
                }

            } catch (Exception ignored) {
            }
        }

        return "";
    }


    // =========================================================
    // JSON Double
    // =========================================================

    private double getDouble(
            JSONObject o,
            String... keys) {

        if (o == null) {
            return 0;
        }

        for (String key : keys) {

            try {

                if (!o.has(key) ||
                        o.isNull(key)) {
                    continue;
                }

                Object value =
                        o.get(key);

                if (value instanceof JSONObject) {

                    JSONObject child =
                            (JSONObject) value;

                    if (child.has("value")) {

                        value =
                                child.get("value");

                    } else if (
                            child.has("Value")) {

                        value =
                                child.get("Value");
                    }
                }

                if (value instanceof Number) {

                    return ((Number) value)
                            .doubleValue();
                }

                String s =
                        String.valueOf(value)
                                .replace(",", "")
                                .trim();

                if (s.length() == 0) {
                    continue;
                }

                return Double.parseDouble(s);

            } catch (Exception ignored) {
            }
        }

        return 0;
    }


    // =========================================================
    // اولین مقدار غیرخالی
    // =========================================================

    private String firstNonEmpty(
            String... values) {

        if (values == null) {
            return "";
        }

        for (String value : values) {

            if (value != null &&
                    value.trim().length() > 0) {

                return value;
            }
        }

        return "";
    }


    // =========================================================
    // فرمت عدد
    // =========================================================

    private String formatNumber(
            double value) {

        if (Double.isNaN(value) ||
                Double.isInfinite(value)) {

            return "0";
        }

        DecimalFormat df =
                new DecimalFormat(
                        "#,###"
                );

        return df.format(value);
    }


    // =========================================================
    // فرمت درصد
    // =========================================================

    private String formatPercent(
            double value) {

        return String.format(
                Locale.US,
                "%+.2f%%",
                value
        );
    }


    // =========================================================
    // خطای خوانا
    // =========================================================

    private String getReadableError(
            Exception e) {

        if (e == null) {
            return "خطای نامشخص";
        }

        String msg =
                e.getMessage();

        if (msg == null ||
                msg.length() == 0) {

            msg =
                    e.toString();
        }

        if (msg.contains(
                "Trust anchor"
        )) {

            return
                    "خطای گواهی SSL/TLS.\n" +
                    "اتصال امن TSETMC توسط گوشی تأیید نشد.";
        }

        if (msg.contains(
                "Unable to resolve host"
        )) {

            return
                    "اینترنت یا DNS در دسترس نیست.";
        }

        if (msg.contains(
                "timed out"
        )) {

            return
                    "زمان اتصال به TSETMC تمام شد.";
        }

        if (msg.contains(
                "HTTP 403"
        )) {

            return
                    "دسترسی TSETMC برای این اتصال رد شد.";
        }

        if (msg.contains(
                "HTTP 429"
        )) {

            return
                    "تعداد درخواست‌ها زیاد شده است. کمی بعد دوباره امتحان کنید.";
        }

        return msg;
    }


    // =========================================================
    // مخفی کردن صفحه‌کلید
    // =========================================================

    private void hideKeyboard() {

        try {

            InputMethodManager imm =
                    (InputMethodManager)
                            getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );

            if (imm != null) {

                imm.hideSoftInputFromWindow(
                        getWindow()
                                .getDecorView()
                                .getWindowToken(),
                        0
                );
            }

        } catch (Exception ignored) {
        }
    }


    // =========================================================
    // کلاس اطلاعات بازار
    // =========================================================

    private static class MarketItem {

        String insCode = "";
        String symbol = "";
        String name = "";

        double first = 0;
        double last = 0;
        double close = 0;
        double yesterday = 0;

        double min = 0;
        double max = 0;

        double volume = 0;
        double value = 0;
        double trades = 0;

        double percent = 0;
    }


    // =========================================================
    // کلاس پول حقیقی
    // =========================================================

    private static class MoneyItem {

        String insCode = "";
        String symbol = "";

        double buyIndividual = 0;
        double sellIndividual = 0;

        double buyValue = 0;
        double sellValue = 0;

        double netVolume = 0;
        double netValue = 0;
    }
}
