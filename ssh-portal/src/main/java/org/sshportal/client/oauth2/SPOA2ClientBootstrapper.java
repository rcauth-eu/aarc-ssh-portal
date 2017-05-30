package org.sshkeyportal.client.oauth2;

import edu.uiuc.ncsa.oa4mp.oauth2.client.OA2ClientBootstrapper;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import edu.uiuc.ncsa.security.servlet.Initialization;

import org.apache.commons.configuration.tree.ConfigurationNode;

/*
 *  Bootstraps SSH Key Portal OA4MP Client
 */
public class SPOA2ClientBootstrapper extends OA2ClientBootstrapper {
	
    public static final String SP_OA2_CONFIG_FILE_KEY = "oa4mp:sp-oa2.client.config.file";
    public static final String SP_OA2_CONFIG_NAME_KEY = "oa4mp:sp-oa2.client.config.name";

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
