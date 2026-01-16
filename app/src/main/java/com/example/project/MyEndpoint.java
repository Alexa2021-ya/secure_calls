package com.example.project;
import static com.example.project.MainActivity.account;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.TlsConfig;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pjsip_ssl_method;
import org.pjsip.pjsua2.pjsip_transport_type_e;

public class MyEndpoint extends Endpoint {
    public MyEndpoint() {
        super();
        this.initializeEndpoint();
    }

    public void initializeEndpoint() {
        try {
            libCreate();
            EpConfig epConfig = new EpConfig();
            libInit(epConfig);

            TlsConfig tlsConfig = new TlsConfig();
            tlsConfig.setMethod(pjsip_ssl_method.PJSIP_TLSV1_2_METHOD);
            tlsConfig.setVerifyServer(false);

            TransportConfig transportConfig = new TransportConfig();
            transportConfig.setPort(0);
            transportConfig.setTlsConfig(tlsConfig);
            transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS, transportConfig);

            libHandleEvents(1000);
            libStart();

            account = new MyAccount();
            account.createAccountServer(MainActivity.phoneUser, MainActivity.phoneUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
