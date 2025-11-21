package inventario.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3306/inventario?useSSL=false&serverTimezone=America/Costa_Rica";
    private static final String USER = "root";        // <-- cambia si tu usuario es diferente
    private static final String PASSWORD = "MyNewPass1";  // <-- pon tu contraseña

    public static Connection getConexion() {
        Connection conexion = null;
        try {
            // Cargar el driver (en versiones nuevas casi no es necesario, pero no estorba)
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Conexión exitosa a la base de datos.");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ No se encontró el driver de MySQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Error al conectar con la base de datos.");
            e.printStackTrace();
        }
        return conexion;
    }

    public static void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                conexion.close();
                System.out.println("🔌 Conexión cerrada.");
            } catch (SQLException e) {
                System.out.println("❌ Error al cerrar la conexión.");
                e.printStackTrace();
            }
        }
    }
}
