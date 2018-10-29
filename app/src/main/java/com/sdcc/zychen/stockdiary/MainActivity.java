package com.sdcc.zychen.stockdiary;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.sdcc.zychen.stockdiary.extra.SwipeItemLayout;
import com.sdcc.zychen.stockdiary.utils.DiaryUtils;
import com.sdcc.zychen.stockdiary.utils.EventCollection;
import com.sdcc.zychen.stockdiary.utils.StockUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarLayout appBarLayout;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

    private CompactCalendarView compactCalendarView;

    private boolean isExpanded = false;

    private ShowDiaryListAdapter diaryListAdapter;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<DiaryUtils.DiaryBean> mDiaryBeanList = new ArrayList<>();
        try {
            mDiaryBeanList = DiaryUtils.readDiary(this);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        diaryListAdapter.setList(mDiaryBeanList);
        diaryListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("StockDiary");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        appBarLayout = findViewById(R.id.app_bar_layout);

        // Set up the CompactCalendarView
        compactCalendarView = findViewById(R.id.compactcalendar_view);

        // Force English
        compactCalendarView.setLocale(TimeZone.getDefault(), Locale.getDefault());

        compactCalendarView.setShouldDrawDaysHeader(true);

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                setSubtitle(dateFormat.format(dateClicked));
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                setSubtitle(dateFormat.format(firstDayOfNewMonth));
            }
        });


        // Set current date to today
        Date date = new Date();
        setSubtitle(dateFormat.format(date));
        if (compactCalendarView != null) {
            compactCalendarView.setCurrentDate(date);
        }

        final ImageView arrow = findViewById(R.id.date_picker_arrow);

        RelativeLayout datePickerButton = findViewById(R.id.date_picker_button);

        datePickerButton.setOnClickListener(v -> {
            float rotation = isExpanded ? 0 : 180;
            ViewCompat.animate(arrow).rotation(rotation).start();

            isExpanded = !isExpanded;
            appBarLayout.setExpanded(isExpanded, true);
        });

        FloatingActionButton addDiaryButton = findViewById(R.id.add_diary_button);
        addDiaryButton.setOnClickListener(v -> {
            startActivity(new Intent(this, DiaryEditActivity.class));
        });
        Timer timer = new Timer("RefreshStocks");
        EventBus.getDefault().register(this);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                StockUtils.getByCodes(new String[]{StockUtils.INDEX_SH,StockUtils.INDEX_SZ,StockUtils.INDEX_CYB}, false);
            }
        }, 0, 2000); // 2 seconds


        List<DiaryUtils.DiaryBean> mDiaryBeanList = new ArrayList<>();
        try {
            mDiaryBeanList = DiaryUtils.readDiary(this);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        RecyclerView rvShowDiary = findViewById(R.id.rv_show_diary);
        rvShowDiary.setLayoutManager(new LinearLayoutManager(this));
        rvShowDiary.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(this));
        diaryListAdapter = new ShowDiaryListAdapter(this, mDiaryBeanList);
        rvShowDiary.setAdapter(diaryListAdapter);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(Color.RED);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },2000);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void delDiary(EventCollection.DelDiaryEvent event){
        int res = DiaryUtils.delDiary(this, event.diaryId);
        if(res>0){
            Toast.makeText(this, "删除成功：" + event.diaryId, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "删除失败：" + event.diaryId, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateStockView(TreeMap<String, StockUtils.StockDataBean> stockRefreshEvent) {
        Collection<StockUtils.StockDataBean> stocks = stockRefreshEvent.values();
        for(StockUtils.StockDataBean stock : stocks) {
            if (stock.id_.equals(StockUtils.INDEX_SH) || stock.id_.equals(StockUtils.INDEX_SZ) || stock.id_.equals(StockUtils.INDEX_CYB)) {
                Double dNow = Double.parseDouble(stock.now_);
                Double dYesterday = Double.parseDouble(stock.yesterday_);
                Double dIncrease = dNow - dYesterday;
                Double dPercent = dIncrease / dYesterday * 100;
                String change = String.format("%.2f", dPercent) + "% " + String.format("%.2f", dIncrease);

                int indexId;
                int changeId;
                if (stock.id_.equals(StockUtils.INDEX_SH)) {
                    indexId = R.id.stock_sh_index;
                    changeId = R.id.stock_sh_change;
                } else if (stock.id_.equals(StockUtils.INDEX_SZ)) {
                    indexId = R.id.stock_sz_index;
                    changeId = R.id.stock_sz_change;
                } else {
                    indexId = R.id.stock_chuang_index;
                    changeId = R.id.stock_chuang_change;
                }

                TextView indexText = (TextView) findViewById(indexId);
                indexText.setText(stock.now_);
                int color = Color.BLACK;
                if (dIncrease > 0) {
                    color = Color.RED;
                } else if (dIncrease < 0) {
                    color = Color.GREEN;
                }
                indexText.setTextColor(color);

                TextView changeText = (TextView) findViewById(changeId);
                changeText.setText(change);
                changeText.setTextColor(color);

                continue;
            }
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView tvTitle = findViewById(R.id.title);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    private void setSubtitle(String subtitle) {
        TextView datePickerTextView = findViewById(R.id.date_picker_text_view);

        if (datePickerTextView != null) {
            datePickerTextView.setText(subtitle);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
