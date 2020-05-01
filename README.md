# TimeTracker

## Summary
### Technologies
* Java 11
* Vaadin 14
* Spring Boot 2.X
    * Vaadin
    * Data
    * Test
    * OAuth2 Client
* JUnit 5 + Mockito
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
`view_project_info` | Content from cell 2
`edit_my_logs` | Content from cell 2
`view_project_logs` | Content from cell 2
`edit_project_info` | Content from cell 2
`edit_project_actions` | Content from cell 2
`edit_project_users` | Content from cell 2

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
`project_admin` | `view_project_info`, `edit_my_logs`, `view_project_logs`, `edit_project_info`, `edit_project_actions`, `edit_project_users`
### Dates Management
{PIC dates}

Users with the `view_project_logs` permission can:
* оpen or close (prohibits the creation of new work logs) the day
* open or close (prohibits the updating and removing) work logs for specific dates interval. Logs related to inactive actions and projects will not be open, however.
### Daily Work Logs
Users can create work logs if the day is open. The working log can be changed or deleted at any time until it is closed.
{PIC daily page}
### Reports
The beginning of the week is determined based on the user's settings.
### Other
#### Current User
{PIC current user page} 
#### Projects
{PIC projects page} 
#### Users
{PIC users page} 