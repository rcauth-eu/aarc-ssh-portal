package eu.rcauth.sshportal.client.oauth2;

import static eu.rcauth.sshportal.client.oauth2.SPOA2Constants.*;

import org.apache.commons.configuration.tree.ConfigurationNode;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2ClientLoader;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientXMLTags;

import java.net.URI;

import eu.rcauth.sshportal.servlet.SSHKeyMainServlet;


/**
 * <p>Created by Mischa Sall&eacute;<br>
 * Loader for the {@link SSHKeyMainServlet} client
 * @see SSHKeyMainServlet
 */
public class SPOA2ClientLoader<T extends ClientEnvironment> extends OA2ClientLoader<T> {

    public SPOA2ClientLoader(ConfigurationNode node) {
        super(node);
    }

    /** @return the sshkeys URI or a default based on
     * {@link ClientXMLTags#BASE_URI} and {@link SPOA2Constants#SSH_KEY_ENDPOINT} */
    public URI getsshkeyURI(){
        return createServiceURI(getCfgValue(SSH_KEY_URI), getCfgValue(ClientXMLTags.BASE_URI), SSH_KEY_ENDPOINT);
    }

    /**
     * Get the configured name of the claim for getting the user's name
     * @param defVal default value
     * @return configured value or defVal in case unset
     */
    public String getNameClaim(String defVal) {
        String value = getCfgValue(NAME_CLAIM_TAG);
        return value == null ? defVal : value;
    }

    /**
     * Get the configured name of the claim for getting the user's given name
     * @param defVal default value
     * @return configured value or defVal in case unset
     */
    public String getGivenNameClaim(String defVal) {
        String value = getCfgValue(GIVEN_NAME_CLAIM_TAG);
        return value == null ? defVal : value;
    }

    /**
     * Get the configured name of the claim for getting the user's family name
     * @param defVal default value
     * @return configured value or defVal in case unset
     */
    public String getFamilyNameClaim(String defVal) {
        String value = getCfgValue(FAMILY_NAME_CLAIM_TAG);
        return value == null ? defVal : value;
    }

    /**
     * Get the configured name of the claim for getting the IdP's display name
     * @param defVal default value
     * @return configured value or defVal in case unset
     */
    public String getIdpDisplayNameClaim(String defVal) {
        String value = getCfgValue(IDP_DISPLAY_NAME_CLAIM_TAG);
        return value == null ? defVal : value;
    }

    @Override
    public String getVersionString() {
        return "SSH Key Portal OAuth2/OIDC client configuration loader version " + VERSION_NUMBER;
    }
}
