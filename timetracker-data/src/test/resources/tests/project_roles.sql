INSERT INTO roles (id, code, description) VALUES (3, 'edit_my_logs', '');
INSERT INTO roles (id, code, description) VALUES (4, 'edit_project_logs', '');
INSERT INTO roles (id, code, description) VALUES (5, 'edit_project_info', '');
INSERT INTO roles (id, code, description) VALUES (6, 'edit_project_actions', '');
INSERT INTO roles (id, code, description) VALUES (7, 'edit_project_users', '');

INSERT INTO users (id, name, email, validated) VALUES (3, 'edit_my_logs', 'edit_my_logs', false);
INSERT INTO users (id, name, email, validated) VALUES (4, 'edit_project_logs', 'edit_project_logs', false);
INSERT INTO users (id, name, email, validated) VALUES (5, 'edit_project_info', 'edit_project_info', false);
INSERT INTO users (id, name, email, validated) VALUES (6, 'edit_project_actions', 'edit_project_actions', false);
INSERT INTO users (id, name, email, validated) VALUES (7, 'edit_project_users', 'edit_project_users', false);

INSERT INTO projects (id, title, active) VALUES (1, 'project', true);

INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 3, 3);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 4, 4);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 5, 5);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 6, 6);
INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 7, 7);


