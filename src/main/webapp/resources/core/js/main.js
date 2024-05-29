// Obtiene el elemento de la dropdown
    var dropdownToggle = document.getElementById('navbarDropdown');
    // Añade un evento clic al enlace de la dropdown
    dropdownToggle.addEventListener('click', function() {
      // Activa la dropdown al hacer clic
      this.nextElementSibling.classList.toggle('show');
    });