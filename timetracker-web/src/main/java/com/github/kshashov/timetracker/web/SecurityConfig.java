package com.github.kshashov.timetracker.web;

import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.vaadin.flow.server.ServletHelper;
import com.vaadin.flow.shared.ApplicationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    /**
     * Tests if the request is an internal framework request. The test consists of
     * checking if the request parameter is present and if its value is consistent
     * with any of the request types know.
     *
     * @param request {@link HttpServletRequest}
     * @return true if is an internal framework request. False otherwise.
     */
    private static boolean isFrameworkInternalRequest(HttpServletRequest request) {
        final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return parameterValue != null
                && Stream.of(ServletHelper.RequestType.values())
                .anyMatch(r -> r.getIdentifier().equals(parameterValue));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/oauth_login").permitAll()
                .requestMatchers(SecurityConfig::isFrameworkInternalRequest).permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .loginPage("/oauth_login")
                .and().csrf().disable();
        //.and().logout()
        //.logoutSuccessHandler(new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository));
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/h2/**",
                // Vaadin Flow static resources //
                "/VAADIN/**",

                // the standard favicon URI
                "/favicon.ico",

                // the robots exclusion standard
                "/robots.txt",

                // web application manifest //
                "/manifest.webmanifest",
                "/sw.js",
                "/offline-page.html",
                "/offline.html",

                // (development mode) static resources //
                "/frontend/**",

                // (development mode) webjars //
                "/webjars/**",

                // (production mode) static resources //
                "/frontend-es5/**", "/frontend-es6/**");
    }

    @Bean
    public PrincipalExtractor principalExtractor(UsersRepository usersRepository) {
        PrincipalExtractor extractor = map -> {
            var email = (String) map.get("email");
            var user = usersRepository.findOneByEmail(email);
            return user;
        };

        return extractor;
    }
}
