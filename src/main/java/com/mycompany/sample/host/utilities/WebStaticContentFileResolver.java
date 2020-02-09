package com.mycompany.sample.host.utilities;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.IOException;

/*
 * Resolve requests for web static content files
 */
public final class WebStaticContentFileResolver extends PathResourceResolver {

    private String spaRootLocation;
    private String desktopRootLocation;
    private String mobileRootLocation;

    public WebStaticContentFileResolver(
            final String spaRootLocation,
            final String desktopRootLocation,
            final String mobileRootLocation) {

        this.spaRootLocation = spaRootLocation;
        this.desktopRootLocation = desktopRootLocation;
        this.mobileRootLocation = mobileRootLocation;
    }

    /*
     * The entry point deals with both web and desktop static content
     */
    @Override
    protected Resource getResource(final String resourcePath, final Resource location) {

        if (resourcePath.toLowerCase().startsWith("spa")) {
            return this.getSinglePageAppResource(resourcePath, location);
        }

        if (resourcePath.toLowerCase().startsWith("desktop")) {
            return this.getDesktopResource(resourcePath, location);
        }

        if (resourcePath.toLowerCase().startsWith("mobile")) {
            return this.getAndroidResource(resourcePath, location);
        }

        if (resourcePath.toLowerCase().equals("favicon.ico")) {
            return this.getFaviconResource(location);
        }

        return null;
    }

    /*
     * Serve HTML for our Single Page App, for a request such as 'spa/css/app.css'
     */
    protected Resource getSinglePageAppResource(final String resourcePath, final Resource location) {

        // The web configuration file is a special case
        if (resourcePath.toLowerCase().contains("spa.config.json")) {

            // When the spa.config.json file is requested, we serve the local API version
            var physicalPath = String.format("%s/spa.config.localapi.json", spaRootLocation);
            return this.getResourceFromPhysicalPath(physicalPath, location);
        }

        // Handle requests for general web resources next
        if (resourcePath.toLowerCase().startsWith("spa/")) {

            // Serve the resource from a path such as 'file:../authguidance.websample.final/css/app.css'
            final var prefixLength = 4;
            var physicalPath = String.format("%s/%s", spaRootLocation, resourcePath.substring(prefixLength));
            var resource = this.getResourceFromPhysicalPath(physicalPath, location);
            if (resource != null) {
                return resource;
            }
        }

        // Fall back to serving the index.html resource for any not found resources
        var indexPhysicalPath = String.format("%s/index.html", spaRootLocation);
        return this.getResourceFromPhysicalPath(indexPhysicalPath, location);
    }

    /*
     * Serve HTML for our Loopback Desktop Sample's post login success and error pages
     */
    protected Resource getDesktopResource(final String resourcePath, final Resource location) {

        // Receive a request for 'desktop/loginsuccess.html' or 'desktop/loginerror.html'
        // Serve the login success page from the path 'file:../authguidance.desktopsample1/web/loginsuccess.html'
        if (resourcePath.toLowerCase().contains("desktop/loginsuccess.html")) {

            // Serve it from the web folder of the desktop sample
            var loginSuccessPhysicalPath = String.format("%s/loginsuccess.html", this.desktopRootLocation);
            return this.getResourceFromPhysicalPath(loginSuccessPhysicalPath, location);
        }

        // Serve the login success page from the path 'file:../authguidance.desktopsample1/web/loginsuccess.html'
        if (resourcePath.toLowerCase().contains("desktop/loginerror.html")) {

            // Serve it from the web folder of the desktop sample
            var loginErrorPhysicalPath = String.format("%s/loginerror.html", this.desktopRootLocation);
            return this.getResourceFromPhysicalPath(loginErrorPhysicalPath, location);
        }

        return null;
    }

    /*
     * Serve HTML for our Android sample's post login and post logout pages
     */
    protected Resource getAndroidResource(final String resourcePath, final Resource location) {

        // Receive a request for 'mopile/postlogin.html' or 'mobile/postlogout.html'
        // Serve the login success page from the path 'file:../authguidance.mobilesample.android/web/postlogin.html'
        if (resourcePath.toLowerCase().contains("mobile/postlogin.html")) {

            // Serve it from the web folder of the mobile sample
            var loginPhysicalPath = String.format("%s/postlogin.html", this.mobileRootLocation);
            return this.getResourceFromPhysicalPath(loginPhysicalPath, location);
        }

        // Serve the login success page from the path 'file:../authguidance.mobilesample.android/web/postlogout.html'
        if (resourcePath.toLowerCase().contains("mobile/postlogout.html")) {

            // Serve it from the web folder of the mobile sample
            var logoutPhysicalPath = String.format("%s/postlogout.html", this.mobileRootLocation);
            return this.getResourceFromPhysicalPath(logoutPhysicalPath, location);
        }

        return null;
    }

    /*
     * Serve the favicon.ico file
     */
    protected Resource getFaviconResource(final Resource location) {

        var faviconPhysicalPath = String.format("%s/favicon.ico", spaRootLocation);
        return this.getResourceFromPhysicalPath(faviconPhysicalPath, location);
    }

    /*
     * A utility to load a resource from its physical path
     */
    private Resource getResourceFromPhysicalPath(final String physicalPath, final Resource location) {

        try {

            var requestedResource = location.createRelative(physicalPath);
            if (requestedResource.exists() && requestedResource.isReadable()) {
                return requestedResource;
            }
        } catch (IOException ex) {

            var message = String.format("IOException serving Android web content for %s", physicalPath);
            throw new RuntimeException(message, ex);
        }

        return null;
    }
}
