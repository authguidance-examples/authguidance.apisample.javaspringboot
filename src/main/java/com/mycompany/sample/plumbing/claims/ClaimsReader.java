package com.mycompany.sample.plumbing.claims;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.errors.ErrorUtils;

/*
 * A utility to read claims values from objects
 */
public final class ClaimsReader {

    private ClaimsReader() {
    }

    /*
     * Return the base claims in a JWT that the API is interested in
     */
    public static BaseClaims baseClaims(final JwtClaims claimsSet) {

        var subject = ClaimsReader.getStringClaim(claimsSet, "sub");
        var scopes = ClaimsReader.getStringClaim(claimsSet, "scope").split(" ");
        var expiry = ClaimsReader.getExpiryClaim(claimsSet);
        return new BaseClaims(subject, scopes, expiry);
    }

    /*
     * Get a string claim from the claims payload
     */
    public static String getStringClaim(final JwtClaims data, final String name) {

        try {
            var value = data.getClaimValue(name, String.class);
            if (!StringUtils.hasLength(value)) {
                throw ErrorUtils.fromMissingClaim(name);
            }

            return value;

        } catch (MalformedClaimException ex) {
            throw ErrorUtils.fromMissingClaim(name);
        }
    }

    /*
     * Get a string array claim from the claims payload
     */
    public static String[] getStringArrayClaim(final JwtClaims data, final String name) {

        try {
            return data.getStringListClaimValue(name).toArray(String[]::new);

        } catch (MalformedClaimException ex) {
            throw ErrorUtils.fromMissingClaim(name);
        }
    }

    /*
     * Get an integer claim from the claims payload
     */
    private static int getExpiryClaim(final JwtClaims data) {

        try {
            return (int) data.getExpirationTime().getValue();

        } catch (MalformedClaimException ex) {
            throw ErrorUtils.fromMissingClaim("exp");
        }
    }
}
