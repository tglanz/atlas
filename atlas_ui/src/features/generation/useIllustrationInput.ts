import { useState } from 'react';
import { useAvatars } from '@/features/avatars';
import type { GenerationForm, StageInput } from './queries';
import { useViewpoints } from './queries';

export type Mode = 'avatar' | 'static';

export const emptyStage = (): StageInput => ({ instructions: '', file: null });

/**
 * The state of the "set illustrations" form. The data model allows any mix of stages; the UI offers
 * one pattern per movement, all generated with an avatar or all static images. Nothing is sent from
 * here: the owner of the form submits `form` when it saves.
 */
export function useIllustrationInput() {
  const avatars = useAvatars();
  const viewpoints = useViewpoints();
  const [mode, setModeState] = useState<Mode>('avatar');
  const [avatarId, setAvatarId] = useState<string | null>(null);
  const [viewpointId, setViewpointId] = useState<string | null>(null);
  const [stages, setStages] = useState<StageInput[]>([emptyStage()]);

  // Preselect the default viewpoint; with a single avatar or viewpoint there is nothing to choose.
  const onlyId = (items: { id: string }[] | undefined) =>
    items?.length === 1 ? items[0].id : null;
  const chosenAvatarId = avatarId ?? onlyId(avatars.data);
  const chosenViewpointId =
    viewpointId ??
    viewpoints.data?.find((v) => v.isDefault)?.id ??
    onlyId(viewpoints.data);

  const hasInput = (s: StageInput) =>
    mode === 'static'
      ? s.file !== null
      : Boolean(s.instructions.trim() || s.file);
  const touched = stages.some((s) => s.instructions.trim() || s.file);
  const complete =
    stages.every(hasInput) &&
    (mode === 'static' || Boolean(chosenAvatarId && chosenViewpointId));

  let form: GenerationForm | null = null;
  if (touched && complete) {
    form =
      mode === 'static'
        ? { mode, stages }
        : {
            mode,
            avatarId: chosenAvatarId as string,
            viewpointId: chosenViewpointId as string,
            stages,
          };
  }

  return {
    avatars,
    viewpoints,
    mode,
    setMode: (next: Mode) => {
      setModeState(next);
      setStages([emptyStage()]);
    },
    chosenAvatarId,
    setAvatarId,
    chosenViewpointId,
    setViewpointId,
    stages,
    setStages,
    /** What to submit, or null when nothing was entered or it is incomplete. */
    form,
    /** Why the entered stages cannot be submitted, or null. */
    problem:
      touched && !complete
        ? 'Complete every illustration stage, or clear them.'
        : null,
    reset: () => setStages([emptyStage()]),
  };
}

export type IllustrationInputState = ReturnType<typeof useIllustrationInput>;
