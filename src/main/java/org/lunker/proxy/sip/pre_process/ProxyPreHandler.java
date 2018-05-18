package org.lunker.proxy.sip.pre_process;

import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipRequest;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipResponse;
import org.lunker.proxy.core.Message;
import org.lunker.proxy.core.ProcessState;
import org.lunker.proxy.core.ProxyHandler;
import org.lunker.proxy.util.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;

/**
 * Created by dongqlee on 2018. 5. 1..
 */
public class ProxyPreHandler implements ProxyHandler {
    private Logger logger= LoggerFactory.getLogger(ProxyPreHandler.class);

    @Override
    public Message handle(Message message) {
        if(message.getProcessState() != ProcessState.PRE) {
            //TODO: State check 공통화
            return message;
        }

        if(logger.isDebugEnabled())
            logger.debug("In ProxyPreHandler");

        validateRequest(message);

        if(message.getValidation().isValidate())
            message.setProcessState(ProcessState.IN);
        else
            message.setProcessState(ProcessState.POST);

        return message;
    }

    /**
     * Validate sip request according to rfc 3261 section 16.3
     */
    public Message validateRequest(Message message){
        checkSipUriScheme(message)
                .checkMaxForwards(message)
                .checkRequestLoop(message);

        return message;
    }

    private ProxyPreHandler checkSipUriScheme(Message message){
        if(message.getOriginalMessage() instanceof DefaultSipRequest) {
            if (!((DefaultSipRequest) message.getOriginalMessage()).getRequestURI().isSipURI()) {
                try {
                    // Publish 416 Unsupported_URI_Scheme
                    DefaultSipResponse unsupportedUriSchemeResponse = ((DefaultSipRequest) message.getOriginalMessage()).createResponse(SIPResponse.UNSUPPORTED_URI_SCHEME);

                    message.invalidate(SIPResponse.UNSUPPORTED_URI_SCHEME, "", unsupportedUriSchemeResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return this;
    }

    /**
     * Check Max-Forwards Header
     * @param message
     * @return
     */
    private ProxyPreHandler checkMaxForwards(Message message){
        MaxForwardsHeader maxForwardsHeader=message.getOriginalMessage().getMaxForwards();

        if(maxForwardsHeader==null){
//            // TODO: Null Check with better approach
//            // TODO: Invalidate with 500 Server Internal Error
//            message.getValidation().setValidate(false);
            return this;
        }
        else{
            int maxForwards=0;
            maxForwards=maxForwardsHeader.getMaxForwards();

            if(maxForwards == 0){
                if(SIPRequest.INFO.equals(message.getOriginalMessage().getMethod())){

//                    message.getValidation().setValidate(false);

                    // TOOD: 올바른 response발행
                }
                else{
                    // ?
                }
            }
            else if(maxForwards < 0){
                // TODO: create 483 response
                try{
                    DefaultSipResponse tooManyHopsResponse=((DefaultSipRequest)message.getOriginalMessage()).createResponse(SIPResponse.TOO_MANY_HOPS);
                    message.invalidate(SIPResponse.TOO_MANY_HOPS, "", tooManyHopsResponse);
                }
                catch (Exception e){
                    e.printStackTrace();
                    // TODO: create 500 ServerInternal Error Response common
                }
            }
        }

        return this;
    }

    /**
     * Check whether received sip request is looped
     * @param message
     * @return
     */
    private ProxyPreHandler checkRequestLoop(Message message){
        String branch=message.getOriginalMessage().getTopmostVia().getBranch();
        String expectedBranch="";

        // generate branch value
        expectedBranch= ProxyHelper.generateBranch(message.getOriginalMessage());

        if(branch.equals(expectedBranch)){
            // Loop detect
            try{
                DefaultSipResponse loopDetectedResponse=((DefaultSipRequest)message.getOriginalMessage()).createResponse(SIPResponse.LOOP_DETECTED);
                message.invalidate(SIPResponse.LOOP_DETECTED, "", loopDetectedResponse);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        return this;
    }

    // TODO
    private ProxyPreHandler checkProxyRequire(Message message){
        Header proxyRequireHeader=message.getOriginalMessage().getHeader("Proxy-Require");

        if(proxyRequireHeader==null){

        }
        else{

        }

        return this;
    }

    // TODO
    private ProxyPreHandler checkProxyAuthorization(Message message){
        Header proxyAuthorizationHeader=message.getOriginalMessage().getHeader("Proxy-Authorization");

        if(proxyAuthorizationHeader==null){

            return this;
        }
        else{
            // TODO: check auth
        }

        return this;
    }
}
