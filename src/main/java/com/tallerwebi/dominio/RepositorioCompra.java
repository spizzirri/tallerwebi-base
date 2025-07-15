package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;
import com.tallerwebi.presentacion.UsuarioDto;

import java.util.List;


public interface RepositorioCompra {

    void guardarCompraDeUsuario(Compra compra);

    void guardarComonentesEnCompraComponente(CompraComponente compraComponente);

    List<Compra> obtenerCompraDeUsuarioLogueado(UsuarioDto usuarioLogueado);

    Compra obtenerUltimaCompraDeUsuarioLogueado(UsuarioDto usuarioLogueado);
}
