package inventario.dao;

import inventario.modelo.DetalleVenta;
import inventario.util.ConexionBD;
import inventario.util.TicketBuilder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleVentaDAO {

    public List<DetalleVenta> listarPorVenta(int idVenta) {
        List<DetalleVenta> lista = new ArrayList<>();

        String sql = "SELECT * FROM DetalleVenta WHERE idVenta=?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetalleVenta d = new DetalleVenta();
                d.setIdDetalle(rs.getInt("idDetalle"));
                d.setIdVenta(rs.getInt("idVenta"));
                d.setIdProducto(rs.getInt("idProducto"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecioUnitario(rs.getDouble("precioUnitario"));
                d.setSubTotal(rs.getDouble("subtotal"));
                lista.add(d);
            }

        } catch (Exception e) {
            System.out.println("Error al listar detalles de venta");
            e.printStackTrace();
        }

        return lista;
    }

    public List<TicketBuilder.Item> obtenerItemsParaTicket(int idVenta) throws SQLException {
        List<TicketBuilder.Item> items = new ArrayList<>();

        String sql = """
            SELECT p.nombreProducto, d.cantidad, d.precioUnitario, d.subtotal
            FROM DetalleVenta d
            JOIN Producto p ON p.idProducto = d.idProducto
            WHERE d.idVenta = ?
            ORDER BY d.idDetalle
        """;

        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new TicketBuilder.Item(
                            rs.getString("nombreProducto"),
                            rs.getInt("cantidad"),
                            rs.getDouble("precioUnitario"),
                            rs.getDouble("subtotal")
                    ));
                }
            }
        }
        return items;
    }

}
