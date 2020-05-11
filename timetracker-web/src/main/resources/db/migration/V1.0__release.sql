create table public.actions
(
    id         int8 generated by default as identity,
    active     boolean      not null,
    title      varchar(255) not null,
    project_id int8         not null,
    primary key (id)
);
create table public.closed_days
(
    obs        date not null,
    project_id int8 not null,
    primary key (obs, project_id)
);
create table public.entries
(
    id         int8 generated by default as identity,
    created_at timestamp,
    hours      float8       not null,
    closed     boolean      not null,
    obs        date         not null,
    title      varchar(255) not null,
    update_at  timestamp,
    action_id  int8         not null,
    user_id    int8         not null,
    primary key (id)
);
create table public.permissions
(
    id   int8 generated by default as identity,
    code varchar(255) not null,
    primary key (id)
);
create table public.project_roles
(
    project_id int8 not null,
    user_id    int8 not null,
    role_id    int8 not null,
    primary key (project_id, user_id)
);
create table public.projects
(
    id     int8 generated by default as identity,
    active boolean      not null,
    title  varchar(255) not null,
    primary key (id)
);
create table public.roles
(
    id          int8 generated by default as identity,
    code        varchar(255) not null,
    description varchar(255) not null,
    title       varchar(255) not null,
    primary key (id)
);
create table public.users
(
    id         int8 generated by default as identity,
    email      varchar(255) not null,
    validated  boolean      not null,
    name       varchar(255) not null,
    week_start int4         not null,
    primary key (id)
);
alter table if exists public.actions
    add constraint actions_unique_project_id_title unique (project_id, title);
alter table if exists public.permissions
    add constraint UK_7lcb6glmvwlro3p2w2cewxtvd unique (code);
alter table if exists public.projects
    add constraint projects_unique_title unique (title);
alter table if exists public.roles
    add constraint roles_unique_code unique (code);
alter table if exists public.roles
    add constraint roles_unique_title unique (title);
alter table if exists public.users
    add constraint users_unique_email unique (email);
create table public.roles_permissions
(
    role_id       int8 not null,
    permission_id int8 not null,
    primary key (role_id, permission_id)
);
alter table if exists public.actions
    add constraint FKelilaf9xl6ym3ighjh5bbxoy4 foreign key (project_id) references public.projects;
alter table if exists public.closed_days
    add constraint FKtrn1jx5vx4bhkv81wnk9d1h2n foreign key (project_id) references public.projects;
alter table if exists public.entries
    add constraint FKibxsps9o4p0p2ibv9ygvhkob9 foreign key (action_id) references public.actions;
alter table if exists public.entries
    add constraint FKoia5s1p9sk4x5fld87yjqpjg9 foreign key (user_id) references public.users;
alter table if exists public.project_roles
    add constraint FKa1xi8mlw6fqpwss0ko4qynki9 foreign key (project_id) references public.projects;
alter table if exists public.project_roles
    add constraint FK9ls1rgoxkmkfchvymyatdbyqd foreign key (role_id) references public.roles;
alter table if exists public.project_roles
    add constraint FKhh9cx7ecbvkaamibehkkusd4k foreign key (user_id) references public.users;
alter table if exists public.roles_permissions
    add constraint FKbx9r9uw77p58gsq4mus0mec0o foreign key (permission_id) references public.permissions;
alter table if exists public.roles_permissions
    add constraint FKqi9odri6c1o81vjox54eedwyh foreign key (role_id) references public.roles;