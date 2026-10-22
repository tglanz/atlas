# User

- **Email**: Unique, case-insensitive.
- **Role**: Practitioner or Maintainer. The first registered user is a Maintainer.

# Movement

- **Key**: Stable identifier.
- **Revision**: Revision number.
- **Status**: Draft or Published.
- **Published at**: Publication time, present only when Published.
- **Name**: String.
- **Description**: Text.
- **Illustrations**: Ordered collection of Illustrations.
- **Cues**: Ordered collection of performance instructions.
- **Tags**: Set of keywords.
- **Links**: Collection of directed links, each containing a target Movement Key and relationship type.

# Illustration

An Illustration is a picture of a Movement. A Movement revision has an ordered sequence of Illustrations, for example the bottom and top phases of a push-up. Illustrations are part of the revision and are frozen once it is published.

All pictures are stored in S3.

## Illustrations Generation Spec

Describes how a Movement draft's whole Illustration sequence is produced.

- **Stages**: Ordered collection of Stage Specs. One Illustration results per Stage Spec, in order.

A Stage Spec is one of:

- **Avatar Image Spec**: a picture generated from an Avatar, a Viewpoint, and Instructions, a reference Image, or both.
  - **Avatar**
  - **Viewpoint**
  - **Instructions**: Free text. Optional.
  - **Reference image**: Optional.
- **Static Image Spec**: an image used as is.
  - **Image**

The model does not require every Stage Spec of a sequence to be the same kind. The user interface offers one pattern per Movement: all stages generated with an Avatar, or all stages static images.

## Generation Request

A request to produce a Movement draft's Illustration sequence from an Illustrations Generation Spec.

- **Draft**: The Movement draft it applies to.
- **Spec**: The Illustrations Generation Spec.
- **Status**: Pending, Running, Succeeded or Failed. A failed request records the reason and can be retried.
- **Results**: The produced Illustrations, in stage order. A successful request replaces the draft's sequence.

## Avatar

The look that generated Illustrations should have, such as a sketch or a cartoon figure. Avatars are used only to generate Illustrations; viewers do not see or choose them.

- **Name**: Unique string.
- **Specification**: Text used to generate Illustrations with this look.

## Viewpoint

A predefined way to specify the viewing angle when generating an Illustration. It is not stored on an Illustration.

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
