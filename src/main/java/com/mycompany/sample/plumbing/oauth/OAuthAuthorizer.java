package com.mycompany.sample.plumbing.oauth;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.BeanFactory;
import com.mycompany.sample.plumbing.claims.ClaimsCache;
import com.mycompany.sample.plumbing.claims.ClaimsSupplier;
import com.mycompany.sample.plumbing.claims.CoreApiClaims;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * The Spring entry point for handling token validation and claims lookup
 */
public final class OAuthAuthorizer<TClaims extends CoreApiClaims> extends BaseAuthorizer {

    public OAuthAuthorizer(final BeanFactory container) {
        super(container);
    }

    /*
     * OAuth authorization involves token validation and claims lookup
     */
    @Override
    protected CoreApiClaims execute(final HttpServletRequest request) {

        // Resolve dependencies
        var cache = super.getContainer().getBean(ClaimsCache.class);
        var claimsSupplier = super.getContainer().getBean(ClaimsSupplier.class);
        var authenticator = super.getContainer().getBean(OAuthAuthenticator.class);

        // First read the access token
        String accessToken = this.readAccessToken(request);
        if (accessToken == null) {
            throw ErrorFactory.createClient401Error("No access token was supplied in the bearer header");
        }

        // Bypass and use cached results if they exist
        var cachedClaims = cache.getClaimsForToken(accessToken);
        if (cachedClaims != null) {
            return cachedClaims;
        }

        // Otherwise create new claims which we will populate
        var claims = claimsSupplier.createEmptyClaims();

        // Add OAuth claims from introspection and user info lookup
        var expiry = authenticator.authenticateAndSetClaims(accessToken, request, claims);

        // Add any custom product specific custom claims if required
        claimsSupplier.createCustomClaimsProvider().addCustomClaims(accessToken, request, claims);

        // Cache the claims against the token hash until the token's expiry time
        // The next time the API is called, all of the above results can be quickly looked up
        cache.addClaimsForToken(accessToken, expiry, claims);

        // Return the result on success
        return claims;
    }

    /*
     * Try to read the access token from the authorization header
     */
    private String readAccessToken(final HttpServletRequest request) {

        var header = request.getHeader("Authorization");
        if (header != null) {
            var parts = header.split(" ");
            if (parts.length == 2 && parts[0].equals("Bearer")) {
                return parts[1];
            }
        }

        return null;
    }
}
