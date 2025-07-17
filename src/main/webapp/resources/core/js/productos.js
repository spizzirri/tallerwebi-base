document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    const categoria = urlParams.get('cat');
    const currentPath = window.location.pathname;

    // Remover active de todas las categorías primero
    document.querySelectorAll('.categoria-item').forEach(item => {
        item.classList.remove('active');
    });

    // Buscar "Todas las categorías" de múltiples maneras
    const todasCategorias = document.querySelector('[href="/productos"]') ||
        document.querySelector('[href*="/productos"]:not([href*="search"])') ||
        document.querySelector('.list-group-item:first-child');

    const activeItem = document.querySelector(`[href*="cat=${categoria}"]`);

    if (todasCategorias) {
        item.classList.remove('active');
    }

    if (categoria) {
        // Si hay categoría en URL, marcar la categoría específica
        if (activeItem) {
            activeItem.classList.add('active');
        }
    } else if (currentPath === '/productos' || currentPath.endsWith('/productos')) {
        // Si estamos en /productos sin parámetros, marcar "Todas las categorías" como activa
        if (todasCategorias) {
            todasCategorias.classList.add('active');
        }
    }
});