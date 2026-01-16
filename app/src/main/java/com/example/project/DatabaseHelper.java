package com.example.project;
import static com.example.project.MainActivity.confContacts;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "user.db";
    private static final int SCHEMA = 1;
    static final String TABLE_CONTACTS = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final  String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    static final String TABLE_CALLS_USERS = "calls_users";
    public static final String COLUMN_ID_CONTACT = "id_contact";
    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_ID_CALL = "id_call";
    static final String TABLE_CALLS = "calls";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TIME_CALL = "time_call";
    static final String TABLE_TYPE_CALL = "type_call";
    public static final String COLUMN_NAME_TYPE = "name";
    static final String TABLE_STATUS_CALL = "call_status";
    public static final String COLUMN_NAME_STATUS = "name";
    static final String TABLE_CONF = "conf";
    public static final String COLUMN_ID_CONF = "id_conf";

    SQLiteDatabase db = getWritableDatabase();
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CONTACTS + "(" + COLUMN_ID
        + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME
        + " TEXT, " + COLUMN_PHONE_NUMBER + " TEXT); ");

        db.execSQL("CREATE TABLE " + TABLE_CALLS_USERS + "(" + COLUMN_ID
                + " INTEGER PRIMARY KEY, " + COLUMN_ID_CONTACT
                + " INTEGER, " + COLUMN_URI +
                " TEXT, " + COLUMN_ID_CALL + " INTEGER, " +
                " FOREIGN KEY (" + COLUMN_ID_CONTACT + ") REFERENCES " +
                TABLE_CONTACTS + "(" + COLUMN_ID + "), " +
                " FOREIGN KEY (" + COLUMN_ID_CALL + ") REFERENCES " +
                TABLE_CALLS + "(" + COLUMN_ID + "));");

        db.execSQL("CREATE TABLE " + TABLE_CONF + "(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ID_CALL + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_ID_CALL + ") REFERENCES " + TABLE_CALLS + "(" + COLUMN_ID + ")" + ");");


        db.execSQL("CREATE TABLE " + TABLE_CALLS + "(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_DATE
                + " TEXT, " + COLUMN_TIME +
                " TEXT, " + COLUMN_TIME_CALL +
                " TEXT, " + COLUMN_STATUS +
                " TEXT, " + COLUMN_TYPE +
                " TEXT, " + COLUMN_ID_CONF +
                " INTEGER, FOREIGN KEY(" + COLUMN_ID_CONF +
                ") REFERENCES " + TABLE_CONF + "(" + COLUMN_ID + ")); ");

        db.execSQL("CREATE TABLE " + TABLE_TYPE_CALL + "(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME_TYPE
                + " TEXT); ");

        db.execSQL("INSERT INTO "+ TABLE_TYPE_CALL +" (" + COLUMN_NAME_TYPE + ") VALUES (" + "'" + CallStructure.inCall + "'), ('" + CallStructure.outCall + "')");

        db.execSQL("CREATE TABLE " + TABLE_STATUS_CALL + "(" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME_STATUS
                + " TEXT); ");

        db.execSQL("INSERT INTO "+ TABLE_STATUS_CALL +" (" + COLUMN_NAME_STATUS + ") VALUES (" + "'" + CallStructure.statusOk + "'), ('"
                + CallStructure.statusHangup + "'), ('"
                + CallStructure.statusNo + "')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    public void addContacts(ArrayList<Contact> contacts) {
        deleteContacts();

        for (int i = 0; i < contacts.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COLUMN_NAME, contacts.get(i).getName());
            cv.put(DatabaseHelper.COLUMN_PHONE_NUMBER, contacts.get(i).getNumber());

            db.insert(TABLE_CONTACTS, null, cv);
        }

        stopConnect();
    }

    public void getContacts() {
        if (MainActivity.contacts != null) {
            MainActivity.contacts.clear();
        }
        MainActivity.contacts = new ArrayList<>();

        Cursor userCursor;
        userCursor =  db.rawQuery("select * from "+ DatabaseHelper.TABLE_CONTACTS, null);

        if (userCursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String item = userCursor.getString(userCursor.getColumnIndex(COLUMN_NAME));
                @SuppressLint("Range") String item2 = userCursor.getString(userCursor.getColumnIndex(COLUMN_PHONE_NUMBER));

                Contact contact = new Contact(item, item2);

                MainActivity.contacts.add(contact);
            } while (userCursor.moveToNext());
        }

        userCursor.close();
        stopConnect();
    }

    public void deleteContacts(){
        db.delete(TABLE_CONTACTS, null, null);
    }

    public void addCall(String type, String name, String number, String date, String time, String status, String allTime) {
        int idType = getIdType(type);
        int idStatus = getIdStatus(status);

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TYPE, idType);
        cv.put(DatabaseHelper.COLUMN_STATUS, idStatus);
        cv.put(DatabaseHelper.COLUMN_DATE, date);
        cv.put(DatabaseHelper.COLUMN_TIME, time);
        cv.put(DatabaseHelper.COLUMN_TIME_CALL, allTime);

        long id = db.insert(TABLE_CALLS, null, cv);
        cv = new ContentValues();

        if (id != -1) {
            cv.put(DatabaseHelper.COLUMN_ID_CALL, id);
        }

        if (name != null) {
            int idContact = getIdContact(name, number);
            cv.put(DatabaseHelper.COLUMN_ID_CONTACT, idContact);
        }
        else {
            cv.put(DatabaseHelper.COLUMN_ID_CONTACT, -1);
        }

        cv.put(DatabaseHelper.COLUMN_URI, number);

        db.insert(TABLE_CALLS_USERS, null, cv);

        stopConnect();
    }

    public void addCall(ArrayList<Contact> contacts, String type, String date, String time, String status, String allTime) {
        int idType = getIdType(type);
        int idStatus = getIdStatus(status);

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TYPE, idType);
        cv.put(DatabaseHelper.COLUMN_STATUS, idStatus);
        cv.put(DatabaseHelper.COLUMN_DATE, date);
        cv.put(DatabaseHelper.COLUMN_TIME, time);
        cv.put(DatabaseHelper.COLUMN_TIME_CALL, allTime);

        long id = db.insert(TABLE_CALLS, null, cv);

        for (Contact contact : confContacts) {
            cv = new ContentValues();

            if (id != -1) {
                cv.put(DatabaseHelper.COLUMN_ID_CALL, id);
            }

            int idContact = getIdContact(contact.getName(), contact.getNumber());
            cv.put(DatabaseHelper.COLUMN_ID_CONTACT, idContact);
            cv.put(DatabaseHelper.COLUMN_URI, contact.getNumber());
            db.insert(TABLE_CALLS_USERS, null, cv);
        }

        cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_ID_CALL, id);
        long id_conf = db.insert(TABLE_CONF, null, cv);

        cv = new ContentValues();
        cv.put(COLUMN_ID_CONF, id_conf);
        db.update(TABLE_CALLS, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        stopConnect();
    }

    public int getIdContact(String name, String phoneNumber) {
        Cursor userCursor;
        userCursor = db.rawQuery("select " + COLUMN_ID + " from " + DatabaseHelper.TABLE_CONTACTS + " where " + COLUMN_NAME + " = '" + name + "' and " + COLUMN_PHONE_NUMBER +
                "='" + phoneNumber + "'", null);

        if (userCursor.moveToFirst()) {
            return userCursor.getInt(0);
        }

        userCursor.close();

        return -1;
    }

    public int getIdType(String name_type) {
        Cursor userCursor;
        userCursor = db.rawQuery("select " + COLUMN_ID + " from " + DatabaseHelper.TABLE_TYPE_CALL + " where " + COLUMN_NAME_TYPE + " = '" + name_type + "'", null);


        if (userCursor.moveToFirst()) {
            return userCursor.getInt(0);
        }

        userCursor.close();

        return -1;
    }

    public int getIdStatus(String name_status) {
        Cursor userCursor;
        userCursor = db.rawQuery("select " + COLUMN_ID + " from " + DatabaseHelper.TABLE_STATUS_CALL + " where " + COLUMN_NAME_STATUS + " = '" + name_status + "'", null);

        if (userCursor.moveToFirst()) {
            return userCursor.getInt(0);
        }

        userCursor.close();

        return -1;
    }

    public String getNameContact(int id_contact) {
        Cursor userCursor;
        userCursor =  db.rawQuery("select " + COLUMN_NAME + " from " + DatabaseHelper.TABLE_CONTACTS + " where " + COLUMN_ID + "=" + id_contact, null);

        if (userCursor.moveToFirst()) {
            return userCursor.getString(0);
        }

        userCursor.close();

        return null;
    }

    public String getPhoneNumberContact(int id_contact) {
        Cursor userCursor;
        userCursor =  db.rawQuery("select " + COLUMN_PHONE_NUMBER + " from " + DatabaseHelper.TABLE_CONTACTS + " where " + COLUMN_ID + "=" + id_contact, null);

        if (userCursor.moveToFirst()) {
            return userCursor.getString(0);
        }

        userCursor.close();

        return null;
    }

    public String getTypeCall(int id_type) {
        Cursor userCursor;
        userCursor =  db.rawQuery("select " + COLUMN_NAME_TYPE + " from " + DatabaseHelper.TABLE_TYPE_CALL + " where " + COLUMN_ID + "=" + id_type, null);

        if (userCursor.moveToFirst()) {
            return userCursor.getString(0);
        }

        userCursor.close();

        return null;
    }

    public String getStatusCall(int id_status) {
        Cursor userCursor;
        userCursor =  db.rawQuery("select " + COLUMN_NAME_STATUS + " from " + DatabaseHelper.TABLE_STATUS_CALL + " where " + COLUMN_ID + "=" + id_status, null);

        if (userCursor.moveToFirst()) {
            return userCursor.getString(0);
        }
        userCursor.close();
        return null;
    }

    @SuppressLint("Range")
    public void getCalls() {
        MainActivity.calls = new ArrayList<>();
        CallForList call;

        int id_call;
        String date = null;
        String time = null;
        String allTime = null;
        int id_type = -1;
        int id_status = -1;
        int id_contact = -1;
        String number = null;
        int id_conf = 0;

        Cursor userCursor;
        userCursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CALLS_USERS + " JOIN "
                + TABLE_CALLS  + " ON " + TABLE_CALLS_USERS + "." + COLUMN_ID_CALL + " = " + TABLE_CALLS + "." + COLUMN_ID +
                " ORDER BY " + TABLE_CALLS + "." + COLUMN_DATE + " DESC," + TABLE_CALLS + "." + COLUMN_TIME + " DESC", null);


        LinkedHashMap<Integer, ArrayList<CallForList>> groupedCalls;
        groupedCalls = new LinkedHashMap<>();

        if (userCursor.moveToFirst()) {
            do {
                long id_ = userCursor.getInt(userCursor.getColumnIndex(COLUMN_ID));
                id_contact = userCursor.getInt(userCursor.getColumnIndex(COLUMN_ID_CONTACT));
                number = userCursor.getString(userCursor.getColumnIndex(COLUMN_URI));
                id_call = userCursor.getInt(userCursor.getColumnIndex(COLUMN_ID_CALL));

                Cursor userCursor3;
                userCursor3 = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CALLS + " WHERE " + COLUMN_ID + " = " + id_call, null);

                if (userCursor3 != null && userCursor3.moveToFirst()) {
                    do {
                        date = userCursor3.getString(userCursor3.getColumnIndex(COLUMN_DATE));
                        time = userCursor3.getString(userCursor3.getColumnIndex(COLUMN_TIME));
                        allTime = userCursor3.getString(userCursor3.getColumnIndex(COLUMN_TIME_CALL));
                        id_type = userCursor3.getInt(userCursor3.getColumnIndex(COLUMN_TYPE));
                        id_status = userCursor3.getInt(userCursor3.getColumnIndex(COLUMN_STATUS));
                        id_conf = userCursor3.getInt(userCursor3.getColumnIndex(COLUMN_ID_CONF));

                    } while (userCursor3.moveToNext());
                }
                userCursor3.close();

                if (id_contact != -1) {
                    call = new CallForList(id_call, getNameContact((int) id_contact), getTypeCall(id_type), date, time, allTime, getStatusCall(id_status), number);
                }
                else {
                    call = new CallForList(id_call, null, getTypeCall(id_type), date, time, allTime, getStatusCall(id_status), number);
                }

                MainActivity.calls.add(call);

                if (id_conf != 0) {
                    if (groupedCalls.containsKey(id_conf)) {
                        groupedCalls.get(id_conf).add(call);
                    }
                    else {
                        ArrayList<CallForList> list = new ArrayList<>();
                        list.add(call);
                        groupedCalls.put(id_conf, list);
                    }
                }
                else {
                    ArrayList<CallForList> list = new ArrayList<>();
                    list.add(call);
                    groupedCalls.put(id_call, list);
                }
            } while (userCursor.moveToNext());
        }
        userCursor.close();

        MainActivity.groupedCalls = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<CallForList>> entry : groupedCalls.entrySet()) {
            ArrayList<CallForList> values = entry.getValue();
            MainActivity.groupedCalls.add(values);
        }

        stopConnect();
    }

    public void deleteCalls(ArrayList<ArrayList<CallForList>> list) {
        db = getWritableDatabase();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).size() == 1) {
                db.delete(TABLE_CALLS_USERS, COLUMN_ID_CALL + "=" + list.get(i).get(0).getIdCall(), null);
                db.delete(TABLE_CALLS, COLUMN_ID + "=" + list.get(i).get(0).getIdCall(), null);
            }
            else {
                for (int j = 0; j < list.get(i).size(); j++) {
                    Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONF + " WHERE " + COLUMN_ID + " = " + list.get(i).get(j).getIdCall(), null);
                    if (cursor.getCount() > 0){
                        db.delete(TABLE_CONF, COLUMN_ID + "=" + list.get(i).get(j).getIdCall(), null);
                    }
                    db.delete(TABLE_CALLS_USERS, COLUMN_ID_CALL + "=" + list.get(i).get(j).getIdCall(), null);
                    db.delete(TABLE_CALLS, COLUMN_ID + "=" + list.get(i).get(j).getIdCall(), null);
                }
            }
        }

        stopConnect();
    }

    public void stopConnect() {
        db.close();
    }
}
