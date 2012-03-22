package org.mitre.jwt.signer;

import java.util.List;

import org.mitre.jwt.model.Jwt;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class AbstractJwtSigner implements JwtSigner {
		
	private String algorithm;

	public AbstractJwtSigner(String algorithm) {
	    this.algorithm = algorithm;
    }

	/**
     * @return the algorithm
     */
    public String getAlgorithm() {
    	return algorithm;
    }

	/**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(String algorithm) {
    	this.algorithm = algorithm;
    }

    /**
     * Ensures that the 'alg' of the given JWT matches the {@link #algorithm} of this signer
     * and signs the jwt.
     * 
     * @param jwt the jwt to sign
     * @return the signed jwt
     */
	@Override
	public Jwt sign(Jwt jwt) {
		if (!Objects.equal(algorithm, jwt.getHeader().getAlgorithm())) {
			// algorithm type doesn't match
			// TODO: should this be an error or should we just fix it in the incoming jwt?
			// for now, we fix the Jwt
			jwt.getHeader().setAlgorithm(algorithm);			
		}
	    
	    String sig = generateSignature(jwt.getSignatureBase());
        
        jwt.setSignature(sig);	
        
        return jwt;
	}

	/* (non-Javadoc)
     * @see org.mitre.jwt.JwtSigner#verify(java.lang.String)
     */
    @Override
    public boolean verify(String jwtString) {
		// split on the dots
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(jwtString));
		
		if (parts.size() != 3) {
			throw new IllegalArgumentException("Invalid JWT format.");
		}
		
		String h64 = parts.get(0);
		String c64 = parts.get(1);
		String s64 = parts.get(2);
    	
		String expectedSignature = generateSignature(h64 + "." + c64 + ".");
		
		return Strings.nullToEmpty(s64).equals(Strings.nullToEmpty(expectedSignature));
    	
    }
	
    
    protected abstract String generateSignature(String signatureBase);
}