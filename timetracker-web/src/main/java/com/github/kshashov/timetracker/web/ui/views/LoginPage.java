package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.FullScreenWidget;
import com.github.kshashov.timetracker.web.ui.layout.size.Top;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.HashMap;
import java.util.Map;

@Route(value = "oauth_login")
@PageTitle("TimeTracker - Login")
public class LoginPage extends FullScreenWidget {
    private static String authorizationRequestBaseUri = "oauth2/authorization";
    private final ClientRegistrationRepository clientRegistrationRepository;
    private FlexBoxLayout ssoButtons = new FlexBoxLayout();

    @Autowired
    public LoginPage(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;

        initSsoButtons();

        setTitle("Login");
        addContentItems(ssoButtons);
    }

    private void initSsoButtons() {
        ssoButtons.setMargin(Top.M);
        ssoButtons.setWidthFull();
        ssoButtons.setFlexDirection(FlexDirection.COLUMN);

        getClients().forEach((name, url) -> {
            Button button = UIUtils.createContrastButton(name);
            button.addClickListener(event -> UI.getCurrent().getPage().setLocation(url));
            button.setWidthFull();
            ssoButtons.add(button);
        });

    }

    private Map<String, String> getClients() {
        Iterable<ClientRegistration> clientRegistrations = null;
        ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository).as(Iterable.class);
        if (type != ResolvableType.NONE && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        }

        Map<String, String> clientUrls = new HashMap<>();
        if (clientRegistrations != null) {
            clientRegistrations.forEach(registration ->
                    clientUrls.put(registration.getClientName(),
                            "/" + authorizationRequestBaseUri + "/" + registration.getRegistrationId()));
        }
        return clientUrls;
    }
}
