# Movement

- **Key**: Stable identifier.
- **Revision**: Revision number.
- **Name**: String.
- **Description**: Text.
- **Illustrations**: Mapping from Avatar to ordered Illustration collections.
- **Cues**: Ordered collection of performance instructions.
- **Tags**: Set of keywords.
- **Links**: Collection of directed links, each containing a target Movement Key and relationship type.

# Illustration Generation

## Avatar

Specification for generating the illustrated figure.

## Viewpoint

Specification of the illustration's viewing angle.

# Routine

- **Name**: String.
- **Description**: Text.
- **Sections**: Ordered collection of Sections.

# Section

- **Name**: String.
- **Description**: Text.
- **Movements**: Ordered collection of references to Movement revisions.