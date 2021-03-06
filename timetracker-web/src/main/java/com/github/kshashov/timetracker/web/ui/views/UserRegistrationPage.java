package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.service.admin.users.AuthorizedUsersService;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.security.SecurityUtils;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.FullScreenButtonsWidget;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.google.common.eventbus.EventBus;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;

@Slf4j
@PageTitle("User Registration")
@Route(value = UserRegistrationPage.USER_VALIDATION_URL)
public class UserRegistrationPage extends FullScreenButtonsWidget implements HasUser, DataHandler, BeforeEnterObserver, HasUrlParameter<String> {
    public static final String USER_VALIDATION_URL = "registration";

    private final AuthorizedUsersService usersService;
    private final EventBus eventBus;

    private final Binder<User> binder = new Binder<>();
    private final TextField email = new TextField("Email");
    private final TextField name = new TextField("Name");
    private final FlexBoxLayout layout = new FlexBoxLayout();
    private final Button save = UIUtils.createPrimaryButton("Save");
    private final Button logout = UIUtils.createTertiaryButton("Log Out");
    private final FormLayout formLayout = new FormLayout();
    private final Label statusText = UIUtils.createErrorLabel("");

    private final User user;
    private String redirectUrl;

    @Autowired
    public UserRegistrationPage(AuthorizedUsersService usersService, EventBus eventBus) {
        this.usersService = usersService;
        this.eventBus = eventBus;
        this.user = getUser();

        binder.withValidator(this::save);

        setTitle("Registration");
        initFormLayout();

        save.addClickListener(event -> {
            if (binder.writeBeanIfValid(user)) {
                redirect();
            }
        });

        logout.addClickListener(event -> {
            VaadinSession.getCurrent().getSession().invalidate();
        });

        addContentItems(statusText, formLayout);
        addFooterItems(save, logout);
    }

    private void initFormLayout() {
        email.setReadOnly(true);

        binder.forField(email).withNullRepresentation("")
                .bind(User::getEmail, User::setEmail);

        binder.forField(name).withNullRepresentation("")
                .withValidator(Strings::isNotBlank, "Name is empty")
                .bind(User::getName, User::setName);
        binder.setStatusLabel(statusText);

        layout.setFlexDirection(FlexDirection.COLUMN);

        formLayout.add(email);
        formLayout.add(name);
    }

    private ValidationResult save(User user, ValueContext valueContext) {
        return handleDataManipulation(() -> {
            this.user.setIsValidated(usersService.validate(user, user.getId(), user.getName()));
        });
    }

    private void redirect() {
        Location location = new Location(redirectUrl);
        getUI().ifPresent(ui -> ui.navigate(location.getPath(), location.getQueryParameters()));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        binder.readBean(user);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (SecurityUtils.hasValidatedUser()) {
            redirect();
        }
    }

    @Override
    public void setParameter(final BeforeEvent event, @OptionalParameter final String parameter) {
        if (parameter == null) {
            redirectUrl = "";
        } else {
            redirectUrl = new String(Base64Utils.decodeFromString(parameter));
        }
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public EventBus eventBus() {
        return eventBus;
    }
}

