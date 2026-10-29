let timer;
let polling = false;
let submitting = false;
let revision = 0;
const panelSelector = '[data-generation-panel]';
const currentPanel = () => document.querySelector(panelSelector);

function showError(panel, message) {
  const error = panel?.querySelector('[data-generation-refresh-error]');
  if (error) { error.textContent = message; error.hidden = false; }
}

function schedule() {
  window.clearTimeout(timer);
  const running = currentPanel()?.dataset.generationRunning === 'true';
  document.querySelectorAll('[data-generate-button]').forEach(button => { button.disabled = submitting || running; });
  const hint = document.querySelector('[data-generation-form-status]');
  if (hint) hint.textContent = running ? 'Generation is running. You can keep editing inputs for the next run.' : 'Results appear below for review before they are applied.';
  if (running && !polling && !submitting) timer = window.setTimeout(poll, 3000);
}

function replacePanel(responseHtml, expected) {
  const html = new DOMParser().parseFromString(responseHtml, 'text/html');
  const replacement = html.querySelector(panelSelector);
  if (!replacement) throw new Error('Could not load generation status.');
  if (expected.isConnected) expected.replaceWith(replacement);
  document.dispatchEvent(new CustomEvent('atlas:fragment'));
}

async function poll() {
  const panel = currentPanel();
  if (!panel || polling || submitting) return;
  if (document.visibilityState === 'hidden') { schedule(); return; }
  polling = true;
  const startedAt = revision;
  try {
    const response = await fetch(panel.dataset.generationStatusUrl, {
      headers: { 'X-Fragment': 'true', Accept: 'text/html' }, credentials: 'same-origin', cache: 'no-store',
    });
    if (!panel.isConnected || revision !== startedAt) return;
    if (response.redirected || response.status === 401 || response.status === 403) {
      panel.dataset.generationRunning = 'false';
      showError(panel, 'Your session has expired. Sign in again to continue.');
      return;
    }
    if (!response.ok) throw new Error('Could not refresh generation status. Retrying automatically.');
    const body = await response.text();
    if (panel.isConnected && revision === startedAt) replacePanel(body, panel);
  } catch {
    if (panel.isConnected && revision === startedAt) showError(panel, 'Could not refresh generation status. Retrying automatically.');
  } finally {
    polling = false;
    schedule();
  }
}

document.addEventListener('submit', async event => {
  const form = event.target;
  if (!form.matches('[data-generation-form]')) {
    if (form.closest(panelSelector)) revision += 1;
    return;
  }
  event.preventDefault();
  if (submitting || currentPanel()?.dataset.generationRunning === 'true') return;
  const panel = currentPanel();
  const body = new FormData(form);
  if (event.submitter?.name) body.append(event.submitter.name, event.submitter.value);
  submitting = true;
  revision += 1;
  form.setAttribute('aria-busy', 'true');
  schedule();
  try {
    const response = await fetch(form.action, { method: 'POST', body, credentials: 'same-origin',
      headers: { 'X-Fragment': 'true', Accept: 'text/html' } });
    if (response.redirected || response.status === 401 || response.status === 403) {
      throw new Error('Your session has expired. Sign in again to continue.');
    }
    if (![200, 400, 409].includes(response.status)) throw new Error('Could not start generation. Please try again.');
    // The form stays intact: only results are replaced, including during subsequent polling.
    replacePanel(await response.text(), panel);
  } catch (error) {
    showError(currentPanel(), error.message);
  } finally {
    submitting = false;
    form.removeAttribute('aria-busy');
    schedule();
  }
}, true);

document.addEventListener('atlas:fragment', schedule);
document.addEventListener('visibilitychange', () => { if (document.visibilityState === 'visible') schedule(); });
schedule();
