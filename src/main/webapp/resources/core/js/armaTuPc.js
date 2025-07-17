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
        col.className = 'col-md-4 mb-3 d-flex justify-content-center';
        const urlRuta = `/uploads/${item.nombre}.png`;
        const tipo = programas.includes(item) ? 'programa' : 'juego';

        col.innerHTML = `
            <label class="program-card" style="background-image: url('${urlRuta}');">
                <input type="checkbox" id="check-${item.id}" value="${item.id}" name="appSeleccionada[]" data-tipo="${tipo}">
                
                <div class="overlay d-flex align-items-end" style="position: absolute; bottom: 0; left: 0; right: 0; background-color: rgba(0,0,0,0.6); padding: 10px;">
                    <span class="text-white">${item.nombre}</span>
                </div>
            </label>
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
