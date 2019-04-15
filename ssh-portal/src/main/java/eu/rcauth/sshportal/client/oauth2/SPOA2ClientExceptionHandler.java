package eu.rcauth.sshportal.client.oauth2;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.ServletException;

import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.oa4mp.oauth2.client.servlet.OA2ClientExceptionHandler;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.servlet.ServiceClientHTTPException;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;

import eu.rcauth.sshportal.servlet.SSHKeyMainServlet;
import net.sf.json.JSONObject;

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

    /**
     * handles exceptions, including a {@link ServiceClientHTTPException} which is
     * thrown when an error is returned from the sshkeys endpoint. In that
     * case the content of the response is typically a JSON which is parsed
     * by {@link #parseContent(String, HttpServletRequest)}
     */
    @Override
    public void handleException(Throwable t, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        logger.warn("Handling exception t: "+t.getMessage());
        // A ServiceClientHTTPException with status 400 is probably an error from the sshkeys servlet.
        // The output is then a JSON with an error_description. It will be parsed by parseContent().
        super.handleException(t, request, response);
    }

    /**
     * Parses the returned error response from the /sshkeys API endpoint.
     * It expects a JSON and will set the key/value pairs as attribute in the request.
     * @see OA2ClientExceptionHandler#handleException(Throwable, HttpServletRequest, HttpServletResponse)
     */
    @Override
    protected void parseContent(String content, HttpServletRequest request) {
        // Content is expected to be a JSON
        boolean hasValidContent = false;
        JSONObject errObject = JSONObject.fromObject(content);
        // put the key/values in request, they will be used on the error page
        Set hset = errObject.keySet();
        for (Object key : hset) {
            if (key instanceof String) {
                hasValidContent = true; // we manage to parse at least one key
                request.setAttribute((String)key, errObject.get(key));
            }
        }

        if (!hasValidContent) {
            logger.warn("Cannot parse body or error");
            throw new GeneralException();
        }
    }

}
