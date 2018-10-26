package com.sdcc.zychen.stockdiary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sdcc.zychen.stockdiary.utils.DiaryUtils;
import com.sdcc.zychen.stockdiary.utils.EventCollection;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.List;

public class ShowDiaryListAdapter extends Adapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<DiaryUtils.DiaryBean> mDiaryBeanList;
    private int mEditPosition = -1;

    public ShowDiaryListAdapter(Context context, List<DiaryUtils.DiaryBean> mDiaryBeanList){
        mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mDiaryBeanList = mDiaryBeanList;
    }

    public void setList(List<DiaryUtils.DiaryBean> list){
        this.mDiaryBeanList = list;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new DiaryViewHolder(mLayoutInflater.inflate(R.layout.item_diary, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        DiaryViewHolder holder = (DiaryViewHolder) viewHolder;
        holder.mTvDay.setText(new SimpleDateFormat("d").format(mDiaryBeanList.get(position).getDate()));
        holder.mTvMonth.setText(new SimpleDateFormat("MM").format(mDiaryBeanList.get(position).getDate())+"月/"+new SimpleDateFormat("EE").format(mDiaryBeanList.get(position).getDate()));
        holder.mTvContent.setText(mDiaryBeanList.get(position).getTitle());
        holder.mTvIndexSh.setText(
                mDiaryBeanList.get(position).getStockDataBeanList().get(0).name_+":"+
                mDiaryBeanList.get(position).getStockDataBeanList().get(0).now_.toString());
        holder.mTvTime.setText(new SimpleDateFormat("HH:mm").format(mDiaryBeanList.get(position).getDate()));
        holder.mIvTime.setMaxHeight(holder.mTvTime.getLineHeight()/2);
//        holder.mIvTime.setMaxWidth(holder.mTvTime.getHeight());
    }

    @Override
    public int getItemCount() {
        return mDiaryBeanList.size();
    }

    class DiaryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView mTvDay;
        TextView mTvMonth;
        TextView mTvContent;
        TextView mTvIndexSh;
        TextView mTvTime;
        ImageView mIvTime;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvDay = itemView.findViewById(R.id.text_diary_day);
            mTvMonth = itemView.findViewById(R.id.text_diary_month);
            mTvContent = itemView.findViewById(R.id.text_diary_content);
            mTvIndexSh = itemView.findViewById(R.id.text_diary_indexsh);
            mTvTime = itemView.findViewById(R.id.text_diary_time);
            mIvTime = itemView.findViewById(R.id.img_diary_time);

            View main = itemView.findViewById(R.id.item_main_view);
            main.setOnClickListener(this);
            main.setOnLongClickListener(this);

            View delete = itemView.findViewById(R.id.btn_diary_delete);
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.main:
                    Toast.makeText(v.getContext(), "点击了main，位置为：" + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                    break;

                case R.id.btn_diary_delete:
                    int pos = getAdapterPosition();
                    EventBus.getDefault().post(new EventCollection.DelDiaryEvent(mDiaryBeanList.get(pos).get_id()));
                    mDiaryBeanList.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemRangeChanged(pos,mDiaryBeanList.size());
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.main:
                    Toast.makeText(v.getContext(), "长按了main，位置为：" + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    }
}
