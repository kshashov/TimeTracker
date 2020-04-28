package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.navigation.bar.AppBar;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.views.entries.DailyEntriesWidget;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Route(value = "weekly", layout = MainLayout.class)
@PageTitle("Weekly Work")
public class WeeklyPage extends ViewFrame implements HasUser {
    private LocalDate date;

    private final List<DailyEntriesWidget> widgets = new ArrayList<>();
    private final FlexBoxLayout content = new FlexBoxLayout();
    private final Button previous = UIUtils.createTertiaryButton(VaadinIcon.ANGLE_LEFT);
    private final Button next = UIUtils.createTertiaryButton(VaadinIcon.ANGLE_RIGHT);

    @Autowired
    public WeeklyPage(
            DailyEntriesWidget first,
            DailyEntriesWidget second,
            DailyEntriesWidget third,
            DailyEntriesWidget forth,
            DailyEntriesWidget fifth,
            DailyEntriesWidget sixth,
            DailyEntriesWidget seventh) {
        this.widgets.add(first);
        this.widgets.add(second);
        this.widgets.add(third);
        this.widgets.add(forth);
        this.widgets.add(fifth);
        this.widgets.add(sixth);
        this.widgets.add(seventh);

        prepareContent();
        setViewContent(content);

        previous.addClickListener(event -> {
            if (date != null) {
                setStartWeek(date.minusWeeks(1));
            }
        });

        next.addClickListener(event -> {
            if (date != null) {
                setStartWeek(date.plusWeeks(1));
            }
        });
    }

    private void prepareContent() {
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
        content.setMaxWidth("840px");

        FlexBoxLayout datePickerLayout = new FlexBoxLayout();
        datePickerLayout.setFlexDirection(FlexDirection.ROW);
        datePickerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        datePickerLayout.add(previous);
        datePickerLayout.add(next);

        content.add(datePickerLayout);
        widgets.forEach(content::add);
    }

    private void setStartWeek(LocalDate date) {
        this.date = date;
        boolean isVisible = date != null;
        widgets.forEach(widget -> widget.setVisible(isVisible));
        if (date != null) {
            for (DailyEntriesWidget widget : widgets) {
                widget.setDate(date);
                date = date.plusDays(1);
            }
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            LocalDate date = extendedClientDetails.getCurrentDate()
                    .toInstant()
                    .atZone(ZoneId.of(extendedClientDetails.getTimeZoneId()))
                    .toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(getUser().getWeekStart()));

            setStartWeek(date);
        });

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setTitle("Weekly Work");
    }
}
