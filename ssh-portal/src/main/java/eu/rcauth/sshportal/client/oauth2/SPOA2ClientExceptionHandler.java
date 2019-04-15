package eu.rcauth.sshportal.client.oauth2;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.ServletException;

import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.oa4mp.oauth2.client.servlet.OA2ClientExceptionHandler;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;

import eu.rcauth.sshportal.servlet.SSHKeyMainServlet;

/**
 * <p>Created by Mischa Sall&eacute;<br>
 * Exception handler for the {@link SSHKeyMainServlet}
 */
public class SPOA2ClientExceptionHandler extends OA2ClientExceptionHandler {

    MyLoggingFacade logger;

    public SPOA2ClientExceptionHandler(ClientServlet clientServlet, MyLoggingFacade myLogger) {
        super(clientServlet, myLogger);
        this.logger = myLogger;
    }

    @Override
    public void handleException(Throwable t, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        logger.warn("Handling exception t: "+t.getMessage());
        super.handleException(t, request, response);
    }

    @Override
    protected void parseContent(String content, HttpServletRequest request) {
        boolean hasValidContent = false;
        StringTokenizer st = new StringTokenizer(content, "\n");
        while (st.hasMoreElements()) {
            String currentLine = st.nextToken();
            StringTokenizer clST = new StringTokenizer(currentLine, "=");
            if (!clST.hasMoreTokens() || clST.countTokens() != 2)
                continue;

            try {
                request.setAttribute(clST.nextToken(), URLDecoder.decode(clST.nextToken(), "UTF-8").replaceAll("\n", ""));
            } catch (UnsupportedEncodingException xx) {
                // ok, try it without decoding it. (This case should never really happen)
                request.setAttribute(clST.nextToken(), clST.nextToken());
            }
            hasValidContent = true;
        }
        if (!hasValidContent) {
            logger.warn("Cannot parse body or error");
            throw new GeneralException();
        }
    }

}
