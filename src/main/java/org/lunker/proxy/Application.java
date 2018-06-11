package org.lunker.proxy;

import io.netty.channel.ChannelHandler;
import org.lunker.new_proxy.Bootstrap;
import org.lunker.new_proxy.model.Transport;
import org.lunker.proxy.sip.SipServletImpl;

/**
 * Created by dongqlee on 2018. 5. 9..
 */
@ChannelHandler.Sharable
public class Application {
    public static void main(String[] args){
        try{
            Bootstrap.addHandler(Transport.TCP, SipServletImpl.class);
            Bootstrap.addHandler(Transport.UDP, SipServletImpl.class);
            Bootstrap.addHandler(Transport.WS, SipServletImpl.class);
            Bootstrap.addHandler(Transport.WSS, SipServletImpl.class);


            // LB-Proxy health-check를 위한 tcp server
//            Bootstrap.addHandler(Transport.TCP, DiscoveryServer.class);
            Bootstrap.run();
        }
        catch (Exception e ){
            e.printStackTrace();
        }
    }
}
