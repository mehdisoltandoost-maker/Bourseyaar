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
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends Activity {

    private LinearLayout root;
    private LinearLayout content;
    private TextView title;
    private TextView status;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler();

    private final List<MarketItem> marketItems = new ArrayList<>();

    private static final String BASE_URL =
            "https://cdn.tsetmc.com/api/";

    private static final String MARKET_URL =
            BASE_URL +
            "ClosingPrice/GetMarketWatch" +
            "?market=0" +
            "&industrialGroup=" +
            "&paperTypes[0]=1" +
            "&paperTypes[1]=2" +
            "&paperTypes[2]=3" +
            "&paperTypes[3]=4" +
            "&paperTypes[4]=5" +
            "&paperTypes[5]=6" +
            "&paperTypes[6]=7" +
            "&paperTypes[7]=8" +
            "&paperTypes[8]=9" +
            "&showTraded=false" +
            "&withBestLimits=false" +
            "&hEven=0" +
            "&RefID=0";

    private static final String MONEY_URL =
            BASE_URL + "ClientType/GetClientTypeAll";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupTsetmcSsl();
        buildUi();
        loadMarketData();
    }

    private void buildUi() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        title = new TextView(this);
        title.setText("بورس‌یار");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(20, 70, 120));
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(10, 25, 10, 15);

        root.addView(title,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(12, 5, 12, 5);

        addButton(menu, "اطلاعات کلی بازار", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMarketOverview();
            }
        });

        addButton(menu, "پول هوشمند", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSmartMoney();
            }
        });

        addButton(menu, "ورود و خروج پول", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoneyFlow();
            }
        });

        addButton(menu, "تحلیل بنیادی", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFundamental();
            }
        });

        addButton(menu, "تحلیل تکنیکال", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTechnical();
            }
        });

        addButton(menu, "بررسی نمادها", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSymbols();
            }
        });

        addButton(menu, "پیشنهادهای معاملاتی", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSuggestions();
            }
        });

        addButton(menu, "به‌روزرسانی اطلاعات", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMarketData();
            }
        });

        root.addView(menu,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

        status = new TextView(this);
        status.setText("در حال اتصال به TSETMC...");
        status.setTextSize(14);
        status.setTextColor(Color.DKGRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(10, 10, 10, 10);

        root.addView(status,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

        ScrollView scrollView = new ScrollView(this);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(15, 10, 15, 30);

        scrollView.addView(content);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(root);

        showWelcome();
    }

    private void addButton(
            LinearLayout parent,
            String text,
            View.OnClickListener listener) {

        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setOnClickListener(listener);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 4, 0, 4);

        parent.addView(button, params);
    }

    private void showWelcome() {

        clearContent();

        addText(
                "بورس‌یار",
                24,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "دستیار تحلیل بازار سرمایه ایران",
                17,
                Color.DKGRAY,
                false
        );

        addText(
                "برای دریافت اطلاعات بازار، یکی از گزینه‌های بالا را انتخاب کنید.",
                15,
                Color.GRAY,
                false
        );

        addText(
                "وضعیت: در انتظار دریافت اطلاعات TSETMC",
                15,
                Color.DKGRAY,
                false
        );
    }

    private void clearContent() {
        if (content != null) {
            content.removeAllViews();
        }
    }

    private void addText(
            String text,
            int size,
            int color,
            boolean bold) {

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(color);
        tv.setPadding(5, 8, 5, 8);

        if (bold) {
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        content.addView(tv);
    }

    private void setStatus(final String text) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (status != null) {
                    status.setText(text);
                }
            }
        });
    }

    private void loadMarketData() {

        setStatus("در حال دریافت اطلاعات بازار از TSETMC...");

        clearContent();

        addText(
                "دریافت اطلاعات بازار",
                21,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "لطفاً چند ثانیه صبر کنید...",
                15,
                Color.DKGRAY,
                false
        );

        executor.execute(new Runnable() {
            @Override
            public void run() {

                try {

                    String response = httpGet(MARKET_URL);

                    if (response == null || response.trim().length() == 0) {
                        throw new Exception(
                                "پاسخ خالی از TSETMC دریافت شد."
                        );
                    }

                    parseMarketWatch(response);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (marketItems.size() > 0) {

                                setStatus(
                                        "اتصال موفق؛ " +
                                        marketItems.size() +
                                        " نماد دریافت شد."
                                );

                                showMarketOverview();

                            } else {

                                setStatus(
                                        "اتصال برقرار شد ولی داده بازار خالی است."
                                );

                                clearContent();

                                addText(
                                        "اتصال به TSETMC برقرار شد",
                                        20,
                                        Color.rgb(20, 100, 50),
                                        true
                                );

                                addText(
                                        "اما پاسخ دیده‌بان بازار خالی است.",
                                        16,
                                        Color.DKGRAY,
                                        false
                                );

                                addText(
                                        "ممکن است TSETMC در این لحظه داده کامل بازار را ارسال نکند.",
                                        15,
                                        Color.GRAY,
                                        false
                                );
                            }
                        }
                    });

                } catch (final Exception e) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            setStatus("خطا در دریافت اطلاعات");

                            clearContent();

                            addText(
                                    "خطا در اتصال به TSETMC",
                                    21,
                                    Color.rgb(180, 30, 30),
                                    true
                            );

                            addText(
                                    getReadableError(e),
                                    15,
                                    Color.DKGRAY,
                                    false
                            );

                            addText(
                                    "دوباره گزینه «به‌روزرسانی اطلاعات» را امتحان کنید.",
                                    15,
                                    Color.GRAY,
                                    false
                            );
                        }
                    });
                }
            }
        });
    }

    private String httpGet(String address) throws Exception {

        URL url = new URL(address);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        if (connection instanceof HttpsURLConnection) {

            HttpsURLConnection https =
                    (HttpsURLConnection) connection;

            https.setSSLSocketFactory(
                    getUnsafeSslContext().getSocketFactory()
            );

            https.setHostnameVerifier(
                    new HostnameVerifier() {
                        @Override
                        public boolean verify(
                                String hostname,
                                SSLSession session) {

                            return "cdn.tsetmc.com".equalsIgnoreCase(hostname);
                        }
                    }
            );
        }

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(30000);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json,text/plain,*/*"
        );

        connection.setRequestProperty(
                "Accept-Language",
                "fa-IR,fa;q=0.9,en-US;q=0.8,en;q=0.7"
        );

        connection.setRequestProperty(
                "Cache-Control",
                "no-cache"
        );

        connection.setRequestProperty(
                "Pragma",
                "no-cache"
        );

        connection.setRequestProperty(
                "Origin",
                "https://tsetmc.com"
        );

        connection.setRequestProperty(
                "Referer",
                "https://tsetmc.com/"
        );

        int code = connection.getResponseCode();

        InputStream input;

        if (code >= 200 && code < 300) {
            input = connection.getInputStream();
        } else {
            input = connection.getErrorStream();

            if (input == null) {
                throw new Exception("HTTP " + code);
            }
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                input,
                                "UTF-8"
                        )
                );

        StringBuilder result =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();
        connection.disconnect();

        if (code < 200 || code >= 300) {
            throw new Exception(
                    "HTTP " + code + "\n" + result.toString()
            );
        }

        return result.toString();
    }

    private SSLContext getUnsafeSslContext() throws Exception {

        TrustManager[] trustAll =
                new TrustManager[] {
                        new X509TrustManager() {

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

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };

        SSLContext context =
                SSLContext.getInstance("TLS");

        context.init(
                null,
                trustAll,
                new java.security.SecureRandom()
        );

        return context;
    }

    private void setupTsetmcSsl() {

        try {

            SSLContext context =
                    getUnsafeSslContext();

            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory()
            );

            HttpsURLConnection.setDefaultHostnameVerifier(
                    new HostnameVerifier() {
                        @Override
                        public boolean verify(
                                String hostname,
                                SSLSession session) {

                            return "cdn.tsetmc.com"
                                    .equalsIgnoreCase(hostname);
                        }
                    }
            );

        } catch (Exception ignored) {
        }
    }

    private void parseMarketWatch(String response)
            throws Exception {

        marketItems.clear();

        JSONObject rootObject =
                new JSONObject(response);

        JSONArray array = null;

        String[] keys = {
                "marketwatch",
                "marketWatch",
                "marketWatchDto"
        };

        for (String key : keys) {

            if (rootObject.has(key)) {

                Object value =
                        rootObject.get(key);

                if (value instanceof JSONArray) {
                    array = (JSONArray) value;
                    break;
                }
            }
        }

        if (array == null) {

            if (rootObject.length() == 0) {
                return;
            }

            return;
        }

        for (int i = 0; i < array.length(); i++) {

            JSONObject obj =
                    array.optJSONObject(i);

            if (obj == null) {
                continue;
            }

            MarketItem item =
                    new MarketItem();

            item.insCode =
                    getString(
                            obj,
                            "insCode",
                            ""
                    );

            item.symbol =
                    firstNonEmpty(
                            getString(obj, "lVal18AFC", ""),
                            getString(obj, "lVal18", ""),
                            getString(obj, "symbol", ""),
                            getString(obj, "symbolName", "")
                    );

            item.name =
                    firstNonEmpty(
                            getString(obj, "lVal30", ""),
                            getString(obj, "name", ""),
                            getString(obj, "title", "")
                    );

            item.last =
                    getDouble(
                            obj,
                            "pDrCotVal",
                            getDouble(
                                    obj,
                                    "pl",
                                    getDouble(obj, "last", 0)
                            )
                    );

            item.close =
                    getDouble(
                            obj,
                            "pClosing",
                            getDouble(
                                    obj,
                                    "pc",
                                    getDouble(obj, "close", 0)
                            )
                    );

            item.yesterday =
                    getDouble(
                            obj,
                            "priceYesterday",
                            getDouble(
                                    obj,
                                    "py",
                                    getDouble(obj, "yesterday", 0)
                            )
                    );

            item.first =
                    getDouble(
                            obj,
                            "priceFirst",
                            getDouble(obj, "pf", 0)
                    );

            item.min =
                    getDouble(
                            obj,
                            "priceMin",
                            getDouble(obj, "pmin", 0)
                    );

            item.max =
                    getDouble(
                            obj,
                            "priceMax",
                            getDouble(obj, "pmax", 0)
                    );

            item.volume =
                    getDouble(
                            obj,
                            "qTotTran5J",
                            getDouble(
                                    obj,
                                    "tvol",
                                    getDouble(obj, "volume", 0)
                            )
                    );

            item.value =
                    getDouble(
                            obj,
                            "qTotCap",
                            getDouble(
                                    obj,
                                    "tval",
                                    getDouble(obj, "value", 0)
                            )
                    );

            item.trades =
                    getDouble(
                            obj,
                            "zTotTran",
                            getDouble(
                                    obj,
                                    "tno",
                                    getDouble(obj, "trades", 0)
                            )
                    );

            item.percent =
                    getDouble(
                            obj,
                            "percent",
                            getDouble(
                                    obj,
                                    "priceChangePercent",
                                    0
                            )
                    );

            if (item.percent == 0 &&
                    item.yesterday != 0) {

                item.percent =
                        ((item.close - item.yesterday)
                                / item.yesterday) * 100.0;
            }

            if (item.symbol.length() > 0 ||
                    item.insCode.length() > 0) {

                marketItems.add(item);
            }
        }
    }

    private void showMarketOverview() {

        clearContent();

        addText(
                "اطلاعات کلی بازار",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        if (marketItems.size() == 0) {

            addText(
                    "هنوز اطلاعات بازار دریافت نشده است.",
                    16,
                    Color.DKGRAY,
                    false
            );

            return;
        }

        int positive = 0;
        int negative = 0;
        int unchanged = 0;

        double totalVolume = 0;
        double totalValue = 0;
        double totalTrades = 0;

        for (MarketItem item : marketItems) {

            totalVolume += item.volume;
            totalValue += item.value;
            totalTrades += item.trades;

            if (item.percent > 0.001) {
                positive++;
            } else if (item.percent < -0.001) {
                negative++;
            } else {
                unchanged++;
            }
        }

        addText(
                "تعداد نمادهای دریافت‌شده: " +
                        marketItems.size(),
                16,
                Color.DKGRAY,
                false
        );

        addText(
                "مثبت: " + positive +
                        "    منفی: " + negative +
                        "    بدون تغییر: " + unchanged,
                16,
                Color.DKGRAY,
                false
        );

        addText(
                "حجم معاملات: " +
                        formatNumber(totalVolume),
                15,
                Color.DKGRAY,
                false
        );

        addText(
                "ارزش معاملات: " +
                        formatNumber(totalValue),
                15,
                Color.DKGRAY,
                false
        );

        addText(
                "تعداد معاملات: " +
                        formatNumber(totalTrades),
                15,
                Color.DKGRAY,
                false
        );

        addText(
                "────── نمادها ──────",
                16,
                Color.rgb(20, 70, 120),
                true
        );

        int limit =
                Math.min(30, marketItems.size());

        for (int i = 0; i < limit; i++) {

            MarketItem item =
                    marketItems.get(i);

            addMarketRow(item);
        }
    }

    private void addMarketRow(MarketItem item) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(8, 10, 8, 10);

        String symbol =
                item.symbol.length() > 0
                        ? item.symbol
                        : "بدون نماد";

        String line1 =
                symbol +
                "    " +
                formatPercent(item.percent);

        TextView first =
                new TextView(this);

        first.setText(line1);
        first.setTextSize(17);
        first.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        if (item.percent > 0) {
            first.setTextColor(
                    Color.rgb(0, 130, 60)
            );
        } else if (item.percent < 0) {
            first.setTextColor(
                    Color.rgb(190, 30, 30)
            );
        } else {
            first.setTextColor(Color.DKGRAY);
        }

        box.addView(first);

        TextView second =
                new TextView(this);

        second.setText(
                "آخرین: " +
                        formatNumber(item.last) +
                        "    پایانی: " +
                        formatNumber(item.close)
        );

        second.setTextSize(14);
        second.setTextColor(Color.DKGRAY);

        box.addView(second);

        TextView third =
                new TextView(this);

        third.setText(
                "حجم: " +
                        formatNumber(item.volume) +
                        "    ارزش: " +
                        formatNumber(item.value)
        );

        third.setTextSize(13);
        third.setTextColor(Color.GRAY);

        box.addView(third);

        content.addView(box);
    }

    private void showSmartMoney() {

        clearContent();

        addText(
                "پول هوشمند",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "در حال دریافت اطلاعات حقیقی و حقوقی...",
                15,
                Color.DKGRAY,
                false
        );

        executor.execute(new Runnable() {
            @Override
            public void run() {

                try {

                    String response =
                            httpGet(MONEY_URL);

                    final List<MoneyItem> list =
                            parseMoney(response);

                    Collections.sort(
                            list,
                            new Comparator<MoneyItem>() {
                                @Override
                                public int compare(
                                        MoneyItem a,
                                        MoneyItem b) {

                                    return Double.compare(
                                            b.netValue,
                                            a.netValue
                                    );
                                }
                            }
                    );

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            clearContent();

                            addText(
                                    "پول هوشمند",
                                    22,
                                    Color.rgb(20, 70, 120),
                                    true
                            );

                            if (list.size() == 0) {

                                addText(
                                        "داده پول حقیقی/حقوقی دریافت نشد.",
                                        16,
                                        Color.DKGRAY,
                                        false
                                );

                                return;
                            }

                            int limit =
                                    Math.min(30, list.size());

                            for (int i = 0;
                                 i < limit;
                                 i++) {

                                MoneyItem item =
                                        list.get(i);

                                addMoneyRow(item);
                            }
                        }
                    });

                } catch (final Exception e) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            clearContent();

                            addText(
                                    "خطا در دریافت پول هوشمند",
                                    21,
                                    Color.RED,
                                    true
                            );

                            addText(
                                    getReadableError(e),
                                    15,
                                    Color.DKGRAY,
                                    false
                            );
                        }
                    });
                }
            }
        });
    }

    private List<MoneyItem> parseMoney(
            String response) throws Exception {

        List<MoneyItem> result =
                new ArrayList<>();

        JSONObject rootObject =
                new JSONObject(response);

        JSONArray array = null;

        if (rootObject.has("clientTypeAllDto")) {

            Object value =
                    rootObject.get("clientTypeAllDto");

            if (value instanceof JSONArray) {
                array = (JSONArray) value;
            }
        }

        if (array == null) {
            return result;
        }

        for (int i = 0;
             i < array.length();
             i++) {

            JSONObject obj =
                    array.optJSONObject(i);

            if (obj == null) {
                continue;
            }

            MoneyItem item =
                    new MoneyItem();

            item.insCode =
                    getString(
                            obj,
                            "insCode",
                            ""
                    );

            item.buyIndividual =
                    getDouble(
                            obj,
                            "buy_I_Volume",
                            getDouble(
                                    obj,
                                    "buyIVolume",
                                    getDouble(
                                            obj,
                                            "nBuyVolume",
                                            0
                                    )
                            )
                    );

            item.sellIndividual =
                    getDouble(
                            obj,
                            "sell_I_Volume",
                            getDouble(
                                    obj,
                                    "sellIVolume",
                                    getDouble(
                                            obj,
                                            "nSellVolume",
                                            0
                                    )
                            )
                    );

            item.buyValue =
                    getDouble(
                            obj,
                            "buy_I_Value",
                            0
                    );

            item.sellValue =
                    getDouble(
                            obj,
                            "sell_I_Value",
                            0
                    );

            item.netValue =
                    item.buyValue -
                    item.sellValue;

            item.netVolume =
                    item.buyIndividual -
                    item.sellIndividual;

            MarketItem market =
                    findMarketItem(
                            item.insCode
                    );

            if (market != null) {
                item.symbol = market.symbol;
            }

            if (item.symbol.length() > 0 ||
                    Math.abs(item.netVolume) > 0) {

                result.add(item);
            }
        }

        return result;
    }

    private MarketItem findMarketItem(
            String insCode) {

        for (MarketItem item : marketItems) {

            if (item.insCode.equals(insCode)) {
                return item;
            }
        }

        return null;
    }

    private void addMoneyRow(
            MoneyItem item) {

        TextView tv =
                new TextView(this);

        String symbol =
                item.symbol.length() > 0
                        ? item.symbol
                        : item.insCode;

        String text =
                symbol +
                "\nخالص حجم حقیقی: " +
                formatNumber(item.netVolume) +
                "\nخرید حقیقی: " +
                formatNumber(item.buyIndividual) +
                "    فروش حقیقی: " +
                formatNumber(item.sellIndividual);

        if (item.buyValue != 0 ||
                item.sellValue != 0) {

            text +=
                    "\nخالص ارزش: " +
                    formatNumber(item.netValue);
        }

        tv.setText(text);
        tv.setTextSize(15);
        tv.setTextColor(Color.DKGRAY);
        tv.setPadding(8, 10, 8, 10);

        if (item.netVolume > 0) {

            tv.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            tv.setTextColor(
                    Color.rgb(0, 120, 60)
            );

        } else if (item.netVolume < 0) {

            tv.setTextColor(
                    Color.rgb(180, 30, 30)
            );
        }

        content.addView(tv);
    }

    private void showMoneyFlow() {

        clearContent();

        addText(
                "ورود و خروج پول",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "در حال محاسبه جریان پول حقیقی...",
                15,
                Color.DKGRAY,
                false
        );

        executor.execute(new Runnable() {
            @Override
            public void run() {

                try {

                    String response =
                            httpGet(MONEY_URL);

                    List<MoneyItem> list =
                            parseMoney(response);

                    double buy = 0;
                    double sell = 0;
                    double net = 0;

                    for (MoneyItem item : list) {

                        buy += item.buyIndividual;
                        sell += item.sellIndividual;
                        net += item.netVolume;
                    }

                    final double totalBuy = buy;
                    final double totalSell = sell;
                    final double totalNet = net;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            clearContent();

                            addText(
                                    "ورود و خروج پول",
                                    22,
                                    Color.rgb(20, 70, 120),
                                    true
                            );

                            addText(
                                    "خرید حقیقی: " +
                                            formatNumber(totalBuy),
                                    16,
                                    Color.DKGRAY,
                                    false
                            );

                            addText(
                                    "فروش حقیقی: " +
                                            formatNumber(totalSell),
                                    16,
                                    Color.DKGRAY,
                                    false
                            );

                            addText(
                                    "خالص جریان حجم: " +
                                            formatNumber(totalNet),
                                    18,
                                    totalNet >= 0
                                            ? Color.rgb(0, 120, 60)
                                            : Color.rgb(180, 30, 30),
                                    true
                            );

                            addText(
                                    "توجه: این مقدار بر اساس حجم خرید و فروش حقیقی محاسبه شده است.",
                                    14,
                                    Color.GRAY,
                                    false
                            );
                        }
                    });

                } catch (final Exception e) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            clearContent();

                            addText(
                                    "خطا در دریافت جریان پول",
                                    21,
                                    Color.RED,
                                    true
                            );

                            addText(
                                    getReadableError(e),
                                    15,
                                    Color.DKGRAY,
                                    false
                            );
                        }
                    });
                }
            }
        });
    }

    private void showFundamental() {

        clearContent();

        addText(
                "تحلیل بنیادی",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "این بخش در حال آماده‌سازی است.",
                17,
                Color.DKGRAY,
                false
        );

        addText(
                "در مرحله بعد EPS، P/E، فروش، سود و اطلاعات کدال به آن اضافه می‌شود.",
                15,
                Color.GRAY,
                false
        );
    }

    private void showTechnical() {

        clearContent();

        addText(
                "تحلیل تکنیکال",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "برای تحلیل تکنیکال ابتدا نماد مورد نظر را جستجو کنید.",
                16,
                Color.DKGRAY,
                false
        );

        addText(
                "در نسخه بعدی RSI، MACD، میانگین متحرک و روند قیمت اضافه می‌شود.",
                15,
                Color.GRAY,
                false
        );
    }

    private void showSymbols() {

        clearContent();

        addText(
                "بررسی نمادها",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        final EditText input =
                new EditText(this);

        input.setHint("مثلاً خودرو");
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setInputType(
                InputType.TYPE_CLASS_TEXT
        );

        content.addView(
                input,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        Button search =
                new Button(this);

        search.setText("جستجوی نماد");
        search.setAllCaps(false);

        content.addView(search);

        final TextView result =
                new TextView(this);

        result.setTextSize(15);
        result.setTextColor(Color.DKGRAY);
        result.setPadding(5, 15, 5, 15);

        content.addView(result);

        search.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String symbol =
                                input.getText()
                                        .toString()
                                        .trim();

                        if (symbol.length() == 0) {

                            result.setText(
                                    "نام نماد را وارد کنید."
                            );

                            return;
                        }

                        hideKeyboard(input);

                        result.setText(
                                "در حال جستجو..."
                        );

                        executor.execute(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        try {

                                            String encoded =
                                                    URLEncoder.encode(
                                                            symbol,
                                                            "UTF-8"
                                                    );

                                            String url =
                                                    BASE_URL +
                                                    "Instrument/GetInstrumentSearch/" +
                                                    encoded;

                                            String response =
                                                    httpGet(url);

                                            final String text =
                                                    formatSearchResult(
                                                            response
                                                    );

                                            handler.post(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            result.setText(text);
                                                        }
                                                    }
                                            );

                                        } catch (
                                                final Exception e) {

                                            handler.post(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            result.setText(
                                                                    getReadableError(e)
                                                            );
                                                        }
                                                    }
                                            );
                                        }
                                    }
                                }
                        );
                    }
                }
        );
    }

    private String formatSearchResult(
            String response) throws Exception {

        JSONObject rootObject =
                new JSONObject(response);

        JSONArray array = null;

        if (rootObject.has("instrumentSearch")) {

            Object value =
                    rootObject.get("instrumentSearch");

            if (value instanceof JSONArray) {
                array = (JSONArray) value;
            }
        }

        if (array == null ||
                array.length() == 0) {

            return "نمادی پیدا نشد.";
        }

        StringBuilder text =
                new StringBuilder();

        text.append("نتایج جستجو:\n\n");

        int limit =
                Math.min(10, array.length());

        for (int i = 0; i < limit; i++) {

            JSONObject obj =
                    array.optJSONObject(i);

            if (obj == null) {
                continue;
            }

            String code =
                    getString(
                            obj,
                            "insCode",
                            ""
                    );

            String symbol =
                    firstNonEmpty(
                            getString(
                                    obj,
                                    "lVal18AFC",
                                    ""
                            ),
                            getString(
                                    obj,
                                    "lVal18",
                                    ""
                            )
                    );

            String name =
                    getString(
                            obj,
                            "lVal30",
                            ""
                    );

            text.append(
                    symbol
            );

            if (name.length() > 0) {

                text.append(
                        " - "
                );

                text.append(name);
            }

            text.append(
                    "\nکد: "
            );

            text.append(code);

            text.append(
                    "\n\n"
            );
        }

        return text.toString();
    }

    private void showSuggestions() {

        clearContent();

        addText(
                "پیشنهادهای معاملاتی",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        if (marketItems.size() == 0) {

            addText(
                    "ابتدا اطلاعات بازار را دریافت کنید.",
                    16,
                    Color.DKGRAY,
                    false
            );

            return;
        }

        List<MarketItem> candidates =
                new ArrayList<>();

        for (MarketItem item :
                marketItems) {

            if (item.yesterday <= 0) {
                continue;
            }

            if (item.volume <= 0) {
                continue;
            }

            if (item.value <= 0) {
                continue;
            }

            if (item.percent > 0) {
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
                                b.percent,
                                a.percent
                        );
                    }
                }
        );

        addText(
                "این فهرست صرفاً غربال اولیه بازار است و توصیه خرید یا فروش نیست.",
                14,
                Color.GRAY,
                false
        );

        if (candidates.size() == 0) {

            addText(
                    "در حال حاضر نماد مناسب بر اساس این فیلتر پیدا نشد.",
                    16,
                    Color.DKGRAY,
                    false
            );

            return;
        }

        int limit =
                Math.min(
                        20,
                        candidates.size()
                );

        for (int i = 0;
             i < limit;
             i++) {

            addMarketRow(
                    candidates.get(i)
            );
        }
    }

    private static String getString(
            JSONObject object,
            String key,
            String defaultValue) {

        try {

            Object value =
                    object.opt(key);

            if (value == null ||
                    value == JSONObject.NULL) {

                return defaultValue;
            }

            return String.valueOf(value);

        } catch (Exception e) {

            return defaultValue;
        }
    }

    private static double getDouble(
            JSONObject object,
            String key,
            double defaultValue) {

        try {

            Object value =
                    object.opt(key);

            if (value == null ||
                    value == JSONObject.NULL) {

                return defaultValue;
            }

            if (value instanceof Number) {

                return ((Number) value)
                        .doubleValue();
            }

            String text =
                    String.valueOf(value)
                            .replace(",", "")
                            .trim();

            if (text.length() == 0) {
                return defaultValue;
            }

            return Double.parseDouble(text);

        } catch (Exception e) {

            return defaultValue;
        }
    }

    private static String firstNonEmpty(
            String... values) {

        for (String value : values) {

            if (value != null &&
                    value.trim().length() > 0) {

                return value;
            }
        }

        return "";
    }

    private static String formatNumber(
            double value) {

        if (Double.isNaN(value) ||
                Double.isInfinite(value)) {

            return "0";
        }

        if (Math.abs(value) >= 1000000000) {

            return String.format(
                    Locale.US,
                    "%.2f B",
                    value / 1000000000.0
            );
        }

        if (Math.abs(value) >= 1000000) {

            return String.format(
                    Locale.US,
                    "%.2f M",
                    value / 1000000.0
            );
        }

        return String.format(
                Locale.US,
                "%,.0f",
                value
        );
    }

    private static String formatPercent(
            double value) {

        return String.format(
                Locale.US,
                "%+.2f%%",
                value
        );
    }

    private static String getReadableError(
            Exception e) {

        String message =
                e.getMessage();

        if (message == null ||
                message.trim().length() == 0) {

            return "خطای نامشخص";
        }

        return message;
    }

    private void hideKeyboard(
            View view) {

        try {

            InputMethodManager imm =
                    (InputMethodManager)
                            getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );

            if (imm != null) {

                imm.hideSoftInputFromWindow(
                        view.getWindowToken(),
                        0
                );
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        try {
            executor.shutdownNow();
        } catch (Exception ignored) {
        }
    }

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
