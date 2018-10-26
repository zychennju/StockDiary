package com.sdcc.zychen.stockdiary.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

public class DiaryUtils {

    public static void saveDiary(Context context, DiaryBean diaryBean){
        SQLiteDatabase mSQLiteDatabase = new DiaryDbHelper(context).getWritableDatabase();

        if(diaryBean._id==-1){//add
            ContentValues contentValues = new ContentValues();
            contentValues.put("title", diaryBean.title);
            contentValues.put("write_date", new SimpleDateFormat("yyyyMMddHHmmss").format(diaryBean.date));
            //保存content到文件中,文件名为yyyyMMddHHmmss.content
            String filename_content = new SimpleDateFormat("yyyyMMddHHmmss").format(diaryBean.date)+".content";
            writeFile(context, filename_content, diaryBean.content);
            contentValues.put("content_file",filename_content);

            //保存stockDataBeanList到文件中，json格式
            String filename_stockDataBeanList = new SimpleDateFormat("yyyyMMddHHmmss").format(diaryBean.date)+".stocklist";
            writeFile(context, filename_stockDataBeanList, new Gson().toJson(diaryBean.stockDataBeanList));
            contentValues.put("stocklist_file", filename_stockDataBeanList);

            mSQLiteDatabase.insert(DiaryDbHelper.TABLE_DIARY, null, contentValues);
        }else {//update

        }
    }

    public static int delDiary(Context context, int id){
        SQLiteDatabase mSQLiteDatabase = new DiaryDbHelper(context).getWritableDatabase();
        String[] args = {String.valueOf(id)};
        return mSQLiteDatabase.delete(DiaryDbHelper.TABLE_DIARY, "id=?", args);
    }

    public static List<DiaryBean> readDiary(Context context) throws ParseException {
        SQLiteDatabase mSQLiteDatabase = new DiaryDbHelper(context).getWritableDatabase();
        List<DiaryBean> diaryBeanList = new ArrayList<>();
        Cursor cursor = mSQLiteDatabase.rawQuery("select * from "+DiaryDbHelper.TABLE_DIARY+" order by id desc", null);
        while (cursor.moveToNext()){
            DiaryBean diaryBean = new DiaryBean();
            diaryBean.set_id(cursor.getInt(0));
            diaryBean.setTitle(cursor.getString(1));
            diaryBean.setDate(new SimpleDateFormat("yyyyMMddHHmmss").parse(cursor.getString(2)));
            diaryBean.setContentFile(cursor.getString(3));
            diaryBean.setStockDataBeanList(readStockListFile(context, cursor.getString(4)));
            diaryBeanList.add(diaryBean);
        }
        cursor.close();
        return diaryBeanList;
    }
    private static void writeFile(Context context, String filename, String content){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(filename, MODE_PRIVATE);
            fos.write(content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
    private static List<StockUtils.StockDataBean> readStockListFile(Context context, String filename){
        FileInputStream fis = null;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(filename)));
            String str_asset = br.readLine();
            Log.d("DiaryUtils", str_asset);
            if(str_asset !=null){
                List<StockUtils.StockDataBean> stockDataBeanList
                        = new Gson().fromJson(str_asset, new TypeToken<List<StockUtils.StockDataBean>>(){}.getType());
                return stockDataBeanList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static class DiaryBean{
        private int _id;//若为-1表示为新的日志
        private String title;//标题
        private String content;//文本内容，html格式
        private String contentFile;
        private Date date;//日期时间
        private List<StockUtils.StockDataBean> stockDataBeanList;//指数数据

        public String getContentFile() {
            return contentFile;
        }

        public void setContentFile(String contentFile) {
            this.contentFile = contentFile;
        }


        public DiaryBean(){
            _id = -1;
        }

        public int get_id() {
            return _id;
        }

        public void set_id(int _id) {
            this._id = _id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public List<StockUtils.StockDataBean> getStockDataBeanList() {
            return stockDataBeanList;
        }

        public void setStockDataBeanList(List<StockUtils.StockDataBean> stockDataBeanList) {
            this.stockDataBeanList = stockDataBeanList;
        }
    }
}
