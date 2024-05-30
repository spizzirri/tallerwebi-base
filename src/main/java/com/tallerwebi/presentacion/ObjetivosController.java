package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Controller
public class ObjetivosController {

    private final DataSource dataSource;

    @Autowired
    public ObjetivosController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/guardar-objetivo")
    public String guardarObjetivo(@RequestParam("objetivo") String objetivo, @RequestParam("email") String email) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        if (usuario == null) {
            return "redirect:/error"; // Redirige a una vista de error si no se encuentra el usuario
        }
        guardarObjetivoUsuario(usuario.getId(), objetivo);
        return "redirect:/objetivos-guardados";
    }

    Usuario obtenerUsuarioPorEmail(String email) {
        String sql = "SELECT id, email FROM Usuario WHERE email = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(resultSet.getLong("id"));
                usuario.setEmail(resultSet.getString("email"));
                return usuario;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    void guardarObjetivoUsuario(Long idUsuario, String objetivo) {
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
}
