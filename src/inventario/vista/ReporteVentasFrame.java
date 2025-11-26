package inventario.vista;

import inventario.dao.VentaDAO;
import inventario.modelo.Venta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReporteVentasFrame extends JFrame {

    private JTable tablaVentas;
    private DefaultTableModel modelo;
    private JLabel lblTotalDia;
    private JLabel lblImpuestoTotal;

    public ReporteVentasFrame() {
        setTitle("Reporte de ventas del día");
        setSize(700, 400);
        setLocationRelativeTo(null);

        initComponents();
        cargarVentasDelDia();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(new Object[]{"ID Venta", "Fecha", "Total", "Servicio"}, 0);
        tablaVentas = new JTable(modelo);
        add(new JScrollPane(tablaVentas), BorderLayout.CENTER);

        JPanel resumen = new JPanel(new GridLayout(2, 2));
        lblTotalDia = new JLabel("0.00");
        lblImpuestoTotal = new JLabel("0.00");

        resumen.add(new JLabel("Total ventas del día: "));
        resumen.add(lblTotalDia);
        resumen.add(new JLabel("Impuestos cobrados: "));
        resumen.add(lblImpuestoTotal);

        add(resumen, BorderLayout.SOUTH);
    }

    private void cargarVentasDelDia() {
        VentaDAO dao = new VentaDAO();
        List<Venta> lista = dao.listarVentasDelDia();

        modelo.setRowCount(0);
        double totalAcumulado = 0;
        double totalServicio = 0;

        for (Venta v : lista) {
            modelo.addRow(new Object[]{
                    v.getIdVenta(),
                    v.getFechaHora(),
                    v.getTotal(),
                    v.getImpServicio()
            });
            totalAcumulado += v.getTotal();
            totalServicio += v.getImpServicio();
        }

        lblTotalDia.setText(String.format("%.2f", totalAcumulado));
        lblImpuestoTotal.setText(String.format("%.2f", totalServicio));
    }
}
