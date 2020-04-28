package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.data.entity.user.User;
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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

@Route(value = "daily", layout = MainLayout.class)
@PageTitle("Daily Work")
public class DailyPage extends ViewFrame implements HasUser {
    private final DailyEntriesWidget dailyEntriesWidget;
    private final User user;

    private final FlexBoxLayout content = new FlexBoxLayout();
    private final DatePicker datePicker = new DatePicker();
    private final Button previous = UIUtils.createTertiaryButton(VaadinIcon.ANGLE_LEFT);
    private final Button next = UIUtils.createTertiaryButton(VaadinIcon.ANGLE_RIGHT);

    @Autowired
    public DailyPage(DailyEntriesWidget dailyEntriesWidget) {
        this.dailyEntriesWidget = dailyEntriesWidget;
        this.user = getUser();

        prepareContent();
        setViewContent(content);

        datePicker.addValueChangeListener(event -> {
            dailyEntriesWidget.setVisible(event.getValue() != null);
            if (event.getValue() != null) {
                dailyEntriesWidget.setDate(event.getValue());
            }
        });

        previous.addClickListener(event -> {
            LocalDate date = dailyEntriesWidget.getDate();
            if (date != null) {
                datePicker.setValue(date.minusDays(1));
            }
        });

        next.addClickListener(event -> {
            LocalDate date = dailyEntriesWidget.getDate();
            if (date != null) {
                datePicker.setValue(date.plusDays(1));
            }
        });
    }

    private void prepareContent() {
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
        content.setMaxWidth("840px");

        datePicker.setWeekNumbersVisible(true);
        datePicker.setLocale(Locale.ENGLISH);

        FlexBoxLayout datePickerLayout = new FlexBoxLayout();
        datePickerLayout.setFlexDirection(FlexDirection.ROW);
        datePickerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        datePickerLayout.add(previous);
        datePickerLayout.add(datePicker);
        datePickerLayout.add(next);

        content.add(datePickerLayout);
        content.add(dailyEntriesWidget);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            LocalDate date = extendedClientDetails.getCurrentDate()
                    .toInstant()
                    .atZone(ZoneId.of(extendedClientDetails.getTimeZoneId()))
                    .toLocalDate();

            datePicker.setInitialPosition(date);
            datePicker.setValue(date);
        });

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setTitle("Daily Work");
    }
}
