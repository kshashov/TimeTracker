package com.github.kshashov.timetracker.web.ui.views.reports;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.shared.Registration;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public class DatesFilter extends FlexBoxLayout implements HasUser {
    private boolean supressEvents = false;
    private final User user;
    private LocalDate date;
    private FilterType type;

    private final Select<FilterType> select = new Select<>(FilterType.values());
    private final DatePicker from = new DatePicker("From");
    private final DatePicker to = new DatePicker("To");
    private final FlexBoxLayout datePickerLayout = new FlexBoxLayout();

    public DatesFilter() {
        this.user = getUser();

        setFlexDirection(FlexDirection.ROW);
        setAlignItems(Alignment.CENTER);
        setSpacing(Horizontal.XS);

        initSelect();
        initDatePicker();

        add(select, datePickerLayout);
    }

    public void setDate(LocalDate date) {
        this.date = date;
        from.setInitialPosition(date);
        to.setInitialPosition(date);
    }

    public void setType(FilterType type) {
        assert type != FilterType.CUSTOM;

        if (select.isEmpty()) {
            supressEvents = true;
            select.setValue(type);
            supressEvents = false;
        }

        LocalDate from;
        LocalDate to;

        if (type.equals(FilterType.THIS_WEEK)) {
            from = date.with(TemporalAdjusters.previousOrSame(user.getWeekStart()));
            to = from.plusDays(6);
        } else if (type.equals(FilterType.LAST_WEEK)) {
            from = date.with(TemporalAdjusters.previousOrSame(user.getWeekStart())).with(TemporalAdjusters.previous(user.getWeekStart()));
            to = from.plusDays(6);
        } else if (type.equals(FilterType.THIS_MONTH)) {
            from = date.with(TemporalAdjusters.firstDayOfMonth());
            to = date.with(TemporalAdjusters.lastDayOfMonth());
        } else if (type.equals(FilterType.LAST_MONTH)) {
            from = date.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            to = from.with(TemporalAdjusters.lastDayOfMonth());
        } else if (type.equals(FilterType.THIS_YEAR)) {
            from = date.with(TemporalAdjusters.firstDayOfYear());
            to = date.with(TemporalAdjusters.lastDayOfYear());
        } else {
            // FilterType.LAST_YEAR
            from = date.minusYears(1).with(TemporalAdjusters.firstDayOfYear());
            to = from.with(TemporalAdjusters.lastDayOfYear());
        }

        fireEvent(new DateChangedEvent(from, to, this, false));

        datePickerLayout.setVisible(false);
    }

    public void setCustomType(LocalDate from, LocalDate to) {
        this.type = FilterType.CUSTOM;

        if (select.isEmpty()) {
            supressEvents = true;
            select.setValue(type);
            supressEvents = false;
        }

        supressEvents = true;
        this.from.setValue(from);
        this.to.setValue(to);
        supressEvents = false;

        fireEvent(new DateChangedEvent(from, to, this, false));

        datePickerLayout.setVisible(true);
    }

    private void initSelect() {
        select.setLabel("Date Range");
        select.setEmptySelectionAllowed(false);
        select.setItemLabelGenerator(FilterType::getTitle);
        select.addValueChangeListener(event -> {
            if (supressEvents) {
                return;
            }
            if (event.getValue().equals(FilterType.CUSTOM)) {
                setCustomType(date.with(TemporalAdjusters.previousOrSame(user.getWeekStart())), date);
            } else {
                setType(event.getValue());
            }
        });
    }

    private void initDatePicker() {
        from.setLocale(Locale.ENGLISH);
        from.setClearButtonVisible(true);
        from.setRequired(true);
        from.setRequiredIndicatorVisible(true);
        from.addValueChangeListener(event -> {
            if (supressEvents) {
                return;
            }
            setCustomType(event.getValue(), to.getValue());
        });

        to.setLocale(Locale.ENGLISH);
        to.setClearButtonVisible(true);
        to.setRequired(true);
        to.addValueChangeListener(event -> {
            if (supressEvents) {
                return;
            }
            setCustomType(from.getValue(), event.getValue());
        });

        datePickerLayout.setFlexDirection(FlexDirection.ROW);
        datePickerLayout.setAlignItems(Alignment.CENTER);
        datePickerLayout.setSpacing(Horizontal.XS);
        datePickerLayout.add(from);
        datePickerLayout.add(to);
        datePickerLayout.setVisible(false);
    }

    public Registration addDateChangedEventListener(ComponentEventListener<DateChangedEvent> listener) {
        return addListener(DateChangedEvent.class, listener);
    }

    @Getter
    @AllArgsConstructor
    public enum FilterType {
        THIS_WEEK("This Week"), LAST_WEEK("Last Week"),
        THIS_MONTH("This Month"), LAST_MONTH("Last Month"),
        THIS_YEAR("This Year"), LAST_YEAR("Last Year"),
        CUSTOM("Custom");

        private final String title;
    }

    @Getter
    public static class DateChangedEvent extends ComponentEvent<DatesFilter> {
        private final LocalDate from;
        private final LocalDate to;

        public DateChangedEvent(LocalDate from, LocalDate to, DatesFilter source, boolean fromClient) {
            super(source, fromClient);
            this.from = from;
            this.to = to;
        }
    }
}
