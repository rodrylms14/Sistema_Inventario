package inventario.vista;

import inventario.dao.CierreCajaDAO;
import inventario.modelo.ResumenCaja;
import inventario.util.TicketPrinter;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;

public class CierreCajaFrame extends JFrame {

    private final CierreCajaDAO dao = new CierreCajaDAO();

    // 🔴 Guardamos el usuario que está haciendo el cierre
    private final int idUsuarioLogueado;

    private LocalDate fecha = LocalDate.now();
    private ResumenCaja resumen = new ResumenCaja(0, 0, 0, 0);

    private JLabel lblFecha = new JLabel();
    private JLabel lblVentas = new JLabel("0");
    private JLabel lblSubtotal = new JLabel("0.00");
    private JLabel lblCargo = new JLabel("0.00");
    private JLabel lblTotal = new JLabel("0.00");
    private JTextField txtObs = new JTextField();

    // ✅ Constructor correcto: recibe idUsuario
    public CierreCajaFrame(int idUsuarioLogueado) {
        this.idUsuarioLogueado = idUsuarioLogueado;

        setTitle("Cierre de Caja (Global)");
        setSize(520, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        cargar();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblFecha.setText("Fecha: " + fecha);
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargar());

        top.add(lblFecha);
        top.add(btnRefrescar);

        JPanel center = new JPanel(new GridLayout(5, 2, 8, 8));
        center.add(new JLabel("Ventas confirmadas:"));
        center.add(lblVentas);
        center.add(new JLabel("Subtotal (sin cargo mesa):"));
        center.add(lblSubtotal);
        center.add(new JLabel("Cargo mesa (10% salón):"));
        center.add(lblCargo);
        center.add(new JLabel("Total final:"));
        center.add(lblTotal);
        center.add(new JLabel("Observaciones:"));
        center.add(txtObs);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnImprimir = new JButton("Imprimir cierre");
        JButton btnGuardar = new JButton("Guardar cierre");

        btnImprimir.addActionListener(e -> imprimir());
        btnGuardar.addActionListener(e -> guardar());

        bottom.add(btnImprimir);
        bottom.add(btnGuardar);

        main.add(top, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void cargar() {
        try {
            resumen = dao.obtenerResumenDelDia(fecha);

            lblVentas.setText(String.valueOf(resumen.getCantidadVentas()));
            lblSubtotal.setText(String.format("%.2f", resumen.getSubtotalVentas()));
            lblCargo.setText(String.format("%.2f", resumen.getTotalCargoMesa()));
            lblTotal.setText(String.format("%.2f", resumen.getTotalFinal()));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando resumen:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void imprimir() {
        String texto =
                "CIERRE DE CAJA (GLOBAL)\n" +
                "================================\n" +
                "Fecha: " + fecha + "\n" +
                "Ventas confirmadas: " + resumen.getCantidadVentas() + "\n" +
                "Subtotal: " + String.format("%.2f", resumen.getSubtotalVentas()) + "\n" +
                "Cargo mesa (10%): " + String.format("%.2f", resumen.getTotalCargoMesa()) + "\n" +
                "TOTAL: " + String.format("%.2f", resumen.getTotalFinal()) + "\n" +
                "================================\n" +
                (txtObs.getText().isBlank() ? "" : ("Obs: " + txtObs.getText() + "\n"));

        TicketPrinter.previewAndPrint(texto, "Cierre de caja - " + fecha);
    }

    private void guardar() {
        try {
            if (dao.existeCierre(fecha)) {
                JOptionPane.showMessageDialog(this, "⚠ Ya existe un cierre guardado para " + fecha);
                return;
            }

            // ✅ ahora sí pasamos el idUsuario correcto
            boolean ok = dao.guardarCierre(fecha, idUsuarioLogueado, resumen, txtObs.getText());

            if (ok) JOptionPane.showMessageDialog(this, "✅ Cierre guardado.");
            else JOptionPane.showMessageDialog(this, "❌ No se pudo guardar el cierre.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error guardando cierre:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
