package inventario;

import inventario.dao.ClienteDAO;
import inventario.dao.ProductoDAO;
import inventario.dao.VentaDAO;
import inventario.modelo.Cliente;
import inventario.modelo.DetalleVenta;
import inventario.modelo.Producto;
import inventario.modelo.Venta;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ProductoDAO pdao = new ProductoDAO();
        ClienteDAO cdao = new ClienteDAO();
        VentaDAO vdao = new VentaDAO();

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

        // 2) Definir usuario que atiende (por ahora, fijo en 1)
        int idUsuario = 1; // más adelante lo tomaremos del login

        // 3) Tipo de servicio
        System.out.print("Tipo de servicio (1=BARRA, 2=SALON): ");
        int tipo = Integer.parseInt(sc.nextLine());
        String tipoServicio = (tipo == 2) ? "SALON" : "BARRA";

        // 4) Crear venta en BD
        Venta v = new Venta();
        v.setIdCliente(idCliente);
        v.setIdUsuario(idUsuario);
        v.setTipoServicio(tipoServicio);

        int idVenta = vdao.crearVenta(v);
        System.out.println("Venta creada con id: " + idVenta);

        // 5) Agregar productos a la venta
        boolean seguir = true;

        while (seguir) {
            System.out.println("\nProductos disponibles:");
            List<Producto> productos = pdao.listarProductos();
            for (Producto p : productos) {
                System.out.println(p.getIdProducto() + " | " + p.getNombreProducto() +
                        " | Precio: " + p.getPrecioProducto() +
                        " | Stock: " + p.getCantidadProducto());
            }

            System.out.print("Ingrese idProducto: ");
            int idProd = Integer.parseInt(sc.nextLine());

            Producto prod = pdao.buscarPorId(idProd);
            if (prod == null) {
                System.out.println("Producto no encontrado.");
                continue;
            }

            System.out.print("Cantidad: ");
            int cant = Integer.parseInt(sc.nextLine());

            double precioUnitario = prod.getPrecioProducto();
            double subtotal = precioUnitario * cant;

            DetalleVenta d = new DetalleVenta();
            d.setIdVenta(idVenta);
            d.setIdProducto(idProd);
            d.setCantidad(cant);
            d.setPrecioUnitario(precioUnitario);
            d.setSubTotal(subtotal);

            boolean ok = vdao.agregarDetalle(d);
            if (ok) {
                System.out.println("Detalle agregado correctamente.");
            }

            System.out.print("¿Agregar otro producto? (s/n): ");
            String r = sc.nextLine().trim().toLowerCase();
            if (!r.equals("s")) {
                seguir = false;
            }
        }

        // 6) Consultar la venta final (total + servicio)
        Venta ventaFinal = vdao.obtenerVenta(idVenta);
        if (ventaFinal != null) {
            double totalPagar = ventaFinal.getTotal() + ventaFinal.getImpServicio();

            System.out.println("\n=== RESUMEN DE LA VENTA ===");
            System.out.println("ID Venta: " + ventaFinal.getIdVenta());
            System.out.println("Tipo servicio: " + ventaFinal.getTipoServicio());
            System.out.println("Total sin servicio: " + ventaFinal.getTotal());
            System.out.println("Impuesto servicio (10% si es SALON): " + ventaFinal.getImpServicio());
            System.out.println("TOTAL A PAGAR: " + totalPagar);
        }

        sc.close();
    }
}
