document.addEventListener("DOMContentLoaded", () => {
    function validarCamposDeLogin(inputEmailValue, inputPasswordValue) {
        return inputEmailValue?.length > 0 && inputPasswordValue?.length > 0;
    }

    const btnLoginNode = document.getElementById("btn-login");
    const inputEmailNode = document.getElementById("email");
    const inputPasswordNode = document.getElementById("password");

    // Buena práctica: Salir si los elementos no se encuentran, para evitar errores si el script se carga en otras páginas.
    if (!btnLoginNode || !inputEmailNode || !inputPasswordNode) {
        return;
    }

    const handleKeyUp = () => {
        const isValid = validarCamposDeLogin(inputEmailNode.value, inputPasswordNode.value);
        btnLoginNode.disabled = !isValid;
    };

    inputEmailNode.addEventListener("keyup", handleKeyUp);
    inputPasswordNode.addEventListener("keyup", handleKeyUp);
});