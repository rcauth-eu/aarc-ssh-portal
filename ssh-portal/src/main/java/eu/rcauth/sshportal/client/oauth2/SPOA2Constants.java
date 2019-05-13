package eu.rcauth.sshportal.client.oauth2;

import eu.rcauth.sshportal.servlet.SSHKeyMainServlet;

/**
 * <p>Created by Mischa Sall&eacute;<br>
 * Various constants used by the {@link SSHKeyMainServlet}
 * @see SSHKeyMainServlet
 */
public final class SPOA2Constants {
    // Config settings for /etc/tomcat/web.xml
    public static final String SP_OA2_CONFIG_FILE_KEY = "oa4mp:sp-oa2.client.config.file";
    public static final String SP_OA2_CONFIG_NAME_KEY = "oa4mp:sp-oa2.client.config.name";

    // SSH_KEY_URI is used as sshkey endpoint. When that's unset, the
    // SSH_KEY_ENDPOINT will be used on the Master Portal
    public static final String SSH_KEY_URI = "sshkeyUri";
    public static final String SSH_KEY_ENDPOINT = "sshkey";

    // Tags in config file for defining the different claim names
    public static final String NAME_CLAIM_TAG             = "nameClaim";
    public static final String GIVEN_NAME_CLAIM_TAG       = "givenNameClaim";
    public static final String FAMILY_NAME_CLAIM_TAG      = "familyNameClaim";
    public static final String IDP_DISPLAY_NAME_CLAIM_TAG = "idpDisplayNameClaim";

    // Prune pattern for input: need to allow also = and / for Base64. We also
    // allow ! and _. Need to escape - and .
    public static final String PRUNE_PATTERN="[^\\p{Lower}\\p{Upper}\\p{Digit} ='()+,\\-_\\./!?@]";

    // Main SSH Key page
    public static final String SSHKEY_MAIN_PAGE="/pages/main.jsp";
    // Login page
    public static final String SSHKEY_LOGIN_PAGE="/pages/login.jsp";
    // Start request page
    public static final String SSHKEY_PORTAL_START="/startRequest";

    // Name of cookie
    public static final String SSH_CLIENT_REQUEST_ID = "sshkey_portal_req_id";

    // Name of top-level node in returned json
    public static final String SSH_KEYS         = "ssh_keys";

    // SSH Key API on Master Portal
    public static final String API_ACTION       = "action";
    public static final String API_ADD          = "add";
    public static final String API_UPDATE       = "update";
    public static final String API_LIST         = "list";
    public static final String API_REMOVE       = "remove";
    public static final String API_PUB_KEY      = "pubkey";
    public static final String API_LABEL        = "label";
    public static final String API_DESCRIPTION  = "description";

    // main.jsp API
    public static final String SUBMIT           = "submit";
    public static final String SUBMIT_ADD       = "add new public key";
    public static final String SUBMIT_UPDATE    = "update selected key";
    public static final String SUBMIT_REMOVE    = "remove selected key";
    public static final String SUBMIT_LOGOUT    = "logout";
    public static final String PUB_KEY_FILE     = "pubkey_file";
    public static final String PUB_KEY_VALUE    = "pubkey_value";
    public static final String DESCRIPTION      = "description";
    public static final String LABEL            = "label";

}
