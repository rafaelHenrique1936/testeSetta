package siemensinsightshubsinglesignon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.CoreRuntimeException;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IUser;

import inh_org.jose4j.jwt.JwtClaims;
import inh_org.jose4j.jwt.MalformedClaimException;
import siemensinsightshubsinglesignon.proxies.Account;
import siemensinsightshubsinglesignon.proxies.Tenant;
import siemensinsightshubsinglesignon.proxies.constants.Constants;
import system.proxies.UserRole;

public class UserHandler {
	
	
	protected static ILogNode LOGGER = Core.getLogger(Constants.getModuleName());
	
	public UserHandler() {				
	}

	public IUser getOrCreateUser(IContext context, String Name, String eMail, String ten) throws Exception {
		IUser user = Core.getUser(context, Name);
		if (user != null) {
			return user;
		}
		// Get the Tenant
		Tenant tenant = getOrCreateTenant(context, ten);
		// We have to create a new user
		context.startTransaction();
		Account account = new Account(context);
		account.setName(Name);
		account.setPassword(getRandomPWD());
		account.setEmail(eMail);
		account.setActive(true);
		account.setAccount_Tenant(tenant);
		account.setWebServiceUser(false);
		Core.commit(context, account.getMendixObject());
		context.endTransaction();
		return Core.getUser(context, Name);
	}
	
	/**
	 * updating user roles for given user, also setting a new random password for
	 * the user
	 *
	 * @param context
	 * @param iuser
	 * @param claims
	 * @throws CoreException
	 * @throws MalformedClaimException
	 * @throws ScopeRoleMappingException
	 */
	public void updateUserRoles(IContext context, IUser iuser, JwtClaims claims)
			throws CoreException, MalformedClaimException, ScopeRoleMappingException {
		context.startTransaction();
		Account account = Account.initialize(context, iuser.getMendixObject());
		account.setUserRoles(context, getCurrentUserRoles(context, claims));
		// account.setPassword(getRandomPWD()); do we really need this????			
		if (claims.hasClaim("subtenant")) {
			account.setIsSubTenantUser(true);
			account.setSubTenantId(claims.getStringClaimValue("subtenant"));
		} else {
			account.setIsSubTenantUser(false);
			account.setSubTenantId(null);
		}		
		Core.commit(context, account.getMendixObject());		
		context.endTransaction();
	}
	
	private String getUserMail() {
		String userMail = Constants.getUserEmail();
		if (userMail.length() > 0) {
			return userMail;
		}
		return "max.mustermann@email.com";			
	}
	
	/**
	 * Method handles the LocalUser case. We are just creating a session for the
	 * Configured administrator. We do not change any roles for this user.
	 *
	 * @param response
	 * @throws Exception
	 */
	public void handleLocalUser(IMxRuntimeResponse response, IContext context) throws Exception {
		LOGGER.info("Running locally, autologin as MendixAdmin");
		String AdminUserName = Core.getConfiguration().getAdminUserName();
		String userMail = this.getUserMail();
		IUser user = Core.getUser(context, AdminUserName);
		if (user == null) {
			user = getOrCreateUser(context, AdminUserName, userMail, Constants.getUserTenant());
		}
		List<UserRole> appUserRoles = getUserRoles(context, user);
		if (!Core.isSubClassOf(Account.getType(), user.getMendixObject().getType())) {
			context.startTransaction();
			Core.delete(context, user.getMendixObject());
			context.endTransaction();
			user = getOrCreateUser(context, AdminUserName, userMail, Constants.getUserTenant());
		}
		// Make sure local Admin has association to configured Tenant
		// Make sure local Admin has the right Roles
		context.startTransaction();
		Account account = Account.initialize(context, user.getMendixObject());
		account.setAccount_Tenant(getOrCreateTenant(context, Constants.getUserTenant()));
		account.setUserRoles(appUserRoles);
		account.setEmail(userMail);
		Core.commit(context, account.getMendixObject());
		context.endTransaction();		
	}
	
	private String randomAscii(int count) {
		int leftLimit = 33; // letter '!'
		int rightLimit = 126; // letter '~'
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		String generatedString = buffer.toString();
		return generatedString;
	}

	private String getRandomPWD() {
		LOGGER.info("getRandomPWD()");
		String pwd = randomAscii(32);
		String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^\\[\\]()@#$%^&+=])(?=\\S+$).{32,}$";
		if (pwd.matches(pattern)) {
			LOGGER.info("finish getRandomPWD()");
			return pwd;
		} else {
			return getRandomPWD();
		}
	}

	private Tenant getOrCreateTenant(IContext context, String TenantName) throws Exception {
		List<Tenant> TenantList = Tenant.load(context, "[Name='" + TenantName + "']");
		if (TenantList.size() == 0) {
			try {
				context.startTransaction();
				Tenant tenant = new Tenant(context);
				tenant.setName(TenantName);
				Core.commit(context, tenant.getMendixObject());
				context.endTransaction();
				return tenant;
			} catch (CoreRuntimeException e) {
				LOGGER.critical(e.getMessage());
				context.rollbackTransaction();
				throw e;
			}				//
		}
		if (TenantList.size() == 1) {
			return TenantList.get(0);
		}
		throw new Exception("This should never happen!!! Your data is corrupted");
	}
	
		
	private String getAppNameFromClaims(JwtClaims claims, String coreTenantName) {
		String appName;
		try {
			appName = claims.getStringClaimValue("client_id");
			appName = appName.replaceFirst("mobile-client-", "");
			appName = appName.substring(0, appName.indexOf("-"));
			if (coreTenantName != null) {
				appName = "mdsp:" + coreTenantName + ":" + appName;
				LOGGER.info("AppName=" + appName);
			}
			return appName;
		} catch (MalformedClaimException e) { 
			LOGGER.error(e);
			return "";
		}				
	}
	/**
	 * get current user roles based on provided scopes in JWT. application scope
	 * name is compared ignoring cases to all available Mendix roles only matching
	 * roles are returned In case of an error some warnings are written to the log.
	 *
	 * @param context
	 * @param claims
	 * @return
	 * @throws CoreException
	 * @throws ScopeRoleMappingException
	 * @throws MalformedClaimException
	 */
	private List<UserRole> getCurrentUserRoles(IContext context, JwtClaims claims)
			throws CoreException, ScopeRoleMappingException, MalformedClaimException {
		// initialize return object as empty List
		List<UserRole> userRoles = new ArrayList<UserRole>();
		// Get a list of all application scopes.
		List<String> scopeList;
		String appName  = this.getAppNameFromClaims(claims,System.getenv("CORE_TENANT_NAME"));
		try {
			scopeList = claims.getStringListClaimValue("scope");			
		} catch (MalformedClaimException e) {
			LOGGER.error(e.getMessage());
			return userRoles;
		}
		// Remove all scopes which are not defined for the application
		scopeList.removeIf(s -> !s.startsWith(appName));
		scopeList = scopeList.stream().map(scope -> scope.replaceFirst(appName + ".", "")).collect(Collectors.toList());
		// Get all roles defined for the application
		List<UserRole> appUserRoles = UserRole.load(context, "");

		// loop through list of scopes and check for each scope if a UserRole has the
		// same name as the scope provided
		scopeList.forEach(scope -> {
			appUserRoles.forEach(userRole -> {
				LOGGER.info("InsightsHub Scope: " + scope + " - MendixUserRole: "	+ userRole.getName());
				if (scope.equalsIgnoreCase(userRole.getName())) {
					LOGGER.info("Assigning UserRole '" + userRole.getName() + "'");
					userRoles.add(userRole);
				}
			});
		});
		if (userRoles.isEmpty()) {
			LOGGER.warn("No Roles assigned to the user.");
			LOGGER.warn("InsightsHub scopes: " + String.join(",", scopeList));
			LOGGER.warn("Mendix Roles: " + appUserRoles.stream().map(ele -> ele.getName()).collect(Collectors.joining(",")));
			throw new ScopeRoleMappingException(String.join(",", scopeList), appUserRoles.stream().map(ele -> ele.getName()).collect(Collectors.joining(",")));
		}
		return userRoles;
	}
		
	private List<UserRole> getUserRoles(IContext context, IUser user) throws CoreException {
		Set<String> userRoles = user.getUserRoleNames();
		List<UserRole> appUserRoles = UserRole.load(context, "");
		appUserRoles.removeIf(e -> !userRoles.contains(e.getName()));
		return appUserRoles;
	}
	
}
