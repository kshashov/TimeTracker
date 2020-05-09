package com.github.kshashov.timetracker.web.ui.views;

import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.repo.EntriesRepository;
import com.github.kshashov.timetracker.data.repo.EntriesStats;
import com.github.kshashov.timetracker.web.security.HasUser;
import com.github.kshashov.timetracker.web.ui.MainLayout;
import com.github.kshashov.timetracker.web.ui.components.FlexBoxLayout;
import com.github.kshashov.timetracker.web.ui.components.Widget;
import com.github.kshashov.timetracker.web.ui.layout.size.Horizontal;
import com.github.kshashov.timetracker.web.ui.layout.size.Right;
import com.github.kshashov.timetracker.web.ui.layout.size.Uniform;
import com.github.kshashov.timetracker.web.ui.layout.size.Vertical;
import com.github.kshashov.timetracker.web.ui.util.FontWeight;
import com.github.kshashov.timetracker.web.ui.util.UIUtils;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.util.css.FlexWrap;
import com.github.kshashov.timetracker.web.ui.views.reports.DatesFilter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static com.github.kshashov.timetracker.web.ui.views.reports.DatesFilter.calcDateRange;

@PageTitle("Home")
@Route(value = "", layout = MainLayout.class)
public class HomePage extends ViewFrame implements HasUser {
    private final EntriesRepository entriesRepository;
    private final User user;

    private final StatsWidget thisWeek = new StatsWidget("This week activity");
    private final StatsWidget lastWeek = new StatsWidget("Last week activity");
    private final StatsWidget thisMonth = new StatsWidget("This month activity");
    private final StatsWidget lastMonth = new StatsWidget("Last month activity");

    public HomePage(EntriesRepository entriesRepository) {
        this.entriesRepository = entriesRepository;
        this.user = getUser();

        setId("home");
        setViewContent(createContent());
    }

    private Component createContent() {
        Html intro = new Html("<center><p>The application is a simple time tracker. All users can create projects, bind other users with specific roles to them. " +
                "Users with the required permissions can create, modify and delete work logs in projects, view other people's logs, commit them to prohibit further changes.</p></center>");

        Anchor documentation = new Anchor("https://github.com/kshashov/TimeTracker", UIUtils.createButton("GitHub Repository", VaadinIcon.EXTERNAL_LINK));

        FlexBoxLayout week = new FlexBoxLayout(thisWeek, lastWeek);
        week.setSpacing(Horizontal.M);

        FlexBoxLayout month = new FlexBoxLayout(thisMonth, lastMonth);
        month.setSpacing(Horizontal.M);

        FlexBoxLayout links = new FlexBoxLayout(documentation);
        links.setFlexWrap(FlexWrap.WRAP);
        links.setSpacing(Right.S);

        FlexBoxLayout content = new FlexBoxLayout(week, month, intro, links);
        content.setSpacing(Vertical.M);
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO);
        content.setMaxWidth("840px");
        content.setPadding(Uniform.RESPONSIVE_L);
        return content;
    }

    private void setDate(LocalDate date) {
        Pair<LocalDate, LocalDate> range = calcDateRange(date, DatesFilter.FilterType.THIS_WEEK, user.getWeekStart());
        thisWeek.setStats(entriesRepository.statsByUser(user, range.getLeft(), range.getRight()));

        range = calcDateRange(date, DatesFilter.FilterType.LAST_WEEK, user.getWeekStart());
        lastWeek.setStats(entriesRepository.statsByUser(user, range.getLeft(), range.getRight()));

        range = calcDateRange(date, DatesFilter.FilterType.THIS_MONTH, user.getWeekStart());
        thisMonth.setStats(entriesRepository.statsByUser(user, range.getLeft(), range.getRight()));

        range = calcDateRange(date, DatesFilter.FilterType.LAST_MONTH, user.getWeekStart());
        lastMonth.setStats(entriesRepository.statsByUser(user, range.getLeft(), range.getRight()));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            LocalDate date = extendedClientDetails.getCurrentDate()
                    .toInstant()
                    .atZone(ZoneId.of(extendedClientDetails.getTimeZoneId()))
                    .toLocalDate();

            setDate(date);
        });
    }

    public static class StatsWidget extends Widget {
        private final Span total = new Span();
        private final Grid<EntriesStats> grid = new Grid<>();

        public StatsWidget(String title) {
            initStatsGrid();

            UIUtils.setFontWeight(FontWeight.BOLD, total);

            setWidthFull();
            setTitle(title);
            addActions(total);
            addContentItems(grid);
        }

        public void setStats(List<EntriesStats> stats) {
            total.setText("Total: " + stats.stream().map(EntriesStats::getHours).reduce(0.0, Double::sum) + "h");
            grid.setItems(stats);
        }

        private Grid<EntriesStats> initStatsGrid() {
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
            grid.addColumn(EntriesStats::getProject).setTextAlign(ColumnTextAlign.START);
            grid.addColumn(s -> s.getHours() + "h").setTextAlign(ColumnTextAlign.END);
            grid.setHeightByRows(true);
            grid.setWidthFull();
            return grid;
        }
    }
}
