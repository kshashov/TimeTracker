package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.service.admin.users.AuthorizedUsersService;
import com.github.kshashov.timetracker.data.service.admin.users.UserInfo;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.navigation.bar.AppBar;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.DataHandler;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.google.common.eventbus.EventBus;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Route(value = "user", layout = MainLayout.class)
@PageTitle("My Profile")
public class UserPage extends ViewFrame implements HasUser, DataHandler {
    private final AuthorizedUsersService usersService;
    private final EventBus eventBus;

    private final User user;

    private final Binder<User> binder = new Binder<>();
    private final TextField email = new TextField("Email");
    private final TextField name = new TextField("Name");
    private final Select<DayOfWeek> weekStart = new Select<>();
    private final FlexBoxLayout layout = new FlexBoxLayout();
    private final Button save = UIUtils.createPrimaryButton("Save");
    private final FormLayout formLayout = new FormLayout();
    private final Label statusText = UIUtils.createErrorLabel("");


    @Autowired
    public UserPage(AuthorizedUsersService usersService, EventBus eventBus) {
        this.usersService = usersService;
        this.eventBus = eventBus;
        this.user = getUser();

        initFormLayout();

        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
        layout.setMaxWidth("840px");

        layout.add(formLayout, new Div(save));
        setViewContent(layout);
    }

    private void initFormLayout() {
        email.setReadOnly(true);
        binder.forField(email).withNullRepresentation("")
                .bind(User::getEmail, User::setEmail);

        name.setRequired(true);
        binder.forField(name).withNullRepresentation("")
                .withValidator(Strings::isNotBlank, "Name is empty")
                .bind(User::getName, User::setName);
        binder.setStatusLabel(statusText);

        weekStart.setItemLabelGenerator(dow -> dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        weekStart.setItems(DayOfWeek.values());
        weekStart.setRequiredIndicatorVisible(true);
        weekStart.setEmptySelectionAllowed(false);
        weekStart.setLabel("Week starting day");
        binder.forField(weekStart)
                .withValidator(Objects::nonNull, "Day is empty")
                .bind(User::getWeekStart, User::setWeekStart);

        layout.setFlexDirection(FlexDirection.COLUMN);

        binder.withValidator(this::save);

        save.addClickListener(event -> {
            if (binder.writeBeanIfValid(user)) {
                setTitle(user.getName());
            }
        });

        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.add(email, name, weekStart);
    }

    private ValidationResult save(User user, ValueContext valueContext) {
        return handleDataManipulation(() -> {
            UserInfo userInfo = new UserInfo(user);
            usersService.updateUser(user, user.getId(), userInfo);
        });
    }

    private void setTitle(String title) {
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setTitle(title);
        UI.getCurrent().getPage().setTitle(title);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        setTitle(user.getName());

        binder.readBean(user);
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
