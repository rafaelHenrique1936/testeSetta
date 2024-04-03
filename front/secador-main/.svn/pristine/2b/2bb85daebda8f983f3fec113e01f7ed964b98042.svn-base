// Build the clientId based on the HostTenant, CockpitApplicationName and the CockpitApplicationVersion
// Try to read out environment variable called like the clientId and returns the value as secret

package siemensinsightshubsinglesignon;

import java.util.Map;
import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import siemensinsightshubsinglesignon.proxies.constants.Constants;


/**
 * Check if user provides ClientSecret as local environment variable.
 */
public class SecretFromEnvironment
{
	private java.lang.String ClientId;
	private java.lang.Boolean PrintEnvToConsole;

	public SecretFromEnvironment()
	{
		this.ClientId = Constants.getHostTenant() + '-' + Constants.getCockpitApplicationName() + '-' + Constants.getCockpitApplicationVersion();
		this.PrintEnvToConsole = false;
	}

	public java.lang.String getClientId()
	{
		return this.ClientId;
	}
	
	public java.lang.String getClientSecret() throws Exception
	{
		ILogNode LOGGER = Core.getLogger(Constants.getModuleName());
		if (this.PrintEnvToConsole == true) {
			Map<String, String> envVars  = System.getenv();
			for (Map.Entry<String, String> entry : envVars.entrySet()) {
			    LOGGER.info(entry.getKey() + " = " + entry.getValue());
			}
		}
		String ClientSecret=System.getenv(ClientId);
		if (ClientSecret != null) {
			return ClientSecret;
		} else {
			return "";
		}
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "SecretfromEnvironment";
	}
}
