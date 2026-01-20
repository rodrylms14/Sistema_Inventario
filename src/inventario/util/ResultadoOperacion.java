package inventario.util;

public class ResultadoOperacion {
    private final boolean ok;
    private final String mensaje;

    public ResultadoOperacion(boolean ok, String mensaje) {
        this.ok = ok;
        this.mensaje = mensaje;
    }

    public boolean isOk() { return ok; }
    public String getMensaje() { return mensaje; }

    public static ResultadoOperacion exito(String msg) { return new ResultadoOperacion(true, msg); }
    public static ResultadoOperacion error(String msg) { return new ResultadoOperacion(false, msg); }
}

