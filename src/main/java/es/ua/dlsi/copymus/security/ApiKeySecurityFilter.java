package es.ua.dlsi.copymus.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class ApiKeySecurityFilter extends BasicAuthenticationFilter {
	
	private final Logger log = LoggerFactory.getLogger(ApiKeySecurityFilter.class);

	private final String AUTH_HEADER = "X-API-KEY";
	// Must be the same as spring.security.user.name
	private final String AUTH_USER = "copymus";
	
	public ApiKeySecurityFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}
	
	@Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String token = req.getHeader(AUTH_HEADER);

        if (token == null) {
        	doError(req, res);
            chain.doFilter(req, res);
            return;
        }

        try {
        	UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(AUTH_USER, token);
        	Authentication result = getAuthenticationManager().authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(result);
        	log.info("Access granted with token " + token.substring(0, 4) + "*** to URL " + req.getRequestURI());
        } catch (Exception e) {
        	doError(req, res);
        }        

        chain.doFilter(req, res);
	}
	
	private void doError(HttpServletRequest req, HttpServletResponse res) {
		String uri = req.getRequestURI();
		if (uri.startsWith("/swagger-ui/")
				|| uri.equals("/")
				|| uri.startsWith("/api/scanner/")
				|| uri.startsWith("/scanner/")
				|| uri.equals("/docs")
				|| uri.equals("/api-docs.yaml")
				|| uri.startsWith("/h2-console")
				|| uri.equals("/favicon.ico")
				|| uri.startsWith("/actuator/"))
			return;
    	log.info("Access denied without token to URL " + uri);
	}

}
