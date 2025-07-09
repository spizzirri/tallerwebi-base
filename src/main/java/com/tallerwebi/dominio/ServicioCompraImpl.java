package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import com.tallerwebi.dominio.entidades.Compra;
import com.tallerwebi.dominio.entidades.CompraComponente;
import com.tallerwebi.presentacion.CompraComponenteDto;
import com.tallerwebi.presentacion.CompraDto;
import com.tallerwebi.presentacion.UsuarioDto;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;

@Service("servicioCompra")
@Transactional
public class ServicioCompraImpl implements ServicioCompra {

    private RepositorioUsuario repositorioUsuario;
    private RepositorioCompra repositorioCompra;

    public ServicioCompraImpl(RepositorioCompra repositorioCompra, RepositorioUsuario repositorioUsuario) {
        this.repositorioCompra = repositorioCompra;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public void guardarCompraConUsuarioLogueado(CompraDto compradto, UsuarioDto usuarioLogueado) {
        Usuario usuario = repositorioUsuario.buscarUsuario(usuarioLogueado.getEmail());
        Compra compraEntidad = new Compra();
        
        compraEntidad.setIdUsuario(usuario);
        compraEntidad.setFecha(compradto.getFecha());
        compraEntidad.setTotal(compradto.getTotal());
        compraEntidad.setMetodoDePago(compradto.getMetodoDePago());

        this.repositorioCompra.guardarCompraDeUsuario(compraEntidad);

        // para guardar los componentes asociados a una compra
        for(CompraComponenteDto dtoComponente : compradto.getProductosComprados()){
            CompraComponente compraComponenteEntidad = getCompraComponente(dtoComponente, compraEntidad);

            this.repositorioCompra.guardarComonentesEnCompraComponente(compraComponenteEntidad);
        }
    }

    private CompraComponente getCompraComponente(CompraComponenteDto dtoComponente, Compra compraEntidad) {
        CompraComponente compraComponenteEntidad = new CompraComponente();

        Componente componenteEntidad = new Componente();
        if (dtoComponente.getComponente() != null) {
            componenteEntidad.setId(dtoComponente.getComponente().getId());
        } else {
            componenteEntidad.setId(dtoComponente.getId());
        }
        compraComponenteEntidad.setCompra(compraEntidad);
        compraComponenteEntidad.setComponente(componenteEntidad);
        compraComponenteEntidad.setCantidad(dtoComponente.getCantidad());
        compraComponenteEntidad.setPrecioUnitario(dtoComponente.getPrecioUnitario());
        //compraComponenteEntidad.setArmado(dtoComponente.getArmado());
        return compraComponenteEntidad;
    }

    public List<Compra> obtenerCompraComponenteDeUnUsuarioLogueado(UsuarioDto usuarioLogueado) {
        List<Compra> comprasUsuarioLogueado = this.repositorioCompra.obtenerCompraDeUsuarioLogueado(usuarioLogueado);
        return comprasUsuarioLogueado;
    }
}
