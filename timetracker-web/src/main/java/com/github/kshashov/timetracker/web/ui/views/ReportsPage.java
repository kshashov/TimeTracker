package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.Permission;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.user.PermissionsRepository;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.Widget;
import com.github.kshashov.timetracker.web.ui.components.navigation.bar.AppBar;
import com.github.kshashov.timetracker.web.ui.layout.size.Bottom;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Top;
import com.github.kshashov.timetracker.web.ui.util.FontSize;
import com.github.kshashov.timetracker.web.ui.util.FontWeight;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.BoxSizing;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.views.reports.ReportsDateFilter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

@Route(value = "reports", layout = MainLayout.class)
@PageTitle("Reports")
public class ReportsPage extends ViewFrame implements HasUser {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM/dd/yyyy", Locale.ENGLISH);

    private final User user;

    private final ListDataProvider<Entry> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private final Grid<Entry> entriesGrid = new Grid<>();
    private final FlexBoxLayout content = new FlexBoxLayout();
    private final EntriesRepository entriesRepository;
    private final PermissionsRepository permissionsRepository;
    private final FlexBoxLayout filtersLayout = new FlexBoxLayout();
    private final Span total = new Span();
    private final ReportsDateFilter dateFilter = new ReportsDateFilter();


    @Autowired
    public ReportsPage(EntriesRepository entriesRepository, PermissionsRepository permissionsRepository) {
        this.entriesRepository = entriesRepository;
        this.permissionsRepository = permissionsRepository;
        this.user = getUser();

        prepareContent();
        setViewContent(content);
    }

    private void prepareContent() {
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        initGrid();

        dateFilter.addDateChangedEventListener(event -> {
            reloadEntities(event.getFrom(), event.getTo());
        });

        filtersLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        filtersLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        filtersLayout.setMargin(Bottom.L);

        filtersLayout.add(dateFilter);

        content.add(filtersLayout);

        Widget widget = new Widget(entriesGrid);
        widget.setTitle("Work Logs");

        UIUtils.setFontWeight(FontWeight.BOLD, total);
        widget.getActions().add(total);

        content.add(widget);
    }

    private void initGrid() {
        entriesGrid.setDataProvider(dataProvider);
        entriesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        entriesGrid.setSelectionMode(Grid.SelectionMode.NONE);
        entriesGrid.setHeightByRows(false);

        Grid.Column<Entry> userColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            return new Span(entry.getUser().getName());
        })).setHeader("Author").setSortable(true).setComparator(e -> e.getUser().getName()).setAutoWidth(true);

        Grid.Column<Entry> projectColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            Action action = entry.getAction();
            Project project = action.getProject();
            Span item = new Span(StringUtils.defaultString(project.getTitle()));

            if (!project.getIsActive()) {
                item.getStyle().set("text-decoration", "line-through");
            }

            return item;
        })).setHeader("Project").setSortable(true).setComparator(e -> e.getAction().getProject().getTitle()).setAutoWidth(true);

        Grid.Column<Entry> actionColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            Action action = entry.getAction();

            Span item = new Span(StringUtils.defaultString(action.getTitle()));

            if (!action.getIsActive()) {
                item.getStyle().set("text-decoration", "line-through");
            }

            return item;
        })).setHeader("Action").setSortable(true).setComparator(e -> e.getAction().getTitle()).setAutoWidth(true);

        Grid.Column<Entry> titleColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            return UIUtils.createLabel(FontSize.M, StringUtils.defaultString(entry.getTitle()));
        })).setHeader("Description").setSortable(true).setComparator(Entry::getTitle).setAutoWidth(true);

        Grid.Column<Entry> dateColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            return new Span(dateFormatter.format(entry.getObs()));
        })).setHeader("Date").setSortable(true).setComparator(Entry::getObs).setAutoWidth(true);

        Grid.Column<Entry> hoursColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            return UIUtils.createHoursLabel(entry.getHours());
        })).setHeader("Time Spent").setSortable(true).setComparator(Entry::getHours).setAutoWidth(true);

        HeaderRow filterRow = entriesGrid.appendHeaderRow();

        // User filter
        TextField userFilter = new TextField();
        userFilter.addValueChangeListener(event -> dataProvider.addFilter(entry ->
                StringUtils.containsIgnoreCase(entry.getUser().getName(), userFilter.getValue())));

        userFilter.setValueChangeMode(ValueChangeMode.EAGER);

        filterRow.getCell(userColumn).setComponent(userFilter);
        userFilter.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        userFilter.setSizeFull();
        userFilter.setPlaceholder("Filter");

        // Project filter
        TextField projectFilter = new TextField();
        projectFilter.addValueChangeListener(event -> dataProvider.addFilter(entry ->
                StringUtils.containsIgnoreCase(entry.getAction().getProject().getTitle(), projectFilter.getValue())));

        projectFilter.setValueChangeMode(ValueChangeMode.EAGER);

        filterRow.getCell(projectColumn).setComponent(projectFilter);
        projectFilter.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        projectFilter.setSizeFull();
        projectFilter.setPlaceholder("Filter");

        // Project filter
        TextField actionFilter = new TextField();
        actionFilter.addValueChangeListener(event -> dataProvider.addFilter(entry ->
                StringUtils.containsIgnoreCase(entry.getAction().getTitle(), actionFilter.getValue())));

        actionFilter.setValueChangeMode(ValueChangeMode.EAGER);

        filterRow.getCell(actionColumn).setComponent(actionFilter);
        actionFilter.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        actionFilter.setSizeFull();
        actionFilter.setPlaceholder("Filter");

        // Description filter
        TextField descriptionFilter = new TextField();
        descriptionFilter.addValueChangeListener(event -> dataProvider.addFilter(entry ->
                StringUtils.containsIgnoreCase(entry.getAction().getTitle(), descriptionFilter.getValue())));

        descriptionFilter.setValueChangeMode(ValueChangeMode.EAGER);

        filterRow.getCell(titleColumn).setComponent(descriptionFilter);
        descriptionFilter.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        descriptionFilter.setSizeFull();
        descriptionFilter.setPlaceholder("Filter");
    }

    private void reloadEntities(LocalDate from, LocalDate to) {
        Permission permission = permissionsRepository.findOneByCode(ProjectPermissionType.VIEW_PROJECT_LOGS.getCode());
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(entriesRepository.findFullByUserAndReporterPermission(getUser(), permission, from, to));
        dataProvider.refreshAll();

        total.setText("Total: " + dataProvider.getItems().stream().map(Entry::getHours).reduce(0.0, Double::sum) + "h");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            LocalDate date = extendedClientDetails.getCurrentDate()
                    .toInstant()
                    .atZone(ZoneId.of(extendedClientDetails.getTimeZoneId()))
                    .toLocalDate();

            dateFilter.setDate(date);
            dateFilter.setType(ReportsDateFilter.FilterType.THIS_WEEK);
        });

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setTitle("Daily Work");
    }
}
