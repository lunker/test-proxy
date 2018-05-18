package org.lunker.proxy.sip.pro_process;

import gov.nist.javax.sip.header.Via;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipRequest;
import org.lunker.proxy.core.Message;
import org.lunker.proxy.core.ProcessState;
import org.lunker.proxy.core.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by dongqlee on 2018. 5. 1..
 */
public class ProxyPostHandler implements ProxyHandler {
    private Logger logger= LoggerFactory.getLogger(ProxyPostHandler.class);
    private

    public static String getHostAddress() {
        InetAddress localAddress = getLocalAddress();
        if (localAddress == null) {
            try {
                return Inet4Address.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                ;
            }
        } else {
            return localAddress.getHostAddress();
        }

        return "";
    }

    private static InetAddress getLocalAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                List<InterfaceAddress> interfaceAddresses = networkInterfaces.nextElement().getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    InetAddress address =interfaceAddress.getAddress();
                    if (address.isSiteLocalAddress()) {
                        return address;
                    }
                }
            }
        } catch (Exception e) {
            ;
        }

        return null;
    }

    @Override
    public Message handle(Message message) {

        // TODO:
        if(message.getProcessState() != ProcessState.POST)
            return message;

        try{
            if(message.getNewMessage() instanceof DefaultSipRequest){
                // add via header, proxy address
                Via proxyVia=new Via();
                proxyVia.setPort(10010);
                proxyVia.setHost();
                proxyVia.setReceived(host);

                proxyVia.setTransport("tcp");

                ((DefaultSipRequest)message.getNewMessage()).addVia(proxyVia);
            }

            message.getNewMessage().send();

            if(logger.isInfoEnabled())
                logger.info("[SENT]\n{}", message.getNewMessage());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return message;
    }
}
