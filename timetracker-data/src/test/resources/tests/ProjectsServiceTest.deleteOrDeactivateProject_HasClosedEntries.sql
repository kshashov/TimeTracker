INSERT INTO projects (id, title, active) VALUES (1, 'project', true);
INSERT INTO actions (id, project_id, title, active) VALUES (1, 1, 'action1', true);
INSERT INTO actions (id, project_id, title, active) VALUES (2, 1, 'action2', true);

INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 1, 2);

INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-01', 'entry', 1, false);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (2, 1, '2020-01-01', 'entry', 1, true);