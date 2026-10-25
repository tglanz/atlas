# Functional Design

## Navigation

- Main navigation: Movements, Routines.
- Movements is a section. Every Movement-related page lives under it, with the Movement Index as its landing page, so a future page such as a graph view belongs in the Movements section.
- Maintainer navigation: Illustrations, shown only to maintainers.
- Signed-in users have Sign Out in the main navigation. Visitors have Sign In and Register buttons.
- Public pages are accessible without authentication.
- Practitioner and maintainer actions require authentication.
- Backend authorization enforces all access restrictions.

## 1. Movement Index

URL: `/movements`

The landing page of the Movements section, for finding Movements.

- Data: Movements as cards with an image, name and tags, and the tags in use with how many Movements have each.
- Actions: Search by name, with name suggestions while typing. Filter by toggling tags in a panel; a Movement must have all of the selected tags. Open Movement Details. Clear the filters.
- The search and the selected tags are part of the address, so a search can be shared.
- Maintainer actions: Create Movement.

## 2. Movement Details

URL: `/movements/:key`

- Data: Name, description, illustrations, items grouped by type, tags, and related Movements, one section per relationship type, such as Progressions.
- Actions: View illustrations, open related Movements.
- Practitioner actions: Add Movement to a Routine.
- Maintainer actions: Edit Movement.

## 3. Movement Editor

URLs: `/movements/new`, `/movements/:key/edit`

- Data: Movement name, description, items, stages, tags, outgoing relationships, and the illustrations.
- Actions: Add, remove, and reorder items of the supported types and stages; add and remove tags; manage outgoing relationships; generate illustrations; remove all illustrations.
- A relationship's target is chosen with a movement search and its type from the offered types. Targets must be existing Movements.
- When creating a Movement, the key is suggested from the name and can be changed.
- Items are added per type, not as free text with a chosen type. Only the supported types are offered.
- Generating illustrations follows the flow "Set Movement Illustrations". Progress is shown for each stage, and the result is reviewed before it replaces the illustrations.
- Details, stages, tags and relationships are available when creating a new Movement as well as when editing one, and a single Save applies them all. Generating illustrations needs a saved Movement, so a new Movement shows a hint instead.
- Buttons: Save, Cancel.
- Only maintainers can access this page.

## 4. Illustrations

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

## 5. Routine Library

URL: `/routines`

- Data: The practitioner's Routines, including names and descriptions.
- Actions: Create Routine, open Routine Details, delete Routine.
- Creating a Routine opens the Routine Editor.
- Deleting a Routine requires confirmation.

## 6. Routine Details

URL: `/routines/:id`

- Data: Routine name, description, ordered Sections, and their ordered Movements.
- Links: Movement Details for each Movement.
- Actions: Edit Routine, delete Routine.

## 7. Routine Editor

URLs: `/routines/new`, `/routines/:id/edit`

- Data: Routine name, description, ordered Sections, and ordered Movements.
- Actions: Add, remove, and reorder Sections and Movements; edit Section names and descriptions.
- Adding a Movement opens a searchable Movement picker modal.
- Buttons: Save, Cancel.
- Practitioners can edit only their own Routines.

## 8. Account

- Registration, sign-in and sign-out are handled by the application.
- Actions: Register, Sign In, Sign Out.

## Access Rules

- Public visitors can view the Movement Index and Movement Details.
- Practitioners have public access and can manage their own Routines.
- Maintainers can edit Movements and manage Avatars and Viewpoints.
- Backend authorization enforces these rules independently of frontend navigation.
