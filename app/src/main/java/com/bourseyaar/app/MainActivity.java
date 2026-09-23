package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSocketFactory;

import java.security.cert.X509Certificate;

public class MainActivity extends Activity {

    private static final String BASE_URL =
            "https://cdn.tsetmc.com/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMainPage();
    }

    private TextView title(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(24);
        t.setTextColor(Color.WHITE);
        t.setGravity(Gravity.CENTER);
        t.setPadding(10, 25, 10, 25);
        return t;
    }

    private Button menuButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(17);
        b.setAllCaps(false);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        p.setMargins(0, 7, 0, 7);
        b.setLayoutParams(p);

        return b;
    }

    private ScrollView newScroll() {
        return new ScrollView(this);
    }

    private LinearLayout newPage() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(20, 20, 20, 20);
        return page;
    }

    private void showMainPage() {

        ScrollView scroll = newScroll();
        LinearLayout page = newPage();

        TextView header = title("بورس‌یار");
        header.setBackgroundColor(Color.rgb(30, 100, 180));
        page.addView(header);

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "دستیار تحلیل بازار بورس ایران\n\n" +
                "اطلاعات زنده بازار و جریان پول");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(10, 20, 10, 20);
        page.addView(subtitle);

        Button market =
                menuButton("📊 اطلاعات کلی بورس ایران");

        Button smartMoney =
                menuButton("💵 پول هوشمند");

        Button flow =
                menuButton("🔄 ورود و خروج پول");

        Button fundamental =
                menuButton("💰 تحلیل بنیادی");

        Button technical =
                menuButton("📈 تحلیل تکنیکال");

        Button valuable =
                menuButton("⭐ سهم‌های ارزنده");

        Button portfolio =
                menuButton("📁 بررسی سهام‌های من");

        page.addView(market);
        page.addView(smartMoney);
        page.addView(flow);
        page.addView(fundamental);
        page.addView(technical);
        page.addView(valuable);
        page.addView(portfolio);

        market.setOnClickListener(
                v -> showMarketPage());

        smartMoney.setOnClickListener(
                v -> showSmartMoneyPage());

        flow.setOnClickListener(
                v -> showMoneyFlowPage());

        fundamental.setOnClickListener(
                v -> showPage(
                        "تحلیل بنیادی",
                        "این بخش در مرحله بعد با اطلاعات بنیادی نمادها تکمیل می‌شود.\n\n" +
                        "EPS\n\nP/E\n\nرشد فروش\n\nسودآوری\n\nارزش ذاتی"));

        technical.setOnClickListener(
                v -> showPage(
                        "تحلیل تکنیکال",
                        "ابزارهای تحلیل تکنیکال:\n\n" +
                        "RSI\n\nMACD\n\nمیانگین متحرک\n\nحمایت و مقاومت\n\nروند سهم"));

        valuable.setOnClickListener(
                v -> showPage(
                        "سهم‌های ارزنده",
                        "در مرحله بعد نمادها بر اساس بنیادی، تکنیکال و جریان پول بررسی می‌شوند."));

        portfolio.setOnClickListener(
                v -> showPage(
                        "سهام‌های من",
                        "در مرحله بعد امکان ثبت و بررسی سبد سهام اضافه می‌شود."));

        scroll.addView(page);
        setContentView(scroll);
    }

    private void showMarketPage() {

        ScrollView scroll = newScroll();
        LinearLayout page = newPage();

        TextView header =
                title("📊 اطلاعات کلی بورس ایران");

        header.setBackgroundColor(
                Color.rgb(30, 100, 180));

        page.addView(header);

        TextView status = new TextView(this);
        status.setText(
                "⏳ در حال دریافت اطلاعات بازار...");
        status.setTextSize(18);
        status.setPadding(15, 30, 15, 30);

        page.addView(status);

        Button refresh =
                menuButton("🔄 دریافت اطلاعات");

        Button back =
                menuButton("⬅ بازگشت");

        page.addView(refresh);
        page.addView(back);

        refresh.setOnClickListener(
                v -> loadMarketData(status));

        back.setOnClickListener(
                v -> showMainPage());

        scroll.addView(page);
        setContentView(scroll);

        loadMarketData(status);
    }

    private void showMoneyFlowPage() {

        ScrollView scroll = newScroll();
        LinearLayout page = newPage();

        TextView header =
                title("🔄 ورود و خروج پول");

        header.setBackgroundColor(
                Color.rgb(30, 100, 180));

        page.addView(header);

        TextView status = new TextView(this);
        status.setText(
                "⏳ در حال دریافت اطلاعات پول حقیقی...");
        status.setTextSize(17);
        status.setPadding(10, 25, 10, 25);

        page.addView(status);

        Button refresh =
                menuButton("🔄 به‌روزرسانی");

        Button back =
                menuButton("⬅ بازگشت");

        page.addView(refresh);
        page.addView(back);

        refresh.setOnClickListener(
                v -> loadMoneyFlow(status));

        back.setOnClickListener(
                v -> showMainPage());

        scroll.addView(page);
        setContentView(scroll);

        loadMoneyFlow(status);
    }

    private void showSmartMoneyPage() {

        ScrollView scroll = newScroll();
        LinearLayout page = newPage();

        TextView header =
                title("💵 پول هوشمند");

        header.setBackgroundColor(
                Color.rgb(30, 100, 180));

        page.addView(header);

        TextView status = new TextView(this);
        status.setText(
                "⏳ در حال بررسی جریان پول...");
        status.setTextSize(17);
        status.setPadding(10, 25, 10, 25);

        page.addView(status);

        Button refresh =
                menuButton("🔄 به‌روزرسانی");

        Button back =
                menuButton("⬅ بازگشت");

        page.addView(refresh);
        page.addView(back);

        refresh.setOnClickListener(
                v -> loadSmartMoney(status));

        back.setOnClickListener(
                v -> showMainPage());

        scroll.addView(page);
        setContentView(scroll);

        loadSmartMoney(status);
    }

    private SSLSocketFactory createTrustAllSocketFactory()
            throws Exception {

        TrustManager[] trustAllCerts =
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
                trustAllCerts,
                new java.security.SecureRandom());

        return sslContext.getSocketFactory();
    }

    private String requestUrl(String address)
            throws Exception {

        URL url = new URL(address);

        HttpsURLConnection connection =
                (HttpsURLConnection)
                        url.openConnection();

        connection.setSSLSocketFactory(
                createTrustAllSocketFactory());

        connection.setHostnameVerifier(
                (hostname, session) -> true);

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0");

        connection.setRequestProperty(
                "Accept",
                "application/json");

        int code =
                connection.getResponseCode();

        if (code < 200 || code >= 300) {
            throw new Exception(
                    "HTTP " + code);
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));

        StringBuilder result =
                new StringBuilder();

        String line;

        while ((line =
                reader.readLine()) != null) {

            result.append(line);
        }

        reader.close();
        connection.disconnect();

        return result.toString();
    }

    private double getDouble(
            JSONObject object,
            String key) {

        if (!object.has(key) ||
                object.isNull(key)) {
            return 0;
        }

        return object.optDouble(key, 0);
    }

    private String formatNumber(double value) {

        if (Double.isNaN(value) ||
                Double.isInfinite(value)) {
            return "0";
        }

        DecimalFormatSymbols symbols =
                new DecimalFormatSymbols(
                        Locale.US);

        DecimalFormat format =
                new DecimalFormat(
                        "#,##0.##",
                        symbols);

        return format.format(value);
    }

    private String money(double value) {

        double abs = Math.abs(value);

        if (abs >= 1000000000000.0) {
            return formatNumber(
                    value / 1000000000000.0)
                    + " همت";
        }

        if (abs >= 1000000000.0) {
            return formatNumber(
                    value / 1000000000.0)
                    + " میلیارد ریال";
        }

        if (abs >= 1000000.0) {
            return formatNumber(
                    value / 1000000.0)
                    + " میلیون ریال";
        }

        return formatNumber(value)
                + " ریال";
    }

    private void loadMarketData(
            TextView status) {

        status.setText(
                "⏳ در حال دریافت شاخص‌های بازار...");

        new Thread(() -> {

            String result;

            try {

                String json =
                        requestUrl(
                                BASE_URL +
                                "Index/GetIndexB1LastAll/SelectedIndexes/1");

                JSONObject root =
                        new JSONObject(json);

                JSONArray indexes =
                        root.optJSONArray("indexB1");

                if (indexes == null ||
                        indexes.length() == 0) {

                    throw new Exception(
                            "اطلاعات شاخص دریافت نشد");
                }

                StringBuilder text =
                        new StringBuilder();

                text.append(
                        "📊 وضعیت شاخص‌های بازار\n\n");

                for (int i = 0;
                        i < indexes.length();
                        i++) {

                    JSONObject item =
                            indexes.getJSONObject(i);

                    String name =
                            item.optString(
                                    "lVal30",
                                    "شاخص");

                    double value =
                            getDouble(
                                    item,
                                    "xDrNivJIdx004");

                    double change =
                            getDouble(
                                    item,
                                    "indexChange");

                    double percent =
                            getDouble(
                                    item,
                                    "xVarIdxJRfV");

                    text.append(
                            "━━━━━━━━━━━━━━━━━━\n");

                    text.append("📈 ")
                            .append(name)
                            .append("\n\n");

                    text.append(
                            "مقدار: ")
                            .append(
                                    formatNumber(value))
                            .append("\n");

                    text.append(
                            "تغییر: ")
                            .append(
                                    formatNumber(change))
                            .append("\n");

                    text.append(
                            "درصد تغییر: ")
                            .append(
                                    formatNumber(percent))
                            .append("%\n\n");
                }

                text.append(
                        "━━━━━━━━━━━━━━━━━━\n");

                text.append(
                        "✅ اطلاعات از TSETMC دریافت شد.");

                result =
                        text.toString();

            } catch (Exception e) {

                result =
                        "❌ خطا در دریافت اطلاعات\n\n" +
                        e.getClass()
                                .getSimpleName() +
                        "\n\n" +
                        e.getMessage();
            }

            final String finalResult =
                    result;

            runOnUiThread(
                    () -> status.setText(
                            finalResult));

        }).start();
    }

    private void loadMoneyFlow(
            TextView status) {

        status.setText(
                "⏳ در حال دریافت ورود و خروج پول...");

        new Thread(() -> {

            String result;

            try {

                String clientJson =
                        requestUrl(
                                BASE_URL +
                                "ClientType/GetClientTypeAll");

                String marketJson =
                        requestUrl(
                                BASE_URL +
                                "ClosingPrice/GetMarketWatch" +
                                "?market=0" +
                                "&industrialGroup=" +
                                "&paperTypes%5B0%5D=1" +
                                "&paperTypes%5B1%5D=2" +
                                "&paperTypes%5B2%5D=3" +
                                "&paperTypes%5B3%5D=4" +
                                "&paperTypes%5B4%5D=5" +
                                "&paperTypes%5B5%5D=6" +
                                "&paperTypes%5B6%5D=7" +
                                "&paperTypes%5B7%5D=8" +
                                "&paperTypes%5B8%5D=9" +
                                "&showTraded=true" +
                                "&withBestLimits=false" +
                                "&hEven=0" +
                                "&RefID=0");

                JSONObject clientRoot =
                        new JSONObject(clientJson);

                JSONObject marketRoot =
                        new JSONObject(marketJson);

                JSONArray clients =
                        clientRoot.optJSONArray(
                                "clientTypeAllDto");

                JSONArray market =
                        marketRoot.optJSONArray(
                                "marketwatch");

                if (clients == null ||
                        clients.length() == 0) {

                    throw new Exception(
                            "فهرست حقیقی و حقوقی خالی است");
                }

                Map<String, JSONObject>
                        marketMap =
                        new HashMap<>();

                if (market != null) {

                    for (int i = 0;
                            i < market.length();
                            i++) {

                        JSONObject item =
                                market.getJSONObject(i);

                        String code =
                                item.optString(
                                        "insCode",
                                        "");

                        if (!code.isEmpty()) {
                            marketMap.put(
                                    code,
                                    item);
                        }
                    }
                }

                ArrayList<FlowItem>
                        list =
                        new ArrayList<>();

                for (int i = 0;
                        i < clients.length();
                        i++) {

                    JSONObject client =
                            clients.getJSONObject(i);

                    String code =
                            client.optString(
                                    "insCode",
                                    "");

                    double buyI =
                            getDouble(
                                    client,
                                    "buy_I_Volume");

                    double sellI =
                            getDouble(
                                    client,
                                    "sell_I_Volume");

                    double buyN =
                            getDouble(
                                    client,
                                    "buy_N_Volume");

                    double sellN =
                            getDouble(
                                    client,
                                    "sell_N_Volume");

                    double netVolume =
                            buyI - sellI;

                    JSONObject marketItem =
                            marketMap.get(code);

                    String symbol = code;
                    double price = 0;

                    if (marketItem != null) {

                        symbol =
                                marketItem.optString(
                                        "lVal18AFC",
                                        code);

                        price =
                                getDouble(
                                        marketItem,
                                        "pClosing");

                        if (price == 0) {
                            price =
                                    getDouble(
                                            marketItem,
                                            "pDrCotVal");
                        }

                        if (price == 0) {
                            price =
                                    getDouble(
                                            marketItem,
                                            "pl");
                        }
                    }

                    FlowItem item =
                            new FlowItem();

                    item.symbol = symbol;
                    item.buyI = buyI;
                    item.sellI = sellI;
                    item.buyN = buyN;
                    item.sellN = sellN;
                    item.netVolume =
                            netVolume;
                    item.price = price;
                    item.netValue =
                            netVolume * price;

                    list.add(item);
                }

                Collections.sort(
                        list,
                        (a, b) ->
                                Double.compare(
                                        b.netValue,
                                        a.netValue));

                StringBuilder text =
                        new StringBuilder();

                text.append(
                        "🟢 بیشترین ورود پول حقیقی\n\n");

                int count = 0;

                for (FlowItem item : list) {

                    if (item.netVolume <= 0) {
                        continue;
                    }

                    text.append(
                            "━━━━━━━━━━━━━━━━━━\n");

                    text.append("🟢 ")
                            .append(item.symbol)
                            .append("\n");

                    text.append(
                            "خرید حقیقی: ")
                            .append(
                                    formatNumber(
                                            item.buyI))
                            .append("\n");

                    text.append(
                            "فروش حقیقی: ")
                            .append(
                                    formatNumber(
                                            item.sellI))
                            .append("\n");

                    text.append(
                            "خالص حجم پول: ")
                            .append(
                                    formatNumber(
                                            item.netVolume))
                            .append("\n");

                    if (item.price > 0) {

                        text.append(
                                "ارزش خالص تقریبی: ")
                                .append(
                                        money(
                                                item.netValue))
                                .append("\n");
                    }

                    count++;

                    if (count >= 15) {
                        break;
                    }
                }

                text.append(
                        "\n🔴 بیشترین خروج پول حقیقی\n\n");

                count = 0;

                for (FlowItem item : list) {

                    if (item.netVolume >= 0) {
                        continue;
                    }

                    text.append(
                            "━━━━━━━━━━━━━━━━━━\n");

                    text.append("🔴 ")
                            .append(item.symbol)
                            .append("\n");

                    text.append(
                            "خرید حقیقی: ")
                            .append(
                                    formatNumber(
                                            item.buyI))
                            .append("\n");

                    text.append(
                            "فروش حقیقی: ")
                            .append(
                                    formatNumber(
                                            item.sellI))
                            .append("\n");

                    text.append(
                            "خالص حجم پول: ")
                            .append(
                                    formatNumber(
                                            item.netVolume))
                            .append("\n");

                    if (item.price > 0) {

                        text.append(
                                "ارزش خالص تقریبی: ")
                                .append(
                                        money(
                                                item.netValue))
                                .append("\n");
                    }

                    count++;

                    if (count >= 15) {
                        break;
                    }
                }

                result =
                        text.toString();

            } catch (Exception e) {

                result =
                        "❌ دریافت ورود و خروج پول انجام نشد.\n\n" +
                        "خطا: " +
                        e.getClass()
                                .getSimpleName() +
                        "\n\n" +
                        e.getMessage();
            }

            final String finalResult =
                    result;

            runOnUiThread(
                    () -> status.setText(
                            finalResult));

        }).start();
    }

    private void loadSmartMoney(
            TextView status) {

        status.setText(
                "⏳ در حال شناسایی نمادهای دارای جریان پول...");

        new Thread(() -> {

            String result;

            try {

                String clientJson =
                        requestUrl(
                                BASE_URL +
                                "ClientType/GetClientTypeAll");

                String marketJson =
                        requestUrl(
                                BASE_URL +
                                "ClosingPrice/GetMarketWatch" +
                                "?market=0" +
                                "&industrialGroup=" +
                                "&paperTypes%5B0%5D=1" +
                                "&paperTypes%5B1%5D=2" +
                                "&paperTypes%5B2%5D=3" +
                                "&paperTypes%5B3%5D=4" +
                                "&paperTypes%5B4%5D=5" +
                                "&paperTypes%5B5%5D=6" +
                                "&paperTypes%5B6%5D=7" +
                                "&paperTypes%5B7%5D=8" +
                                "&paperTypes%5B8%5D=9" +
                                "&showTraded=true" +
                                "&withBestLimits=false" +
                                "&hEven=0" +
                                "&RefID=0");

                JSONObject clientRoot =
                        new JSONObject(clientJson);

                JSONObject marketRoot =
                        new JSONObject(marketJson);

                JSONArray clients =
                        clientRoot.optJSONArray(
                                "clientTypeAllDto");

                JSONArray market =
                        marketRoot.optJSONArray(
                                "marketwatch");

                if (clients == null ||
                        clients.length() == 0) {

                    throw new Exception(
                            "اطلاعات پول حقیقی دریافت نشد");
                }

                Map<String, JSONObject>
                        marketMap =
                        new HashMap<>();

                if (market != null) {

                    for (int i = 0;
                            i < market.length();
                            i++) {

                        JSONObject m =
                                market.getJSONObject(i);

                        String code =
                                m.optString(
                                        "insCode",
                                        "");

                        if (!code.isEmpty()) {
                            marketMap.put(
                                    code,
                                    m);
                        }
                    }
                }

                ArrayList<SmartItem>
                        list =
                        new ArrayList<>();

                for (int i = 0;
                        i < clients.length();
                        i++) {

                    JSONObject c =
                            clients.getJSONObject(i);

                    String code =
                            c.optString(
                                    "insCode",
                                    "");

                    double buyI =
                            getDouble(
                                    c,
                                    "buy_I_Volume");

                    double sellI =
                            getDouble(
                                    c,
                                    "sell_I_Volume");

                    double buyN =
                            getDouble(
                                    c,
                                    "buy_N_Volume");

                    double sellN =
                            getDouble(
                                    c,
                                    "sell_N_Volume");

                    double buyCount =
                            getDouble(
                                    c,
                                    "buy_CountI");

                    double sellCount =
                            getDouble(
                                    c,
                                    "sell_CountI");

                    double netVolume =
                            buyI - sellI;

                    if (netVolume <= 0) {
                        continue;
                    }

                    double power = 0;

                    if (buyCount > 0 &&
                            sellCount > 0) {

                        double avgBuy =
                                buyI / buyCount;

                        double avgSell =
                                sellI / sellCount;

                        if (avgSell > 0) {
                            power =
                                    avgBuy /
                                    avgSell;
                        }
                    }

                    JSONObject m =
                            marketMap.get(code);

                    String symbol = code;
                    double price = 0;

                    if (m != null) {

                        symbol =
                                m.optString(
                                        "lVal18AFC",
                                        code);

                        price =
                                getDouble(
                                        m,
                                        "pClosing");

                        if (price == 0) {
                            price =
                                    getDouble(
                                            m,
                                            "pDrCotVal");
                        }

                        if (price == 0) {
                            price =
                                    getDouble(
                                            m,
                                            "pl");
                        }
                    }

                    double netValue =
                            netVolume * price;

                    double score = 0;

                    if (netVolume > 0) {
                        score += 40;
                    }

                    if (power >= 2.0) {
                        score += 40;
                    } else if (power >= 1.5) {
                        score += 30;
                    } else if (power >= 1.2) {
                        score += 20;
                    }

                    if (buyN < sellN) {
                        score += 20;
                    }

                    if (score < 40) {
                        continue;
                    }

                    SmartItem item =
                            new SmartItem();

                    item.symbol = symbol;
                    item.buyI = buyI;
                    item.sellI = sellI;
                    item.netVolume =
                            netVolume;
                    item.netValue =
                            netValue;
                    item.power =
                            power;
                    item.score =
                            score;

                    list.add(item);
                }

                Collections.sort(
                        list,
                        (a, b) ->
                                Double.compare(
                                        b.score,
                                        a.score));

                StringBuilder text =
                        new StringBuilder();

                text.append(
                        "💵 نمادهای دارای نشانه جریان پول\n\n");

                text.append(
                        "این فهرست یک فیلتر تحلیلی است و به‌تنهایی سیگنال خرید یا فروش نیست.\n\n");

                int count = 0;

                for (SmartItem item :
                        list) {

                    text.append(
                            "━━━━━━━━━━━━━━━━━━\n");

                    text.append("🧠 ")
                            .append(
                                    item.symbol)
                            .append("\n");

                    text.append(
                            "خرید حقیقی: ")
                            .append(
                                    formatNumber(
                                            item.buyI))
                            .append("\n");

                    text.append(
                            "فروش حقیقی: ")
                            .append(
                                    formatNumber(
                                            item.sellI))
                            .append("\n");

                    text.append(
                            "خالص ورود پول: ")
                            .append(
                                    formatNumber(
                                            item.netVolume))
                            .append("\n");

                    text.append(
                            "قدرت خرید: ")
                            .append(
                                    formatNumber(
                                            item.power))
                            .append("\n");

                    if (item.netValue > 0) {

                        text.append(
                                "ارزش تقریبی ورود: ")
                                .append(
                                        money(
                                                item.netValue))
                                .append("\n");
                    }

                    text.append(
                            "امتیاز فیلتر: ")
                            .append(
                                    formatNumber(
                                            item.score))
                            .append("\n");

                    count++;

                    if (count >= 20) {
                        break;
                    }
                }

                if (count == 0) {

                    text.append(
                            "در حال حاضر نمادی با معیارهای این فیلتر پیدا نشد.");
                }

                result =
                        text.toString();

            } catch (Exception e) {

                result =
                        "❌ تحلیل پول هوشمند انجام نشد.\n\n" +
                        "خطا: " +
                        e.getClass()
                                .getSimpleName() +
                        "\n\n" +
                        e.getMessage();
            }

            final String finalResult =
                    result;

            runOnUiThread(
                    () -> status.setText(
                            finalResult));

        }).start();
    }

    private void showPage(
            String pageTitle,
            String text) {

        ScrollView scroll = newScroll();

        LinearLayout page = newPage();

        TextView header =
                title(pageTitle);

        header.setBackgroundColor(
                Color.rgb(30, 100, 180));

        page.addView(header);

        TextView content =
                new TextView(this);

        content.setText(text);
        content.setTextSize(18);
        content.setPadding(
                15, 35, 15, 35);

        page.addView(content);

        Button back =
                menuButton("⬅ بازگشت");

        page.addView(back);

        back.setOnClickListener(
                v -> showMainPage());

        scroll.addView(page);
        setContentView(scroll);
    }

    private static class FlowItem {

        String symbol;

        double buyI;
        double sellI;

        double buyN;
        double sellN;

        double netVolume;
        double price;
        double netValue;
    }

    private static class SmartItem {

        String symbol;

        double buyI;
        double sellI;

        double netVolume;
        double netValue;

        double power;
        double score;
    }
}
