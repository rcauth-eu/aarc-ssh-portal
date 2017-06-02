// Note: this servlet is running under the /startRequest endpoint and is
// returning a redirect to the user

package org.sshkeyportal.client.oauth2.servlet;

import static org.sshkeyportal.client.oauth2.SPOA2Constants.*;

import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.myproxy.oa4mp.client.storage.AssetStoreUtil;
import edu.uiuc.ncsa.security.core.Identifier;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A simple servlet that starts the request. It will make the initial request and set an identifier
 * cookie in the users browser. If there is an {@link edu.uiuc.ncsa.myproxy.oa4mp.client.storage.AssetStore} configured, it will make
 * an entry for the {@link edu.uiuc.ncsa.myproxy.oa4mp.client.Asset} resulting from this delegation.
 * <br><br>
 * This example is intended to show control flow rather than be a polished application.
 * Feel free to boilerplate from it as needed. Do not deploy this in production environments.
 * <p>Created by Jeff Gaynor<br>
 * on 2/10/12 at  10:24 AM
 */
public class SPOA2StartRequest extends ClientServlet {
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

    protected Identifier createCookie(HttpServletResponse response) {
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
