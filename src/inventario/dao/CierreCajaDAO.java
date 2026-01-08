package inventario.dao;

import inventario.modelo.ResumenCaja;
import inventario.util.ConexionBD;
import java.sql.*;
import java.time.LocalDate;

public class CierreCajaDAO {

    public ResumenCaja obtenerResumenDelDia(LocalDate fecha) throws SQLException {
        String sql = """
            SELECT
              COUNT(*) AS cantidadVentas,
              COALESCE(SUM(total - impuestoServicio), 0) AS subtotalVentas,
              COALESCE(SUM(impuestoServicio), 0) AS totalCargoMesa,
              COALESCE(SUM(total), 0) AS totalFinal
            FROM Venta
            WHERE DATE(fechaHora) = ?
              AND estado = 'CONFIRMADA'
        """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ResumenCaja(
                            rs.getInt("cantidadVentas"),
                            rs.getDouble("subtotalVentas"),
                            rs.getDouble("totalCargoMesa"),
                            rs.getDouble("totalFinal")
                    );
                }
            }
        }
        return new ResumenCaja(0, 0, 0, 0);
    }

    public boolean existeCierre(LocalDate fecha) throws SQLException {
        String sql = "SELECT 1 FROM CierreCaja WHERE fecha = ? LIMIT 1";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean guardarCierre(LocalDate fecha, ResumenCaja r, String observaciones) throws SQLException {
        String sql = """
            INSERT INTO CierreCaja (fecha, cantidadVentas, subtotalVentas, totalCargoMesa, totalFinal, observaciones)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));
            ps.setInt(2, r.getCantidadVentas());
            ps.setDouble(3, r.getSubtotalVentas());
            ps.setDouble(4, r.getTotalCargoMesa());
            ps.setDouble(5, r.getTotalFinal());
            ps.setString(6, observaciones);

            return ps.executeUpdate() > 0;
        }
    }
}
