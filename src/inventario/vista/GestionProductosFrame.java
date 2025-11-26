package inventario.vista;

import inventario.dao.ProductoDAO;
import inventario.modelo.Producto;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class GestionProductosFrame extends JFrame {

    private JTable tablaProductos;
    private DefaultTableModel modelo;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnDesactivar;
    private JButton btnCerrar;

    private ProductoDAO productoDAO = new ProductoDAO();
    private VentanaVenta ventanaVenta; // para recargar productos en POS

    public GestionProductosFrame() {

        setTitle("Gestión de productos");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarProductosEnTabla();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Precio", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablaProductos = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tablaProductos);
        add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnDesactivar = new JButton("Desactivar");
        btnCerrar = new JButton("Cerrar");

        btnNuevo.addActionListener(e -> nuevoProducto());
        btnEditar.addActionListener(e -> editarProducto());
        btnDesactivar.addActionListener(e -> desactivarProducto());
        btnCerrar.addActionListener(e -> dispose());

        panelBotones.add(btnNuevo);
        panelBotones.add(btnEditar);
        panelBotones.add(btnDesactivar);
        panelBotones.add(btnCerrar);

        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarProductosEnTabla() {
        modelo.setRowCount(0);
        List<Producto> lista = productoDAO.listarProductos();
        for (Producto p : lista) {
            modelo.addRow(new Object[]{
                    p.getIdProducto(),
                    p.getNombreProducto(),
                    p.getPrecioProducto(),
                    p.getCantidadProducto()
            });
        }
    }

    private void nuevoProducto() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del producto:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        String precioStr = JOptionPane.showInputDialog(this, "Precio:");
        if (precioStr == null) return;

        String stockStr = JOptionPane.showInputDialog(this, "Stock inicial:");
        if (stockStr == null) return;

        try {
            double precio = Double.parseDouble(precioStr);
            int stock = Integer.parseInt(stockStr);

            Producto p = new Producto();
            p.setNombreProducto(nombre.trim());
            p.setDetalleProducto(""); // opcional
            p.setPrecioProducto(precio);
            p.setCantidadProducto(stock);
            p.setEstado(true);

            boolean ok = productoDAO.insertarProducto(p);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Producto creado.");
                cargarProductosEnTabla();
                if (ventanaVenta != null) ventanaVenta.cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo crear el producto.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio o stock inválidos.");
        }
    }

    private void editarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto.");
            return;
        }

        int idProducto = (int) modelo.getValueAt(fila, 0);
        String nombreActual = (String) modelo.getValueAt(fila, 1);
        double precioActual = (double) modelo.getValueAt(fila, 2);
        int stockActual = (int) modelo.getValueAt(fila, 3);

        String nombre = JOptionPane.showInputDialog(this, "Nombre:", nombreActual);
        if (nombre == null || nombre.trim().isEmpty()) return;

        String precioStr = JOptionPane.showInputDialog(this, "Precio:", precioActual);
        if (precioStr == null) return;

        String stockStr = JOptionPane.showInputDialog(this, "Stock:", stockActual);
        if (stockStr == null) return;

        try {
            double precio = Double.parseDouble(precioStr);
            int stock = Integer.parseInt(stockStr);

            Producto p = new Producto();
            p.setIdProducto(idProducto);
            p.setNombreProducto(nombre.trim());
            p.setDetalleProducto("");
            p.setPrecioProducto(precio);
            p.setCantidadProducto(stock);

            boolean ok = productoDAO.actualizarProducto(p);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Producto actualizado.");
                cargarProductosEnTabla();
                if (ventanaVenta != null) ventanaVenta.cargarProductos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el producto.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio o stock inválidos.");
        }
    }

    private void desactivarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto.");
            return;
        }

        int idProducto = (int) modelo.getValueAt(fila, 0);

        int resp = JOptionPane.showConfirmDialog(this,
                "¿Desactivar este producto? Ya no aparecerá en ventas.",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (resp != JOptionPane.YES_OPTION) return;

        boolean ok = productoDAO.desactivarProducto(idProducto);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Producto desactivado.");
            cargarProductosEnTabla();
            if (ventanaVenta != null) ventanaVenta.cargarProductos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo desactivar el producto.");
        }
    }
}
