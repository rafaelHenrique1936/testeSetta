package siemensinsightshubsinglesignon;

import java.util.Date;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.MendixRuntimeException;

import siemensinsightshubsinglesignon.proxies.constants.Constants;
import inh_org.jose4j.jwa.AlgorithmConstraints;
import inh_org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import inh_org.jose4j.jwk.JsonWebKeySet;
import inh_org.jose4j.jws.AlgorithmIdentifiers;
import inh_org.jose4j.jwt.JwtClaims;
import inh_org.jose4j.jwt.consumer.InvalidJwtException;
import inh_org.jose4j.jwt.consumer.JwtConsumer;
import inh_org.jose4j.jwt.consumer.JwtConsumerBuilder;
import inh_org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import inh_org.jose4j.keys.resolvers.VerificationKeyResolver;
import inh_org.jose4j.lang.JoseException;

public class VerifyJWT {
	private String audience = "";
	private String trustedIssuer = "";
	private long lastPublicKeyUpdateTime = 0;
	private String PublicKeys = "";
	private JwtConsumer jwtConsumer = null;
	private JwtConsumer jwtConsumerNoIssuer = null;
	private IPublicKeys iPublicKeys;
	
	protected static ILogNode LOGGER = Core.getLogger(Constants.getModuleName());

	public VerifyJWT(String audience, String trustedIssuer, IPublicKeys iPublicKeys) {
		this.audience = audience;
		this.trustedIssuer = trustedIssuer;
		this.iPublicKeys = iPublicKeys;	
	}
	public VerifyJWT(String audience, IPublicKeys iPublicKeys) {
		this.iPublicKeys = iPublicKeys;
		this.trustedIssuer = null;
		this.audience = audience;
	}

	private String getPublicKeys() {
		LOGGER.info("fetching publicKeys");		
		try {
			// It could be, that publicKeys could not be loaded... 
			String newPublicKeys = iPublicKeys.getPublicKeys();
			lastPublicKeyUpdateTime = new Date().getTime();
			this.PublicKeys = newPublicKeys;
		} catch (MendixRuntimeException e) {
			LOGGER.error(e.getMessage());			
		}		
		return this.PublicKeys;
	}

	private VerificationKeyResolver getVerificationKeyResolver() {
		try {
			JsonWebKeySet jwks = new JsonWebKeySet(getPublicKeys());
			JwksVerificationKeyResolver vkr = new JwksVerificationKeyResolver(jwks.getJsonWebKeys());
			return vkr;
		} catch (JoseException e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	private JwtConsumer getJwtConsumerAllChecks() {
		long currentTime = new Date().getTime();
		if (currentTime - lastPublicKeyUpdateTime < 24 * 60 * 60 * 1000 && jwtConsumer != null) {
			return jwtConsumer;
		}

		jwtConsumer = new JwtConsumerBuilder()
				.setRequireExpirationTime()
				.setRequireIssuedAt()
				.setAllowedClockSkewInSeconds(300)
				.setVerificationKeyResolver(getVerificationKeyResolver())
				.setJwsAlgorithmConstraints(
						new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.RSA_USING_SHA256))
				.setExpectedIssuer(trustedIssuer)	
				.setSkipDefaultAudienceValidation()
				.build();

		return jwtConsumer;
	}
	
	private JwtConsumer getJwtConsumerNoIssuer() {
		long currentTime = new Date().getTime();
		if (currentTime - lastPublicKeyUpdateTime < 24 * 60 * 60 * 1000 && jwtConsumerNoIssuer != null) {
			return jwtConsumerNoIssuer;
		}

		jwtConsumerNoIssuer = new JwtConsumerBuilder()
				.setRequireExpirationTime()
				.setRequireIssuedAt()
				.setAllowedClockSkewInSeconds(300)
				.setVerificationKeyResolver(getVerificationKeyResolver())
				.setJwsAlgorithmConstraints(
						new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.RSA_USING_SHA256))		
				.setSkipDefaultAudienceValidation()
				.build();

		return jwtConsumerNoIssuer;
	}
		
	static public JwtClaims getClaimsNoSecurityCheck(String jwt) throws InvalidJwtException, NoJWTException {
		JwtConsumer consumer = new JwtConsumerBuilder()
				.setSkipVerificationKeyResolutionOnNone()
				.setSkipAllValidators()
				.setSkipSignatureVerification()
				.build();
		jwt = checkJwt(jwt);
		return consumer.processToClaims(jwt);
	}
	static private String checkJwt(String jwt) throws NoJWTException {
		if (jwt == null || jwt.isEmpty())
			throw new NoJWTException();
		return jwt.replaceFirst("(?i)bearer ", "");		
	}

	public JwtClaims getJwtClaims(String jwt) throws InvalidJwtException, NoJWTException {
		jwt = checkJwt(jwt);
		return getJwtConsumerAllChecks().processToClaims(jwt);
	}
	
	
	public JwtClaims getJWTClaimsNoIssuerCheck(String jwt) throws InvalidJwtException, NoJWTException {
		jwt = checkJwt(jwt);
		return getJwtConsumerNoIssuer().processToClaims(jwt);
	}
}