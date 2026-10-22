# Functional Design

## Navigation

- Main navigation: Movements, Routines.
- Maintainer navigation: Illustration Settings.
- Account menu: Sign In, Sign Out, Account Management.
- Public pages are accessible without authentication.
- Practitioner and maintainer actions require authentication.
- Backend authorization enforces all access restrictions.

## 1. Movement Library

URL: `/movements`

- Data: Movements, including names, tags, and representative illustrations.
- Actions: Search, filter, open Movement Details.
- Maintainer actions: Create Movement.

## 2. Movement Details

URL: `/movements/:key`

- Data: Name, description, illustrations, items grouped by type, tags, and related Movements: outgoing grouped by relationship type, and incoming.
- Actions: View illustrations, open related Movements.
- Practitioner actions: Add Movement to a Routine.
- Maintainer actions: Edit Movement, manage relationships.

## 3. Movement Editor

URLs: `/movements/new`, `/movements/:key/edit`

- Data: Movement name, description, items, tags, and the ordered illustrations.
- Actions: Add, remove, and reorder items of the supported types; add and remove tags; set illustrations; remove all illustrations.
- When creating a Movement, the key is suggested from the name and can be changed.
- Items are added per type, not as free text with a chosen type. Only the supported types are offered.
- Setting illustrations follows the flow "Set Movement Illustrations". Generation progress is shown, and failed generations can be retried.
- Buttons: Save, Cancel.
- Only maintainers can access this page.

## 4. Movement Relationships

URL: `/movements/:key/relationships`

- Data: The Movement's outgoing relationships, each with a target Movement and a type, and the incoming relationships.
- Actions: Add, change, and remove outgoing relationships. The target is chosen with a movement search and the type from the offered types.
- Only maintainers can access this page.

## 5. Illustration Settings

URL: `/settings/illustrations`

Maintainer-only page with two tabs.

### Avatars

- Data: Avatar names.
- Actions: Create, edit, delete Avatars.
- Create and edit open a modal containing the Avatar name and illustration-generation specification.

### Viewpoints

- Data: Viewpoint names and viewing-angle specifications.
- Actions: Create, edit, delete Viewpoints, and choose the default Viewpoint.
- Create and edit open a modal containing the Viewpoint name and specification.

## 6. Routine Library

URL: `/routines`

- Data: The practitioner's Routines, including names and descriptions.
- Actions: Create Routine, open Routine Details, delete Routine.
- Creating a Routine opens the Routine Editor.
- Deleting a Routine requires confirmation.

## 7. Routine Details

URL: `/routines/:id`

- Data: Routine name, description, ordered Sections, and their ordered Movements.
- Links: Movement Details for each Movement.
- Actions: Edit Routine, delete Routine.

## 8. Routine Editor

URLs: `/routines/new`, `/routines/:id/edit`

- Data: Routine name, description, ordered Sections, and ordered Movements.
- Actions: Add, remove, and reorder Sections and Movements; edit Section names and descriptions.
- Adding a Movement opens a searchable Movement picker modal.
- Buttons: Save, Cancel.
- Practitioners can edit only their own Routines.

## 9. Account

- Registration, sign-in and sign-out are handled by the application.
- Actions: Register, Sign In, Sign Out.

## Access Rules

- Public visitors can view the Movement Library and Movement Details.
- Practitioners have public access and can manage their own Routines.
- Maintainers can edit Movements and manage Avatars and Viewpoints.
- Backend authorization enforces these rules independently of frontend navigation.
