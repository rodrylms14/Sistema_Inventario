package inventario.vista;

import inventario.dao.UsuarioDAO;
import inventario.modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public LoginFrame() {
        setTitle("MiTienda POS - Login");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblUser = new JLabel("Usuario:");
        JLabel lblPass = new JLabel("Contraseña:");

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        btnLogin = new JButton("Ingresar");

        btnLogin.addActionListener(e -> intentarLogin());

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblUser, gbc);

        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblPass, gbc);

        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnLogin, gbc);

        add(panel);
    }

    private void intentarLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese usuario y contraseña.");
            return;
        }

        Usuario u = usuarioDAO.login(user, pass);

        if (u == null) {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Bienvenido, " + u.getNombreCompleto() + "!");

        this.dispose(); // cerrar login

        if (u.getRol().equalsIgnoreCase("ADMIN")) {
            PanelAdmin pa = new PanelAdmin(u);
            pa.setVisible(true);
        } else {
            VentanaVenta vv = new VentanaVenta(u);
            vv.setVisible(true);
        }
    }

}
