package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectRoleType;
import com.github.kshashov.timetracker.data.repo.ActionsRepository;
import com.github.kshashov.timetracker.data.repo.ProjectsRepository;
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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "projects", layout = MainLayout.class)
@PageTitle("Project Info")
public class ProjectsPage extends ViewFrame implements HasUser, HasUrlParameter<Long> {
    private final UsersRepository usersRepository;
    private final User user;
    private Project project;

    FlexBoxLayout content = new FlexBoxLayout();
    private final ProjectRolesRepository projectRolesRepository;
    private final ActionsRepository actionsRepository;
    private final ProjectsRepository projectsRepository;

    @Autowired
    public ProjectsPage(UsersRepository usersRepository, ProjectRolesRepository projectRolesRepository, ActionsRepository actionsRepository, ProjectsRepository projectsRepository) {
        this.usersRepository = usersRepository;
        this.projectRolesRepository = projectRolesRepository;
        this.actionsRepository = actionsRepository;
        this.projectsRepository = projectsRepository;
        this.user = getUser();

        prepareContent();
        setViewContent(content);
    }

    private void prepareContent() {
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
        content.setMaxWidth("calc(var(--lumo-size-m)*16)");
    }

    private void initDetails() {
        initProjectHeader();
        initProjectDetailsList();
        initProjectActions();
        initProjectUsers();
    }

    private void initProjectHeader() {
        Label title = UIUtils.createH3Label(project.getTitle());
        FlexBoxLayout header = new FlexBoxLayout(title);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setMargin(Bottom.M, Horizontal.RESPONSIVE_L, Top.L);

        content.add(header);
    }

    private void initProjectDetailsList() {
        Div items = new Div();
        items.addClassNames(LumoStyles.Padding.Bottom.L);

        ListItem title = new ListItem("Title", project.getTitle());
        title.setDividerVisible(true);

        ListItem status = new ListItem("Status", project.getIsActive() ? "Active" : "Inactive");
        status.setDividerVisible(true);

        items.add(title, status);
        content.add(items);
    }

    private void initProjectActions() {
        List<Action> actions = actionsRepository.findByProject(project);

        FlexBoxLayout header = new FlexBoxLayout(UIUtils.createH4Label("Actions (" + actions.size() + "):"));
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setMargin(Bottom.M, Horizontal.RESPONSIVE_L, Top.L);

        content.add(header);

        Div items = new Div();
        items.addClassNames(LumoStyles.Padding.Bottom.L);

        for (Action action : actions) {
            BadgeColor color = action.getIsActive() ? BadgeColor.NORMAL : BadgeColor.ERROR;
            Badge badge = new Badge(action.getTitle(), color, BadgeSize.M, BadgeShape.NORMAL);
            badge.addClassName(LumoStyles.Margin.Horizontal.S);
            items.add(badge);
        }
        content.add(items);
    }

    private void initProjectUsers() {
        List<ProjectRole> users = projectRolesRepository.findByProject(project);

        FlexBoxLayout header = new FlexBoxLayout(UIUtils.createH4Label("Users (" + users.size() + "):"));
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setMargin(Bottom.M, Horizontal.RESPONSIVE_L, Top.L);

        content.add(header);

        Div items = new Div();
        items.addClassNames(LumoStyles.Padding.Bottom.L);

        for (ProjectRole projectRole : users) {
            BadgeColor color = (ProjectRoleType.isInactive(projectRole.getRole()))
                    ? BadgeColor.ERROR
                    : BadgeColor.NORMAL;

            Badge badge = new Badge(projectRole.getUser().getName(), color, BadgeSize.M, BadgeShape.NORMAL);
            Anchor anchor = new Anchor("/users/" + projectRole.getUser().getId(), badge);
            anchor.addClassName(LumoStyles.Margin.Horizontal.S);
            items.add(anchor);
        }
        content.add(items);
    }

    private void initNotFound() {
        content.add(UIUtils.createH1Label("Project not found"));
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        if (parameter != null) {
            projectsRepository
                    .findById(parameter)
                    .ifPresent(u -> project = u);
        }

        if (project == null) {
            // Show error page
            initNotFound();
        } else {
            initDetails();
        }
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        String title = project == null ? "Project not found" : project.getTitle();

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setTitle(title);
        UI.getCurrent().getPage().setTitle(title);
    }
}
