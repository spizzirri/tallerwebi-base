document.addEventListener('DOMContentLoaded', () => {
  const input = document.getElementById('plan-share-link');
  const button = document.getElementById('copy-plan-share-link');
  const status = document.getElementById('plan-share-status');
  if (!input || !button || !status) return;
  input.value = new URL(input.value, window.location.origin).href;
  button.addEventListener('click', async () => {
    try {
      await navigator.clipboard.writeText(input.value);
      status.textContent = 'Link copiado.';
    } catch {
      input.focus();
      input.select();
      status.textContent = 'Seleccionamos el link para que puedas copiarlo.';
    }
  });
});
