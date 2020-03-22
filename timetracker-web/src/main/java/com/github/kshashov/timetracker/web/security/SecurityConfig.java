package com.github.kshashov.timetracker.web.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity(debug = true)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/oauth_login").permitAll()
                .requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .loginPage("/oauth_login")
                .and().csrf().disable();

//        http.addFilterBefore(new UserValidationFilter("/registration"), AnonymousAuthenticationFilter.class);
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

/*    @Bean
    public PrincipalExtractor principalExtractor(UsersRepository usersRepository) {
        PrincipalExtractor extractor = map -> {
            var email = (String) map.get("email");
            var user = usersRepository.findOneByEmail(email);
            return user;
        };

        return extractor;
    }*/
}
