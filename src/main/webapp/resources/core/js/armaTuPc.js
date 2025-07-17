function desaparecerBanners() {
    let banners = document.querySelectorAll('.banners-info');
    banners.forEach(banner => {
        setTimeout(() => {
            banner.style.display = 'none';
        }, 2500);
    });
}

document.addEventListener("DOMContentLoaded", function () {
    desaparecerBanners();
    obtenerDatos();
    mostrarOpciones();
});

// URLs de MockAPI (simuladas)
const urlProgramas = 'https://686ef83391e85fac429f6ce1.mockapi.io/programas'; // Reemplazar con la real
const urlJuegos = 'https://686ef83391e85fac429f6ce1.mockapi.io/juegos';       // Reemplazar con la real

let programas = [];
let juegos = [];

const selectorCategoria = document.getElementById('selectorCategoria');
const selectorRequisitos = document.getElementById('selectorRequisitos').value;
const contenedor = document.getElementById('contenedorOpciones');
const btnFiltrar = document.getElementById('filtrarBtn');

async function obtenerDatos() {
    try {
        // Simulación de fetch real
        const resProgramas = await fetch(urlProgramas);
        const resJuegos = await fetch(urlJuegos);
        programas = await resProgramas.json();
        juegos = await resJuegos.json();
        mostrarOpciones('todos');
    } catch (error) {
        contenedor.innerHTML = "<p>Error al cargar los datos.</p>";
        console.error(error);
    }
}

function mostrarOpciones(filtro) {
    const contenedor = document.getElementById('contenedorOpciones');
    contenedor.innerHTML = '';

    const datosAMostrar = filtro === 'programas' ? programas
        : filtro === 'juegos' ? juegos
            : [...programas, ...juegos];

    datosAMostrar.forEach(item => {
        const col = document.createElement('div');
        col.className = 'col-md-4 mb-3';

        col.innerHTML = `
            <div class="card h-100">
                <div class="card-body d-flex align-items-center">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" id="check-${item.id}" value="${item.id}" name="appSeleccionada[]" data-tipo="${programas.includes(item) ? 'programa' : 'juego'}">
                        <label class="form-check-label ms-2" for="check-${item.id}">
                            ${item.nombre}
                        </label>
                    </div>
                </div>
            </div>
        `;
        contenedor.appendChild(col);
    });
}

selectorCategoria.addEventListener('change', () => {
    mostrarOpciones(selectorCategoria.value);
});

btnFiltrar.addEventListener('click', () => {
    const seleccionados = Array.from(document.querySelectorAll('#contenedorOpciones input[type="checkbox"]:checked'))
    .map(cb => ({
        id: cb.value,
        tipo: cb.dataset.tipo
    }));
});
