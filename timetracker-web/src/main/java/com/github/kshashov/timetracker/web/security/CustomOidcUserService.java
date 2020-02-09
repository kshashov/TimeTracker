package com.github.kshashov.timetracker.web.security;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        // TODO check client
        // TODO populate authorities
        User user = updateGoogleUser(oidcUser.getAttributes());
        return new CustomUser(oidcUser, user);
    }

    private User updateGoogleUser(Map attributes) {
        var email = (String) attributes.get("email");
        var name = (String) attributes.get("name");
        var picture = (String) attributes.get("picture");
        var id = (String) attributes.get("sub");

        User user = usersRepository.findOneByEmail(email);
        if (user == null) {
            user = new User();
            user.setName(name);
            user.setEmail(email);
            usersRepository.save(user);
        }
        return user;
    }

    private class CustomUser implements OidcUser, UserPrinciple {

        private final OidcUser oidcUser;
        private final User user;

        public CustomUser(OidcUser oidcUser, User user) {
            this.oidcUser = oidcUser;
            this.user = user;
        }

        @Override
        public User getUser() {
            return user;
        }

        @Override
        public Map<String, Object> getClaims() {
            return oidcUser.getClaims();
        }

        @Override
        public OidcUserInfo getUserInfo() {
            return oidcUser.getUserInfo();
        }

        @Override
        public OidcIdToken getIdToken() {
            return oidcUser.getIdToken();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return oidcUser.getAttributes();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return oidcUser.getAuthorities();
        }

        @Override
        public String getName() {
            return user.getName();
        }
    }
}