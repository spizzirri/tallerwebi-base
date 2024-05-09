package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ObjetivoUsuario;
import com.tallerwebi.dominio.ObjetivoUsuarioRepository;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Controller
public class ObjetivosController {

    private final DataSource dataSource;

    @Autowired
    public ObjetivosController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/guardar-objetivo")
    public String guardarObjetivo(@RequestParam("objetivo") String objetivo) {
        return "redirect:/vistaObjetivos";
    }

    private Usuario obtenerUsuarioPorEmail(String email) {

        return null;
    }

    private void guardarObjetivoUsuario(Long idUsuario, String objetivo) {
        String sql = "INSERT INTO ObjetivoUsuario (id_usuario, objetivo) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idUsuario);
            statement.setString(2, objetivo);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @GetMapping("/vistaObjetivos")
    public String mostrarVistaObjetivos() {
        return "vistaObjetivos";
    }

    public String guardarObjetivo(String objetivo, Object o) {
        return objetivo;
    }
}
