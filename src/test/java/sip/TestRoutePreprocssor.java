package sip;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;
import org.junit.Before;
import org.junit.Test;
import org.lunker.new_proxy.sip.util.SipMessageFactory;
import org.lunker.new_proxy.sip.wrapper.message.DefaultSipMessage;

/**
 * Created by dongqlee on 2018. 5. 23..
 */
public class TestRoutePreprocssor {
    @Before
    public void createSipMessage(){

        SipMessageFactory sipMessageFactory=SipMessageFactory.getInstance();
        StringMsgParser stringMsgParser=new StringMsgParser();

        String rawMessage="";
        try{
            SIPMessage sipMessage=stringMsgParser.parseSIPMessage(rawMessage.getBytes(), false, false, null);
            DefaultSipMessage defaultSipMessage=new DefaultSipMessage(sipMessage);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testRoutePreprocessor(){

    }
}
