package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.user.ProjectRolesRepository;
import com.github.kshashov.timetracker.data.repo.user.UsersRepository;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.Badge;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.ListItem;
import com.github.kshashov.timetracker.web.ui.components.navigation.bar.AppBar;
import com.github.kshashov.timetracker.web.ui.layout.size.Bottom;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Top;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.LumoStyles;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.util.css.lumo.BadgeColor;
import com.github.kshashov.timetracker.web.ui.util.css.lumo.BadgeShape;
import com.github.kshashov.timetracker.web.ui.util.css.lumo.BadgeSize;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "users", layout = MainLayout.class)
public class UsersPage extends ViewFrame implements HasUser, HasUrlParameter<Long> {
    private final UsersRepository usersRepository;
    private final ProjectRolesRepository projectRolesRepository;
    private final User currentUser;
    private User user;

    FlexBoxLayout content = new FlexBoxLayout();

    @Autowired
    public UsersPage(UsersRepository usersRepository, ProjectRolesRepository projectRolesRepository) {
        this.usersRepository = usersRepository;
        this.projectRolesRepository = projectRolesRepository;
        this.currentUser = getUser();

        prepareContent();
        setViewContent(content);
    }

    private void prepareContent() {
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
        content.setMaxWidth("840px");
    }

    private void initDetails() {
        initUserHeader();
        initUserDetailsList();
        initUserProjects();
    }

    private void initUserHeader() {
        Label title = UIUtils.createH3Label(user.getName());
        FlexBoxLayout header = new FlexBoxLayout(title);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setMargin(Bottom.M, Horizontal.RESPONSIVE_L, Top.L);

        // Add edit button for current user
        if (user.getId().equals(currentUser.getId())) {
            Button viewAll = UIUtils.createSmallButton("Edit");
            viewAll.addClickListener(e -> UI.getCurrent().getPage().setLocation("user"));
            viewAll.addClassName(LumoStyles.Margin.Left.AUTO);
            header.add(viewAll);
        }

        content.add(header);
    }

    private void initUserDetailsList() {
        Div items = new Div();
        items.addClassNames(LumoStyles.Padding.Bottom.L);

        ListItem name = new ListItem("Name", user.getName());
        name.setDividerVisible(true);

        ListItem email = new ListItem("Email", user.getEmail());
        email.setDividerVisible(false);

        items.add(name);
        items.add(email);
        content.add(items);
    }

    private void initUserProjects() {
        List<ProjectRole> projects = projectRolesRepository.findWithProjectByUserAndRoleCodeNotOrderByProjectTitle(user, ProjectRoleType.INACTIVE.getCode());

        FlexBoxLayout header = new FlexBoxLayout(UIUtils.createH4Label("Projects (" + projects.size() + "):"));
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setMargin(Bottom.M, Horizontal.RESPONSIVE_L, Top.L);

        content.add(header);

        Div items = new Div();
        items.addClassNames(LumoStyles.Padding.Bottom.L);

        for (ProjectRole projectRole : projects) {
            BadgeColor color = (ProjectRoleType.isInactive(projectRole.getRole()))
                    ? BadgeColor.ERROR
                    : BadgeColor.NORMAL;

            Badge badge = new Badge(projectRole.getProject().getTitle(), color, BadgeSize.M, BadgeShape.NORMAL);
            Anchor anchor = new Anchor("/projects/" + projectRole.getProject().getId(), badge);
            anchor.addClassName(LumoStyles.Margin.Horizontal.S);
            items.add(anchor);
        }
        content.add(items);
    }

    private void initNotFound() {
        content.add(UIUtils.createH1Label("User not found"));
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        if (parameter != null) {
            usersRepository
                    .findById(parameter)
                    .ifPresent(u -> user = u);
        }

        if (user == null) {
            // Show error page
            initNotFound();
        } else {
            initDetails();
        }
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        String name = user == null ? "User not found" : user.getName();

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setTitle(name);
        UI.getCurrent().getPage().setTitle(name);
    }
}
