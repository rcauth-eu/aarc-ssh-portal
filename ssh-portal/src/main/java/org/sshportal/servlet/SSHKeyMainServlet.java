// Note: this is showing the .jsp file which contains the form to submit

package org.sshkeyportal.servlet;

import static org.sshkeyportal.client.oauth2.SPOA2Constants.*;

import org.sshkeyportal.client.oauth2.SPOA2ClientLoader;

import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2Asset;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2MPService;
import edu.uiuc.ncsa.security.core.exceptions.InvalidTimestampException;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.token.AccessToken;
import edu.uiuc.ncsa.security.delegation.token.RefreshToken;
import edu.uiuc.ncsa.security.oauth_2_0.OA2Constants;
import edu.uiuc.ncsa.security.servlet.ServiceClient;
import edu.uiuc.ncsa.security.servlet.ServiceClientHTTPException;

import static edu.uiuc.ncsa.security.core.util.DateUtils.checkTimestamp;
import static edu.uiuc.ncsa.security.core.util.DateUtils.MAX_TIMEOUT;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.output.*;
import org.apache.commons.io.IOUtils;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;


public class SSHKeyMainServlet extends ClientServlet {
    private static JSONParser parser = new JSONParser(0);

    private URI sshEndpoint = null;
    /*
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);

	sshEndpoint = ((SPOA2ClientLoader)getConfigurationLoader()).getsshkeyURI();
    }
    
    /*
     */ 
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	info("Doing post");

	try {
	    handleRequest(request, response, true);
	} catch (Throwable t)	{
	    getExceptionHandler().handleException(t, request, response);
	}
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	info("Doing get");
	
	try {
	    handleRequest(request, response, false);
	} catch (Throwable t)	{
	    getExceptionHandler().handleException(t, request, response);
	}
    }

    @Override
    public void doIt(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	warn("In doIt()");
	throw new ServletException("doIt is not implemented");
    }

    /*
     */ 
    protected void handleRequest(HttpServletRequest request, HttpServletResponse response, boolean isPost) throws ServletException, IOException {
	AccessToken at = null;

	// Try to get asset based on Cookie
	OA2Asset asset = getAsset(request, response);
	if (asset != null)  {
	    // handle logout
	    if (isLogout(request))  {
		// Remove the asset, we'll afterwards continue with the login flow
		info("Removing existing asset");
		getCE().getAssetStore().remove(asset.getIdentifier());
	    } else {
		// Otherwise get the access token
		at = getAccessToken(asset);
	    }
	}

	if (at == null)	{
	    // No valid session: clear cookie and go to the login page.
	    clearCookie(request, response);

	    // Internally forward to the login.jsp
	    info("Forwarding to: "+SSHKEY_LOGIN_PAGE);
	    RequestDispatcher dispatcher = getServletConfig().getServletContext().getRequestDispatcher(SSHKEY_LOGIN_PAGE);
	    request.setAttribute("redirect_host", getServletConfig().getServletContext().getContextPath() + SSHKEY_PORTAL_START);
	    dispatcher.forward(request, response);
	    return; // Need return to finalize doPost or doGet
	}

	// Get the access_token value
	String tok = at.getToken();

	info("Starting the main handling");
	if (isPost) {
	    // For post we first handle that, then we continue to list the keys

	    // First get post parameters
	    Map<String,String> params = getPostParams(request, response);

	    Map m1 = new HashMap();
	    m1 = createSSHKeyRequestParams(params);

	    // Add the access token
	    m1.put(OA2Constants.ACCESS_TOKEN, tok);
	    ServiceClient client = ((SPOA2ClientLoader)getConfigurationLoader()).createServiceClient(sshEndpoint);
	    String resp = null;
	    try {
		info("Executing: "+client.convertToStringRequest(client.host().toString(), m1));
		resp = client.getRawResponse(m1);
	    } catch (Throwable t) {
		warn("Failed with URI: "+client.convertToStringRequest(client.host().toString(), m1));
		throw t;
	    }
	}

	// Now use the access token to get the list of keys
        HashMap m2 = new HashMap();
	m2.put(API_ACTION, API_LIST);
	m2.put(OA2Constants.ACCESS_TOKEN, tok);
	ServiceClient client = ((SPOA2ClientLoader)getConfigurationLoader()).createServiceClient(sshEndpoint);
	String resp = null;
	try {
	    resp = client.getRawResponse(m2);
	} catch (Throwable t) {
	    warn("Failed with URI: "+client.convertToStringRequest(client.host().toString(), m2));
	    throw t;
	}

	// Set the jsp variables: for ssh_keys, set value to mapping
	request.setAttribute(SSH_KEYS, getKeysFromJson(resp));
	// Set forwarding page to main.jsp
	RequestDispatcher dispatcher = getServletConfig().getServletContext().getRequestDispatcher(SSHKEY_MAIN_PAGE);
	info("Forwarding to: "+SSHKEY_MAIN_PAGE);
	request.setAttribute("redirect_host", getServletConfig().getServletContext().getContextPath() + "/");
	request.setAttribute("username", asset.getUsername());

	dispatcher.forward(request, response);
    }

    protected boolean isLogout(HttpServletRequest request)  {
	String value=request.getParameter(SUBMIT);
	return (value!=null && value.equals(SUBMIT_LOGOUT));
    }

    protected OA2Asset getAsset(HttpServletRequest request, HttpServletResponse response)  {
	// Do we have a cookie? If yes, get it and clear it
	String identifier = getCookie(request, response);
	if (identifier == null)
	    return null;

	info("Found old identifier: "+identifier);

	// Found existing identifier, get the asset
	OA2Asset asset = (OA2Asset) getCE().getAssetStore().get(identifier);
	if (asset == null)	{
	    warn("No asset for identifier");
	    return null;
	}

	return asset;
    }

    protected AccessToken getAccessToken(OA2Asset asset)  {
	final long SHORT_GRACETIME = 1*60*1000L;
	final long LONG_GRACETIME = 5*60*1000L;

	// First get current access token from the asset
	AccessToken at = asset.getAccessToken();
	if (at==null)	{
	    warn("No access token for identifier");
	    return null;
	}

	// Do we have refresh tokens?
	RefreshToken rt = asset.getRefreshToken();
	if (rt == null)	{
	    // We'll have to do with the access token
	    try {
		// Don't use ATs valid shorter than SHORT_GRACETIME
		checkTimestamp(at.getToken(), MAX_TIMEOUT-SHORT_GRACETIME);
	    } catch (InvalidTimestampException e)	{
		// Access token is about to expire
		info("No refresh tokens and AT is about to expire");
		return null;
	    }
	    // Valid long enough: return it
	    return at;
	}

	// We have refresh tokens, check whether we need to use it
	try {
	    checkTimestamp(at.getToken(), MAX_TIMEOUT-LONG_GRACETIME);
	    return at;
	} catch (InvalidTimestampException e)	{
	    info("Token about to expire, will refresh");
	}

	// Do a refresh token request
	OA2Asset newAsset = null;
	try {
	    OA2MPService oa2MPService = (OA2MPService)getOA4MPService();
	    newAsset = oa2MPService.refresh(asset.getIdentifierString());
	} catch (IOException e)	{
	    warn("Could not get new refresh token");
	    return null;
	}

	// Get the new token, note that the new asset is actually the old one
	// with new tokens.
	at = newAsset.getAccessToken();
	if (at==null)	{
	    warn("No access token for new asset: "+newAsset.getIdentifier());
	    return null;
	}

	return at;
    }

    protected String getCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(SSH_CLIENT_REQUEST_ID)) {
                    return cookie.getValue();
                }
            }
        }
	// No match
        return null;
    }

    //
    @Override
    protected String clearCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(SSH_CLIENT_REQUEST_ID)) {
		    String value = cookie.getValue();
		    // remove cookie
		    cookie.setMaxAge(0); // expire
		    cookie.setValue("");
		    cookie.setSecure(true);
		    response.addCookie(cookie);
                    return value;
                }
            }
        }
	return null;
    }

    protected List<Map<String, String>> getKeysFromJson(String json)	{
	JSONObject top = null;
        try {
            Object obj = parser.parse(json);
            if ( obj instanceof JSONObject ) {
                top = (JSONObject)obj;
            } else {
		warn("parsed input is not valid JSONObject: "+json);
	    }
        } catch (ParseException e)  {
            warn("input is not valid json: "+json);
	    return null;
        }
	Object obj = top.get(SSH_KEYS);
	if (obj == null)    {
	    warn("input is missing ssh_keys node");
	    return null;
	}

	// Input might be either simple object or an array
	List<Map<String, String>> list=new ArrayList <Map <String, String>>(); ;
	if (obj instanceof JSONArray)	{
	    // Add array
	    JSONArray arr = (JSONArray)obj;
	    for (int i=0; i<arr.size(); i++)    {
		JSONObject entry = (JSONObject)arr.get(i);
		list.add((Map)entry);
	    }
	} else {
	    // Add object
	    JSONObject entry = (JSONObject)obj;
	    list.add((Map)entry);
	}
	return list;
    }

    protected Map<String,String> getPostParams(HttpServletRequest request, 
               HttpServletResponse response) throws ServletException, IOException {

	int maxFileSize = 50 * 1024;
//	int maxMemSize = 4 * 1024;

	if (!ServletFileUpload.isMultipartContent(request)) {
	    throw new ServletException("No valid multipart content");
	}

	// Handle submit separately

	// Create a new file upload handler
	ServletFileUpload upload = new ServletFileUpload();
	upload.setSizeMax(maxFileSize);

	Map<String,String> params = new HashMap<String,String>();
	// Parse the request
	try {
	    FileItemIterator iter = upload.getItemIterator(request);
	    while (iter.hasNext()) {
		FileItemStream item = iter.next();
		String name = item.getFieldName();
		InputStream stream = item.openStream();
		String value = IOUtils.toString(stream, null);
		// Can use same IOUtils.toString() on both type, either
		// formfield or not. Just check for non-valid filename fields
		if (value != null)  {
		    String prunedValue = null;
		    // Skipping unknown non-form fields
		    if (!item.isFormField() && !name.equals(PUB_KEY_FILE))  {
			warn("Skipping unknown non-formfield "+name);
			continue;
		    }
		    // Prune pubkeys differently from the rest
		    if (name.equals(PUB_KEY_FILE) || name.equals(PUB_KEY_VALUE)) {
			// Replace first white-space with a space and strip all
			// other non-space whitespace. Then prune the input.
			prunedValue = value.replaceFirst("[\\s]+", " ").
					    replaceAll("[\\t\\n\\v\\f\\r]", "").
					    replaceAll(PRUNEPATTERN, "_");
			// Log file
			if (!item.isFormField())    {
			    info("Getting input from file "+item.getName());
			}
		    } else {
			prunedValue = value.replaceAll(PRUNEPATTERN, "_");
			info("Adding: ("+name+","+prunedValue+")");
		    }
		    // Add parameter
		    params.put(name, prunedValue);
		}
	    }
	} catch (FileUploadException e)	{
	    throw new ServletException("upload failure");
	}
	return params;
    }

    protected Map<String, String> createSSHKeyRequestParams(Map<String, String> params) {
	String pub_key = null;
	if (params.get(PUB_KEY_FILE) != null && !params.get(PUB_KEY_FILE).isEmpty())	{
	    if (params.get(PUB_KEY_VALUE) != null && !params.get(PUB_KEY_VALUE).isEmpty())
		throw new ServiceClientHTTPException("Public key specified both as file and value");
	    pub_key = params.get(PUB_KEY_FILE);
	} else {
	    pub_key = params.get(PUB_KEY_VALUE);
	}

	String label = params.get(LABEL);
	String description = params.get(DESCRIPTION);
	String submit = params.get(SUBMIT);
	if (submit==null)
	    throw new ServiceClientHTTPException("Missing "+SUBMIT+" from POST");

	Client client = getCE().getClient();
	if (client ==null)
	    throw new ServiceClientHTTPException("Cannot find client in environment");

	// Now use the access token to access a protected resource
        HashMap postParams = new HashMap();

	switch (submit) {
	    case SUBMIT_ADD:
		if (pub_key==null)
		    throw new ServiceClientHTTPException("Need label and public key for action \"add\"");
		postParams.put(OA2Constants.CLIENT_ID, client.getIdentifierString());
		postParams.put(OA2Constants.CLIENT_SECRET, client.getSecret());
		postParams.put(API_ACTION, API_ADD);
		postParams.put(API_PUB_KEY, pub_key);
		if (label != null && !label.isEmpty())
		    postParams.put(API_LABEL, label);
		if (description != null && !description.isEmpty())
		    postParams.put(API_DESCRIPTION, description);
		break;
	    case SUBMIT_UPDATE:
		if (label==null)
		    throw new ServiceClientHTTPException("Need at least label for action \"update\"");
		postParams.put(OA2Constants.CLIENT_ID, client.getIdentifierString());
		postParams.put(OA2Constants.CLIENT_SECRET, client.getSecret());
		postParams.put(API_ACTION, API_UPDATE);
		postParams.put(API_LABEL, label);
		if (pub_key != null && !pub_key.isEmpty())
		    postParams.put(API_PUB_KEY, pub_key);
		if (description != null && !description.isEmpty())
		    postParams.put(API_DESCRIPTION, description);
		break;
	    case SUBMIT_REMOVE:
		if (label==null)
		    throw new ServiceClientHTTPException("Need at label for remove");
		postParams.put(API_ACTION, API_REMOVE);
		postParams.put(API_LABEL, label);
		break;
	    default:
		warn("Unknown "+SUBMIT+" value: "+submit);
		throw new ServiceClientHTTPException("Unknown "+SUBMIT+" value");
	}

	return postParams;
    }
}
