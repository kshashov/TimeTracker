INSERT INTO projects (id, title, active) VALUES (2, 'deleteOrDeactivateProject_NoClosedEntries', true);

INSERT INTO closed_days (project_id, obs) VALUES (2, '2020-01-5');

INSERT INTO actions (id, project_id, title, active) VALUES (1, 2, 'action1', true);
INSERT INTO actions (id, project_id, title, active) VALUES (2, 2, 'action2', true);

INSERT INTO project_roles (project_id, user_id, role_id) VALUES (2, 1, 2);

INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-01', 'entry', 1, false);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (2, 1, '2020-01-01', 'entry', 1, false);