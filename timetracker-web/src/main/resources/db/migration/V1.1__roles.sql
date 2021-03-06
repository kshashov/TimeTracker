INSERT INTO permissions (id, code)
VALUES (0, 'view_project_info');
INSERT INTO permissions (id, code)
VALUES (1, 'edit_my_logs');

INSERT INTO permissions (id, code)
VALUES (2, 'view_project_logs');
INSERT INTO permissions (id, code)
VALUES (3, 'edit_project_info');
INSERT INTO permissions (id, code)
VALUES (4, 'edit_project_actions');
INSERT INTO permissions (id, code)
VALUES (5, 'edit_project_users');
INSERT INTO permissions (id, code)
VALUES (6, 'edit_project_logs');

INSERT INTO roles (id, code, title, description)
VALUES (1, 'project_user', 'User', 'User can create, edit and delete his working logs');
INSERT INTO roles (id, code, title, description)
VALUES (2, 'project_admin', 'Admin',
        'Admin can manage project activities and users, his work logs, view other people''s logs, close days and work logs');
INSERT INTO roles (id, code, title, description)
VALUES (3, 'project_inactive', 'Inactive', 'Inactive user can view his already created work logs');
INSERT INTO roles (id, code, title, description)
VALUES (4, 'project_reporter', 'Reporter',
        'Reporter can manage his work logs, view other people''s logs, close days and project work logs');

INSERT INTO roles_permissions (role_id, permission_id)
VALUES (1, 0);
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (1, 1);

INSERT INTO roles_permissions (role_id, permission_id)
VALUES (2, 0);
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (2, 1);
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (2, 2);
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (2, 3);
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (2, 4);
INSERT INTO roles_permissions (role_id, permission_id)
VALUES (2, 5);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (2, 6);

INSERT INTO roles_permissions (role_id, permission_id) VALUES (4, 0);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (4, 1);
INSERT INTO roles_permissions (role_id, permission_id) VALUES (4, 2);