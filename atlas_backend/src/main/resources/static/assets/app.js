const navToggle = document.querySelector('.nav-toggle');
const navigation = document.getElementById('site-navigation');
if (navToggle && navigation) {
  const mobile = window.matchMedia('(max-width: 40rem)');
  const setExpanded = (expanded) => {
    navToggle.setAttribute('aria-expanded', String(expanded));
    navToggle.setAttribute('aria-label', expanded ? 'Close navigation' : 'Open navigation');
    navigation.dataset.collapsed = String(!expanded);
  };
  navToggle.hidden = false;
  setExpanded(false);
  navToggle.addEventListener('click', () => setExpanded(navToggle.getAttribute('aria-expanded') !== 'true'));
  mobile.addEventListener('change', () => setExpanded(false));
  navToggle.closest('nav').addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && mobile.matches) { setExpanded(false); navToggle.focus(); }
  });
}

document.addEventListener('submit', async (event) => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;
  const confirmation = form.dataset.confirm;
  if (confirmation && !window.confirm(confirmation)) { event.preventDefault(); return; }
  const submitter = event.submitter;
  const markPending = () => {
    form.setAttribute('aria-busy', 'true');
    if (submitter) { submitter.disabled = true; submitter.classList.add('is-pending'); submitter.dataset.pendingButton = ''; }
  };
  if (!form.matches('[data-fragment-form]')) {
    if (form.method.toLowerCase() === 'post' && !event.defaultPrevented) markPending();
    return;
  }
  const container = form.closest('[data-fragment-container]');
  if (!container) return;
  event.preventDefault();
  if (container.getAttribute('aria-busy') === 'true') return;
  container.setAttribute('aria-busy', 'true');
  markPending();
  try {
    const body = new FormData(form);
    if (submitter?.name) body.append(submitter.name, submitter.value);
    const response = await fetch(form.action, { method: form.method, body,
      credentials: 'same-origin', headers: { 'X-Fragment': 'true', Accept: 'text/html' } });
    if (response.redirected) { window.location.assign(response.url); return; }
    if (![200, 400, 409].includes(response.status)) throw new Error('The action could not be completed. Please try again.');
    const html = await response.text();
    const template = document.createElement('template');
    template.innerHTML = html;
    const replacement = template.content.querySelector('[data-fragment-container]');
    if (!replacement) throw new Error('The action could not be completed. Please reload the page.');
    container.replaceWith(replacement);
    replacement.querySelector('[role=alert]')?.focus();
    document.dispatchEvent(new CustomEvent('atlas:fragment'));
  } catch (error) {
    let alert = container.querySelector('[data-request-error]');
    if (!alert) { alert = document.createElement('p'); alert.className = 'alert error';
      alert.dataset.requestError = ''; alert.setAttribute('role', 'alert'); container.prepend(alert); }
    alert.textContent = error.message;
  } finally {
    container.removeAttribute('aria-busy');
    form.removeAttribute('aria-busy');
    if (submitter) { submitter.disabled = false; submitter.classList.remove('is-pending'); delete submitter.dataset.pendingButton; }
  }
});

window.addEventListener('pageshow', () => {
  document.querySelectorAll('form[aria-busy]').forEach(form => form.removeAttribute('aria-busy'));
  document.querySelectorAll('[data-pending-button]').forEach(button => {
    button.disabled = false; button.classList.remove('is-pending'); delete button.dataset.pendingButton;
  });
});
