package com.example.project;
import static com.example.project.MainActivity.confContacts;
import android.content.Context;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CallStructure {
    public static final String statusOk = "Принят";
    public static final String statusNo = "Не принят";
    public static final String statusHangup = "Отклонён";
    public static final String outCall = "Исходящий вызов";
    public static final String inCall = "Входящий вызов";
    public String name;
    public String number;
    public String currentDate;
    public String currentTime;
    public String isStatus = statusNo;
    public String isType;
    public String allTime;

    CallStructure(String name, String isType, String number) {
        this.name = name;
        this.isType = isType;

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = date.format(formatter);this.currentDate = formattedDate;

        LocalTime time = LocalTime.now();
        formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.currentTime = time.format(formatter);

        this.number = number;
    }

    public void addCall(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        db.addCall(isType, name, number, currentDate, currentTime, isStatus, allTime);
    }

    public void addCallConf(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        db.addCall(confContacts, isType, currentDate, currentTime, isStatus, allTime);
    }
}
