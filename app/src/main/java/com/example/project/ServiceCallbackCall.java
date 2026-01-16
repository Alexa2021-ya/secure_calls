package com.example.project;
import static com.example.project.MainActivity.account;
import static com.example.project.MainActivity.adapter;
import static com.example.project.MainActivity.confCalls;
import static com.example.project.MainActivity.confCallsList;
import static com.example.project.MainActivity.confContacts;
import static com.example.project.MainActivity.confContactsList;
import static com.example.project.MainActivity.ep;
import static com.example.project.MainActivity.currentCall;
import static com.example.project.MainActivity.isConfStart;
import static com.example.project.MainActivity.statusConfs;
import static com.example.project.MyCall.newCall;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import java.util.ArrayList;

public class ServiceCallbackCall extends Service {
    public static Handler handler = null;
    private static String currentCallName = null;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String CHANNEL_INCOMING_CALL_ID = "IncomingCallChannel";
    private static final String CHANNEL_OUTGOING_CALL_ID = "OutgoingCallChannel";
    private static final String CHANNEL_CALL_ID = "CallChannel";
    public static final int NOTIFICATION_ID = 1;
    public static int NOTIFICATION_INCOMING_CALL_ID = 0;
    public static int NOTIFICATION_OUTGOING_CALL_ID = 0;
    public static int NOTIFICATION_CALL_ID = 0;
    private boolean isRunning = true;
    public  static  AudioManager audioManager;


    @Override
    public void onCreate() {
        System.loadLibrary("pjsua2");
        ep = new MyEndpoint();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkChangeReceiver(), filter);

        handler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_TYPE.CALL) {;
                    startOutgoingCallActivity();
                }
                if (msg.what == MSG_TYPE.INCOMING_CALL) {
                    startIncomingCallActivity();
                }

                if (msg.what == MSG_TYPE.CALL_CONFIRMED) {
                        Message m = handler.obtainMessage();
                        m.what = MSG_TYPE.CALL_CONFIRMED;
                        if (OutgoingCallActivity.isOut) {
                            if (OutgoingCallActivity.handler != null) {
                                OutgoingCallActivity.handler.sendMessage(m);
                            }
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(ServiceCallbackCall.NOTIFICATION_OUTGOING_CALL_ID);
                            OutgoingCallActivity.isOut = false;
                        }
                        startCallActivity();
                }

                if (msg.what == MSG_TYPE.CALL_DISCONNECTED) {
                    ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_NORMAL);
                    ServiceCallbackCall.setCurrentCallName(null);
                    Message m = handler.obtainMessage();
                    m.what = MSG_TYPE.CALL_DISCONNECTED;
                    if (CallActivity.isCallStart) {
                        if (CallActivity.handler != null) {
                            CallActivity.handler.sendMessage(m);
                        }

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager != null) {
                            notificationManager.cancel(ServiceCallbackCall.NOTIFICATION_CALL_ID);
                        }

                        CallActivity.isCallStart = false;

                        m = handler.obtainMessage();
                        m.what = MSG_TYPE.CALL_DISCONNECTED;
                        if (ListContactsCall.handler != null) {
                            ListContactsCall.handler.sendMessage(m);
                        }

                        m = handler.obtainMessage();
                        m.what = MSG_TYPE.CALL_DISCONNECTED;
                        if (AddUserActivity.handler != null) {
                            AddUserActivity.handler.sendMessage(m);
                        }
                    } else if (IncomingCallActivity.isIncoming) {
                        if (IncomingCallActivity.handler != null) {
                            IncomingCallActivity.handler.sendMessage(m);
                        }
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(ServiceCallbackCall.NOTIFICATION_INCOMING_CALL_ID);
                        IncomingCallActivity.isIncoming = false;
                    } else {
                        if (OutgoingCallActivity.handler != null) {
                            OutgoingCallActivity.handler.sendMessage(m);
                        }
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(ServiceCallbackCall.NOTIFICATION_OUTGOING_CALL_ID);
                        OutgoingCallActivity.isOut = false;
                    }


                    if (MainActivity.confContacts != null) {
                        if (newCall != null) {;
                            newCall.addCallConf(getApplicationContext());
                            confContacts = null;
                            if (confCalls != null) {
                                confCalls = null;
                            }
                        }
                    } else {
                        if (newCall != null) {
                            newCall.addCall(getApplicationContext());
                            if (confContacts != null) {
                                confContacts = null;
                            }
                        }
                    }

                    newCall = null;

                    DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
                    databaseHelper.getCalls();

                    adapter.notifyDataSetChanged();
                }
            }
        };
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel(CHANNEL_ID);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("App")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();
        }

        startForeground(NOTIFICATION_ID, notification);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ep.libRegisterThread(Thread.currentThread().getName());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                while (isRunning) {
                    if (confContactsList != null && confCallsList == null) {
                        if (confCallsList == null) {
                            confCallsList = new ArrayList<>();
                        }
                        confCallsList.add(currentCall);
                    }

                    if (isConfStart) {
                        try {
                            String message = "STATUS_CALLS:";
                            if (confCalls != null) {
                                for (int i = 0; i < confCalls.size(); i++) {
                                    Contact contact = confContacts.get(i);
                                    String uri = "sips:" + contact.getNumber() + "@" + MainActivity.URL_SERVER + ";transport=tls";
                                    if (confCalls.get(i).isActive()) {
                                        message = message + confCalls.get(i).isActive() + "\n";
                                        try {
                                            account.sendMessageCalls(uri, message);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        String message1 = "ACTIVE:true:" + MainActivity.phoneUser;
                                        try {
                                            account.sendMessageActive(uri, message1);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                        }
                    }

                    if (confCallsList != null) {
                        try {
                            for (int i = 0; i < confCallsList.size(); i++) {
                                if (!statusConfs.get(i)) {
                                    confCallsList.remove(i);
                                    confContactsList.get(i).remove(i);
                                    MyAccount.confUri.remove(i);
                                }
                            }
                            if (confCallsList.size() == 0) {
                                confContactsList = null;
                                confCallsList = null;
                                statusConfs = null;
                                MyAccount.confUri = null;
                            }
                        }
                        catch (Exception e) {
                        }
                    }


                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        return START_STICKY;
    }

    private void createNotificationChannel(String id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    id,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ForegroundServiceType")
    public void startIncomingCallActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_INCOMING_CALL_ID,
                    "Incoming Call",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);

            Intent notificationIntent = new Intent(this, IncomingCallActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

            Intent notificationIntentAnswer = new Intent(this, NotificationReceiver.class);
            notificationIntentAnswer.setAction("ACTION_ANSWER_CALL");
            PendingIntent pendingIntentAnswer = PendingIntent.getBroadcast(this, 0, notificationIntentAnswer, PendingIntent.FLAG_MUTABLE);

            Intent notificationIntentHangup = new Intent(this, NotificationReceiver.class);
            notificationIntentHangup.setAction("ACTION_HANGUP_CALL");
            PendingIntent pendingIntentHangup = PendingIntent.getBroadcast(this, 0, notificationIntentHangup, PendingIntent.FLAG_IMMUTABLE);


            Notification.Builder builder = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                builder = new Notification.Builder(this, CHANNEL_INCOMING_CALL_ID)
                        .setContentTitle("Входящий вызов")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .addAction(0, "Ответить", pendingIntentAnswer)
                        .addAction(0, "Отклонить", pendingIntentHangup);
            }

            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            NOTIFICATION_INCOMING_CALL_ID = (int)(System.currentTimeMillis()%10000);


            manager.notify(NOTIFICATION_INCOMING_CALL_ID, notification);
        }
    }

    @SuppressLint("ForegroundServiceType")
    public void startOutgoingCallActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_OUTGOING_CALL_ID,
                    "Outgoing Call",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);

            Intent notificationIntent = new Intent(this, OutgoingCallActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

            Intent notificationIntentHangup = new Intent(this, NotificationReceiver.class);
            notificationIntentHangup.setAction("ACTION_HANGUP_CALL");
            PendingIntent pendingIntentHangup = PendingIntent.getBroadcast(this, 0, notificationIntentHangup, PendingIntent.FLAG_IMMUTABLE);


            Notification.Builder builder = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                builder = new Notification.Builder(this, CHANNEL_OUTGOING_CALL_ID)
                        .setContentTitle("Исходящий вызов")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .addAction(0, "Завершить", pendingIntentHangup);
            }

            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            NOTIFICATION_OUTGOING_CALL_ID = (int)(System.currentTimeMillis()%10000);


            manager.notify(NOTIFICATION_OUTGOING_CALL_ID, notification);
        }
    }

    @SuppressLint("ForegroundServiceType")
    public void startCallActivity() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        if (ServiceCallbackCall.audioManager.isMicrophoneMute()) {
            ServiceCallbackCall.audioManager.setMicrophoneMute(false);
        }
        if (ServiceCallbackCall.audioManager.isSpeakerphoneOn()) {
            ServiceCallbackCall.audioManager.setSpeakerphoneOn(false);
        }

        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setMode(AudioManager.MODE_IN_COMMUNICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_CALL_ID,
                    "Call",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);

            Intent notificationIntent = new Intent(this, CallActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

            Intent notificationIntentHangup = new Intent(this, NotificationReceiver.class);
            notificationIntentHangup.setAction("ACTION_HANGUP_CALL");
            PendingIntent pendingIntentHangup = PendingIntent.getBroadcast(this, 0, notificationIntentHangup, PendingIntent.FLAG_IMMUTABLE);


            Notification.Builder builder = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                builder = new Notification.Builder(this, CHANNEL_CALL_ID)
                        .setContentTitle("Текущий вызов")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .addAction(0, "Завершить", pendingIntentHangup);
            }

            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            NOTIFICATION_CALL_ID = (int)(System.currentTimeMillis()%10000);


            manager.notify(NOTIFICATION_CALL_ID, notification);
        }
    }

    public static void setCurrentCallName(String name) {
        currentCallName = name;
    }

    public static String getCurrentCallName() {
        return currentCallName;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        account.delete();
        ep.delete();
        currentCall = null;
        handler = null;
        isRunning = false;
    }

    public static void deleteNotification(int id, NotificationManager notificationManager) {
        notificationManager.cancel(id);
        id = 0;
        System.out.println(ServiceCallbackCall.NOTIFICATION_OUTGOING_CALL_ID);
    }
}


