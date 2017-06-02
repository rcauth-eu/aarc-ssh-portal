// Note: this servlet is running at the redirect_uri and handles getting the
// access_token and using it

package org.sshkeyportal.client.oauth2.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.client.AssetResponse;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2Asset;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2MPProxyService;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.BasicIdentifier;
import edu.uiuc.ncsa.security.delegation.token.AuthorizationGrant;
import edu.uiuc.ncsa.security.delegation.token.impl.AuthorizationGrantImpl;
import edu.uiuc.ncsa.security.oauth_2_0.OA2Constants;
import edu.uiuc.ncsa.security.oauth_2_0.OA2RedirectableError;
import edu.uiuc.ncsa.security.oauth_2_0.UserInfo;
import edu.uiuc.ncsa.security.oauth_2_0.client.ATResponse2;
import edu.uiuc.ncsa.security.servlet.JSPUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

import java.io.PrintWriter;

import static org.sshkeyportal.client.oauth2.servlet.SPOA2Constants.*;

public class SPOA2ReadyServlet extends ClientServlet {
	
    @Override
    protected void doIt(HttpServletRequest request, HttpServletResponse response) throws Throwable {
	// Check for errors
        if (request.getParameterMap().containsKey(OA2Constants.ERROR)) {
            throw new OA2RedirectableError(request.getParameter(OA2Constants.ERROR),
                    request.getParameter(OA2Constants.ERROR_DESCRIPTION),
                    request.getParameter(OA2Constants.STATE));
        }

	// Get the AuthZ grant and state from the request
        info("2.a. Getting AuthZ grant and verifier.");
        String token = request.getParameter(CONST(ClientEnvironment.TOKEN));
        String state = request.getParameter(OA2Constants.STATE);
        if (token == null) {
	    // Forward the error to the error page
            warn("2.a. The AuthZ grant is null");
            GeneralException ge = new GeneralException("Error: This servlet requires parameters for the AuthZ grant and possibly verifier.");
            request.setAttribute("exception", ge);
            JSPUtil.fwd(request, response, getCE().getErrorPagePath());
            return;
        }
        info("2.a AuthZ grant found");
        AuthorizationGrant grant = new AuthorizationGrantImpl(URI.create(token));

	// Get and clear cookie, sets new one in response
        info("2.a Retrieving identifier from cookie");
        String identifier= getCookie(request, response);
	// If there isn't a cookie, fail
        if (identifier== null) {
            debug("No cookie found! Cannot identify session!");
            throw new GeneralException("Unable to identify session!");
        }
        debug("id = " + identifier);

	// Fish up the asset identified by the cookie
	OA2Asset asset = (OA2Asset) getCE().getAssetStore().get(BasicIdentifier.newID(identifier));
	if(asset.getState() == null || !asset.getState().equals(state)){
	    warn("The expected state from the server was \"" + asset.getState() + "\", but instead \"" + state + "\" was returned. Transaction aborted.");
	    throw new IllegalArgumentException("Error: The state returned by the server is invalid.");
	}

	// Get the access (and refresh) token from the token end point
        OA2MPProxyService oa2MPService = (OA2MPProxyService)getOA4MPService();
	ATResponse2 atResponse2 = oa2MPService.getAccessToken(asset, grant);
	info("2.a Found access_token: "+atResponse2.getAccessToken()+", refresh_token: "+atResponse2.getRefreshToken());

	// Now redirect back to main page
	info("Redirecting to main page "+SSHKEY_MAIN_PAGE);
	response.sendRedirect(getServletConfig().getServletContext().getContextPath() + "/");
    }
    
    protected String getCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String identifier = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(SSH_CLIENT_REQUEST_ID)) {
		    // update cookie: don't let it expire
		    cookie.setMaxAge(-1);
		    cookie.setSecure(true);
		    response.addCookie(cookie);
                    return cookie.getValue();
                }
            }
        }

        return identifier;
    }
}
