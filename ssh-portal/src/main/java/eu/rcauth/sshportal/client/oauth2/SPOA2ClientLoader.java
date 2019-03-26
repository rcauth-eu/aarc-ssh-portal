package eu.rcauth.sshportal.client.oauth2;

import static eu.rcauth.sshportal.client.oauth2.SPOA2Constants.SSH_KEY_URI;
import static eu.rcauth.sshportal.client.oauth2.SPOA2Constants.SSH_KEY_ENDPOINT;

import org.apache.commons.configuration.tree.ConfigurationNode;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2ClientLoader;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientXMLTags;

import java.net.URI;

/**
 * <p>Created by Mischa Sall&eacute;<br>
 * Loader for the SSHKeyMainServlet client
 * @see eu.rcauth.sshportal.servlet.SSHKeyMainServlet
 */
public class SPOA2ClientLoader<T extends ClientEnvironment> extends OA2ClientLoader<T> {

    public SPOA2ClientLoader(ConfigurationNode node) {
        super(node);
    }

    public URI getsshkeyURI(){
        return createServiceURI(getCfgValue(SSH_KEY_URI), getCfgValue(ClientXMLTags.BASE_URI), SSH_KEY_ENDPOINT);
    }

    @Override
    public String getVersionString() {
        return "SSH Key Portal OAuth2/OIDC client configuration loader version " + VERSION_NUMBER;
    }
}
