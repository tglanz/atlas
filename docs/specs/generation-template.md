# Generation template

How the text sent to the image provider is composed for one IllustrationStage. A provider request has two texts: a system prompt, the rules shared by every image of a Movement, and an image prompt, what to draw in one image.

The templates are Mustache files in `atlas_backend/src/main/resources/generation/`: `system-prompt.mustache` and `image-prompt.mustache`. Edit them there; this page describes what they can reference.

## Model

- `movement.name`, `movement.description`, `movement.cues` (the Movement's cues, in order)
- `avatar.specification`
- `viewpoint.specification`
- `stage.instructions`

Both templates get the same model. A text that is empty or blank counts as absent, so a Mustache section on it (`{{#stage.instructions}}...{{/stage.instructions}}`) renders only when there is something to show. Nothing is HTML-escaped. Leading and trailing whitespace of each rendered text is stripped.

Adding something to the model, such as a Movement's cues, means extending what the worker gives the renderer.

## Where the text goes

For fal.ai, the system prompt is sent as fal's `system_prompt` and the image prompt as its `prompt`, one provider request per IllustrationStage. Image size, ratio and format are connector settings, not part of the template.
