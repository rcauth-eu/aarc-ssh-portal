package eu.rcauth.sshportal.client.oauth2;

import static eu.rcauth.sshportal.client.oauth2.SPOA2Constants.SP_OA2_CONFIG_FILE_KEY;
import static eu.rcauth.sshportal.client.oauth2.SPOA2Constants.SP_OA2_CONFIG_NAME_KEY;

import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2ClientBootstrapper;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.servlet.Initialization;

import org.apache.commons.configuration.tree.ConfigurationNode;

import eu.rcauth.sshportal.servlet.SSHKeyMainServlet;

/**
 * <p>Created by Mischa Sall&eacute;<br>
 *  Bootstraps SSH Key Portal OA4MP Client
 * @see SSHKeyMainServlet
 */
public class SPOA2ClientBootstrapper extends OA2ClientBootstrapper {

    @Override
    public String getOa4mpConfigFileKey() {
        return SP_OA2_CONFIG_FILE_KEY;
    }

    @Override
    public String getOa4mpConfigNameKey() {
        return SP_OA2_CONFIG_NAME_KEY;
    }

    @Override
    public ConfigurationLoader getConfigurationLoader(ConfigurationNode node) throws MyConfigurationException {
        return new SPOA2ClientLoader(node);
    }

    @Override
    public Initialization getInitialization() {
        return new SPOA2ClientServletInitializer();
    }

}
