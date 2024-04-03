package siemensinsightshubsinglesignon;

public class ScopeRoleMappingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String scopes;
	private String roles;
	public ScopeRoleMappingException(String insightsHubApplicationScopes, String mendixRoles ){
		this.setScopes(insightsHubApplicationScopes);
		this.setRoles(mendixRoles);
	}
	public String getScopes() {
		return scopes;
	}
	public void setScopes(String scopes) {
		this.scopes = scopes;
	}
	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	public String getMessage() {
		return "ScopeRoleMappingExcpetion | InsightsHub application scopes: " + this.getScopes() + " Mendix roles : " + getRoles();
	}
}
