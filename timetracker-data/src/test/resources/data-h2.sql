INSERT INTO tests (title) VALUES ('text');

INSERT INTO users (id, name, email, validated) VALUES (1, 'user', 'user@mail.com', true);
INSERT INTO projects (id, title, active) VALUES (1, 'project', true);
INSERT INTO actions (id, project_id, title, active) VALUES (1, 1, 'action', true);

INSERT INTO permissions (id, code) VALUES (1, 'edit_my_logs');

INSERT INTO permissions (id, code) VALUES (2, 'edit_project_logs');
INSERT INTO permissions (id, code) VALUES (3, 'edit_project_info');
INSERT INTO permissions (id, code) VALUES (4, 'edit_project_actions');
INSERT INTO permissions (id, code) VALUES (5, 'edit_project_users');

INSERT INTO roles (id, code, title, description) VALUES (1, 'project_user','User', 'Just a user');
INSERT INTO roles (id, code, title, description) VALUES (2, 'project_admin', 'Admin', 'Admin user');
INSERT INTO roles (id, code, title, description) VALUES (3, 'project_inactive', 'Inactive', 'Inactive user');
--INSERT INTO roles (id, title) VALUES (3, 'project_creator');

INSERT INTO roles_permissions (role_id, permission_id) VALUES (1, 1);

INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 1);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 2);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 3);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 4);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 5);

