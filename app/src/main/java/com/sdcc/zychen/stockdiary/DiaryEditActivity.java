package com.sdcc.zychen.stockdiary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.scrat.app.richtext.RichEditText;
import com.sdcc.zychen.stockdiary.utils.DiaryUtils;
import com.sdcc.zychen.stockdiary.utils.EventCollection;
import com.sdcc.zychen.stockdiary.utils.StockUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class DiaryEditActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_GET_CONTENT = 666;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 444;

    private RichEditText contentEditText;
    private EditText titleEditText;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.diary_edit_toolbar);
        toolbar.setTitle("编辑日记");
        toolbar.setSubtitle(new SimpleDateFormat("dd MMMM").format(new Date()));

        setSupportActionBar(toolbar);
        ActionBar actionBar =  getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setOnMenuItemClickListener(saveActionListener);

        contentEditText = (RichEditText) findViewById(R.id.rich_text);
        contentEditText.setHint("正文");
        contentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                HorizontalScrollView tools = findViewById(R.id.tools);
                tools.setVisibility(View.VISIBLE);
            }
        });
        titleEditText = (EditText) findViewById(R.id.diary_title);
        titleEditText.setHint("标题");
        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                HorizontalScrollView tools = findViewById(R.id.tools);
                tools.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void addFinish(EventCollection.AddFinishEvent addFinishEvent){
        Toast.makeText(this, "保存成功" , Toast.LENGTH_SHORT).show();
        finish();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void addDiary(EventCollection.AddActionEvent addActionEvent){
        Log.d("DiaryEditActivity","start ");
        DiaryUtils.DiaryBean diaryBean = new DiaryUtils.DiaryBean();
        diaryBean.setDate(new Date());
        diaryBean.setTitle(titleEditText.getText().toString());
        diaryBean.setContent(contentEditText.toHtml());
        diaryBean.setStockDataBeanList(StockUtils.getByCodes(
                new String[]{StockUtils.INDEX_SH,StockUtils.INDEX_SZ,StockUtils.INDEX_CYB}, true));
//        Log.d("DiaryEditActivity",diaryBean.getStockDataBeanList().get(0).id_);
        DiaryUtils.saveDiary(this, diaryBean);
        EventBus.getDefault().post(new EventCollection.AddFinishEvent(diaryBean));
    }

    private Toolbar.OnMenuItemClickListener saveActionListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_save:
                    EventBus.getDefault().post(new EventCollection.AddActionEvent());
                    break;
            }
            return true;
        }
    } ;

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_diaryedit_drawer, menu);
        return true;
    }
    /**
     * 加粗
     */
    public void setBold(View v) {
        contentEditText.bold(!contentEditText.contains(RichEditText.FORMAT_BOLD));
    }

    /**
     * 斜体
     */
    public void setItalic(View v) {
        contentEditText.italic(!contentEditText.contains(RichEditText.FORMAT_ITALIC));
    }

    /**
     * 下划线
     */
    public void setUnderline(View v) {
        contentEditText.underline(!contentEditText.contains(RichEditText.FORMAT_UNDERLINED));
    }

    /**
     * 删除线
     */
    public void setStrikethrough(View v) {
        contentEditText.strikethrough(!contentEditText.contains(RichEditText.FORMAT_STRIKETHROUGH));
    }

    /**
     * 序号
     */
    public void setBullet(View v) {
        contentEditText.bullet(!contentEditText.contains(RichEditText.FORMAT_BULLET));
    }

    /**
     * 引用块
     */
    public void setQuote(View v) {
        contentEditText.quote(!contentEditText.contains(RichEditText.FORMAT_QUOTE));
    }

    public void insertImg(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }

        Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
        getImage.addCategory(Intent.CATEGORY_OPENABLE);
        getImage.setType("image/*");
        startActivityForResult(getImage, REQUEST_CODE_GET_CONTENT);
    }
}
