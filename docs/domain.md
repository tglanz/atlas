# User

- **Email**: Unique, case-insensitive.
- **Role**: Practitioner or Maintainer. The first registered user is a Maintainer.
- **Preferred Avatar**: The Avatar whose illustrations the user wants to see. Optional.

# Movement

- **Key**: Stable identifier.
- **Revision**: Revision number.
- **Status**: Draft or Published.
- **Published at**: Publication time, present only when Published.
- **Name**: String.
- **Description**: Text.
- **Illustrations**: Mapping from Avatar to an ordered collection of Illustrations.
- **Cues**: Ordered collection of performance instructions.
- **Tags**: Set of keywords.
- **Links**: Collection of directed links, each containing a target Movement Key and relationship type.

# Illustration

An Illustration is a picture of a Movement. A Movement's Illustrations for an Avatar are ordered, for example the bottom and top phases of a push-up.

## Avatar

A kind of figure the user can choose to see, such as a sketch or a cartoon figure.

- **Name**: Unique string.
- **Specification**: Text used to generate Illustrations with this Avatar's look.
- **Default**: Exactly one Avatar is the default. Defining an Avatar does not generate Illustrations.

A user sees the Illustrations of their preferred Avatar. Visitors without a preference, and Movements with no Illustrations for the preferred Avatar, use the default Avatar.

## Viewpoint

A predefined way to specify the viewing angle when generating Illustrations. It is not stored on an Illustration.

- **Name**: Unique string.
- **Specification**: Text describing the viewing angle.

# Routine

- **Owner**: The user who created it.
- **Name**: String.
- **Description**: Text.
- **Sections**: Ordered collection of Sections.

# Section

- **Name**: String.
- **Description**: Text.
- **Movements**: Ordered collection of references to Movement revisions.
