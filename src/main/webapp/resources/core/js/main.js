function suma(a, b) {
  return a + b
}

function multiplicar(a, b) {
  return a * b;
}

document.addEventListener("DOMContentLoaded", function () {
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
                    0: { slidesPerView: 3 },
                    576: { slidesPerView: 4 },
                    768: { slidesPerView: 5 },
                    992: { slidesPerView: 6 },
                }
            });
        }
    }, 100);
});


function cargarProductos(idCategoria) {
    fetch(`/spring/productos/categoria/${idCategoria}`)
        .then(response => response.text())
        .then(html => {
            document.getElementById("productos-container").innerHTML = html;
        });
}

// window.onload = function() {
//     cargarProductos(1);
//
// };

