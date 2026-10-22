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

- Data: Current published Movements, including names, tags, and representative illustrations.
- Actions: Search, filter, open Movement Details.
- Maintainer actions: Create Movement.

## 2. Movement Details

URL: `/movements/:key`

- Data: Current published revision's name, description, illustrations, cues, tags, and linked Movements.
- Actions: View illustrations, open linked Movements.
- Practitioner actions: Add Movement to a Routine.
- Maintainer actions: Edit draft, create revision, view Revision History.

## 3. Movement Editor

URLs: `/movements/new`, `/movements/:key/edit`

- Data: Movement name, description, cues, tags, links, and the ordered illustrations.
- Actions: Add and remove tags, manage links, set illustrations, remove all illustrations.
- Setting illustrations follows the flow "Set Movement Illustrations". Generation progress is shown, and failed generations can be retried.
- Buttons: Save Draft, Publish, Cancel.
- Only maintainers can access this page.
- Creating a revision from a published Movement creates a new draft with the same key and the next revision number.

## 4. Movement Revision History

URL: `/movements/:key/revisions`

- Data: Published revision numbers and publication information.
- Links: Open a historical published revision.
- Maintainer actions: View drafts and create a new draft from the latest published revision.
- Practitioners and public visitors see published revisions only.

## 5. Historical Movement Details

URL: `/movements/:key/revisions/:revision`

- Data: Selected published revision's name, description, illustrations, cues, tags, and links.
- Actions: Navigate to linked Movements or return to Revision History.
- Historical revisions are read-only.
- Maintainers can open drafts in the Movement Editor.

## 6. Illustration Settings

URL: `/settings/illustrations`

Maintainer-only page with two tabs.

### Avatars

- Data: Avatar names.
- Actions: Create, edit, delete Avatars.
- Create and edit open a modal containing the Avatar name and illustration-generation specification.

### Viewpoints

- Data: Viewpoint names and viewing-angle specifications.
- Actions: Create, edit, delete Viewpoints.
- Create and edit open a modal containing the Viewpoint name and specification.

## 7. Routine Library

URL: `/routines`

- Data: The practitioner's Routines, including names and descriptions.
- Actions: Create Routine, open Routine Details, delete Routine.
- Creating a Routine opens the Routine Editor.
- Deleting a Routine requires confirmation.

## 8. Routine Details

URL: `/routines/:id`

- Data: Routine name, description, ordered Sections, and their ordered Movements.
- Links: Movement Details for each referenced revision.
- Actions: Edit Routine, delete Routine.
- Displays the exact published Movement revisions referenced by the Routine.

## 9. Routine Editor

URLs: `/routines/new`, `/routines/:id/edit`

- Data: Routine name, description, ordered Sections, and ordered Movement references.
- Actions: Add, remove, and reorder Sections and Movements; edit Section names and descriptions.
- Adding a Movement opens a searchable Movement picker modal.
- Selecting a Movement adds a reference to its current published revision.
- Buttons: Save, Cancel.
- Practitioners can edit only their own Routines.

## 10. Account

- Registration, sign-in and sign-out are handled by the application.
- Actions: Register, Sign In, Sign Out.

## Access and Revision Rules

- Public visitors can view the Movement Library, current published Movement Details, and published Revision History.
- Practitioners have public access and can manage their own Routines.
- Maintainers can manage Movement drafts, publish revisions, and manage Avatars and Viewpoints.
- Drafts are visible only to maintainers.
- Publishing a draft makes it the current published revision.
- Previously published revisions remain available and unchanged.
- Linked Movements resolve to their current published revisions.
- Routines retain references to the exact published revisions selected when the Movements were added.
- Backend authorization enforces these rules independently of frontend navigation.
