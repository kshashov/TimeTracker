package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.navigation.bar.AppBar;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "user", layout = MainLayout.class)
@PageTitle("My Profile")
public class UserPage extends ViewFrame implements HasUser {
    private final UsersRepository usersRepository;
    private final User user;
    private Binder<User> binder = new Binder<>();

    @Autowired
    public UserPage(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
        this.user = getUser();
        this.setViewContent(createContent());
    }

    private Component createContent() {
        return new FlexBoxLayout();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setTitle(user.getName());
        UI.getCurrent().getPage().setTitle(user.getName());
    }
}
