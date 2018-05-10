package proxy;

import org.lunker.new_proxy.Bootstrap;
import proxy.core.SipServletImpl;

/**
 * Created by dongqlee on 2018. 5. 9..
 */
public class Application {


    public static void main(String[] args){
        try{
            Bootstrap.start("tcp", new SipServletImpl());
        }
        catch (Exception e ){
            e.printStackTrace();
        }
    }
}
