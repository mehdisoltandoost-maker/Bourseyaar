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
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;

import javax.net.ssl.*;

public class MainActivity extends Activity {

    private LinearLayout content;
    private TextView statusText;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final List<MarketItem> marketItems = new ArrayList<>();

    private static final String API =
            "https://cdn.tsetmc.com/api/";

    private static final String HOST = "cdn.tsetmc.com";

    private static final String UA =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36";

    private SSLSocketFactory sslFactory;
    private HostnameVerifier hostnameVerifier;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        setupSSL();
        buildPage();
        loadMarketData();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void setupSSL() {
        try {
            TrustManager[] trust = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(
                                X509Certificate[] c, String a) {}

                        public void checkServerTrusted(
                                X509Certificate[] c, String a) {}

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trust, new SecureRandom());

            sslFactory = ctx.getSocketFactory();

            hostnameVerifier = (host, session) ->
                    HOST.equalsIgnoreCase(host);

        } catch (Exception e) {
            sslFactory = null;
            hostnameVerifier = null;
        }
    }

    private void buildPage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 24, 24, 24);

        scroll.addView(content);
        setContentView(scroll);

        showMainMenu();
    }

    private void clear() {
        content.removeAllViews();
    }

    private TextView title(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(27);
        t.setTextColor(Color.rgb(20,65,100));
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(10,20,10,25);
        return t;
    }

    private TextView txt(String s, int size) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Color.DKGRAY);
        t.setGravity(Gravity.RIGHT);
        t.setPadding(10,8,10,8);
        return t;
    }

    private Button btn(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(18);
        b.setAllCaps(false);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        p.setMargins(0,6,0,6);
        b.setLayoutParams(p);

        return b;
    }

    private void back() {
        Button b = btn("⬅️ بازگشت");
        b.setOnClickListener(v -> showMainMenu());
        content.addView(b);
    }

    private void showMainMenu() {
        clear();

        content.addView(title("بورس‌یار 📈"));

        statusText = txt(
                "🟡 در حال بررسی اتصال TSETMC...",
                16);
        statusText.setGravity(Gravity.CENTER);
        content.addView(statusText);

        Button market = btn("📊 اطلاعات کلی بازار");
        market.setOnClickListener(v -> showMarket());
        content.addView(market);

        Button smart = btn("💵 پول هوشمند");
        smart.setOnClickListener(v -> showSmartMoney());
        content.addView(smart);

        Button flow = btn("🔄 ورود و خروج پول");
        flow.setOnClickListener(v -> showMoneyFlow());
        content.addView(flow);

        Button fundamental = btn("📚 تحلیل بنیادی");
        fundamental.setOnClickListener(v -> showFundamental());
        content.addView(fundamental);

        Button technical = btn("📈 تحلیل تکنیکال");
        technical.setOnClickListener(v -> showTechnical());
        content.addView(technical);

        Button symbols = btn("🔎 بررسی نمادها");
        symbols.setOnClickListener(v -> showSymbols());
        content.addView(symbols);

        Button suggestions = btn("💡 پیشنهادهای معاملاتی");
        suggestions.setOnClickListener(v -> showSuggestions());
        content.addView(suggestions);

        Button refresh = btn("🔄 دریافت دوباره اطلاعات");
        refresh.setOnClickListener(v -> loadMarketData());
        content.addView(refresh);
    }

    private void loadMarketData() {

        if (statusText != null)
            statusText.setText(
                    "🟡 در حال دریافت اطلاعات واقعی TSETMC...");

        executor.execute(() -> {

            Exception error = null;

            for (int attempt = 1; attempt <= 3; attempt++) {

                try {

                    String url = API +
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

                    parseMarket(response);

                    if (marketItems.isEmpty())
                        throw new Exception(
                                "اطلاعات بازار خالی است.");

                    int count = marketItems.size();

                    handler.post(() -> {
                        statusText.setText(
                                "🟢 اتصال برقرار است\n" +
                                count +
                                " نماد از TSETMC دریافت شد.");
                    });

                    return;

                } catch (Exception e) {
                    error = e;

                    try {
                        Thread.sleep(1200);
                    } catch (Exception ignored) {}
                }
            }

            String msg = readableError(error);

            handler.post(() -> {
                if (statusText != null)
                    statusText.setText(
                            "🔴 خطا در دریافت اطلاعات\n\n" + msg);
            });
        });
    }

    private String httpGet(String address) throws Exception {

        HttpURLConnection c = null;

        try {
            URL url = new URL(address);

            c = (HttpURLConnection) url.openConnection();

            c.setRequestMethod("GET");
            c.setConnectTimeout(20000);
            c.setReadTimeout(30000);
            c.setUseCaches(false);

            c.setRequestProperty("User-Agent", UA);
            c.setRequestProperty(
                    "Accept",
                    "application/json, text/plain, */*");
            c.setRequestProperty(
                    "Accept-Language",
                    "fa-IR,fa;q=0.9,en;q=0.8");
            c.setRequestProperty(
                    "Referer",
                    "https://tsetmc.com/");
            c.setRequestProperty(
                    "Cache-Control",
                    "no-cache");

            if (c instanceof HttpsURLConnection &&
                    HOST.equalsIgnoreCase(url.getHost())) {

                HttpsURLConnection h =
                        (HttpsURLConnection)c;

                if (sslFactory != null)
                    h.setSSLSocketFactory(sslFactory);

                if (hostnameVerifier != null)
                    h.setHostnameVerifier(hostnameVerifier);
            }

            int code = c.getResponseCode();

            InputStream in;

            if (code >= 200 && code < 300) {
                in = c.getInputStream();
            } else {
                in = c.getErrorStream();

                String body = read(in);

                throw new IOException(
                        "HTTP " + code + "\n" + body);
            }

            String result = read(in);

            if (result == null || result.trim().isEmpty())
                throw new IOException("پاسخ خالی است.");

            return result;

        } finally {
            if (c != null)
                c.disconnect();
        }
    }

    private String read(InputStream in) throws IOException {

        if (in == null)
            return "";

        BufferedReader r =
                new BufferedReader(
                        new InputStreamReader(
                                in,
                                StandardCharsets.UTF_8));

        StringBuilder s = new StringBuilder();
        String line;

        while ((line = r.readLine()) != null)
            s.append(line);

        r.close();

        return s.toString();
    }

    private void parseMarket(String response)
            throws Exception {

        marketItems.clear();

        JSONObject root =
                new JSONObject(response.trim());

        JSONArray arr =
                root.optJSONArray("marketwatch");

        if (arr == null)
            arr = root.optJSONArray("marketWatch");

        if (arr == null)
            throw new Exception(
                    "داده marketwatch در پاسخ TSETMC وجود ندارد.");

        for (int i = 0; i < arr.length(); i++) {

            JSONObject o = arr.getJSONObject(i);

            MarketItem m = new MarketItem();

            /*
             * فیلدهای صحیح Market Watch
             */
            m.insCode = str(o,
                    "insCode");

            m.symbol = str(o,
                    "lVal18AFC",
                    "lVal18",
                    "symbol");

            m.name = str(o,
                    "lVal30",
                    "name",
                    "title");

            m.last = num(o,
                    "pDrCotVal",
                    "pl");

            m.close = num(o,
                    "pClosing",
                    "pc");

            m.yesterday = num(o,
                    "priceYesterday",
                    "py");

            m.first = num(o,
                    "priceFirst",
                    "pf");

            m.min = num(o,
                    "priceMin",
                    "pmin");

            m.max = num(o,
                    "priceMax",
                    "pmax");

            m.volume = num(o,
                    "qTotTran5J",
                    "tvol");

            m.value = num(o,
                    "qTotCap",
                    "tval");

            m.trades = num(o,
                    "zTotTran",
                    "tno");

            /*
             * بعضی پاسخ‌ها درصد تغییر را مستقیماً دارند.
             * اگر نداشت، خودمان محاسبه می‌کنیم.
             */
            m.percent = num(o,
                    "percent",
                    "priceChangePercent");

            if (m.percent == 0 &&
                    m.yesterday != 0) {

                m.percent =
                        ((m.close - m.yesterday) /
                                m.yesterday) * 100.0;
            }

            if (m.symbol == null ||
                    m.symbol.trim().isEmpty()) {

                m.symbol = "بدون نماد";
            }

            marketItems.add(m);
        }
    }

    private String str(JSONObject o, String... keys) {

        for (String k : keys) {
            try {
                if (o.has(k) && !o.isNull(k)) {
                    String s = o.getString(k);

                    if (s != null &&
                            !s.trim().isEmpty() &&
                            !"null".equalsIgnoreCase(s))
                        return s;
                }
            } catch (Exception ignored) {}
        }

        return "";
    }

    private double num(JSONObject o, String... keys) {

        for (String k : keys) {

            try {

                if (!o.has(k) || o.isNull(k))
                    continue;

                Object v = o.get(k);

                if (v instanceof Number)
                    return ((Number)v).doubleValue();

                String s = String.valueOf(v)
                        .replace(",", "")
                        .trim();

                if (!s.isEmpty() &&
                        !"null".equalsIgnoreCase(s))
                    return Double.parseDouble(s);

            } catch (Exception ignored) {}
        }

        return 0;
    }

    private void showMarket() {

        clear();

        content.addView(
                title("اطلاعات کلی بازار 📊"));

        if (marketItems.isEmpty()) {

            content.addView(
                    txt(
                            "🟡 هنوز اطلاعات بازار دریافت نشده.",
                            18));

            Button b = btn(
                    "🔄 دریافت اطلاعات");

            b.setOnClickListener(v -> {
                showMainMenu();
                loadMarketData();
            });

            content.addView(b);
            back();
            return;
        }

        int positive = 0;
        int negative = 0;
        int unchanged = 0;

        double volume = 0;
        double value = 0;
        double trades = 0;

        for (MarketItem m : marketItems) {

            volume += m.volume;
            value += m.value;
            trades += m.trades;

            if (m.percent > 0.0001)
                positive++;
            else if (m.percent < -0.0001)
                negative++;
            else
                unchanged++;
        }

        content.addView(
                txt("🟢 اتصال TSETMC برقرار است", 19));

        content.addView(
                txt(
                        "تعداد نمادهای دریافتی: " +
                        marketItems.size(),
                        19));

        content.addView(
                txt(
                        "🟢 مثبت: " + positive,
                        18));

        content.addView(
                txt(
                        "🔴 منفی: " + negative,
                        18));

        content.addView(
                txt(
                        "⚪ بدون تغییر: " + unchanged,
                        18));

        content.addView(
                txt(
                        "حجم معاملات: " +
                        format(volume),
                        18));

        content.addView(
                txt(
                        "ارزش معاملات: " +
                        format(value),
                        18));

        content.addView(
                txt(
                        "تعداد معاملات: " +
                        format(trades),
                        18));

        content.addView(
                txt(
                        "\nنمونه اطلاعات نمادها:",
                        20));

        int limit = Math.min(30, marketItems.size());

        for (int i = 0; i < limit; i++) {

            MarketItem m = marketItems.get(i);

            String symbol =
                    m.symbol == null ||
                    m.symbol.trim().isEmpty()
                            ? "بدون نماد"
                            : m.symbol;

            String name =
                    m.name == null ||
                    m.name.trim().isEmpty()
                            ? "-"
                            : m.name;

            content.addView(
                    txt(
                            (i + 1) +
                            ". " + symbol +
                            "\nنام: " + name +
                            "\nآخرین: " +
                            format(m.last) +
                            "\nپایانی: " +
                            format(m.close) +
                            "\nدرصد: " +
                            String.format(
                                    Locale.US,
                                    "%.2f%%",
                                    m.percent) +
                            "\nحجم: " +
                            format(m.volume) +
                            "\nارزش: " +
                            format(m.value),
                            16));
        }

        back();
    }

    private void showSmartMoney() {

        clear();

        content.addView(
                title("پول هوشمند 💵"));

        content.addView(
                txt(
                        "🟡 در حال دریافت اطلاعات حقیقی و حقوقی...",
                        18));

        executor.execute(() -> {

            try {

                String response =
                        httpGet(
                                API +
                                "ClientType/GetClientTypeAll");

                JSONObject root =
                        new JSONObject(response);

                JSONArray a =
                        root.optJSONArray(
                                "clientTypeAllDto");

                if (a == null)
                    throw new Exception(
                            "داده حقیقی/حقوقی پیدا نشد.");

                List<Money> list =
                        new ArrayList<>();

                for (int i = 0;
                     i < a.length();
                     i++) {

                    JSONObject o =
                            a.getJSONObject(i);

                    Money m = new Money();

                    m.code = str(o, "insCode");

                    m.buy = num(o,
                            "buy_I_Volume",
                            "buyIVolume");

                    m.sell = num(o,
                            "sell_I_Volume",
                            "sellIVolume");

                    m.net = m.buy - m.sell;

                    if (m.net > 0)
                        list.add(m);
                }

                list.sort(
                        (x,y) ->
                                Double.compare(
                                        y.net,
                                        x.net));

                handler.post(() ->
                        showMoneyResult(list));

            } catch (Exception e) {

                String error =
                        readableError(e);

                handler.post(() -> {

                    clear();

                    content.addView(
                            title("پول هوشمند 💵"));

                    content.addView(
                            txt("❌ " + error, 17));

                    back();
                });
            }
        });
    }

    private void showMoneyResult(
            List<Money> list) {

        clear();

        content.addView(
                title("پول هوشمند 💵"));

        if (list.isEmpty()) {

            content.addView(
                    txt(
                            "ورود خالص حقیقی مثبت پیدا نشد.",
                            18));

        } else {

            int limit =
                    Math.min(20, list.size());

            for (int i = 0; i < limit; i++) {

                Money m = list.get(i);

                content.addView(
                        txt(
                                (i + 1) +
                                ". " +
                                findSymbol(m.code) +
                                "\nخرید حقیقی: " +
                                format(m.buy) +
                                "\nفروش حقیقی: " +
                                format(m.sell) +
                                "\nورود خالص: " +
                                format(m.net),
                                17));
            }
        }

        content.addView(
                txt(
                        "\nتوجه: این بخش فعلاً ورود خالص را بر اساس حجم سهم نشان می‌دهد؛ محاسبه پول ریالی دقیق باید با قیمت هر نماد انجام شود.",
                        15));

        back();
    }

    private void showMoneyFlow() {

        clear();

        content.addView(
                title("ورود و خروج پول 🔄"));

        content.addView(
                txt(
                        "🟡 در حال دریافت اطلاعات...",
                        18));

        executor.execute(() -> {

            try {

                String response =
                        httpGet(
                                API +
                                "ClientType/GetClientTypeAll");

                JSONObject root =
                        new JSONObject(response);

                JSONArray a =
                        root.optJSONArray(
                                "clientTypeAllDto");

                if (a == null)
                    throw new Exception(
                            "اطلاعات حقیقی/حقوقی موجود نیست.");

                double buy = 0;
                double sell = 0;

                for (int i = 0;
                     i < a.length();
                     i++) {

                    JSONObject o =
                            a.getJSONObject(i);

                    buy += num(o,
                            "buy_I_Volume",
                            "buyIVolume");

                    sell += num(o,
                            "sell_I_Volume",
                            "sellIVolume");
                }

                double net = buy - sell;

                handler.post(() -> {

                    clear();

                    content.addView(
                            title(
                                    "ورود و خروج پول 🔄"));

                    content.addView(
                            txt(
                                    "خرید حقیقی: " +
                                    format(buy),
                                    19));

                    content.addView(
                            txt(
                                    "فروش حقیقی: " +
                                    format(sell),
                                    19));

                    if (net > 0) {

                        content.addView(
                                txt(
                                        "🟢 ورود خالص حقیقی: " +
                                        format(net),
                                        21));

                    } else if (net < 0) {

                        content.addView(
                                txt(
                                        "🔴 خروج خالص حقیقی: " +
                                        format(Math.abs(net)),
                                        21));

                    } else {

                        content.addView(
                                txt(
                                        "⚪ خالص ورود و خروج صفر است.",
                                        20));
                    }

                    back();
                });

            } catch (Exception e) {

                String error =
                        readableError(e);

                handler.post(() -> {

                    clear();

                    content.addView(
                            title(
                                    "ورود و خروج پول 🔄"));

                    content.addView(
                            txt(
                                    "❌ " + error,
                                    17));

                    back();
                });
            }
        });
    }

    private String findSymbol(String code) {

        if (code == null)
            return "نامشخص";

        for (MarketItem m : marketItems) {

            if (code.equals(m.insCode))
                return m.symbol;
        }

        return code;
    }

    private void showSymbols() {

        clear();

        content.addView(
                title("بررسی نمادها 🔎"));

        EditText input = new EditText(this);
        input.setHint("مثلاً فولاد");
        input.setTextSize(18);
        input.setSingleLine(true);

        content.addView(input);

        Button search = btn("🔍 جستجو");
        content.addView(search);

        TextView result = txt("",17);
        content.addView(result);

        search.setOnClickListener(v -> {

            String s =
                    input.getText()
                            .toString()
                            .trim();

            if (s.isEmpty()) {
                result.setText(
                        "نام نماد را وارد کنید.");
                return;
            }

            hideKeyboard(input);

            result.setText(
                    "🟡 در حال جستجو...");

            executor.execute(() -> {

                try {

                    String q =
                            URLEncoder.encode(
                                    s,
                                    "UTF-8");

                    String response =
                            httpGet(
                                    API +
                                    "Instrument/GetInstrumentSearch/" +
                                    q);

                    JSONObject root =
                            new JSONObject(response);

                    JSONArray a =
                            root.optJSONArray(
                                    "instrumentSearch");

                    if (a == null ||
                            a.length() == 0) {

                        handler.post(() ->
                                result.setText(
                                        "نمادی پیدا نشد."));
                        return;
                    }

                    StringBuilder out =
                            new StringBuilder();

                    for (int i = 0;
                         i < Math.min(10,a.length());
                         i++) {

                        JSONObject o =
                                a.getJSONObject(i);

                        out.append("نماد: ")
                                .append(
                                        str(o,
                                                "lVal18AFC",
                                                "lVal18"))
                                .append("\nنام: ")
                                .append(
                                        str(o,
                                                "lVal30"))
                                .append("\n\n");
                    }

                    String finalOut =
                            out.toString();

                    handler.post(() ->
                            result.setText(finalOut));

                } catch (Exception e) {

                    String error =
                            readableError(e);

                    handler.post(() ->
                            result.setText(
                                    "❌ " + error));
                }
            });
        });

        back();
    }

    private void showFundamental() {

        clear();

        content.addView(
                title("تحلیل بنیادی 📚"));

        content.addView(
                txt(
                        "این بخش آماده اتصال به کدال است.\n\n" +
                        "EPS\nP/E\nفروش\nسود\n" +
                        "گزارش‌های کدال\n\n" +
                        "مرحله بعد اتصال اطلاعات بنیادی خواهد بود.",
                        18));

        back();
    }

    private void showTechnical() {

        clear();

        content.addView(
                title("تحلیل تکنیکال 📈"));

        content.addView(
                txt(
                        "برای تحلیل تکنیکال باید سابقه قیمت نماد دریافت شود.\n\n" +
                        "RSI\nMACD\nمیانگین متحرک\nحجم\nروند قیمت",
                        18));

        back();
    }

    private void showSuggestions() {

        clear();

        content.addView(
                title("پیشنهادهای معاملاتی 💡"));

        if (marketItems.isEmpty()) {

            content.addView(
                    txt(
                            "ابتدا اطلاعات بازار را دریافت کنید.",
                            18));

            back();
            return;
        }

        List<MarketItem> list =
                new ArrayList<>();

        for (MarketItem m :
                marketItems) {

            if (m.percent > 0 &&
                    m.volume > 0 &&
                    m.value > 0) {

                list.add(m);
            }
        }

        list.sort(
                (a,b) ->
                        Double.compare(
                                b.percent,
                                a.percent));

        int limit =
                Math.min(20,list.size());

        content.addView(
                txt(
                        "نمادهای دارای رشد قیمت و فعالیت معاملاتی:",
                        18));

        for (int i = 0; i < limit; i++) {

            MarketItem m = list.get(i);

            content.addView(
                    txt(
                            (i + 1) +
                            ". " + m.symbol +
                            "\nآخرین: " +
                            format(m.last) +
                            "\nپایانی: " +
                            format(m.close) +
                            "\nدرصد: " +
                            String.format(
                                    Locale.US,
                                    "%.2f%%",
                                    m.percent) +
                            "\nحجم: " +
                            format(m.volume),
                            17));
        }

        back();
    }

    private String readableError(Exception e) {

        if (e == null)
            return "خطای نامشخص";

        String s = e.getMessage();

        if (s == null || s.isEmpty())
            s = e.toString();

        if (s.contains("Trust anchor") ||
                s.toLowerCase(Locale.US)
                        .contains("ssl")) {

            return "خطای SSL:\n" + s;
        }

        if (s.contains("HTTP 403"))
            return "TSETMC دسترسی را رد کرده است.";

        if (s.toLowerCase(Locale.US)
                .contains("timeout"))

            return "زمان اتصال به TSETMC تمام شد.";

        return s;
    }

    private String format(double n) {

        if (Math.abs(n) >= 1000000000)
            return String.format(
                    Locale.US,
                    "%.2f میلیارد",
                    n / 1000000000.0);

        if (Math.abs(n) >= 1000000)
            return String.format(
                    Locale.US,
                    "%.2f میلیون",
                    n / 1000000.0);

        if (Math.abs(n) >= 1000)
            return String.format(
                    Locale.US,
                    "%.0f هزار",
                    n / 1000.0);

        return String.format(
                Locale.US,
                "%.0f",
                n);
    }

    private void hideKeyboard(View v) {

        try {

            InputMethodManager imm =
                    (InputMethodManager)
                            getSystemService(
                                    Context.INPUT_METHOD_SERVICE);

            if (imm != null)
                imm.hideSoftInputFromWindow(
                        v.getWindowToken(),0);

        } catch (Exception ignored) {}
    }

    private static class MarketItem {

        String insCode = "";
        String symbol = "";
        String name = "";

        double last;
        double close;
        double yesterday;
        double first;
        double min;
        double max;
        double volume;
        double value;
        double trades;
        double percent;
    }

    private static class Money {

        String code = "";

        double buy;
        double sell;
        double net;
    }
}
