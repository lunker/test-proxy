package org.lunker.proxy.sip;

import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import org.lunker.new_proxy.model.ServerInfo;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipMessage;
import org.lunker.new_proxy.sip.wrapper.message.proxy.ProxySipRequest;
import org.lunker.new_proxy.stub.SipMessageHandler;
import org.lunker.proxy.core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by dongqlee on 2018. 5. 31..
 */
public class DiscoveryServer extends SipMessageHandler {
    private Logger logger= LoggerFactory.getLogger(DiscoveryServer.class);

    public DiscoveryServer(ServerInfo serverInfo) {
        super(serverInfo);
    }

    @Override
    public void handle(Optional<DefaultSipMessage> maybeDefaultSipMessage) {
        maybeDefaultSipMessage.map((defaultSipMessage)->{
            Message message=new Message(defaultSipMessage);

            String method=defaultSipMessage.getMethod();

            if(SIPRequest.INFO.equals(method)){
                // Info


                // Send Response to Info

            }
            else if(SIPRequest.OPTIONS.equals(method)){
                // Options
                // Broadcast proxy info

            }

            return 1;
        });
    }

    /**
     * Broadcasting proxy server info
     */
    private Message handleOption(Message message){
        ProxySipRequest proxySipRequest=(ProxySipRequest) message.getOriginalMessage();
        proxySipRequest.createResponse(SIPResponse.OK);

        return message;
    }

    private Message handleInfo(Message message){

        return message;
    }


}
