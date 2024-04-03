package siemensinsightshubsinglesignon;

import java.util.Hashtable;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IUser;

import inh_org.jose4j.jwt.JwtClaims;
import inh_org.jose4j.jwt.consumer.InvalidJwtException;
import siemensinsightshubsinglesignon.proxies.constants.Constants;
import siemensinsightshubsinglesignon.proxies.microflows.Microflows;
import system.proxies.HttpHeader;
import system.proxies.HttpResponse;


public class CheckAuthentication implements IPublicKeys {
	
	protected static ILogNode LOGGER = Core.getLogger(Constants.getModuleName());

	static private VerifyJWT verifyJWTInstance = null;
	static private Hashtable<String, IMendixObject> cachedUsers = new Hashtable<String, IMendixObject>(100);

	private String authorization = null;
	private IContext context = null;
	private HttpResponse response = null;

	public CheckAuthentication(IContext context, HttpResponse response, String authorization) {
		this.context = context;
		this.response = response;
		this.authorization = authorization;	
	}
	
	public IMendixObject check() {
		if (!this.hasAuthorizationToken()) {
			return null;
		}
		if (!cachedUsers.containsKey(authorization)) {
			IMendixObject user = this.getUser();
			if (user != null) {
				cachedUsers.put(authorization, user);
			}
		}
		return cachedUsers.get(authorization);
	}

	@Override
	public String getPublicKeys() throws MendixRuntimeException {
		return Microflows.gET_PublicKeys(Core.createSystemContext());
	}

	private boolean hasAuthorizationToken() {
		if (authorization == null || authorization.isEmpty()) {
			response.setStatusCode(401);
			response.setContent("Unauhtorized");
			HttpHeader wwwAuthenticateHeader = new HttpHeader(this.context);
			wwwAuthenticateHeader.setKey("WWW-Authenticate");
			wwwAuthenticateHeader.setValue("Bearer realm=\"token missing!\", charset=\"UTF-8\"");
			wwwAuthenticateHeader.setHttpHeaders(this.context, response);
			return false;
		}
		return true;
	}

	private VerifyJWT getVerifyJWT() {
		if (verifyJWTInstance != null) {
			return verifyJWTInstance;
		}		
		verifyJWTInstance = new VerifyJWT(Constants.getCockpitApplicationName(), this);
		return verifyJWTInstance;
	}

	private IMendixObject getUser() {
		try {
			JwtClaims claims = getVerifyJWT().getJWTClaimsNoIssuerCheck(authorization);
			if (claims.hasClaim("user_id") && claims.hasClaim("email") && claims.hasClaim("ten")) {
				UserHandler userHandler = new UserHandler();
				IUser user = Core.getUser(context, claims.getStringClaimValue("user_id"));

				if (user != null) {
					userHandler.updateUserRoles(context, user, claims);
				} else {
					user = userHandler.getOrCreateUser(context, claims.getStringClaimValue("user_id"),
							claims.getStringClaimValue("email"), claims.getStringClaimValue("ten"));
					userHandler.updateUserRoles(context, user, claims);
				}
				return user.getMendixObject();
			} else {
				// No User Token. Unknown what to do in such a case...
				LOGGER.error("No User Token: " + claims.toJson());
				response.setStatusCode(403);
				response.setContent("TechUser Token not supported");
				return null;
			}
		} catch (InvalidJwtException e) {
			LOGGER.error(e.getMessage());
			response.setStatusCode(403);
			response.setContent("Forbidden");
			return null;
		} catch(ScopeRoleMappingException e) {
			LOGGER.error(e.getMessage());
			response.setStatusCode(403);
			response.setContent("Forbidden");
			return null;
		} catch (Throwable e) {
			LOGGER.error(e.getMessage());
			response.setStatusCode(403);
			response.setContent("Unknown error occured");
			return null;
		}
	}
}