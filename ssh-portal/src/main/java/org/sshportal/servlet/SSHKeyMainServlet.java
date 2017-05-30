// Note: this is showing the .jsp file which contains the form to submit

package org.sshkeyportal.servlet;

//import java.util.logging.Level;
//import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.uiuc.ncsa.security.oauth_2_0.OA2Constants;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientXMLTags;
import edu.uiuc.ncsa.security.servlet.ServiceClient;

import edu.uiuc.ncsa.security.delegation.storage.Client;

import edu.uiuc.ncsa.security.core.Identifier;
import edu.uiuc.ncsa.myproxy.oa4mp.client.storage.AssetStoreUtil;
import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.security.servlet.ServiceClientHTTPException;
import edu.uiuc.ncsa.security.core.exceptions.InvalidTimestampException;

import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2Asset;
import edu.uiuc.ncsa.security.delegation.token.AccessToken;

import org.sshkeyportal.client.oauth2.SPOA2ClientLoader;

import static edu.uiuc.ncsa.security.core.util.DateUtils.checkTimestamp;

import java.net.URI;
import java.io.PrintWriter;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.output.*;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;

import java.io.File;
import java.util.List;
import java.util.Iterator;


public class SSHKeyMainServlet extends ClientServlet {
    // Note: need to allow also = for Base64. We also allow ! and _ and for the
    // file content we allow newlines and tabs.
    private final String PRUNEPATTERN_VALUE="[^\\p{Lower}\\p{Upper}\\p{Digit} ='()+,-_.!?@]";
    private final String PRUNEPATTERN_FILE="[^\\p{Lower}\\p{Upper}\\p{Digit}\\n\\r\\t ='()+,-_.!?@]";

    public static final String SSHKEY_MAIN_PAGE="/pages/main.jsp";
    public static final String SSHKEY_PORTAL_START="/startRequest";

    public static final String SSH_KEYS		= "ssh_keys";

    public static final String PUB_KEY		= "pubkey";

    // These fields are also all used in the main.jsp
    public static final String PUB_KEY_FILE	= "pubkey_file";
    public static final String PUB_KEY_VALUE	= "pubkey_value";
    public static final String LABEL		= "label";
    public static final String DESCRIPTION	= "description";
    public static final String ACTION		= "action";
    
    public static final String ACTION_ADD	= "add new public key";
    public static final String ACTION_UPDATE	= "update selected key";
    public static final String ACTION_REMOVE	= "remove selected key";
   
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
	AccessToken at = getAccessToken(request, response);
	if (at == null)	{
	    // Create a new cookie and add it to the response, not ideal but we
	    // can't do it at submit time
	    info("Creating cookie");
	    createCookie(response);

	    // Internally forward to the startRequest servlet
	    RequestDispatcher dispatcher = getServletConfig().getServletContext().getRequestDispatcher(SSHKEY_PORTAL_START);
	    info("Forwarding to: "+SSHKEY_PORTAL_START);
	    dispatcher.forward(request, response);
	    return; // Need return to finalize doPost or doGet
	}

	String tok = at.getToken();
	// Get the access_token, this might also initiate a redirect
//	String tok = getToken(request, response);

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
	m2.put(ACTION, "list");
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
	dispatcher.forward(request, response);
    }

    protected AccessToken getAccessToken(HttpServletRequest request, HttpServletResponse response)  {
	// Do we have a cookie? If yes, get it and clear it
	String identifier = updateCookie(request, response);
	if (identifier == null)
	    return null;

	// If we have a  cookie, verify that the token is not expired
	info("Found old identifier: "+identifier);

	// Found existing identifier, get the asset
	OA2Asset asset = (OA2Asset) getCE().getAssetStore().get(identifier);
	if (asset == null)	{
	    warn("No asset for identifier");
	    return null;
	}

	// get Access Token
	AccessToken at = asset.getAccessToken();
	if (at==null)	{
	    warn("No access token for identifier");
	    return null;
	} 
	try {
	    checkTimestamp(at.getToken());
	} catch (InvalidTimestampException e)	{
	    warn("Token is expired: "+e.getMessage());
	    return null;
	}

	return at;
    }

    protected void createCookie(HttpServletResponse response)	{
	// Create new identifier
        Identifier id = AssetStoreUtil.createID();

        // Create a cookie such that we recognize the session
        Cookie cookie = new Cookie(OA4MP_CLIENT_REQUEST_ID, id.getUri().toString());
        cookie.setMaxAge(-1);
        cookie.setSecure(true);
        info("Cookie with new id = " + id.getUri());
        response.addCookie(cookie);
    }

    //
    protected String updateCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String identifier = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(OA4MP_CLIENT_REQUEST_ID)) {
		    // update cookie: no lifetime
		    cookie.setMaxAge(-1); // no expiry
		    cookie.setSecure(true);
		    response.addCookie(cookie);
                    return cookie.getValue();
                }
            }
        }

        return identifier;
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
		if (item.isFormField())	{
		    String prunedValue = (value == null ? null : value.replaceAll(PRUNEPATTERN_VALUE, "_"));
		    info("Adding: ("+name+","+prunedValue+")");
		    params.put(name, prunedValue);
		} else if (name.equals(PUB_KEY_FILE))	{
		    String prunedValue = (value == null ? null : value.replaceAll(PRUNEPATTERN_FILE, "_"));
		    // Not a formfield: only for correct post param
		    // Process the input stream
		    if (!prunedValue.isEmpty())	{
			info("Getting input from file "+item.getName());
			params.put(name, prunedValue);
		    }
		} else {
		    warn("Skipping unknown non-formfield "+name);
		}
	    }
	} catch (FileUploadException e)	{
	    throw new ServletException("upload failure");
	}
	return params;
    }

    protected Map<String, String> createSSHKeyRequestParams(Map<String, String> params) {
	String pub_key = null;
	if (params.get(PUB_KEY_FILE) != null)	{
	    if (params.get(PUB_KEY_VALUE) != null && !params.get(PUB_KEY_VALUE).isEmpty())
		throw new ServiceClientHTTPException("Public key specified both as file and value");
	    pub_key = params.get(PUB_KEY_FILE);
	} else {
	    pub_key = params.get(PUB_KEY_VALUE);
	}

	String label = params.get(LABEL);
	String description = params.get(DESCRIPTION);
	String action = params.get(ACTION);
	if (action==null)
	    throw new ServiceClientHTTPException("Missing action from POST");

	Client client = getCE().getClient();
	if (client ==null)
	    throw new ServiceClientHTTPException("Cannot find client in environment");

	// Now use the access token to access a protected resource
        HashMap postParams = new HashMap();

	switch (action) {
	    case ACTION_ADD:
		if (pub_key==null)
		    throw new ServiceClientHTTPException("Need label and public key for add");
		postParams.put(OA2Constants.CLIENT_ID, client.getIdentifierString());
		postParams.put(OA2Constants.CLIENT_SECRET, client.getSecret());
		postParams.put(ACTION, "add");
		postParams.put(PUB_KEY, pub_key);
		if (label != null && !label.isEmpty())
		    postParams.put(LABEL, label);
		if (description != null && !description.isEmpty())
		    postParams.put(DESCRIPTION, description);
		break;
	    case ACTION_UPDATE:
		if (label==null)
		    throw new ServiceClientHTTPException("Need at least label for update");
		postParams.put(OA2Constants.CLIENT_ID, client.getIdentifierString());
		postParams.put(OA2Constants.CLIENT_SECRET, client.getSecret());
		postParams.put(ACTION, "update");
		postParams.put(LABEL, label);
		if (pub_key != null && !pub_key.isEmpty())
		    postParams.put(PUB_KEY, pub_key);
		if (description != null && !description.isEmpty())
		    postParams.put(DESCRIPTION, description);
		break;
	    case ACTION_REMOVE:
		if (label==null)
		    throw new ServiceClientHTTPException("Need at label for remove");
		postParams.put(ACTION, "remove");
		postParams.put(LABEL, label);
		break;
	    default:
		warn("Unknown submit value: "+action);
		throw new ServiceClientHTTPException("Unknown submit value");
	}

	return postParams;
    }
}
