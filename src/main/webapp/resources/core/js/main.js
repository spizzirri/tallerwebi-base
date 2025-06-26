function suma(a, b) {
    return a + b
}

function multiplicar(a, b) {
    return a * b;
}

// Función para obtener cantidad vía AJAX como respaldo
function obtenerCantidadCarritoAjax() {

    fetch('/carritoDeCompras/cantidad')
        .then(response => response.json())
        .then(data => {
            actualizarContadorCarrito(data.cantidadEnCarrito);
        })
        .catch(error => {
            actualizarContadorCarrito(0);
        });
}

document.addEventListener("DOMContentLoaded", function () {
    // Inicializar contador del carrito
    if (typeof window.cantidadEnCarrito !== 'undefined') {
        actualizarContadorCarrito(window.cantidadEnCarrito);
    } else {
        obtenerCantidadCarritoAjax();
    }

    cargarProductos(1);

    const swiperInterval = setInterval(() => {
        if (document.querySelector('.mySwiper .swiper-wrapper')) {
            clearInterval(swiperInterval);
            new Swiper('.mySwiper', {
                slidesPerView: 5,
                spaceBetween: 16,
                navigation: {
                    nextEl: '.swiper-button-next',
                    prevEl: '.swiper-button-prev',
                },
                breakpoints: {
                    0: {slidesPerView: 3},
                    576: {slidesPerView: 4},
                    768: {slidesPerView: 5},
                    992: {slidesPerView: 6},
                }
            });
        }
    }, 100);
});

function cargarProductos(idCategoria) {
    fetch(`/productos/tipoComponente/${idCategoria}`)
        .then(response => response.text())
        .then(html => {
            document.getElementById("productos-container").innerHTML = html;
        })
}

