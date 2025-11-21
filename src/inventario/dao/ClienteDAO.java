package inventario.dao;

import inventario.modelo.Cliente;
import inventario.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Cliente";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt("idCliente"));
                c.setNombreCliente(rs.getString("nombreCliente"));
                lista.add(c);
            }

        } catch (Exception e) {
            System.out.println("Error al listar clientes");
            e.printStackTrace();
        }
        return lista;
    }

    public boolean insertarCliente(Cliente c) {
        String sql = "INSERT INTO Cliente (nombreCliente) VALUES (?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombreCliente());
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Error al insertar cliente");
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarCliente(Cliente c) {
        String sql = "UPDATE Cliente SET nombreCliente=? WHERE idCliente=?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombreCliente());
            ps.setInt(2, c.getIdCliente());
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Error al actualizar cliente");
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarCliente(int idCliente) {
        String sql = "DELETE FROM Cliente WHERE idCliente=?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Error al eliminar cliente");
            e.printStackTrace();
            return false;
        }
    }

    public Cliente buscarPorId(int idCliente) {
        String sql = "SELECT * FROM Cliente WHERE idCliente=?";
        Cliente c = null;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                c = new Cliente();
                c.setIdCliente(rs.getInt("idCliente"));
                c.setNombreCliente(rs.getString("nombreCliente"));
            }

        } catch (Exception e) {
            System.out.println("Error al buscar cliente");
            e.printStackTrace();
        }
        return c;
    }
}
