Image generation is integrated behind a pluggable connector, with fal.ai as the first provider and a stub. The design is in `docs/rfcs/image-generation.md`, and how fal.ai behaves is in `docs/specs/falai.md`.

What remains is the RFC's later work:

- Retrying a failed generation, and transient provider errors.
- Copying approved images into our own storage, so they no longer depend on the provider.
- Reference images for generation.
- Setting a Movement's Illustrations from the maintainer's own images.
- Validating the fal.ai connector against the real provider, including the failure path.
