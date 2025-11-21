package inventario.modelo;

public class Cliente {
    
    //Atributos
    private int idCliente;
    private String nombreCliente;

    public Cliente() {}

    public Cliente (int idClienteObj, String nombreClienteObj) {
        idCliente = idClienteObj;
        nombreCliente = nombreClienteObj;
    }

    public int getIdCliente() {
        return idCliente;
    }
    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setIdCliente(int idClienteNuevo) {
        idCliente = idClienteNuevo;
    }
    public void setNombreCliente(String nombreClienteNuevo) {
        nombreCliente = nombreClienteNuevo;
    }

    @Override
    public String toString() {
        return idCliente + "Nombre: " + nombreCliente;  
    }
}
