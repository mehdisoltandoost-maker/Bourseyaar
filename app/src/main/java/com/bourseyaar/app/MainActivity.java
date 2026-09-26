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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private LinearLayout root;
    private LinearLayout content;
    private TextView status;

    private final Handler handler =
            new Handler();

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final List<MarketItem> marketItems =
            new ArrayList<>();

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
            BASE_URL +
            "ClientType/GetClientTypeAll";


    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(
                savedInstanceState
        );

        buildMainMenu();

        loadMarketData();
    }


    /*
     * ============================================================
     * منوی اصلی
     * ============================================================
     */

    private void buildMainMenu() {

        root =
                new LinearLayout(this);

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

        title.setText(
                "بورس‌یار"
        );

        title.setTextSize(
                26
        );

        title.setTextColor(
                Color.rgb(
                        20,
                        70,
                        120
                )
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
                title
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
                    public void onClick(
                            View v) {

                        showMarketOverview();
                    }
                }
        );


        addButton(
                menu,
                "پول هوشمند",
                new View.OnClickListener() {

                    @Override
                    public void onClick(
                            View v) {

                        showSmartMoney();
                    }
                }
        );


        addButton(
                menu,
                "ورود و خروج پول",
                new View.OnClickListener() {

                    @Override
                    public void onClick(
                            View v) {

                        showMoneyFlow();
                    }
                }
        );


        addButton(
                menu,
                "تحلیل بنیادی",
                new View.OnClickListener() {

                    @Override
                    public void onClick(
                            View v) {

                        showFundamental();
                    }
                }
        );


        addButton(
                menu,
                "تحلیل تکنیکال",
                new View.OnClickListener() {

                    @Override
                    public void onClick(
                            View v) {

                        showTechnical();
                    }
                }
        );


        addButton(
                menu,
                "بررسی نمادها",
                new View.OnClickListener() {

                    @Override
                    public void onClick(
                            View v) {

                        showSymbols();
                    }
                }
        );


        addButton(
                menu,
                "پیشنهادهای معاملاتی",
                new View.OnClickListener() {

                    @Override
                    public void onClick(
                            View v) {

                        showSuggestions();
                    }
                }
        );


        addButton(
                menu,
                "به‌روزرسانی اطلاعات",
                new View.OnClickListener() {

                    @Override
                    public void onClick(
                            View v) {

                        loadMarketData();
                    }
                }
        );


        root.addView(
                menu
        );


        TextView info =
                new TextView(this);

        info.setText(
                "دستیار تحلیل بازار سرمایه ایران"
        );

        info.setTextSize(
                16
        );

        info.setTextColor(
                Color.DKGRAY
        );

        info.setGravity(
                Gravity.CENTER
        );

        info.setPadding(
                10,
                20,
                10,
                10
        );

        root.addView(
                info
        );


        status =
                new TextView(this);

        status.setText(
                "در حال اتصال به TSETMC..."
        );

        status.setTextSize(
                14
        );

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
                status
        );


        setContentView(
                root
        );
    }


    /*
     * ============================================================
     * دکمه
     * ============================================================
     */

    private void addButton(
            LinearLayout parent,
            String text,
            View.OnClickListener listener) {

        Button button =
                new Button(this);

        button.setText(
                text
        );

        button.setTextSize(
                16
        );

        button.setAllCaps(
                false
        );

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


    /*
     * ============================================================
     * صفحه
     * ============================================================
     */

    private void openPage(
            String titleText) {

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

        title.setText(
                titleText
        );

        title.setTextSize(
                24
        );

        title.setTextColor(
                Color.rgb(
                        20,
                        70,
                        120
                )
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
                title
        );


        Button back =
                new Button(this);

        back.setText(
                "← بازگشت به منوی اصلی"
        );

        back.setTextSize(
                16
        );

        back.setAllCaps(
                false
        );

        back.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(
                            View v) {

                        buildMainMenu();
                    }
                }
        );

        root.addView(
                back
        );


        status =
                new TextView(this);

        status.setText(
                ""
        );

        status.setTextSize(
                14
        );

        status.setGravity(
                Gravity.CENTER
        );

        root.addView(
                status
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

        scroll.addView(
                content
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(
                root
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

        if (content == null) {
            return;
        }

        TextView tv =
                new TextView(this);

        tv.setText(
                text
        );

        tv.setTextSize(
                size
        );

        tv.setTextColor(
                color
        );

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

        content.addView(
                tv
        );
    }


    private void setStatus(
            final String text) {

        handler.post(
                new Runnable() {

                    @Override
                    public void run() {

                        if (status != null) {

                            status.setText(
                                    text
                            );
                        }
                    }
                }
        );
    }


    /*
     * ============================================================
     * دریافت بازار
     * ============================================================
     */

    private void loadMarketData() {

        openPage(
                "اطلاعات بازار"
        );

        addText(
                "دریافت اطلاعات بازار",
                21,
                Color.rgb(
                        20,
                        70,
                        120
                ),
                true
        );

        addText(
                "در حال اتصال به TSETMC...",
                15,
                Color.DKGRAY,
                false
        );

        setStatus(
                "در حال دریافت اطلاعات..."
        );


        executor.execute(
                new Runnable() {

                    @Override
                    public void run() {

                        try {

                            String response =
                                    httpGet(
                                            MARKET_URL
                                    );

                            if (response == null ||
                                    response.trim()
                                            .length() == 0) {

                                throw new Exception(
                                        "پاسخ TSETMC خالی است."
                                );
                            }


                            parseMarketWatch(
                                    response
                            );


                            handler.post(
                                    new Runnable() {

                                        @Override
                                        public void run() {

                                            if (marketItems.size()
                                                    > 0) {

                                                setStatus(
                                                        marketItems.size()
                                                                +
                                                                " نماد دریافت شد."
                                                );

                                                showMarketOverview();

                                            } else {

                                                clearContent();

                                                addText(
                                                        "پاسخ دریافت شد ولی نمادها قابل پردازش نیستند.",
                                                        18,
                                                        Color.RED,
                                                        true
                                                );

                                                addText(
                                                        "ساختار پاسخ TSETMC با ساختار مورد انتظار متفاوت است.",
                                                        15,
                                                        Color.DKGRAY,
                                                        false
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
                                                    "خطا در اتصال به TSETMC",
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
                Color.rgb(
                        20,
                        70,
                        120
                ),
                true
        );


        if (marketItems.size() == 0) {

            addText(
                    "اطلاعات بازار هنوز دریافت نشده است.",
                    16,
                    Color.DKGRAY,
                    false
            );

            return;
        }


        int positive = 0;
        int negative = 0;
        int unchanged = 0;

        double volume = 0;
        double value = 0;
        double trades = 0;


        for (MarketItem item :
                marketItems) {

            volume += item.volume;
            value += item.value;
            trades += item.trades;


            if (item.percent > 0.001) {

                positive++;

            } else if (item.percent < -0.001) {

                negative++;

            } else {

                unchanged++;
            }
        }


        addText(
                "تعداد نمادها: " +
                        marketItems.size(),
                17,
                Color.DKGRAY,
                true
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
                        formatNumber(volume),
                15,
                Color.DKGRAY,
                false
        );


        addText(
                "ارزش معاملات: " +
                        formatNumber(value),
                15,
                Color.DKGRAY,
                false
        );


        addText(
                "تعداد معاملات: " +
                        formatNumber(trades),
                15,
                Color.DKGRAY,
                false
        );


        addText(
                "نمادها",
                19,
                Color.rgb(
                        20,
                        70,
                        120
                ),
                true
        );


        ArrayList<MarketItem> list =
                new ArrayList<>(
                        marketItems
                );


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
                        40,
                        list.size()
                );


        for (int i = 0;
             i < limit;
             i++) {

            addMarketRow(
                    list.get(i)
            );
        }
    }


    private void addMarketRow(
            MarketItem item) {

        if (content == null) {
            return;
        }


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

        first.setTextSize(
                17
        );

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


        box.addView(
                first
        );


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
                        )
        );

        second.setTextSize(
                14
        );

        second.setGravity(
                Gravity.RIGHT
        );

        second.setTextColor(
                Color.DKGRAY
        );

        box.addView(
                second
        );


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
                        )
        );

        third.setTextSize(
                13
        );

        third.setGravity(
                Gravity.RIGHT
        );

        third.setTextColor(
                Color.GRAY
        );

        box.addView(
                third
        );


        content.addView(
                box
        );
    }


    /*
     * ============================================================
     * پول هوشمند
     * ============================================================
     */

    private void showSmartMoney() {

        openPage(
                "پول هوشمند"
        );

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
                "در حال دریافت اطلاعات حقیقی...",
                15,
                Color.DKGRAY,
                false
        );


        executor.execute(
                new Runnable() {

                    @Override
                    public void run() {

                        try {

                            if (marketItems.size() == 0) {

                                String market =
                                        httpGet(
                                                MARKET_URL
                                        );

                                parseMarketWatch(
                                        market
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


                                            if (list.size()
                                                    == 0) {

                                                addText(
                                                        "اطلاعات پول حقیقی دریافت نشد.",
                                                        16,
                                                        Color.DKGRAY,
                                                        false
                                                );

                                                return;
                                            }


                                            addText(
                                                    "مرتب‌سازی بر اساس خالص حجم حقیقی",
                                                    14,
                                                    Color.GRAY,
                                                    false
                                            );


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
                                                    "خطا در پول هوشمند",
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
     * ورود و خروج پول
     * ============================================================
     */

    private void showMoneyFlow() {

        openPage(
                "ورود و خروج پول"
        );

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
                "در حال محاسبه...",
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
     * بررسی نماد
     * ============================================================
     */

    private void showSymbols() {

        openPage(
                "بررسی نمادها"
        );

        addText(
                "بررسی نمادها",
                22,
                Color.rgb(
                        20,
                        70,
                        120
                ),
                true
        );


        final EditText input =
                new EditText(this);

        input.setHint(
                "مثلاً خودرو"
        );

        input.setSingleLine(
                true
        );

        input.setTextSize(
                16
        );

        input.setGravity(
                Gravity.RIGHT
        );

        input.setInputType(
                InputType.TYPE_CLASS_TEXT
        );


        content.addView(
                input
        );


        Button search =
                new Button(this);

        search.setText(
                "جستجوی نماد"
        );

        search.setAllCaps(
                false
        );

        content.addView(
                search
        );


        final TextView result =
                new TextView(this);

        result.setTextSize(
                15
        );

        result.setTextColor(
                Color.DKGRAY
        );

        result.setGravity(
                Gravity.RIGHT
        );

        result.setPadding(
                5,
                15,
                5,
                15
        );

        content.addView(
                result
        );


        search.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(
                            View v) {

                        final String text =
                                input.getText()
                                        .toString()
                                        .trim();


                        if (text.length() == 0) {

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


                        boolean found =
                                false;


                        StringBuilder builder =
                                new StringBuilder();


                        for (MarketItem item :
                                marketItems) {

                            if (item.symbol != null &&
                                    item.symbol.contains(
                                            text
                                    )) {

                                found =
                                        true;


                                builder.append(
                                        item.symbol
                                );

                                builder.append(
                                        "\nآخرین: "
                                );

                                builder.append(
                                        formatNumber(
                                                item.last
                                        )
                                );

                                builder.append(
                                        "\nپایانی: "
                                );

                                builder.append(
                                        formatNumber(
                                                item.close
                                        )
                                );

                                builder.append(
                                        "\nدرصد: "
                                );

                                builder.append(
                                        formatPercent(
                                                item.percent
                                        )
                                );

                                builder.append(
                                        "\nحجم: "
                                );

                                builder.append(
                                        formatNumber(
                                                item.volume
                                        )
                                );

                                builder.append(
                                        "\n\n"
                                );
                            }
                        }


                        if (found) {

                            result.setText(
                                    builder.toString()
                            );

                        } else {

                            result.setText(
                                    "نماد در اطلاعات فعلی بازار پیدا نشد."
                            );
                        }
                    }
                }
        );
    }


    /*
     * ============================================================
     * پیشنهادهای معاملاتی
     * ============================================================
     */

    private void showSuggestions() {

        openPage(
                "پیشنهادهای معاملاتی"
        );

        addText(
                "پیشنهادهای معاملاتی",
                22,
                Color.rgb(
                        20,
                        70,
                        120
                ),
                true
        );

        addText(
                "فیلتر بر اساس اطلاعات فعلی بازار؛ این بخش توصیه قطعی خرید یا فروش نیست.",
                14,
                Color.GRAY,
                false
        );


        ArrayList<MarketItem> candidates =
                new ArrayList<>();


        for (MarketItem item :
                marketItems) {

            if (item.symbol.length() > 0 &&
                    item.last > 0 &&
                    item.yesterday > 0 &&
                    item.volume > 0 &&
                    item.percent > 0) {

                candidates.add(
                        item
                );
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
                    "موردی مطابق فیلتر پیدا نشد.",
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


    /*
     * ============================================================
     * تحلیل بنیادی
     * ============================================================
     */

    private void showFundamental() {

        openPage(
                "تحلیل بنیادی"
        );

        addText(
                "تحلیل بنیادی",
                22,
                Color.rgb(
                        20,
                        70,
                        120
                ),
                true
        );

        addText(
                "اطلاعات کامل بنیادی باید از صورت‌های مالی و کدال دریافت شود.",
                16,
                Color.DKGRAY,
                false
        );

        addText(
                "در این نسخه اطلاعات واقعی بازار TSETMC نمایش داده می‌شود و داده بنیادی ساختگی تولید نمی‌شود.",
                15,
                Color.GRAY,
                false
        );


        if (marketItems.size() == 0) {

            addText(
                    "اطلاعات بازار هنوز دریافت نشده است.",
                    16,
                    Color.DKGRAY,
                    false
            );

            return;
        }


        int count = 0;


        for (MarketItem item :
                marketItems) {

            if (item.symbol.length() == 0) {
                continue;
            }


            addText(
                    item.symbol +
                            "\nآخرین: " +
                            formatNumber(
                                    item.last
                            ) +
                            "\nپایانی: " +
                            formatNumber(
                                    item.close
                            ) +
                            "\nحجم: " +
                            formatNumber(
                                    item.volume
                            ) +
                            "\nارزش: " +
                            formatNumber(
                                    item.value
                            ),
                    15,
                    Color.DKGRAY,
                    false
            );


            count++;


            if (count >= 20) {
                break;
            }
        }
    }


    /*
     * ============================================================
     * تحلیل تکنیکال
     * ============================================================
     */

    private void showTechnical() {

        openPage(
                "تحلیل تکنیکال"
        );

        addText(
                "تحلیل تکنیکال",
                22,
                Color.rgb(
                        20,
                        70,
                        120
                ),
                true
        );

        addText(
                "تحلیل اولیه قیمت و روند تغییرات بازار",
                15,
                Color.GRAY,
                false
        );


        if (marketItems.size() == 0) {

            addText(
                    "اطلاعات بازار موجود نیست.",
                    16,
                    Color.DKGRAY,
                    false
            );

            return;
        }


        ArrayList<MarketItem> list =
                new ArrayList<>(
                        marketItems
                );


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


            addText(
                    item.symbol +
                            "\nوضعیت: " +
                            signal +
                            "\nدرصد تغییر: " +
                            formatPercent(
                                    item.percent
                            ) +
                            "\nآخرین: " +
                            formatNumber(
                                    item.last
                            ) +
                            "\nپایانی: " +
                            formatNumber(
                                    item.close
                            ),
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
     * تجزیه اطلاعات MarketWatch
     * ============================================================
     */

    private void parseMarketWatch(
            String response)
            throws Exception {

        marketItems.clear();


        Object root =
                new JSONObject(
                        response
                );


        JSONArray array =
                findArray(
                        root,
                        0
                );


        if (array == null) {

            /*
             * اگر خود پاسخ آرایه باشد.
             */
            try {

                array =
                        new JSONArray(
                                response
                        );

            } catch (Exception ignored) {
            }
        }


        if (array == null) {

            return;
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


            item.insCode =
                    firstNonEmpty(
                            getString(
                                    obj,
                                    "insCode",
                                    ""
                            ),
                            getString(
                                    obj,
                                    "InsCode",
                                    ""
                            ),
                            getString(
                                    obj,
                                    "instrumentId",
                                    ""
                            )
                    );


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
                                    "symbolName",
                                    ""
                            )
                    );


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
                            )
                    );


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


            item.last =
                    getDouble(
                            obj,
                            "pl",
                            getDouble(
                                    obj,
                                    "pDrCotVal",
                                    getDouble(
                                            obj,
                                            "last",
                                            0
                                    )
                            )
                    );


            item.close =
                    getDouble(
                            obj,
                            "pc",
                            getDouble(
                                    obj,
                                    "pClosing",
                                    getDouble(
                                            obj,
                                            "close",
                                            0
                                    )
                            )
                    );


            item.yesterday =
                    getDouble(
                            obj,
                            "py",
                            getDouble(
                                    obj,
                                    "priceYesterday",
                                    getDouble(
                                            obj,
                                            "yesterday",
                                            0
                                    )
                            )
                    );


            item.min =
                    getDouble(
                            obj,
                            "pmin",
                            getDouble(
                                    obj,
                                    "priceMin",
                                    0
                            )
                    );


            item.max =
                    getDouble(
                            obj,
                            "pmax",
                            getDouble(
                                    obj,
                                    "priceMax",
                                    0
                            )
                    );


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
                                            0
                                    )
                            )
                    );


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
                                            0
                                    )
                            )
                    );


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
                                            0
                                    )
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
                        (
                                (
                                        item.close -
                                                item.yesterday
                                )
                                        /
                                        item.yesterday
                        )
                                *
                                100.0;
            }


            if (item.insCode.length() > 0 ||
                    item.symbol.length() > 0) {

                marketItems.add(
                        item
                );
            }
        }
    }


    /*
     * ============================================================
     * پیدا کردن Array داخل پاسخ
     * ============================================================
     */

    private JSONArray findArray(
            Object value,
            int depth) {

        if (value == null ||
                depth > 6) {

            return null;
        }


        if (value instanceof JSONArray) {

            return (
                    JSONArray
            ) value;
        }


        if (!(value instanceof JSONObject)) {

            return null;
        }


        JSONObject obj =
                (JSONObject) value;


        String[] keys = {

                "marketwatch",
                "marketWatch",
                "marketWatchDto",
                "marketMap",
                "marketmap",
                "data",
                "items",
                "result",
                "value"
        };


        for (String key :
                keys) {

            Object child =
                    obj.opt(
                            key
                    );


            if (child instanceof JSONArray) {

                return (
                        JSONArray
                ) child;
            }


            JSONArray nested =
                    findArray(
                            child,
                            depth + 1
                    );


            if (nested != null) {

                return nested;
            }
        }


        return null;
    }


    /*
     * ============================================================
     * پول حقیقی
     * ============================================================
     */

    private List<MoneyItem> parseMoney(
            String response)
            throws Exception {

        List<MoneyItem> result =
                new ArrayList<>();


        JSONObject root =
                new JSONObject(
                        response
                );


        JSONArray array =
                null;


        Object value =
                root.opt(
                        "clientTypeAllDto"
                );


        if (value instanceof JSONArray) {

            array =
                    (JSONArray) value;

        } else {

            array =
                    findArray(
                            value,
                            0
                    );
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
                                    "InsCode",
                                    ""
                            )
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
                            getDouble(
                                    obj,
                                    "buyIValue",
                                    0
                            )
                    );


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


            MarketItem market =
                    findMarketItem(
                            item.insCode
                    );


            if (market != null) {

                item.symbol =
                        market.symbol;
            }


            if (item.netVolume != 0 ||
                    item.netValue != 0) {

                result.add(
                        item
                );
            }
        }


        return result;
    }


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


    private void addMoneyRow(
            MoneyItem item) {

        TextView tv =
                new TextView(this);


        String symbol =
                item.symbol.length() > 0
                        ? item.symbol
                        : "نماد ناشناس";


        tv.setText(
                symbol +
                        "\nخالص حجم حقیقی: " +
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
                        )
        );


        if (item.buyValue != 0 ||
                item.sellValue != 0) {

            tv.append(
                    "\nخالص ارزش حقیقی: " +
                            formatNumber(
                                    item.netValue
                            )
            );
        }


        tv.setTextSize(
                15
        );

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


        content.addView(
                tv
        );
    }


    /*
     * ============================================================
     * HTTP
     * ============================================================
     */

    private String httpGet(
            String urlString)
            throws Exception {

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
                "Cache-Control",
                "no-cache"
        );


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


            String error =
                    input == null
                            ? ""
                            : readStream(
                                    input
                            );


            connection.disconnect();


            throw new Exception(
                    "HTTP " +
                            code +
                            "\n" +
                            error
            );
        }


        String result =
                readStream(
                        input
                );


        connection.disconnect();


        return result;
    }


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


        StringBuilder result =
                new StringBuilder();


        String line;


        while (
                (line =
                        reader.readLine())
                        != null) {

            result.append(
                    line
            );
        }


        reader.close();


        return result.toString();
    }


    /*
     * ============================================================
     * JSON
     * ============================================================
     */

    private static String getString(
            JSONObject object,
            String key,
            String defaultValue) {

        try {

            Object value =
                    object.opt(
                            key
                    );


            if (value == null ||
                    value == JSONObject.NULL) {

                return defaultValue;
            }


            /*
             * پشتیبانی از:
             * {"value": "..."}
             */
            if (value instanceof JSONObject) {

                JSONObject child =
                        (JSONObject) value;


                Object nested =
                        child.opt(
                                "value"
                        );


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


    private static double getDouble(
            JSONObject object,
            String key,
            double defaultValue) {

        try {

            Object value =
                    object.opt(
                            key
                    );


            if (value == null ||
                    value == JSONObject.NULL) {

                return defaultValue;
            }


            if (value instanceof JSONObject) {

                JSONObject child =
                        (JSONObject) value;


                Object nested =
                        child.opt(
                                "value"
                        );


                if (nested != null &&
                        nested != JSONObject.NULL) {

                    value =
                            nested;
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
                    )
                            .replace(
                                    ",",
                                    ""
                            )
                            .trim();


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


    private static String firstNonEmpty(
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
     * فرمت
     * ============================================================
     */

    private static String formatNumber(
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
                    value /
                            1000000000.0
            );
        }


        if (Math.abs(value) >=
                1000000) {

            return String.format(
                    Locale.US,
                    "%.2f M",
                    value /
                            1000000.0
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

        if (e == null) {

            return "خطای نامشخص";
        }


        String message =
                e.getMessage();


        if (message == null ||
                message.trim().length() == 0) {

            return e.getClass()
                    .getSimpleName();
        }


        return message;
    }


    private void hideKeyboard(
            View view) {

        try {

            InputMethodManager manager =
                    (InputMethodManager)
                            getSystemService(
                                    Context.INPUT_METHOD_SERVICE
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
     * پایان
     * ============================================================
     */

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
     * مدل بازار
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
     * مدل پول
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
