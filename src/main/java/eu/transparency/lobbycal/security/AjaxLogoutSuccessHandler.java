package eu.transparency.lobbycal.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Spring Security logout handler, specialized for Ajax requests.
 */
@Component
public class AjaxLogoutSuccessHandler extends
		AbstractAuthenticationTargetUrlRequestHandler implements
		LogoutSuccessHandler {

	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_OK);

	}
}
