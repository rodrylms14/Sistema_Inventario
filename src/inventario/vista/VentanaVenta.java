package inventario.vista;

import inventario.dao.DetalleVentaDAO;
import inventario.dao.ProductoDAO;
import inventario.dao.VentaDAO;
import inventario.modelo.DetalleVenta;
import inventario.modelo.Producto;
import inventario.modelo.Usuario;
import inventario.modelo.Venta;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaVenta extends JFrame {

    private JTable tablaProductos;
    private JTable tablaCarrito;
    private DefaultTableModel modeloProductos;
    private DefaultTableModel modeloCarrito;

    private JLabel lblTotal;
    private JLabel lblServicio;
    private JLabel lblTotalPagar;

    private JButton btnAgregar;
    private JButton btnEliminar;
    private JButton btnModificar;
    private JButton btnConfirmar;
    private JButton btnCancelar;

    private int idVenta = -1;
    private Usuario usuario;
    private int idUsuarioLogueado;

    private ProductoDAO productoDAO = new ProductoDAO();

    public VentanaVenta(Usuario usuario) {
        this.usuario = usuario;
        this.idUsuarioLogueado = usuario.getIdUsuario();

        setTitle("MiTienda POS - Venta | Usuario: " + usuario.getNombreCompleto() +
                " (" + usuario.getRol() + ")");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        initComponents();
        cargarProductos();
    }


    private void initComponents() {
        setLayout(new BorderLayout());

        // ---------- Tabla de Productos ----------
        modeloProductos = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Precio", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // solo lectura
            }
        };

        tablaProductos = new JTable(modeloProductos);
        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.setBorder(BorderFactory.createTitledBorder("Productos"));

        // ---------- Tabla de Carrito ----------
        modeloCarrito = new DefaultTableModel(
                new Object[]{"ID Det.", "Producto", "Cant.", "P. Unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCarrito = new JTable(modeloCarrito);
        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        scrollCarrito.setBorder(BorderFactory.createTitledBorder("Carrito"));

        // Dividir pantalla en dos paneles
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scrollProductos, scrollCarrito);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        // ---------- Panel inferior: totales + botones ----------
        JPanel panelInferior = new JPanel(new BorderLayout());

        // Totales
        JPanel panelTotales = new JPanel(new GridLayout(3, 2, 5, 5));
        lblTotal = new JLabel("0.00");
        lblServicio = new JLabel("0.00");
        lblTotalPagar = new JLabel("0.00");

        panelTotales.add(new JLabel("Total:"));
        panelTotales.add(lblTotal);
        panelTotales.add(new JLabel("Servicio 10%:"));
        panelTotales.add(lblServicio);
        panelTotales.add(new JLabel("Total a pagar:"));
        panelTotales.add(lblTotalPagar);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAgregar = new JButton("Agregar al carrito");
        btnAgregar.addActionListener(e -> agregarProductoCarrito());
        btnEliminar = new JButton("Eliminar ítem");
        btnEliminar.addActionListener(e -> eliminarItem());
        btnModificar = new JButton("Modificar cantidad");
        btnModificar.addActionListener(e -> modificarCantidad());
        btnConfirmar = new JButton("Confirmar venta");
        btnConfirmar.addActionListener(e -> confirmarVentaActual());
        btnCancelar = new JButton("Cancelar venta");
        btnCancelar.addActionListener(e -> cancelarVentaActual());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnConfirmar);
        panelBotones.add(btnCancelar);

        panelInferior.add(panelTotales, BorderLayout.WEST);
        panelInferior.add(panelBotones, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarProductos() {
        modeloProductos.setRowCount(0); // limpiar

        List<Producto> productos = productoDAO.listarProductos();
        for (Producto p : productos) {
            modeloProductos.addRow(new Object[]{
                    p.getIdProducto(),
                    p.getNombreProducto(),
                    p.getPrecioProducto(),
                    p.getCantidadProducto()
            });
        }
    }


    private void agregarProductoCarrito() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto.");
            return;
        }

        int idProd = (int) modeloProductos.getValueAt(fila, 0);
        String nombreProd = (String) modeloProductos.getValueAt(fila, 1);
        double precioUnitario = (double) modeloProductos.getValueAt(fila, 2);

        // -------- Preguntar cantidad --------
        String input = JOptionPane.showInputDialog(this, "Cantidad para " + nombreProd + ":");
        if (input == null) return; // cancelado
        int cantidad = Integer.parseInt(input);

        // -------- Crear venta si no existe --------
        if (idVenta == -1) {
        Venta v = new Venta();
        v.setIdCliente(null);
        v.setIdUsuario(idUsuarioLogueado);
        v.setTipoServicio("SALON");

            VentaDAO vdao = new VentaDAO();
            idVenta = vdao.crearVenta(v);
            System.out.println("🔵 idVenta creada = " + idVenta);
        }

        // -------- Insertar el detalle --------
        double subtotal = precioUnitario * cantidad;

        DetalleVenta d = new DetalleVenta();
        d.setIdVenta(idVenta);
        d.setIdProducto(idProd);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(precioUnitario);
        d.setSubTotal(subtotal);

        VentaDAO vdao = new VentaDAO();
        boolean ok = vdao.agregarDetalle(d);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Agregado al carrito.");
            cargarCarrito();
            cargarProductos();
            actualizarTotales();
        } else {
            JOptionPane.showMessageDialog(this, "Error al agregar producto.");
        }
    }

    private void cargarCarrito() {
        modeloCarrito.setRowCount(0);

        if (idVenta == -1) return;

        DetalleVentaDAO ddao = new DetalleVentaDAO();
        List<DetalleVenta> lista = ddao.listarPorVenta(idVenta);
        ProductoDAO pdao = new ProductoDAO();

        for (DetalleVenta det : lista) {
            Producto p = pdao.buscarPorId(det.getIdProducto());
            modeloCarrito.addRow(new Object[]{
                    det.getIdDetalle(),
                    p.getNombreProducto(),
                    det.getCantidad(),
                    det.getPrecioUnitario(),
                    det.getSubTotal()
            });
        }
    }

    private void actualizarTotales() {
        if (idVenta == -1) return;

        VentaDAO vdao = new VentaDAO();
        Venta v = vdao.obtenerVenta(idVenta);

        lblTotal.setText(String.format("%.2f", v.getTotal()));
        lblServicio.setText(String.format("%.2f", v.getImpServicio()));
        lblTotalPagar.setText(String.format("%.2f", v.getTotal() + v.getImpServicio()));
    }

    private void eliminarItem() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem del carrito.");
            return;
        }

        int idDetalle = (int) modeloCarrito.getValueAt(fila, 0);

        int resp = JOptionPane.showConfirmDialog(this,
                "¿Eliminar este ítem?", "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (resp != JOptionPane.YES_OPTION) return;

        VentaDAO vdao = new VentaDAO();
        boolean ok = vdao.eliminarDetalle(idDetalle);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Ítem eliminado.");
            cargarCarrito();
            cargarProductos();
            actualizarTotales();
        } else {
            JOptionPane.showMessageDialog(this, "Error al eliminar ítem.");
        }
    }

    private void modificarCantidad() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un ítem del carrito.");
            return;
        }

        int idDetalle = (int) modeloCarrito.getValueAt(fila, 0);
        String nombreProd = (String) modeloCarrito.getValueAt(fila, 1);
        int cantActual = (int) modeloCarrito.getValueAt(fila, 2);

        String input = JOptionPane.showInputDialog(
                this,
                "Nueva cantidad para " + nombreProd + " (actual: " + cantActual + "):",
                cantActual
        );

        if (input == null) return; // Cancelado
        int nuevaCant;
        try {
            nuevaCant = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida.");
            return;
        }

        if (nuevaCant <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que cero.");
            return;
        }

        VentaDAO vdao = new VentaDAO();
        boolean ok = vdao.actualizarCantidadDetalle(idDetalle, nuevaCant);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Cantidad actualizada.");
            cargarCarrito();
            cargarProductos();
            actualizarTotales();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar la cantidad.");
        }
    }

    private void reiniciarVenta() {
        idVenta = -1;
        modeloCarrito.setRowCount(0);
        lblTotal.setText("0.00");
        lblServicio.setText("0.00");
        lblTotalPagar.setText("0.00");
        cargarProductos(); // recarga stock actualizado
    }

    private void confirmarVentaActual() {
        if (idVenta == -1) {
            JOptionPane.showMessageDialog(this, "No hay venta en curso.");
            return;
        }

        if (modeloCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }

        int resp = JOptionPane.showConfirmDialog(
                this,
                "¿Confirmar la venta?",
                "Confirmar venta",
                JOptionPane.YES_NO_OPTION
        );

        if (resp != JOptionPane.YES_OPTION) {
            return;
        }

        // Aquí podrías, en futuro, imprimir un tiquete, registrar método de pago, etc.

        JOptionPane.showMessageDialog(this, " Venta confirmada correctamente.");
        reiniciarVenta();
    }

    private void cancelarVentaActual() {
        if (idVenta == -1) {
            JOptionPane.showMessageDialog(this, "No hay venta en curso para cancelar.");
            return;
        }

        int resp = JOptionPane.showConfirmDialog(
                this,
                "¿Cancelar la venta actual? Se revertirá el stock y se eliminarán los detalles.",
                "Cancelar venta",
                JOptionPane.YES_NO_OPTION
        );

        if (resp != JOptionPane.YES_OPTION) {
            return;
        }

        VentaDAO vdao = new VentaDAO();
        boolean ok = vdao.cancelarVenta(idVenta);

        if (ok) {
            JOptionPane.showMessageDialog(this, "⚠ Venta cancelada y stock revertido.");
            reiniciarVenta();
        } else {
            JOptionPane.showMessageDialog(this, " No se pudo cancelar la venta.");
        }
    }

}
