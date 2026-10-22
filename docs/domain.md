# User

- **Email**: Unique, case-insensitive.
- **Role**: Practitioner or Maintainer.

# Movement

- **Key**: Stable identifier.
- **Name**: String.
- **Description**: Text.
- **Cues**: Ordered collection of performance instructions.
- **Tags**: Set of keywords.
- **Links**: Collection of directed links, each containing a target Movement Key and a relationship type.
- **Illustrations**: Ordered collection of Illustrations.

Movements are edited in place; see `technicalities.md`.

# Illustration

A picture of a Movement. A Movement's Illustrations are ordered, for example the bottom and top phases of a push-up.

## Illustrations Generation Spec

Describes how a Movement's whole Illustration sequence is produced. It is an ordered collection of Stage Specs, and one Illustration results per Stage Spec, in order.

A Stage Spec is one of:

- **Avatar Image Spec**: a picture generated from an Avatar and a Viewpoint, plus Instructions, a reference Image, or both.
- **Static Image Spec**: an image used as is.

The model does not require the Stage Specs of a sequence to be of the same kind. The user interface offers one pattern per Movement, either all generated or all static, so the rules can change without changing the data.

## Generation Request

A request to produce a Movement's Illustration sequence from a spec. It moves from pending to running to succeeded or failed. A failed request records why and can be retried. A successful request replaces the Movement's whole sequence.

## Avatar

The look that generated Illustrations should have, such as a sketch or a cartoon figure. It has a unique name and a specification used to generate with that look. Avatars are only inputs to generation; viewers do not see or choose them.

## Viewpoint

A predefined way to specify the viewing angle when generating an Illustration. It has a unique name and a specification. It is not stored on an Illustration.

# Routine

- **Owner**: The user who created it.
- **Name**: String.
- **Description**: Text.
- **Sections**: Ordered collection of Sections.

# Section

- **Name**: String.
- **Description**: Text.
- **Movements**: Ordered collection of references to Movements.
