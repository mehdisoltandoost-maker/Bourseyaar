package com.bourseyaar.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends Activity {

    private LinearLayout layout;
    private Handler handler = new Handler();
    private Runnable refreshRunnable;

    private boolean smartPageOpen = false;
    private boolean loadingSmart = false;

    private final String API = "https://cdn.tsetmc.com/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        installTrustAllForTsetmc();

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

        p.setMargins(0, 8, 0, 8);
        b.setLayoutParams(p);

        return b;
    }

    private void showMainPage() {

        stopSmartRefresh();
        smartPageOpen = false;

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        TextView header = title("بورس‌یار");
        header.setBackgroundColor(Color.rgb(30, 100, 180));
        layout.addView(header);

        Button market = menuButton("📊 اطلاعات کلی بورس ایران");
        Button fundamental = menuButton("💰 بهترین نمادها از نظر بنیادی");
        Button technical = menuButton("📈 تحلیل تکنیکال");
        Button smartMoney = menuButton("💵 پول هوشمند");
        Button flow = menuButton("🔄 ورود و خروج پول");
        Button valuable = menuButton("⭐ سهم‌های ارزنده");
        Button portfolio = menuButton("📁 بررسی سهام‌های من");

        layout.addView(market);
        layout.addView(fundamental);
        layout.addView(technical);
        layout.addView(smartMoney);
        layout.addView(flow);
        layout.addView(valuable);
        layout.addView(portfolio);

        market.setOnClickListener(v -> showMarketPage());

        fundamental.setOnClickListener(v ->
                showPage(
                        "بهترین نمادها از نظر بنیادی",
                        "این بخش در مرحله بعد به اطلاعات بنیادی متصل می‌شود."
                ));

        technical.setOnClickListener(v ->
                showPage(
                        "تحلیل تکنیکال",
                        "RSI\n\nMACD\n\nمیانگین متحرک\n\nحمایت و مقاومت"
                ));

        smartMoney.setOnClickListener(v -> showSmartMoneyPage());

        flow.setOnClickListener(v -> showMoneyFlowPage());

        valuable.setOnClickListener(v ->
                showPage(
                        "سهم‌های ارزنده",
                        "ترکیب تحلیل بنیادی و تکنیکال در مرحله بعد اضافه می‌شود."
                ));

        portfolio.setOnClickListener(v ->
                showPage(
                        "سهام‌های من",
                        "بررسی سبد سهام در مرحله بعد اضافه می‌شود."
                ));

        setContentView(layout);
    }

    /*
     * ============================================================
     * اطلاعات کلی بورس
     * ============================================================
     */

    private void showMarketPage() {

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(15, 15, 15, 15);
        page.setBackgroundColor(Color.WHITE);

        TextView header = title("اطلاعات کلی بورس ایران");
        header.setBackgroundColor(Color.rgb(30, 100, 180));
        page.addView(header);

        TextView status = new TextView(this);

        status.setText(
                "⏳ در حال دریافت اطلاعات بازار...\n\n" +
                "لطفاً چند ثانیه صبر کنید."
        );

        status.setTextSize(18);
        status.setTextColor(Color.DKGRAY);
        status.setGravity(Gravity.RIGHT);
        status.setPadding(15, 25, 15, 25);

        ScrollView scroll = new ScrollView(this);

        scroll.setBackgroundColor(Color.WHITE);
        scroll.addView(status);

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1);

        page.addView(scroll, scrollParams);

        Button refresh = new Button(this);
        refresh.setText("🔄 دریافت اطلاعات");
        refresh.setTextSize(16);
        refresh.setAllCaps(false);

        page.addView(refresh);

        Button back = new Button(this);
        back.setText("⬅ بازگشت");
        back.setTextSize(16);
        back.setAllCaps(false);

        page.addView(back);

        refresh.setOnClickListener(v -> loadMarketData(status));

        back.setOnClickListener(v -> showMainPage());

        setContentView(page);

        loadMarketData(status);
    }

    private void loadMarketData(TextView status) {

        status.setText(
                "⏳ در حال اتصال به TSETMC...\n\n" +
                "دریافت شاخص‌های بازار..."
        );

        new Thread(() -> {

            String result;

            try {

                String json = httpGet(
                        API + "Index/GetIndexB1LastAll/SelectedIndexes/1"
                );

                if (json == null || json.trim().equals("")) {
                    throw new Exception("پاسخ TSETMC خالی است");
                }

                JSONObject root =
                        new JSONObject(json);

                JSONArray indexes =
                        findIndexArray(root);

                if (indexes == null || indexes.length() == 0) {

                    result =
                            "⚠ اطلاعات شاخص دریافت شد، " +
                            "اما ساختار پاسخ قابل شناسایی نبود.\n\n" +
                            "پاسخ سرور خالی نیست.\n\n" +
                            "لطفاً دکمه «دریافت اطلاعات» را دوباره بزنید.";
                } else {

                    StringBuilder text =
                            new StringBuilder();

                    text.append("📊 اطلاعات کلی بازار\n\n");

                    int shown = 0;

                    for (int i = 0;
                         i < indexes.length();
                         i++) {

                        try {

                            JSONObject item =
                                    indexes.getJSONObject(i);

                            String name =
                                    firstValue(
                                            item,
                                            "lVal30",
                                            "lVal18",
                                            "indexName",
                                            "name",
                                            "indexNameEn"
                                    );

                            String value =
                                    firstValue(
                                            item,
                                            "xVal",
                                            "xVal1",
                                            "indexValue",
                                            "value"
                                    );

                            String change =
                                    firstValue(
                                            item,
                                            "xVarIdx",
                                            "xVar",
                                            "change",
                                            "changeValue"
                                    );

                            String percent =
                                    firstValue(
                                            item,
                                            "xVarPrc",
                                            "xVarPrcIdx",
                                            "changePercent",
                                            "percent"
                                    );

                            if (name.equals("")) {
                                name = "شاخص";
                            }

                            if (value.equals("")) {
                                value = "-";
                            }

                            if (change.equals("")) {
                                change = "-";
                            }

                            if (percent.equals("")) {
                                percent = "-";
                            }

                            boolean important =
                                    name.contains("کل") ||
                                    name.contains("هم وزن") ||
                                    name.contains("هم‌وزن") ||
                                    name.contains("قیمت") ||
                                    name.contains("فرابورس") ||
                                    name.contains("صنعت");

                            if (important || shown < 10) {

                                text.append("📈 ")
                                        .append(name)
                                        .append("\n");

                                text.append("مقدار شاخص: ")
                                        .append(value)
                                        .append("\n");

                                text.append("تغییر: ")
                                        .append(change)
                                        .append("\n");

                                text.append("درصد تغییر: ")
                                        .append(percent)
                                        .append("\n\n");

                                shown++;
                            }

                        } catch (Exception ignored) {
                        }
                    }

                    if (shown == 0) {

                        text.append(
                                "داده دریافت شد، " +
                                "اما شاخص قابل نمایش پیدا نشد.\n\n"
                        );

                    } else {

                        text.append(
                                "────────────────\n\n"
                        );

                        text.append(
                                "⏱ آخرین بروزرسانی: "
                        );

                        text.append(currentTime());
                    }

                    result = text.toString();
                }

            } catch (Exception e) {

                String error =
                        e.getMessage();

                if (error == null ||
                        error.trim().equals("")) {
                    error = "خطای نامشخص";
                }

                result =
                        "❌ دریافت اطلاعات بورس انجام نشد.\n\n" +
                        "خطا:\n" +
                        error +
                        "\n\n" +
                        "اگر اینترنت وصل است، " +
                        "دکمه «دریافت اطلاعات» را دوباره بزنید.";
            }

            final String finalResult =
                    result;

            runOnUiThread(() -> {

                status.setText(finalResult);
                status.setTextColor(Color.DKGRAY);

            });

        }).start();
    }

    /*
     * پیدا کردن آرایه شاخص‌ها در پاسخ‌های مختلف TSETMC
     */

    private JSONArray findIndexArray(JSONObject root) {

        String[] keys = {
                "indexB1",
                "indexB1LastAll",
                "indexB1LastAllDto",
                "indexes",
                "index",
                "data"
        };

        for (String key : keys) {

            JSONArray arr =
                    root.optJSONArray(key);

            if (arr != null &&
                    arr.length() > 0) {

                return arr;
            }
        }

        return findArrayRecursively(root, 0);
    }

    private JSONArray findArrayRecursively(
            JSONObject object,
            int depth
    ) {

        if (depth > 3) {
            return null;
        }

        JSONArray possible =
                object.optJSONArray("indexB1");

        if (possible != null &&
                possible.length() > 0) {

            return possible;
        }

        java.util.Iterator<String> keys =
                object.keys();

        while (keys.hasNext()) {

            String key =
                    keys.next();

            try {

                Object value =
                        object.get(key);

                if (value instanceof JSONArray) {

                    JSONArray arr =
                            (JSONArray) value;

                    if (arr.length() > 0 &&
                            arr.get(0) instanceof JSONObject) {

                        JSONObject first =
                                arr.getJSONObject(0);

                        if (looksLikeIndex(first)) {
                            return arr;
                        }
                    }
                }

                if (value instanceof JSONObject) {

                    JSONArray found =
                            findArrayRecursively(
                                    (JSONObject) value,
                                    depth + 1
                            );

                    if (found != null) {
                        return found;
                    }
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private boolean looksLikeIndex(JSONObject o) {

        return o.has("lVal30") ||
                o.has("xVal") ||
                o.has("xVarIdx") ||
                o.has("indexValue") ||
                o.has("indexName");
    }

    private String firstValue(
            JSONObject object,
            String... keys
    ) {

        for (String key : keys) {

            String value =
                    object.optString(
                            key,
                            ""
                    ).trim();

            if (!value.equals("")) {
                return value;
            }
        }

        return "";
    }

    /*
     * ============================================================
     * پول هوشمند
     * ============================================================
     */

    private void showSmartMoneyPage() {

        smartPageOpen = true;

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(15, 15, 15, 15);
        page.setBackgroundColor(Color.WHITE);

        TextView header = title("💵 پول هوشمند");
        header.setBackgroundColor(Color.rgb(30, 100, 180));
        page.addView(header);

        TextView status = new TextView(this);

        status.setText(
                "⏳ در حال دریافت اطلاعات حقیقی و حقوقی...\n\n" +
                "ممکن است چند ثانیه زمان ببرد."
        );

        status.setTextSize(17);
        status.setTextColor(Color.DKGRAY);
        status.setPadding(10, 20, 10, 20);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(status);

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1);

        page.addView(scroll, scrollParams);

        Button refresh = new Button(this);
        refresh.setText("🔄 بروزرسانی");
        refresh.setAllCaps(false);
        page.addView(refresh);

        Button back = new Button(this);
        back.setText("⬅ بازگشت");
        back.setAllCaps(false);
        page.addView(back);

        refresh.setOnClickListener(v -> loadSmartMoney(status));

        back.setOnClickListener(v -> {
            stopSmartRefresh();
            smartPageOpen = false;
            showMainPage();
        });

        setContentView(page);

        loadSmartMoney(status);

        startSmartRefresh(status);
    }

    private void showMoneyFlowPage() {

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(15, 15, 15, 15);
        page.setBackgroundColor(Color.WHITE);

        TextView header = title("🔄 ورود و خروج پول");
        header.setBackgroundColor(Color.rgb(30, 100, 180));
        page.addView(header);

        TextView status = new TextView(this);
        status.setText("⏳ در حال دریافت جریان پول...");
        status.setTextSize(17);
        status.setTextColor(Color.DKGRAY);
        status.setPadding(10, 20, 10, 20);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(status);

        page.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1)
        );

        Button refresh = new Button(this);
        refresh.setText("🔄 بروزرسانی");
        refresh.setAllCaps(false);
        page.addView(refresh);

        Button back = new Button(this);
        back.setText("⬅ بازگشت");
        back.setAllCaps(false);
        page.addView(back);

        refresh.setOnClickListener(v -> loadMoneyFlow(status));
        back.setOnClickListener(v -> showMainPage());

        setContentView(page);

        loadMoneyFlow(status);
    }

    private void loadSmartMoney(TextView status) {

        if (loadingSmart) {
            return;
        }

        loadingSmart = true;

        status.setText(
                "⏳ دریافت اطلاعات پول هوشمند...\n\n" +
                "در حال بررسی حقیقی و حقوقی و مقایسه ۵ روز گذشته."
        );

        new Thread(() -> {

            String result;

            try {

                String marketJson = httpGet(
                        API +
                                "ClosingPrice/GetMarketWatch" +
                                "?market=0" +
                                "&paperTypes[0]=1" +
                                "&paperTypes[1]=2" +
                                "&paperTypes[2]=3" +
                                "&paperTypes[3]=4" +
                                "&paperTypes[4]=5" +
                                "&paperTypes[5]=6" +
                                "&paperTypes[6]=7" +
                                "&paperTypes[7]=8" +
                                "&paperTypes[8]=9" +
                                "&withBestLimits=false" +
                                "&hEven=0" +
                                "&RefID=0"
                );

                JSONObject marketRoot =
                        new JSONObject(marketJson);

                JSONArray market =
                        marketRoot.optJSONArray("marketwatch");

                if (market == null) {
                    market =
                            marketRoot.optJSONArray("marketWatch");
                }

                if (market == null) {
                    throw new Exception("داده بازار دریافت نشد");
                }

                Map<String, JSONObject> marketMap =
                        new HashMap<>();

                for (int i = 0;
                     i < market.length();
                     i++) {

                    JSONObject item =
                            market.getJSONObject(i);

                    String code =
                            item.optString(
                                    "insCode",
                                    item.optString(
                                            "ins_code",
                                            ""
                                    )
                            );

                    if (!code.equals("")) {
                        marketMap.put(code, item);
                    }
                }

                String clientJson =
                        httpGet(
                                API +
                                        "ClientType/GetClientTypeAll"
                        );

                JSONObject clientRoot =
                        new JSONObject(clientJson);

                JSONArray clients =
                        clientRoot.optJSONArray(
                                "clientTypeAllDto"
                        );

                if (clients == null) {
                    throw new Exception(
                            "اطلاعات حقیقی و حقوقی دریافت نشد"
                    );
                }

                ArrayList<StockData> stocks =
                        new ArrayList<>();

                double totalRealNet = 0;
                double totalLegalNet = 0;

                int realInCount = 0;
                int realOutCount = 0;

                for (int i = 0;
                     i < clients.length();
                     i++) {

                    JSONObject c =
                            clients.getJSONObject(i);

                    String code =
                            c.optString(
                                    "insCode",
                                    ""
                            );

                    if (code.equals("")) {
                        continue;
                    }

                    JSONObject m =
                            marketMap.get(code);

                    if (m == null) {
                        continue;
                    }

                    String symbol =
                            getSymbolName(m);

                    if (symbol.equals("")) {
                        continue;
                    }

                    double buyI =
                            num(c, "buy_I_Volume");

                    double sellI =
                            num(c, "sell_I_Volume");

                    double buyN =
                            num(c, "buy_N_Volume");

                    double sellN =
                            num(c, "sell_N_Volume");

                    double countBuyI =
                            num(c, "buy_CountI");

                    double countSellI =
                            num(c, "sell_CountI");

                    double countBuyN =
                            num(c, "buy_CountN");

                    double countSellN =
                            num(c, "sell_CountN");

                    double price =
                            num(m, "pl");

                    if (price <= 0) {
                        price =
                                num(m, "pDrCotVal");
                    }

                    double avgBuyI =
                            countBuyI > 0
                                    ? buyI / countBuyI
                                    : 0;

                    double avgSellI =
                            countSellI > 0
                                    ? sellI / countSellI
                                    : 0;

                    double avgBuyN =
                            countBuyN > 0
                                    ? buyN / countBuyN
                                    : 0;

                    double avgSellN =
                            countSellN > 0
                                    ? sellN / countSellN
                                    : 0;

                    double powerI =
                            avgSellI > 0
                                    ? avgBuyI / avgSellI
                                    : 0;

                    double powerN =
                            avgBuyN > 0 &&
                            avgSellN > 0
                                    ? avgBuyN / avgSellN
                                    : 0;

                    double netI =
                            buyI - sellI;

                    double netN =
                            buyN - sellN;

                    double netIValue =
                            netI * price;

                    double netNValue =
                            netN * price;

                    totalRealNet +=
                            netIValue;

                    totalLegalNet +=
                            netNValue;

                    if (netIValue > 0) {
                        realInCount++;
                    } else if (netIValue < 0) {
                        realOutCount++;
                    }

                    StockData s =
                            new StockData();

                    s.code = code;
                    s.symbol = symbol;

                    s.buyI = buyI;
                    s.sellI = sellI;

                    s.buyN = buyN;
                    s.sellN = sellN;

                    s.countBuyI =
                            countBuyI;

                    s.countSellI =
                            countSellI;

                    s.countBuyN =
                            countBuyN;

                    s.countSellN =
                            countSellN;

                    s.avgBuyI =
                            avgBuyI;

                    s.avgSellI =
                            avgSellI;

                    s.avgBuyN =
                            avgBuyN;

                    s.avgSellN =
                            avgSellN;

                    s.powerI =
                            powerI;

                    s.powerN =
                            powerN;

                    s.netI =
                            netI;

                    s.netN =
                            netN;

                    s.netIValue =
                            netIValue;

                    s.netNValue =
                            netNValue;

                    s.price =
                            price;

                    s.score =
                            calculateScore(s);

                    stocks.add(s);
                }

                Collections.sort(
                        stocks,
                        (a, b) ->
                                Double.compare(
                                        b.score,
                                        a.score
                                )
                );

                int historyCount =
                        Math.min(
                                12,
                                stocks.size()
                        );

                for (int i = 0;
                     i < historyCount;
                     i++) {

                    StockData s =
                            stocks.get(i);

                    try {

                        loadFiveDayHistory(s);

                        Thread.sleep(600);

                    } catch (Exception ignored) {
                    }
                }

                Collections.sort(
                        stocks,
                        (a, b) ->
                                Double.compare(
                                        b.score,
                                        a.score
                                )
                );

                StringBuilder out =
                        new StringBuilder();

                out.append(
                        "🧠 پول هوشمند\n\n"
                );

                out.append(
                        "📅 مقایسه امروز با ۵ روز معاملاتی گذشته\n\n"
                );

                out.append(
                        "📊 وضعیت کل بازار\n"
                );

                out.append(
                        "خالص خرید حقیقی: "
                ).append(
                        money(totalRealNet)
                ).append("\n");

                out.append(
                        "خالص خرید حقوقی: "
                ).append(
                        money(totalLegalNet)
                ).append("\n");

                out.append(
                        "تعداد نماد با ورود پول حقیقی: "
                ).append(
                        realInCount
                ).append("\n");

                out.append(
                        "تعداد نماد با خروج پول حقیقی: "
                ).append(
                        realOutCount
                ).append("\n\n");

                out.append(
                        "⚠ این ارقام «خالص خرید حقیقی/حقوقی» هستند؛ " +
                        "انتقال بین حقیقی و حقوقی را نشان می‌دهند و " +
                        "به‌تنهایی به معنی ورود پول جدید به کل بازار نیست.\n\n"
                );

                out.append(
                        "🔥 نمادهای دارای قدرت خرید و پول هوشمند\n\n"
                );

                int shown = 0;

                for (StockData s : stocks) {

                    if (shown >= 10) {
                        break;
                    }

                    if (s.netI <= 0 ||
                            s.powerI <= 0) {
                        continue;
                    }

                    out.append(
                            formatStock(s)
                    );

                    shown++;
                }

                out.append(
                        "\n🔴 نمادهای دارای فشار فروش / خروج پول\n\n"
                );

                ArrayList<StockData> selling =
                        new ArrayList<>(stocks);

                Collections.sort(
                        selling,
                        (a, b) ->
                                Double.compare(
                                        sellScore(b),
                                        sellScore(a)
                                )
                );

                shown = 0;

                for (StockData s : selling) {

                    if (shown >= 10) {
                        break;
                    }

                    if (s.netI >= 0) {
                        continue;
                    }

                    out.append(
                            formatSellingStock(s)
                    );

                    shown++;
                }

                if (shown == 0) {

                    out.append(
                            "مورد قابل توجهی برای خروج پول حقیقی پیدا نشد.\n"
                    );
                }

                out.append(
                        "\n\n⏱ بروزرسانی: "
                ).append(
                        currentTime()
                );

                result =
                        out.toString();

            } catch (Exception e) {

                result =
                        "❌ خطا در دریافت پول هوشمند\n\n" +
                        e.getMessage();
            }

            final String finalResult =
                    result;

            runOnUiThread(() -> {

                loadingSmart = false;

                if (smartPageOpen) {
                    status.setText(
                            finalResult
                    );
                }

            });

        }).start();
    }

    private void loadMoneyFlow(TextView status) {

        status.setText(
                "⏳ در حال دریافت ورود و خروج پول..."
        );

        new Thread(() -> {

            String result;

            try {

                String marketJson =
                        httpGet(
                                API +
                                        "ClosingPrice/GetMarketWatch" +
                                        "?market=0" +
                                        "&paperTypes[0]=1" +
                                        "&paperTypes[1]=2" +
                                        "&paperTypes[2]=3" +
                                        "&paperTypes[3]=4" +
                                        "&paperTypes[4]=5" +
                                        "&paperTypes[5]=6" +
                                        "&paperTypes[6]=7" +
                                        "&paperTypes[7]=8" +
                                        "&paperTypes[8]=9" +
                                        "&withBestLimits=false" +
                                        "&hEven=0" +
                                        "&RefID=0"
                        );

                JSONObject marketRoot =
                        new JSONObject(
                                marketJson
                        );

                JSONArray market =
                        marketRoot.optJSONArray(
                                "marketwatch"
                        );

                if (market == null) {
                    market =
                            marketRoot.optJSONArray(
                                    "marketWatch"
                            );
                }

                Map<String, JSONObject> map =
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
                                        ""
                                );

                        if (!code.equals("")) {
                            map.put(code, m);
                        }
                    }
                }

                String clientJson =
                        httpGet(
                                API +
                                        "ClientType/GetClientTypeAll"
                        );

                JSONObject clientRoot =
                        new JSONObject(
                                clientJson
                        );

                JSONArray clients =
                        clientRoot.optJSONArray(
                                "clientTypeAllDto"
                        );

                if (clients == null) {

                    throw new Exception(
                            "داده حقیقی/حقوقی دریافت نشد"
                    );
                }

                ArrayList<FlowData> flows =
                        new ArrayList<>();

                for (int i = 0;
                     i < clients.length();
                     i++) {

                    JSONObject c =
                            clients.getJSONObject(i);

                    String code =
                            c.optString(
                                    "insCode",
                                    ""
                            );

                    JSONObject m =
                            map.get(code);

                    if (m == null) {
                        continue;
                    }

                    String symbol =
                            getSymbolName(m);

                    if (symbol.equals("")) {
                        continue;
                    }

                    double buyI =
                            num(
                                    c,
                                    "buy_I_Volume"
                            );

                    double sellI =
                            num(
                                    c,
                                    "sell_I_Volume"
                            );

                    double price =
                            num(
                                    m,
                                    "pl"
                            );

                    if (price <= 0) {
                        price =
                                num(
                                        m,
                                        "pDrCotVal"
                                );
                    }

                    FlowData f =
                            new FlowData();

                    f.symbol =
                            symbol;

                    f.net =
                            (buyI - sellI) *
                                    price;

                    flows.add(f);
                }

                Collections.sort(
                        flows,
                        (a, b) ->
                                Double.compare(
                                        b.net,
                                        a.net
                                )
                );

                StringBuilder out =
                        new StringBuilder();

                out.append(
                        "🔄 ورود و خروج پول حقیقی\n\n"
                );

                out.append(
                        "📥 بیشترین ورود پول\n\n"
                );

                int count = 0;

                for (FlowData f : flows) {

                    if (count >= 15) {
                        break;
                    }

                    if (f.net <= 0) {
                        continue;
                    }

                    out.append("🟢 ")
                            .append(f.symbol)
                            .append("\n");

                    out.append(
                            "ورود خالص: "
                    ).append(
                            money(f.net)
                    ).append("\n\n");

                    count++;
                }

                out.append(
                        "📤 بیشترین خروج پول\n\n"
                );

                Collections.sort(
                        flows,
                        (a, b) ->
                                Double.compare(
                                        a.net,
                                        b.net
                                )
                );

                count = 0;

                for (FlowData f : flows) {

                    if (count >= 15) {
                        break;
                    }

                    if (f.net >= 0) {
                        continue;
                    }

                    out.append("🔴 ")
                            .append(f.symbol)
                            .append("\n");

                    out.append(
                            "خروج خالص: "
                    ).append(
                            money(
                                    Math.abs(
                                            f.net
                                    )
                            )
                    ).append("\n\n");

                    count++;
                }

                out.append(
                        "⏱ بروزرسانی: "
                ).append(
                        currentTime()
                );

                result =
                        out.toString();

            } catch (Exception e) {

                result =
                        "❌ دریافت ورود و خروج پول انجام نشد.\n\n" +
                        "خطا: " +
                        e.getMessage();
            }

            final String finalResult =
                    result;

            runOnUiThread(() ->
                    status.setText(
                            finalResult
                    )
            );

        }).start();
    }

    /*
     * ============================================================
     * تاریخچه ۵ روزه
     * ============================================================
     */

    private void loadFiveDayHistory(
            StockData s
    ) throws Exception {

        String json =
                httpGet(
                        API +
                                "ClientType/GetClientTypeHistory/" +
                                s.code
                );

        JSONObject root =
                new JSONObject(json);

        JSONArray arr =
                root.optJSONArray(
                        "clientType"
                );

        if (arr == null) {
            return;
        }

        ArrayList<JSONObject> days =
                new ArrayList<>();

        String today =
                currentDate();

        for (int i = 0;
             i < arr.length();
             i++) {

            JSONObject d =
                    arr.getJSONObject(i);

            String date =
                    d.optString(
                            "recDate",
                            ""
                    );

            if (!date.equals(today)) {
                days.add(d);
            }
        }

        Collections.sort(
                days,
                (a, b) ->
                        b.optString(
                                "recDate",
                                ""
                        ).compareTo(
                                a.optString(
                                        "recDate",
                                        ""
                                )
                        )
        );

        int n =
                Math.min(
                        5,
                        days.size()
                );

        if (n == 0) {
            return;
        }

        double powerSum = 0;
        double netValueSum = 0;

        for (int i = 0;
             i < n;
             i++) {

            JSONObject d =
                    days.get(i);

            double buyI =
                    num(
                            d,
                            "buy_I_Volume"
                    );

            double sellI =
                    num(
                            d,
                            "sell_I_Volume"
                    );

            double buyCount =
                    num(
                            d,
                            "buy_I_Count"
                    );

            if (buyCount <= 0) {

                buyCount =
                        num(
                                d,
                                "buy_CountI"
                        );
            }

            double sellCount =
                    num(
                            d,
                            "sell_I_Count"
                    );

            if (sellCount <= 0) {

                sellCount =
                        num(
                                d,
                                "sell_CountI"
                        );
            }

            double avgBuy =
                    buyCount > 0
                            ? buyI / buyCount
                            : 0;

            double avgSell =
                    sellCount > 0
                            ? sellI / sellCount
                            : 0;

            double power =
                    avgSell > 0
                            ? avgBuy / avgSell
                            : 0;

            double buyValue =
                    num(
                            d,
                            "buy_I_Value"
                    );

            double sellValue =
                    num(
                            d,
                            "sell_I_Value"
                    );

            double netValue =
                    buyValue - sellValue;

            powerSum += power;
            netValueSum += netValue;
        }

        s.avgFivePower =
                powerSum / n;

        s.avgFiveNet =
                netValueSum / n;

        s.historyDays =
                n;
    }

    /*
     * ============================================================
     * امتیاز پول هوشمند
     * ============================================================
     */

    private double calculateScore(
            StockData s
    ) {

        double score = 0;

        if (s.powerI >= 3) {

            score += 40;

        } else if (s.powerI >= 2) {

            score += 32;

        } else if (s.powerI >= 1.5) {

            score += 24;

        } else if (s.powerI >= 1.2) {

            score += 15;
        }

        double total =
                s.buyI + s.sellI;

        if (total > 0) {

            double ratio =
                    Math.abs(
                            s.netI
                    ) / total;

            score +=
                    Math.min(
                            25,
                            ratio * 100
                    );
        }

        if (s.netI > 0 &&
                s.netN < 0) {

            score += 20;
        }

        if (s.powerN > 0 &&
                s.powerN < 1) {

            score += 10;
        }

        if (s.avgFivePower > 0 &&
                s.powerI >
                        s.avgFivePower) {

            score += 15;
        }

        return score;
    }

    private double sellScore(
            StockData s
    ) {

        double score = 0;

        if (s.powerI > 0 &&
                s.powerI < 0.7) {

            score += 40;

        } else if (s.powerI < 0.85) {

            score += 30;
        }

        if (s.netI < 0) {
            score += 30;
        }

        if (s.netI < 0 &&
                s.netN > 0) {

            score += 20;
        }

        if (s.avgFivePower > 0 &&
                s.powerI <
                        s.avgFivePower) {

            score += 10;
        }

        return score;
    }

    /*
     * ============================================================
     * نمایش نمادهای پول هوشمند
     * ============================================================
     */

    private String formatStock(
            StockData s
    ) {

        StringBuilder x =
                new StringBuilder();

        x.append("🟢 ")
                .append(s.symbol)
                .append("\n\n");

        x.append(
                "خریداران حقیقی: "
        ).append(
                integer(
                        s.countBuyI
                )
        ).append(" نفر\n");

        x.append(
                "حجم خرید حقیقی: "
        ).append(
                integer(
                        s.buyI
                )
        ).append(" سهم\n");

        x.append(
                "میانگین خرید هر نفر: "
        ).append(
                integer(
                        s.avgBuyI
                )
        ).append(" سهم\n\n");

        x.append(
                "فروشندگان حقیقی: "
        ).append(
                integer(
                        s.countSellI
                )
        ).append(" نفر\n");

        x.append(
                "حجم فروش حقیقی: "
        ).append(
                integer(
                        s.sellI
                )
        ).append(" سهم\n");

        x.append(
                "میانگین فروش هر نفر: "
        ).append(
                integer(
                        s.avgSellI
                )
        ).append(" سهم\n\n");

        x.append(
                "⚡ قدرت خریدار: "
        ).append(
                decimal(
                        s.powerI
                )
        ).append(" برابر\n");

        x.append(
                "📥 ورود پول حقیقی: "
        ).append(
                money(
                        s.netIValue
                )
        ).append("\n");

        x.append(
                "🏢 خالص حقوقی: "
        ).append(
                money(
                        s.netNValue
                )
        ).append("\n");

        if (s.netI > 0 &&
                s.netN < 0) {

            x.append(
                    "🟢 حقیقی در حال خرید / حقوقی در حال فروش\n"
            );

        } else if (
                s.netI < 0 &&
                s.netN > 0) {

            x.append(
                    "🔴 حقیقی در حال فروش / حقوقی در حال خرید\n"
            );
        }

        if (s.historyDays > 0) {

            x.append(
                    "\n📅 مقایسه ۵ روز گذشته:\n"
            );

            x.append(
                    "قدرت امروز: "
            ).append(
                    decimal(
                            s.powerI
                    )
            ).append("\n");

            x.append(
                    "میانگین قدرت ۵ روز: "
            ).append(
                    decimal(
                            s.avgFivePower
                    )
            ).append("\n");

            if (s.powerI >
                    s.avgFivePower) {

                x.append(
                        "📈 قدرت خرید نسبت به ۵ روز قبل بیشتر شده\n"
                );

            } else {

                x.append(
                        "📉 قدرت خرید نسبت به ۵ روز قبل کمتر شده\n"
                );
            }
        }

        x.append(
                "امتیاز پول هوشمند: "
        ).append(
                decimal(
                        s.score
                )
        ).append("\n\n");

        return x.toString();
    }

    private String formatSellingStock(
            StockData s
    ) {

        StringBuilder x =
                new StringBuilder();

        x.append("🔴 ")
                .append(s.symbol)
                .append("\n\n");

        x.append(
                "خریدار حقیقی: "
        ).append(
                integer(
                        s.countBuyI
                )
        ).append(" نفر\n");

        x.append(
                "فروشنده حقیقی: "
        ).append(
                integer(
                        s.countSellI
                )
        ).append(" نفر\n");

        x.append(
                "میانگین خرید: "
        ).append(
                integer(
                        s.avgBuyI
                )
        ).append(" سهم\n");

        x.append(
                "میانگین فروش: "
        ).append(
                integer(
                        s.avgSellI
                )
        ).append(" سهم\n");

        x.append(
                "⚡ قدرت خریدار: "
        ).append(
                decimal(
                        s.powerI
                )
        ).append(" برابر\n");

        x.append(
                "📤 خروج پول حقیقی: "
        ).append(
                money(
                        Math.abs(
                                s.netIValue
                        )
                )
        ).append("\n\n");

        return x.toString();
    }

    /*
     * ============================================================
     * ابزارها
     * ============================================================
     */

    private String getSymbolName(
            JSONObject o
    ) {

        String[] keys = {
                "lVal18AFC",
                "lVal18",
                "symbol",
                "symbolName"
        };

        for (String key : keys) {

            String value =
                    o.optString(
                            key,
                            ""
                    ).trim();

            if (!value.equals("") &&
                    !isOnlyNumber(value)) {

                return value;
            }
        }

        return "";
    }

    private boolean isOnlyNumber(
            String s
    ) {

        if (s == null ||
                s.trim().equals("")) {

            return true;
        }

        return s.matches(
                "\\d+"
        );
    }

    private double num(
            JSONObject o,
            String key
    ) {

        try {

            Object value =
                    o.opt(key);

            if (value == null ||
                    value == JSONObject.NULL) {

                return 0;
            }

            if (value instanceof Number) {

                return ((Number) value)
                        .doubleValue();
            }

            String s =
                    String.valueOf(value)
                            .replace(",", "")
                            .trim();

            if (s.equals("")) {
                return 0;
            }

            return Double.parseDouble(s);

        } catch (Exception e) {

            return 0;
        }
    }

    private String integer(
            double value
    ) {

        NumberFormat nf =
                NumberFormat.getIntegerInstance(
                        Locale.US
                );

        return nf.format(
                Math.round(value)
        );
    }

    private String decimal(
            double value
    ) {

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }

    private String money(
            double value
    ) {

        boolean negative =
                value < 0;

        value =
                Math.abs(value);

        String unit;

        if (value >=
                1_000_000_000_000.0) {

            unit = "همت";

            value /=
                    1_000_000_000_000.0;

        } else if (value >=
                1_000_000_000.0) {

            unit = "میلیارد ریال";

            value /=
                    1_000_000_000.0;

        } else if (value >=
                1_000_000.0) {

            unit = "میلیون ریال";

            value /=
                    1_000_000.0;

        } else {

            unit = "ریال";
        }

        String result;

        if (unit.equals("ریال")) {

            result =
                    integer(value) +
                            " " +
                            unit;

        } else {

            result =
                    String.format(
                            Locale.US,
                            "%.2f %s",
                            value,
                            unit
                    );
        }

        if (negative) {
            result =
                    "-" + result;
        }

        return result;
    }

    private String currentDate() {

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat(
                        "yyyyMMdd",
                        Locale.US
                );

        sdf.setTimeZone(
                TimeZone.getTimeZone(
                        "Asia/Tehran"
                )
        );

        return sdf.format(
                new java.util.Date()
        );
    }

    private String currentTime() {

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.US
                );

        sdf.setTimeZone(
                TimeZone.getTimeZone(
                        "Asia/Tehran"
                )
        );

        return sdf.format(
                new java.util.Date()
        );
    }

    private boolean isMarketOpen() {

        java.util.Calendar cal =
                java.util.Calendar.getInstance(
                        TimeZone.getTimeZone(
                                "Asia/Tehran"
                        )
                );

        int day =
                cal.get(
                        java.util.Calendar.DAY_OF_WEEK
                );

        int hour =
                cal.get(
                        java.util.Calendar.HOUR_OF_DAY
                );

        int minute =
                cal.get(
                        java.util.Calendar.MINUTE
                );

        boolean tradingDay =
                day == java.util.Calendar.SATURDAY ||
                day == java.util.Calendar.SUNDAY ||
                day == java.util.Calendar.MONDAY ||
                day == java.util.Calendar.TUESDAY ||
                day == java.util.Calendar.WEDNESDAY;

        int now =
                hour * 60 + minute;

        return tradingDay &&
                now >= 9 * 60 &&
                now <= 12 * 60 + 30;
    }

    private void startSmartRefresh(
            TextView status
    ) {

        stopSmartRefresh();

        refreshRunnable =
                new Runnable() {

                    @Override
                    public void run() {

                        if (!smartPageOpen) {
                            return;
                        }

                        if (isMarketOpen()) {

                            loadSmartMoney(
                                    status
                            );
                        }

                        handler.postDelayed(
                                this,
                                30000
                        );
                    }
                };

        handler.postDelayed(
                refreshRunnable,
                30000
        );
    }

    private void stopSmartRefresh() {

        if (refreshRunnable != null) {

            handler.removeCallbacks(
                    refreshRunnable
            );

            refreshRunnable = null;
        }
    }

    /*
     * ============================================================
     * اتصال اینترنتی
     * ============================================================
     */

    private String httpGet(
            String urlString
    ) throws Exception {

        URL url =
                new URL(
                        urlString
                );

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(
                "GET"
        );

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                20000
        );

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        int code =
                connection.getResponseCode();

        if (code < 200 ||
                code >= 300) {

            throw new Exception(
                    "HTTP " + code
            );
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream(),
                                "UTF-8"
                        )
                );

        StringBuilder builder =
                new StringBuilder();

        String line;

        while (
                (line =
                        reader.readLine())
                        != null
        ) {

            builder.append(
                    line
            );
        }

        reader.close();

        connection.disconnect();

        return builder.toString();
    }

    /*
     * ============================================================
     * رفع مشکل SSL برای TSETMC
     * ============================================================
     */

    private void installTrustAllForTsetmc() {

        try {

            TrustManager[] trustAll =
                    new TrustManager[]{

                            new X509TrustManager() {

                                public java.security.cert.X509Certificate[]
                                getAcceptedIssuers() {

                                    return new java.security.cert.X509Certificate[0];
                                }

                                public void checkClientTrusted(
                                        java.security.cert.X509Certificate[] chain,
                                        String authType) {
                                }

                                public void checkServerTrusted(
                                        java.security.cert.X509Certificate[] chain,
                                        String authType) {
                                }
                            }
                    };

            SSLContext context =
                    SSLContext.getInstance(
                            "TLS"
                    );

            context.init(
                    null,
                    trustAll,
                    new java.security.SecureRandom()
            );

            SSLSocketFactory factory =
                    context.getSocketFactory();

            HttpsURLConnection.setDefaultSSLSocketFactory(
                    factory
            );

            HostnameVerifier verifier =
                    (hostname, session) ->
                            true;

            HttpsURLConnection.setDefaultHostnameVerifier(
                    verifier
            );

        } catch (Exception ignored) {
        }
    }

    /*
     * ============================================================
     * صفحات ساده
     * ============================================================
     */

    private void showPage(
            String pageTitle,
            String text
    ) {

        LinearLayout page =
                new LinearLayout(this);

        page.setOrientation(
                LinearLayout.VERTICAL
        );

        page.setPadding(
                20,
                20,
                20,
                20
        );

        page.setBackgroundColor(
                Color.WHITE
        );

        TextView header =
                title(pageTitle);

        header.setBackgroundColor(
                Color.rgb(
                        30,
                        100,
                        180
                )
        );

        page.addView(header);

        TextView content =
                new TextView(this);

        content.setText(text);
        content.setTextSize(18);
        content.setTextColor(
                Color.DKGRAY
        );

        content.setPadding(
                15,
                35,
                15,
                35
        );

        page.addView(content);

        Button back =
                new Button(this);

        back.setText(
                "⬅ بازگشت"
        );

        back.setAllCaps(false);

        page.addView(back);

        back.setOnClickListener(
                v -> showMainPage()
        );

        setContentView(page);
    }

    @Override
    protected void onDestroy() {

        stopSmartRefresh();

        super.onDestroy();
    }

    /*
     * ============================================================
     * کلاس اطلاعات سهم
     * ============================================================
     */

    private static class StockData {

        String code = "";
        String symbol = "";

        double buyI;
        double sellI;

        double buyN;
        double sellN;

        double countBuyI;
        double countSellI;

        double countBuyN;
        double countSellN;

        double avgBuyI;
        double avgSellI;

        double avgBuyN;
        double avgSellN;

        double powerI;
        double powerN;

        double netI;
        double netN;

        double netIValue;
        double netNValue;

        double price;

        double score;

        double avgFivePower;
        double avgFiveNet;

        int historyDays;
    }

    /*
     * ============================================================
     * کلاس ورود و خروج پول
     * ============================================================
     */

    private static class FlowData {

        String symbol = "";

        double net;
    }
}
