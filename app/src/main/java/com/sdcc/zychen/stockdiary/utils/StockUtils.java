package com.sdcc.zychen.stockdiary.utils;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockUtils {
    public static final String INDEX_SH = "sh000001";
    public static final String INDEX_SZ = "sz399001";
    public static final String INDEX_CYB = "sz399006";

    /**
     * @param codes 格式如sh000001
     * @return 股票实时数据
     */
    public static List<StockDataBean> getByCodes(String[] codes, boolean isSync){
        String list = codes[0];
        for (int i = 1; i < codes.length; i++) {
            list = list +","+codes[i];
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://hq.sinajs.cn/list="+list).method("GET",null).build();
        Call call  = okHttpClient.newCall(request);
        if(isSync){
            try {
                Response response = call.execute();
                return new ArrayList<>(sinaResponseToStocks(response.body().string()).values());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("StockUtils", e.getLocalizedMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        EventBus.getDefault().post(sinaResponseToStocks(response.body().string()));
                    }
                }
            });
        }
        return null;
    }

    private static TreeMap<String, StockDataBean> sinaResponseToStocks(String response){
        response = response.replaceAll("\n", "");
        String[] stocks = response.split(";");

        TreeMap<String, StockDataBean> stockMap = new TreeMap();
        for(String stock : stocks) {
            String[] leftRight = stock.split("=");
            if (leftRight.length < 2)
                continue;

            String right = leftRight[1].replaceAll("\"", "");
            if (right.isEmpty())
                continue;

            String left = leftRight[0];
            if (left.isEmpty())
                continue;

            StockDataBean stockNow = new StockDataBean();
            stockNow.id_ = left.split("_")[2];

            String[] values = right.split(",");
            stockNow.name_ = values[0];
            stockNow.open_ = values[1];
            stockNow.yesterday_ = values[2];
            stockNow.now_ = values[3];
            stockNow.high_ = values[4];
            stockNow.low_ = values[5];
            stockNow.b1_ = values[10];
            stockNow.b2_ = values[12];
            stockNow.b3_ = values[14];
            stockNow.b4_ = values[16];
            stockNow.b5_ = values[18];
            stockNow.bp1_ = values[11];
            stockNow.bp2_ = values[13];
            stockNow.bp3_ = values[15];
            stockNow.bp4_ = values[17];
            stockNow.bp5_ = values[19];
            stockNow.s1_ = values[20];
            stockNow.s2_ = values[22];
            stockNow.s3_ = values[24];
            stockNow.s4_ = values[26];
            stockNow.s5_ = values[28];
            stockNow.sp1_ = values[21];
            stockNow.sp2_ = values[23];
            stockNow.sp3_ = values[25];
            stockNow.sp4_ = values[27];
            stockNow.sp5_ = values[29];
            stockNow.time_ = values[values.length - 3] + "_" + values[values.length - 2];
            stockMap.put(stockNow.id_, stockNow);
        }

        return stockMap;
    }

    public static class StockDataBean{
        public String id_, name_;
        public String open_, yesterday_, now_, high_, low_;
        public String b1_, b2_, b3_, b4_, b5_;
        public String bp1_, bp2_, bp3_, bp4_, bp5_;
        public String s1_, s2_, s3_, s4_, s5_;
        public String sp1_, sp2_, sp3_, sp4_, sp5_;
        public String time_;
    }
}
