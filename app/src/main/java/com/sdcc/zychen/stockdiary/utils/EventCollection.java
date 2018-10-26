package com.sdcc.zychen.stockdiary.utils;

public class EventCollection {
    public static class DelDiaryEvent {
        public int diaryId;

        public DelDiaryEvent(int diaryId){
            this.diaryId = diaryId;
        }
    }

    public static class AddActionEvent{}

    public static class AddFinishEvent{
        public DiaryUtils.DiaryBean diaryBean;
        public AddFinishEvent(DiaryUtils.DiaryBean diaryBean){
            this.diaryBean = diaryBean;
        }
    }
}
