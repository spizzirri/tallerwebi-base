document.addEventListener('DOMContentLoaded', function () {
    const tarjetas = document.querySelectorAll('.precio-producto');

    tarjetas.forEach(tarjeta => {
        let precioStr = tarjeta.dataset.precio.replace(/\./g, '').replace(',', '.');
        let cantidad = parseInt(tarjeta.dataset.cantidad);
        let total = (parseFloat(precioStr) * cantidad).toFixed(2);

        // formateo nuevamente a como estaba
        const totalFormateado = new Intl.NumberFormat('es-AR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(total);

        tarjeta.textContent = `$${totalFormateado}`;
    });
});