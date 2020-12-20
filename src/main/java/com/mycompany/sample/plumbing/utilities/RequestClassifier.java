package com.mycompany.sample.plumbing.utilities;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;

/*
 * A helper class used by interceptors to determine whether to execute for the current HTTP request
 */
public final class RequestClassifier {

    private final String apiBasePath;

    public RequestClassifier(final String apiBasePath) {
        this.apiBasePath = apiBasePath;
    }

    /*
     * Spring Boot calls us again during async completion, and we do not want to repeat startup processing
     */
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean isApiStartRequest(final HttpServletRequest request) {

        if (!this.isApiRequest(request)) {
            return false;
        }

        if (request.getDispatcherType().equals(DispatcherType.ASYNC)) {
            return false;
        }

        return true;
    }

    /*
     * Ignore pre flight OPTIONS requests
     */
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean isApiRequest(final HttpServletRequest request) {

        if (request.getMethod().equals("OPTIONS")) {
            return false;
        }

        if (!request.getRequestURI().toLowerCase().startsWith(apiBasePath)) {
            return false;
        }

        return true;
    }
}
