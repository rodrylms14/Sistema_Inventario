package inventario.modelo;

public class ResumenCaja {
    private int cantidadVentas;
    private double subtotalVentas;
    private double totalCargoMesa;
    private double totalFinal;

    public ResumenCaja(int cantidadVentas, double subtotalVentas, double totalCargoMesa, double totalFinal) {
        this.cantidadVentas = cantidadVentas;
        this.subtotalVentas = subtotalVentas;
        this.totalCargoMesa = totalCargoMesa;
        this.totalFinal = totalFinal;
    }

    public int getCantidadVentas() { return cantidadVentas; }
    public double getSubtotalVentas() { return subtotalVentas; }
    public double getTotalCargoMesa() { return totalCargoMesa; }
    public double getTotalFinal() { return totalFinal; }
}
