package inventario.vista;

import inventario.dao.VentaDAO;
import inventario.modelo.Venta;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.JSpinner.DateEditor; 
import javax.swing.table.DefaultTableModel; 


public class ReporteVentasRangoFrame extends JFrame {

    private JSpinner spDesde;
    private JSpinner spHasta;
    
    private JTable tabla;
    private DefaultTableModel modelo;
    private JLabel lblTotal;
    private JLabel lblServicio;

    private VentaDAO ventaDAO = new VentaDAO();
    private DateTimeFormatter fmtTabla = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public ReporteVentasRangoFrame() {
        setTitle("Reporte de ventas por rango de fechas");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ----- Panel superior: filtros -----
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panelFiltros.add(new JLabel("Desde:"));
        
        // 1. CREACIÓN DEL PRIMER JSpinner
        SpinnerDateModel smDesde = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spDesde = new JSpinner(smDesde);
        // Formato para que solo muestre la fecha (yyyy-MM-dd)
        DateEditor deDesde = new JSpinner.DateEditor(spDesde, "yyyy-MM-dd");
        spDesde.setEditor(deDesde);
        spDesde.setPreferredSize(new Dimension(100, 25)); 
        panelFiltros.add(spDesde);

        panelFiltros.add(new JLabel("Hasta:"));
        
        // 2. CREACIÓN DEL SEGUNDO JSpinner
        SpinnerDateModel smHasta = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spHasta = new JSpinner(smHasta);
        DateEditor deHasta = new JSpinner.DateEditor(spHasta, "yyyy-MM-dd");
        spHasta.setEditor(deHasta);
        spHasta.setPreferredSize(new Dimension(100, 25));
        panelFiltros.add(spHasta);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscar());
        panelFiltros.add(btnBuscar);

        add(panelFiltros, BorderLayout.NORTH);

        // ----- Tabla -----
        modelo = new DefaultTableModel(
                new Object[]{"ID Venta", "Fecha", "Total", "Servicio"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // ----- Totales -----
        JPanel panelTotales = new JPanel(new GridLayout(2, 2));
        lblTotal = new JLabel("0.00");
        lblServicio = new JLabel("0.00");

        panelTotales.add(new JLabel("Total vendido en rango:"));
        panelTotales.add(lblTotal);
        panelTotales.add(new JLabel("Servicio cobrado en rango:"));
        panelTotales.add(lblServicio);

        add(panelTotales, BorderLayout.SOUTH);
    }

    private void buscar() {
        Date dateDesde = (Date) spDesde.getValue();
        Date dateHasta = (Date) spHasta.getValue();
        LocalDate desde = dateDesde.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dateHasta.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (hasta.isBefore(desde)) {
            JOptionPane.showMessageDialog(this, "La fecha 'hasta' no puede ser menor que 'desde'.");
            return;
        }

        List<Venta> ventas = ventaDAO.listarVentasPorRango(desde, hasta);

        modelo.setRowCount(0);
        double total = 0;
        double servicio = 0;

        for (Venta v : ventas) {
            String fechaStr = v.getFechaHora() != null
                    ? v.getFechaHora().format(fmtTabla)
                    : "";

            modelo.addRow(new Object[]{
                    v.getIdVenta(),
                    fechaStr,
                    v.getTotal(),
                    v.getImpServicio()
            });

            total += v.getTotal();
            servicio += v.getImpServicio();
        }

        lblTotal.setText(String.format("%.2f", total));
        lblServicio.setText(String.format("%.2f", servicio));

        if (ventas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ventas en ese rango.");
        }
    }
}