# TimeTracker
{PIC about}

## Summary
The application is a simple time tracker.
All users can create projects, bind other users with specific roles to them.
Users with the required permissions can create, modify and delete work logs in projects, view other people's logs, commit them to prohibit further changes.

### Technologies
The goal was to try to create a project from scratch using Vaadin 14+ and Spring Security, Spring Data.

* Java 11
* Vaadin 14 (For all UI stuff)
* Spring Boot 2.X
    * Vaadin
    * Data
    * Test
    * OAuth2 Client (For Google OpenID)
* JUnit 5 + Mockito (Tests cover only complex database manipulations in `com.github.kshashov.timetracker.data.service` package.

### Database scheme

## Pages
### Projects Management

The central entity here is the project. Project containы several actions - types of work (development, code review, meeting, etc) and can be used by several users, depending on their roles. 

{PIC my projects page} 

It is a classic master-detail interface with four widgets:
* Projects list
* Project details
* Project actions list
* Project users list

Each 'list' component is a classic Vaadin Grid with crud functionality. Depending on the state of the project, action, as well as the project role of the current user, certain components and buttons may not be displayed .

#### Inactive state
Since the working logs at some point can be commited (aka closed), they should not be changed or deleted in any way. This complicates the process of deleting a project or an action, to which such logs relate - we cannot delete them! 

{PIC message after deactivating} 

In such cases, an action (or a project with all actions) instead of deleting moves into a 'inactive' state. Such inactive entities are only suitable for viewing existing closed work logs and can't be updated in any way.

#### Project roles and permissions

A user attached to a project has a project role, each of which contains a set of permissions.

Permissions:

Permission Code | Description
------------ | -------------
`view_project_info` | Viewing project information
`edit_my_logs` | Creating, editing, deleting by the user of his work logs
`view_project_logs` | Viewing, closing project work logs
`edit_project_logs` | Creating, editing, deleting project work logs (Not implemented)
`edit_project_info` | Editing, deleting project Information
`edit_project_actions` | Creating, editing, deleting actions in the project
`edit_project_users` | Adding a user to the project, changing the user role, exclusion from the project

In general, there are no restrictions on the number and composition of project roles, so you can create any roles in the database. The only limitation is that the following roles must exist:

Role Code | Description
------------ | -------------
`project_inactive` | Used when removing a user from a project when he has closed work logs.
`project_admin` | Used when a user creates a new project.

By default I use the following roles:

Role Code | Permission Codes
------------ | -------------
`project_inactive` | -
`project_user` |  `view_project_info`, `edit_my_logs`
`project_reporter` | `view_project_info`, `edit_my_logs`, `view_project_logs`
`project_admin` | `view_project_info`, `edit_my_logs`, `view_project_logs`, `edit_project_logs`, `edit_project_info`, `edit_project_actions`, `edit_project_users`

### Dates Management

Users with the `view_project_logs` permissions can:
* оpen or close (prohibits the creation of new work logs) the day
* open or close (prohibits the updating and removing) work logs for specific dates interval. Logs related to inactive actions, projects or user roles will not be open, however.

{PIC dates}

The beginning of the week is determined based on the user's settings.
### Daily Work Logs

Users can create work logs if the day is open. The working log can be changed or deleted at any time until it is closed.

{PIC daily page}

### Reports

Users with the `view_project_logs` permissions can view all project's work logs for specified date range. The beginning of the week is determined based on the user's settings.

{PIC reports page}

### Other
#### Current User

{PIC current user page}

#### Projects

{PIC projects page}

#### Users

{PIC users page} 