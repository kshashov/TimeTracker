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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "user", layout = MainLayout.class)
@PageTitle("User")
public class UserPage extends ViewFrame implements HasUser, HasUrlParameter<Long> {
    private final UsersRepository usersRepository;
    private final User currentUser;
    private User user;
    private Binder<User> binder = new Binder<>();

    @Autowired
    public UserPage(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
        this.currentUser = getUser();
        this.setViewContent(createContent());

    }

    private Component createContent() {
//        FlexLayout layout = new FlexLayout(UIUtils.createH2Label("Hello " + user.getName()));
//        if (currentUser.getUser().getId().equals(user.getId())) {
//            layout.add(UIUtils.createH3Label("IT IS YOU"));
//        }
        return new FlexBoxLayout();
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        if (parameter != null) {
            usersRepository
                    .findById(parameter)
                    .ifPresent(u -> user = u);

            if (user == null) {
                // TODO show error page
            }
        } else {
            // Show current user
            user = currentUser;
        }
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setTitle(user.getName());
        UI.getCurrent().getPage().setTitle(user.getName());
    }
}
