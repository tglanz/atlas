const editor = document.querySelector('[data-routine-editor]');

if (editor) {
  const sections = editor.querySelector('[data-routine-sections]');
  const sectionTemplate = document.querySelector('[data-routine-section-template]');
  const movementTemplate = document.querySelector('[data-routine-movement-template]');
  const picker = document.querySelector('[data-routine-picker]');
  const search = picker.querySelector('[data-routine-picker-search]');
  const options = [...picker.querySelectorAll('[data-movement-search]')];
  const status = editor.querySelector('[data-routine-status]');
  let pickerSection;
  let pickerTrigger;

  const announce = (message) => { status.textContent = message; };

  function reindex() {
    const sectionRows = [...sections.children];
    editor.querySelector('[data-no-sections]').hidden = sectionRows.length > 0;
    sectionRows.forEach((section, index) => {
      section.querySelector('[data-section-number]').textContent = `Section ${index + 1}`;
      section.querySelector('[data-routine-action="section-up"]').disabled = index === 0;
      section.querySelector('[data-routine-action="section-down"]').disabled = index === sectionRows.length - 1;
      section.querySelectorAll('[data-section-field]').forEach((field) => {
        const property = field.dataset.sectionField;
        field.name = `sections[${index}].${property}`;
        field.id = `section-${index}-${property}`;
        section.querySelector(`[data-section-label="${property}"]`).htmlFor = field.id;
      });
      const movements = [...section.querySelector('[data-routine-movements]').children];
      section.querySelector('[data-no-movements]').hidden = movements.length > 0;
      movements.forEach((movement, movementIndex) => {
        movement.querySelector('[data-movement-key]').name = `sections[${index}].movements[${movementIndex}]`;
        movement.querySelector('[data-routine-action="movement-up"]').disabled = movementIndex === 0;
        movement.querySelector('[data-routine-action="movement-down"]').disabled = movementIndex === movements.length - 1;
      });
    });
  }

  function move(row, direction, button) {
    const sibling = direction === 'up' ? row.previousElementSibling : row.nextElementSibling;
    if (!sibling) return;
    if (direction === 'up') sibling.before(row);
    else sibling.after(row);
    reindex();
    // Moving to the edge disables the clicked button; focus its row's text field or action.
    if (button.disabled) {
      row.querySelector('[data-section-field="name"], [data-routine-action]:not(:disabled)').focus();
    } else button.focus();
    announce(`Moved ${row.matches('[data-routine-section]') ? 'section' : 'movement'} ${direction}.`);
  }

  function filterChoices() {
    const query = search.value.trim().toLocaleLowerCase();
    let count = 0;
    options.forEach((option) => {
      option.hidden = !option.dataset.movementSearch.toLocaleLowerCase().includes(query);
      if (!option.hidden) count += 1;
    });
    picker.querySelector('[data-routine-picker-status]').textContent = count === 0
      ? 'No movements found.'
      : `${count} movement${count === 1 ? '' : 's'} available.`;
  }

  editor.addEventListener('click', (event) => {
    const button = event.target.closest('[data-routine-action]');
    if (!button) return;
    const action = button.dataset.routineAction;
    const section = button.closest('[data-routine-section]');
    const movement = button.closest('[data-routine-movement]');
    if (action === 'section-add') {
      const next = sectionTemplate.content.firstElementChild.cloneNode(true);
      sections.append(next);
      reindex();
      next.querySelector('[data-section-field="name"]').focus();
      announce('Section added.');
    } else if (action === 'section-remove') {
      const next = section.nextElementSibling || section.previousElementSibling;
      section.remove();
      reindex();
      (next ? next.querySelector('[data-section-field="name"]') : editor.querySelector('[data-routine-action="section-add"]')).focus();
      announce('Section removed.');
    } else if (action === 'section-up' || action === 'section-down') {
      move(section, action.endsWith('up') ? 'up' : 'down', button);
    } else if (action === 'movement-up' || action === 'movement-down') {
      move(movement, action.endsWith('up') ? 'up' : 'down', button);
    } else if (action === 'movement-remove') {
      const name = movement.querySelector('[data-movement-name]').textContent;
      const next = movement.nextElementSibling || movement.previousElementSibling;
      movement.remove();
      reindex();
      (next ? next.querySelector('[data-routine-action]:not(:disabled)') : section.querySelector('[data-routine-action="movement-add"]')).focus();
      announce(`${name} removed.`);
    } else if (action === 'movement-add') {
      pickerSection = section;
      pickerTrigger = button;
      search.value = '';
      filterChoices();
      picker.showModal();
      search.focus();
    }
  });

  picker.addEventListener('click', (event) => {
    if (event.target.closest('[data-routine-picker-close]')) picker.close();
    const button = event.target.closest('[data-routine-pick]');
    if (!button || !pickerSection) return;
    const movement = movementTemplate.content.firstElementChild.cloneNode(true);
    movement.querySelector('[data-movement-key]').value = button.dataset.key;
    movement.querySelector('[data-movement-name]').textContent = button.dataset.name;
    pickerSection.querySelector('[data-routine-movements]').append(movement);
    reindex();
    announce(`${button.dataset.name} added.`);
    picker.close();
  });
  picker.addEventListener('close', () => {
    pickerTrigger?.focus();
    pickerSection = undefined;
  });
  search.addEventListener('input', filterChoices);
  reindex();
}
