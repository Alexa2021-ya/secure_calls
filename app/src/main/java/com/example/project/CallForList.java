package com.example.project;
import java.io.Serializable;


public class CallForList implements Serializable {
    private String nameCall = null;
    private String date = null;
    private String type = null;
    private String time = null;
    private String allTime = null;
    private String status = null;
    private String number = null;
    private int id;
    CallForList(int id, String nameCall, String isType, String date, String time, String allTime, String status, String number) {
        this.id = id;
        this.nameCall = nameCall;
        this.number = number;
        this.type = isType;
        this.date = date;
        this.time = time;
        this.allTime = allTime;
        this.status = status;
    }

    public String getNameCall() {
        if (nameCall == null) {
            return number;
        }
        else {
            return nameCall;
        }
    }

    public String getTypeCall() {
        return type;
    }

    public String getDateCall() {
        return date;
    }
    public String getStatusCall() {
        return status;
    }
    public String getTimeCall() {
        return time;
    }
    public String getAllTimeCall() {
        return allTime;
    }
    public int getIdCall() {
        return id;
    }
    public String getNumberCall() {
        return number;
    }
}
