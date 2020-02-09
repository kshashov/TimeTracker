package com.github.kshashov.timetracker.web.mvc.view.component.dialog;

import com.github.kshashov.timetracker.data.entity.user.ProjectRoles;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ProjectUserEditorDialog extends AbstractEditorDialog<ProjectRoles> {
    private final TextField user = new TextField("User");
    private final Select<Role> role = new Select<>();

    public ProjectUserEditorDialog(String title, Consumer<ProjectRoles> itemSaver, List<Role> roles) {
        super(title, itemSaver);
        getFormLayout().add(createUserField());
        getFormLayout().add(createRoleField(roles));
    }

    protected Select<Role> createRoleField(List<Role> roles) {
        role.setLabel("Role");
        role.setItemLabelGenerator(r -> r == null ? "" : r.getTitle());
        role.setEmptySelectionAllowed(true);
        role.setRequiredIndicatorVisible(true);
        role.setItems(roles);

        getBinder().forField(role)
                .withValidator(Objects::nonNull, "Role is empty")
                .bind(ProjectRoles::getRole, ProjectRoles::setRole);

        return role;
    }

    protected TextField createUserField() {
        user.setReadOnly(true);
        getBinder().forField(user).withNullRepresentation("")
                .bind(pr -> pr.getUser().getName(), (pr, s) -> {
                });

        return user;
    }

    @Override
    protected void onDialogOpened(ProjectRoles item) {

    }
}
