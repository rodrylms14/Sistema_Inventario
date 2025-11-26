package inventario.dao;

import inventario.modelo.DetalleVenta;
import inventario.modelo.Venta;
import inventario.util.ConexionBD;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public boolean eliminarDetalle(int idDetalle) {
        String sql = "DELETE FROM DetalleVenta WHERE idDetalle=?";

        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.println("Intentando borrar idDetalle = " + idDetalle);
            ps.setInt(1, idDetalle);

            int filas = ps.executeUpdate();
            System.out.println("Filas afectadas = " + filas);

            return filas > 0;

        } catch (Exception e) {
            System.out.println(" Error al eliminar detalle");
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

   public boolean cancelarVenta(int idVenta) {
        String selectDetalles = "SELECT idDetalle FROM DetalleVenta WHERE idVenta=?";
        String deleteDetalle = "DELETE FROM DetalleVenta WHERE idDetalle=?";
        String deleteVenta = "DELETE FROM Venta WHERE idVenta=?";

        try (Connection con = ConexionBD.getConexion()) {

            // 1) Obtener los idDetalle
            PreparedStatement psSel = con.prepareStatement(selectDetalles);
            psSel.setInt(1, idVenta);
            ResultSet rs = psSel.executeQuery();

            // 2) Borrarlos uno por uno
            while (rs.next()) {
                int idDet = rs.getInt("idDetalle");
                PreparedStatement psDelDet = con.prepareStatement(deleteDetalle);
                psDelDet.setInt(1, idDet);
                psDelDet.executeUpdate();  // ✔ dispara trigger
            }

            // 3) Borrar la venta
            PreparedStatement psVenta = con.prepareStatement(deleteVenta);
            psVenta.setInt(1, idVenta);
            psVenta.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean actualizarCantidadDetalle(int idDetalle, int nuevaCantidad) {
        String sqlSelect = "SELECT precioUnitario FROM DetalleVenta WHERE idDetalle=?";
        String sqlUpdate = "UPDATE DetalleVenta SET cantidad=?, subtotal=? WHERE idDetalle=?";

        try (Connection con = ConexionBD.getConexion();
            PreparedStatement psSel = con.prepareStatement(sqlSelect)) {

            // 1) Obtener el precioUnitario actual del detalle
            psSel.setInt(1, idDetalle);
            ResultSet rs = psSel.executeQuery();

            if (!rs.next()) {
                System.out.println("No se encontró el detalle con idDetalle = " + idDetalle);
                return false;
            }

            double precioUnitario = rs.getDouble("precioUnitario");
            double nuevoSubtotal = precioUnitario * nuevaCantidad;

            // 2) Actualizar cantidad y subtotal
            try (PreparedStatement psUpd = con.prepareStatement(sqlUpdate)) {
                psUpd.setInt(1, nuevaCantidad);
                psUpd.setDouble(2, nuevoSubtotal);
                psUpd.setInt(3, idDetalle);

                int filas = psUpd.executeUpdate();
                System.out.println("Filas actualizadas (detalle): " + filas);
                return filas > 0;
            }

        } catch (Exception e) {
            System.out.println(" Error al actualizar cantidad del detalle");
            e.printStackTrace();
            return false;
        }
    }

    public List<Venta> listarVentasDelDia() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM Venta WHERE DATE(fechaHora) = CURDATE()";

        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Venta v = new Venta();
                v.setIdVenta(rs.getInt("idVenta"));
                v.setIdCliente(rs.getInt("idCliente"));
                v.setIdUsuario(rs.getInt("idUsuario"));
                v.setTipoServicio(rs.getString("tipoServicio"));
                v.setTotal(rs.getDouble("total"));
                v.setImpServicio(rs.getDouble("impuestoServicio"));
                Timestamp ts = rs.getTimestamp("fechaHora");
                if (ts != null) {
                    v.setFechaHora(ts.toLocalDateTime());
                }

                lista.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Venta> listarVentasPorUsuario(int idUsuario) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM Venta WHERE idUsuario = ?";

        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta();
                    v.setIdVenta(rs.getInt("idVenta"));
                    v.setTotal(rs.getDouble("total"));
                    v.setImpServicio(rs.getDouble("impuestoServicio"));

                    // fechaHora es LocalDateTime en tu modelo
                    java.sql.Timestamp ts = rs.getTimestamp("fechaHora");
                    if (ts != null) {
                        v.setFechaHora(ts.toLocalDateTime());
                    }

                    v.setIdUsuario(rs.getInt("idUsuario"));
                    lista.add(v);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }


    public List<Venta> listarVentasPorRango(LocalDate desde, LocalDate hasta) {
        List<Venta> lista = new ArrayList<>();

        String sql = "SELECT * FROM Venta " +
                    "WHERE DATE(fechaHora) BETWEEN ? AND ? " +
                    "ORDER BY fechaHora";

        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta();
                    v.setIdVenta(rs.getInt("idVenta"));
                    v.setTotal(rs.getDouble("total"));
                    v.setImpServicio(rs.getDouble("impuestoServicio"));
                    v.setIdUsuario(rs.getInt("idUsuario"));

                    Timestamp ts = rs.getTimestamp("fechaHora");
                    if (ts != null) {
                        v.setFechaHora(ts.toLocalDateTime());
                    }

                    lista.add(v);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }



}
