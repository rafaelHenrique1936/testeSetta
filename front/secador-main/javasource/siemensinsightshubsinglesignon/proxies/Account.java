// This file was generated by Mendix Studio Pro.
//
// WARNING: Code you write here will be lost the next time you deploy the project.

package siemensinsightshubsinglesignon.proxies;

public class Account extends system.proxies.User
{
	/**
	 * Internal name of this entity
	 */
	public static final java.lang.String entityName = "SiemensInsightsHubSingleSignOn.Account";

	/**
	 * Enum describing members of this entity
	 */
	public enum MemberNames
	{
		Email("Email"),
		IsSubTenantUser("IsSubTenantUser"),
		SubTenantId("SubTenantId"),
		Name("Name"),
		Password("Password"),
		LastLogin("LastLogin"),
		Blocked("Blocked"),
		BlockedSince("BlockedSince"),
		Active("Active"),
		FailedLogins("FailedLogins"),
		WebServiceUser("WebServiceUser"),
		IsAnonymous("IsAnonymous"),
		Account_Tenant("SiemensInsightsHubSingleSignOn.Account_Tenant"),
		UserRoles("System.UserRoles"),
		User_Language("System.User_Language"),
		User_TimeZone("System.User_TimeZone");

		private final java.lang.String metaName;

		MemberNames(java.lang.String s)
		{
			metaName = s;
		}

		@java.lang.Override
		public java.lang.String toString()
		{
			return metaName;
		}
	}

	public Account(com.mendix.systemwideinterfaces.core.IContext context)
	{
		this(context, com.mendix.core.Core.instantiate(context, entityName));
	}

	protected Account(com.mendix.systemwideinterfaces.core.IContext context, com.mendix.systemwideinterfaces.core.IMendixObject accountMendixObject)
	{
		super(context, accountMendixObject);
		if (!com.mendix.core.Core.isSubClassOf(entityName, accountMendixObject.getType())) {
			throw new java.lang.IllegalArgumentException(String.format("The given object is not a %s", entityName));
		}	
	}

	/**
	 * @deprecated Use 'Account.load(IContext, IMendixIdentifier)' instead.
	 */
	@java.lang.Deprecated
	public static siemensinsightshubsinglesignon.proxies.Account initialize(com.mendix.systemwideinterfaces.core.IContext context, com.mendix.systemwideinterfaces.core.IMendixIdentifier mendixIdentifier) throws com.mendix.core.CoreException
	{
		return siemensinsightshubsinglesignon.proxies.Account.load(context, mendixIdentifier);
	}

	/**
	 * Initialize a proxy using context (recommended). This context will be used for security checking when the get- and set-methods without context parameters are called.
	 * The get- and set-methods with context parameter should be used when for instance sudo access is necessary (IContext.createSudoClone() can be used to obtain sudo access).
	 * @param context The context to be used
	 * @param mendixObject The Mendix object for the new instance
	 * @return a new instance of this proxy class
	 */
	public static siemensinsightshubsinglesignon.proxies.Account initialize(com.mendix.systemwideinterfaces.core.IContext context, com.mendix.systemwideinterfaces.core.IMendixObject mendixObject)
	{
		return new siemensinsightshubsinglesignon.proxies.Account(context, mendixObject);
	}

	public static siemensinsightshubsinglesignon.proxies.Account load(com.mendix.systemwideinterfaces.core.IContext context, com.mendix.systemwideinterfaces.core.IMendixIdentifier mendixIdentifier) throws com.mendix.core.CoreException
	{
		com.mendix.systemwideinterfaces.core.IMendixObject mendixObject = com.mendix.core.Core.retrieveId(context, mendixIdentifier);
		return siemensinsightshubsinglesignon.proxies.Account.initialize(context, mendixObject);
	}

	public static java.util.List<siemensinsightshubsinglesignon.proxies.Account> load(com.mendix.systemwideinterfaces.core.IContext context, java.lang.String xpathConstraint) throws com.mendix.core.CoreException
	{
		return com.mendix.core.Core.createXPathQuery(String.format("//%1$s%2$s", entityName, xpathConstraint))
			.execute(context)
			.stream()
			.map(obj -> siemensinsightshubsinglesignon.proxies.Account.initialize(context, obj))
			.collect(java.util.stream.Collectors.toList());
	}

	/**
	 * @return value of Email
	 */
	public final java.lang.String getEmail()
	{
		return getEmail(getContext());
	}

	/**
	 * @param context
	 * @return value of Email
	 */
	public final java.lang.String getEmail(com.mendix.systemwideinterfaces.core.IContext context)
	{
		return (java.lang.String) getMendixObject().getValue(context, MemberNames.Email.toString());
	}

	/**
	 * Set value of Email
	 * @param email
	 */
	public final void setEmail(java.lang.String email)
	{
		setEmail(getContext(), email);
	}

	/**
	 * Set value of Email
	 * @param context
	 * @param email
	 */
	public final void setEmail(com.mendix.systemwideinterfaces.core.IContext context, java.lang.String email)
	{
		getMendixObject().setValue(context, MemberNames.Email.toString(), email);
	}

	/**
	 * @return value of IsSubTenantUser
	 */
	public final java.lang.Boolean getIsSubTenantUser()
	{
		return getIsSubTenantUser(getContext());
	}

	/**
	 * @param context
	 * @return value of IsSubTenantUser
	 */
	public final java.lang.Boolean getIsSubTenantUser(com.mendix.systemwideinterfaces.core.IContext context)
	{
		return (java.lang.Boolean) getMendixObject().getValue(context, MemberNames.IsSubTenantUser.toString());
	}

	/**
	 * Set value of IsSubTenantUser
	 * @param issubtenantuser
	 */
	public final void setIsSubTenantUser(java.lang.Boolean issubtenantuser)
	{
		setIsSubTenantUser(getContext(), issubtenantuser);
	}

	/**
	 * Set value of IsSubTenantUser
	 * @param context
	 * @param issubtenantuser
	 */
	public final void setIsSubTenantUser(com.mendix.systemwideinterfaces.core.IContext context, java.lang.Boolean issubtenantuser)
	{
		getMendixObject().setValue(context, MemberNames.IsSubTenantUser.toString(), issubtenantuser);
	}

	/**
	 * @return value of SubTenantId
	 */
	public final java.lang.String getSubTenantId()
	{
		return getSubTenantId(getContext());
	}

	/**
	 * @param context
	 * @return value of SubTenantId
	 */
	public final java.lang.String getSubTenantId(com.mendix.systemwideinterfaces.core.IContext context)
	{
		return (java.lang.String) getMendixObject().getValue(context, MemberNames.SubTenantId.toString());
	}

	/**
	 * Set value of SubTenantId
	 * @param subtenantid
	 */
	public final void setSubTenantId(java.lang.String subtenantid)
	{
		setSubTenantId(getContext(), subtenantid);
	}

	/**
	 * Set value of SubTenantId
	 * @param context
	 * @param subtenantid
	 */
	public final void setSubTenantId(com.mendix.systemwideinterfaces.core.IContext context, java.lang.String subtenantid)
	{
		getMendixObject().setValue(context, MemberNames.SubTenantId.toString(), subtenantid);
	}

	/**
	 * @throws com.mendix.core.CoreException
	 * @return value of Account_Tenant
	 */
	public final siemensinsightshubsinglesignon.proxies.Tenant getAccount_Tenant() throws com.mendix.core.CoreException
	{
		return getAccount_Tenant(getContext());
	}

	/**
	 * @param context
	 * @return value of Account_Tenant
	 * @throws com.mendix.core.CoreException
	 */
	public final siemensinsightshubsinglesignon.proxies.Tenant getAccount_Tenant(com.mendix.systemwideinterfaces.core.IContext context) throws com.mendix.core.CoreException
	{
		siemensinsightshubsinglesignon.proxies.Tenant result = null;
		com.mendix.systemwideinterfaces.core.IMendixIdentifier identifier = getMendixObject().getValue(context, MemberNames.Account_Tenant.toString());
		if (identifier != null) {
			result = siemensinsightshubsinglesignon.proxies.Tenant.load(context, identifier);
		}
		return result;
	}

	/**
	 * Set value of Account_Tenant
	 * @param account_tenant
	 */
	public final void setAccount_Tenant(siemensinsightshubsinglesignon.proxies.Tenant account_tenant)
	{
		setAccount_Tenant(getContext(), account_tenant);
	}

	/**
	 * Set value of Account_Tenant
	 * @param context
	 * @param account_tenant
	 */
	public final void setAccount_Tenant(com.mendix.systemwideinterfaces.core.IContext context, siemensinsightshubsinglesignon.proxies.Tenant account_tenant)
	{
		if (account_tenant == null) {
			getMendixObject().setValue(context, MemberNames.Account_Tenant.toString(), null);
		} else {
			getMendixObject().setValue(context, MemberNames.Account_Tenant.toString(), account_tenant.getMendixObject().getId());
		}
	}

	@java.lang.Override
	public boolean equals(Object obj)
	{
		if (obj == this) {
			return true;
		}
		if (obj != null && getClass().equals(obj.getClass()))
		{
			final siemensinsightshubsinglesignon.proxies.Account that = (siemensinsightshubsinglesignon.proxies.Account) obj;
			return getMendixObject().equals(that.getMendixObject());
		}
		return false;
	}

	@java.lang.Override
	public int hashCode()
	{
		return getMendixObject().hashCode();
	}

	/**
	 * @return String name of this class
	 */
	public static java.lang.String getType()
	{
		return entityName;
	}

	/**
	 * @return String GUID from this object, format: ID_0000000000
	 * @deprecated Use getMendixObject().getId().toLong() to get a unique identifier for this object.
	 */
	@java.lang.Override
	@java.lang.Deprecated
	public java.lang.String getGUID()
	{
		return "ID_" + getMendixObject().getId().toLong();
	}
}