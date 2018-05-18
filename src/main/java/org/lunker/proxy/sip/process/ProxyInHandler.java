package org.lunker.proxy.sip.process;

import com.google.gson.Gson;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipMessage;
import org.lunker.new_proxy.sip.wrapper.message.proxy.ProxySipRequest;
import org.lunker.new_proxy.sip.wrapper.message.proxy.ProxySipResponse;
import org.lunker.new_proxy.stub.AbstractSIPHandler;
import org.lunker.proxy.core.Message;
import org.lunker.proxy.core.ProcessState;
import org.lunker.proxy.core.ProxyHandler;
import org.lunker.proxy.model.RemoteAddress;
import org.lunker.proxy.registrar.Registrar;
import org.lunker.proxy.registrar.Registration;
import org.lunker.proxy.util.AuthUtil;
import org.lunker.proxy.util.JedisConnection;
import org.lunker.proxy.util.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.MessageFactory;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by dongqlee on 2018. 5. 15..
 */
public class ProxyInHandler implements AbstractSIPHandler, ProxyHandler {
    private Logger logger= LoggerFactory.getLogger(ProxyInHandler.class);

    private javax.sip.SipFactory sipFactory=null;
    private AddressFactory addressFactory=null;
    private HeaderFactory headerFactory=null;
    private MessageFactory messageFactory=null;
    private Registrar registrar=null;
    private JedisConnection jedisConnection=null;
    private Gson gson=null;

    private String host="";
    private int port=10010;

    public ProxyInHandler() {
        jedisConnection=JedisConnection.getInstance();
        gson=new Gson();

        try{
            this.sipFactory=javax.sip.SipFactory.getInstance();
            this.headerFactory=sipFactory.createHeaderFactory();
            this.registrar= Registrar.getInstance();
            this.messageFactory=sipFactory.createMessageFactory();
            this.addressFactory=sipFactory.createAddressFactory();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        host=getHostAddress();
    }

    @Override
    public Message handle(Message message) {
        if(message.getProcessState() != ProcessState.IN)
            return message;

        DefaultSipMessage originalMessage=message.getOriginalMessage();
        DefaultSipMessage newMessage=null;

        // message
        if(originalMessage instanceof ProxySipRequest){
            String method=originalMessage.getMethod();

            if(method.equals(SIPRequest.REGISTER))
                newMessage=this.handleRegister(originalMessage);
            else if (method.equals(SIPRequest.INVITE))
                newMessage=this.handleInvite(originalMessage);
            else if(method.equals(SIPRequest.ACK))
                newMessage=this.handleAck(originalMessage);
            else if(method.equals(SIPRequest.BYE))
                newMessage=this.handleBye(originalMessage);
        }
        else if(originalMessage instanceof ProxySipResponse){
            newMessage=handleResponse(originalMessage);
        }

        message.setNewMessage(newMessage);
        message.setProcessState(ProcessState.POST);

        return message;
    }



    /**
     * Remove top Via
     * @param response
     * @return
     */
    public DefaultSipMessage handleResponse(DefaultSipMessage response){
        ProxySipResponse proxySipResponse=(ProxySipResponse) response;
        Via via=proxySipResponse.getTopmostVia();

        if(via.getHost().equalsIgnoreCase(host)){
            System.out.println("breakpoint");

            proxySipResponse.removeTopVia();
        }
        else{
            logger.warn("Invalid routed sip message. {}\ndrop...", response);
        }

        return response;
    }

    @Override
    public DefaultSipMessage handleInvite(DefaultSipMessage inviteRequest) {
        logger.info("handleInvite");

        ProxySipRequest proxyInviteRequest=null;
        proxyInviteRequest=(ProxySipRequest) inviteRequest;

        String userKey=inviteRequest.getTo().getAddress().getURI().toString().split(":")[1];
        Registration registration=null;

        registration=this.registrar.getRegistration(userKey);
        String displayName=inviteRequest.getTo().getAddress().getDisplayName();

        try{
            URI requestURI=this.addressFactory.createURI("sip:" + displayName + "@" + registration.getRemoteAddress() + ":" + registration.getRemotePort());
            proxyInviteRequest.setRequestURI(requestURI);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return proxyInviteRequest;
    }

    @Override
    public DefaultSipMessage handleCancel(DefaultSipMessage cancelRequest) {

        return cancelRequest;
    }

    @Override
    public DefaultSipMessage handleAck(DefaultSipMessage ackRequest) {
        logger.info("handleAck");

        // send ack to callee
        String aor=ackRequest.getTo().getAddress().getURI().toString().split(":")[1];
        Registration registration=null;

        registration=this.registrar.getRegistration(aor);

        String displayName=ackRequest.getTo().getAddress().getDisplayName();
        try{
            URI requestURI=this.addressFactory.createURI("sip:" + displayName + "@" + registration.getRemoteAddress() + ":" + registration.getRemotePort());
            ((ProxySipRequest)ackRequest).setRequestURI(requestURI);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return ackRequest;
    }

    @Override
    public DefaultSipMessage handleBye(DefaultSipMessage byeRequest) {
        logger.info("handleBye");

        String aor=byeRequest.getTo().getAddress().getURI().toString().split(":")[1];
        Registration registration=null;

        registration=this.registrar.getRegistration(aor);

        String displayName=byeRequest.getTo().getAddress().getDisplayName();
        try{
            URI requestURI=this.addressFactory.createURI("sip:" + displayName + "@" + registration.getRemoteAddress() + ":" + registration.getRemotePort());
            ((ProxySipRequest)byeRequest).setRequestURI(requestURI);
        }
        catch (Exception e){
            e.printStackTrace();
        }


        return byeRequest;
    }

    @Override
    public DefaultSipMessage handleRegister(DefaultSipMessage registerRequest) {
        registerRequest=(ProxySipRequest) registerRequest;
        Header authHeader= registerRequest.getHeader("Authorization");
        String authorization="";
        ProxySipResponse sipResponse=null;

        if(authHeader==null){
            // non-auth
            sipResponse=((ProxySipRequest) registerRequest).createResponse(SIPResponse.UNAUTHORIZED);
            String domain=registerRequest.getFrom().getAddress().getURI().toString().split("@")[1];

            try{
                WWWAuthenticateHeader wwwAuthenticateHeader=this.headerFactory.createWWWAuthenticateHeader("Digest");
                wwwAuthenticateHeader.setAlgorithm("MD5");
                wwwAuthenticateHeader.setQop("auth");
                wwwAuthenticateHeader.setNonce(AuthUtil.getNonce());
                wwwAuthenticateHeader.setRealm(domain);

                sipResponse.addHeader(wwwAuthenticateHeader);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            // do auth
            String aor="";
            String account="";
            String domain="";
            String userKey="";
            String ipPhonePassword="aaaaaa";

            aor=registerRequest.getFrom().getAddress().getURI().toString().split(":")[1];
            account=aor.split("@")[0];
            domain=aor.split("@")[1];

            userKey=aor;// 1단계에서는 OPMD 지원 고려 안하는걸로.

            authorization=authHeader.toString();

            // TODO(lunker): get password from rest
            /*
            HttpService httpService=HttpService.getInstance();

            try{
                JsonObject response=httpService.get("/ims/users/"+aor+"/password", JsonObject.class);
                String status=response.getAsJsonObject("header").get("status").getAsString();
                if (!status.equals("error")) {
                    ipPhonePassword = response.get("body").getAsJsonObject().get("telNoPassword").getAsString();
                }
            }
            catch (Exception e){
                e.printStackTrace();
                // return
            }
            */

            AuthUtil authUtil=new AuthUtil(authorization);
            authUtil.setPassword(ipPhonePassword);

            if(authUtil.isEqualHA()){
                // Auth success
                logger.warn("REGISTER Success");
                sipResponse=((ProxySipRequest) registerRequest).createResponse(SIPResponse.OK);

                //TODO: get first via received & rport
                RemoteAddress clientRemoteAddress=ProxyHelper.getClientRemoteAddress(registerRequest);

                Registration registration=new Registration(userKey, aor,account, domain, clientRemoteAddress.getHost(), clientRemoteAddress.getPort());

                registrar.register(userKey, registration);
//                registrar.register(userKey, registration, ctx);
//                jedisConnection.set(userKey, gson.toJson(registration));
            }
            else{
                logger.warn("REGISTER Fail");
            }
        }

        // fire response
//        this.currentCtx.fireChannelRead(sipResponse);
        return sipResponse;
    }

}
