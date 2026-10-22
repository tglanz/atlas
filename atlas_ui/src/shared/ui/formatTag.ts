/** Tags are stored in lowercase; this is how they are shown. */
export const formatTag = (tag: string) =>
  tag.charAt(0).toUpperCase() + tag.slice(1);
