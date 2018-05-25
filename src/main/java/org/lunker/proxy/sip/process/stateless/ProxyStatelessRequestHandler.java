package org.lunker.proxy.sip.process.stateless;

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.header.Via;
import org.lunker.new_proxy.model.ServerInfo;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipRequest;
import org.lunker.new_proxy.sip.wrapper.message.proxy.ProxySipRequest;
import org.lunker.proxy.core.Message;
import org.lunker.proxy.core.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dongqlee on 2018. 5. 24..
 */
public class ProxyStatelessRequestHandler implements ProxyHandler {
    private Logger logger= LoggerFactory.getLogger(ProxyStatelessRequestHandler.class);
    private ServerInfo serverInfo=null;
    private Via proxyVia=null;

    public ProxyStatelessRequestHandler(ServerInfo serverInfo) {
        this.serverInfo=serverInfo;
        this.proxyVia=generateProxyVia(serverInfo);
    }

    @Override
    public Message handle(Message message) {
        copyRequest(message);
        updateRequestURI(message);
        updateMaxForwards(message);
        addRecordRouteHeader(message);

        return message;
    }

    /**
     * 1) copy request
     * 2) update request-uri
     * 3) update max-forwards
     * 4) optionally add record-route
     * 5) optionally add additional header
     * 6) postprocess routing information
     * 7) determine next hop ip, port,
     * 8) add via header
     * 9) add content-length
     * 10) forward new request
     * 11) set timer c
     */
    private void copyRequest(Message message){
        message.setNewMessage((ProxySipRequest) message.getOriginalMessage().clone());
    }

    private void updateRequestURI(Message message){
        // none
    }

    private void updateMaxForwards(Message message){
        ((DefaultSipRequest)message.getNewMessage()).decrementMaxForwards();
    }

    private void addRecordRouteHeader(Message message){
        /*
        <sip:203.240.153.30:5061;transport=tcp;lr;node_host=203.240.153.12;node_port=5080;version=0>
         */
        ProxySipRequest proxySipRequest=(ProxySipRequest) message.getNewMessage();

        RecordRoute recordRoute=new RecordRoute();
        AddressImpl address=new AddressImpl();
        SipUri sipUri=new SipUri();

        try{
            // create sip-uri
            sipUri.setTransportParam("tcp");
            sipUri.setHost(this.serverInfo.getHost());
            sipUri.setPort(this.serverInfo.getPort());

            address.setAddess(sipUri);

            // create record-route
            recordRoute.setAddress(address);
            recordRoute.setParameter("lr","");
            recordRoute.setParameter("node_host", this.serverInfo.getHost());
            recordRoute.setParameter("node_port", this.serverInfo.getPort()+"");

            proxySipRequest.addHeader(recordRoute);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void postProcessRoutingInformation(Message message){

    }

    private void determineNextHop(Message message){

    }

    private void addViaHeader(Message message){
        if(logger.isDebugEnabled())
            logger.debug("Add via header");

        ((ProxySipRequest)message.getNewMessage()).addVia(this.proxyVia);
    }

    private Via generateProxyVia(ServerInfo serverInfo){
        Via via=new Via();
        try{

            via.setHost(serverInfo.getHost());
            via.setPort(serverInfo.getPort());
            via.setReceived(serverInfo.getHost());
            via.setTransport(serverInfo.getTransport().getValue());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return via;
    }

    private void addContentLength(){

    }

    private void forwardNewRequest(){

    }

    // TODO: implement on next step, Stateful
    private void setTimerC(){

    }

}
