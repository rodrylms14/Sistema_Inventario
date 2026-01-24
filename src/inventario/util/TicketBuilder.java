package inventario.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketBuilder {

    public static class Item {
        public final String nombre;
        public final int cantidad;
        public final double precioUnit;
        public final double subtotal;

        public Item(String nombre, int cantidad, double precioUnit, double subtotal) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precioUnit = precioUnit;
            this.subtotal = subtotal;
        }
    }

    public static String build(
            String nombreNegocio,
            int idVenta,
            String tipoServicio,
            double subtotal,
            double cargoServicio,
            double total,
            List<Item> items
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append(nombreNegocio).append("\n");
        sb.append("================================\n");
        sb.append("Venta #").append(idVenta).append("\n");
        sb.append("Fecha: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        sb.append("Servicio: ").append(tipoServicio).append("\n");
        sb.append("--------------------------------\n");

        sb.append(String.format("%-18s %3s %7s\n", "Producto", "Cant", "Subt"));
        sb.append("--------------------------------\n");

        for (Item it : items) {
            String nombre = it.nombre.length() > 18 ? it.nombre.substring(0, 18) : it.nombre;
            sb.append(String.format("%-18s %3d %7.2f\n", nombre, it.cantidad, it.subtotal));
        }

        sb.append("--------------------------------\n");
        sb.append(String.format("Subtotal:           %10.2f\n", subtotal));
        sb.append(String.format("Cargo mesa (10%%):   %10.2f\n", cargoServicio));
        sb.append(String.format("TOTAL:              %10.2f\n", total));
        sb.append("================================\n");
        sb.append("Gracias por su compra\n");

        return sb.toString();
    }
}
