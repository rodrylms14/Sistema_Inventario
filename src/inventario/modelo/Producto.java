package inventario.modelo;

public class Producto {
    
    private int idProducto;
    private String nombreProducto;
    private String detalleProducto;
    private double precioProducto;
    private int cantidadProducto;
    private boolean estado;

    // Constructor vacio
    public Producto() {}

    // Constructor
    public Producto(int idProductoObj, String nombreProductoObj, String detalleProductoObj, double precioProductoObj, int cantidadProductoObj, boolean estadoObj) {
        idProducto = idProductoObj;
        nombreProducto = nombreProductoObj;
        detalleProducto = detalleProductoObj;
        precioProducto = precioProductoObj;
        cantidadProducto = cantidadProductoObj;
        estado = estadoObj;
    }

    //Getters 

    public int getIdProducto() {
        return idProducto;
    }
    public String getNombreProducto() {
        return nombreProducto;
    }
    public String getDetalleProducto() {
        return detalleProducto;
    }
    public double getPrecioProducto() {
        return precioProducto;
    }
    public int getCantidadProducto() {
        return cantidadProducto;
    }
    public boolean isEstado() {
        return estado;
    }

    //Setters 

    public void setIdProducto(int idProductoNuevo) {
        idProducto = idProductoNuevo;
    }
        public void setNombreProducto(String nombreProductoNuevo) {
        nombreProducto = nombreProductoNuevo;
    }
        public void setDetalleProducto(String detalleProductoNuevo) {
        detalleProducto = detalleProductoNuevo;
    }
        public void setPrecioProducto(double precioProductoNuevo) {
        precioProducto = precioProductoNuevo;
    }
    public void setCantidadProducto(int cantidadProductoNuevo) {
        cantidadProducto = cantidadProductoNuevo;
    }
    public void setEstado(boolean estadoNuevo) {
        estado  = estadoNuevo;
    }

}
