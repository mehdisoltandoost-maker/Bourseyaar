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

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {

    private LinearLayout root;
    private LinearLayout content;
    private TextView statusText;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<MarketItem> marketItems = new ArrayList<>();

    private static final String[] API_BASES = {
            "https://cdn.tsetmc.com/api/",
            "https://cdn10.tsetmc.com/api/"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildMainPage();

        // دریافت اولیه داده بازار
        loadMarketData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    // =========================================================
    // ظاهر اصلی
    // =========================================================

    private void buildMainPage() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(24, 24, 24, 24);

        ScrollView scroll = new ScrollView(this);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);

        scroll.addView(content);

        setContentView(scroll);

        showMainMenu();
    }

    private void clearContent() {
        content.removeAllViews();
    }

    private TextView title(String text) {

        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(28);
        t.setTextColor(Color.rgb(20, 65, 100));
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(10, 20, 10, 25);

        return t;
    }

    private TextView text(String value, int size) {

        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.DKGRAY);
        t.setGravity(Gravity.RIGHT);
        t.setPadding(12, 10, 12, 10);

        return t;
    }

    private Button makeButton(String name) {

        Button b = new Button(this);
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

        Button back = makeButton("⬅️ بازگشت به صفحه اصلی");

        back.setOnClickListener(v -> showMainMenu());

        content.addView(back);
    }

    // =========================================================
    // منوی اصلی
    // =========================================================

    private void showMainMenu() {

        clearContent();

        content.addView(title("بورس‌یار 📈"));

        statusText = text(
                "وضعیت اتصال: در حال بررسی...",
                16
        );

        statusText.setGravity(Gravity.CENTER);
        content.addView(statusText);

        Button market = makeButton("📊 اطلاعات کلی بازار");
        market.setOnClickListener(v -> showMarketOverview());
        content.addView(market);

        Button smart = makeButton("💵 پول هوشمند");
        smart.setOnClickListener(v -> showSmartMoney());
        content.addView(smart);

        Button money = makeButton("🔄 ورود و خروج پول");
        money.setOnClickListener(v -> showMoneyFlow());
        content.addView(money);

        Button fundamental = makeButton("📚 تحلیل بنیادی");
        fundamental.setOnClickListener(v -> showFundamental());
        content.addView(fundamental);

        Button technical = makeButton("📈 تحلیل تکنیکال");
        technical.setOnClickListener(v -> showTechnical());
        content.addView(technical);

        Button symbols = makeButton("🔎 بررسی نمادها");
        symbols.setOnClickListener(v -> showSymbols());
        content.addView(symbols);

        Button suggestions = makeButton("💡 پیشنهادهای معاملاتی");
        suggestions.setOnClickListener(v -> showSuggestions());
        content.addView(suggestions);

        Button refresh = makeButton("🔄 دریافت دوباره اطلاعات بازار");
        refresh.setOnClickListener(v -> loadMarketData());
        content.addView(refresh);

        TextView info = text(
                "\nبورس‌یار\n" +
                "داده‌ها از سرویس بازار دریافت می‌شوند.\n" +
                "برای محاسبات پول حقیقی، حجم و ارزش معاملات از داده‌های بازار استفاده می‌شود.",
                16
        );

        info.setGravity(Gravity.CENTER);
        content.addView(info);
    }

    // =========================================================
    // اتصال به TSETMC
    // =========================================================

    private void loadMarketData() {

        if (statusText != null) {
            statusText.setText("وضعیت اتصال: در حال دریافت اطلاعات بازار...");
        }

        executor.execute(() -> {

            boolean success = false;
            String error = "";

            for (String base : API_BASES) {

                try {

                    String url =
                            base +
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

                    String response = httpGet(url);

                    if (response != null &&
                            response.trim().length() > 10) {

                        parseMarketWatch(response);

                        if (!marketItems.isEmpty()) {
                            success = true;
                            break;
                        }
                    }

                } catch (Exception e) {
                    error = e.getMessage();
                }
            }

            final boolean result = success;
            final String finalError = error;

            handler.post(() -> {

                if (result) {

                    if (statusText != null) {
                        statusText.setText(
                                "🟢 اتصال برقرار است — " +
                                marketItems.size() +
                                " نماد دریافت شد."
                        );
                    }

                    Toast.makeText(
                            MainActivity.this,
                            "اطلاعات بازار دریافت شد",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    if (statusText != null) {
                        statusText.setText(
                                "🔴 اطلاعات بازار دریافت نشد.\n" +
                                "اتصال اینترنت یا دسترسی به سرویس بازار را بررسی کنید."
                        );
                    }
                }
            });
        });
    }

    private String httpGet(String address) throws Exception {

        URL url = new URL(address);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Android) AppleWebKit/537.36"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json,text/plain,*/*"
        );

        connection.setRequestProperty(
                "Accept-Language",
                "fa-IR,fa;q=0.9,en;q=0.8"
        );

        connection.setUseCaches(false);

        int code = connection.getResponseCode();

        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code);
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder result = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();
        connection.disconnect();

        return result.toString();
    }

    // =========================================================
    // تبدیل دیده بان بازار
    // =========================================================

    private void parseMarketWatch(String response) throws Exception {

        marketItems.clear();

        JSONObject object = new JSONObject(response);

        JSONArray array = null;

        if (object.has("marketwatch")) {
            array = object.getJSONArray("marketwatch");
        } else if (object.has("marketWatch")) {
            array = object.getJSONArray("marketWatch");
        }

        if (array == null) {
            return;
        }

        for (int i = 0; i < array.length(); i++) {

            JSONObject o = array.getJSONObject(i);

            MarketItem item = new MarketItem();

            item.insCode = getString(o,
                    "insCode",
                    "insCode");

            item.symbol = getString(o,
                    "lVal18AFC",
                    "lVal18",
                    "symbol",
                    "n");

            item.name = getString(o,
                    "lVal30",
                    "name");

            item.last =
                    getDouble(o,
                            "pDrCotVal",
                            "pl",
                            "last");

            item.close =
                    getDouble(o,
                            "pClosing",
                            "pc",
                            "close");

            item.yesterday =
                    getDouble(o,
                            "priceYesterday",
                            "py",
                            "yesterday");

            item.volume =
                    getDouble(o,
                            "qTotTran5J",
                            "tvol",
                            "volume");

            item.value =
                    getDouble(o,
                            "qTotCap",
                            "tval",
                            "value");

            item.trades =
                    getDouble(o,
                            "zTotTran",
                            "tno",
                            "trades");

            if (item.symbol == null ||
                    item.symbol.trim().isEmpty()) {
                item.symbol = "نماد";
            }

            marketItems.add(item);
        }
    }

    // =========================================================
    // پول حقیقی / حقوقی
    // =========================================================

    private void loadClientType(FlowCallback callback) {

        executor.execute(() -> {

            String response = null;

            Exception lastException = null;

            for (String base : API_BASES) {

                try {

                    String url =
                            base +
                            "ClientType/GetClientTypeAll";

                    response = httpGet(url);

                    if (response != null &&
                            response.length() > 10) {
                        break;
                    }

                } catch (Exception e) {
                    lastException = e;
                }
            }

            final String finalResponse = response;

            handler.post(() -> {

                if (finalResponse == null ||
                        finalResponse.length() < 10) {

                    callback.onError(
                            "اطلاعات حقیقی و حقوقی دریافت نشد."
                    );

                    return;
                }

                try {

                    JSONObject obj =
                            new JSONObject(finalResponse);

                    JSONArray array = null;

                    if (obj.has("clientTypeAllDto")) {
                        array =
                                obj.getJSONArray(
                                        "clientTypeAllDto"
                                );
                    }

                    if (array == null) {
                        callback.onError(
                                "ساختار اطلاعات حقیقی/حقوقی قابل خواندن نیست."
                        );
                        return;
                    }

                    callback.onSuccess(array);

                } catch (Exception e) {

                    callback.onError(
                            "خطا در پردازش اطلاعات حقیقی/حقوقی"
                    );
                }
            });
        });
    }

    // =========================================================
    // اطلاعات کلی بازار
    // =========================================================

    private void showMarketOverview() {

        clearContent();

        content.addView(title("اطلاعات کلی بازار 📊"));

        if (marketItems.isEmpty()) {

            content.addView(text(
                    "در حال دریافت اطلاعات بازار...",
                    18
            ));

            addBackButton();

            loadMarketData();
            return;
        }

        double totalValue = 0;
        double totalVolume = 0;

        int positive = 0;
        int negative = 0;

        for (MarketItem item : marketItems) {

            totalValue += item.value;
            totalVolume += item.volume;

            if (item.yesterday > 0 &&
                    item.close > item.yesterday) {
                positive++;
            }

            if (item.yesterday > 0 &&
                    item.close < item.yesterday) {
                negative++;
            }
        }

        content.addView(text(
                "تعداد نمادهای دریافت‌شده: " +
                        marketItems.size(),
                19
        ));

        content.addView(text(
                "نمادهای مثبت: " + positive,
                19
        ));

        content.addView(text(
                "نمادهای منفی: " + negative,
                19
        ));

        content.addView(text(
                "حجم کل معاملات: " +
                        formatNumber(totalVolume),
                19
        ));

        content.addView(text(
                "ارزش کل معاملات: " +
                        formatNumber(totalValue),
                19
        ));

        content.addView(text(
                "\nمنبع: داده دیده‌بان بازار",
                15
        ));

        addBackButton();
    }

    // =========================================================
    // پول هوشمند
    // =========================================================

    private void showSmartMoney() {

        clearContent();

        content.addView(title("پول هوشمند 💵"));

        content.addView(text(
                "در این بخش ورود پول حقیقی به نمادها بررسی می‌شود.\n" +
                "در حال دریافت اطلاعات واقعی بازار...",
                18
        ));

        loadClientType(new FlowCallback() {

            @Override
            public void onSuccess(JSONArray array) {

                showSmartMoneyResult(array);
            }

            @Override
            public void onError(String message) {

                content.addView(text(
                        "\n❌ " + message,
                        18
                ));

                content.addView(text(
                        "\nاگر VPN یا اینترنت بین‌المللی استفاده می‌کنید، " +
                        "اتصال به سرویس TSETMC ممکن است توسط شبکه مسدود شده باشد.",
                        16
                ));

                addBackButton();
            }
        });
    }

    private void showSmartMoneyResult(JSONArray array) {

        clearContent();

        content.addView(title("پول هوشمند 💵"));

        List<MoneyItem> candidates =
                new ArrayList<>();

        try {

            for (int i = 0; i < array.length(); i++) {

                JSONObject o =
                        array.getJSONObject(i);

                String insCode =
                        getString(o,
                                "insCode",
                                "insCode");

                double buyIndividual =
                        getDouble(o,
                                "buy_I_Volume",
                                "buyIVolume",
                                "nBuyVolume");

                double sellIndividual =
                        getDouble(o,
                                "sell_I_Volume",
                                "sellIVolume",
                                "nSellVolume");

                double net =
                        buyIndividual -
                                sellIndividual;

                if (net > 0) {

                    MoneyItem m =
                            new MoneyItem();

                    m.insCode = insCode;
                    m.buy = buyIndividual;
                    m.sell = sellIndividual;
                    m.net = net;

                    candidates.add(m);
                }
            }

            // مرتب‌سازی نزولی بر اساس ورود پول
            for (int i = 0; i < candidates.size(); i++) {

                for (int j = i + 1;
                     j < candidates.size();
                     j++) {

                    if (candidates.get(j).net >
                            candidates.get(i).net) {

                        MoneyItem temp =
                                candidates.get(i);

                        candidates.set(
                                i,
                                candidates.get(j)
                        );

                        candidates.set(
                                j,
                                temp
                        );
                    }
                }
            }

            int limit =
                    Math.min(15, candidates.size());

            if (limit == 0) {

                content.addView(text(
                        "فعلاً داده قابل استفاده‌ای برای ورود پول حقیقی پیدا نشد.",
                        18
                ));

            } else {

                content.addView(text(
                        "نمادهای دارای ورود پول حقیقی:",
                        19
                ));

                for (int i = 0; i < limit; i++) {

                    MoneyItem m =
                            candidates.get(i);

                    String symbol =
                            findSymbol(m.insCode);

                    content.addView(text(
                            (i + 1) +
                                    ". " +
                                    symbol +
                                    "\nورود خالص حقیقی: " +
                                    formatNumber(m.net) +
                                    "\nخرید حقیقی: " +
                                    formatNumber(m.buy) +
                                    "\nفروش حقیقی: " +
                                    formatNumber(m.sell),
                            17
                    ));
                }
            }

        } catch (Exception e) {

            content.addView(text(
                    "خطا در پردازش پول هوشمند.",
                    18
            ));
        }

        addBackButton();
    }

    // =========================================================
    // ورود و خروج پول
    // =========================================================

    private void showMoneyFlow() {

        clearContent();

        content.addView(title("ورود و خروج پول 🔄"));

        content.addView(text(
                "خرید حقیقی\n" +
                "فروش حقیقی\n" +
                "ورود پول\n" +
                "خروج پول\n" +
                "ارزش معاملات",
                19
        ));

        loadClientType(new FlowCallback() {

            @Override
            public void onSuccess(JSONArray array) {

                calculateMoneyFlow(array);
            }

            @Override
            public void onError(String message) {

                content.addView(text(
                        "\n❌ " + message,
                        18
                ));

                addBackButton();
            }
        });
    }

    private void calculateMoneyFlow(JSONArray array) {

        double totalBuy = 0;
        double totalSell = 0;

        int count = 0;

        try {

            for (int i = 0;
                 i < array.length();
                 i++) {

                JSONObject o =
                        array.getJSONObject(i);

                double buy =
                        getDouble(o,
                                "buy_I_Volume",
                                "buyIVolume",
                                "nBuyVolume");

                double sell =
                        getDouble(o,
                                "sell_I_Volume",
                                "sellIVolume",
                                "nSellVolume");

                totalBuy += buy;
                totalSell += sell;

                count++;
            }

        } catch (Exception ignored) {
        }

        double net =
                totalBuy - totalSell;

        content.addView(text(
                "\nتعداد نمادهای بررسی‌شده: " +
                        count,
                18
        ));

        content.addView(text(
                "خرید حقیقی: " +
                        formatNumber(totalBuy),
                18
        ));

        content.addView(text(
                "فروش حقیقی: " +
                        formatNumber(totalSell),
                18
        ));

        if (net > 0) {

            content.addView(text(
                    "🟢 ورود خالص پول حقیقی: " +
                            formatNumber(net),
                    20
            ));

        } else if (net < 0) {

            content.addView(text(
                    "🔴 خروج خالص پول حقیقی: " +
                            formatNumber(Math.abs(net)),
                    20
            ));

        } else {

            content.addView(text(
                    "⚪ ورود و خروج پول تقریباً برابر است.",
                    20
            ));
        }

        addBackButton();
    }

    // =========================================================
    // بررسی نماد
    // =========================================================

    private void showSymbols() {

        clearContent();

        content.addView(title("بررسی نمادها 🔎"));

        EditText input =
                new EditText(this);

        input.setHint("نام نماد، مثلاً فولاد");
        input.setTextSize(18);
        input.setSingleLine(true);

        content.addView(input);

        Button search =
                makeButton("🔍 جستجوی نماد");

        content.addView(search);

        TextView result =
                text("", 18);

        content.addView(result);

        search.setOnClickListener(v -> {

            String symbol =
                    input.getText()
                            .toString()
                            .trim();

            if (symbol.isEmpty()) {

                result.setText(
                        "نام نماد را وارد کنید."
                );

                return;
            }

            hideKeyboard(input);

            searchSymbol(symbol, result);
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
                                "UTF-8"
                        );

                String response = null;

                for (String base : API_BASES) {

                    try {

                        response =
                                httpGet(
                                        base +
                                        "Instrument/GetInstrumentSearch/" +
                                        encoded
                                );

                        if (response != null &&
                                response.length() > 10) {
                            break;
                        }

                    } catch (Exception ignored) {
                    }
                }

                if (response == null) {

                    handler.post(() ->
                            result.setText(
                                    "اطلاعات نماد دریافت نشد."
                            )
                    );

                    return;
                }

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
                                    "نمادی با این نام پیدا نشد."
                            )
                    );

                    return;
                }

                StringBuilder sb =
                        new StringBuilder();

                int limit =
                        Math.min(10, arr.length());

                for (int i = 0;
                     i < limit;
                     i++) {

                    JSONObject o =
                            arr.getJSONObject(i);

                    String s =
                            getString(
                                    o,
                                    "lVal18AFC",
                                    "lVal18"
                            );

                    String name =
                            getString(
                                    o,
                                    "lVal30",
                                    "name"
                            );

                    sb.append("نماد: ")
                            .append(s)
                            .append("\n");

                    sb.append("نام: ")
                            .append(name)
                            .append("\n\n");
                }

                handler.post(() ->
                        result.setText(
                                sb.toString()
                        )
                );

            } catch (Exception e) {

                handler.post(() ->
                        result.setText(
                                "خطا در جستجوی نماد."
                        )
                );
            }
        });
    }

    // =========================================================
    // تحلیل بنیادی
    // =========================================================

    private void showFundamental() {

        clearContent();

        content.addView(title("تحلیل بنیادی 📚"));

        content.addView(text(
                "در نسخه فعلی، اطلاعات پایه بازار دریافت می‌شود.\n\n" +
                "برای تحلیل بنیادی کامل هر نماد باید اطلاعات شرکت، " +
                "EPS، P/E، فروش، سود و گزارش‌های کدال نیز دریافت شود.\n\n" +
                "این بخش در مرحله بعد به داده‌های واقعی متصل می‌شود.",
                18
        ));

        addBackButton();
    }

    // =========================================================
    // تحلیل تکنیکال
    // =========================================================

    private void showTechnical() {

        clearContent();

        content.addView(title("تحلیل تکنیکال 📈"));

        content.addView(text(
                "تحلیل تکنیکال با استفاده از قیمت و حجم انجام خواهد شد.\n\n" +
                "شاخص‌های قابل استفاده:\n" +
                "• میانگین متحرک\n" +
                "• RSI\n" +
                "• MACD\n" +
                "• حجم معاملات\n" +
                "• روند قیمت\n\n" +
                "برای محاسبه دقیق این موارد به سابقه قیمت نماد نیاز است.",
                18
        ));

        addBackButton();
    }

    // =========================================================
    // پیشنهادهای معاملاتی
    // =========================================================

    private void showSuggestions() {

        clearContent();

        content.addView(title("پیشنهادهای معاملاتی 💡"));

        if (marketItems.isEmpty()) {

            content.addView(text(
                    "ابتدا اطلاعات بازار را دریافت کنید.",
                    18
            ));

            Button refresh =
                    makeButton(
                            "🔄 دریافت اطلاعات بازار"
                    );

            refresh.setOnClickListener(
                    v -> {
                        showMainMenu();
                        loadMarketData();
                    }
            );

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
                    item.close > item.yesterday) {

                list.add(item);
            }
        }

        // بیشترین درصد رشد
        for (int i = 0;
             i < list.size();
             i++) {

            for (int j = i + 1;
                 j < list.size();
                 j++) {

                double pi =
                        percent(
                                list.get(i)
                                        .close,
                                list.get(i)
                                        .yesterday
                        );

                double pj =
                        percent(
                                list.get(j)
                                        .close,
                                list.get(j)
                                        .yesterday
                        );

                if (pj > pi) {

                    MarketItem temp =
                            list.get(i);

                    list.set(
                            i,
                            list.get(j)
                    );

                    list.set(
                            j,
                            temp
                    );
                }
            }
        }

        int limit =
                Math.min(15, list.size());

        if (limit == 0) {

            content.addView(text(
                    "در حال حاضر نماد مناسب بر اساس فیلتر اولیه پیدا نشد.",
                    18
            ));

        } else {

            content.addView(text(
                    "نمادهای دارای رشد قیمت و فعالیت معاملاتی:",
                    18
            ));

            for (int i = 0;
                 i < limit;
                 i++) {

                MarketItem item =
                        list.get(i);

                double p =
                        percent(
                                item.close,
                                item.yesterday
                        );

                content.addView(text(
                        (i + 1) +
                                ". " +
                                item.symbol +
                                "\nقیمت: " +
                                formatNumber(item.close) +
                                "\nدرصد تغییر: " +
                                String.format(
                                        Locale.US,
                                        "%.2f%%",
                                        p
                                ) +
                                "\nارزش معاملات: " +
                                formatNumber(item.value),
                        17
                ));
            }
        }

        content.addView(text(
                "\nتوجه: این فهرست صرفاً فیلتر اولیه داده بازار است و توصیه قطعی خرید یا فروش نیست.",
                15
        ));

        addBackButton();
    }

    // =========================================================
    // ابزارها
    // =========================================================

    private String findSymbol(String insCode) {

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
                        ""
                );
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

    private String formatNumber(double value) {

        if (Math.abs(value) >= 1000000000) {

            return String.format(
                    Locale.US,
                    "%.2f میلیارد",
                    value / 1000000000.0
            );
        }

        if (Math.abs(value) >= 1000000) {

            return String.format(
                    Locale.US,
                    "%.2f میلیون",
                    value / 1000000.0
            );
        }

        return String.format(
                Locale.US,
                "%.0f",
                value
        );
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

    private void hideKeyboard(View view) {

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
