package inventario.dao;

import inventario.util.ConexionBD;
import java.sql.*;

public class ConfigDAO {

    public String getValor(String clave) throws SQLException {
        String sql = "SELECT valor FROM Config WHERE clave = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, clave);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("valor");
            }
        }
        return null;
    }

    public boolean setValor(String clave, String valor) throws SQLException {
        String sql = """
            INSERT INTO Config (clave, valor)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE valor = VALUES(valor)
        """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, clave);
            ps.setString(2, valor);
            return ps.executeUpdate() > 0;
        }
    }
}
