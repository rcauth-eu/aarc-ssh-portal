package org.sshkeyportal.client.oauth2.servlet;

public class SPOA2Constants {
    // Prune pattern for input: need to allow also = for Base64. We also allow !
    // and _
    public static final String PRUNEPATTERN="[^\\p{Lower}\\p{Upper}\\p{Digit} ='()+,-_.!?@]";

    // Main SSH Key page
    public static final String SSHKEY_MAIN_PAGE="/pages/main.jsp";
    // Login page
    public static final String SSHKEY_LOGIN_PAGE="/pages/login.jsp";
    // Start request page
    public static final String SSHKEY_PORTAL_START="/startRequest";

    // Name of cookie
    public static final String SSH_CLIENT_REQUEST_ID = "sshkey_portal_req_id";

    // Name of top-level node in returned json
    public static final String SSH_KEYS		= "ssh_keys";

    // SSH Key API on Master Portal
    public static final String API_ACTION	= "action";
    public static final String API_ADD		= "add";
    public static final String API_UPDATE	= "update";
    public static final String API_LIST		= "list";
    public static final String API_REMOVE	= "remove";
    public static final String API_PUB_KEY	= "pubkey";
    public static final String API_LABEL	= "label";
    public static final String API_DESCRIPTION	= "description";

    // main.jsp API
    public static final String SUBMIT		= "submit";
    public static final String SUBMIT_ADD	= "add new public key";
    public static final String SUBMIT_UPDATE	= "update selected key";
    public static final String SUBMIT_REMOVE	= "remove selected key";
    public static final String SUBMIT_LOGOUT	= "logout";
    public static final String PUB_KEY_FILE	= "pubkey_file";
    public static final String PUB_KEY_VALUE	= "pubkey_value";
    public static final String DESCRIPTION	= "description";
    public static final String LABEL		= "label";
    
}