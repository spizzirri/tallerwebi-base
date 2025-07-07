package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.presentacion.CompraDto;
import com.tallerwebi.presentacion.UsuarioDto;

import java.util.List;

public interface ServicioCompra {

    void guardarCompraConUsuarioLogueado(CompraDto compra, UsuarioDto usuarioLogueado);

    List<Compra> obtenerCompraComponenteDeUnUsuarioLogueado(UsuarioDto usuarioLogueado);
}
