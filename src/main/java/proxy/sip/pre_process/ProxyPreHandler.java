package proxy.sip.pre_process;

import org.lunker.new_proxy.sip.wrapper.message.DefaultSipMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dongqlee on 2018. 5. 1..
 */
public class ProxyPreHandler {
    private Logger logger= LoggerFactory.getLogger(ProxyPreHandler.class);

    public DefaultSipMessage handle(DefaultSipMessage defaultSipMessage){
        if(logger.isDebugEnabled())
            logger.debug("In ProxyPreHandler");

        requestValidate();
        checkUri();

        return defaultSipMessage;
    }

    public void requestValidate(){

    }

    private boolean checkUri(){
        return false;
    }

    private boolean checkMaxForwards(){
        return false;
    }

    private boolean checkProxyAuthorization(){
        return false;
    }
}
