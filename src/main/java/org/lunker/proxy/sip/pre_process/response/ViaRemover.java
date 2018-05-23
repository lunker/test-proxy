package org.lunker.proxy.sip.pre_process.response;

import org.lunker.proxy.core.Message;
import org.lunker.proxy.core.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dongqlee on 2018. 5. 23..
 */
public class ViaRemover implements ProxyHandler{
    private Logger logger= LoggerFactory.getLogger(ViaRemover.class);

    @Override
    public Message handle(Message message) {
        return message;
    }
}
