package inventario.modelo;

import java.time.LocalDateTime;

public class Venta {
    
    private int idVenta;
    private LocalDateTime fechaHora;
    private double total;
    private Integer idCliente;
    private int idUsuario;
    private String tipoServicio; //Barra Salon
    private double descuentoTotal;
    private double impServicio;

    public Venta () {}

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVentaNuevo) {
        idVenta = idVentaNuevo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHoraNueva) {
        fechaHora = fechaHoraNueva;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double nuevoTotal) {
        total = nuevoTotal;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idClienteNuevo) {
        idCliente = idClienteNuevo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuarioNuevo) {
        idUsuario = idUsuarioNuevo;
    }

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicioNuevo) {
        tipoServicio = tipoServicioNuevo;
    }

    public double getDescuentoTotal() {
        return descuentoTotal;
    }

    public void setDescuentoTotal(double descuentoTotalNuevo) {
        descuentoTotal = descuentoTotalNuevo;
    }

    public double getImpServicio() {
        return impServicio;
    }

    public void setImpServicio(double impServicioNuevo) {
        impServicio = impServicioNuevo;
    }

    
}
