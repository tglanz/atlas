const searchForm = document.querySelector('[data-movement-search]');

if (searchForm) {
  let pending;
  let timer;
  let revision = 0;
  const error = document.querySelector('[data-search-error]');

  const update = async () => {
    const current = ++revision;
    pending?.abort();
    pending = new AbortController();
    const values = new FormData(searchForm);
    const url = new URL(searchForm.action);
    const text = String(values.get('text') || '').trim();
    if (text) url.searchParams.set('text', text);
    [...new Set(values.getAll('tags'))].sort().forEach(tag => url.searchParams.append('tags', tag));
    const oldResults = document.getElementById('movement-results');
    oldResults?.setAttribute('aria-busy', 'true');
    error.hidden = true;
    try {
      const response = await fetch(url, {
        headers: { 'X-Atlas-Fragment': 'movements' },
        signal: pending.signal,
      });
      if (!response.ok) throw new Error('Search failed');
      const html = await response.text();
      if (current !== revision) return;
      const parsed = new DOMParser().parseFromString(html, 'text/html');
      const results = parsed.getElementById('movement-results');
      if (!results) throw new Error('Search results missing');
      oldResults.replaceWith(results);
      history.replaceState(null, '', url);
      const canonical = document.querySelector('link[rel="canonical"]');
      if (canonical) {
        const trusted = new URL(canonical.href);
        trusted.pathname = url.pathname;
        trusted.search = url.search;
        canonical.href = trusted.href;
      }
      const robots = document.querySelector('meta[name="robots"]');
      if (robots) robots.content = url.search ? 'noindex,follow' : 'index,follow';
      document.dispatchEvent(new CustomEvent('atlas:fragment'));
    } catch (failure) {
      if (failure.name !== 'AbortError' && current === revision) error.hidden = false;
    } finally {
      if (current === revision) document.getElementById('movement-results')?.removeAttribute('aria-busy');
    }
  };

  searchForm.addEventListener('input', event => {
    if (event.target.name !== 'text') return;
    clearTimeout(timer);
    timer = setTimeout(update, 300);
  });
  searchForm.addEventListener('change', event => {
    if (event.target.name !== 'tags') return;
    clearTimeout(timer);
    update();
  });
}

const editor = document.querySelector('[data-movement-editor]');

if (editor) {
  const announce = (list, message) => {
    const status = list.querySelector('[data-row-status]');
    if (status) status.textContent = message;
  };

  const renumber = list => {
    const rows = [...list.querySelector('[data-rows]').children];
    rows.forEach((row, index) => {
      row.querySelectorAll('[data-property]').forEach(input => {
        const property = input.dataset.property;
        input.name = `${list.dataset.list}[${index}]${property ? `.${property}` : ''}`;
      });
      const number = row.querySelector('[data-row-number]');
      if (number) number.textContent = String(index + 1);
      row.querySelectorAll('[data-row-action]').forEach(button => {
        const action = button.dataset.rowAction;
        button.disabled = (action === 'up' && index === 0) || (action === 'down' && index === rows.length - 1);
        button.setAttribute('aria-label', `${action === 'remove' ? 'Remove' : 'Move'} ${list.dataset.list === 'stages' ? 'stage' : 'item'} ${index + 1}${action === 'remove' ? '' : ` ${action}`}`);
      });
    });
  };

  editor.querySelectorAll('[data-list]').forEach(list => {
    const rows = list.querySelector('[data-rows]');
    renumber(list);
    list.querySelector('[data-add-row]').addEventListener('click', () => {
      const row = list.querySelector('template').content.firstElementChild.cloneNode(true);
      rows.append(row);
      renumber(list);
      row.querySelector('input,textarea,select')?.focus();
      announce(list, 'Item added.');
    });
    rows.addEventListener('click', event => {
      const button = event.target.closest('[data-row-action]');
      if (!button) return;
      const row = button.closest('[data-row]');
      const action = button.dataset.rowAction;
      if (action === 'up' && row.previousElementSibling) rows.insertBefore(row, row.previousElementSibling);
      else if (action === 'down' && row.nextElementSibling) rows.insertBefore(row.nextElementSibling, row);
      else if (action === 'remove') {
        const next = row.nextElementSibling || row.previousElementSibling;
        row.remove();
        (next?.querySelector('input,textarea,select') || list.querySelector('[data-add-row]')).focus();
      }
      renumber(list);
      if (action !== 'remove') button.focus();
      announce(list, action === 'remove' ? 'Item removed.' : 'Item order updated.');
    });
  });

  const name = editor.querySelector('[data-movement-name]');
  const key = editor.querySelector('[data-movement-key]');
  if (name && key) {
    let manuallyEdited = Boolean(key.value);
    key.addEventListener('input', () => { manuallyEdited = Boolean(key.value); });
    name.addEventListener('input', () => {
      if (manuallyEdited) return;
      key.value = name.value.trim().toLowerCase().replace(/\s+/g, '_')
        .replace(/[^a-z0-9_-]/g, '').replace(/[_-]{2,}/g, '_').replace(/^[_-]+|[_-]+$/g, '');
    });
  }
  editor.querySelector('[data-form-errors]')?.focus();
}
