package siemensinsightshubsinglesignon;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.externalinterface.connector.RequestHandler;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.IContext;

import siemensinsightshubsinglesignon.proxies.Credentials;
import siemensinsightshubsinglesignon.proxies.CredentialsResponse;
import siemensinsightshubsinglesignon.proxies.constants.Constants;
import siemensinsightshubsinglesignon.proxies.microflows.Microflows;

public class CredentialsHandler extends RequestHandler {

	protected static ILogNode LOGGER = Core.getLogger(Constants.getModuleName());
	static private String CREDENTIAL_SESSION_ATTR_NAME = "CredentialsResponse";
	static private String clientId = "";
	static private String clientSecret = "";

	public static void storeCredentialsResponsInSession(IMxRuntimeRequest request, CredentialsResponse appCredResp) {
		request.getHttpServletRequest().getSession().setAttribute(CREDENTIAL_SESSION_ATTR_NAME, appCredResp);
	}

	private static void storeClientIdClientSecret(IMxRuntimeRequest request) {
		clientId = request.getParameter("clientId");
		clientSecret = request.getParameter("clientSecret");
	}

	public static String getClientId() {
		return clientId;
	}

	public static String getClientSecret() {
		return clientSecret;
	}

	public static CredentialsResponse retrievCredentialsResponsFromSession(IMxRuntimeRequest request) {
		CredentialsResponse credentialsResponse = (CredentialsResponse) request.getHttpServletRequest().getSession()
				.getAttribute(CREDENTIAL_SESSION_ATTR_NAME);

		if (credentialsResponse != null) {
			// Check if token is still valid.
			if (credentialsResponse.getExpires_at().getTime() > System.currentTimeMillis()) {
				return credentialsResponse;
			} else {
				// Try to fetch new Token
				CredentialsHandler cdHandler = new CredentialsHandler();
				try {
					CredentialsResponse resp = cdHandler.getTokenfromApp_Service_Credentials(request);
					if (resp != null && resp.getStatusCode() == 200) {
						storeCredentialsResponsInSession(request, resp);
						return resp;
					}
				} catch (CoreException ex) {
					return null;
				}
			}
		}
		return credentialsResponse;

	}

	public static void removeCredentialsFromSession(IMxRuntimeRequest request) {
		request.getHttpServletRequest().getSession().removeAttribute(CREDENTIAL_SESSION_ATTR_NAME);
	}

	private CredentialsResponse getTokenfromApp_Service_Credentials(IMxRuntimeRequest request) throws CoreException {
		// Let's call the application Credentials Microflow to do the work
		Credentials appcreds = new Credentials(getContext(request));
		appcreds.setClientID(clientId);
		appcreds.setClientSecret(clientSecret);
		appcreds.setappName(Constants.getCockpitApplicationName());
		appcreds.setappVersion(Constants.getCockpitApplicationVersion());
		appcreds.sethostTenant(Constants.getHostTenant());
		appcreds.setuserTenant(Constants.getUserTenant());		
	
		
		return  Microflows.aCT_ApplicationCredentials(getContext(request), appcreds);			
	}

	@Override
	protected void processRequest(IMxRuntimeRequest request, IMxRuntimeResponse response, String path)
			throws Exception {
		if (!"POST".equals(request.getHttpServletRequest().getMethod())) {
			LOGGER.warn("appcredentials request not of type 'POST'");
			response.setStatus(IMxRuntimeResponse.BAD_REQUEST);
			return;
		}
		storeClientIdClientSecret(request);
		CredentialsResponse resp = this.getTokenfromApp_Service_Credentials(request);

		if (resp == null || (resp != null && resp.getStatusCode() != null && resp.getStatusCode() != 200)) {
			int statusCode = 0;
			String errorMessage = "";
			String errorType = "";

			if (resp != null) {
				statusCode = resp.getStatusCode();
				errorMessage = resp.getErrorMessage();
				errorType = resp.getErrorType();
			}
			
			AppCredentialsPage page = new AppCredentialsPage();
			page.WriteAppCredPageToResponse(response, true, statusCode, request.getParameter("queryString"), errorMessage, errorType);
			
		} else {
			storeCredentialsResponsInSession(request, resp);
			// Redirect to home
			response.setStatus(IMxRuntimeResponse.SEE_OTHER);
			String queryString = request.getParameter("queryString");	
			response.addHeader("Location", this.getRedirectTo(queryString));			
		}
	}

	private String getRedirectTo(String queryString) {
		if (queryString != null && !queryString.isEmpty()) {
			String[] pairs = queryString.split("&");
			for (String pair : pairs) {
				int idx = pair.indexOf("=");
				if (pair.substring(0, idx).compareToIgnoreCase("redirect_uri") == 0) {
					return pair.substring(idx + 1);
				}
			}
			return "/index.html?"+queryString;
		}
		return "/index.html";
	}

	private IContext getContext(IMxRuntimeRequest request) throws CoreException {
		return this.getSessionFromRequest(request).createContext();
	}
}