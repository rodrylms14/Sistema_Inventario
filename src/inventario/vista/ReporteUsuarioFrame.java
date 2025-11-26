package inventario.vista;

import inventario.dao.VentaDAO;
import inventario.modelo.Usuario;
import inventario.modelo.Venta;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ReporteUsuarioFrame extends JFrame {

    private Usuario usuario;

    public ReporteUsuarioFrame(Usuario usuario) {
        this.usuario = usuario;

        setTitle("Ventas realizadas por: " + usuario.getNombreCompleto());
        setSize(650, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID Venta", "Fecha", "Total", "Servicio"}, 0);

        JTable tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);

        VentaDAO vdao = new VentaDAO();
        List<Venta> lista = vdao.listarVentasPorUsuario(usuario.getIdUsuario());

        double acumuladoVentas = 0;
        double acumuladoServicios = 0;

        for (Venta v : lista) {
            modelo.addRow(new Object[]{
                    v.getIdVenta(),
                    v.getFechaHora(),
                    v.getTotal(),
                    v.getImpServicio()
            });

            acumuladoVentas += v.getTotal();
            acumuladoServicios += v.getImpServicio();
        }

        JPanel panelTotales = new JPanel(new GridLayout(2, 2));
        panelTotales.add(new JLabel("Ventas totales:"));
        panelTotales.add(new JLabel(String.format("%.2f", acumuladoVentas)));
        panelTotales.add(new JLabel("Impuestos acumulados:"));
        panelTotales.add(new JLabel(String.format("%.2f", acumuladoServicios)));

        add(scroll, BorderLayout.CENTER);
        add(panelTotales, BorderLayout.SOUTH);
    }
}
