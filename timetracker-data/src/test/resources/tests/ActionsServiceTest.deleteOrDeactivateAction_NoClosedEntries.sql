INSERT INTO actions (id, project_id, title, active) VALUES (1, 1, 'deleteOrDeactivateAction_NoClosedEntries', true);

INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-01', 'entry', 1, false);
INSERT INTO entries (action_id, user_id, obs, title, hours, closed) VALUES (1, 1, '2020-01-02', 'entry', 1, false);