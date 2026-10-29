# User

- **Email**: Unique, case-insensitive.
- **Role**: Practitioner or Maintainer.

# Movement

- **Key**: Stable identifier.
- **Name**: String.
- **Description**: Text saying what the movement is generally for. How it relates to other movements belongs in the Movement Graph, not here.
- **Items**: Ordered collection of typed pieces of text, such as cues and comments.
- **Stages**: Ordered collection of Movement Stages.
- **Tags**: Set of keywords.
- **Illustrations**: Ordered collection of Illustrations.

Movements are edited in place; see `technicalities.md`.

# Movement Item

- **Type**: The kind of item, such as Cue or Comment. New types can be added without changing the model.
- **Content**: Text.

The model does not restrict how items are used. The user interface groups items by type for display, and offers only the types it supports when editing.

# Movement Stage

- **Name**: String.
- **Description**: Text saying what the movement looks like at this stage, as a position of the body. Generation uses it to draw the stage, so it describes what can be seen.

A Movement Stage is a position or phase worth showing. One Illustration is produced for each stage.

# Movement Graph

The Movements and the relationships between them. It exists outside the Movements themselves, so relating Movements never changes a Movement.

An **Edge** is a directed relationship from a source Movement to a target Movement, with a type such as progression or variant. The type is free text, normalized to lowercase. The system knows a list of types to offer, but an Edge may carry any type.

- Both ends of an Edge must be existing Movements.
- A Movement cannot be related to itself.
- Some types have an inverse: adding an Edge of the type also adds the inverse Edge in the other direction, and removing one removes both. A progression from X to Y comes with a regression from Y to X, and a variant is its own inverse. The system knows which types have inverses.

The graph can be traversed: from a start Movement, follow Edges outgoing, incoming, or both, optionally only of given types and up to a given number of steps, to find the Movements reachable from it.

# Illustration

An image created for a Movement and shown on it. A Movement's Illustrations are ordered, for example the bottom and top phases of a push-up. A generated Illustration retains its stage association even when other stages have no image.

## Illustrations Generation Spec

Describes how a Movement's whole Illustration sequence is produced. It is an ordered collection of Stage Specs, and one Illustration results per Stage Spec, in order.

A Stage Spec is one of:

- **Avatar Image Spec**: an image generated from an Avatar and a Viewpoint, plus Instructions, a reference Image, or both.
- **Static Image Spec**: an image used as is.

The model does not require the Stage Specs of a sequence to be of the same kind. The user interface offers one pattern per Movement, either all generated or all static, so the rules can change without changing the data.

## Generation Request

A request captures the Movement inputs, additional guidelines and preset specifications used for generation. It targets all stages or a specific stage. Temporary input edits belong to the request and do not change the saved Movement. Candidates require approval before becoming Illustrations. Approval replaces the whole sequence or only the targeted stage; discarding preserves existing Illustrations. A failed request records why and can be retried.

## Avatar

The look that generated Illustrations should have, such as a sketch or a cartoon figure. It has a unique name and a specification used to generate with that look. Avatars are only inputs to generation; viewers do not see or choose them.

## Viewpoint

A predefined way to specify the viewing angle when generating an Illustration. It has a unique name and a specification. It is not stored on an Illustration. At most one Viewpoint is the default, which the user interface preselects when generating.

# Routine

- **Owner**: The user who created it.
- **Name**: String.
- **Description**: Text.
- **Sections**: Ordered collection of Sections.

A Routine is private by default. Its owner can enable a public link that lets anyone view its current content without signing in. The link grants no editing rights and does not place the Routine in a public listing. Disabling sharing invalidates the link; enabling it again creates a new link.

# Section

- **Name**: String.
- **Description**: Text.
- **Movements**: Ordered collection of references to Movements.
