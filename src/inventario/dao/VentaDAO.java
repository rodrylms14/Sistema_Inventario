package inventario.dao;

import inventario.modelo.DetalleVenta;
import inventario.modelo.Venta;
import inventario.util.ConexionBD;
import inventario.util.ResultadoOperacion;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    public int crearVenta(Venta v) {

        String sql = """
            INSERT INTO venta (total, idCliente, idUsuario, tipoServicio, descuentoTotal, impuestoServicio, estado)
            VALUES (0, ?, ?, ?, 0, 0, 'ABIERTA')
        """;

        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(
                    sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // idCliente puede ser NULL
            if (v.getIdCliente() == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, v.getIdCliente());
            }

            ps.setInt(2, v.getIdUsuario());
            ps.setString(3, v.getTipoServicio()); // BARRA o SALON

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // idVenta generado
                }
            }

        } catch (SQLException e) {
            System.out.println(" Error al crear venta");
            e.printStackTrace();
        }

        return -1;
    }


    public ResultadoOperacion agregarDetalle(DetalleVenta d) {
        String sqlProd = "SELECT cantidadProducto FROM Producto WHERE idProducto=? FOR UPDATE";
        String sqlDescontar = "UPDATE Producto SET cantidadProducto = cantidadProducto - ? WHERE idProducto=?";
        String sqlInsert = "INSERT INTO DetalleVenta (idVenta, idProducto, cantidad, precioUnitario, subtotal) VALUES (?,?,?,?,?)";

        Connection con = null;
        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            int stock;
            try (PreparedStatement ps = con.prepareStatement(sqlProd)) {
                ps.setInt(1, d.getIdProducto());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return ResultadoOperacion.error("Producto no existe.");
                    }
                    stock = rs.getInt(1);
                }
            }

            if (d.getCantidad() <= 0) {
                con.rollback();
                return ResultadoOperacion.error("Cantidad inválida.");
            }

            if (stock < d.getCantidad()) {
                con.rollback();
                return ResultadoOperacion.error("Stock insuficiente. Disponible: " + stock);
            }

            // Reservar stock
            try (PreparedStatement ps = con.prepareStatement(sqlDescontar)) {
                ps.setInt(1, d.getCantidad());
                ps.setInt(2, d.getIdProducto());
                ps.executeUpdate();
            }

            // Insertar detalle
            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setInt(1, d.getIdVenta());
                ps.setInt(2, d.getIdProducto());
                ps.setInt(3, d.getCantidad());
                ps.setDouble(4, d.getPrecioUnitario());
                ps.setDouble(5, d.getSubTotal());
                ps.executeUpdate();
            }

            recalcularTotales(con, d.getIdVenta());

            con.commit();
            return ResultadoOperacion.exito("Agregado al carrito.");

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignore) {}
            e.printStackTrace();
            return ResultadoOperacion.error("Error de base de datos: " + e.getMessage());

        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignore) {}
            try { if (con != null) con.close(); } catch (SQLException ignore) {}
        }
    }



    public boolean eliminarDetalle(int idDetalle) {
        String sqlSel = "SELECT idVenta, idProducto, cantidad FROM DetalleVenta WHERE idDetalle=? FOR UPDATE";
        String sqlDel = "DELETE FROM DetalleVenta WHERE idDetalle=?";
        String sqlDev = "UPDATE Producto SET cantidadProducto = cantidadProducto + ? WHERE idProducto=?";

        Connection con = null;
        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            int idVenta, idProducto, cantidad;

            // 1) Leer el detalle (bloqueado)
            try (PreparedStatement ps = con.prepareStatement(sqlSel)) {
                ps.setInt(1, idDetalle);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    idVenta = rs.getInt("idVenta");
                    idProducto = rs.getInt("idProducto");
                    cantidad = rs.getInt("cantidad");
                }
            }

            // 2) Borrar el detalle
            int filas;
            try (PreparedStatement ps = con.prepareStatement(sqlDel)) {
                ps.setInt(1, idDetalle);
                filas = ps.executeUpdate();
            }

            if (filas == 0) {
                con.rollback();
                return false;
            }

            // 3) Devolver stock reservado
            try (PreparedStatement ps = con.prepareStatement(sqlDev)) {
                ps.setInt(1, cantidad);
                ps.setInt(2, idProducto);
                ps.executeUpdate();
            }

            // 4) Recalcular totales
            recalcularTotales(con, idVenta);

            con.commit();
            return true;

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignore) {}
            e.printStackTrace();
            return false;

        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignore) {}
            try { if (con != null) con.close(); } catch (SQLException ignore) {}
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
    String sqlLockVenta = "SELECT estado FROM Venta WHERE idVenta = ? FOR UPDATE";

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
        WHERE idVenta = ? AND estado = 'ABIERTA'
    """;

    Connection con = null;

    try {
        con = ConexionBD.getConexion();
        con.setAutoCommit(false);

        // 0) Bloquear y validar venta
        String estado;
        try (PreparedStatement ps = con.prepareStatement(sqlLockVenta)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    con.rollback();
                    return false; // venta no existe
                }
                estado = rs.getString(1);
            }
        }

        // Si ya no está ABIERTA, no cancelar
        if (estado == null || !estado.equalsIgnoreCase("ABIERTA")) {
            con.rollback();
            return false;
        }

        // 1) Traer detalles y devolver stock
        try (PreparedStatement ps = con.prepareStatement(sqlTraerDetalles)) {
            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery();
                 PreparedStatement psStock = con.prepareStatement(sqlRevertirStock)) {

                while (rs.next()) {
                    int idProducto = rs.getInt("idProducto");
                    int cantidad = rs.getInt("cantidad");

                    psStock.setInt(1, cantidad);
                    psStock.setInt(2, idProducto);
                    psStock.executeUpdate();
                }
            }
        }

        // 2) Borrar detalles
        try (PreparedStatement ps = con.prepareStatement(sqlEliminarDetalles)) {
            ps.setInt(1, idVenta);
            ps.executeUpdate();
        }

        // 3) Marcar ANULADA (solo si estaba ABIERTA)
        int filas;
        try (PreparedStatement ps = con.prepareStatement(sqlMarcarAnulada)) {
            ps.setInt(1, idVenta);
            filas = ps.executeUpdate();
        }

        if (filas == 0) {
            con.rollback();
            return false;
        }

        con.commit();
        return true;

    } catch (Exception e) {
        try { if (con != null) con.rollback(); } catch (SQLException ignore) {}
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
    if (nuevaCantidad <= 0) return false;

    String sqlSel = """
        SELECT d.idVenta, d.idProducto, d.cantidad, d.precioUnitario, p.cantidadProducto
        FROM DetalleVenta d
        JOIN Producto p ON p.idProducto = d.idProducto
        WHERE d.idDetalle = ?
        FOR UPDATE
    """;

    String sqlUpdDet = "UPDATE DetalleVenta SET cantidad=?, subtotal=? WHERE idDetalle=?";
    String sqlDesc = "UPDATE Producto SET cantidadProducto = cantidadProducto - ? WHERE idProducto=?";
    String sqlDev  = "UPDATE Producto SET cantidadProducto = cantidadProducto + ? WHERE idProducto=?";

    Connection con = null;
    try {
        con = ConexionBD.getConexion();
        con.setAutoCommit(false);

        int idVenta, idProducto, cantActual, stock;
        double precio;

        try (PreparedStatement ps = con.prepareStatement(sqlSel)) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    con.rollback();
                    return false;
                }
                idVenta = rs.getInt("idVenta");
                idProducto = rs.getInt("idProducto");
                cantActual = rs.getInt("cantidad");
                precio = rs.getDouble("precioUnitario");
                stock = rs.getInt("cantidadProducto");
            }
        }

        int delta = nuevaCantidad - cantActual;

        // Si no cambió la cantidad, no hacemos nada
        if (delta == 0) {
            con.rollback(); // o con.commit(); (no hay cambios). Rollback ahorra trabajo.
            return true;
        }

        if (delta > 0) {
            if (stock < delta) throw new SQLException("Stock insuficiente para aumentar cantidad.");
            try (PreparedStatement ps = con.prepareStatement(sqlDesc)) {
                ps.setInt(1, delta);
                ps.setInt(2, idProducto);
                ps.executeUpdate();
            }
        } else { // delta < 0
            try (PreparedStatement ps = con.prepareStatement(sqlDev)) {
                ps.setInt(1, -delta);
                ps.setInt(2, idProducto);
                ps.executeUpdate();
            }
        }

        double nuevoSubtotal = precio * nuevaCantidad;
        int filas;
        try (PreparedStatement ps = con.prepareStatement(sqlUpdDet)) {
            ps.setInt(1, nuevaCantidad);
            ps.setDouble(2, nuevoSubtotal);
            ps.setInt(3, idDetalle);
            filas = ps.executeUpdate();
        }

        if (filas == 0) {
            con.rollback();
            return false;
        }

        recalcularTotales(con, idVenta);

        con.commit();
        return true;

    } catch (Exception e) {
        try { if (con != null) con.rollback(); } catch (SQLException ignore) {}
        e.printStackTrace();
        return false;

    } finally {
        try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignore) {}
        try { if (con != null) con.close(); } catch (SQLException ignore) {}
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

    public Venta confirmarVenta(int idVenta, String tipoServicio) {
        Connection con = null;

        String sqlTieneItems = "SELECT COUNT(*) FROM DetalleVenta WHERE idVenta = ?";
        String sqlUpdServicio = "UPDATE Venta SET tipoServicio = ? WHERE idVenta = ?";
        String sqlConfirmar = "UPDATE Venta SET estado = 'CONFIRMADA' WHERE idVenta = ? AND estado = 'ABIERTA'";

        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            // (Opcional pero PRO) bloquear la venta durante confirmación
            try (PreparedStatement ps = con.prepareStatement("SELECT idVenta FROM Venta WHERE idVenta = ? FOR UPDATE")) {
                ps.setInt(1, idVenta);
                ps.executeQuery();
            }

            // 1) Validar que la venta tenga items
            int items = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlTieneItems)) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) items = rs.getInt(1);
                }
            }
            if (items == 0) {
                con.rollback();
                return null;
            }

            // 2) Guardar tipoServicio
            try (PreparedStatement ps = con.prepareStatement(sqlUpdServicio)) {
                ps.setString(1, tipoServicio);
                ps.setInt(2, idVenta);
                ps.executeUpdate();
            }

            // 3) Recalcular totales
            recalcularTotales(con, idVenta);

            // 4) Confirmar solo si estaba ABIERTA
            int rows;
            try (PreparedStatement ps = con.prepareStatement(sqlConfirmar)) {
                ps.setInt(1, idVenta);
                rows = ps.executeUpdate();
            }
            if (rows == 0) {
                con.rollback();
                return null;
            }

            con.commit();
            return obtenerVenta(idVenta);

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ignore) {}
            e.printStackTrace();
            return null;

        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignore) {}
            try { if (con != null) con.close(); } catch (SQLException ignore) {}
        }
    }



    private void recalcularTotales(Connection conn, int idVenta) throws SQLException {
        double subtotal = 0.0;
        String tipoServicio = "BARRA";

        // 1) subtotal del carrito
        String sqlSubtotal = "SELECT COALESCE(SUM(subtotal), 0) FROM detalleventa WHERE idVenta = ?";
        try (var ps = conn.prepareStatement(sqlSubtotal)) {
            ps.setInt(1, idVenta);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) subtotal = rs.getDouble(1);
            }
        }

        // 2) tipo de servicio
        String sqlServicio = "SELECT tipoServicio FROM venta WHERE idVenta = ?";
        try (var ps = conn.prepareStatement(sqlServicio)) {
            ps.setInt(1, idVenta);
            try (var rs = ps.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) tipoServicio = rs.getString(1);
            }
        }

        double impServicio = tipoServicio.equalsIgnoreCase("SALON") ? subtotal * 0.10 : 0.0;
        double total = subtotal + impServicio;

        // 3) actualizar venta
        String sqlUpd = "UPDATE venta SET total = ?, impuestoServicio = ? WHERE idVenta = ?";
        try (var ps = conn.prepareStatement(sqlUpd)) {
            ps.setDouble(1, total);
            ps.setDouble(2, impServicio);
            ps.setInt(3, idVenta);
            ps.executeUpdate();
        }
    }



}
