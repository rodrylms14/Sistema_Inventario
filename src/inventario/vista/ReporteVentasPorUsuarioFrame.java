package inventario.vista;

import inventario.dao.UsuarioDAO;
import inventario.dao.VentaDAO;
import inventario.modelo.Usuario;
import inventario.modelo.Venta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteVentasPorUsuarioFrame extends JFrame {

    private JComboBox<Usuario> comboUsuarios;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JLabel lblTotalVentas;
    private JLabel lblTotalServicio;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private VentaDAO ventaDAO = new VentaDAO();

    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReporteVentasPorUsuarioFrame() {
        setTitle("Reporte de ventas por usuario");
        setSize(750, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        cargarUsuarios();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ----- Panel superior: selección de usuario -----
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTop.add(new JLabel("Usuario:"));
        comboUsuarios = new JComboBox<>();
        comboUsuarios.addActionListener(e -> cargarVentasUsuarioSeleccionado());
        panelTop.add(comboUsuarios);

        add(panelTop, BorderLayout.NORTH);

        // ----- Tabla -----
        modelo = new DefaultTableModel(
                new Object[]{"ID Venta", "Fecha", "Total", "Servicio"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // ----- Totales -----
        JPanel panelBottom = new JPanel(new GridLayout(2, 2));
        lblTotalVentas = new JLabel("0.00");
        lblTotalServicio = new JLabel("0.00");

        panelBottom.add(new JLabel("Total vendido:"));
        panelBottom.add(lblTotalVentas);
        panelBottom.add(new JLabel("Total servicio:"));
        panelBottom.add(lblTotalServicio);

        add(panelBottom, BorderLayout.SOUTH);
    }

    private void cargarUsuarios() {
        comboUsuarios.removeAllItems();
        List<Usuario> usuarios = usuarioDAO.listarUsuarios();
        for (Usuario u : usuarios) {
            comboUsuarios.addItem(u);
        }

        // Si hay al menos un usuario, cargar sus ventas
        if (comboUsuarios.getItemCount() > 0) {
            comboUsuarios.setSelectedIndex(0);
            cargarVentasUsuarioSeleccionado();
        }
    }

    private void cargarVentasUsuarioSeleccionado() {
        Usuario seleccionado = (Usuario) comboUsuarios.getSelectedItem();
        if (seleccionado == null) return;

        List<Venta> ventas = ventaDAO.listarVentasPorUsuario(seleccionado.getIdUsuario());

        modelo.setRowCount(0);
        double total = 0;
        double totalServicio = 0;

        for (Venta v : ventas) {
            String fechaStr = (v.getFechaHora() != null)
                    ? v.getFechaHora().format(fmt)
                    : "";

            modelo.addRow(new Object[]{
                    v.getIdVenta(),
                    fechaStr,
                    v.getTotal(),
                    v.getImpServicio()
            });

            total += v.getTotal();
            totalServicio += v.getImpServicio();
        }

        lblTotalVentas.setText(String.format("%.2f", total));
        lblTotalServicio.setText(String.format("%.2f", totalServicio));
    }
}
