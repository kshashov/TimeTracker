package com.github.kshashov.timetracker.web.ui.views.admin.projects.dialogs;

import com.github.kshashov.timetracker.data.entity.user.ProjectRole;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.web.ui.components.AbstractEditorDialog;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationResult;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ProjectRoleEditorDialog extends AbstractEditorDialog<ProjectRole> {
    private final TextField user = new TextField("User");
    private final Select<Role> role = new Select<>();

    public ProjectRoleEditorDialog(String title, Function<ProjectRole, ValidationResult> itemSaver, List<Role> roles) {
        super(title, itemSaver);
        getFormLayout().add(createUserField());
        getFormLayout().add(createRoleField(roles));
    }

    protected Select<Role> createRoleField(List<Role> roles) {
        role.setLabel("Role");
        role.setItemLabelGenerator(r -> r == null ? "" : r.getTitle());
        role.setEmptySelectionAllowed(false);
        role.setRequiredIndicatorVisible(true);
        role.setItems(roles);

        getBinder().forField(role)
                .withValidator(Objects::nonNull, "Role is empty")
                .bind(
                        pr -> roles.stream().filter(r -> r.getId().equals(pr.getRole().getId())).findFirst().orElse(null),
                        ProjectRole::setRole
                );

        return role;
    }

    protected TextField createUserField() {
        user.setReadOnly(true);
        getBinder().forField(user)
                .withNullRepresentation("")
                .bind(
                        pr -> pr.getUser().getName(),
                        (pr, s) -> {
                        }
                );

        return user;
    }

    @Override
    protected void onDialogOpened(ProjectRole item) {

    }
}
