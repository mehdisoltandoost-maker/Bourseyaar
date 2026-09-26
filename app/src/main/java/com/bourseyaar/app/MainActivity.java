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
import org.json.JSONTokener;

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
    private TextView status;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler handler =
            new Handler();

    private final List<MarketItem> marketItems =
            new ArrayList<>();

    private static final String BASE_URL =
            "https://cdn.tsetmc.com/api/";

    /*
     * نسخه جدید دریافت اطلاعات بازار
     */
    private static final String MARKET_URL =
            BASE_URL +
            "ClosingPrice/GetMarketMap" +
            "?market=0" +
            "&size=9999" +
            "&sector=0" +
            "&typeSelected=1" +
            "&hEven=0";

    private static final String MONEY_URL =
            BASE_URL +
            "ClientType/GetClientTypeAll";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupTsetmcSsl();

        buildMainMenu();

        loadMarketData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            executor.shutdownNow();
        } catch (Exception ignored) {
        }
    }

    /*
     * ============================================================
     * صفحه اصلی
     * ============================================================
     */

    private void buildMainMenu() {

        root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.WHITE
        );

        root.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        TextView title =
                new TextView(this);

        title.setText("بورس‌یار");

        title.setTextSize(25);

        title.setTextColor(
                Color.rgb(20, 70, 120)
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                10,
                25,
                10,
                15
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout menu =
                new LinearLayout(this);

        menu.setOrientation(
                LinearLayout.VERTICAL
        );

        menu.setPadding(
                12,
                5,
                12,
                5
        );

        menu.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        addButton(
                menu,
                "اطلاعات کلی بازار",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMarketOverview();
                    }
                }
        );

        addButton(
                menu,
                "پول هوشمند",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSmartMoney();
                    }
                }
        );

        addButton(
                menu,
                "ورود و خروج پول",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMoneyFlow();
                    }
                }
        );

        addButton(
                menu,
                "تحلیل بنیادی",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFundamental();
                    }
                }
        );

        addButton(
                menu,
                "تحلیل تکنیکال",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTechnical();
                    }
                }
        );

        addButton(
                menu,
                "بررسی نمادها",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSymbols();
                    }
                }
        );

        addButton(
                menu,
                "پیشنهادهای معاملاتی",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSuggestions();
                    }
                }
        );

        addButton(
                menu,
                "به‌روزرسانی اطلاعات",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMarketData();
                    }
                }
        );

        root.addView(
                menu,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        TextView info =
                new TextView(this);

        info.setText(
                "دستیار تحلیل بازار سرمایه ایران"
        );

        info.setTextSize(16);

        info.setTextColor(
                Color.DKGRAY
        );

        info.setGravity(
                Gravity.CENTER
        );

        info.setPadding(
                10,
                25,
                10,
                10
        );

        root.addView(
                info,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        status =
                new TextView(this);

        status.setText(
                "در حال اتصال به TSETMC..."
        );

        status.setTextSize(14);

        status.setTextColor(
                Color.DKGRAY
        );

        status.setGravity(
                Gravity.CENTER
        );

        status.setPadding(
                10,
                10,
                10,
                10
        );

        root.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(root);
    }

    /*
     * ============================================================
     * ساخت صفحه
     * ============================================================
     */

    private void openPage(
            String pageTitle) {

        root.removeAllViews();

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.WHITE
        );

        root.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        TextView title =
                new TextView(this);

        title.setText(pageTitle);

        title.setTextSize(25);

        title.setTextColor(
                Color.rgb(20, 70, 120)
        );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                10,
                20,
                10,
                10
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        Button back =
                new Button(this);

        back.setText(
                "← بازگشت به منوی اصلی"
        );

        back.setTextSize(16);

        back.setAllCaps(false);

        back.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buildMainMenu();
                    }
                }
        );

        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        backParams.setMargins(
                15,
                0,
                15,
                5
        );

        root.addView(
                back,
                backParams
        );

        status =
                new TextView(this);

        status.setText("");

        status.setTextSize(14);

        status.setTextColor(
                Color.DKGRAY
        );

        status.setGravity(
                Gravity.CENTER
        );

        status.setPadding(
                10,
                3,
                10,
                3
        );

        root.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
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
                15,
                10,
                15,
                30
        );

        content.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
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

        setContentView(root);
    }

    private void addButton(
            LinearLayout parent,
            String text,
            View.OnClickListener listener) {

        Button button =
                new Button(this);

        button.setText(text);

        button.setTextSize(16);

        button.setAllCaps(false);

        button.setOnClickListener(
                listener
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                4,
                0,
                4
        );

        parent.addView(
                button,
                params
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

        TextView tv =
                new TextView(this);

        tv.setText(text);

        tv.setTextSize(size);

        tv.setTextColor(color);

        tv.setGravity(
                Gravity.RIGHT
        );

        tv.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        tv.setPadding(
                5,
                8,
                5,
                8
        );

        if (bold) {

            tv.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        content.addView(tv);
    }

    private void setStatus(
            final String text) {

        handler.post(
                new Runnable() {
                    @Override
                    public void run() {

                        if (status != null) {
                            status.setText(text);
                        }
                    }
                }
        );    /*
     * ============================================================
     * دریافت اطلاعات بازار
     * ============================================================
     */

    private void loadMarketData() {

        setStatus(
                "در حال دریافت اطلاعات بازار از TSETMC..."
        );

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String response =
                                    httpGet(MARKET_URL);

                            if (response == null ||
                                    response.trim().length() == 0) {

                                throw new Exception(
                                        "پاسخ خالی از TSETMC دریافت شد."
                                );
                            }

                            parseMarketMap(
                                    response
                            );

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            if (marketItems.size() > 0) {

                                                setStatus(
                                                        "اتصال موفق؛ " +
                                                        marketItems.size() +
                                                        " نماد دریافت شد."
                                                );

                                            } else {

                                                setStatus(
                                                        "پاسخ دریافت شد ولی نمادی پیدا نشد."
                                                );
                                            }
                                        }
                                    }
                            );

                        } catch (
                                final Exception e) {

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            setStatus(
                                                    "خطا: " +
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

    /*
     * ============================================================
     * تجزیه MarketMap
     * ============================================================
     */

    private void parseMarketMap(
            String response)
            throws Exception {

        marketItems.clear();

        Object parsed =
                new JSONTokener(
                        response
                ).nextValue();

        JSONArray array = null;

        if (parsed instanceof JSONArray) {

            array =
                    (JSONArray) parsed;

        } else if (parsed instanceof JSONObject) {

            JSONObject rootObject =
                    (JSONObject) parsed;

            String[] possibleKeys = {

                    "marketMap",
                    "marketmap",
                    "marketMapDto",
                    "data",
                    "items",
                    "marketwatch",
                    "marketWatch"
            };

            for (String key :
                    possibleKeys) {

                Object value =
                        rootObject.opt(key);

                if (value instanceof JSONArray) {

                    array =
                            (JSONArray) value;

                    break;
                }
            }
        }

        if (array == null) {

            throw new Exception(
                    "ساختار پاسخ بازار قابل شناسایی نیست."
            );
        }

        for (int i = 0;
             i < array.length();
             i++) {

            JSONObject obj =
                    array.optJSONObject(i);

            if (obj == null) {
                continue;
            }

            MarketItem item =
                    new MarketItem();

            /*
             * کد نماد
             */
            item.insCode =
                    firstNonEmpty(

                            getString(
                                    obj,
                                    "insCode",
                                    ""
                            ),

                            getString(
                                    obj,
                                    "instrumentId",
                                    ""
                            )
                    );

            /*
             * نام نماد
             */
            item.symbol =
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
                            ),

                            getString(
                                    obj,
                                    "symbol",
                                    ""
                            ),

                            getString(
                                    obj,
                                    "symbolName",
                                    ""
                            ),

                            getString(
                                    obj,
                                    "instrumentName",
                                    ""
                            )
                    );

            /*
             * نام شرکت
             */
            item.name =
                    firstNonEmpty(

                            getString(
                                    obj,
                                    "lVal30",
                                    ""
                            ),

                            getString(
                                    obj,
                                    "name",
                                    ""
                            ),

                            getString(
                                    obj,
                                    "title",
                                    ""
                            ),

                            getString(
                                    obj,
                                    "companyNamePersian",
                                    ""
                            )
                    );

            /*
             * قیمت اولین معامله
             */
            item.first =
                    getDouble(
                            obj,
                            "pf",
                            getDouble(
                                    obj,
                                    "priceFirst",
                                    0
                            )
                    );

            /*
             * قیمت پایانی
             */
            item.close =
                    getDouble(
                            obj,
                            "pClosing",
                            getDouble(
                                    obj,
                                    "pc",
                                    getDouble(
                                            obj,
                                            "close",
                                            getDouble(
                                                    obj,
                                                    "closingPrice",
                                                    0
                                            )
                                    )
                            )
                    );

            /*
             * آخرین قیمت
             */
            item.last =
                    getDouble(
                            obj,
                            "pDrCotVal",
                            getDouble(
                                    obj,
                                    "pl",
                                    getDouble(
                                            obj,
                                            "last",
                                            getDouble(
                                                    obj,
                                                    "lastPrice",
                                                    0
                                            )
                                    )
                            )
                    );

            /*
             * قیمت روز قبل
             */
            item.yesterday =
                    getDouble(
                            obj,
                            "priceYesterday",
                            getDouble(
                                    obj,
                                    "py",
                                    getDouble(
                                            obj,
                                            "yesterday",
                                            getDouble(
                                                    obj,
                                                    "yesterdayPrice",
                                                    0
                                            )
                                    )
                            )
                    );

            /*
             * کمترین قیمت
             */
            item.min =
                    getDouble(
                            obj,
                            "priceMin",
                            getDouble(
                                    obj,
                                    "pmin",
                                    0
                            )
                    );

            /*
             * بیشترین قیمت
             */
            item.max =
                    getDouble(
                            obj,
                            "priceMax",
                            getDouble(
                                    obj,
                                    "pmax",
                                    0
                            )
                    );

            /*
             * حجم
             */
            item.volume =
                    getDouble(
                            obj,
                            "qTotTran5J",
                            getDouble(
                                    obj,
                                    "tvol",
                                    getDouble(
                                            obj,
                                            "volume",
                                            getDouble(
                                                    obj,
                                                    "tradeVolume",
                                                    0
                                            )
                                    )
                            )
                    );

            /*
             * ارزش
             */
            item.value =
                    getDouble(
                            obj,
                            "qTotCap",
                            getDouble(
                                    obj,
                                    "tval",
                                    getDouble(
                                            obj,
                                            "value",
                                            getDouble(
                                                    obj,
                                                    "tradeValue",
                                                    0
                                            )
                                    )
                            )
                    );

            /*
             * تعداد معاملات
             */
            item.trades =
                    getDouble(
                            obj,
                            "zTotTran",
                            getDouble(
                                    obj,
                                    "tno",
                                    getDouble(
                                            obj,
                                            "trades",
                                            getDouble(
                                                    obj,
                                                    "tradeCount",
                                                    0
                                            )
                                    )
                            )
                    );

            /*
             * درصد تغییر
             */
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

            /*
             * اگر درصد وجود نداشت،
             * خودمان محاسبه می‌کنیم.
             */
            if (item.percent == 0 &&
                    item.yesterday != 0 &&
                    item.close != 0) {

                item.percent =
                        (
                                (
                                        item.close -
                                        item.yesterday
                                )
                                /
                                item.yesterday
                        )
                        * 100.0;
            }

            /*
             * فقط رکوردهای دارای نماد را قبول می‌کنیم.
             */
            if (item.symbol.length() > 0) {

                marketItems.add(item);
            }
        }
    }

    /*
     * ============================================================
     * اطلاعات کلی بازار
     * ============================================================
     */

    private void showMarketOverview() {

        openPage(
                "اطلاعات کلی بازار"
        );

        clearContent();

        addText(
                "اطلاعات کلی بازار",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        if (marketItems.size() == 0) {

            addText(
                    "اطلاعات بازار هنوز دریافت نشده است.",
                    16,
                    Color.DKGRAY,
                    false
            );

            Button refresh =
                    new Button(this);

            refresh.setText(
                    "دریافت مجدد اطلاعات"
            );

            refresh.setAllCaps(false);

            refresh.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            loadMarketData();
                        }
                    }
            );

            content.addView(refresh);

            return;
        }

        int positive = 0;
        int negative = 0;
        int unchanged = 0;

        double totalVolume = 0;
        double totalValue = 0;
        double totalTrades = 0;

        for (MarketItem item :
                marketItems) {

            totalVolume +=
                    item.volume;

            totalValue +=
                    item.value;

            totalTrades +=
                    item.trades;

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
                "مثبت: " +
                positive +
                "    منفی: " +
                negative +
                "    بدون تغییر: " +
                unchanged,
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
                "──────────────",
                15,
                Color.GRAY,
                false
        );

        addText(
                "نمادهای بازار",
                19,
                Color.rgb(20, 70, 120),
                true
        );

        /*
         * مرتب‌سازی بر اساس درصد تغییر
         */
        List<MarketItem> sorted =
                new ArrayList<>(
                        marketItems
                );

        Collections.sort(
                sorted,
                new Comparator<MarketItem>() {

                    @Override
                    public int compare(
                            MarketItem a,
                            MarketItem b) {

                        return Double.compare(
                                Math.abs(
                                        b.percent
                                ),
                                Math.abs(
                                        a.percent
                                )
                        );
                    }
                }
        );

        int limit =
                Math.min(
                        50,
                        sorted.size()
                );

        for (int i = 0;
             i < limit;
             i++) {

            addMarketRow(
                    sorted.get(i)
            );
        }
    }

    private void addMarketRow(
            MarketItem item) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.RIGHT
        );

        box.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        box.setPadding(
                8,
                10,
                8,
                10
        );

        String symbol =
                item.symbol.length() > 0
                        ? item.symbol
                        : "بدون نماد";

        TextView first =
                new TextView(this);

        first.setText(
                symbol +
                "    " +
                formatPercent(
                        item.percent
                )
        );

        first.setTextSize(17);

        first.setGravity(
                Gravity.RIGHT
        );

        first.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        if (item.percent > 0) {

            first.setTextColor(
                    Color.rgb(
                            0,
                            130,
                            60
                    )
            );

        } else if (item.percent < 0) {

            first.setTextColor(
                    Color.rgb(
                            190,
                            30,
                            30
                    )
            );

        } else {

            first.setTextColor(
                    Color.DKGRAY
            );
        }

        box.addView(first);

        TextView second =
                new TextView(this);

        second.setText(
                "آخرین: " +
                formatNumber(
                        item.last
                ) +
                "    پایانی: " +
                formatNumber(
                        item.close
                ) +
                "    روز قبل: " +
                formatNumber(
                        item.yesterday
                )
        );

        second.setTextSize(14);

        second.setGravity(
                Gravity.RIGHT
        );

        second.setTextColor(
                Color.DKGRAY
        );

        box.addView(second);

        TextView third =
                new TextView(this);

        third.setText(
                "حجم: " +
                formatNumber(
                        item.volume
                ) +
                "    ارزش: " +
                formatNumber(
                        item.value
                ) +
                "    معاملات: " +
                formatNumber(
                        item.trades
                )
        );

        third.setTextSize(13);

        third.setGravity(
                Gravity.RIGHT
        );

        third.setTextColor(
                Color.GRAY
        );

        box.addView(third);

        content.addView(box);
    }
    }    /*
     * ============================================================
     * پول هوشمند
     * ============================================================
     */

    private void showSmartMoney() {

        openPage(
                "پول هوشمند"
        );

        clearContent();

        addText(
                "پول هوشمند",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "بر اساس خالص حجم خرید و فروش حقیقی",
                14,
                Color.GRAY,
                false
        );

        addText(
                "در حال دریافت اطلاعات...",
                15,
                Color.DKGRAY,
                false
        );

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            /*
                             * ابتدا باید اطلاعات بازار موجود باشد
                             * تا InsCode به نام نماد تبدیل شود.
                             */
                            if (marketItems.size() == 0) {

                                String marketResponse =
                                        httpGet(
                                                MARKET_URL
                                        );

                                parseMarketMap(
                                        marketResponse
                                );
                            }

                            String response =
                                    httpGet(
                                            MONEY_URL
                                    );

                            final List<MoneyItem> list =
                                    parseMoney(
                                            response
                                    );

                            /*
                             * مرتب‌سازی بر اساس خالص حجم حقیقی
                             */
                            Collections.sort(
                                    list,
                                    new Comparator<MoneyItem>() {

                                        @Override
                                        public int compare(
                                                MoneyItem a,
                                                MoneyItem b) {

                                            return Double.compare(
                                                    b.netVolume,
                                                    a.netVolume
                                            );
                                        }
                                    }
                            );

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            clearContent();

                                            addText(
                                                    "پول هوشمند",
                                                    22,
                                                    Color.rgb(
                                                            20,
                                                            70,
                                                            120
                                                    ),
                                                    true
                                            );

                                            addText(
                                                    "مرتب‌شده بر اساس خالص حجم حقیقی",
                                                    14,
                                                    Color.GRAY,
                                                    false
                                            );

                                            if (list.size() == 0) {

                                                addText(
                                                        "اطلاعات پول حقیقی دریافت نشد.",
                                                        16,
                                                        Color.DKGRAY,
                                                        false
                                                );

                                                return;
                                            }

                                            int limit =
                                                    Math.min(
                                                            30,
                                                            list.size()
                                                    );

                                            for (int i = 0;
                                                 i < limit;
                                                 i++) {

                                                addMoneyRow(
                                                        list.get(i)
                                                );
                                            }
                                        }
                                    }
                            );

                        } catch (
                                final Exception e) {

                            handler.post(
                                    new Runnable() {
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
                                    }
                            );
                        }
                    }
                }
        );
    }

    /*
     * ============================================================
     * تجزیه ClientTypeAll
     * ============================================================
     */

    private List<MoneyItem> parseMoney(
            String response)
            throws Exception {

        List<MoneyItem> result =
                new ArrayList<>();

        if (response == null ||
                response.trim().length() == 0) {

            return result;
        }

        JSONObject rootObject =
                new JSONObject(response);

        Object rootValue =
                rootObject.opt(
                        "clientTypeAllDto"
                );

        JSONArray array = null;

        if (rootValue instanceof JSONArray) {

            array =
                    (JSONArray) rootValue;
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
                    firstNonEmpty(

                            getString(
                                    obj,
                                    "insCode",
                                    ""
                            ),

                            getString(
                                    obj,
                                    "instrumentId",
                                    ""
                            )
                    );

            /*
             * خرید حقیقی
             */
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

            /*
             * فروش حقیقی
             */
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

            /*
             * ارزش خرید حقیقی
             */
            item.buyValue =
                    getDouble(
                            obj,
                            "buy_I_Value",
                            getDouble(
                                    obj,
                                    "buyIValue",
                                    0
                            )
                    );

            /*
             * ارزش فروش حقیقی
             */
            item.sellValue =
                    getDouble(
                            obj,
                            "sell_I_Value",
                            getDouble(
                                    obj,
                                    "sellIValue",
                                    0
                            )
                    );

            item.netVolume =
                    item.buyIndividual -
                    item.sellIndividual;

            item.netValue =
                    item.buyValue -
                    item.sellValue;

            /*
             * پیدا کردن نام نماد
             */
            MarketItem market =
                    findMarketItem(
                            item.insCode
                    );

            if (market != null) {

                item.symbol =
                        market.symbol;
            }

            /*
             * فقط اطلاعات واقعی را اضافه می‌کنیم.
             */
            if (item.symbol.length() > 0 ||
                    Math.abs(
                            item.netVolume
                    ) > 0) {

                result.add(item);
            }
        }

        return result;
    }

    /*
     * ============================================================
     * تطبیق InsCode با نماد
     * ============================================================
     */

    private MarketItem findMarketItem(
            String insCode) {

        if (insCode == null ||
                insCode.length() == 0) {

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

    /*
     * ============================================================
     * نمایش هر ردیف پول هوشمند
     * ============================================================
     */

    private void addMoneyRow(
            MoneyItem item) {

        String symbol =
                item.symbol.length() > 0
                        ? item.symbol
                        : "نماد ناشناس";

        TextView tv =
                new TextView(this);

        StringBuilder text =
                new StringBuilder();

        text.append(
                symbol
        );

        text.append(
                "\nخالص حجم حقیقی: "
        );

        text.append(
                formatNumber(
                        item.netVolume
                )
        );

        text.append(
                "\nخرید حقیقی: "
        );

        text.append(
                formatNumber(
                        item.buyIndividual
                )
        );

        text.append(
                "    فروش حقیقی: "
        );

        text.append(
                formatNumber(
                        item.sellIndividual
                )
        );

        /*
         * اگر ارزش حقیقی موجود باشد
         * آن را هم نمایش می‌دهیم.
         */
        if (item.buyValue != 0 ||
                item.sellValue != 0) {

            text.append(
                    "\nخالص ارزش حقیقی: "
            );

            text.append(
                    formatNumber(
                            item.netValue
                    )
            );
        }

        tv.setText(
                text.toString()
        );

        tv.setTextSize(15);

        tv.setGravity(
                Gravity.RIGHT
        );

        tv.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        tv.setPadding(
                8,
                10,
                8,
                10
        );

        if (item.netVolume > 0) {

            tv.setTextColor(
                    Color.rgb(
                            0,
                            120,
                            60
                    )
            );

            tv.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

        } else if (item.netVolume < 0) {

            tv.setTextColor(
                    Color.rgb(
                            180,
                            30,
                            30
                    )
            );

        } else {

            tv.setTextColor(
                    Color.DKGRAY
            );
        }

        content.addView(tv);
    }

    /*
     * ============================================================
     * ورود و خروج پول
     * ============================================================
     */

    private void showMoneyFlow() {

        openPage(
                "ورود و خروج پول"
        );

        clearContent();

        addText(
                "ورود و خروج پول",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "در حال دریافت اطلاعات جریان پول...",
                15,
                Color.DKGRAY,
                false
        );

        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String response =
                                    httpGet(
                                            MONEY_URL
                                    );

                            final List<MoneyItem> list =
                                    parseMoney(
                                            response
                                    );

                            double buy = 0;
                            double sell = 0;

                            for (MoneyItem item :
                                    list) {

                                buy +=
                                        item.buyIndividual;

                                sell +=
                                        item.sellIndividual;
                            }

                            final double totalBuy =
                                    buy;

                            final double totalSell =
                                    sell;

                            final double net =
                                    buy - sell;

                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            clearContent();

                                            addText(
                                                    "ورود و خروج پول",
                                                    22,
                                                    Color.rgb(
                                                            20,
                                                            70,
                                                            120
                                                    ),
                                                    true
                                            );

                                            addText(
                                                    "خرید حقیقی: " +
                                                    formatNumber(
                                                            totalBuy
                                                    ),
                                                    17,
                                                    Color.rgb(
                                                            0,
                                                            120,
                                                            60
                                                    ),
                                                    true
                                            );

                                            addText(
                                                    "فروش حقیقی: " +
                                                    formatNumber(
                                                            totalSell
                                                    ),
                                                    17,
                                                    Color.rgb(
                                                            180,
                                                            30,
                                                            30
                                                    ),
                                                    true
                                            );

                                            addText(
                                                    "خالص جریان حجم: " +
                                                    formatNumber(
                                                            net
                                                    ),
                                                    18,
                                                    net >= 0
                                                            ? Color.rgb(
                                                                    0,
                                                                    120,
                                                                    60
                                                            )
                                                            : Color.rgb(
                                                                    180,
                                                                    30,
                                                                    30
                                                            ),
                                                    true
                                            );

                                            addText(
                                                    "تعداد نمادهای دارای داده: " +
                                                    list.size(),
                                                    15,
                                                    Color.DKGRAY,
                                                    false
                                            );

                                            addText(
                                                    "مبنای محاسبه: حجم خرید و فروش حقیقی",
                                                    14,
                                                    Color.GRAY,
                                                    false
                                            );
                                        }
                                    }
                            );

                        } catch (
                                final Exception e) {

                            handler.post(
                                    new Runnable() {
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
                                    }
                            );
                        }
                    }
                }
        );
    }

    /*
     * ============================================================
     * بررسی نمادها
     * ============================================================
     */

    private void showSymbols() {

        openPage(
                "بررسی نمادها"
        );

        clearContent();

        addText(
                "بررسی نمادها",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        final EditText input =
                new EditText(this);

        input.setHint(
                "مثلاً خودرو"
        );

        input.setTextSize(16);

        input.setSingleLine(true);

        input.setGravity(
                Gravity.RIGHT
        );

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

        search.setText(
                "جستجوی نماد"
        );

        search.setAllCaps(false);

        content.addView(search);

        final TextView result =
                new TextView(this);

        result.setTextSize(15);

        result.setTextColor(
                Color.DKGRAY
        );

        result.setGravity(
                Gravity.RIGHT
        );

        result.setLayoutDirection(
                View.LAYOUT_DIRECTION_RTL
        );

        result.setPadding(
                5,
                15,
                5,
                15
        );

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

                        hideKeyboard(
                                input
                        );

                        result.setText(
                                "در حال جستجو..."
                        );

                        /*
                         * جستجو ابتدا در داده‌های دریافت‌شده بازار
                         * انجام می‌شود تا درخواست اضافی کم شود.
                         */
                        boolean found = false;

                        StringBuilder local =
                                new StringBuilder();

                        for (MarketItem item :
                                marketItems) {

                            if (item.symbol
                                    .contains(symbol)) {

                                found = true;

                                local.append(
                                        item.symbol
                                );

                                local.append(
                                        "\nآخرین: "
                                );

                                local.append(
                                        formatNumber(
                                                item.last
                                        )
                                );

                                local.append(
                                        "\nپایانی: "
                                );

                                local.append(
                                        formatNumber(
                                                item.close
                                        )
                                );

                                local.append(
                                        "\nدرصد: "
                                );

                                local.append(
                                        formatPercent(
                                                item.percent
                                        )
                                );

                                local.append(
                                        "\nحجم: "
                                );

                                local.append(
                                        formatNumber(
                                                item.volume
                                        )
                                );

                                local.append(
                                        "\n\n"
                                );
                            }
                        }

                        if (found) {

                            result.setText(
                                    local.toString()
                            );

                        } else {

                            result.setText(
                                    "نماد مورد نظر در اطلاعات فعلی بازار پیدا نشد."
                            );
                        }
                    }
                }
        );
    }    /*
     * ============================================================
     * پیشنهادهای معاملاتی
     * ============================================================
     */

    private void showSuggestions() {

        openPage("پیشنهادهای معاملاتی");

        clearContent();

        addText(
                "پیشنهادهای معاملاتی",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "این بخش فقط فیلتر و محاسبه اطلاعات بازار است؛ تصمیم خرید و فروش با کاربر است.",
                14,
                Color.GRAY,
                false
        );

        if (marketItems.size() == 0) {

            addText(
                    "ابتدا اطلاعات بازار را به‌روزرسانی کنید.",
                    16,
                    Color.DKGRAY,
                    false
            );

            return;
        }

        ArrayList<MarketItem> candidates =
                new ArrayList<>();

        for (MarketItem item : marketItems) {

            if (item.symbol == null ||
                    item.symbol.length() == 0) {
                continue;
            }

            /*
             * فیلتر اولیه:
             * حجم > صفر
             * قیمت معتبر
             * درصد مثبت
             */
            if (item.volume > 0 &&
                    item.last > 0 &&
                    item.yesterday > 0 &&
                    item.percent > 0) {

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

        if (candidates.size() == 0) {

            addText(
                    "در اطلاعات فعلی بازار موردی مطابق فیلتر پیدا نشد.",
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

            MarketItem item =
                    candidates.get(i);

            addMarketRow(item);
        }
    }


    /*
     * ============================================================
     * تحلیل بنیادی
     * ============================================================
     */

    private void showFundamental() {

        openPage("تحلیل بنیادی");

        clearContent();

        addText(
                "تحلیل بنیادی",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "اطلاعات بنیادی کامل نیازمند داده‌های کدال و صورت‌های مالی است.",
                15,
                Color.DKGRAY,
                false
        );

        addText(
                "در این نسخه، اطلاعات بازار TSETMC نمایش داده می‌شود و از ساختن داده بنیادی جعلی خودداری شده است.",
                15,
                Color.GRAY,
                false
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

        addText(
                "نمونه اطلاعات قابل استفاده:",
                18,
                Color.rgb(20, 70, 120),
                true
        );

        int limit =
                Math.min(
                        20,
                        marketItems.size()
                );

        for (int i = 0;
             i < limit;
             i++) {

            MarketItem item =
                    marketItems.get(i);

            if (item.symbol == null ||
                    item.symbol.length() == 0) {
                continue;
            }

            String text =
                    item.symbol +
                    "\nآخرین قیمت: " +
                    formatNumber(item.last) +
                    "\nقیمت پایانی: " +
                    formatNumber(item.close) +
                    "\nحجم معاملات: " +
                    formatNumber(item.volume) +
                    "\nارزش معاملات: " +
                    formatNumber(item.value);

            addText(
                    text,
                    15,
                    Color.DKGRAY,
                    false
            );
        }
    }


    /*
     * ============================================================
     * تحلیل تکنیکال
     * ============================================================
     */

    private void showTechnical() {

        openPage("تحلیل تکنیکال");

        clearContent();

        addText(
                "تحلیل تکنیکال",
                22,
                Color.rgb(20, 70, 120),
                true
        );

        addText(
                "تحلیل اولیه بر اساس قیمت و درصد تغییرات بازار",
                15,
                Color.GRAY,
                false
        );

        if (marketItems.size() == 0) {

            addText(
                    "ابتدا اطلاعات بازار را به‌روزرسانی کنید.",
                    16,
                    Color.DKGRAY,
                    false
            );

            return;
        }

        ArrayList<MarketItem> list =
                new ArrayList<>();

        for (MarketItem item :
                marketItems) {

            if (item.symbol != null &&
                    item.symbol.length() > 0 &&
                    item.last > 0 &&
                    item.yesterday > 0) {

                list.add(item);
            }
        }

        Collections.sort(
                list,
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

        int limit =
                Math.min(
                        20,
                        list.size()
                );

        for (int i = 0;
             i < limit;
             i++) {

            MarketItem item =
                    list.get(i);

            String signal;

            if (item.percent > 0) {

                signal =
                        "مثبت";

            } else if (item.percent < 0) {

                signal =
                        "منفی";

            } else {

                signal =
                        "بدون تغییر";
            }

            String text =
                    item.symbol +
                    "\nوضعیت: " +
                    signal +
                    "\nدرصد تغییر: " +
                    formatPercent(item.percent) +
                    "\nآخرین: " +
                    formatNumber(item.last) +
                    "\nپایانی: " +
                    formatNumber(item.close) +
                    "\nحجم: " +
                    formatNumber(item.volume);

            addText(
                    text,
                    15,
                    item.percent >= 0
                            ? Color.rgb(
                                    0,
                                    120,
                                    60
                            )
                            : Color.rgb(
                                    180,
                                    30,
                                    30
                            ),
                    false
            );
        }
    }


    /*
     * ============================================================
     * اطمینان از وجود اطلاعات بازار
     * ============================================================
     */

    private boolean ensureMarketLoaded() {

        return marketItems != null &&
                marketItems.size() > 0;
    }


    /*
     * ============================================================
     * ارتباط اینترنتی
     * ============================================================
     */

    private String httpGet(
            String urlString)
            throws Exception {

        setupTsetmcSsl();

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(
                "GET"
        );

        connection.setConnectTimeout(
                20000
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
                "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json,text/plain,*/*"
        );

        connection.setRequestProperty(
                "Accept-Language",
                "fa-IR,fa;q=0.9,en-US;q=0.8"
        );

        connection.setRequestProperty(
                "Referer",
                "https://tsetmc.com/"
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
                    stream == null
                            ? ""
                            : readStream(stream);

            throw new IOException(
                    "HTTP " +
                    code +
                    " " +
                    error
            );
        }

        String result =
                readStream(stream);

        connection.disconnect();

        return result;
    }


    /*
     * ============================================================
     * خواندن Stream
     * ============================================================
     */

    private String readStream(
            InputStream input)
            throws Exception {

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


    /*
     * ============================================================
     * SSL مخصوص TSETMC
     * ============================================================
     */

    private void setupTsetmcSsl() {

        try {

            final SSLContext context =
                    SSLContext.getInstance(
                            "TLS"
                    );

            context.init(
                    null,
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
                    },
                    new SecureRandom()
            );

            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory()
            );

            HttpsURLConnection.setDefaultHostnameVerifier(
                    new HostnameVerifier() {

                        @Override
                        public boolean verify(
                                String hostname,
                                SSLSession session) {

                            return hostname.equals(
                                    "cdn.tsetmc.com"
                            );
                        }
                    }
            );

        } catch (Exception ignored) {
        }
    }


    /*
     * ============================================================
     * JSON String
     * ============================================================
     */

    private String getString(
            JSONObject obj,
            String key,
            String defaultValue) {

        try {

            Object value =
                    obj.opt(key);

            if (value == null ||
                    value == JSONObject.NULL) {

                return defaultValue;
            }

            /*
             * بعضی سرویس‌های بازار
             * مقدار را داخل value قرار می‌دهند.
             */
            if (value instanceof JSONObject) {

                JSONObject child =
                        (JSONObject) value;

                Object nested =
                        child.opt("value");

                if (nested != null &&
                        nested != JSONObject.NULL) {

                    return String.valueOf(
                            nested
                    );
                }
            }

            return String.valueOf(
                    value
            );

        } catch (Exception e) {

            return defaultValue;
        }
    }


    /*
     * ============================================================
     * JSON Number
     * ============================================================
     */

    private double getDouble(
            JSONObject obj,
            String key,
            double defaultValue) {

        try {

            Object value =
                    obj.opt(key);

            if (value == null ||
                    value == JSONObject.NULL) {

                return defaultValue;
            }

            if (value instanceof JSONObject) {

                JSONObject child =
                        (JSONObject) value;

                Object nested =
                        child.opt("value");

                if (nested != null &&
                        nested != JSONObject.NULL) {

                    value = nested;
                }
            }

            if (value instanceof Number) {

                return (
                        (Number) value
                ).doubleValue();
            }

            String text =
                    String.valueOf(
                            value
                    ).trim();

            if (text.length() == 0) {

                return defaultValue;
            }

            return Double.parseDouble(
                    text
            );

        } catch (Exception e) {

            return defaultValue;
        }
    }


    /*
     * ============================================================
     * اولین مقدار غیرخالی
     * ============================================================
     */

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


    /*
     * ============================================================
     * فرمت اعداد
     * ============================================================
     */

    private String formatNumber(
            double value) {

        if (Double.isNaN(value) ||
                Double.isInfinite(value)) {

            return "0";
        }

        if (Math.abs(value) >=
                1000000000) {

            return String.format(
                    Locale.US,
                    "%.2f B",
                    value / 1000000000.0
            );
        }

        if (Math.abs(value) >=
                1000000) {

            return String.format(
                    Locale.US,
                    "%.2f M",
                    value / 1000000.0
            );
        }

        if (Math.abs(value) >=
                1000) {

            return String.format(
                    Locale.US,
                    "%.0f",
                    value
            );
        }

        return String.format(
                Locale.US,
                "%.0f",
                value
        );
    }


    /*
     * ============================================================
     * درصد
     * ============================================================
     */

    private String formatPercent(
            double value) {

        return String.format(
                Locale.US,
                "%.2f%%",
                value
        );
    }


    /*
     * ============================================================
     * نمایش خطای خوانا
     * ============================================================
     */

    private String getReadableError(
            Exception e) {

        if (e == null) {

            return "خطای نامشخص";
        }

        String message =
                e.getMessage();

        if (message == null ||
                message.length() == 0) {

            return e.getClass()
                    .getSimpleName();
        }

        return message;
    }


    /*
     * ============================================================
     * بستن کیبورد
     * ============================================================
     */

    private void hideKeyboard(
            View view) {

        try {

            InputMethodManager manager =
                    (InputMethodManager)
                            getSystemService(
                                    INPUT_METHOD_SERVICE
                            );

            if (manager != null) {

                manager.hideSoftInputFromWindow(
                        view.getWindowToken(),
                        0
                );
            }

        } catch (Exception ignored) {
        }
    }


    /*
     * ============================================================
     * کلاس اطلاعات بازار
     * ============================================================
     */

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


    /*
     * ============================================================
     * کلاس پول حقیقی
     * ============================================================
     */

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
