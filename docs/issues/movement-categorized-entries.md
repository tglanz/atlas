Currently, from a model perspective, a movement has Cues that is a string array.

Id like to generalize to someting like

Movement {
    ...
    items: MovementItem[]
    ...
}

MovementItem {
    type: one of {
        Cue, Comment, ...
    }
    content: string
}

the UI will take all MovementItems and group them according to type, i.e.

Cues:

1. Shoulder elevated
2. Keep hollowbody

Comments:

1. If you feel tension on the wrist, turn hands outside a bit

...


And in the edit, its not a free text box, and not generic types. The UI enforces addition of specific types (Keep only Cues and Comments for now).

The goal is that the model is flexible, and the usage pattern is enforced by the Usage (UI).