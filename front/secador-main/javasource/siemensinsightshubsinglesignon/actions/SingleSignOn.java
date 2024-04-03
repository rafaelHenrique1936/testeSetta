// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package siemensinsightshubsinglesignon.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import inh_org.jose4j.jwt.JwtClaims;
import inh_org.jose4j.jwt.consumer.InvalidJwtException;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.CoreRuntimeException;
import com.mendix.core.conf.RuntimeVersion;
import com.mendix.externalinterface.connector.RequestHandler;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.ISession;
import com.mendix.systemwideinterfaces.core.IUser;
import com.mendix.webui.CustomJavaAction;
import siemensinsightshubsinglesignon.IPublicKeys;
import siemensinsightshubsinglesignon.NoJWTException;
import siemensinsightshubsinglesignon.PubKeyException;
import siemensinsightshubsinglesignon.ScopeRoleMappingException;
import siemensinsightshubsinglesignon.VerifyJWT;
import siemensinsightshubsinglesignon.ApiReverseProxy;
import siemensinsightshubsinglesignon.AppCredentialsPage;
import siemensinsightshubsinglesignon.CredentialsHandler;
import siemensinsightshubsinglesignon.LogoutHandler;
import siemensinsightshubsinglesignon.proxies.constants.Constants;
import siemensinsightshubsinglesignon.proxies.microflows.Microflows;
import siemensinsightshubsinglesignon.AutoRegistration;
import siemensinsightshubsinglesignon.UserHandler;
import siemensinsightshubsinglesignon.Util;

public class SingleSignOn extends CustomJavaAction<java.lang.Boolean>
{
	private java.lang.String AppName;

	public SingleSignOn(IContext context, java.lang.String AppName)
	{
		super(context);
		this.AppName = AppName;
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		// BEGIN USER CODE
		LOGGER.info("Starting up SingleSignOn Request Handler ... ");
		boolean local = System.getenv("VCAP_SERVICES") == null;
		IPublicKeys publicKeys = new PublicKeys();
		SingleSignOnRequestHandler ssoHandler = new SingleSignOnRequestHandler(this.AppName, local, publicKeys); 				
		Core.addRequestHandler("sso", ssoHandler);
		Core.addRequestHandler("sso/", ssoHandler);		
		if (local && Constants.getEnableLocalApiReverseProxy()) {
			Core.addRequestHandler("api/", new ApiReverseProxy(Core.http()));
		}
		if (local && Constants.getAskForCredentialsOnStartup()) {
			Core.addRequestHandler("credentials", new CredentialsHandler());			
			Core.addRequestHandler("credentials.html", new AppCredentialsPage());			
		}
		LogoutHandler logoutHandler = new LogoutHandler();
		Core.addRequestHandler("logout/", logoutHandler);
		Core.addRequestHandler("logout", logoutHandler);
		LOGGER.info("Starting up SingleSignOn Request Handler ... done");
		return true;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "SingleSignOn";
	}

	// BEGIN EXTRA CODE
	protected static ILogNode LOGGER = Core.getLogger(Constants.getModuleName());
	private static final String XAS_ID = "xasid";
	private static UserHandler userHandler = new UserHandler();
	
	public class PublicKeys implements IPublicKeys {
		@Override
		public String getPublicKeys() {
			return Microflows.gET_PublicKeys(context());
		}
	}

	/**
	 * SingleSignOnRequestHandler for login automatically into Mendix application
	 * based on provided MindSphere JWT.
	 *
	 * In case that the application is running locally a session is created for the
	 * configured Mendix administrator account. In case that the application is
	 * running on MindSphere the JWT is validated. The following claims are checked:
	 * Issuer, Signature, Audience
	 *
	 * In case of an error during processing the request the application is
	 * redirected to "error_page/403.html"
	 */
	public class SingleSignOnRequestHandler extends RequestHandler {

		private String appName;
		private String queryString;
		private String redirectTo;
		private boolean local = false;
		private IPublicKeys publicKeys = null;
		
		private HashMap<String, VerifyJWT> verifyJWTCache = new HashMap<String, VerifyJWT>();
		

		public SingleSignOnRequestHandler(String AppName, boolean Local, IPublicKeys PublicKeys) {
			super();
			this.appName = AppName;
			this.local = Local;			
			this.publicKeys = PublicKeys;
		}
				

		@Override
		protected void processRequest(IMxRuntimeRequest request, IMxRuntimeResponse response, String path) {

			try {
				// return BAD_REQUEST if we do not have a GET Request
				if (!"GET".equals(request.getHttpServletRequest().getMethod())) {
					LOGGER.warn("SSO request not of type 'GET'");
					response.setStatus(IMxRuntimeResponse.BAD_REQUEST);
					return;
				}

				// Get queryString
				this.queryString = request.getHttpServletRequest().getQueryString();
				// Save redirect_uri if present, needed for deep link-module
				// https://docs.mendix.com/appstore/modules/deep-link
				this.redirectTo = request.getHttpServletRequest().getParameter("redirect_uri");

				// Get SudoContext for creating a new user
				IContext context = getSudoContext(request);

				// Check if we have a Token and run in Cloud
				if (!hasAuthorizationToken(request) && !this.local) {
					// No token --> redirect to AutoRegistration
					AutoRegistration autoRegistration = new AutoRegistration();
					autoRegistration.redirectToAutoRegistration(response);
					return;
				}

				// check if we run locally
				if (this.local) {
					userHandler.handleLocalUser(response, context);
					createSession(response, context, Core.getUser(context, Core.getConfiguration().getAdminUserName()));					
					redirectToIndex(response);
					return;
				}

				// check if publicKeyUrl matches issuer Url
				this.checkPublicKeyURL(request);

				// Validate JWT and get claims
				JwtClaims claims = getClaims(request);

				// Get the user_id from JWT. The user_id is unique across all tenants
				String userId = claims.getStringClaimValue("user_id");
				String eMail = claims.getStringClaimValue("email");
				String tenant = claims.getStringClaimValue("ten");

				// Check if there is already a Session established for the given user
				IUser user = context.getSession().getUser(context);
				if (user != null && user.getName().equals(userId)) {
					userHandler.updateUserRoles(context, user, claims);
					redirectToIndex(response);
					return;
				}

				// Create user if not exist based on userId
				user = userHandler.getOrCreateUser(context, userId, eMail, tenant);
				userHandler.updateUserRoles(context, user, claims);
				createSession(response, context, user);
				redirectToIndex(response);
			} catch (ScopeRoleMappingException e) {
				LOGGER.error(e.getMessage(), e);
				redirectToErrorPage(response,
						"/error_page/RoleMapping.html?mdspScopes=" + e.getScopes() + "&mendixRoles=" + e.getRoles());
			} catch (PubKeyException e) {
				LOGGER.error(e.getMessage(), e);
				redirectToErrorPage(response,
						"/error_page/PublicKeyUrl.html?PubKey=" + e.getPubKey() + "&Issuer=" + e.getIssuer());
				
			} catch(CoreRuntimeException e) {
				LOGGER.error(e.getMessage(), e);
				Throwable rootCause = Util.getRootCause(e);
				LOGGER.error(rootCause.getMessage());
				if (rootCause.getClass().toString().contains("LicenseRuntimeException")) {					
					redirectToErrorPage(response,"/error_page/LicenseException.html?message="+rootCause.getMessage());
				} else {
					redirectToErrorPage(response, "/error_page/403.html");
				}
			} catch (Throwable e) {
				LOGGER.error(e.getMessage(), e);				
				redirectToErrorPage(response, "/error_page/403.html");				
			}
		}
			
		
		private boolean hasAuthorizationToken(IMxRuntimeRequest request) {
			String AuthHeader = request.getHeader("authorization");
			String AuthParam = request.getParameter("authorization");
			if (AuthHeader != null && AuthHeader.length() > 0 || AuthParam != null && AuthParam.length() > 0) {
				return true;
			} else {
				return false;
			}
		}

		private void checkPublicKeyURL(IMxRuntimeRequest request) throws PubKeyException, Exception {
			// Public Key URL e.g.: https://core.piam.{Region}.{Domain}/token_keys
			// Issuer e.g.: https://{Tenant}.piam.{Region}.{Domain}/oauth/token
			JwtClaims claims = VerifyJWT.getClaimsNoSecurityCheck(request.getHeader("authorization"));
			String issuerHost = claims.getIssuer().substring(claims.getIssuer().indexOf("."),
					claims.getIssuer().lastIndexOf("/oauth"));
			if (!Constants.getPublicKeyURL().contains(issuerHost)) {
				URL issuerURL = new URL(claims.getIssuer());
				URL pubKeyURL = new URL(Constants.getPublicKeyURL());
				String iss = issuerURL.getProtocol() + "://" + issuerURL.getHost();
				String pubKey = pubKeyURL.getProtocol() + "://" + pubKeyURL.getHost();
				LOGGER.info("iss = " + iss);
				LOGGER.info("pubKey = " + pubKey);
				PubKeyException ex = new PubKeyException(pubKey, iss);
				throw ex;
			}
		}

		private void redirectToErrorPage(IMxRuntimeResponse response, String location) {
			response.setStatus(IMxRuntimeResponse.SEE_OTHER);
			response.addHeader("Location", location);
		}

		/**
		 * create a new session for given user, redirects to index.html
		 *
		 * @param response
		 * @param context
		 * @param user
		 * @throws CoreException
		 * @throws InvocationTargetException 
		 * @throws IllegalArgumentException 
		 * @throws IllegalAccessException 
		 * @throws SecurityException 
		 * @throws NoSuchMethodException 
		 */
		private void createSession(IMxRuntimeResponse response, IContext context, IUser user) throws CoreException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			// Logout if currentSession is SystemSession
			ISession currentSession = context.getSession();
			if (currentSession != null && !currentSession.isSystemSession()) {
				Core.logout(currentSession);
			}
		
			// Create a new Session
			UUID uuid = UUID.randomUUID();
			ISession newSession = Core.initializeSession(user, uuid.toString());
			setCookies(response, newSession);			
		}

		/**
		 * add session cookies to response
		 *
		 * @param response
		 * @param session
		 * @throws SecurityException 
		 * @throws NoSuchMethodException 
		 * @throws InvocationTargetException 
		 * @throws IllegalArgumentException 
		 * @throws IllegalAccessException 
		 */
		private void setCookies(IMxRuntimeResponse response, ISession session) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {			
			response.addCookie(Core.getConfiguration().getSessionIdCookieName(), session.getId().toString(), "/", "", -1, true,true);			
			response.addCookie(XAS_ID, "0." + Core.getXASId(), "/", "", -1, true);
		}

		private String getQueryString() {
			return this.queryString != null ? "?" + this.queryString : "";
		}

		private String getRedirectTo() {
			return (this.redirectTo != null && !this.redirectTo.isEmpty()) ? this.redirectTo
					: "/index.html" + this.getQueryString();
		}

		private void redirectToIndex(IMxRuntimeResponse response) {
			response.setStatus(IMxRuntimeResponse.SEE_OTHER);
			if (this.local && Constants.getAskForCredentialsOnStartup()) {
				response.addHeader("Location", "/credentials.html" + this.getQueryString());
			} else {
				response.addHeader("Location", this.getRedirectTo());
			}
		}

		private IContext getSudoContext(IMxRuntimeRequest request) throws CoreException {
			// Create Context from Session if available
			ISession oldSession = this.getSessionFromRequest(request);
			IContext context = null;
			if (oldSession != null) {
				context = oldSession.createContext();
			} else {
				context = Core.createSystemContext();
			}

			if (!context.isSudo()) {
				context = context.createSudoClone();
			}
			return context;
		}

		/**
		 * returns claims of the request after token validation
		 *
		 * @param request
		 * @return JwtClaims
		 * @throws InvalidJwtException
		 * @throws NoJWTException
		 */
		private JwtClaims getClaims(IMxRuntimeRequest request) throws InvalidJwtException, NoJWTException {
			VerifyJWT verifyJWT = getVerifyJWT(getTrustedIssuer(request, System.getenv("CORE_TENANT_NAME")));
			String jwt = request.getHeader("authorization");
			if (jwt == null || jwt == "") {
				throw new NoJWTException();
			}
			return verifyJWT.getJwtClaims(jwt);
		}

		/**
		 * Simple Cache for the Token Validation Class. As the creation of this object
		 * is expensive we put them in a HashMap.
		 *
		 * @param trustedIssuer
		 * @return
		 */
		private VerifyJWT getVerifyJWT(String trustedIssuer) {
			String Key = trustedIssuer;
			if (!verifyJWTCache.containsKey(Key)) {
				verifyJWTCache.put(Key, new VerifyJWT(appName, trustedIssuer, this.publicKeys));
			}
			return verifyJWTCache.get(Key);
		}

		/**
		 * Method generates the TrustedIssuer String based on requestURL In case of an
		 * error method returns an empty string.
		 *
		 * @param request
		 * @return The TrustedIssuer which must be checked in the JWT.
		 */
		private String getTrustedIssuer(IMxRuntimeRequest request, String coreTenantName) {
			// Trusted Issuer URL has the following format
			// https://<Tenant>.piam.<Env>.mindsphere.io/oauth/token
			// With <Tenant> = Provider Name
			// With <Env> on PROD = eu1 or on PROD-B = eu1-b
			try {
				// Referer header looks like:
				// https://<Tenant>-<AppName>-<Provider>.<Env>.mindsphere.io
				URL refererURL = new URL(request.getHeader("referer"));
				LOGGER.info(refererURL);
				String Host = refererURL.getHost();
				String Part = Host.substring(0, Host.indexOf("."));
				String TrustedIssuer = "https://" + Part.substring(Part.lastIndexOf("-") + 1) + ".piam"
						+ Host.substring(Host.indexOf(".")) + "/oauth/token";
				if (coreTenantName != null) {
					TrustedIssuer = "https://" + coreTenantName + ".piam"
						+ Host.substring(Host.indexOf(".")) + "/oauth/token";
				}
				LOGGER.info("TrustedIssuer = " + TrustedIssuer);
				return TrustedIssuer;
			} catch (MalformedURLException e) {
				LOGGER.error(e.getMessage());
				return "";
			}
		}	
	}
	// END EXTRA CODE
}
