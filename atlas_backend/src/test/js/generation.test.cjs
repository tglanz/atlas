const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const vm = require('node:vm');
const source = readFileSync(require('node:path').join(__dirname, '../../main/resources/static/assets/generation.js'), 'utf8');

function studio(initial = 'running') {
  const listeners = new Map();
  const timers = new Map();
  const buttons = [{ disabled: false }, { disabled: false }];
  const input = { value: 'Unsaved custom guidelines' };
  const hint = { textContent: '' };
  const requests = [];
  let nextTimer = 0;
  let panel;
  function makePanel(state) {
    return {
      isConnected: true,
      error: { textContent: '', hidden: true },
      dataset: { generationRunning: String(state === 'running'), generationStatusUrl: '/illustrations/squats/status' },
      querySelector() { return this.error; },
      replaceWith(replacement) { this.isConnected = false; panel = replacement; },
    };
  }
  panel = makePanel(initial);
  const document = {
    visibilityState: 'visible',
    querySelector(selector) { return selector === '[data-generation-panel]' ? panel : hint; },
    querySelectorAll() { return buttons; },
    addEventListener(name, fn) { if (!listeners.has(name)) listeners.set(name, []); listeners.get(name).push(fn); },
    dispatchEvent(event) { for (const fn of listeners.get(event.type) || []) fn(event); },
  };
  const environment = {
    document,
    window: { setTimeout(fn) { const id = ++nextTimer; timers.set(id, fn); return id; }, clearTimeout(id) { timers.delete(id); } },
    DOMParser: class { parseFromString(state) { return { querySelector() { return makePanel(state); } }; } },
    CustomEvent: class { constructor(type) { this.type = type; } },
    FormData: class { constructor(form) { this.values = new Map(Object.entries(form.values)); } append(name, value) { this.values.set(name, value); } get(name) { return this.values.get(name); } },
    fetch: async (url, options) => { requests.push({ url, ...options }); return environment.answer(url, options); },
    answer: async () => ({ ok: true, status: 200, redirected: false, text: async () => 'ready' }),
  };
  vm.runInNewContext(source, environment);
  return {
    document, buttons, input, timers, requests, environment,
    panel: () => panel,
    async tick() { assert.equal(timers.size, 1); const [id, fn] = timers.entries().next().value; timers.delete(id); await fn(); },
    async submit() {
      const form = { action: '/illustrations/squats/generations', values: { guidelines: input.value, name: 'Live draft' },
        matches: () => true, setAttribute() {}, removeAttribute() {} };
      for (const fn of listeners.get('submit')) await fn({ target: form, preventDefault() {}, submitter: { name: 'stagePosition', value: '1' } });
    },
  };
}

test('polls automatically, preserves the editable draft, and stops when ready', async () => {
  const page = studio();
  assert.equal(page.buttons[0].disabled, true);
  await page.tick();
  assert.equal(page.requests.length, 1);
  assert.equal(page.requests[0].url, '/illustrations/squats/status');
  assert.equal(page.input.value, 'Unsaved custom guidelines');
  assert.equal(page.panel().dataset.generationRunning, 'false');
  assert.equal(page.timers.size, 0);
  assert.equal(page.buttons[0].disabled, false);
});

test('a temporary server failure retries without losing inputs', async () => {
  const page = studio();
  page.environment.answer = async () => ({ ok: false, status: 503 });
  await page.tick();
  assert.equal(page.panel().error.hidden, false);
  assert.equal(page.timers.size, 1);
  assert.equal(page.input.value, 'Unsaved custom guidelines');
  page.environment.answer = async () => ({ ok: true, status: 200, text: async () => 'failed' });
  await page.tick();
  assert.equal(page.timers.size, 0);
});

test('pauses network polling while hidden and resumes on return', async () => {
  const page = studio();
  page.document.visibilityState = 'hidden';
  await page.tick();
  assert.equal(page.requests.length, 0);
  page.document.visibilityState = 'visible';
  page.document.dispatchEvent({ type: 'visibilitychange' });
  await page.tick();
  assert.equal(page.requests.length, 1);
});

test('submits live inputs and selected stage, while a stale poll cannot replace the new run', async () => {
  const page = studio();
  let completePoll;
  page.environment.answer = () => new Promise(resolve => { completePoll = resolve; });
  const oldPoll = page.tick();
  // A review action or another completed refresh can finish the previous run while its poll is pending.
  page.panel().dataset.generationRunning = 'false';
  page.environment.answer = async () => ({ ok: true, status: 200, text: async () => 'running' });
  page.input.value = 'Edited right before submit';
  await page.submit();
  const request = page.requests[1];
  assert.equal(request.method, 'POST');
  assert.equal(request.body.get('guidelines'), 'Edited right before submit');
  assert.equal(request.body.get('stagePosition'), '1');
  completePoll({ ok: true, status: 200, text: async () => 'ready' });
  await oldPoll;
  assert.equal(page.panel().dataset.generationRunning, 'true');
  assert.equal(page.input.value, 'Edited right before submit');
  assert.equal(page.timers.size, 1);
});
