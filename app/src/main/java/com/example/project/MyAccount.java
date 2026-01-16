package com.example.project;
import static com.example.project.MainActivity.URL_SERVER;
import static com.example.project.MainActivity.account;
import static com.example.project.MainActivity.confCalls;
import static com.example.project.MainActivity.confCallsList;
import static com.example.project.MainActivity.confContacts;
import static com.example.project.MainActivity.confContactsList;
import static com.example.project.MainActivity.currentCall;
import static com.example.project.MainActivity.isCall;
import static com.example.project.MainActivity.isConfStart;
import static com.example.project.MainActivity.statusCalls;
import static com.example.project.MainActivity.statusConfs;
import static com.example.project.MyCall.newCall;
import android.content.Context;
import android.os.Message;
import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.Buddy;
import org.pjsip.pjsua2.BuddyConfig;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.IntVector;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.SendInstantMessageParam;
import org.pjsip.pjsua2.SrtpOpt;
import org.pjsip.pjsua2.pjmedia_srtp_keying_method;
import org.pjsip.pjsua2.pjmedia_srtp_use;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyAccount extends Account {
    private boolean keepAlive = true;
    public static ArrayList<Contact> confUri;
    public MyAccount() {}

    public void createAccountServer(String name, String password){
        AccountConfig accCfg = new AccountConfig();
        accCfg.setIdUri("sips:" + name + "@" + URL_SERVER);
        accCfg.getRegConfig().setRegistrarUri("sips:" + URL_SERVER + ";transport=tls");
        AuthCredInfo cred = new AuthCredInfo("digest", "*", name, 0, password);
        accCfg.getSipConfig().getAuthCreds().add(cred);
        accCfg.getNatConfig().setSipStunUse(0);
        accCfg.getNatConfig().setTurnEnabled(false);
        accCfg.getRegConfig().setTimeoutSec(20);
        accCfg.getNatConfig().setIceAlwaysUpdate(true);

        SrtpOpt opt = new SrtpOpt();
        IntVector optVector = new IntVector();
        optVector.add(pjmedia_srtp_keying_method.PJMEDIA_SRTP_KEYING_DTLS_SRTP);
        optVector.add(pjmedia_srtp_keying_method.PJMEDIA_SRTP_KEYING_SDES);
        opt.setKeyings(optVector);

        accCfg.getMediaConfig().setSrtpUse(pjmedia_srtp_use.PJMEDIA_SRTP_MANDATORY);
        accCfg.getMediaConfig().setSrtpOpt(opt);
        accCfg.getMediaConfig().setRtcpMuxEnabled(true);
        accCfg.getMediaConfig().setSrtpSecureSignaling(2);

        try {
            create(accCfg);
        } catch (Exception e) {
        }
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        System.out.println("Registration status: " + prm.getCode() + " " + prm.getReason());
        if (prm.getCode() / 100 == 2) {
            if (keepAlive) {
                System.out.println("MyAccount Updating registration timeout");
            }
        }
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        super.onIncomingCall(prm);
        if (isCall) {
            currentCall = new MyCall(account, prm.getCallId());
            try {
                currentCall.answerCall();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!isConfStart) {
            IncomingCallActivity.isIncoming = true;

            if (currentCall == null) {
                currentCall = new MyCall(this, prm.getCallId());
                String uri = getCallerUri(prm.getRdata().getWholeMsg());
                Contact contact = findContact(uri);

                Message msg = ServiceCallbackCall.handler.obtainMessage();
                msg.what = MSG_TYPE.INCOMING_CALL;

                if (contact != null) {
                    if (confCalls == null) {
                        confCalls = new ArrayList<>();
                    }

                    if (confContacts != null) {
                        newCall = new CallStructure(null, CallStructure.outCall, null);
                    } else {
                        newCall = new CallStructure(contact.getName(), CallStructure.inCall, uri);
                    }
                    ServiceCallbackCall.setCurrentCallName(contact.getName());
                } else {
                    if (confContacts != null) {
                        if (confCallsList == null) {
                            confCallsList = new ArrayList<>();
                        }

                        confCalls.add(currentCall);
                        confCallsList.add(currentCall);
                        newCall = new CallStructure(null, CallStructure.outCall, null);
                    } else {
                        newCall = new CallStructure(null, CallStructure.inCall, uri);
                    }
                    ServiceCallbackCall.setCurrentCallName(uri);
                }

                if (confUri == null) {
                    confUri = new ArrayList<>();
                }
                confUri.add(contact);

                ServiceCallbackCall.handler.sendMessage(msg);
            }
        }
        else {
            MyCall call = new MyCall(this, prm.getCallId());
            String uri = getCallerUri(prm.getRdata().getWholeMsg());

            Contact contact = findContact(uri);

            for (int i = 0; i < confContacts.size(); i++) {
                if (contact.getNumber().equals(confContacts.get(i).getNumber())) {
                    confCalls.set(i, call);
                    try {
                        call.answerCall();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
        }
    }

    private String getCallerUri(String sipHeader) {
        String recordRouteHeader = null;
        Pattern pattern = Pattern.compile("From: ([^\\n]+)\\n");
        Matcher matcher = pattern.matcher(sipHeader);
        if (matcher.find()) {
            recordRouteHeader = matcher.group(1);
        }
        String[] parts = recordRouteHeader.split(":|@");
        System.out.println(sipHeader);
        String caller = parts[1];
        return caller;
    }

    private Contact findContact(String number) {
        for (int i = 0; i < MainActivity.contacts.size(); i++) {
            Contact contact = MainActivity.contacts.get(i);
            if (contact.getNumber().equals(number)) {
                return contact;
            }
        }
        return null;
    }

    @Override
    public void onInstantMessage(OnInstantMessageParam prm) {
        String message = prm.getMsgBody();
        String fromUri = prm.getFromUri();

        if (message.startsWith("Conf:")) {
            String searchString = "Conf:";
            message = message.substring(searchString.length());

            String[] contacts = message.split("\n");
            List<String> contactsList = Arrays.asList(contacts);
            confContacts = new ArrayList<>();
            statusConfs = new ArrayList<>();
            statusConfs.add(true);

            if (confContactsList == null) {
                confContactsList = new ArrayList<>();
            }

            for (int i = 0; i < contactsList.size(); i++) {
                String phoneNumber = contactsList.get(i);
                Contact contact;

                if (phoneNumber.equals(MainActivity.phoneUser)) {
                    String originalString = fromUri;
                    String extractedString = originalString.substring(originalString.indexOf(":") + 1, originalString.indexOf("@"));
                    contact = findContact(extractedString);
                    if (contact == null){
                        contact = new Contact(extractedString, extractedString);
                    }
                    confContacts.add(contact);
                }
                else {
                    contact = findContact(phoneNumber);
                    if (contact == null) {
                        contact = new Contact(phoneNumber, phoneNumber);
                    }
                    confContacts.add(contact);
                }
            }
            confContactsList.add(confContacts);
        }

        if (message.startsWith("STATUS_CALLS:")) {
            String searchString = "STATUS_CALLS:";
            message = message.substring(searchString.length());

            String[] calls = message.split("\n");
            List<String> callsList = Arrays.asList(calls);
            statusCalls = new ArrayList<>();
            //statusCalls.add(true);

            for (int i = 0; i < callsList.size(); i++) {
                String status = callsList.get(i);

                System.out.println("ST " + status);
                if (status.equals("true")) {
                    statusCalls.add(true);
                }
                else {
                    statusCalls.add(false);
                }
            }
        }

        if (message.startsWith("DELETE:")) {
            String searchString = "DELETE:";
            message = message.substring(searchString.length());
            int position = Integer.parseInt(message);
            confContacts.remove(position);
            statusCalls.remove(position);
        }

        if (message.startsWith("ADD:")) {
            String searchString = "ADD:";
            message = message.substring(searchString.length());
            String phoneNumber = message;
            Contact contact;
            contact = findContact(phoneNumber);
            if (contact == null) {
                contact = new Contact(phoneNumber, phoneNumber);
            }
            confContacts.add(contact);
            statusCalls.add(true);
        }

        if (message.startsWith("ACTIVE:")) {
            String input = message;
            String[] parts = input.split(":");
            String value = parts[1];
            String phoneNumber = parts[2];

            if (value.equals("false")) {
                for (int i = 0; i < confContactsList.size(); i++) {
                    for (int j = 0; j < confContactsList.get(i).size(); j++) {
                        if (confContactsList.get(i).get(j).getNumber().equals(phoneNumber)) {
                            statusConfs.add(i, false);
                        }
                        confContactsList.remove(i);
                        confCallsList.remove(i);
                        System.out.println("REMOVE " + confCallsList + " " + confContactsList);
                        break;
                    }
                }
                if (confContactsList.size() == 0) {
                    confContactsList = null;
                    confCallsList = null;
                    statusConfs = null;
                }
            }
        }

        if (message.equals("adduser")) {
            String originalString = fromUri;
            String extractedString = originalString.substring(originalString.indexOf(":") + 1, originalString.indexOf("@"));
            Contact contact = findContact(extractedString);
            if (contact == null){
                contact = new Contact(extractedString, extractedString);
            }
            for (int i = 0; i < confCalls.size(); i++) {
                if (confContacts.get(i).getNumber().equals(contact.getNumber())) {
                    CallOpParam callOpParam = new CallOpParam(true);
                    try {
                        MyCall call = new MyCall(account, -1);
                        MyCall.isAddContactConf = true;
                        call.makeCall(originalString, callOpParam);
                        confCalls.set(i, call);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public void sendMessageCalls(String to, String message) throws Exception {
        SendInstantMessageParam prm = new SendInstantMessageParam();
        prm.setContent(message);
        BuddyConfig buddyConfig = new BuddyConfig();
        buddyConfig.setUri(to);
        Buddy buddy = new Buddy();
        buddy.create(account, buddyConfig);
        buddy.sendInstantMessage(prm);
        buddy.delete();
    }

    public void sendMessageConf(String to, String message) throws Exception {
        SendInstantMessageParam prm = new SendInstantMessageParam();
        prm.setContent(message);
        BuddyConfig buddyConfig = new BuddyConfig();
        buddyConfig.setUri(to);
        Buddy buddy = new Buddy();
        buddy.create(account, buddyConfig);
        buddy.sendInstantMessage(prm);
        buddy.delete();
    }

    public void sendMessageDeleteContact(String to, String message) throws Exception {
        SendInstantMessageParam prm = new SendInstantMessageParam();
        prm.setContent(message);
        BuddyConfig buddyConfig = new BuddyConfig();
        buddyConfig.setUri(to);
        Buddy buddy = new Buddy();
        buddy.create(account, buddyConfig);
        buddy.sendInstantMessage(prm);
        buddy.delete();
    }

    public void sendMessageAddContact(String to, String message) throws Exception {
        SendInstantMessageParam prm = new SendInstantMessageParam();
        prm.setContent(message);
        BuddyConfig buddyConfig = new BuddyConfig();
        buddyConfig.setUri(to);
        Buddy buddy = new Buddy();
        buddy.create(account, buddyConfig);
        buddy.sendInstantMessage(prm);
        buddy.delete();
    }

    public void sendMessageActive(String to, String message) throws Exception {
        SendInstantMessageParam prm = new SendInstantMessageParam();
        prm.setContent(message);
        BuddyConfig buddyConfig = new BuddyConfig();
        buddyConfig.setUri(to);
        Buddy buddy = new Buddy();
        buddy.create(account, buddyConfig);
        buddy.sendInstantMessage(prm);
        buddy.delete();
    }

    public void sendAddUser(String to, String message) throws Exception {
        isCall = true;
        SendInstantMessageParam prm = new SendInstantMessageParam();
        prm.setContent(message);
        BuddyConfig buddyConfig = new BuddyConfig();
        buddyConfig.setUri(to);
        Buddy buddy = new Buddy();
        buddy.create(account, buddyConfig);
        buddy.sendInstantMessage(prm);
        buddy.delete();
    }
}
