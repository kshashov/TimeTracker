INSERT INTO roles (id, code, description) VALUES (3, 'edit_my_logs', '');
INSERT INTO roles (id, code, description) VALUES (4, 'view_project_logs', '');
INSERT INTO roles (id, code, description) VALUES (5, 'edit_project_info', '');
INSERT INTO roles (id, code, description) VALUES (6, 'edit_project_actions', '');
INSERT INTO roles (id, code, description) VALUES (7, 'edit_project_users', '');

INSERT INTO roles_permissions (role_id, permission_id) VALUES (3, 1);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (4, 2);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (5, 3);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (6, 4);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (7, 5);

INSERT INTO users (id, name, email, validated) VALUES (3, 'edit_my_logs', 'edit_my_logs', false);
INSERT INTO users (id, name, email, validated) VALUES (4, 'view_project_logs', 'view_project_logs', false);
INSERT INTO users (id, name, email, validated) VALUES (5, 'edit_project_info', 'edit_project_info', false);
INSERT INTO users (id, name, email, validated) VALUES (6, 'edit_project_actions', 'edit_project_actions', false);
INSERT INTO users (id, name, email, validated) VALUES (7, 'edit_project_users', 'edit_project_users', false);

INSERT INTO projects (id, title, active) VALUES (1, 'project', true);

INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 3, 3);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 4, 4);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 5, 5);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 6, 6);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 7, 7);


