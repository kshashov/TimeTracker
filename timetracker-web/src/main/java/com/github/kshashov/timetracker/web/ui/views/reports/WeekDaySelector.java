package com.github.kshashov.timetracker.web.ui.views.reports;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WeekDaySelector extends FlexBoxLayout implements HasUser {
    private final User user;
    private LocalDate date;
    private LocalDate weekStart;
    private final List<Button> days = new ArrayList<>();

    public WeekDaySelector() {
        this.user = getUser();

        initDaysLayout();

        setAlignItems(Alignment.CENTER);
        setSpacing(Horizontal.XS);
    }

    private void initDaysLayout() {

        int start = user.getWeekStart().getValue() - 1;
        for (int i = 0; i < 7; i++) {
            final int finalI = i;
            Button button = UIUtils.createTertiaryButton("");
            button.setText(DayOfWeek.of(start % 7 + 1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            button.addClickListener(event -> {
                fireEvent(new DateChangedEvent(weekStart.plusDays(finalI), WeekDaySelector.this, false));
            });
            days.add(button);
            add(button);
            start++;
        }
    }

    public void setDate(@NotNull LocalDate date) {
        if (this.date != null && date.isEqual(this.date)) {
            return;
        }

        this.date = date;
        this.weekStart = date.with(TemporalAdjusters.previousOrSame(user.getWeekStart()));

        date = weekStart;
        for (int i = 0; i < 7; i++) {
            Button button = days.get(i);

            // Highlight the current day
            if (date.isEqual(this.date)) {
                button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            } else {
                button.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
            }

            date = date.plusDays(1);
        }
    }

    public Registration addDateChangedEventListener(ComponentEventListener<DateChangedEvent> listener) {
        return addListener(DateChangedEvent.class, listener);
    }

    @Getter
    public static class DateChangedEvent extends ComponentEvent<WeekDaySelector> {
        private LocalDate date;

        public DateChangedEvent(LocalDate date, WeekDaySelector source, boolean fromClient) {
            super(source, fromClient);
            this.date = date;
        }
    }
}
