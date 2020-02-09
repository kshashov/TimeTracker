package com.github.kshashov.timetracker.web.security;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static String homeUrl = "http://localhost:8080/";
    private UsersRepository usersRepository;

    @Autowired
    public CustomAuthenticationSuccessHandler(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
        Map attributes = oidcUser.getAttributes();
        String email = (String) attributes.get("email");
        User user = usersRepository.findOneByEmail(email);
        String token = JwtTokenUtil.generateToken(user);
        String redirectionUrl = UriComponentsBuilder.fromUriString(homeUrl)
                .queryParam("auth_token", token)
                .build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectionUrl);
    }

}