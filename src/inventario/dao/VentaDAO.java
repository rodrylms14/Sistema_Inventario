package inventario.dao;

import inventario.modelo.DetalleVenta;
import inventario.modelo.Venta;
import inventario.util.ConexionBD;
import java.sql.*;

public class VentaDAO {

    public int crearVenta(Venta v) {
        int idGenerado = -1;

        String sql = "INSERT INTO Venta (total, idCliente, idUsuario, tipoServicio, descuentoTotal, impuestoServicio) "
                   + "VALUES (0, ?, ?, ?, 0, 0)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // idCliente puede ser null
            if (v.getIdCliente() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, v.getIdCliente());
            }

            ps.setInt(2, v.getIdUsuario());
            ps.setString(3, v.getTipoServicio());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                idGenerado = rs.getInt(1);
            }

        } catch (Exception e) {
            System.out.println("Error al crear venta");
            e.printStackTrace();
        }

        return idGenerado;
    }

    public boolean agregarDetalle(DetalleVenta d) {
        String sql = "INSERT INTO DetalleVenta (idVenta, idProducto, cantidad, precioUnitario, subtotal) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getIdVenta());
            ps.setInt(2, d.getIdProducto());
            ps.setInt(3, d.getCantidad());
            ps.setDouble(4, d.getPrecioUnitario());
            ps.setDouble(5, d.getSubTotal());

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println(" Error al agregar detalle de venta");
            e.printStackTrace();
            return false;
        }
    }

    public Venta obtenerVenta(int idVenta) {
        String sql = "SELECT * FROM Venta WHERE idVenta=?";
        Venta v = null;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                v = new Venta();
                v.setIdVenta(rs.getInt("idVenta"));
                Timestamp ts = rs.getTimestamp("fechaHora");
                if (ts != null) {
                    v.setFechaHora(ts.toLocalDateTime());
                }
                v.setTotal(rs.getDouble("total"));
                int idCli = rs.getInt("idCliente");
                if (rs.wasNull()) {
                    v.setIdCliente(null);
                } else {
                    v.setIdCliente(idCli);
                }
                v.setIdUsuario(rs.getInt("idUsuario"));
                v.setTipoServicio(rs.getString("tipoServicio"));
                v.setDescuentoTotal(rs.getDouble("descuentoTotal"));
                v.setImpServicio(rs.getDouble("impuestoServicio"));
            }

        } catch (Exception e) {
            System.out.println("Error al obtener venta");
            e.printStackTrace();
        }

        return v;
    }
}
