package inventario.dao;

import inventario.modelo.Usuario;
import inventario.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM Usuario";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("idUsuario"));
                u.setNombreCompleto(rs.getString("nombreCompleto"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setRol(rs.getString("rol"));
                lista.add(u);
            }

        } catch (Exception e) {
            System.out.println(" Error al listar usuarios");
            e.printStackTrace();
        }
        return lista;
    }

    public boolean insertarUsuario(Usuario u) {
        String sql = "INSERT INTO Usuario (nombreCompleto, username, password, rol) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombreCompleto());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getRol());

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println(" Error al insertar usuario");
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarUsuario(Usuario u) {
        String sql = "UPDATE Usuario SET nombreCompleto=?, username=?, password=?, rol=? "
                   + "WHERE idUsuario=?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombreCompleto());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getRol());
            ps.setInt(6, u.getIdUsuario());

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Error al actualizar usuario");
            e.printStackTrace();
            return false;
        }
    }

    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT * FROM Usuario WHERE username=?";
        Usuario u = null;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                u = new Usuario();
                u.setIdUsuario(rs.getInt("idUsuario"));
                u.setNombreCompleto(rs.getString("nombreCompleto"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setRol(rs.getString("rol"));
            }

        } catch (Exception e) {
            System.out.println("Error al buscar usuario por username");
            e.printStackTrace();
        }
        return u;
    }

    public Usuario login(String username, String password) {
        String sql = "SELECT * FROM Usuario WHERE username=? AND password=?";
        Usuario u = null;

        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                u = new Usuario();
                u.setIdUsuario(rs.getInt("idUsuario"));
                u.setNombreCompleto(rs.getString("nombreCompleto"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setRol(rs.getString("rol"));
            }

        } catch (Exception e) {
            System.out.println("Error en login");
            e.printStackTrace();
        }
        return u;
    }

    public boolean validarPasswordAdmin(String password) throws SQLException {
        String sql = "SELECT 1 FROM Usuario WHERE rol='ADMIN' AND password=? LIMIT 1";

        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


}
