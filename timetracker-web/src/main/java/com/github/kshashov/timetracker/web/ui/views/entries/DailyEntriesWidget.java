package com.github.kshashov.timetracker.web.ui.views.entries;

import com.github.kshashov.timetracker.data.entity.Action;
import com.github.kshashov.timetracker.data.entity.Entry;
import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.web.ui.components.*;
import com.github.kshashov.timetracker.web.ui.layout.size.Uniform;
import com.github.kshashov.timetracker.web.ui.util.*;
import com.github.kshashov.timetracker.web.ui.util.css.BoxSizing;
import com.github.kshashov.timetracker.web.ui.util.css.FlexDirection;
import com.github.kshashov.timetracker.web.ui.util.css.lumo.BadgeColor;
import com.github.kshashov.timetracker.web.ui.views.entries.actionchooser.ActionChooser;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import rx.Subscription;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
public class DailyEntriesWidget extends LargeWidget implements HasSubscriptions {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("E, d MMM", Locale.ENGLISH);

    private final DailyEntriesViewModel viewModel;
    private final List<Subscription> subscriptions = new ArrayList<>();

    private LocalDate date;
    private Entry createdEntry;
    private Function<Entry, ValidationResult> createEntryValidator;
    private Function<Entry, ValidationResult> updateEntryValidator;

    private final ActionChooser actionChooser = new ActionChooser(FlexDirection.COLUMN);
    private final ActionChooser createActionChooser = new ActionChooser(FlexDirection.ROW);
    private final Grid<Entry> entriesGrid = new Grid<>();
    private final Span total = new Span();
    private final Editor<Entry> editor = entriesGrid.getEditor();
    private final FlexBoxLayout createLayout = new FlexBoxLayout();
    private final Binder<Entry> createBinder = new Binder<>(Entry.class);
    private final ConfirmDialog deleteDialog = new ConfirmDialog("Delete", "Are you sure you want to delete this work item?");

    @Autowired
    public DailyEntriesWidget(DailyEntriesViewModel viewModel) {
        this.viewModel = viewModel;

        initActions(viewModel);

        initCreateLayout();
        initEntriesGrid();

        addContentItems(createLayout, entriesGrid);
    }

    public void setDate(LocalDate date) {
        this.date = date;

        setTitle(date.format(dateFormatter));

        viewModel.setDate(date);
        viewModel.createEntry();
    }

    public LocalDate getDate() {
        return date;
    }

    private void initActions(DailyEntriesViewModel viewModel) {
        UIUtils.setFontWeight(FontWeight.BOLD, total);

        Button refresh = UIUtils.createTertiaryButton(VaadinIcon.REFRESH);
        refresh.addClickListener(event -> {
            editor.cancel();
            viewModel.reloadEntries();
        });

        addActions(total, refresh);
    }

    private void initCreateLayout() {
        createLayout.setFlexDirection(FlexDirection.ROW);
        createLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        createLayout.setWidthFull();
        createLayout.setAlignItems(Alignment.CENTER);
        createLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        createLayout.setPadding(Uniform.L);
        createLayout.setSpacing(Uniform.XS);
        createLayout.setPadding(Uniform.XS);
        createLayout.setBackgroundColor(LumoStyles.Color.Contrast._5);

        createBinder.withValidator((entry, context) -> createEntryValidator.apply(entry));

        createBinder.forField(createActionChooser).bind(Entry::getAction, Entry::setAction);

        NumberField hoursField = new NumberField("Hours");
        hoursField.addThemeName("small");
        createBinder.forField(hoursField).bind(Entry::getHours, Entry::setHours);

        TextField titleField = new TextField("Description");
        titleField.addThemeName("small");
        titleField.setWidthFull();
        createBinder.forField(titleField).bind(Entry::getTitle, Entry::setTitle);

        Button plus = UIUtils.createTertiaryButton(VaadinIcon.PLUS_CIRCLE_O);
        plus.addClickListener(e -> {
            editor.cancel();
            if (createBinder.writeBeanIfValid(createdEntry)) {
                // Show new entry
                viewModel.createEntry();
            }
        });

        createLayout.add(createActionChooser, hoursField, titleField, plus);
    }

    private void initEntriesGrid() {
        entriesGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        entriesGrid.setSelectionMode(Grid.SelectionMode.NONE);
        entriesGrid.setHeightByRows(true);

        Grid.Column<Entry> infoColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            Action action = entry.getAction();
            Project project = action.getProject();

            ListItem item = new ListItem(
                    StringUtils.defaultString(project.getTitle()),
                    StringUtils.defaultString(action.getTitle()));

            if (!project.getIsActive()) {
                item.getPrimary().getStyle().set("text-decoration", "line-through");
            }

            if (!action.getIsActive()) {
                item.getSecondary().getStyle().set("text-decoration", "line-through");
            }

            return item;
        })).setAutoWidth(true);

        Grid.Column<Entry> hoursColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            return UIUtils.createHoursLabel(entry.getHours());
        })).setAutoWidth(true);

        Grid.Column<Entry> titleColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            return UIUtils.createLabel(FontSize.M, StringUtils.defaultString(entry.getTitle()));
        })).setAutoWidth(true);

        Grid.Column<Entry> actionsColumn = entriesGrid.addColumn(new ComponentRenderer<>(entry -> {
            if (entry.getIsClosed()) {
                return new Badge("Closed", BadgeColor.NORMAL);
            }

            Button edit = UIUtils.createActionButton(VaadinIcon.PENCIL);
            edit.addClickListener(e -> {
                editor.cancel();
                viewModel.updateEntry(entry);
            });

            Button delete = UIUtils.createActionButton(VaadinIcon.MINUS_CIRCLE_O);
            delete.addClickListener(e -> {
                editor.cancel();
                deleteDialog.open(b -> {
                    if (b) {
                        viewModel.deleteEntry(entry);
                    }
                });
            });

            HorizontalLayout layout = new HorizontalLayout(edit, delete);
            return layout;
        })).setFlexGrow(0).setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

        // Configure editor

        Binder<Entry> binder = new Binder<>(Entry.class);
        binder.withValidator((entry, context) -> updateEntryValidator.apply(entry));

        editor.setBinder(binder);
        editor.setBuffered(true);

        binder.forField(actionChooser).bind(Entry::getAction, Entry::setAction);

        infoColumn.setEditorComponent(actionChooser);

        HorizontalLayout infoField = new HorizontalLayout();

        NumberField hoursField = new NumberField("Hours");
        hoursField.addThemeName("small");
        binder.forField(hoursField).bind(Entry::getHours, Entry::setHours);

        hoursColumn.setEditorComponent(hoursField);

        TextField titleField = new TextField("Description");
        titleField.addThemeName("small");
        titleField.setWidthFull();
        binder.forField(titleField).bind(Entry::getTitle, Entry::setTitle);

        infoField.add(hoursField, titleField);
        infoField.setAlignItems(FlexComponent.Alignment.CENTER);

        titleColumn.setEditorComponent(titleField);

        Button save = UIUtils.createActionButton(VaadinIcon.CHECK_CIRCLE_O);
        save.addClickListener(e -> {
            if (editor.isOpen() && editor.isBuffered()) {
                if (binder.writeBeanIfValid(editor.getItem())) {
                    editor.cancel();
                }
            }
        });

        Button cancel = UIUtils.createActionButton(VaadinIcon.CLOSE_CIRCLE_O);
        cancel.addClickListener(e -> editor.cancel());

        HorizontalLayout actionsField = new HorizontalLayout(save, cancel);
        actionsColumn.setEditorComponent(actionsField);

        entriesGrid.getElement().addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        subscribe(viewModel.createEntryDialog()
                .subscribe(dialog -> {
                    createdEntry = dialog.getEntry();
                    createEntryValidator = dialog.getValidator();
                    createBinder.readBean(null); // clear fields
                    createBinder.readBean(createdEntry);
                }));

        subscribe(viewModel.updateEntryDialog()
                .subscribe(dialog -> {
                    updateEntryValidator = dialog.getValidator();
                    editor.editItem(dialog.getEntry());
                }));

        subscribe(viewModel.projects()
                .subscribe(projects -> {
                    editor.cancel();
                    actionChooser.setProjects(projects);
                }));

        subscribe(viewModel.openProjects()
                .subscribe(projects -> {
                    createActionChooser.setProjects(projects);
                }));

        subscribe(viewModel.entries()
                .subscribe(entries -> {
                    editor.cancel();
                    total.setText("Total: " + entries.stream().map(Entry::getHours).reduce(0.0, Double::sum) + "h");
                    entriesGrid.setItems(entries);
                }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        unsubscribeAll();
    }

    @Override
    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }
}
