package org.sshkeyportal.client.oauth2;

import org.apache.commons.configuration.tree.ConfigurationNode;
import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2ClientLoader;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientEnvironment;
import edu.uiuc.ncsa.myproxy.oa4mp.client.ClientXMLTags;

import java.net.URI;

public class SPOA2ClientLoader<T extends ClientEnvironment> extends OA2ClientLoader<T> {
    public static final String SSH_KEY_URI = "sshkeyUri";
    public static final String SSH_KEY_ENDPOINT = "sshkey";


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
