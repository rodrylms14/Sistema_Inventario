package inventario.modelo;

public class DetalleVenta {
    
    private int idDetalle;
    private int idVenta;
    private int idProducto;
    private int cantidad;
    private double precioUnitario;
    private double subTotal;


    public DetalleVenta() {}

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalleNuevo) {
        idDetalle = idDetalleNuevo;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVentaNuevo) {
        idVenta = idVentaNuevo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProductoNuevo) {
        idProducto = idProductoNuevo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidadNuevo) {
        cantidad = cantidadNuevo;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitarioNuevo) {
        precioUnitario = precioUnitarioNuevo;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotalNuevo) {
        subTotal = subTotalNuevo;
    }

}
