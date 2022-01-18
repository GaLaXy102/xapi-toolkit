package de.tudresden.inf.verdatas.xapitools.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This Class overrides the Realm property of BasicAuthenticationEntryPoint and adds a fancy error text to the error response.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Component
public class BasicAuthController extends BasicAuthenticationEntryPoint {

    @Value("${spring.security.realm}")
    private String realmFromProperties;

    /**
     * Add an error text to Response.
     *
     * @see org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint#commence(HttpServletRequest, HttpServletResponse, AuthenticationException)
     */
    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
            throws IOException {
        response.addHeader("WWW-Authenticate", "Basic realm=\"" + this.getRealmName() + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.println("HTTP Status 401 - " + authEx.getMessage());
    }

    /**
     * Set the BasicAuth Realm from a Property
     */
    @Override
    public void afterPropertiesSet() {
        setRealmName(this.realmFromProperties);
        super.afterPropertiesSet();
    }
}
