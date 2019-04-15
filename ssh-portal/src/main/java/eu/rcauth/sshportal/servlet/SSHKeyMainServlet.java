// Note: this is showing the .jsp file which contains the form to submit

package eu.rcauth.sshportal.servlet;

import static eu.rcauth.sshportal.client.oauth2.SPOA2Constants.*;

import eu.rcauth.sshportal.client.oauth2.SPOA2ClientLoader;

import edu.uiuc.ncsa.myproxy.oa4mp.client.servlet.ClientServlet;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2Asset;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2MPService;
import edu.uiuc.ncsa.security.core.exceptions.InvalidTimestampException;
import edu.uiuc.ncsa.security.delegation.storage.Client;
import edu.uiuc.ncsa.security.delegation.token.AccessToken;
import edu.uiuc.ncsa.security.delegation.token.RefreshToken;
import edu.uiuc.ncsa.security.delegation.client.request.RTResponse;
import edu.uiuc.ncsa.security.oauth_2_0.OA2Constants;
import edu.uiuc.ncsa.security.servlet.ServiceClient;
import edu.uiuc.ncsa.security.servlet.ServiceClientHTTPException;


import static edu.uiuc.ncsa.security.core.util.DateUtils.checkTimestamp;
import static edu.uiuc.ncsa.security.core.util.DateUtils.MAX_TIMEOUT;

import eu.rcauth.sshportal.client.oauth2.SPOA2Constants;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
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
import java.util.Iterator;
import java.net.URI;

/**
 * <p>Created by Mischa Sall&eacute;<br>
 * Servlet to upload and manage SSH public keys, client to the OIDC protected
 * org.masterportal.oauth2.server.servlet.MPOA2SSHKeyServlet endpoint of
 * a Master Portal.
 */

public class SSHKeyMainServlet extends ClientServlet {
    /** Access tokens valid for less than this are considered expired */
    private static final long SHORT_GRACETIME = 60*1000L;

    /** When using refresh tokens, then access tokens valid for less will be
     * refreshed */
    private static final long LONG_GRACETIME = 5*60*1000L;

    /** API endpoint on the Master Portal server, to be obtained from the
     * configuration */
    private URI sshEndpoint = null;

    /**
     * Initialized the servlet, getting the API endpoint
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        sshEndpoint = ((SPOA2ClientLoader)getConfigurationLoader()).getsshkeyURI();
    }

    /**
     * Called when a POST is received, the actual handling is done in {@link #handleRequest}
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        debug("Doing post");

        try {
            handleRequest(request, response, true);
        } catch (Throwable t) {
            getExceptionHandler().handleException(t, request, response);
        }
    }

    /**
     * Called when a GET is received, the actual handling is done in {@link #handleRequest}
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        debug("Doing get");

        try {
            handleRequest(request, response, false);
        } catch (Throwable t)   {
            getExceptionHandler().handleException(t, request, response);
        }
    }

    /**
     * doIt is called by AbstractServlet, but since we override doGet and
     * doPost, we will not hit that code.
     */
    @Override
    public void doIt(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        warn("In doIt() which is not implemented");
        throw new ServletException("doIt is not implemented");
    }

    /**
     * Handles the actual request, using a slightly different flow for a GET or
     * POST. The flow is as follows:
     * <ul>
     * <li>Get asset from cookie in request.
     * <ul>
     *  <li>if we request logout and there is an asset, remove and continue with
     *  login
     *  <li>if there isn't an asset, login, i.e. redirect to login page.
     * </ul>
     * <li>We have an asset and are not logging out: first handle POST when
     * applicable: e.g. add, update, remove.
     * <li>Then do an API_LIST
     * <li>Show the main page
     * </ul>
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

        if (at == null) {
            // No valid session: clear cookie and go to the login page.
            clearCookie(request, response);

            // Internally forward to the login.jsp
            info("Forwarding to: "+SSHKEY_LOGIN_PAGE);
            ServletContext ctx = getServletConfig().getServletContext();
            RequestDispatcher dispatcher = ctx.getRequestDispatcher(SSHKEY_LOGIN_PAGE);
            request.setAttribute("redirect_host", ctx.getContextPath() + SSHKEY_PORTAL_START);
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

            Map<String,String> m1 = createSSHKeyRequestParams(params);

            // Add the access token
            m1.put(OA2Constants.ACCESS_TOKEN, tok);
            ServiceClient client = ((SPOA2ClientLoader)getConfigurationLoader()).createServiceClient(sshEndpoint);
            String strReq = null;
            try {
                strReq = ServiceClient.convertToStringRequest(client.host().toString(), m1);
                info("Executing: " + strReq);
                // Note we don't use the response
                // TODO check this indeed is ok and we don't need the return response
                client.getRawResponse(m1);
            } catch (Throwable t) {
                warn("Failed with URI: " + strReq);
                throw t;
            }
        }

        // Now use the access token to get the list of keys
        HashMap<String,String> m2 = new HashMap<>();
        m2.put(API_ACTION, API_LIST);
        m2.put(OA2Constants.ACCESS_TOKEN, tok);
        ServiceClient client = ((SPOA2ClientLoader)getConfigurationLoader()).createServiceClient(sshEndpoint);
        String resp = null;
        try {
            resp = client.getRawResponse(m2);
        } catch (Throwable t) {
            warn("Failed with URI: "+ ServiceClient.convertToStringRequest(client.host().toString(), m2));
            throw t;
        }

        // Set the jsp variables: for ssh_keys, set value to mapping
        request.setAttribute(SSH_KEYS, getKeysFromJson(resp));
        ServletContext ctx = getServletConfig().getServletContext();
        // Set forwarding page to main.jsp
        RequestDispatcher dispatcher = ctx.getRequestDispatcher(SSHKEY_MAIN_PAGE);
        info("Forwarding to: "+SSHKEY_MAIN_PAGE);
        request.setAttribute("redirect_host", ctx.getContextPath() + "/");
        String userName=asset.getUsername();
        if (userName == null) {
            warn("Cannot get username from asset, make sure OIDCEnabled is set to true");
            throw new ServiceClientHTTPException("Cannot get username");
        }
        info("Setting attribute username to:"+userName);
        request.setAttribute("username", userName);

        dispatcher.forward(request, response);
    }

    /////////////////////////////////////////////////////////////////////////
    // Private helper methods
    /////////////////////////////////////////////////////////////////////////

    /**
     * @return whether we requested a logout
     */
    private boolean isLogout(HttpServletRequest request)  {
        String value=request.getParameter(SUBMIT);
        return (value!=null && value.equals(SUBMIT_LOGOUT));
    }

    /**
     * retrieves the identifier from the cookie, then tries to obtain the
     * OA2Asset corresponding to that identifier
     */
    private OA2Asset getAsset(HttpServletRequest request, HttpServletResponse response)  {
        // Do we have a cookie? If yes, get it and clear it
        String identifier = getCookie(request, response);
        if (identifier == null)
            return null;

        info("Found old identifier: "+identifier);

        // Found existing identifier, get the asset
        OA2Asset asset = (OA2Asset) getCE().getAssetStore().get(identifier);
        if (asset == null)  {
            warn("No asset for identifier");
            return null;
        }

        return asset;
    }

    /**
     * Tries to obtain a valid access token for given OA2Asset. If the token is
     * old, the behaviour depends on whether we have refresh tokens. If we have,
     * it will use it to get a new one, otherwise it will return null, forcing a
     * re-login.
     */
    private AccessToken getAccessToken(OA2Asset asset)  {
        // First get current access token from the asset
        AccessToken at = asset.getAccessToken();
        if (at==null)   {
            warn("No access token for identifier");
            return null;
        }

        // Do we have refresh tokens?
        RefreshToken rt = asset.getRefreshToken();
        if (rt == null) {
            // We'll have to do with the access token
            try {
                // Don't use ATs valid shorter than SHORT_GRACETIME
                checkTimestamp(at.getToken(), MAX_TIMEOUT-SHORT_GRACETIME);
            } catch (InvalidTimestampException e)   {
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
        } catch (InvalidTimestampException e)   {
            info("Token about to expire, will refresh");
        }

        // TODO: MIGHT BE SIMPLIFIED
        // Do a refresh token request
        RTResponse rtResponse = null;
        String identifier = asset.getIdentifierString();
        try {
            OA2MPService oa2MPService = (OA2MPService)getOA4MPService();
            rtResponse = oa2MPService.refresh(identifier);
        } catch (IOException e) {
            warn("Could not get new refresh token for: "+identifier);
            return null;
        }

        if (rtResponse==null)   {
            warn("Could not get new refresh token for: "+identifier);
            return null;
        }
        // Get the new token, note that the new asset is actually the old one
        // with new tokens.
        at = rtResponse.getAccessToken();
        if (at==null)   {
            warn("No access token for refreshed token for identifier: "+identifier);
            return null;
        }

        return at;
    }

    /**
     * Tries to get the cookie value for the cookie named {@link
     * SPOA2Constants#SSH_CLIENT_REQUEST_ID} from
     * the request.
     */
    private String getCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(SSH_CLIENT_REQUEST_ID))
                    return cookie.getValue();
            }
        }
        // No match
        return null;
    }

    /**
     * Sets a cookie removal for the cookie named {@link
     * SPOA2Constants#SSH_CLIENT_REQUEST_ID} in the
     * response.
     */
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

    /**
     * @return the POST parameters as a Map of key-value pairs. The values are
     * pruned, depending on the type of input. This also reads in the posted ssh
     * keys. This will form the input for the API call to the MasterPortal
     * endpoint.
     * @see SPOA2Constants#PRUNE_PATTERN
     * @see #createSSHKeyRequestParams(Map)
     */
    private Map<String,String> getPostParams(HttpServletRequest request,
               HttpServletResponse response) throws ServletException, IOException {

        int maxFileSize = 50 * 1024;
        //int maxMemSize = 4 * 1024;

        if (!ServletFileUpload.isMultipartContent(request))
            throw new ServletException("No valid multipart content");

        // Handle submit separately

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();
        upload.setSizeMax(maxFileSize);

        Map<String,String> params = new HashMap<>();
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
                                            replaceAll(PRUNE_PATTERN, "_");
                        // Log file
                        if (!item.isFormField())    {
                            info("Getting input from file "+item.getName());
                        }
                    } else {
                        prunedValue = value.replaceAll(PRUNE_PATTERN, "_");
                        info("Adding: ("+name+","+prunedValue+")");
                    }
                    // Add parameter
                    params.put(name, prunedValue);
                }
            }
        } catch (FileUploadException e) {
            throw new ServletException("upload failure");
        }
        return params;
    }

    /**
     * create and return the correct request parameters to be send to the
     * SSH-Key API endpoint.
     * @see #getPostParams(HttpServletRequest, HttpServletResponse)
     */
    private Map<String, String> createSSHKeyRequestParams(Map<String, String> params) {
        String pub_key = null;
        if (params.get(PUB_KEY_FILE) != null && !params.get(PUB_KEY_FILE).isEmpty())    {
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
        HashMap<String,String> postParams = new HashMap<>();

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

    /**
     * @return the Keys in the input json in the form of a List of Map from
     * String to String, i.e. an array of a set of key-value pairs.
     */
    private List<Map<String, String>> getKeysFromJson(String inputJSON) {
        JSON rawJSON = JSONSerializer.toJSON(inputJSON);

        if (!(rawJSON instanceof JSONObject))
           throw new IllegalStateException("Error: Attempted to get JSON Object but returned result is not JSON");

        // Convert to a JSONObject
        JSONObject obj = (JSONObject)rawJSON;
        debug("parsed keys = "+obj.toString());

        Object elem = obj.get(SSH_KEYS);
        if (elem == null)   {
            warn("input is missing ssh_keys node");
            return null;
        }
        // Input might be either simple object or an array
        List<Map<String, String>> list= new ArrayList<>();
        if (elem instanceof JSONArray)  {
            // Need to loop over each entry in the array
            JSONArray arr = (JSONArray)elem;
            for (Object entry : arr) {
                // Add each object as a Map
                list.add(jsonObjecttoMap((JSONObject)entry));
            }
        } else {
            // Add object as a Map
            JSONObject entry = (JSONObject)elem;
            list.add(jsonObjecttoMap(entry));
        }

        return list;
    }

    /**
     * Converts {@link JSONObject} into a
     * key/value {@link Map}&lt;String,String&gt;.
     * @param input input {@link JSONObject}
     * @return key value pair(s) represented as
     * a {@link Map}&lt;String,String&gt;
     */
    private Map<String, String> jsonObjecttoMap(JSONObject input) {
        Map<String, String> map = new HashMap<>();
        Iterator keysItr = input.keys();
        while (keysItr.hasNext()) {
            // Force keys to be String
            String key = keysItr.next().toString();
            // Force the value at this level to be a String
            String value = input.get(key).toString();
            map.put(key, value);
        }
        return map;
    }
}
