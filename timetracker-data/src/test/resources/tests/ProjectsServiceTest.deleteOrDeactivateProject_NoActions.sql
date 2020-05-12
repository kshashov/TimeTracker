INSERT INTO projects (id, title, active) VALUES (2, 'deleteOrDeactivateProject_NoActions', true);

INSERT INTO closed_days (project_id, obs) VALUES (2, '2020-01-5');

INSERT INTO project_roles (project_id, user_id, role_id) VALUES (2, 1, 2);
