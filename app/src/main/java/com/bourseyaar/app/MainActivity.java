package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class MainActivity extends Activity {

    private LinearLayout content;
    private TextView statusText;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final List<MarketItem> marketItems =
            new ArrayList<>();

    private static final String API_BASE =
            "https://cdn.tsetmc.com/api/";

    private static final String TSETMC_HOST =
            "cdn.tsetmc.com";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13) " +
            "AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36";

    private SSLSocketFactory tsetmcSslSocketFactory;
    private HostnameVerifier tsetmcHostnameVerifier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupTsetmcSsl();

        buildMainPage();

        loadMarketData();
    }


    @Override
    protected void onDestroy() {

        try {
            executor.shutdownNow();
        } catch (Exception ignored) {
        }

        super.onDestroy();
    }


    private void setupTsetmcSsl() {

        try {

            TrustManager[] trustAllCerts =
                    new TrustManager[]{
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


            SSLContext sslContext =
                    SSLContext.getInstance("TLS");


            sslContext.init(
                    null,
                    trustAllCerts,
                    new SecureRandom());


            tsetmcSslSocketFactory =
                    sslContext.getSocketFactory();


            tsetmcHostnameVerifier =
                    new HostnameVerifier() {

                        @Override
                        public boolean verify(
                                String hostname,
                                SSLSession session) {

                            return TSETMC_HOST.equalsIgnoreCase(
                                    hostname);
                        }
                    };

        } catch (Exception e) {

            tsetmcSslSocketFactory = null;
            tsetmcHostnameVerifier = null;
        }
    }


    private void buildMainPage() {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setBackgroundColor(Color.WHITE);


        content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL);

        content.setGravity(
                Gravity.CENTER_HORIZONTAL);

        content.setPadding(
                24,
                24,
                24,
                24);


        scroll.addView(content);

        setContentView(scroll);

        showMainMenu();
    }


    private void clearContent() {

        if (content != null) {
            content.removeAllViews();
        }
    }


    private TextView title(String value) {

        TextView t =
                new TextView(this);

        t.setText(value);

        t.setTextSize(28);

        t.setTextColor(
                Color.rgb(20, 65, 100));

        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD);

        t.setGravity(
                Gravity.CENTER);

        t.setPadding(
                10,
                20,
                10,
                25);

        return t;
    }


    private TextView text(
            String value,
            int size) {

        TextView t =
                new TextView(this);

        t.setText(value);

        t.setTextSize(size);

        t.setTextColor(
                Color.DKGRAY);

        t.setGravity(
                Gravity.RIGHT);

        t.setPadding(
                12,
                10,
                12,
                10);

        return t;
    }


    private Button makeButton(
            String name) {

        Button b =
                new Button(this);

        b.setText(name);

        b.setTextSize(18);

        b.setAllCaps(false);

        b.setTextColor(
                Color.DKGRAY);


        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);


        p.setMargins(
                0,
                7,
                0,
                7);


        b.setLayoutParams(p);

        return b;
    }


    private void addBackButton() {

        Button back =
                makeButton(
                        "⬅️ بازگشت به صفحه اصلی");

        back.setOnClickListener(
                v -> showMainMenu());

        content.addView(back);
    }


    private void showMainMenu() {

        clearContent();


        content.addView(
                title("بورس‌یار 📈"));


        statusText =
                text(
                        "وضعیت اتصال: در حال بررسی...",
                        16);

        statusText.setGravity(
                Gravity.CENTER);

        content.addView(statusText);


        Button market =
                makeButton(
                        "📊 اطلاعات کلی بازار");

        market.setOnClickListener(
                v -> showMarketOverview());

        content.addView(market);


        Button smart =
                makeButton(
                        "💵 پول هوشمند");

        smart.setOnClickListener(
                v -> showSmartMoney());

        content.addView(smart);


        Button money =
                makeButton(
                        "🔄 ورود و خروج پول");

        money.setOnClickListener(
                v -> showMoneyFlow());

        content.addView(money);


        Button fundamental =
                makeButton(
                        "📚 تحلیل بنیادی");

        fundamental.setOnClickListener(
                v -> showFundamental());

        content.addView(fundamental);


        Button technical =
                makeButton(
                        "📈 تحلیل تکنیکال");

        technical.setOnClickListener(
                v -> showTechnical());

        content.addView(technical);


        Button symbols =
                makeButton(
                        "🔎 بررسی نمادها");

        symbols.setOnClickListener(
                v -> showSymbols());

        content.addView(symbols);


        Button suggestions =
                makeButton(
                        "💡 پیشنهادهای معاملاتی");

        suggestions.setOnClickListener(
                v -> showSuggestions());

        content.addView(suggestions);


        Button refresh =
                makeButton(
                        "🔄 دریافت دوباره اطلاعات بازار");

        refresh.setOnClickListener(
                v -> loadMarketData());

        content.addView(refresh);


        TextView info =
                text(
                        "\nبورس‌یار\n\n" +
                        "اتصال مستقیم به سرویس TSETMC\n" +
                        "دریافت اطلاعات واقعی بازار",
                        16);

        info.setGravity(
                Gravity.CENTER);

        content.addView(info);
    }


    private void loadMarketData() {

        if (statusText != null) {

            statusText.setText(
                    "🟡 در حال اتصال به TSETMC...");
        }


        executor.execute(() -> {

            Exception lastError = null;


            for (int attempt = 1;
                 attempt <= 3;
                 attempt++) {

                try {

                    String url =
                            API_BASE +
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


                    String response =
                            httpGet(url);


                    if (response == null ||
                            response.trim().isEmpty()) {

                        throw new Exception(
                                "پاسخ TSETMC خالی است.");
                    }


                    parseMarketWatch(response);


                    /*
                     * اگر پاسخ سالم باشد ولی بازار خالی باشد،
                     * دیگر آن را خطای اتصال محسوب نمی‌کنیم.
                     */
                    if (marketItems.isEmpty()) {

                        final String preview =
                                safePreview(response);

                        handler.post(() -> {

                            if (statusText != null) {

                                statusText.setText(
                                        "🟡 اتصال به TSETMC برقرار شد\n\n" +
                                        "اما اطلاعات بازار خالی است.\n\n" +
                                        "پیش‌نمایش پاسخ:\n" +
                                        preview);
                            }
                        });

                        return;
                    }


                    final int count =
                            marketItems.size();


                    handler.post(() -> {

                        if (statusText != null) {

                            statusText.setText(
                                    "🟢 اتصال برقرار شد\n" +
                                    count +
                                    " نماد از TSETMC دریافت شد.");
                        }
                    });


                    return;


                } catch (Exception e) {

                    lastError = e;


                    if (attempt < 3) {

                        try {

                            Thread.sleep(1500);

                        } catch (InterruptedException ignored) {

                            Thread.currentThread()
                                    .interrupt();

                            break;
                        }
                    }
                }
            }


            final String error =
                    getReadableError(lastError);


            handler.post(() -> {

                if (statusText != null) {

                    statusText.setText(
                            "🔴 اتصال TSETMC برقرار نشد\n\n" +
                            error);
                }
            });
        });
    }


    private String httpGet(
            String address)
            throws Exception {

        HttpURLConnection connection = null;


        try {

            URL url =
                    new URL(address);


            connection =
                    (HttpURLConnection)
                            url.openConnection();


            connection.setRequestMethod(
                    "GET");

            connection.setConnectTimeout(
                    20000);

            connection.setReadTimeout(
                    30000);

            connection.setUseCaches(false);

            connection.setInstanceFollowRedirects(
                    false);


            connection.setRequestProperty(
                    "User-Agent",
                    USER_AGENT);

            connection.setRequestProperty(
                    "Accept",
                    "application/json, text/plain, */*");

            connection.setRequestProperty(
                    "Accept-Language",
                    "fa-IR,fa;q=0.9,en-US;q=0.8,en;q=0.7");

            connection.setRequestProperty(
                    "Cache-Control",
                    "no-cache");

            connection.setRequestProperty(
                    "Pragma",
                    "no-cache");

            connection.setRequestProperty(
                    "Referer",
                    "https://tsetmc.com/");

            connection.setRequestProperty(
                    "Origin",
                    "https://tsetmc.com");

            connection.setRequestProperty(
                    "Connection",
                    "close");


            if (connection instanceof HttpsURLConnection &&
                    url.getHost().equalsIgnoreCase(
                            TSETMC_HOST)) {

                HttpsURLConnection https =
                        (HttpsURLConnection)
                                connection;


                if (tsetmcSslSocketFactory != null) {

                    https.setSSLSocketFactory(
                            tsetmcSslSocketFactory);
                }


                if (tsetmcHostnameVerifier != null) {

                    https.setHostnameVerifier(
                            tsetmcHostnameVerifier);
                }
            }


            int code =
                    connection.getResponseCode();


            if (code == 301 ||
                    code == 302 ||
                    code == 303 ||
                    code == 307 ||
                    code == 308) {

                String location =
                        connection.getHeaderField(
                                "Location");

                throw new IOException(
                        "HTTP " +
                        code +
                        "\nRedirect:\n" +
                        (location == null
                                ? "نامشخص"
                                : location));
            }


            InputStream input;


            if (code >= 200 &&
                    code < 300) {

                input =
                        connection.getInputStream();

            } else {

                input =
                        connection.getErrorStream();


                String errorBody =
                        readStream(input);


                if (errorBody != null &&
                        errorBody.length() > 500) {

                    errorBody =
                            errorBody.substring(
                                    0,
                                    500);
                }


                throw new IOException(
                        "HTTP " +
                        code +
                        "\n" +
                        (errorBody == null
                                ? ""
                                : errorBody));
            }


            String result =
                    readStream(input);


            if (result == null ||
                    result.trim().isEmpty()) {

                throw new IOException(
                        "پاسخ خالی از سرور");
            }


            return result;


        } finally {

            if (connection != null) {

                connection.disconnect();
            }
        }
    }


    private String readStream(
            InputStream input)
            throws IOException {

        if (input == null) {
            return "";
        }


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                input,
                                StandardCharsets.UTF_8));


        StringBuilder sb =
                new StringBuilder();


        String line;


        while ((line =
                reader.readLine()) != null) {

            sb.append(line);
        }


        reader.close();


        return sb.toString();
    }


    private void parseMarketWatch(
            String response)
            throws Exception {

        marketItems.clear();


        String trimmed =
                response.trim();


        if (!trimmed.startsWith("{")) {

            throw new Exception(
                    "پاسخ TSETMC JSON نیست.\n\n" +
                    safePreview(trimmed));
        }


        JSONObject object =
                new JSONObject(trimmed);


        JSONArray array = null;


        if (object.has("marketwatch") &&
                !object.isNull("marketwatch")) {

            array =
                    object.getJSONArray(
                            "marketwatch");

        } else if (
                object.has("marketWatch") &&
                !object.isNull("marketWatch")) {

            array =
                    object.getJSONArray(
                            "marketWatch");
        }


        /*
         * بعضی پاسخ‌ها ممکن است با ساختار دیگری
         * شامل marketWatch باشند.
         */
        if (array == null &&
                object.has("marketWatchDto")) {

            Object value =
                    object.get("marketWatchDto");

            if (value instanceof JSONArray) {

                array =
                        (JSONArray)value;
            }
        }


        if (array == null) {

            throw new Exception(
                    "کلید marketwatch در پاسخ TSETMC وجود ندارد.\n\n" +
                    safePreview(trimmed));
        }


        for (int i = 0;
             i < array.length();
             i++) {

            try {

                JSONObject o =
                        array.getJSONObject(i);


                MarketItem item =
                        new MarketItem();


                item.insCode =
                        getString(
                                o,
                                "insCode",
                                "insCode");

                item.symbol =
                        getString(
                                o,
                                "lVal18AFC",
                                "lVal18",
                                "symbol",
                                "symbolName");

                item.name =
                        getString(
                                o,
                                "lVal30",
                                "name",
                                "title");


                item.last =
                        getDouble(
                                o,
                                "pDrCotVal",
                                "pl",
                                "last");


                item.close =
                        getDouble(
                                o,
                                "pClosing",
                                "pc",
                                "close");


                item.yesterday =
                        getDouble(
                                o,
                                "priceYesterday",
                                "py",
                                "yesterday");


                item.first =
                        getDouble(
                                o,
                                "priceFirst",
                                "pf");


                item.min =
                        getDouble(
                                o,
                                "priceMin",
                                "pmin");


                item.max =
                        getDouble(
                                o,
                                "priceMax",
                                "pmax");


                item.volume =
                        getDouble(
                                o,
                                "qTotTran5J",
                                "tvol",
                                "volume");


                item.value =
                        getDouble(
                                o,
                                "qTotCap",
                                "tval",
                                "value");


                item.trades =
                        getDouble(
                                o,
                                "zTotTran",
                                "tno",
                                "trades");


                item.percent =
                        getDouble(
                                o,
                                "percent",
                                "priceChangePercent");


                /*
                 * اگر درصد مستقیماً وجود نداشت،
                 * از قیمت پایانی و روز قبل محاسبه می‌کنیم.
                 */
                if (Math.abs(item.percent) < 0.000001 &&
                        item.yesterday != 0) {

                    item.percent =
                            ((item.close -
                                    item.yesterday) /
                                    item.yesterday) *
                                    100.0;
                }


                if (item.symbol == null ||
                        item.symbol.trim().isEmpty()) {

                    item.symbol =
                            "نماد نامشخص";
                }


                marketItems.add(item);

            } catch (Exception ignored) {
                /*
                 * اگر یک رکورد خراب باشد،
                 * کل بازار متوقف نمی‌شود.
                 */
            }
        }
    }


    private String safePreview(
            String value) {

        if (value == null) {
            return "";
        }


        if (value.length() > 500) {

            return value.substring(
                    0,
                    500);
        }


        return value;
    }


    private void showMarketOverview() {

        clearContent();


        content.addView(
                title(
                        "اطلاعات کلی بازار 📊"));


        if (marketItems.isEmpty()) {

            content.addView(
                    text(
                            "🟡 هنوز اطلاعات بازار دریافت نشده است.",
                            18));


            Button retry =
                    makeButton(
                            "🔄 تلاش دوباره");

            retry.setOnClickListener(
                    v -> {

                        showMainMenu();
                        loadMarketData();
                    });

            content.addView(retry);


            addBackButton();

            return;
        }


        double totalValue = 0;
        double totalVolume = 0;
        double totalTrades = 0;


        int positive = 0;
        int negative = 0;
        int unchanged = 0;


        for (MarketItem item :
                marketItems) {

            totalValue +=
                    item.value;

            totalVolume +=
                    item.volume;

            totalTrades +=
                    item.trades;


            if (item.percent > 0.0001) {

                positive++;

            } else if (
                    item.percent < -0.0001) {

                negative++;

            } else {

                unchanged++;
            }
        }


        content.addView(
                text(
                        "🟢 اتصال TSETMC برقرار است",
                        19));


        content.addView(
                text(
                        "تعداد نمادها: " +
                        marketItems.size(),
                        19));


        content.addView(
                text(
                        "🟢 نمادهای مثبت: " +
                        positive,
                        19));


        content.addView(
                text(
                        "🔴 نمادهای منفی: " +
                        negative,
                        19));


        content.addView(
                text(
                        "⚪ بدون تغییر: " +
                        unchanged,
                        19));


        content.addView(
                text(
                        "حجم کل معاملات: " +
                        formatNumber(totalVolume),
                        19));


        content.addView(
                text(
                        "ارزش کل معاملات: " +
                        formatNumber(totalValue),
                        19));


        content.addView(
                text(
                        "تعداد کل معاملات: " +
                        formatNumber(totalTrades),
                        19));


        content.addView(
                text(
                        "\nنمونه اطلاعات نمادها:",
                        20));


        int limit =
                Math.min(
                        30,
                        marketItems.size());


        for (int i = 0;
             i < limit;
             i++) {

            MarketItem item =
                    marketItems.get(i);


            String symbol =
                    item.symbol;

            if (symbol == null ||
                    symbol.trim().isEmpty()) {

                symbol =
                        "نماد نامشخص";
            }


            String name =
                    item.name;

            if (name == null ||
                    name.trim().isEmpty()) {

                name =
                        "-";
            }


            String row =
                    (i + 1) +
                    ". " +
                    symbol +
                    "\n" +
                    "نام شرکت: " +
                    name +
                    "\n" +
                    "آخرین معامله: " +
                    formatNumber(item.last) +
                    "\n" +
                    "قیمت پایانی: " +
                    formatNumber(item.close) +
                    "\n" +
                    "درصد تغییر: " +
                    String.format(
                            Locale.US,
                            "%.2f%%",
                            item.percent) +
                    "\n" +
                    "حجم: " +
                    formatNumber(item.volume) +
                    "\n" +
                    "ارزش: " +
                    formatNumber(item.value) +
                    "\n" +
                    "تعداد معاملات: " +
                    formatNumber(item.trades);


            content.addView(
                    text(
                            row,
                            16));
        }


        addBackButton();
    }


    private void showSmartMoney() {

        clearContent();


        content.addView(
                title(
                        "پول هوشمند 💵"));


        content.addView(
                text(
                        "🟡 در حال دریافت اطلاعات حقیقی و حقوقی...",
                        18));


        executor.execute(() -> {

            try {

                String response =
                        httpGet(
                                API_BASE +
                                "ClientType/GetClientTypeAll");


                JSONObject obj =
                        new JSONObject(response);


                JSONArray array =
                        obj.optJSONArray(
                                "clientTypeAllDto");


                if (array == null) {

                    throw new Exception(
                            "اطلاعات حقیقی و حقوقی پیدا نشد.");
                }


                List<MoneyItem> candidates =
                        new ArrayList<>();


                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject o =
                            array.getJSONObject(i);


                    MoneyItem m =
                            new MoneyItem();


                    m.insCode =
                            getString(
                                    o,
                                    "insCode");


                    m.buy =
                            getDouble(
                                    o,
                                    "buy_I_Volume",
                                    "buyIVolume",
                                    "nBuyVolume");


                    m.sell =
                            getDouble(
                                    o,
                                    "sell_I_Volume",
                                    "sellIVolume",
                                    "nSellVolume");


                    m.buyValue =
                            getDouble(
                                    o,
                                    "buy_I_Value",
                                    "buyIValue");


                    m.sellValue =
                            getDouble(
                                    o,
                                    "sell_I_Value",
                                    "sellIValue");


                    m.net =
                            m.buy -
                            m.sell;


                    m.netValue =
                            m.buyValue -
                            m.sellValue;


                    if (m.netValue > 0 ||
                            m.net > 0) {

                        candidates.add(m);
                    }
                }


                sortMoneyItems(
                        candidates);


                handler.post(() ->
                        showSmartMoneyResult(
                                candidates));


            } catch (Exception e) {

                final String error =
                        getReadableError(e);


                handler.post(() -> {

                    clearContent();


                    content.addView(
                            title(
                                    "پول هوشمند 💵"));


                    content.addView(
                            text(
                                    "❌ " +
                                    error,
                                    17));


                    addBackButton();
                });
            }
        });
   
