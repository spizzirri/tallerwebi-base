(function () {
  const region = document.createElement('div');
  region.className = 'planit-toasts';
  region.setAttribute('aria-live', 'polite');
  region.setAttribute('aria-relevant', 'additions');
  document.body.appendChild(region);

  function show(message) {
    if (!message) return;
    const toast = document.createElement('div');
    toast.className = 'planit-toast';
    toast.setAttribute('role', 'status');
    const text = document.createElement('span');
    text.textContent = message;
    const close = document.createElement('button');
    close.type = 'button';
    close.textContent = '\u00d7';
    close.setAttribute('aria-label', 'Cerrar aviso');
    const progress = document.createElement('div');
    progress.className = 'planit-toast-progress';
    progress.setAttribute('aria-hidden', 'true');
    toast.append(text, close, progress);
    region.appendChild(toast);
    const animation = progress.animate(
      [{ transform: 'scaleX(0)' }, { transform: 'scaleX(1)' }],
      { duration: 6000, easing: 'linear', fill: 'forwards' }
    );
    const dismiss = () => { animation.cancel(); toast.remove(); };
    let hovered = false;
    const syncPlayback = () => {
      if (hovered || toast.contains(document.activeElement)) animation.pause();
      else animation.play();
    };
    animation.finished.then(() => toast.remove()).catch(() => {});
    close.addEventListener('click', dismiss);
    toast.addEventListener('mouseenter', () => { hovered = true; syncPlayback(); });
    toast.addEventListener('mouseleave', () => { hovered = false; syncPlayback(); });
    toast.addEventListener('focusin', syncPlayback);
    toast.addEventListener('focusout', () => queueMicrotask(syncPlayback));
    syncPlayback();
  }
  document.querySelectorAll('[data-toast]').forEach(node => {
    show(node.textContent.trim());
    node.remove();
  });
  const url = new URL(window.location.href);
  const messages = {
    passwordChanged: 'Contraseña actualizada.',
    saved: 'Configuración guardada.'
  };
  let consumed = false;
  Object.entries(messages).forEach(([key, message]) => {
    if (url.searchParams.get(key) === 'true') {
      show(message);
      url.searchParams.delete(key);
      consumed = true;
    }
  });
  if (consumed) history.replaceState(history.state, '', url.pathname + url.search + url.hash);
})();
