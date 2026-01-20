package inventario.vista;

import inventario.dao.ConfigDAO;
import inventario.dao.DetalleVentaDAO;
import inventario.dao.ProductoDAO;
import inventario.dao.VentaDAO;
import inventario.modelo.DetalleVenta;
import inventario.modelo.Producto;
import inventario.modelo.Usuario;
import inventario.modelo.Venta;
import inventario.util.ResultadoOperacion;
import inventario.util.TicketBuilder;
import inventario.util.TicketPrinter;
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
    private JButton btnCierreCaja;

    private int idVenta = -1;
    private final Usuario usuario;
    private final int idUsuarioLogueado;
    public final boolean esAdmin;

    private final ProductoDAO productoDAO = new ProductoDAO();

    public VentanaVenta(Usuario usuario) {
        this.usuario = usuario;
        this.idUsuarioLogueado = usuario.getIdUsuario();
        this.esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());

        setTitle("MiTienda POS - Venta | Usuario: " + usuario.getNombreCompleto() +
                " (" + usuario.getRol() + ")");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        initComponents();
        configurarPermisosPorRol();
        crearMenuAdmin();
        cargarProductos();
    }

    private void initComponents() {

        // ROOT (evita que se pierda el panel inferior)
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        // ---------- Tabla de Productos ----------
        modeloProductos = new DefaultTableModel(new Object[]{"ID", "Nombre", "Precio", "Stock"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaProductos = new JTable(modeloProductos);
        tablaProductos.setRowHeight(28);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setAutoCreateRowSorter(true);
        tablaProductos.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.setBorder(BorderFactory.createTitledBorder("Productos"));

        // ---------- Tabla de Carrito ----------
        modeloCarrito = new DefaultTableModel(new Object[]{"ID Det.", "Producto", "Cant.", "P. Unit.", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setRowHeight(28);
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCarrito.setAutoCreateRowSorter(true);
        tablaCarrito.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        scrollCarrito.setBorder(BorderFactory.createTitledBorder("Carrito"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollProductos, scrollCarrito);
        split.setResizeWeight(0.5);
        split.setOneTouchExpandable(true);

        root.add(split, BorderLayout.CENTER);

        // ---------- Labels Totales ----------
        lblTotal = new JLabel("0.00");
        lblServicio = new JLabel("0.00");
        lblTotalPagar = new JLabel("0.00");

        // ---------- Botones (usar el ATRIBUTO, no variable local) ----------
        btnAgregar = new JButton("Agregar");
        btnEliminar = new JButton("Eliminar");
        btnModificar = new JButton("Modificar");
        btnConfirmar = new JButton("Confirmar");
        btnCancelar = new JButton("Cancelar");
        btnCierreCaja = new JButton("Cierre de caja"); // ✅ ahora sí es el atributo

        btnAgregar.addActionListener(e -> agregarProductoCarrito());
        btnEliminar.addActionListener(e -> eliminarItem());
        btnModificar.addActionListener(e -> modificarCantidad());
        btnConfirmar.addActionListener(e -> confirmarVentaActual());
        btnCancelar.addActionListener(e -> cancelarVentaActual());
        btnCierreCaja.addActionListener(e -> abrirCierreConPIN());

        Dimension btnSize = new Dimension(140, 34);
        btnAgregar.setPreferredSize(btnSize);
        btnEliminar.setPreferredSize(btnSize);
        btnModificar.setPreferredSize(btnSize);
        btnConfirmar.setPreferredSize(btnSize);
        btnCancelar.setPreferredSize(btnSize);
        btnCierreCaja.setPreferredSize(btnSize);

        // ---------- Panel inferior ----------
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setPreferredSize(new Dimension(900, 160));

        JPanel panelTotales = new JPanel(new GridLayout(1, 3, 10, 10));
        panelTotales.add(crearCardTotal("Subtotal", lblTotal));
        panelTotales.add(crearCardTotal("Servicio 10%", lblServicio));
        panelTotales.add(crearCardTotal("Total a pagar", lblTotalPagar));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnConfirmar);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnCierreCaja);

        panelInferior.add(panelTotales, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        root.add(panelInferior, BorderLayout.SOUTH);

        // UX: doble click para agregar
        tablaProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) agregarProductoCarrito();
            }
        });

        // Debug opcional (puedes borrar)
        System.out.println("btnCierreCaja null? " + (btnCierreCaja == null));
    }

    public void cargarProductos() {
        modeloProductos.setRowCount(0);
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

        String input = JOptionPane.showInputDialog(this, "Cantidad para " + nombreProd + ":");
        if (input == null) return;

        input = input.trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes ingresar una cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida. Solo números enteros.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (idVenta == -1) {
            Venta v = new Venta();
            v.setIdCliente(null);
            v.setIdUsuario(idUsuarioLogueado);
            v.setTipoServicio("SALON");

            VentaDAO vdao = new VentaDAO();
            idVenta = vdao.crearVenta(v);

            if (idVenta <= 0) {
                JOptionPane.showMessageDialog(this, "No se pudo crear la venta.", "Error", JOptionPane.ERROR_MESSAGE);
                idVenta = -1;
                return;
            }
        }

        double subtotal = precioUnitario * cantidad;

        DetalleVenta d = new DetalleVenta();
        d.setIdVenta(idVenta);
        d.setIdProducto(idProd);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(precioUnitario);
        d.setSubTotal(subtotal);

        VentaDAO vdao = new VentaDAO();
        ResultadoOperacion r = vdao.agregarDetalle(d);

        if (r.isOk()) {
            JOptionPane.showMessageDialog(this, r.getMensaje());
            cargarCarrito();
            cargarProductos();
            actualizarTotales();
        } else {
            JOptionPane.showMessageDialog(this, r.getMensaje(), "Error", JOptionPane.ERROR_MESSAGE);
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
                    (p != null ? p.getNombreProducto() : "Desconocido"),
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
        if (v == null) return;

        double subtotal = v.getTotal() - v.getImpServicio();
        lblTotal.setText(String.format("%.2f", subtotal));
        lblServicio.setText(String.format("%.2f", v.getImpServicio()));
        lblTotalPagar.setText(String.format("%.2f", v.getTotal()));
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

        if (input == null) return;

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
        cargarProductos();
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

        String[] opciones = {"SALON", "BARRA"};
        String tipoServicio = (String) JOptionPane.showInputDialog(
                this,
                "Tipo de servicio:",
                "Confirmar venta",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                "SALON"
        );

        if (tipoServicio == null) return;

        int resp = JOptionPane.showConfirmDialog(
                this,
                "¿Confirmar la venta? (SALON aplica 10% por mesa)",
                "Confirmar venta",
                JOptionPane.YES_NO_OPTION
        );

        if (resp != JOptionPane.YES_OPTION) return;

        try {
            VentaDAO vdao = new VentaDAO();
            Venta ventaConfirmada = vdao.confirmarVenta(idVenta, tipoServicio);

            DetalleVentaDAO ddao = new DetalleVentaDAO();
            List<TicketBuilder.Item> items = ddao.obtenerItemsParaTicket(idVenta);

            double subtotal = ventaConfirmada.getTotal() - ventaConfirmada.getImpServicio();

            String ticket = TicketBuilder.build(
                    "Pollos Corraleros",
                    idVenta,
                    ventaConfirmada.getTipoServicio(),
                    subtotal,
                    ventaConfirmada.getImpServicio(),
                    ventaConfirmada.getTotal(),
                    items
            );

            TicketPrinter.previewAndPrint(ticket, "Tiquete - Venta #" + idVenta);

            JOptionPane.showMessageDialog(
                    this,
                    "✅ Venta confirmada\n" +
                            "Tipo: " + ventaConfirmada.getTipoServicio() + "\n" +
                            "Cargo servicio: " + String.format("%.2f", ventaConfirmada.getImpServicio()) + "\n" +
                            "Total a pagar: " + String.format("%.2f", ventaConfirmada.getTotal())
            );

            reiniciarVenta();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ No se pudo confirmar la venta:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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

        if (resp != JOptionPane.YES_OPTION) return;

        VentaDAO vdao = new VentaDAO();
        boolean ok = vdao.cancelarVenta(idVenta);

        if (ok) {
            JOptionPane.showMessageDialog(this, "⚠ Venta cancelada y stock revertido.");
            reiniciarVenta();
        } else {
            JOptionPane.showMessageDialog(this, "❌ No se pudo cancelar la venta.");
        }
    }

    private void configurarPermisosPorRol() {
        // Si quieres que cualquier usuario vea el botón, NO escondas nada aquí.
        // Si luego quieres restricciones, se agregan aquí.
    }

    private void crearMenuAdmin() {
        if (!esAdmin) return;

        JMenuBar bar = new JMenuBar();
        JMenu menuAdmin = new JMenu("Admin");
        JMenuItem itemProductos = new JMenuItem("Gestión de productos");
        JMenuItem itemVentasDia = new JMenuItem("Reporte de ventas del día");
        JMenuItem itemCierre = new JMenuItem("Cierre de caja (global)");

        itemCierre.addActionListener(e -> {
            CierreCajaFrame c = new CierreCajaFrame(idUsuarioLogueado);
            c.setVisible(true);
        });

        menuAdmin.add(itemCierre);

        itemVentasDia.addActionListener(e -> {
            ReporteVentasRangoFrame r = new ReporteVentasRangoFrame();
            r.setVisible(true);
        });

        menuAdmin.add(itemVentasDia);

        itemProductos.addActionListener(e -> {
            GestionProductosFrame frame = new GestionProductosFrame();
            frame.setVisible(true);
        });

        menuAdmin.add(itemProductos);
        bar.add(menuAdmin);
        setJMenuBar(bar);
    }

    private void abrirCierreConPIN() {
        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(
                this,
                pf,
                "Ingrese PIN de gerente para Cierre de Caja",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (ok != JOptionPane.OK_OPTION) return;

        String pinIngresado = new String(pf.getPassword()).trim();
        if (pinIngresado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "PIN vacío.");
            return;
        }

        try {
            ConfigDAO cdao = new ConfigDAO();
            String pinReal = cdao.getValor("PIN_CIERRE_CAJA");

            if (pinReal == null) {
                JOptionPane.showMessageDialog(this,
                        "No hay PIN configurado. Configure 'PIN_CIERRE_CAJA' en la tabla Config.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!pinReal.equals(pinIngresado)) {
                JOptionPane.showMessageDialog(this, "PIN incorrecto.", "Acceso denegado",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            CierreCajaFrame c = new CierreCajaFrame(idUsuarioLogueado);
            c.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error verificando PIN:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel crearCardTotal(String titulo, JLabel valor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 12f));

        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 20f));
        valor.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(valor, BorderLayout.CENTER);
        return card;
    }
}
