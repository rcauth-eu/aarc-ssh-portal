// Note: this servlet is running under the /startRequest endpoint and is
// returning a redirect to the user

package eu.rcauth.sshportal.client.oauth2.servlet;

import static eu.rcauth.sshportal.client.oauth2.SPOA2Constants.*;

import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.myproxy.oa4mp.client.storage.AssetStoreUtil;
import edu.uiuc.ncsa.security.core.Identifier;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <p>Created by Mischa Sall&eacute;<br>
 * Servlet that starts the OIDC authorization flow by building and sending the
 * redirect response.
 */
public class SPOA2StartRequest extends ClientServlet {
    /**
     * doIt is called by AbstractServlet for either GET or POST
     */
    @Override
    protected void doIt(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        info("1.a. Starting transaction");
        OA4MPResponse gtwResp = null;

        // Set cookie
        info("2.a Retrieving identifier from cookie");

        // Get cookie from response, since we get internally forwarded here,
        // without browser interaction, so the cookie is still in the response,
        // not in the request
        Identifier identifier = createCookie(response);

        // Create a authZ grant flow request
        gtwResp = getOA4MPService().requestCert(identifier);

        // Redirect to the AS
        info("1.b. Got response. Creating page with redirect for " + gtwResp.getRedirect().getHost());
        response.sendRedirect(gtwResp.getRedirect().toString());
    }

    /**
     * Creates new asset ID and sets it as a browser cookie in the response to
     * the user.
     */
    private Identifier createCookie(HttpServletResponse response) {
        // Create new identifier
        Identifier id = AssetStoreUtil.createID();

        // Create a cookie such that we recognize the session
        Cookie cookie = new Cookie(SSH_CLIENT_REQUEST_ID, id.getUri().toString());
        cookie.setMaxAge(15 * 60); // 15 minutes
        cookie.setSecure(true);
        info("Cookie with new id = " + id.getUri());
        response.addCookie(cookie);

        return id;
    }

}
