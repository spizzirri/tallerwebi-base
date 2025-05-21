function suma(a, b) {
  return a + b
}

function multiplicar(a, b) {
  return a * b;
}


function cargarProductos(idCategoria) {
    fetch(`/spring/productos/categoria/${idCategoria}`)
        .then(response => response.text())
        .then(html => {
            document.getElementById("productos-container").innerHTML = html;
        });
}
window.onload = function() {
    cargarProductos(1);
};

