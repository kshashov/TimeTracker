package com.github.kshashov.timetracker.web.ui;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.navigation.bar.AppBar;
import com.github.kshashov.timetracker.web.ui.components.navigation.drawer.NaviDrawer;
import com.github.kshashov.timetracker.web.ui.components.navigation.drawer.NaviItem;
import com.github.kshashov.timetracker.web.ui.components.navigation.drawer.NaviMenu;
import com.github.kshashov.timetracker.web.ui.util.LumoStyles;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.BorderRadius;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.util.css.Overflow;
import com.github.kshashov.timetracker.web.ui.views.*;
import com.github.kshashov.timetracker.web.ui.views.admin.projects.ProjectsPage;
import com.github.kshashov.timetracker.web.ui.views.personnel.Accountants;
import com.github.kshashov.timetracker.web.ui.views.personnel.Managers;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@CssImport(value = "frontend://styles/components/charts.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
@CssImport(value = "frontend://styles/components/floating-action-button.css", themeFor = "vaadin-button")
@CssImport(value = "frontend://styles/components/grid.css", themeFor = "vaadin-grid")
@CssImport("frontend://styles/lumo/border-radius.css")
@CssImport("frontend://styles/lumo/icon-size.css")
@CssImport("frontend://styles/lumo/margin.css")
@CssImport("frontend://styles/lumo/padding.css")
@CssImport("frontend://styles/lumo/shadow.css")
@CssImport("frontend://styles/lumo/spacing.css")
@CssImport("frontend://styles/lumo/typography.css")
@CssImport("frontend://styles/misc/box-shadow-borders.css")
@CssImport(value = "frontend://styles/styles.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class MainLayout extends FlexBoxLayout
        implements HasUser, RouterLayout, PageConfigurator, AfterNavigationObserver {

    private static final String CLASS_NAME = "root";

    private final User user;

    private Div appHeaderOuter;

    private FlexBoxLayout row;
    private NaviDrawer naviDrawer;
    private FlexBoxLayout column;

    private Div appHeaderInner;
    private FlexBoxLayout viewContainer;
    private Div appFooterInner;

    private Div appFooterOuter;

    private AppBar appBar;

    @Autowired
    public MainLayout() {
        this.user = getUser();

        VaadinSession.getCurrent()
                .setErrorHandler((ErrorHandler) errorEvent -> {
                    log.error("Uncaught UI exception", errorEvent.getThrowable());
                    Notification.show(
                            "We are sorry, but an internal error occurred");
                });

        addClassName(CLASS_NAME);
        setFlexDirection(FlexDirection.COLUMN);
        setSizeFull();

        // Initialise the UI building blocks
        initStructure();

        // Populate the navigation drawer
        initNaviItems();

        // Configure the headers and footers (optional)
        initHeadersAndFooters();
    }

    public static MainLayout get() {
        return (MainLayout) UI.getCurrent().getChildren()
                .filter(component -> component.getClass() == MainLayout.class)
                .findFirst().get();
    }

    /**
     * Initialise the required components and containers.
     */
    private void initStructure() {
        naviDrawer = new NaviDrawer();

        viewContainer = new FlexBoxLayout();
        viewContainer.addClassName(CLASS_NAME + "__view-container");
        viewContainer.setOverflow(Overflow.HIDDEN);

        column = new FlexBoxLayout(viewContainer);
        column.addClassName(CLASS_NAME + "__column");
        column.setFlexDirection(FlexDirection.COLUMN);
        column.setFlexGrow(1, viewContainer);
        column.setOverflow(Overflow.HIDDEN);

        row = new FlexBoxLayout(naviDrawer, column);
        row.addClassName(CLASS_NAME + "__row");
        row.setFlexGrow(1, column);
        row.setOverflow(Overflow.HIDDEN);
        add(row);
        setFlexGrow(1, row);
    }

    /**
     * Initialise the navigation items.
     */
    private void initNaviItems() {
        NaviMenu menu = naviDrawer.getMenu();

        menu.addNaviItem(VaadinIcon.INSTITUTION, "Accounts", Accounts.class);
        menu.addNaviItem(VaadinIcon.CREDIT_CARD, "Payments", Payments.class);
        menu.addNaviItem(VaadinIcon.CHART, "Statistics", Statistics.class);

        NaviItem personnel = menu.addNaviItem(VaadinIcon.USERS, "Personnel", null);
        menu.addNaviItem(personnel, "Accountants", Accountants.class);
        menu.addNaviItem(personnel, "Managers", Managers.class);

        NaviItem home = menu.addNaviItem(VaadinIcon.HOME, "Home", null);
        menu.addNaviItem(home, "About", About.class);

        NaviItem entries = menu.addNaviItem(VaadinIcon.CALENDAR_CLOCK, "Work Logs", null);
        menu.addNaviItem(entries, "Daily", DailyPage.class);
        menu.addNaviItem(entries, "Weekly", WeeklyPage.class);
        menu.addNaviItem(entries, "Reports", ReportsPage.class);

        NaviItem admin = menu.addNaviItem(VaadinIcon.CALENDAR_BRIEFCASE, "Admin", null);
        menu.addNaviItem(admin, "Projects", ProjectsPage.class);
        menu.addNaviItem(admin, "Days", WeeklyPage.class);
    }

    /**
     * Configure the app's inner and outer headers and footers.
     */
    private void initHeadersAndFooters() {
        appBar = new AppBar("");
        if (user != null) {
            appBar.addRightCorner(createUserInfo());
        }
        setAppHeaderInner(appBar);
    }

    private Component createUserInfo() {
        FlexBoxLayout avatar = new FlexBoxLayout(UIUtils.createTertiaryIcon(VaadinIcon.USER));
        avatar.setBackgroundColor(LumoStyles.Color.Contrast._10);
        avatar.setBorderRadius(BorderRadius.L);
        avatar.setHeight(LumoStyles.Size.M);
        avatar.setWidth(LumoStyles.Size.M);
        avatar.setAlignItems(FlexComponent.Alignment.CENTER);
        avatar.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        ContextMenu contextMenu = new ContextMenu(avatar);
        contextMenu.setOpenOnClick(true);
        contextMenu.addItem("Profile", e -> {
            UI.getCurrent().navigate(UserPage.class);
        });
        contextMenu.addItem("Log Out", e -> {
            VaadinSession.getCurrent().getSession().invalidate();
        });

        return avatar;
    }

    private void setAppHeaderOuter(Component... components) {
        if (appHeaderOuter == null) {
            appHeaderOuter = new Div();
            appHeaderOuter.addClassName("app-header-outer");
            getElement().insertChild(0, appHeaderOuter.getElement());
        }
        appHeaderOuter.removeAll();
        appHeaderOuter.add(components);
    }

    private void setAppHeaderInner(Component... components) {
        if (appHeaderInner == null) {
            appHeaderInner = new Div();
            appHeaderInner.addClassName("app-header-inner");
            column.getElement().insertChild(0, appHeaderInner.getElement());
        }
        appHeaderInner.removeAll();
        appHeaderInner.add(components);
    }

    private void setAppFooterInner(Component... components) {
        if (appFooterInner == null) {
            appFooterInner = new Div();
            appFooterInner.addClassName("app-footer-inner");
            column.getElement().insertChild(column.getElement().getChildCount(),
                    appFooterInner.getElement());
        }
        appFooterInner.removeAll();
        appFooterInner.add(components);
    }

    private void setAppFooterOuter(Component... components) {
        if (appFooterOuter == null) {
            appFooterOuter = new Div();
            appFooterOuter.addClassName("app-footer-outer");
            getElement().insertChild(getElement().getChildCount(),
                    appFooterOuter.getElement());
        }
        appFooterOuter.removeAll();
        appFooterOuter.add(components);
    }

    @Override
    public void configurePage(InitialPageSettings settings) {
        settings.addMetaTag("apple-mobile-web-app-capable", "yes");
        settings.addMetaTag("apple-mobile-web-app-status-bar-style", "black");

        settings.addFavIcon("icon", "frontend/images/favicons/favicon.ico",
                "256x256");
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        this.viewContainer.getElement().appendChild(content.getElement());
    }

    public NaviDrawer getNaviDrawer() {
        return naviDrawer;
    }

    public AppBar getAppBar() {
        return appBar;
    }

    private NaviItem getActiveItem(AfterNavigationEvent e) {
        for (NaviItem item : naviDrawer.getMenu().getNaviItems()) {
            if (item.isHighlighted(e)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        NaviItem active = getActiveItem(event);
        if (active != null) {
            getAppBar().setTitle(active.getText());
        }
    }
}
