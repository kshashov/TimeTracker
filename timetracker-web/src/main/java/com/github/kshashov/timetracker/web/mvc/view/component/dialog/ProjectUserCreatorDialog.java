package com.github.kshashov.timetracker.web.mvc.view.component.dialog;

import com.github.kshashov.timetracker.data.entity.user.ProjectRoles;
import com.github.kshashov.timetracker.data.entity.user.Role;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.CallbackDataProvider;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ProjectUserCreatorDialog extends AbstractEditorDialog<ProjectRoles> {
    private final ComboBox<User> user = new ComboBox<>();
    private final Select<Role> role = new Select<>();

    public ProjectUserCreatorDialog(String title, Consumer<ProjectRoles> itemSaver, CallbackDataProvider<User, String> usersDataProvider, List<Role> roles) {
        super(title, itemSaver);
        getFormLayout().add(createUserField(usersDataProvider));
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

    protected ComboBox<User> createUserField(CallbackDataProvider<User, String> usersDataProvider) {
        user.setLabel("User");
/*        user.setRenderer(new ComponentRenderer<>(u -> {
            var name = u == null? "": u.getName();
            return new Span(StringUtils.defaultString(name));
        }));*/
        user.setItemLabelGenerator(u -> u == null ? "" : u.getName());
        user.setAllowCustomValue(false);
        user.setRequiredIndicatorVisible(true);
        user.setPageSize(30);
        user.setDataProvider(usersDataProvider);

        getBinder().forField(user)
                .withValidator(Objects::nonNull, "User is empty")
                .bind(ProjectRoles::getUser, ProjectRoles::setUser);

        return user;
    }

    @Override
    protected void onDialogOpened(ProjectRoles item) {

    }
}
