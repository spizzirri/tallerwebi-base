/**
 * [USEARCH] Buscador de personas del navbar.
 *
 * El input de escritorio y el del menú mobile comparten `[data-user-search]`. A las 300 ms de la
 * última tecla, el form GET navega a /users?q=...; al llegar a esa página, los dos inputs se
 * precargan con la consulta para que el navbar no parezca vacío mientras se ven los resultados.
 * Sin JavaScript el form sigue funcionando con Enter.
 */
(function () {
  const DEBOUNCE_MS = 300;
  const inputs = document.querySelectorAll('[data-user-search]');
  if (!inputs.length) return;

  if (window.location.pathname === '/users') {
    const current = new URLSearchParams(window.location.search).get('q');
    if (current) {
      for (let i = 0; i < inputs.length; i++) {
        if (!inputs[i].value) inputs[i].value = current;
      }
    }
  }

  for (let i = 0; i < inputs.length; i++) {
    let timer = null;
    inputs[i].addEventListener('input', function () {
      const input = inputs[i];
      if (timer) clearTimeout(timer);
      timer = setTimeout(function () {
        const form = input.form;
        if (!form) return;
        const target = new URL(form.action, window.location.origin);
        const value = input.value.trim();
        if (value) target.searchParams.set('q', value);
        else target.searchParams.delete('q');
        if (target.href === window.location.href) return;
        window.location.assign(target.href);
      }, DEBOUNCE_MS);
    });
  }
})();
