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
        String sqlTraerDetalles = """
            SELECT idProducto, cantidad
            FROM DetalleVenta
            WHERE idVenta = ?
            FOR UPDATE
        """;

        String sqlRevertirStock = """
            UPDATE Producto
            SET cantidadProducto = cantidadProducto + ?
            WHERE idProducto = ?
        """;

        String sqlEliminarDetalles = "DELETE FROM DetalleVenta WHERE idVenta = ?";

        String sqlMarcarAnulada = """
            UPDATE Venta
            SET estado = 'ANULADA', total = 0, impuestoServicio = 0
            WHERE idVenta = ?
        """;

        Connection con = null;

        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            // 1) Traer detalles (y bloquear para evitar carreras)
            try (PreparedStatement ps = con.prepareStatement(sqlTraerDetalles)) {
                ps.setInt(1, idVenta);

                try (ResultSet rs = ps.executeQuery();
                    PreparedStatement psStock = con.prepareStatement(sqlRevertirStock)) {

                    // 2) Revertir stock por cada item del carrito
                    while (rs.next()) {
                        int idProducto = rs.getInt("idProducto");
                        int cantidad = rs.getInt("cantidad");

                        psStock.setInt(1, cantidad);
                        psStock.setInt(2, idProducto);
                        psStock.executeUpdate();
                    }
                }
            }

            // 3) Borrar detalles de la venta (carrito)
            try (PreparedStatement ps = con.prepareStatement(sqlEliminarDetalles)) {
                ps.setInt(1, idVenta);
                ps.executeUpdate();
            }

            // 4) Marcar la venta como ANULADA (no borramos la venta)
            try (PreparedStatement ps = con.prepareStatement(sqlMarcarAnulada)) {
                ps.setInt(1, idVenta);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se encontró la venta id=" + idVenta);
                }
            }

            con.commit();
            return true;

        } catch (Exception e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ignore) {}
            e.printStackTrace();
            return false;

        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException ignore) {}
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
        String sql = "SELECT * FROM Venta WHERE DATE(fechaHora) = CURDATE() AND estado='CONFIRMADA'";

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

    public Venta confirmarVenta(int idVenta, String tipoServicio) throws SQLException {
    if (tipoServicio == null) tipoServicio = "BARRA";
    tipoServicio = tipoServicio.trim().toUpperCase();

    Connection con = null;
    try {
        con = ConexionBD.getConexion();
        con.setAutoCommit(false);

        String sqlItems = """
            SELECT d.idProducto, d.cantidad, p.cantidadProducto, d.subtotal
            FROM DetalleVenta d
            JOIN Producto p ON p.idProducto = d.idProducto
            WHERE d.idVenta = ?
            FOR UPDATE
        """;

        double subtotalVenta = 0.0;

        try (PreparedStatement ps = con.prepareStatement(sqlItems)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hayItems = false;

                while (rs.next()) {
                    hayItems = true;

                    int idProducto = rs.getInt("idProducto");
                    int cantidad = rs.getInt("cantidad");
                    int stock = rs.getInt("cantidadProducto");
                    double subtotalLinea = rs.getDouble("subtotal");

                    if (cantidad <= 0) {
                        throw new SQLException("Cantidad inválida para producto id=" + idProducto);
                    }
                    if (stock < cantidad) {
                        throw new SQLException(
                            "Stock insuficiente para producto id=" + idProducto +
                            ". Stock: " + stock + ", requerido: " + cantidad
                        );
                    }

                    subtotalVenta += subtotalLinea;
                }

                if (!hayItems) {
                    throw new SQLException("El carrito está vacío.");
                }
            }
        }

        String sqlDescontar = """
            UPDATE Producto
            SET cantidadProducto = cantidadProducto - ?
            WHERE idProducto = ?
        """;

        String sqlDetalles = """
            SELECT idProducto, cantidad
            FROM DetalleVenta
            WHERE idVenta = ?
        """;

        try (PreparedStatement psDetalles = con.prepareStatement(sqlDetalles);
             PreparedStatement psDescontar = con.prepareStatement(sqlDescontar)) {

            psDetalles.setInt(1, idVenta);
            try (ResultSet rs = psDetalles.executeQuery()) {
                while (rs.next()) {
                    int idProducto = rs.getInt("idProducto");
                    int cantidad = rs.getInt("cantidad");

                    psDescontar.setInt(1, cantidad);
                    psDescontar.setInt(2, idProducto);
                    psDescontar.executeUpdate();
                }
            }
        }

        // Calcular cargo por mesa (10% si es SALON)
        double impServicio = 0.0;
        if ("SALON".equals(tipoServicio)) {
            impServicio = subtotalVenta * 0.10;
        }

        double totalFinal = subtotalVenta + impServicio;

        // Actualizar la venta con totales finales
        String sqlUpdateVenta = """
            UPDATE Venta
            SET tipoServicio = ?, impuestoServicio = ?, total = ?, estado='CONFIRMADA'
            WHERE idVenta = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sqlUpdateVenta)) {
            ps.setString(1, tipoServicio);
            ps.setDouble(2, impServicio);
            ps.setDouble(3, totalFinal);
            ps.setInt(4, idVenta);

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se encontró la venta id=" + idVenta);
            }
        }

        //  Commit
        con.commit();

        // Devolver un objeto Venta con los valores finales
        Venta v = new Venta();
        v.setIdVenta(idVenta);
        v.setTipoServicio(tipoServicio);
        v.setImpServicio(impServicio);
        v.setTotal(totalFinal);
        return v;

    } catch (SQLException ex) {
        if (con != null) {
            try { con.rollback(); } catch (SQLException ignore) {}
        }
        throw ex; // para que la UI muestre el error exacto (stock insuficiente, etc.)
    } finally {
        if (con != null) {
            try { con.setAutoCommit(true); } catch (SQLException ignore) {}
            try { con.close(); } catch (SQLException ignore) {}
        }
    }
}


}
