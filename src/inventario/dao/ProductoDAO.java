package inventario.dao;

import inventario.modelo.Producto;
import inventario.util.ConexionBD;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listarProductos() {
        List<Producto> lista = new ArrayList<>();

        String sql = "SELECT * FROM Producto";

        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("idProducto"));
                p.setNombreProducto(rs.getString("nombreProducto"));
                p.setDetalleProducto(rs.getString("detalleProducto"));
                p.setPrecioProducto(rs.getDouble("precioProducto"));
                p.setCantidadProducto(rs.getInt("cantidadProducto"));
                p.setEstado(rs.getBoolean("estado"));

                lista.add(p);
            }

        } catch (Exception e) {
            System.out.println(" Error al listar productos");
            e.printStackTrace();
        }

        return lista;
    }

    public boolean insertarProducto(Producto p) {
        String sql = "INSERT INTO Producto (nombreProducto, detalleProducto, precioProducto, cantidadProducto, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
            java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombreProducto());
            ps.setString(2, p.getDetalleProducto());
            ps.setDouble(3, p.getPrecioProducto());
            ps.setInt(4, p.getCantidadProducto());
            ps.setBoolean(5, p.isEstado());

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println(" Error al insertar producto");
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarProducto(Producto p) {
        String sql = "UPDATE Producto SET nombreProducto=?, detalleProducto=?, precioProducto=?, cantidadProducto=?, estado=? "
                + "WHERE idProducto=?";

        try (Connection con = ConexionBD.getConexion();
            java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombreProducto());
            ps.setString(2, p.getDetalleProducto());
            ps.setDouble(3, p.getPrecioProducto());
            ps.setInt(4, p.getCantidadProducto());
            ps.setBoolean(5, p.isEstado());
            ps.setInt(6, p.getIdProducto());

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Error al actualizar producto");
            e.printStackTrace();
            return false;
        }
    }

    public boolean desactivarProducto(int idProducto) {
        String sql = "UPDATE Producto SET estado=0 WHERE idProducto=?";

        try (Connection con = ConexionBD.getConexion();
            java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println(" Error al desactivar producto");
            e.printStackTrace();
            return false;
        
        }
    }

    public Producto buscarPorId(int idProducto) {
        String sql = "SELECT * FROM Producto WHERE idProducto=?";
        Producto p = null;

        try (Connection con = ConexionBD.getConexion();
            java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                p = new Producto();
                p.setIdProducto(rs.getInt("idProducto"));
                p.setNombreProducto(rs.getString("nombreProducto"));
                p.setDetalleProducto(rs.getString("detalleProducto"));
                p.setPrecioProducto(rs.getDouble("precioProducto"));
                p.setCantidadProducto(rs.getInt("cantidadProducto"));
                p.setEstado(rs.getBoolean("estado"));
            }

        } catch (Exception e) {
            System.out.println("Error al buscar producto");
            e.printStackTrace();
        }
        return p;
    }




}
