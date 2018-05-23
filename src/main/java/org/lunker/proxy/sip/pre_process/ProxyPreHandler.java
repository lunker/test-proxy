package org.lunker.proxy.sip.pre_process;

import gov.nist.javax.sip.header.Via;
import org.lunker.new_proxy.model.ServerInfo;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipRequest;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipResponse;
import org.lunker.proxy.core.Message;
import org.lunker.proxy.core.ProcessState;
import org.lunker.proxy.core.ProxyHandler;
import org.lunker.proxy.sip.pre_process.request.RequestTargetDetector;
import org.lunker.proxy.sip.pre_process.request.RequestValidator;
import org.lunker.proxy.sip.pre_process.request.RoutePreprocessor;
import org.lunker.proxy.sip.pre_process.response.ViaRemover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Created by dongqlee on 2018. 5. 1..
 */
public class ProxyPreHandler implements ProxyHandler {
    private Logger logger= LoggerFactory.getLogger(ProxyPreHandler.class);

    private String host="";
    private int port=0;
    private String transport="";


    // Request pre-handler
    private RequestValidator requestValidator=null;
    private RoutePreprocessor routePreprocessor=null;
    private RequestTargetDetector requestTargetDetector=null;

    // Response pre-handler
    private ProxyHandler viaRemover=null;

    public ProxyPreHandler() {
    }

    public ProxyPreHandler(ServerInfo serverInfo) {
        this.host=serverInfo.getHost();
        this.port=serverInfo.getPort();
        this.transport=serverInfo.getTransport().getValue();

        // pre-handler

        this.requestValidator=new RequestValidator();
        this.routePreprocessor=new RoutePreprocessor();
        this.requestTargetDetector=new RequestTargetDetector();

        this.viaRemover=new ViaRemover();
    }

    @Override
    public Message handle(Message message) {
        if(message.getProcessState() != ProcessState.PRE) {
            //TODO: State check 공통화
            return message;
        }

        if(logger.isDebugEnabled())
            logger.debug("In ProxyPreHandler");

        if(message.getOriginalMessage() instanceof DefaultSipRequest){
            //Request Preprocessing
            // TODO: add more filter validation
            Mono<?> testMono=Mono.just(message)
                    .map(requestValidator::handle)
                    .filter(propagatedMessage-> propagatedMessage.getValidation().isValidate())
                    .map(routePreprocessor::handle)
                    .map(requestTargetDetector::handle);

            testMono.subscribeOn(Schedulers.single());
            testMono.subscribe();
        }
        else{
            //Response Preprocessing
            Mono.just(message).map(viaRemover::handle).subscribeOn(Schedulers.single()).subscribe();
        }

        // Change proxy message state
        if(message.getValidation().isValidate())
            message.setProcessState(ProcessState.IN);
        else
            message.setProcessState(ProcessState.POST);

        return message;
    }

    private Message removeTopVia(Message message){
        DefaultSipResponse defaultSipResponse=(DefaultSipResponse) message.getOriginalMessage();
        Via via=defaultSipResponse.getTopmostVia();

        if(via.getHost().equalsIgnoreCase(host)){
            System.out.println("breakpoint");

            defaultSipResponse.removeTopVia();
        }
        else{
            logger.warn("Invalid routed sip message. {}\ndrop...", defaultSipResponse);
        }

        return message;
    }


}
