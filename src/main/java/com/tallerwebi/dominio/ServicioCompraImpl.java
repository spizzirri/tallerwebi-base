package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;
import com.tallerwebi.dominio.entidades.Imagen;
import com.tallerwebi.presentacion.CompraComponenteDto;
import com.tallerwebi.presentacion.CompraDto;
import com.tallerwebi.presentacion.UsuarioDto;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.List;

@Service("servicioCompra")
@Transactional
public class ServicioCompraImpl implements ServicioCompra {

    private RepositorioUsuario repositorioUsuario;
    private RepositorioCompra repositorioCompra;
    private RepositorioComponente repositorioComponente;

    public ServicioCompraImpl(RepositorioCompra repositorioCompra, RepositorioUsuario repositorioUsuario, RepositorioComponente repositorioComponente) {
        this.repositorioCompra = repositorioCompra;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioComponente = repositorioComponente;
    }

    @Override
    public void guardarCompraConUsuarioLogueado(CompraDto compradto, UsuarioDto usuarioLogueado, HttpSession session) {
        Usuario usuario = repositorioUsuario.buscarUsuario(usuarioLogueado.getEmail());
        Compra compraEntidad = new Compra();

        compraEntidad.setIdUsuario(usuario);
        Object cpSession = session.getAttribute("cp");
        if (cpSession != null) {
            compraEntidad.setCp(cpSession.toString());
        } else {
            compraEntidad.setCp("1440");
        }

        compraEntidad.setFecha(compradto.getFecha());
        compraEntidad.setTotal(compradto.getTotal());
        compraEntidad.setMetodoDePago(compradto.getMetodoDePago());
        compraEntidad.setCp(compradto.getCp());

        this.repositorioCompra.guardarCompraDeUsuario(compraEntidad);

        // para guardar los componentes asociados a una compra
        for (CompraComponenteDto dtoComponente : compradto.getProductosComprados()) {
            CompraComponente compraComponenteEntidad = getCompraComponente(dtoComponente, compraEntidad);

            this.repositorioCompra.guardarComonentesEnCompraComponente(compraComponenteEntidad);
        }
    }

    //    private CompraComponente getCompraComponente(CompraComponenteDto dtoComponente, Compra compraEntidad) {
//        CompraComponente compraComponenteEntidad = new CompraComponente();
//
//        Componente componenteEntidad = new Componente();
//        if (dtoComponente.getComponente() != null) {
//            componenteEntidad.setId(dtoComponente.getComponente().getId());
//            componenteEntidad.setImagenes(dtoComponente.g);
//        } else {
//            componenteEntidad.setId(dtoComponente.getId());
//        }
//        compraComponenteEntidad.setCompra(compraEntidad);
//        compraComponenteEntidad.setComponente(componenteEntidad);
//        compraComponenteEntidad.setCantidad(dtoComponente.getCantidad());
//        compraComponenteEntidad.setPrecioUnitario(dtoComponente.getPrecioUnitario());
//        compraComponenteEntidad.setEsArmado(dtoComponente.getEsArmado());
//        compraComponenteEntidad.setNumeroDeArmado(dtoComponente.getNumeroDeArmado());
//        compraComponenteEntidad.setUrlImagen(dtoComponente.getUrlImagen());
//
//        return compraComponenteEntidad;
//    }
    private CompraComponente getCompraComponente(CompraComponenteDto dtoComponente, Compra compraEntidad) {
        CompraComponente compraComponenteEntidad = new CompraComponente();

        Componente componenteEntidad;

        Long componenteId = dtoComponente.getComponente() != null ? dtoComponente.getComponente().getId() : dtoComponente.getId();

        componenteEntidad = this.repositorioComponente.obtenerComponentePorId(componenteId);
        if (componenteEntidad == null) {
            componenteEntidad = new Componente();
            componenteEntidad.setId(componenteId);
        }

        compraComponenteEntidad.setCompra(compraEntidad);
        compraComponenteEntidad.setComponente(componenteEntidad);

        List<Imagen> imagenes = componenteEntidad.getImagenes();
        if (imagenes != null && !imagenes.isEmpty()) {
            compraComponenteEntidad.setUrlImagen(imagenes.get(0).getUrlImagen());
        } else {
            compraComponenteEntidad.setUrlImagen(""); // o una imagen por defecto
        }

        compraComponenteEntidad.setCantidad(dtoComponente.getCantidad());
        compraComponenteEntidad.setPrecioUnitario(dtoComponente.getPrecioUnitario());
        compraComponenteEntidad.setEsArmado(dtoComponente.getEsArmado());
        compraComponenteEntidad.setNumeroDeArmado(dtoComponente.getNumeroDeArmado());

        return compraComponenteEntidad;
    }


    public List<Compra> obtenerCompraComponenteDeUnUsuarioLogueado(UsuarioDto usuarioLogueado) {
        List<Compra> comprasUsuarioLogueado = this.repositorioCompra.obtenerCompraDeUsuarioLogueado(usuarioLogueado);
        return comprasUsuarioLogueado;
    }

    public Compra obtenerUltimaCompraComponenteDeUnUsuarioLogueado(UsuarioDto usuarioLogueado) {
        Compra comprasUsuarioLogueado = this.repositorioCompra.obtenerUltimaCompraDeUsuarioLogueado(usuarioLogueado);
        return comprasUsuarioLogueado;
    }
}
