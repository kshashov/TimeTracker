INSERT INTO actions (id, project_id, title, active) VALUES (1, 1, 'openEntries_Ok', true);

INSERT INTO project_roles (project_id, user_id, role_id) VALUES (1, 1, 1);

INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-5', 'entry', 1, false);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-6', 'entry', 1, true);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-7', 'entry', 1, false);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-8', 'entry', 1, false);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-9', 'entry', 1, true);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-10', 'entry', 1, false);
