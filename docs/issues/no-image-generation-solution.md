currently we don't have an image generation solution.
we should find the cheapest one we could and integrate with it.

integration should be pluggable; so that if we have different generation connectors, we can switch.

The design is in `docs/rfcs/image-generation.md`, and how fal.ai behaves is in `docs/specs/falai.md`.

## Context

- Generation sits behind a port and is currently a stub that returns a placeholder. Image storage is also a stub (S3 is the plan).
- A stage to generate has the Avatar's specification, the Viewpoint's specification, and optionally instructions and a reference image. One image is produced per stage.
- Volume is small: a few images per movement, generated once and stored. The cost that matters is the number of attempts needed and keeping the figure consistent across the stages of one movement, more than the price of a single picture.

## Direction

- One connector per provider, selected by configuration, with the stub kept as one of them.
- Reference images are P1 in the RFC.
- Provider keys come from the environment, never source control.
- Generated images stay at the provider's URLs at first. Our own storage is P1 in the RFC.
