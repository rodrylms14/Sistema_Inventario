package inventario.util;

import java.awt.*;
import java.awt.print.PrinterException;
import javax.swing.*;

public class TicketPrinter {

    // Muestra el ticket y permite imprimirlo (simple y efectivo)
    public static void previewAndPrint(String ticketText, String title) {
        JTextArea area = new JTextArea(ticketText);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(420, 520));

        int option = JOptionPane.showConfirmDialog(
                null,
                scroll,
                title == null ? "Tiquete" : title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                // Dialogo nativo de impresión
                boolean ok = area.print();
                if (!ok) {
                    JOptionPane.showMessageDialog(null, "Impresión cancelada.");
                }
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null,
                        "Error al imprimir:\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
