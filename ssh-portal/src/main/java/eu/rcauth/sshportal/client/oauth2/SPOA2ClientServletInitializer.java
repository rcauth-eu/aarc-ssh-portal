package eu.rcauth.sshportal.client.oauth2;

import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2ClientServletInitializer;
import edu.uiuc.ncsa.security.servlet.ExceptionHandler;

/**
 * <p>Created by Mischa Sall&eacute;<br>
 * Initializes SSH Key Portal OA4MP Client
 * @see eu.rcauth.sshportal.servlet.SSHKeyMainServlet
 */
public class SPOA2ClientServletInitializer extends OA2ClientServletInitializer {

    @Override
    public ExceptionHandler getExceptionHandler() {
        if(exceptionHandler == null){
            exceptionHandler = new SPOA2ClientExceptionHandler((ClientServlet) getServlet(), getEnvironment().getMyLogger());
            getEnvironment().getMyLogger().warn("Set new exceptionHandler");
        } else {
            getEnvironment().getMyLogger().warn("Using old exceptionHandler");
        }
        return exceptionHandler;
    }

}
