package inventario;

import java.sql.Connection;
import inventario.util.ConexionBD;

public class Main {
    public static void main(String[] args) {
        // Probar la conexión
        Connection con = ConexionBD.getConexion();

        // Aquí más adelante llamaremos a DAOs, etc.

        // Cerrar la conexión
        ConexionBD.cerrarConexion(con);
    }
}
