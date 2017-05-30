// Note: this servlet is running under the /startRequest endpoint and is
// returning a redirect to the user

package org.sshkeyportal.client.oauth2.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.client.OA4MPResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.myproxy.oa4mp.client.storage.AssetStoreUtil;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;

import java.util.List;
import java.util.Collection;

import javax.servlet.http.Cookie;
import java.net.HttpCookie;
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

	// Get and clear cookie, sets new one in response
        info("2.a Retrieving identifier from cookie");
	// Get cookie from response, since we get internally forwarded here,
	// without browser interaction, so the cookie is still in the response,
	// not in the request
        String identifier = getCookie(response);
	// If there isn't a cookie, fail
        if (identifier == null) {
            debug("No cookie found! Cannot identify session!");
            throw new GeneralException("Unable to identify session!");
        }

	// Create a authZ grant flow request
        gtwResp = getOA4MPService().requestCert(BasicIdentifier.newID(identifier));

	// Redirect to the AS
        info("1.b. Got response. Creating page with redirect for " + gtwResp.getRedirect().getHost());
        response.sendRedirect(gtwResp.getRedirect().toString());
    }

    protected String getCookie(HttpServletResponse response) {
	Collection<String> cookieHeaders = response.getHeaders("Set-Cookie");
	for (String cookieHeader: cookieHeaders)	{
	    List<HttpCookie> cookies = HttpCookie.parse(cookieHeader);
	    for (HttpCookie cookie: cookies)   {
		if (cookie.getName().equals(OA4MP_CLIENT_REQUEST_ID))   {
		    String value=cookie.getValue();
		    // update cookie
		    Cookie newCookie = new Cookie(OA4MP_CLIENT_REQUEST_ID, value);
		    newCookie.setMaxAge(15 * 60); // 15 minutes
		    newCookie.setSecure(true);
		    response.addCookie(newCookie);
		    return value;
		}
	    }
	}

        return null;
    }
}
