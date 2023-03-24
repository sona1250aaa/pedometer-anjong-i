package com.example.team678_final;

public class PedometerInfo {

    private String date;
    private String count;

    PedometerInfo(String date, String count){
        this.date = date;
        this.count = count;
    }

    public String getDate() {
        return date;
    }

    public String getCount() {
        return count;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCount(String count) {
        this.count = count;
    }


}
