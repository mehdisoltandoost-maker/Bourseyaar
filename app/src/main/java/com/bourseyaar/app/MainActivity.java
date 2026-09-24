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
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLHandshakeException;

public class MainActivity extends Activity {

    private LinearLayout content;
    private TextView statusText;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final List<MarketItem> marketItems =
            new ArrayList<>();

    // فقط آدرس رسمی API
    private static final String API_BASE =
            "https://cdn.tsetmc.com/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildMainPage();

        // تست اتصال بعد از نمایش صفحه
        loadMarketData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        executor.shutdownNow();
    }

    // =========================================================
    // ساخت صفحه
    // =========================================================

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
                24, 24, 24, 24);

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

        t.setGravity(Gravity.CENTER);

        t.setPadding(
                10, 20, 10, 25);

        return t;
    }

    private TextView text(
            String value,
            int size) {

        TextView t =
                new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.DKGRAY);
        t.setGravity(Gravity.RIGHT);

        t.setPadding(
                12, 10, 12, 10);

        return t;
    }

    private Button makeButton(
            String name) {

        Button b =
                new Button(this);

        b.setText(name);
        b.setTextSize(18);
        b.setAllCaps(false);
        b.setTextColor(Color.DKGRAY);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        p.setMargins(0, 7, 0, 7);

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

    // =========================================================
    // منوی اصلی
    // =========================================================

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
                        "و دریافت اطلاعات واقعی بازار",
                        16);

        info.setGravity(
                Gravity.CENTER);

        content.addView(info);
    }

    // =========================================================
    // دریافت بازار
    // =========================================================

    private void loadMarketData() {

        if (statusText != null) {

            statusText.setText(
                    "🟡 در حال اتصال به TSETMC...");
        }

        executor.execute(() -> {

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

                if (marketItems.isEmpty()) {

                    throw new Exception(
                            "پاسخ دریافت شد ولی آرایه marketwatch خالی است.");
                }

                handler.post(() -> {

                    if (statusText != null) {

                        statusText.setText(
                                "🟢 اتصال برقرار شد\n" +
                                marketItems.size() +
                                " نماد از TSETMC دریافت شد.");
                    }

                    Toast.makeText(
                            MainActivity.this,
                            "اطلاعات واقعی بازار دریافت شد",
                            Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {

                final String error =
                        getReadableError(e);

                handler.post(() -> {

                    if (statusText != null) {

                        statusText.setText(
                                "🔴 خطای اتصال TSETMC\n\n" +
                                error);
                    }
                });
            }
        });
    }

    // =========================================================
    // اتصال HTTP
    // =========================================================

    private String httpGet(
            String address) throws Exception {

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
                    true);

            // بسیار مهم برای TSETMC
            connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 13) " +
                    "AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) " +
                    "Chrome/120.0 Mobile Safari/537.36");

            connection.setRequestProperty(
                    "Accept",
                    "application/json, text/plain, */*");

            connection.setRequestProperty(
                    "Accept-Language",
                    "fa-IR,fa;q=0.9,en-US;q=0.8,en;q=0.7");

            connection.setRequestProperty(
                    "Connection",
                    "close");

            int code =
                    connection.getResponseCode();

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
                        errorBody.length() > 300) {

                    errorBody =
                            errorBody.substring(
                                    0, 300);
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

        } catch (SSLHandshakeException e) {

            throw new IOException(
                    "خطای SSL / گواهی امنیتی\n" +
                    e.getMessage());

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

    // =========================================================
    // تشخیص خطای واقعی
    // =========================================================

    private String getReadableError(
            Exception e) {

        String msg =
                e.getMessage();

        if (msg == null) {
            msg = e.toString();
        }

        if (msg.contains("HTTP 403")) {

            return "HTTP 403\n\n" +
                    "دسترسی این اتصال به TSETMC رد شده است.\n" +
                    "احتمالاً IP یا شبکه مورد استفاده توسط TSETMC مسدود شده است.";
        }

        if (msg.contains("HTTP 429")) {

            return "HTTP 429\n\n" +
                    "تعداد درخواست‌ها زیاد شده است.\n" +
                    "چند لحظه بعد دوباره امتحان کنید.";
        }

        if (msg.contains("HTTP 500") ||
                msg.contains("HTTP 502") ||
                msg.contains("HTTP 503") ||
                msg.contains("HTTP 504")) {

            return msg +
                    "\n\nسرویس TSETMC موقتاً پاسخ مناسب نداده است.";
        }

        if (msg.toLowerCase(
                Locale.US).contains("timeout")) {

            return "Timeout\n\n" +
                    "پاسخ TSETMC در زمان تعیین‌شده دریافت نشد.";
        }

        if (msg.toLowerCase(
                Locale.US).contains("ssl")) {

            return "SSL\n\n" +
                    "اتصال امن به TSETMC با مشکل گواهی امنیتی مواجه شد.";
        }

        if (msg.contains("Unable to resolve") ||
                msg.contains("UnknownHost")) {

            return "DNS / اینترنت\n\n" +
                    "دامنه cdn.tsetmc.com پیدا نشد.\n" +
                    "اتصال اینترنت یا DNS را بررسی کنید.";
        }

        return msg;
    }

    // =========================================================
    // پردازش MarketWatch
    // =========================================================

    private void parseMarketWatch(
            String response)
            throws Exception {

        marketItems.clear();

        JSONObject object =
                new JSONObject(response);

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

        if (array == null) {

            throw new Exception(
                    "کلید marketwatch در پاسخ TSETMC وجود ندارد.");
        }

        for (int i = 0;
             i < array.length();
             i++) {

            JSONObject o =
                    array.getJSONObject(i);

            MarketItem item =
                    new MarketItem();

            item.insCode =
                    getString(
                            o,
                            "insCode");

            item.symbol =
                    getString(
                            o,
                            "lVal18AFC",
                            "lVal18",
                            "symbol");

            item.name =
                    getString(
                            o,
                            "lVal30",
                            "name");

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

            if (item.symbol == null ||
                    item.symbol.trim().isEmpty()) {

                item.symbol =
                        "نماد";
            }

            marketItems.add(item);
        }
    }

    // =========================================================
    // حقیقی و حقوقی
    // =========================================================

    private void loadClientType(
            FlowCallback callback) {

        executor.execute(() -> {

            try {

                String url =
                        API_BASE +
                        "ClientType/GetClientTypeAll";

                String response =
                        httpGet(url);

                if (response == null ||
                        response.trim().isEmpty()) {

                    throw new Exception(
                            "پاسخ اطلاعات حقیقی/حقوقی خالی است.");
                }

                JSONObject obj =
                        new JSONObject(response);

                if (!obj.has(
                        "clientTypeAllDto")) {

                    throw new Exception(
                            "کلید clientTypeAllDto در پاسخ وجود ندارد.");
                }

                JSONArray array =
                        obj.getJSONArray(
                                "clientTypeAllDto");

                handler.post(() ->
                        callback.onSuccess(
                                array));

            } catch (Exception e) {

                final String error =
                        getReadableError(e);

                handler.post(() ->
                        callback.onError(
                                error));
            }
        });
    }

    // =========================================================
    // اطلاعات کلی بازار
    // =========================================================

    private void showMarketOverview() {

        clearContent();

        content.addView(
                title("اطلاعات کلی بازار 📊"));

        if (marketItems.isEmpty()) {

            TextView loading =
                    text(
                            "🟡 در حال دریافت اطلاعات بازار...",
                            18);

            content.addView(loading);

            addBackButton();

            loadMarketData();

            return;
        }

        double totalValue = 0;
        double totalVolume = 0;

        int positive = 0;
        int negative = 0;

        for (MarketItem item :
                marketItems) {

            totalValue += item.value;
            totalVolume += item.volume;

            if (item.yesterday > 0 &&
                    item.close >
                            item.yesterday) {

                positive++;
            }

            if (item.yesterday > 0 &&
                    item.close <
                            item.yesterday) {

                negative++;
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
                        "نمادهای مثبت: " +
                        positive,
                        19));

        content.addView(
                text(
                        "نمادهای منفی: " +
                        negative,
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
                        "\nمنبع: TSETMC",
                        15));

        addBackButton();
    }

    // =========================================================
    // پول هوشمند
    // =========================================================

    private void showSmartMoney() {

        clearContent();

        content.addView(
                title("پول هوشمند 💵"));

        content.addView(
                text(
                        "در حال دریافت اطلاعات حقیقی و حقوقی از TSETMC...",
                        18));

        loadClientType(
                new FlowCallback() {

                    @Override
                    public void onSuccess(
                            JSONArray array) {

                        showSmartMoneyResult(
                                array);
                    }

                    @Override
                    public void onError(
                            String message) {

                        content.addView(
                                text(
                                        "\n❌ " +
                                        message,
                                        17));

                        addBackButton();
                    }
                });
    }

    private void showSmartMoneyResult(
            JSONArray array) {

        clearContent();

        content.addView(
                title("پول هوشمند 💵"));

        List<MoneyItem> candidates =
                new ArrayList<>();

        try {

            for (int i = 0;
                 i < array.length();
                 i++) {

                JSONObject o =
                        array.getJSONObject(i);

                String insCode =
                        getString(
                                o,
                                "insCode");

                double buy =
                        getDouble(
                                o,
                                "buy_I_Volume",
                                "buyIVolume",
                                "nBuyVolume");

                double sell =
                        getDouble(
                                o,
                                "sell_I_Volume",
                                "sellIVolume",
                                "nSellVolume");

                double net =
                        buy - sell;

                if (net > 0) {

                    MoneyItem m =
                            new MoneyItem();

                    m.insCode =
                            insCode;

                    m.buy = buy;
                    m.sell = sell;
                    m.net = net;

                    candidates.add(m);
                }
            }

            // مرتب‌سازی ورود خالص
            for (int i = 0;
                 i < candidates.size();
                 i++) {

                for (int j = i + 1;
                     j < candidates.size();
                     j++) {

                    if (candidates.get(j).net >
                            candidates.get(i).net) {

                        MoneyItem temp =
                                candidates.get(i);

                        candidates.set(
                                i,
                                candidates.get(j));

                        candidates.set(
                                j,
                                temp);
                    }
                }
            }

            int limit =
                    Math.min(
                            15,
                            candidates.size());

            content.addView(
                    text(
                            "تعداد رکوردهای حقیقی/حقوقی: " +
                            array.length(),
                            17));

            if (limit == 0) {

                content.addView(
                        text(
                                "ورود خالص حقیقی مثبت پیدا نشد.",
                                18));

            } else {

                for (int i = 0;
                     i < limit;
                     i++) {

                    MoneyItem m =
                            candidates.get(i);

                    String symbol =
                            findSymbol(
                                    m.insCode);

                    content.addView(
                            text(
                                    (i + 1) +
                                    ". " +
                                    symbol +
                                    "\nورود خالص حقیقی: " +
                                    formatNumber(m.net) +
                                    " سهم" +
                                    "\nخرید حقیقی: " +
                                    formatNumber(m.buy) +
                                    " سهم" +
                                    "\nفروش حقیقی: " +
                                    formatNumber(m.sell) +
                                    " سهم",
                                    17));
                }
            }

        } catch (Exception e) {

            content.addView(
                    text(
                            "❌ خطا در پردازش: " +
                            e.getMessage(),
                            17));
        }

        content.addView(
                text(
                        "\nتوجه: این مقدار فعلاً بر اساس حجم خرید و فروش حقیقی است؛ برای محاسبه ارزش ریالی دقیق باید قیمت هر نماد نیز لحاظ شود.",
                        15));

        addBackButton();
    }

    // =========================================================
    // ورود و خروج پول
    // =========================================================

    private void showMoneyFlow() {

        clearContent();

        content.addView(
                title("ورود و خروج پول 🔄"));

        content.addView(
                text(
                        "🟡 در حال دریافت اطلاعات حقیقی و حقوقی...",
                        18));

        loadClientType(
                new FlowCallback() {

                    @Override
                    public void onSuccess(
                            JSONArray array) {

                        calculateMoneyFlow(
                                array);
                    }

                    @Override
                    public void onError(
                            String message) {

                        content.addView(
                                text(
                                        "\n❌ " +
                                        message,
                                        17));

                        addBackButton();
                    }
                });
    }

    private void calculateMoneyFlow(
            JSONArray array) {

        double totalBuy = 0;
        double totalSell = 0;

        int count = 0;

        for (int i = 0;
             i < array.length();
             i++) {

            try {

                JSONObject o =
                        array.getJSONObject(i);

                double buy =
                        getDouble(
                                o,
                                "buy_I_Volume",
                                "buyIVolume",
                                "nBuyVolume");

                double sell =
                        getDouble(
                                o,
                                "sell_I_Volume",
                                "sellIVolume",
                                "nSellVolume");

                totalBuy += buy;
                totalSell += sell;

                count++;

            } catch (Exception ignored) {
            }
        }

        double net =
                totalBuy - totalSell;

        clearContent();

        content.addView(
                title("ورود و خروج پول 🔄"));

        content.addView(
                text(
                        "تعداد نمادهای بررسی‌شده: " +
                        count,
                        18));

        content.addView(
                text(
                        "خرید حقیقی: " +
                        formatNumber(totalBuy) +
                        " سهم",
                        18));

        content.addView(
                text(
                        "فروش حقیقی: " +
                        formatNumber(totalSell) +
                        " سهم",
                        18));

        if (net > 0) {

            content.addView(
                    text(
                            "🟢 ورود خالص حقیقی: " +
                            formatNumber(net) +
                            " سهم",
                            20));

        } else if (net < 0) {

            content.addView(
                    text(
                            "🔴 خروج خالص حقیقی: " +
                            formatNumber(
                                    Math.abs(net)) +
                            " سهم",
                            20));

        } else {

            content.addView(
                    text(
                            "⚪ خرید و فروش حقیقی برابر است.",
                            20));
        }

        content.addView(
                text(
                        "\nاین مرحله حجم خالص را نشان می‌دهد؛ محاسبه پول ریالی در مرحله بعد با قیمت هر نماد انجام می‌شود.",
                        15));

        addBackButton();
    }

    // =========================================================
    // جستجوی نماد
    // =========================================================

    private void showSymbols() {

        clearContent();

        content.addView(
                title("بررسی نمادها 🔎"));

        EditText input =
                new EditText(this);

        input.setHint(
                "نام نماد، مثلاً فولاد");

        input.setTextSize(18);
        input.setSingleLine(true);

        content.addView(input);

        Button search =
                makeButton(
                        "🔍 جستجوی نماد");

        content.addView(search);

        TextView result =
                text("", 18);

        content.addView(result);

        search.setOnClickListener(
                v -> {

                    String symbol =
                            input.getText()
                                    .toString()
                                    .trim();

                    if (symbol.isEmpty()) {

                        result.setText(
                                "نام نماد را وارد کنید.");

                        return;
                    }

                    hideKeyboard(input);

                    searchSymbol(
                            symbol,
                            result);
                });

        addBackButton();
    }

    private void searchSymbol(
            String symbol,
            TextView result) {

        executor.execute(() -> {

            try {

                String encoded =
                        URLEncoder.encode(
                                symbol,
                                "UTF-8");

                String response =
                        httpGet(
                                API_BASE +
                                "Instrument/GetInstrumentSearch/" +
                                encoded);

                JSONObject obj =
                        new JSONObject(response);

                JSONArray arr =
                        obj.has("instrumentSearch")
                                ? obj.getJSONArray(
                                "instrumentSearch")
                                : null;

                if (arr == null ||
                        arr.length() == 0) {

                    handler.post(() ->
                            result.setText(
                                    "نمادی پیدا نشد."));

                    return;
                }

                StringBuilder sb =
                        new StringBuilder();

                int limit =
                        Math.min(
                                10,
                                arr.length());

                for (int i = 0;
                     i < limit;
                     i++) {

                    JSONObject o =
                            arr.getJSONObject(i);

                    String s =
                            getString(
                                    o,
                                    "lVal18AFC",
                                    "lVal18");

                    String name =
                            getString(
                                    o,
                                    "lVal30",
                                    "name");

                    sb.append(
                            "نماد: ")
                            .append(s)
                            .append("\n");

                    sb.append(
                            "نام: ")
                            .append(name)
                            .append("\n\n");
                }

                handler.post(() ->
                        result.setText(
                                sb.toString()));

            } catch (Exception e) {

                final String error =
                        getReadableError(e);

                handler.post(() ->
                        result.setText(
                                "❌ " +
                                error));
            }
        });
    }

    // =========================================================
    // بنیادی
    // =========================================================

    private void showFundamental() {

        clearContent();

        content.addView(
                title("تحلیل بنیادی 📚"));

        content.addView(
                text(
                        "این بخش آماده اتصال به اطلاعات بنیادی است.\n\n" +
                        "EPS\n" +
                        "P/E\n" +
                        "فروش\n" +
                        "سود\n" +
                        "گزارش‌های کدال\n\n" +
                        "مرحله بعد اتصال اطلاعات کدال خواهد بود.",
                        18));

        addBackButton();
    }

    // =========================================================
    // تکنیکال
    // =========================================================

    private void showTechnical() {

        clearContent();

        content.addView(
                title("تحلیل تکنیکال 📈"));

        content.addView(
                text(
                        "برای تحلیل تکنیکال دقیق باید سابقه قیمت هر نماد دریافت شود.\n\n" +
                        "RSI\n" +
                        "MACD\n" +
                        "میانگین متحرک\n" +
                        "حجم\n" +
                        "روند قیمت\n\n" +
                        "اتصال سابقه قیمت در مرحله بعد انجام می‌شود.",
                        18));

        addBackButton();
    }

    // =========================================================
    // پیشنهادها
    // =========================================================

    private void showSuggestions() {

        clearContent();

        content.addView(
                title("پیشنهادهای معاملاتی 💡"));

        if (marketItems.isEmpty()) {

            content.addView(
                    text(
                            "ابتدا اطلاعات بازار را دریافت کنید.",
                            18));

            Button refresh =
                    makeButton(
                            "🔄 دریافت اطلاعات بازار");

            refresh.setOnClickListener(
                    v -> {

                        showMainMenu();
                        loadMarketData();
                    });

            content.addView(refresh);

            addBackButton();

            return;
        }

        List<MarketItem> list =
                new ArrayList<>();

        for (MarketItem item :
                marketItems) {

            if (item.yesterday > 0 &&
                    item.volume > 0 &&
                    item.value > 0 &&
                    item.close >
                            item.yesterday) {

                list.add(item);
            }
        }

        for (int i = 0;
             i < list.size();
             i++) {

            for (int j = i + 1;
                 j < list.size();
                 j++) {

                double pi =
                        percent(
                                list.get(i).close,
                                list.get(i).yesterday);

                double pj =
                        percent(
                                list.get(j).close,
                                list.get(j).yesterday);

                if (pj > pi) {

                    MarketItem temp =
                            list.get(i);

                    list.set(
                            i,
                            list.get(j));

                    list.set(
                            j,
                            temp);
                }
            }
        }

        int limit =
                Math.min(
                        15,
                        list.size());

        if (limit == 0) {

            content.addView(
                    text(
                            "نمادی با این فیلتر پیدا نشد.",
                            18));

        } else {

            content.addView(
                    text(
                            "نمادهای دارای رشد قیمت و فعالیت معاملاتی:",
                            18));

            for (int i = 0;
                 i < limit;
                 i++) {

                MarketItem item =
                        list.get(i);

                double p =
                        percent(
                                item.close,
                                item.yesterday);

                content.addView(
                        text(
                                (i + 1) +
                                ". " +
                                item.symbol +
                                "\nقیمت: " +
                                formatNumber(
                                        item.close) +
                                "\nدرصد تغییر: " +
                                String.format(
                                        Locale.US,
                                        "%.2f%%",
                                        p) +
                                "\nارزش معاملات: " +
                                formatNumber(
                                        item.value),
                                17));
            }
        }

        content.addView(
                text(
                        "\nاین فهرست صرفاً فیلتر اولیه داده بازار است و توصیه قطعی خرید یا فروش نیست.",
                        15));

        addBackButton();
    }

    // =========================================================
    // ابزارها
    // =========================================================

    private String findSymbol(
            String insCode) {

        if (insCode == null) {
            return "نماد";
        }

        for (MarketItem item :
                marketItems) {

            if (insCode.equals(
                    item.insCode)) {

                return item.symbol;
            }
        }

        return "نماد " + insCode;
    }

    private String getString(
            JSONObject o,
            String... keys) {

        for (String key : keys) {

            if (o.has(key) &&
                    !o.isNull(key)) {

                return o.optString(
                        key,
                        "");
            }
        }

        return "";
    }

    private double getDouble(
            JSONObject o,
            String... keys) {

        for (String key : keys) {

            try {

                if (!o.has(key) ||
                        o.isNull(key)) {

                    continue;
                }

                Object value =
                        o.get(key);

                if (value instanceof Number) {

                    return ((Number) value)
                            .doubleValue();
                }

                String s =
                        String.valueOf(value)
                                .replace(",", "")
                                .trim();

                if (!s.isEmpty()) {

                    return Double.parseDouble(s);
                }

            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    private String formatNumber(
            double value) {

        if (Math.abs(value) >=
                1000000000) {

            return String.format(
                    Locale.US,
                    "%.2f میلیارد",
                    value / 1000000000.0);
        }

        if (Math.abs(value) >=
                1000000) {

            return String.format(
                    Locale.US,
                    "%.2f میلیون",
                    value / 1000000.0);
        }

        return String.format(
                Locale.US,
                "%.0f",
                value);
    }

    private double percent(
            double current,
            double previous) {

        if (previous == 0) {
            return 0;
        }

        return ((current - previous) /
                previous) * 100.0;
    }

    private void hideKeyboard(
            View view) {

        InputMethodManager imm =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE);

        if (imm != null) {

            imm.hideSoftInputFromWindow(
                    view.getWindowToken(),
                    0);
        }
    }

    // =========================================================
    // کلاس‌های داده
    // =========================================================

    private static class MarketItem {

        String insCode = "";
        String symbol = "";
        String name = "";

        double last = 0;
        double close = 0;
        double yesterday = 0;
        double volume = 0;
        double value = 0;
        double trades = 0;
    }

    private static class MoneyItem {

        String insCode = "";

        double buy = 0;
        double sell = 0;
        double net = 0;
    }

    private interface FlowCallback {

        void onSuccess(JSONArray array);

        void onError(String message);
    }
}
