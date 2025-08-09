const fs = require('fs');
const path = require('path');
const { fireEvent } = require('@testing-library/dom');

describe('Vista de Login', () => {

    let html;

    beforeAll(() => {
        const htmlFilePath = path.join(__dirname, '../resources/generated/login.html');
        html = fs.readFileSync(htmlFilePath, 'utf-8');
    });

    beforeEach((done) => {
        // Adjuntamos un listener al evento 'load' de la ventana.
        // JSDOM disparará este evento una vez que el HTML esté parseado y
        // los scripts externos (como login.js) se hayan cargado y ejecutado.
        window.addEventListener('load', () => {
            done();
        });

        document.open();
        document.write(html);
        document.close();
    });

    test('debe mostrar el título principal de la página', () => {
        const h3 = document.querySelector('h3.form-signin-heading');
        expect(h3).not.toBeNull();
        expect(h3.textContent).toBe('Taller Web I');
    });

    test('debe mostrar un mensaje de error cuando se provee en el contexto', () => {
        const errorElement = document.querySelector('p.alert-danger');
        expect(errorElement).not.toBeNull();
        expect(errorElement.textContent).toContain('Credenciales inválidas.');
    });

    test('debe tener un campo de entrada para el email', () => {
        const emailInput = document.getElementById('email');
        expect(emailInput).not.toBeNull();
        expect(emailInput.type).toBe('email');
    });

    test('debe tener un botón de Login', () => {
        const loginButton = document.getElementById('btn-login');
        expect(loginButton).not.toBeNull();
        expect(loginButton.textContent).toBe('Login');
    });

    test('debe tener un enlace para registrarse', () => {
        const registerLink = document.getElementById('btn-register');
        expect(registerLink).not.toBeNull();
        expect(registerLink.getAttribute('href')).toBe('nuevo-usuario');
    });

    test('el botón de login debe habilitarse/deshabilitarse según los campos de email y password', () => {
        const inputEmailNode = document.getElementById('email');
        const inputPasswordNode = document.getElementById('password');
        const btnLoginNode = document.getElementById('btn-login');

        expect(btnLoginNode.disabled).toBe(true);
        fireEvent.keyUp(inputEmailNode, { target: { value: 'test@example.com' } });
        expect(btnLoginNode.disabled).toBe(true);
        fireEvent.keyUp(inputPasswordNode, { target: { value: 'password123' } });
        expect(btnLoginNode.disabled).toBe(false);
    });
});
