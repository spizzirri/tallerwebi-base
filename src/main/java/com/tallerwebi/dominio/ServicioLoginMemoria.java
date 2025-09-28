package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/*al no tener bdd , se tuvo que crear para guardar los datos, problemas con HIBERNATE*/
@Service

@Primary
public class ServicioLoginMemoria implements ServicioLogin {

    private List<Usuario> usuarios = new ArrayList<>();

    public ServicioLoginMemoria() {
        // Usuario de prueba

        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("test@unlam");
        usuarioMock.setPassword("123");
        usuarioMock.setRol("ADMIN");
        usuarioMock.setNombre("Administrador");
        usuarioMock.setApellido("AdministradorApellido");
        // Crear objetos Carrera
        Carrera c1 = new Carrera();
        c1.setNombre("Ingenieria en Sistema");

        Carrera c2 = new Carrera();
        c2.setNombre("Tecnicatura en Desarrollo Web");

// Crear lista y asignar
        List<Carrera> carreras = new ArrayList<>();
        carreras.add(c1);
        carreras.add(c2);

        usuarioMock.setCarreras(carreras);


        usuarios.add(usuarioMock);
    }

    @Override
    public Usuario consultarUsuario(String email, String password) {
        return usuarios.stream()
                .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void registrar(Usuario usuario) throws UsuarioExistente {
        boolean existe = usuarios.stream()
                .anyMatch(u -> u.getEmail().equals(usuario.getEmail()));
        if (existe) {
            throw new UsuarioExistente();
        }
        usuarios.add(usuario);
    }


    @Override
    public void asignarCarrerasPorNombre(Usuario usuario, List<String> nombresCarreras) {
        List<Carrera> carreras = nombresCarreras.stream()
                .map(nombre -> {
                    Carrera c = new Carrera();
                    c.setNombre(nombre);
                    return c;
                })
                .collect(Collectors.toList());
        usuario.setCarreras(carreras);
    }


}

/* PORQUE SE CREO */

       /* Explicación ampliada de ServicioLoginMemoria:

        Propósito del archivo:

        Esta clase es un mock de servicio de login que guarda los usuarios en memoria, usando una List<Usuario>.

        Se creó porque no hay base de datos (BDD) disponible en el entorno de prueba, y Hibernate no puede persistir los datos.

        Permite simular el registro y la autenticación de usuarios sin tocar una base de datos real.

        Usuarios de prueba:

        Se crea un usuario inicial usuarioMock con rol ADMIN para poder probar el login.

        También se le asignan un par de carreras de ejemplo, simulando la relación Usuario -> Carrera.

        Métodos principales:

        consultarUsuario(email, password) → busca un usuario que coincida con el email y la contraseña en la lista de memoria.

        registrar(usuario) → agrega un usuario nuevo a la lista; si ya existe un usuario con el mismo email, lanza la excepción UsuarioExistente.

        asignarCarrerasPorNombre(usuario, nombresCarreras) → convierte los nombres de carreras enviados desde el formulario en objetos Carrera y los asigna al usuario.

        Limitaciones de esta implementación:

        Datos volátiles: Todo lo que se registre desaparece al reiniciar la aplicación porque no hay persistencia real.

        No hay validación real ni integridad referencial: Las carreras se crean “al vuelo” a partir de nombres; no hay IDs ni control sobre materias u otras propiedades.

        Solo sirve para pruebas locales o demos: No es seguro ni adecuado para un entorno de producción.

        Por qué existe:

        Es una solución temporal para poder trabajar con la lógica de login y registro sin depender de una base de datos.

        Permite probar el flujo completo del registro y login, incluyendo la asignación de carreras, sin configurar Hibernate ni MySQL/PostgreSQL.*/


 /***EN CONLUSION***/

 /*Lo tuve que crear ServicioLoginMemoria porque defini relaciones entre Usuario y Carrera y en   modelo de dominio. Hibernate, al no tener una base de datos configurada, no puede persistir automáticamente esos objetos relacionados, y entonces:

Si se intenta usar el servicio con Hibernate sin base de datos, la aplicación se iria a la m# al intentar guardar un Usuario con su lista de Carrera.

Para poder probar el flujo de registro y asignación de carreras sin depender de la BDD, se creó este servicio en memoria. (otro archivo mas)

Así, se simula el almacenamiento y la relación Usuario -> Carrera mientras la aplicación está corriendo, pero los datos se pierden al reiniciar.*/