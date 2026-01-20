package inventario;

import inventario.dao.ClienteDAO;
import inventario.dao.DetalleVentaDAO;
import inventario.dao.ProductoDAO;
import inventario.dao.VentaDAO;
import inventario.modelo.Cliente;
import inventario.modelo.DetalleVenta;
import inventario.modelo.Producto;
import inventario.modelo.Venta;
import inventario.util.ResultadoOperacion;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ProductoDAO pdao = new ProductoDAO();
        ClienteDAO cdao = new ClienteDAO();
        VentaDAO vdao = new VentaDAO();
        DetalleVentaDAO ddao = new DetalleVentaDAO();

        System.out.println("=== NUEVA VENTA ===");

        // 1) Elegir cliente (opcional)
        System.out.println("Clientes registrados:");
        List<Cliente> clientes = cdao.listarClientes();
        for (Cliente c : clientes) {
            System.out.println(c.getIdCliente() + " - " + c.getNombreCliente());
        }
        System.out.print("Ingrese idCliente (0 = sin cliente): ");
        int idCli = Integer.parseInt(sc.nextLine());
        Integer idCliente = (idCli == 0) ? null : idCli;

        // 2) Usuario que atiende (por ahora fijo)
        int idUsuario = 1;

        // 3) Tipo de servicio
        System.out.print("Tipo de servicio (1=BARRA, 2=SALON): ");
        int tipo = Integer.parseInt(sc.nextLine());
        String tipoServicio = (tipo == 2) ? "SALON" : "BARRA";

        // 4) Crear venta en BD (ABIERTA)
        Venta v = new Venta();
        v.setIdCliente(idCliente);
        v.setIdUsuario(idUsuario);
        v.setTipoServicio(tipoServicio);

        int idVenta = vdao.crearVenta(v);
        if (idVenta <= 0) {
            System.out.println("No se pudo crear la venta.");
            return;
        }
        System.out.println("Venta creada con id: " + idVenta);

        // 5) Agregar productos a la venta (carrito)
        boolean seguir = true;

        while (seguir) {
            System.out.println("\nProductos disponibles:");
            List<Producto> productos = pdao.listarProductos();
            for (Producto p : productos) {
                System.out.println(p.getIdProducto() + " | " + p.getNombreProducto()
                        + " | Precio: " + p.getPrecioProducto()
                        + " | Stock: " + p.getCantidadProducto());
            }

            System.out.print("Ingrese idProducto: ");
            int idProd = Integer.parseInt(sc.nextLine());

            Producto prod = pdao.buscarPorId(idProd);
            if (prod == null) {
                System.out.println(" Producto no encontrado.");
                continue;
            }

            System.out.print("Cantidad: ");
            int cant = Integer.parseInt(sc.nextLine());
            if (cant <= 0) {
                System.out.println(" Cantidad inválida.");
                continue;
            }

            double precioUnitario = prod.getPrecioProducto();
            double subtotal = precioUnitario * cant;

            DetalleVenta d = new DetalleVenta();
            d.setIdVenta(idVenta);
            d.setIdProducto(idProd);
            d.setCantidad(cant);
            d.setPrecioUnitario(precioUnitario);
            d.setSubTotal(subtotal);

            // ✅ En modo PRO: agregarDetalle reserva stock, inserta detalle y recalcula totales
            ResultadoOperacion r = vdao.agregarDetalle(d);
            if (r.isOk()) {
                System.out.println(" " + r.getMensaje());
            } else {
                System.out.println(" " + r.getMensaje());
            }

            System.out.print("¿Agregar otro producto? (s/n): ");
            String resp = sc.nextLine().trim().toLowerCase();
            if (!resp.equals("s")) {
                seguir = false;
            }
        }

        // 6) Gestión del carrito antes de confirmar
        boolean gestionCarrito = true;

        while (gestionCarrito) {
            System.out.println("\n=== CARRITO DE LA VENTA " + idVenta + " ===");
            List<DetalleVenta> detalles = ddao.listarPorVenta(idVenta);

            if (detalles.isEmpty()) {
                System.out.println("(Carrito vacío)");
            } else {
                for (DetalleVenta det : detalles) {
                    Producto prodDet = pdao.buscarPorId(det.getIdProducto());
                    String nombre = (prodDet != null) ? prodDet.getNombreProducto() : ("ID:" + det.getIdProducto());

                    System.out.println(det.getIdDetalle() + " | " + nombre
                            + " | Cant: " + det.getCantidad()
                            + " | Subtotal: " + det.getSubTotal());
                }
            }

            // ✅ En nuestro diseño PRO: Venta.total ya incluye impuestoServicio (subtotal + servicio)
            Venta ventaActual = vdao.obtenerVenta(idVenta);
            if (ventaActual == null) {
                System.out.println(" No se pudo obtener la venta.");
                return;
            }

            System.out.println("\nTotal (incluye servicio si aplica): " + ventaActual.getTotal());
            System.out.println("Impuesto servicio: " + ventaActual.getImpServicio());
            System.out.println("TOTAL A PAGAR: " + ventaActual.getTotal());

            System.out.println("\nOpciones:");
            System.out.println("1. Eliminar ítem del carrito");
            System.out.println("2. Modificar cantidad de un ítem");
            System.out.println("3. Confirmar venta");
            System.out.println("4. Cancelar venta");

            System.out.print("Seleccione opción: ");
            int op = Integer.parseInt(sc.nextLine());

            if (op == 1) {
                System.out.print("Ingrese idDetalle a eliminar: ");
                int idDet = Integer.parseInt(sc.nextLine());

                boolean eliminado = vdao.eliminarDetalle(idDet);
                if (eliminado) {
                    System.out.println(" Ítem eliminado (stock revertido).");
                } else {
                    System.out.println(" No se pudo eliminar el ítem.");
                }

            } else if (op == 2) {
                System.out.print("Ingrese idDetalle a modificar: ");
                int idDet = Integer.parseInt(sc.nextLine());

                System.out.print("Ingrese nueva cantidad: ");
                int nuevaCant = Integer.parseInt(sc.nextLine());

                boolean actualizado = vdao.actualizarCantidadDetalle(idDet, nuevaCant);
                if (actualizado) {
                    System.out.println(" Cantidad actualizada.");
                } else {
                    System.out.println(" No se pudo actualizar la cantidad (stock insuficiente o dato inválido).");
                }

            } else if (op == 3) {
                Venta confirmada = vdao.confirmarVenta(idVenta, tipoServicio);
                if (confirmada != null) {
                    System.out.println(" Venta confirmada.");
                } else {
                    System.out.println(" No se pudo confirmar la venta (¿carrito vacío o venta no ABIERTA?).");
                }
                gestionCarrito = false;

            } else if (op == 4) {
                boolean cancelada = vdao.cancelarVenta(idVenta);
                if (cancelada) {
                    System.out.println(" Venta cancelada (stock revertido).");
                } else {
                    System.out.println(" No se pudo cancelar la venta.");
                }
                gestionCarrito = false;

            } else {
                System.out.println(" Opción inválida.");
            }
        }

        // 7) Consultar la venta final (total + servicio)
        Venta ventaFinal = vdao.obtenerVenta(idVenta);
        if (ventaFinal != null) {
            System.out.println("\n=== RESUMEN DE LA VENTA ===");
            System.out.println("ID Venta: " + ventaFinal.getIdVenta());
            System.out.println("Tipo servicio: " + ventaFinal.getTipoServicio());
            System.out.println("Total (incluye servicio si aplica): " + ventaFinal.getTotal());
            System.out.println("Impuesto servicio: " + ventaFinal.getImpServicio());
            System.out.println("TOTAL A PAGAR: " + ventaFinal.getTotal());
        }
    }
}
