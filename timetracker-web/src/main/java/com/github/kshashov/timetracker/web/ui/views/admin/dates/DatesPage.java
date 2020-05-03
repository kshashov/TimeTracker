package com.github.kshashov.timetracker.web.ui.views.admin.dates;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.HasSubscriptions;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.views.ViewFrame;
import com.github.kshashov.timetracker.web.ui.views.reports.DatesFilter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Subscription;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Route(value = "dates", layout = MainLayout.class)
@PageTitle("Dates")
public class DatesPage extends ViewFrame implements HasSubscriptions {
    private final DatesViewModel viewModel;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private final FlexBoxLayout content = new FlexBoxLayout();
    private final Button openDays = UIUtils.createTertiaryButton("Open");
    private final Button closeDays = UIUtils.createTertiaryButton("Close");
    private final Button openEntries = UIUtils.createTertiaryButton("Open");
    private final Button closeEntries = UIUtils.createTertiaryButton("Close");
    private final DatesFilter datesFilter = new DatesFilter();
    private final FlexBoxLayout daysLayout = new FlexBoxLayout();
    private final FlexBoxLayout entriesLayout = new FlexBoxLayout();
    private final Select<Project> projectFilter = new Select<>();

    @Autowired
    public DatesPage(DatesViewModel viewModel) {
        this.viewModel = viewModel;

        prepareContent();
        setViewContent(content);
    }

    private void prepareContent() {
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setSpacing(Vertical.XS);
        content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
        content.setMaxWidth("840px");

        initFilterLayout();
        initDaysLayout();
        initEntriesLayout();

        content.add(new Div(projectFilter));
        content.add(datesFilter);
        content.add(daysLayout);
        content.add(entriesLayout);
    }

    private void initFilterLayout() {
        projectFilter.setLabel("Project");
        projectFilter.setItemEnabledProvider(Project::getIsActive);
        projectFilter.setEmptySelectionAllowed(false);
        projectFilter.setItemLabelGenerator(pr -> pr.getTitle());
        projectFilter.addValueChangeListener(event -> {
            viewModel.setProject(event.getValue());
        });

        datesFilter.addDateChangedEventListener(event -> {
            viewModel.setDates(event.getFrom(), event.getTo());
        });
    }

    private void initDaysLayout() {
        daysLayout.setSpacing(Horizontal.XS);
        daysLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        openDays.addClickListener(event -> {
            viewModel.openDays();
        });

        closeDays.addClickListener(event -> {
            viewModel.closeDays();
        });

        daysLayout.add(new Span("Days:"));
        daysLayout.add(openDays);
        daysLayout.add(closeDays);
    }

    private void initEntriesLayout() {
        entriesLayout.setSpacing(Horizontal.XS);
        entriesLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        openEntries.addClickListener(event -> {
            viewModel.openEntries();
        });

        closeEntries.addClickListener(event -> {
            viewModel.closeEntries();
        });

        entriesLayout.add(new Span("Work Logs:"));
        entriesLayout.add(openEntries);
        entriesLayout.add(closeEntries);
    }

    @Override
    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        subscribe(viewModel.projects().subscribe(projects -> {
            projectFilter.setItems(projects);
        }));

        subscribe(viewModel.visibility().subscribe(visibility -> {
            openDays.setEnabled(visibility.get(DatesViewModel.VisibilityItem.OPEN_DAYS));
            closeDays.setEnabled(visibility.get(DatesViewModel.VisibilityItem.CLOSE_DAYS));
            openEntries.setEnabled(visibility.get(DatesViewModel.VisibilityItem.OPEN_ENTRIES));
            closeEntries.setEnabled(visibility.get(DatesViewModel.VisibilityItem.CLOSE_ENTRIES));
        }));

        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            LocalDate date = extendedClientDetails.getCurrentDate()
                    .toInstant()
                    .atZone(ZoneId.of(extendedClientDetails.getTimeZoneId()))
                    .toLocalDate();

            datesFilter.setDate(date);
            datesFilter.setType(DatesFilter.FilterType.THIS_WEEK);
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        unsubscribeAll();
    }
}
