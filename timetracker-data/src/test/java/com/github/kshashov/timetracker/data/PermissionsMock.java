package com.github.kshashov.timetracker.data;

import com.github.kshashov.timetracker.data.entity.Project;
import com.github.kshashov.timetracker.data.entity.user.User;
import com.github.kshashov.timetracker.data.enums.ProjectPermissionType;
import com.github.kshashov.timetracker.data.utils.RolePermissionsHelper;
import lombok.AllArgsConstructor;
import org.mockito.ArgumentMatcher;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class PermissionsMock {
    public static PermissionMockProcess whenPermission(RolePermissionsHelper helper) {
        return new PermissionMockProcess(helper);
    }

    @AllArgsConstructor
    public static class PermissionMockProcess {
        private final RolePermissionsHelper helper;

        public void allow(User user, Project project, ProjectPermissionType projectPermissionType) {
            when(helper.hasProjectPermission(eq(user), argThat(ProjectArgumentMatcher.of(project)), eq(projectPermissionType)))
                    .thenReturn(true);
        }

        public void deny(User user, Project project, ProjectPermissionType projectPermissionType) {
            when(helper.hasProjectPermission(eq(user), argThat(ProjectArgumentMatcher.of(project)), eq(projectPermissionType)))
                    .thenReturn(false);
        }

        @AllArgsConstructor
        public static class ProjectArgumentMatcher implements ArgumentMatcher<Project> {
            private final Project left;

            @Override
            public boolean matches(Project right) {
                return (left == right) || (left.getId().equals(right.getId()));
            }

            public static ProjectArgumentMatcher of(Project left) {
                return new ProjectArgumentMatcher(left);
            }
        }

        @AllArgsConstructor
        public static class UserArgumentMatcher implements ArgumentMatcher<User> {
            private final User left;

            @Override
            public boolean matches(User right) {
                return (left == right) || (left.getId().equals(right.getId()));
            }

            public static UserArgumentMatcher of(User left) {
                return new UserArgumentMatcher(left);
            }
        }
    }
}
