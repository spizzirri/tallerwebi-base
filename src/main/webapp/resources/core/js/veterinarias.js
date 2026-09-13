/* Función para normalizar el texto de búsqueda (eliminar espacios y convertir a minúsculas) */
const normalizarTexto = (texto) => texto.trim().toLowerCase();

/* Función para aplicar las etiquetas de especialidades a las tarjetas de veterinarias */
const aplicarEtiquetasEspecialidades = () => {
  document.querySelectorAll("[data-especialidad]").forEach((chip) => {
    console.log(chip, "Chip, aplicarEtiquetasEspecialidades()");
    const codigo = chip.dataset.especialidad;
    console.log(codigo, "Codigo, aplicarEtiquetasEspecialidades()");
    if (!codigo) return;
    chip.textContent = codigo.replaceAll("_", " ").toLowerCase();
    console.log(
      chip.textContent,
      "Chip.textContent, aplicarEtiquetasEspecialidades()",
    );
  });
};

/* Buscador de veterinarias */
const inicializarBusqueda = () => {
  const input = document.getElementById("buscar-veterinaria");
  const lista = document.getElementById("veterinarias-lista");
  const mensajeSinResultados = document.getElementById(
    "veterinarias-sin-resultados",
  );

  if (!input || !lista || !mensajeSinResultados) return;

  const tarjetas = Array.from(lista.querySelectorAll(".veterinaria-card"));

  const filtrarVeterinarias = () => {
    const criterio = normalizarTexto(input.value);
    let visibles = 0;

    tarjetas.forEach((tarjeta) => {
      const nombre = normalizarTexto(tarjeta.dataset.nombre || "");
      const direccion = normalizarTexto(tarjeta.dataset.direccion || "");
      const coincide =
        !criterio || nombre.includes(criterio) || direccion.includes(criterio);

      tarjeta.classList.toggle("is-hidden", !coincide);
      if (coincide) visibles += 1;
    });

    mensajeSinResultados.hidden = visibles > 0 || !criterio;
  };

  input.addEventListener("input", filtrarVeterinarias);
};

document.addEventListener("DOMContentLoaded", () => {
  aplicarEtiquetasEspecialidades();
  inicializarBusqueda();
});
