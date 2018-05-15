package proxy.sip.pro_process;

import org.lunker.new_proxy.sip.wrapper.message.DefaultSipMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dongqlee on 2018. 5. 1..
 */
public class ProxyProHandler {
    private Logger logger= LoggerFactory.getLogger(ProxyProHandler.class);

    public int handle(DefaultSipMessage defaultSipMessage){
        try{
            defaultSipMessage.send();

            if(logger.isInfoEnabled())
                logger.info("[SENT]\n{}", defaultSipMessage);
        }
        catch (Exception e){
            e.printStackTrace();

            return -1;
        }

        return 0;
    }
}
