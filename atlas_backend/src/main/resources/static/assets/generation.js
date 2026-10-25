const scheduled = new WeakMap();
const refreshing = new WeakSet();

function showRefreshError(panel, message) {
  const error = panel.querySelector('[data-generation-refresh-error]');
  error.textContent = message;
  error.hidden = false;
}

function schedulePanels() {
  for (const panel of document.querySelectorAll('[data-generation-panel][data-generation-running="true"]')) {
    if (scheduled.has(panel) || refreshing.has(panel)) continue;
    scheduled.set(panel, window.setTimeout(() => refresh(panel), 10_000));
  }
}

async function refresh(panel, userRequested = false) {
  window.clearTimeout(scheduled.get(panel));
  scheduled.delete(panel);
  if (!panel.isConnected || refreshing.has(panel)) return;
  if (panel.getAttribute('aria-busy') === 'true' || (!userRequested && panel.contains(document.activeElement))) {
    schedulePanels();
    return;
  }
  refreshing.add(panel);
  const revision = panel.dataset.generationRevision ?? '0';
  const isCurrent = () => panel.isConnected && panel.getAttribute('aria-busy') !== 'true'
    && (panel.dataset.generationRevision ?? '0') === revision;
  try {
    const response = await fetch(panel.dataset.generationStatusUrl, {
      headers: { 'X-Fragment': 'true', Accept: 'text/html' },
      credentials: 'same-origin',
      cache: 'no-store',
    });
    if (!isCurrent()) return;
    if (response.redirected || response.status === 401 || response.status === 403) {
      panel.dataset.generationRunning = 'false';
      showRefreshError(panel, 'Your session has expired. Sign in again to continue.');
      return;
    }
    if (!response.ok) throw new Error('Generation status could not be refreshed. Retrying shortly.');
    const html = new DOMParser().parseFromString(await response.text(), 'text/html');
    const replacement = html.querySelector('[data-generation-panel]');
    if (!replacement) throw new Error('Generation status could not be refreshed. Retrying shortly.');
    // Only this panel is replaced; the movement editor retains its unsaved fields.
    if (isCurrent() && (userRequested || !panel.contains(document.activeElement))) panel.replaceWith(replacement);
  } catch {
    if (isCurrent()) showRefreshError(panel, 'Generation status could not be refreshed. Retrying shortly.');
  } finally {
    refreshing.delete(panel);
    schedulePanels();
  }
}

document.addEventListener('atlas:fragment', schedulePanels);
document.addEventListener('submit', (event) => {
  const panel = event.target.closest('[data-generation-panel]');
  if (panel) panel.dataset.generationRevision = String(Number(panel.dataset.generationRevision ?? '0') + 1);
}, true);
document.addEventListener('click', (event) => {
  const link = event.target.closest('[data-generation-refresh]');
  if (!link || event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
  event.preventDefault();
  refresh(link.closest('[data-generation-panel]'), true);
});
schedulePanels();
