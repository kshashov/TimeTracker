package com.github.kshashov.timetracker.web.mvc.views;

import com.github.kshashov.timetracker.web.mvc.components.Divider;
import com.github.kshashov.timetracker.web.mvc.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.mvc.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.mvc.layout.size.Top;
import com.github.kshashov.timetracker.web.mvc.util.LumoStyles;
import com.github.kshashov.timetracker.web.mvc.util.UIUtils;
import com.github.kshashov.timetracker.web.mvc.util.css.FlexDirection;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.HashMap;
import java.util.Map;

@Route(value = "oauth_login")
public class LoginPage extends FlexBoxLayout {
    private static String authorizationRequestBaseUri = "oauth2/authorization";
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    public LoginPage(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;

        setSizeFull();
        setClassName(LumoStyles.Color.Contrast._5);
        setAlignItems(Alignment.CENTER);

        add(createLoginForm());
    }

    private Component createLoginForm() {
        Iterable<ClientRegistration> clientRegistrations = null;
        ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository).as(Iterable.class);
        if (type != ResolvableType.NONE && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        }

        if (clientRegistrations == null) {
            // TODO return custom error page
            return new Span("internal error");
        }

        Map<String, String> clientUrls = new HashMap<>();
        clientRegistrations.forEach(registration ->
                clientUrls.put(registration.getClientName(),
                        "/" + authorizationRequestBaseUri + "/" + registration.getRegistrationId()));
        setJustifyContentMode(JustifyContentMode.CENTER);
        var login = new FlexBoxLayout();
        login.setFlexDirection(FlexDirection.COLUMN);
        login.setWidth("350px");
        login.setSpacing(Top.S);
        login.setPadding(Horizontal.L);
        login.setAlignItems(Alignment.CENTER);
        login.add(UIUtils.createH2Label("Login"), new Divider("1px"));

        var buttons = new FlexBoxLayout();
        buttons.setWidthFull();
        buttons.setFlexDirection(FlexDirection.COLUMN);

        clientUrls.forEach((name, url) -> {
            Button button = UIUtils.createContrastButton(name);
            button.addClickListener(event -> UI.getCurrent().getPage().setLocation(url));
            button.setWidthFull();
            buttons.add(button);
        });

        login.add(buttons);
        return login;
    }
}
