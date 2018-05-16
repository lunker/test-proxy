package proxy.sip;

import io.netty.channel.ChannelHandlerContext;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipMessage;
import org.lunker.new_proxy.stub.SipMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proxy.sip.pre_process.ProxyPreHandler;
import proxy.sip.pro_process.ProxyProHandler;
import proxy.sip.process.ProxyInHandler;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

/**
 * Created by dongqlee on 2018. 4. 25..
 */
public class SipServletImpl implements SipMessageHandler {
    private Logger logger= LoggerFactory.getLogger(SipServletImpl.class);

    private ProxyPreHandler proxyPreHandler=null;
    private ProxyInHandler proxyInHandler=null;
    private ProxyProHandler proxyProHandler=null;

    public SipServletImpl() {
        proxyPreHandler=new ProxyPreHandler();
        proxyInHandler=new ProxyInHandler();
        proxyProHandler=new ProxyProHandler();
    }

    @Override
    public void handle(ChannelHandlerContext ctx, Optional<DefaultSipMessage> maybeDefaultSipMessage) {
        if(logger.isInfoEnabled())
            logger.info("[RECEIVED]:\n" + maybeDefaultSipMessage.get().toString());

        maybeDefaultSipMessage.ifPresent((defaultSipMessage)->{
            Mono<?> proxyAsync=Mono.just(defaultSipMessage).map(proxyPreHandler::handle).map((preProcessedSipMessage)-> proxyInHandler.handle(ctx, preProcessedSipMessage)).map(proxyProHandler::handle);

            proxyAsync=proxyAsync.subscribeOn(Schedulers.immediate());
            proxyAsync.subscribe();
        });
    }
}
