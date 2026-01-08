package inventario.vista;

import inventario.modelo.Usuario;
import java.awt.*;
import javax.swing.*;

public class PanelAdmin extends JFrame {

    private Usuario usuario;

    public PanelAdmin(Usuario usuario) {
        this.usuario = usuario;

        setTitle("Panel Administrativo | " + usuario.getNombreCompleto());
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 10, 10)); //  se ajusta a todos los botones
        panel.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));

        JLabel titulo = new JLabel("PANEL DEL ADMINISTRADOR", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton btnIrVentas = new JButton("🧾 Ir a Ventas");
        btnIrVentas.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnIrVentas.addActionListener(e -> {
            VentanaVenta v = new VentanaVenta(usuario); // ✅ usa el admin logueado
            v.setVisible(true);
            dispose(); // opcional
        });

        JButton btnProductos = new JButton("📦 Gestión de productos");
        btnProductos.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnProductos.addActionListener(e -> {
            GestionProductosFrame frame = new GestionProductosFrame();
            frame.setVisible(true);
        });

        JButton btnReporteUsuario = new JButton("📋 Ventas por usuario");
        btnReporteUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnReporteUsuario.addActionListener(e -> {
            ReporteVentasPorUsuarioFrame r = new ReporteVentasPorUsuarioFrame();
            r.setVisible(true);
        });

        JButton btnVentasRango = new JButton("📅 Ventas por rango de fechas");
        btnVentasRango.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnVentasRango.addActionListener(e -> {
            ReporteVentasRangoFrame r = new ReporteVentasRangoFrame();
            r.setVisible(true);
        });

        JButton btnReporteVentas = new JButton("📊 Reporte de ventas");
        btnReporteVentas.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnReporteVentas.addActionListener(e -> {
            ReporteVentasRangoFrame r = new ReporteVentasRangoFrame();
            r.setVisible(true);
        });

        JButton btnUsuarios = new JButton("👥 Gestión de usuarios (futuro)");
        btnUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnUsuarios.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Módulo en desarrollo.");
        });

        JButton btnCerrar = new JButton("🚪 Cerrar sesión");
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnCerrar.addActionListener(e -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
            this.dispose();
        });

        // ✅ Orden: título → ventas → reportes → productos → etc
        panel.add(titulo);
        panel.add(btnIrVentas);
        panel.add(btnProductos);
        panel.add(btnReporteUsuario);
        panel.add(btnVentasRango);
        panel.add(btnReporteVentas);
        panel.add(btnUsuarios);
        panel.add(btnCerrar);

        add(panel);
    }

}
