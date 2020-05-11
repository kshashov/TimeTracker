INSERT INTO permissions (id, code) VALUES (0, 'view_project_info');
INSERT INTO permissions (id, code) VALUES (1, 'edit_my_logs');

INSERT INTO permissions (id, code) VALUES (2, 'view_project_logs');
INSERT INTO permissions (id, code) VALUES (3, 'edit_project_info');
INSERT INTO permissions (id, code) VALUES (4, 'edit_project_actions');
INSERT INTO permissions (id, code) VALUES (5, 'edit_project_users');
INSERT INTO permissions (id, code) VALUES (6, 'edit_project_logs');

INSERT INTO roles (id, code, title, description) VALUES (1, 'project_user','User', 'Just a user');
INSERT INTO roles (id, code, title, description) VALUES (2, 'project_admin', 'Admin', 'Admin user');
INSERT INTO roles (id, code, title, description) VALUES (3, 'project_inactive', 'Inactive', 'Inactive user');
INSERT INTO roles (id, code, title, description) VALUES (4, 'project_reporter', 'Reporter', 'Reporter user');

INSERT INTO roles_permissions (role_id, permission_id) VALUES (1, 0);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (1, 1);

INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 0);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 1);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 2);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 3);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 4);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 5);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 6);

INSERT INTO roles_permissions (role_id, permission_id) VALUES (4, 0);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (4, 1);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (4, 2);

-- tests
INSERT INTO users (id, name, email, week_start, validated) VALUES (1, 'user', 'user@mail.com', 0, false);
INSERT INTO users (id, name, email, week_start, validated) VALUES (2, 'user2', 'envoy93@gmail.com', 0, true);

INSERT INTO projects (id, title, active) VALUES (0, 'project0', true);
INSERT INTO actions (id, project_id, title, active) VALUES (0, 0, 'action0', true);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (0, 2, 2);

INSERT INTO projects (id, title, active) VALUES (1, 'project1', true);
INSERT INTO actions (id, project_id, title, active) VALUES (1, 1, 'action1', true);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 2, 1);

INSERT INTO projects (id, title, active) VALUES (2, 'project2', true);
INSERT INTO actions (id, project_id, title, active) VALUES (2, 2, 'action2', true);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (2, 1, 3);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (2, 2, 2);

INSERT INTO projects (id, title, active) VALUES (3, 'project3', false);
INSERT INTO actions (id, project_id, title, active) VALUES (3, 3, 'action3', false);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (3, 1, 2);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (3, 2, 2);

INSERT INTO projects (id, title, active) VALUES (4, 'project4', false);
INSERT INTO actions (id, project_id, title, active) VALUES (4, 4, 'action4', false);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (4, 1, 3);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (4, 2, 3);

INSERT INTO projects (id, title, active) VALUES (5, 'project5', true);
INSERT INTO actions (id, project_id, title, active) VALUES (5, 5, 'action5', true);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (5, 1, 2);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (5, 2, 3);

INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (2, 2, '2020-01-01', 'entry', 1, false);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (3, 2, '2020-01-01', 'entry', 1, true);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (4, 2, '2020-01-02', 'entry', 1, true);


