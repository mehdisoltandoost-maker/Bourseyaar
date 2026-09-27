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
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/*
 * بورس‌یار
 *
 * نسخه اصلاح‌شده
 *
 * اصلاحات اصلی:
 *
 * 1- دریافت MarketWatch از TSETMC
 * 2- تطبیق دقیق insCode
 * 3- دریافت ClientTypeAll
 * 4- اصلاح نمایش نمادهای فارسی
 * 5- محاسبه خالص حجم پول حقیقی
 * 6- محاسبه خالص ارزش پول حقیقی
 * 7- رتبه‌بندی پول هوشمند بر اساس قدرت ورود پول
 * 8- تفکیک ورود و خروج پول
 * 9- مقاوم‌سازی JSON Parser
 *
 */

public class MainActivity extends Activity {

    // =========================================================
    // TSETMC
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
            BASE_URL +
            "ClientType/GetClientTypeAll";


    // =========================================================
    // UI
    // =========================================================

    private LinearLayout root;
    private LinearLayout content;
    private TextView titleText;
    private TextView statusText;

    private EditText searchBox;

    private final Handler handler =
            new Handler();


    // =========================================================
    // داده‌ها
    // =========================================================

    private final List<MarketItem> marketItems =
            new ArrayList<>();

    private final List<MoneyItem> moneyItems =
            new ArrayList<>();


    // =========================================================
    // Activity
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

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.WHITE
        );


        titleText = new TextView(this);

        titleText.setText(
                "بورس‌یار"
        );

        titleText.setTextSize(30);

        titleText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        titleText.setTextColor(
                Color.rgb(25, 70, 110)
        );

        titleText.setGravity(
                Gravity.CENTER
        );

        titleText.setPadding(
                10,
                25,
                10,
                20
        );

        root.addView(
                titleText,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );


        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "دستیار تحلیل بازار سرمایه ایران"
        );

        subtitle.setTextSize(16);

        subtitle.setGravity(
                Gravity.CENTER
        );

        subtitle.setTextColor(
                Color.DKGRAY
        );

        subtitle.setPadding(
                10,
                0,
                10,
                20
        );

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );


        ScrollView scroll =
                new ScrollView(this);


        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                20,
                10,
                20,
                30
        );


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

                        marketItems.clear();
                        moneyItems.clear();

                        loadMarketData(true);
                    }
                }
        );


        setContentView(root);
    }


    // =========================================================
    // Button
    // =========================================================

    private void addButton(
            String text,
            View.OnClickListener listener) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextSize(18);

        button.setTextColor(
                Color.rgb(30, 30, 30)
        );

        button.setAllCaps(false);

        button.setGravity(
                Gravity.CENTER
        );

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

        button.setOnClickListener(
                listener
        );

        content.addView(
                button,
                params
        );
    }


    // =========================================================
    // Page
    // =========================================================

    private void openPage(
            String title) {

        content.removeAllViews();

        titleText.setText(title);

        Button back =
                new Button(this);

        back.setText(
                "←  بازگشت به منوی اصلی"
        );

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
    }


    // =========================================================
    // Text
    // =========================================================

    private TextView addText(
            String text) {

        TextView tv =
                new TextView(this);

        tv.setText(text);

        tv.setTextSize(17);

        tv.setTextColor(
                Color.DKGRAY
        );

        tv.setPadding(
                8,
                8,
                8,
                8
        );

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
    // Status
    // =========================================================

    private void setStatus(
            String text) {

        if (statusText == null ||
                statusText.getParent() != content) {

            statusText =
                    new TextView(this);

            statusText.setTextSize(17);

            statusText.setGravity(
                    Gravity.CENTER
            );

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
    // Market
    // =========================================================

    private void showMarketOverview() {

        openPage(
                "اطلاعات بازار"
        );

        setStatus(
                "در حال دریافت اطلاعات بازار..."
        );

        loadMarketData(false);
    }


    private void loadMarketData(
            final boolean returnToMenu) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String response =
                                    httpGet(
                                            MARKET_URL
                                    );

                            parseMarketWatch(
                                    response
                            );

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            if (marketItems.isEmpty()) {

                                                setStatus(
                                                        "اطلاعات بازار دریافت نشد."
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
    // Market Parser
    // =========================================================

    private void parseMarketWatch(
            String response)
            throws Exception {

        marketItems.clear();

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

            JSONObject object =
                    new JSONObject(text);

            array =
                    findArray(
                            object,
                            "marketwatch",
                            "marketWatch",
                            "marketWatchDto",
                            "data",
                            "items",
                            "result"
                    );
        }

        if (array != null) {

            parseMarketArray(array);
        }
    }


    private void parseMarketArray(
            JSONArray array) {

        if (array == null) {
            return;
        }

        for (int i = 0;
             i < array.length();
             i++) {

            try {

                Object object =
                        array.get(i);

                if (!(object instanceof JSONObject)) {
                    continue;
                }

                JSONObject o =
                        (JSONObject) object;


                MarketItem item =
                        new MarketItem();


                item.insCode =
                        cleanInsCode(
                                getString(
                                        o,
                                        "insCode",
                                        "InsCode",
                                        "instrumentId",
                                        "instrumentID"
                                )
                        );


                item.symbol =
                        cleanPersian(
                                firstNonEmpty(
                                        getString(
                                                o,
                                                "lVal18AFC"
                                        ),
                                        getString(
                                                o,
                                                "lVal18"
                                        ),
                                        getString(
                                                o,
                                                "symbol"
                                        ),
                                        getString(
                                                o,
                                                "symbolName"
                                        )
                                )
                        );


                item.name =
                        cleanPersian(
                                firstNonEmpty(
                                        getString(
                                                o,
                                                "lVal30"
                                        ),
                                        getString(
                                                o,
                                                "name"
                                        ),
                                        getString(
                                                o,
                                                "instrumentName"
                                        ),
                                        getString(
                                                o,
                                                "title"
                                        )
                                )
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
                                "priceChange",
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
                                        /
                                        item.yesterday)
                                        * 100.0;
                    }
                }


                item.percent =
                        change;


                if (item.insCode.length() > 0) {

                    marketItems.add(item);
                }

            } catch (Exception ignored) {
            }
        }
    }


    // =========================================================
    // Market Result
    // =========================================================

    private void showMarketResult() {

        content.removeAllViews();

        addBackButton();

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
                        formatNumber(
                                marketItems.size()
                        )
        );


        addText(
                "مثبت: " +
                        positive +
                        "    منفی: " +
                        negative +
                        "    بدون تغییر: " +
                        unchanged
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


    private void addMarketRow(
            MarketItem item) {

        TextView row =
                new TextView(this);


        String symbol =
                firstNonEmpty(
                        item.symbol,
                        "نماد ناشناس"
                );


        double price =
                item.last != 0
                        ? item.last
                        : item.close;


        String text =
                symbol +
                "    " +
                formatPercent(
                        item.percent
                ) +
                "\nقیمت: " +
                formatNumber(price) +
                "    دیروز: " +
                formatNumber(
                        item.yesterday
                ) +
                "\nحجم: " +
                formatNumber(
                        item.volume
                ) +
                "    ارزش: " +
                formatNumber(
                        item.value
                ) +
                "\nمعاملات: " +
                formatNumber(
                        item.trades
                );


        row.setText(text);

        row.setTextSize(16);

        row.setPadding(
                15,
                14,
                15,
                14
        );


        if (item.percent > 0) {

            row.setTextColor(
                    Color.rgb(
                            0,
                            120,
                            60
                    )
            );

        } else if (item.percent < 0) {

            row.setTextColor(
                    Color.rgb(
                            190,
                            30,
                            30
                    )
            );

        } else {

            row.setTextColor(
                    Color.DKGRAY
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
    // Smart Money
    // =========================================================

    private void showSmartMoney() {

        openPage(
                "پول هوشمند"
        );

        setStatus(
                "در حال دریافت اطلاعات بازار..."
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
    // Ensure Market
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
                                    httpGet(
                                            MARKET_URL
                                    );

                            parseMarketWatch(
                                    response
                            );


                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            if (marketItems.isEmpty()) {

                                                setStatus(
                                                        "اطلاعات بازار دریافت نشد."
                                                );

                                            } else {

                                                next.run();
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
                                                    "خطا در دریافت بازار\n\n" +
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
    // Money
    // =========================================================

    private void loadMoneyData(
            final Runnable next) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String response =
                                    httpGet(
                                            MONEY_URL
                                    );


                            parseMoney(
                                    response
                            );


                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            if (moneyItems.isEmpty()) {

                                                setStatus(
                                                        "داده پول حقیقی از TSETMC دریافت نشد."
                                                );

                                            } else {

                                                next.run();
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
                                                    "خطا در پول حقیقی\n\n" +
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
    // Money Parser
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

            JSONObject object =
                    new JSONObject(text);


            array =
                    findArray(
                            object,
                            "clientTypeAllDto",
                            "clientType",
                            "data",
                            "items",
                            "result"
                    );
        }


        if (array == null) {
            return;
        }


        for (int i = 0;
             i < array.length();
             i++) {

            try {

                Object raw =
                        array.get(i);


                if (!(raw instanceof JSONObject)) {
                    continue;
                }


                JSONObject o =
                        (JSONObject) raw;


                MoneyItem item =
                        new MoneyItem();


                item.insCode =
                        cleanInsCode(
                                getString(
                                        o,
                                        "insCode",
                                        "InsCode"
                                )
                        );


                if (item.insCode.length() == 0) {
                    continue;
                }


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


                item.buyIndividualCount =
                        getDouble(
                                o,
                                "buy_I_Count",
                                "buy_CountI"
                        );


                item.sellIndividualCount =
                        getDouble(
                                o,
                                "sell_I_Count",
                                "sell_CountI"
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
                            cleanPersian(
                                    market.symbol
                            );

                    item.price =
                            market.last != 0
                                    ? market.last
                                    : market.close;

                    item.marketVolume =
                            market.volume;

                    item.percent =
                            market.percent;

                } else {

                    item.symbol =
                            cleanPersian(
                                    getString(
                                            o,
                                            "lVal18AFC",
                                            "lVal18",
                                            "symbol"
                                    )
                            );
                }


                if (item.symbol.length() == 0) {

                    item.symbol =
                            "نماد " +
                            item.insCode;
                }


                moneyItems.add(item);


            } catch (Exception ignored) {
            }
        }
    }


    // =========================================================
    // Find Market
    // =========================================================

    private MarketItem findMarketItem(
            String insCode) {

        String target =
                cleanInsCode(insCode);


        if (target.length() == 0) {
            return null;
        }


        for (MarketItem item :
                marketItems) {

            if (target.equals(
                    cleanInsCode(
                            item.insCode
                    )
            )) {

                return item;
            }
        }


        return null;
    }


    // =========================================================
    // Smart Money Display
    // =========================================================

    private void displaySmartMoney() {

        content.removeAllViews();

        addBackButton();


        addText(
                "پول هوشمند"
        );


        addText(
                "رتبه‌بندی بر اساس خالص ارزش پول حقیقی"
        );


        addText(
                "سبز = ورود پول حقیقی\n" +
                "قرمز = خروج پول حقیقی"
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

                        double av =
                                Math.abs(
                                        a.netValue
                                );

                        double bv =
                                Math.abs(
                                        b.netValue
                                );


                        if (av == 0 &&
                                bv == 0) {

                            return Double.compare(
                                    Math.abs(
                                            b.netVolume
                                    ),
                                    Math.abs(
                                            a.netVolume
                                    )
                            );
                        }


                        return Double.compare(
                                bv,
                                av
                        );
                    }
                }
        );


        int count = 0;


        for (MoneyItem item :
                moneyItems) {

            if (count >= 50) {
                break;
            }


            if (Math.abs(
                    item.netVolume
            ) < 1 &&
                    Math.abs(
                            item.netValue
                    ) < 1) {

                continue;
            }


            addSmartMoneyRow(
                    item
            );


            count++;
        }


        if (count == 0) {

            addText(
                    "داده قابل استفاده برای پول هوشمند پیدا نشد."
            );
        }
    }


    // =========================================================
    // Smart Money Row
    // =========================================================

    private void addSmartMoneyRow(
            MoneyItem item) {

        TextView row =
                new TextView(this);


        String direction;


        if (item.netValue > 0) {

            direction =
                    "ورود پول حقیقی";

        } else if (item.netValue < 0) {

            direction =
                    "خروج پول حقیقی";

        } else if (item.netVolume > 0) {

            direction =
                    "ورود بر اساس حجم";

        } else {

            direction =
                    "خروج بر اساس حجم";
        }


        String text =
                item.symbol +
                "\n" +
                direction +
                "\nخالص ارزش: " +
                formatNumber(
                        item.netValue
                ) +
                "\nخالص حجم: " +
                formatNumber(
                        item.netVolume
                ) +
                "\nخرید حقیقی: " +
                formatNumber(
                        item.buyIndividual
                ) +
                "    فروش حقیقی: " +
                formatNumber(
                        item.sellIndividual
                );


        if (item.buyIndividualCount != 0 ||
                item.sellIndividualCount != 0) {

            text +=
                    "\nتعداد خریدار حقیقی: " +
                    formatNumber(
                            item.buyIndividualCount
                    ) +
                    "    فروشنده حقیقی: " +
                    formatNumber(
                            item.sellIndividualCount
                    );
        }


        row.setText(text);

        row.setTextSize(16);

        row.setPadding(
                15,
                15,
                15,
                15
        );


        if (item.netValue > 0 ||
                (item.netValue == 0 &&
                        item.netVolume > 0)) {

            row.setTextColor(
                    Color.rgb(
                            0,
                            120,
                            60
                    )
            );

        } else {

            row.setTextColor(
                    Color.rgb(
                            190,
                            30,
                            30
                    )
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
    // Money Flow
    // =========================================================

    private void showMoneyFlow() {

        openPage(
                "ورود و خروج پول"
        );


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


    private void displayMoneyFlow() {

        content.removeAllViews();

        addBackButton();


        double buy = 0;
        double sell = 0;

        double buyValue = 0;
        double sellValue = 0;


        for (MoneyItem item :
                moneyItems) {

            buy +=
                    item.buyIndividual;

            sell +=
                    item.sellIndividual;

            buyValue +=
                    item.buyValue;

            sellValue +=
                    item.sellValue;
        }


        double netVolume =
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
                "خالص حجم: " +
                        formatNumber(netVolume)
        );


        addText(
                "خالص ارزش پول حقیقی: " +
                        formatNumber(netValue)
        );


        addText(
                "تعداد نمادها: " +
                        formatNumber(
                                moneyItems.size()
                        )
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
                                Math.abs(
                                        b.netValue
                                ),
                                Math.abs(
                                        a.netValue
                                )
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


            if (Math.abs(
                    item.netValue
            ) < 1 &&
                    Math.abs(
                            item.netVolume
                    ) < 1) {

                continue;
            }


            addSmartMoneyRow(item);

            count++;
        }
    }


    // =========================================================
    // Search Symbols
    // =========================================================

    private void showSymbols() {

        openPage(
                "بررسی نمادها"
        );


        searchBox =
                new EditText(this);

        searchBox.setHint(
                "نام نماد؛ مثلا فولاد"
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

        search.setText(
                "جستجوی نماد"
        );

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


    private void displaySearchResult(
            String response) {

        content.removeAllViews();

        addBackButton();


        try {

            JSONArray array;


            if (response.trim().startsWith("[")) {

                array =
                        new JSONArray(response);

            } else {

                JSONObject object =
                        new JSONObject(response);

                array =
                        findArray(
                                object,
                                "instrumentSearch",
                                "data",
                                "items",
                                "result"
                        );
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
                        cleanPersian(
                                getString(
                                        o,
                                        "lVal18AFC",
                                        "lVal18",
                                        "symbol"
                                )
                        );


                String name =
                        cleanPersian(
                                getString(
                                        o,
                                        "lVal30",
                                        "name"
                                )
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
                        name +
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
    // Fundamental
    // =========================================================

    private void showFundamental() {

        openPage(
                "تحلیل بنیادی"
        );


        addText(
                "تحلیل بنیادی بورس‌یار"
        );


        addText(
                "معیارهای بنیادی:"
        );


        addText(
                "• EPS\n" +
                "• P/E\n" +
                "• ارزش بازار\n" +
                "• سودآوری\n" +
                "• رشد درآمد\n" +
                "• وضعیت صنعت"
        );


        addText(
                "اتصال اطلاعات بنیادی و Codal " +
                "در مرحله بعد تکمیل می‌شود."
        );
    }


    // =========================================================
    // Technical
    // =========================================================

    private void showTechnical() {

        openPage(
                "تحلیل تکنیکال"
        );


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
                "در مرحله بعد محاسبات واقعی " +
                "اندیکاتورها به برنامه اضافه می‌شود."
        );
    }


    // =========================================================
    // Suggestions
    // =========================================================

    private void showSuggestions() {

        openPage(
                "پیشنهادهای معاملاتی"
        );


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


    private void displaySuggestions() {

        content.removeAllViews();

        addBackButton();


        addText(
                "پیشنهادهای معاملاتی"
        );


        addText(
                "این فهرست صرفاً بر اساس داده‌های فعلی بازار مرتب شده و توصیه قطعی خرید یا فروش نیست."
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
                    "داده مناسب پیدا نشد."
            );
        }
    }


    // =========================================================
    // Back
    // =========================================================

    private void addBackButton() {

        Button back =
                new Button(this);


        back.setText(
                "←  بازگشت به منوی اصلی"
        );


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
    }


    // =========================================================
    // HTTP
    // =========================================================

    private String httpGet(
            String urlString)
            throws Exception {

        HttpURLConnection connection =
                null;


        try {

            URL url =
                    new URL(urlString);


            connection =
                    (HttpURLConnection)
                            url.openConnection();


            connection.setRequestMethod(
                    "GET"
            );


            connection.setConnectTimeout(
                    25000
            );


            connection.setReadTimeout(
                    30000
            );


            connection.setUseCaches(
                    false
            );


            connection.setDoInput(
                    true
            );


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
    // Read Stream
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
                    SSLContext.getInstance(
                            "TLS"
                    );


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

                            return hostname != null &&
                                    hostname.equals(
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
    // Find JSON Array
    // =========================================================

    private JSONArray findArray(
            JSONObject object,
            String... keys) {

        if (object == null) {
            return null;
        }


        for (String key :
                keys) {

            try {

                if (!object.has(key) ||
                        object.isNull(key)) {

                    continue;
                }


                Object value =
                        object.get(key);


                if (value instanceof JSONArray) {

                    return (JSONArray) value;
                }


                if (value instanceof JSONObject) {

                    JSONArray nested =
                            findArray(
                                    (JSONObject) value,
                                    "data",
                                    "items",
                                    "marketwatch",
                                    "marketWatch",
                                    "clientTypeAllDto",
                                    "clientType",
                                    "result"
                            );


                    if (nested != null) {
                        return nested;
                    }
                }


            } catch (Exception ignored) {
            }
        }


        return null;
    }


    // =========================================================
    // JSON String
    // =========================================================

    private String getString(
            JSONObject object,
            String... keys) {

        if (object == null) {
            return "";
        }


        for (String key :
                keys) {

            try {

                if (!object.has(key) ||
                        object.isNull(key)) {

                    continue;
                }


                Object value =
                        object.get(key);


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


                String result =
                        String.valueOf(value);


                if (!result.equalsIgnoreCase(
                        "null"
                ) &&
                        result.trim().length() > 0) {

                    return result;
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
            JSONObject object,
            String... keys) {

        if (object == null) {
            return 0;
        }


        for (String key :
                keys) {

            try {

                if (!object.has(key) ||
                        object.isNull(key)) {

                    continue;
                }


                Object value =
                        object.get(key);


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
                        normalizeDigits(
                                String.valueOf(value)
                        );


                s =
                        s.replace(
                                ",",
                                ""
                        ).trim();


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
    // Normalize Persian Digits
    // =========================================================

    private String normalizeDigits(
            String value) {

        if (value == null) {
            return "";
        }


        StringBuilder result =
                new StringBuilder();


        for (int i = 0;
             i < value.length();
             i++) {

            char c =
                    value.charAt(i);


            switch (c) {

                case '۰':
                    result.append('0');
                    break;

                case '۱':
                    result.append('1');
                    break;

                case '۲':
                    result.append('2');
                    break;

                case '۳':
                    result.append('3');
                    break;

                case '۴':
                    result.append('4');
                    break;

                case '۵':
                    result.append('5');
                    break;

                case '۶':
                    result.append('6');
                    break;

                case '۷':
                    result.append('7');
                    break;

                case '۸':
                    result.append('8');
                    break;

                case '۹':
                    result.append('9');
                    break;

                case '٠':
                    result.append('0');
                    break;

                case '١':
                    result.append('1');
                    break;

                case '٢':
                    result.append('2');
                    break;

                case '٣':
                    result.append('3');
                    break;

                case '٤':
                    result.append('4');
                    break;

                case '٥':
                    result.append('5');
                    break;

                case '٦':
                    result.append('6');
                    break;

                case '٧':
                    result.append('7');
                    break;

                case '٨':
                    result.append('8');
                    break;

                case '٩':
                    result.append('9');
                    break;

                default:
                    result.append(c);
            }
        }


        return result.toString();
    }


    // =========================================================
    // Persian Normalize
    // =========================================================

    private String cleanPersian(
            String value) {

        if (value == null) {
            return "";
        }


        String result =
                value.trim();


        result =
                result.replace(
                        'ي',
                        'ی'
                );


        result =
                result.replace(
                        'ى',
                        'ی'
                );


        result =
                result.replace(
                        'ك',
                        'ک'
                );


        result =
                result.replace(
                        'ۀ',
                        'ه'
                );


        result =
                result.replace(
                        '\u200C',
                        '‌'
                );


        return result;
    }


    // =========================================================
    // InsCode Normalize
    // =========================================================

    private String cleanInsCode(
            String value) {

        if (value == null) {
            return "";
        }


        return normalizeDigits(
                value.trim()
        );
    }


    // =========================================================
    // First Non Empty
    // =========================================================

    private String firstNonEmpty(
            String... values) {

        if (values == null) {
            return "";
        }


        for (String value :
                values) {

            if (value != null &&
                    value.trim().length() > 0) {

                return value.trim();
            }
        }


        return "";
    }


    // =========================================================
    // Number
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
    // Percent
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
    // Error
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
                    "خطای گواهی SSL/TLS.";
        }


        if (msg.contains(
                "Unable to resolve host"
        )) {

            return
                    "اینترنت یا DNS در دسترس نیست.";
        }


        if (msg.toLowerCase(
                Locale.US
        ).contains(
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
    // Keyboard
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
    // Market Item
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
    // Money Item
    // =========================================================

    private static class MoneyItem {

        String insCode = "";

        String symbol = "";

        double buyIndividual = 0;

        double sellIndividual = 0;

        double buyValue = 0;

        double sellValue = 0;

        double buyIndividualCount = 0;

        double sellIndividualCount = 0;

        double netVolume = 0;

        double netValue = 0;

        double price = 0;

        double marketVolume = 0;

        double percent = 0;
    }
}
