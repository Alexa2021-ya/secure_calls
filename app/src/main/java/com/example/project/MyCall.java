package com.example.project;
import static com.example.project.CallActivity.seconds;
import static com.example.project.MainActivity.account;
import static com.example.project.MainActivity.confCalls;
import static com.example.project.MainActivity.confCallsList;
import static com.example.project.MainActivity.confContacts;
import static com.example.project.MainActivity.confContactsList;
import static com.example.project.MainActivity.currentCall;
import static com.example.project.MainActivity.ep;
import static com.example.project.MainActivity.isCall;
import static com.example.project.MainActivity.isConfStart;
import static com.example.project.MainActivity.statusCalls;
import static org.pjsip.pjsua2.pjmedia_type.PJMEDIA_TYPE_AUDIO;
import static org.pjsip.pjsua2.pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED;
import static org.pjsip.pjsua2.pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED;
import static org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_OK;
import android.os.Bundle;
import android.os.Message;
import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.pjsip_status_code;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MyCall extends Call {
    public static CallStructure newCall;
    public static ArrayList<AudioMedia> listAM;
    private Long startTimeCall;
    private Long stopTimeCall;
    public static boolean isAddContactConf = false;
    public static boolean isConfirmed = false;
    public MyCall(Account acc, int call_id){
        super(acc, call_id);
    }

    public void answerCall() throws Exception {
        CallOpParam prm = new CallOpParam();
        prm.setStatusCode(PJSIP_SC_OK);
        answer(prm);
    }
    public void hangupCall() throws Exception {
        CallOpParam prm = new CallOpParam();
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
        if (isActive()) {
            hangup(prm);
        }
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        super.onCallState(prm);
        CallInfo ci = null;
        try {
            ci = getInfo();
            if (ci.getState() == PJSIP_INV_STATE_DISCONNECTED) {
                if (confContacts == null || confContacts.size() == 0) {
                    MyAccount.confUri = null;
                    currentCall = null;

                    CallActivity.stopTimer();

                    if (startTimeCall != null) {
                        stopTimeCall = System.currentTimeMillis();
                        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                        String time = sdf.format(new Date(stopTimeCall - startTimeCall));

                        newCall.allTime = time;
                    }
                    else {
                        newCall.allTime = String.valueOf(0);
                    }
                    if (newCall.isStatus == null) {
                        newCall.isStatus = CallStructure.statusNo;
                    }

                    stopTimeCall = null;
                    startTimeCall = null;

                    Message msg = ServiceCallbackCall.handler.obtainMessage();
                    msg.what = MSG_TYPE.CALL_DISCONNECTED;
                    ServiceCallbackCall.handler.sendMessage(msg);
                    isConfirmed = false;

                    seconds = 0;
                }
                else {
                    currentCall = null;
                    int m = 0;
                    for (int i = 0; i < MainActivity.confCalls.size(); i++) {
                            if (!confCalls.get(i).isActive()) {
                                m += 1;
                            }
                    }

                    if (m == confCalls.size()) {
                        if (startTimeCall != null) {
                            stopTimeCall = System.currentTimeMillis();

                            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                            String time = sdf.format(new Date(stopTimeCall - startTimeCall));

                            newCall.allTime = time;
                        }
                        else {
                            newCall.allTime = String.valueOf(0);
                        }

                        if (newCall.isStatus == null) {
                            newCall.isStatus = CallStructure.statusNo;
                        }

                        stopTimeCall = null;
                        startTimeCall = null;


                        Message msg = ServiceCallbackCall.handler.obtainMessage();
                        msg.what = MSG_TYPE.CALL_DISCONNECTED;
                        ServiceCallbackCall.handler.sendMessage(msg);

                        listAM = null;

                        if (statusCalls != null) {
                            statusCalls = null;
                        }
                        isConfirmed = false;

                        if (isConfStart) {
                            for (int i = 0; i < confContacts.size(); i++) {
                                Contact contact = confContacts.get(i);
                                String uri = "sips:" + contact.getNumber() + "@" + MainActivity.URL_SERVER + ";transport=tls";
                                String message1 = "ACTIVE:false:" + MainActivity.phoneUser;
                                try {
                                    account.sendMessageActive(uri, message1);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        isConfStart = false;
                        seconds = 0;
                    }
                }
            }
            else if (ci.getState() == PJSIP_INV_STATE_CONFIRMED){
                System.out.println("Обработка состояния установленного соединения (звонок успешно установлен)");
                if (!isConfirmed && !isCall) {
                    if (seconds == 0) {
                        //Время звонка начало отсчёта
                        startTimeCall = System.currentTimeMillis();

                        CallActivity.startTimer();

                        newCall.isStatus = CallStructure.statusOk;

                        CallActivity.isCallStart = true;
                        Message msg = ServiceCallbackCall.handler.obtainMessage();
                        msg.what = MSG_TYPE.CALL_CONFIRMED;
                        ServiceCallbackCall.handler.sendMessage(msg);
                        isConfirmed = true;
                    }
                }

                if (isCall) {
                    Message msg = ServiceCallbackCall.handler.obtainMessage();
                    msg.what = MSG_TYPE.CALL_CONFIRMED;
                    ServiceCallbackCall.handler.sendMessage(msg);
                }
            }
        } catch (Exception e) {
            if (currentCall != null) {
                currentCall.delete();
                currentCall = null;
            }

            if (MainActivity.confCalls.size() != 0) {
                MainActivity.confCalls.clear();
                MainActivity.confContacts.clear();
            }
        }
    }

    public void makeCallSip(String number, String name) {
        if (currentCall == null) {
            try {
                CallOpParam callOpParam = new CallOpParam(true);
                String uri = "sips:" + number + "@" + MainActivity.URL_SERVER + ";transport=tls";
                currentCall = new MyCall(account, -1);
                currentCall.makeCall(uri, callOpParam);

                newCall = new CallStructure(name, CallStructure.outCall, number);

                OutgoingCallActivity.isOut = true;

                Message msg = ServiceCallbackCall.handler.obtainMessage();
                msg.what = MSG_TYPE.CALL;
                Bundle data = new Bundle();

                if (name != null) {
                    ServiceCallbackCall.setCurrentCallName(name);
                }
                else {
                    ServiceCallbackCall.setCurrentCallName(number);
                }

                msg.setData(data);
                ServiceCallbackCall.handler.sendMessage(msg);

                System.out.println("Звонок разрешён");
            } catch (Exception e) {
                if (currentCall != null) {
                    currentCall.delete();
                }
                System.out.println("Ошибка при осуществлении звонка " + e);
            }
        }
    }

    public void makeCallSip(ArrayList<Contact> contacts) throws Exception {
        isConfStart = true;

        if (confCallsList == null) {
            confCallsList = new ArrayList<>();
        }

        if (confContactsList == null) {
            confContactsList = new ArrayList<>();
        }

        confContactsList.add(contacts);



        confCalls = new ArrayList<MyCall>();
        confContacts = new ArrayList<Contact>();
        String message = "Conf:";

        for (Contact contact : contacts) {
            message = message + contact.getNumber() + "\n";
            confContacts.add(contact);
        }

        for (Contact contact : contacts) {
            MyCall call = new MyCall(account, -1);
            CallOpParam callOpParam = new CallOpParam(true);
            String uri = "sips:" + contact.getNumber() + "@" + MainActivity.URL_SERVER + ";transport=tls";
            call.makeCall(uri, callOpParam);

            confCalls.add(call);
            account.sendMessageConf(uri, message);
        }

        newCall = new CallStructure(null, CallStructure.outCall, null);

        Message msg = ServiceCallbackCall.handler.obtainMessage();
        msg.what = MSG_TYPE.CALL;
        Bundle data = new Bundle();
        msg.setData(data);
        ServiceCallbackCall.handler.sendMessage(msg);

        OutgoingCallActivity.isOut = true;
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ep.libRegisterThread(Thread.currentThread().getName());
                    CallInfo callInfo1 = getInfo();
                    AudioMedia audioMedia1 = null;

                    for (int i = 0; i < callInfo1.getMedia().size(); i++) {
                        if (callInfo1.getMedia().get(i).getType() == PJMEDIA_TYPE_AUDIO) {
                            audioMedia1 = (AudioMedia) getAudioMedia(i);
                            break;
                        }
                    }

                    if (audioMedia1 != null) {
                        ep.audDevManager().getCaptureDevMedia().startTransmit(audioMedia1);
                        audioMedia1.startTransmit(MainActivity.ep.audDevManager().getPlaybackDevMedia());
                    }

                    if (audioMedia1 != null) {
                        if (isConfStart) {
                            for (Call call : confCalls) {
                                if (call.isActive()) {
                                    AudioMedia audioMedia = null;
                                    CallInfo callInfo = call.getInfo();

                                    for (int j = 0; j < callInfo.getMedia().size(); j++) {
                                        if (callInfo.getMedia().get(j).getType() == PJMEDIA_TYPE_AUDIO) {
                                            audioMedia = (AudioMedia) call.getAudioMedia(j);
                                            break;
                                        }
                                    }

                                    if (audioMedia != null) {
                                        audioMedia1.startTransmit(audioMedia);
                                        audioMedia.startTransmit(audioMedia1);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
        }).start();
    }
}
