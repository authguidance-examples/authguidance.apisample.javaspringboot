package com.mycompany.sample.host.authorization;

import com.mycompany.sample.host.claims.SampleApiClaims;
import com.mycompany.sample.host.claims.CustomClaimsProvider;
import javax.servlet.http.HttpServletRequest;

/*
 * Extend our base class to provide custom claims
 */
public final class SampleApiClaimsProvider extends CustomClaimsProvider<SampleApiClaims> {

    /*
     * Our sample will allow the user to access data associated to the below regions but not for Asia
     */
    @Override
    public void addCustomClaims(
            final String accessToken,
            final HttpServletRequest request,
            final SampleApiClaims claims) {

        // We will hard code the coverage, whereas a real scenario would look up the user data
        claims.setRegionsCovered(new String[]{"Europe", "USA"});
    }
}
