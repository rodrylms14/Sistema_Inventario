package inventario.modelo;

public class Usuario {
    
    private int idUsuario;
    private String nombreCompleto;
    private String username;
    private String password;
    private String rol;
    
    public Usuario() {

    }

    public Usuario(int idUsuarioObj, String nombreCompletoObj, String usernameObj, String passwordObj, String rolObj) {
        idUsuario = idUsuarioObj;
        nombreCompleto = nombreCompletoObj;
        username = usernameObj;
        password = passwordObj;
        rol = rolObj;
    }

    public int getIdUsuario() {
        return idUsuario;
    }
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getRol() {
        return rol;
    }

    public void setIdUsuario(int idUsuarioNuevo) {
        idUsuario = idUsuarioNuevo;
    }
    public void setNombreCompleto(String nombreCompletoNuevo) {
        nombreCompleto = nombreCompletoNuevo;
    }
    public void setUsername(String usernameNuevo) {
        username = usernameNuevo;
    }
    public void setPassword(String passwordNueva) {
        password = passwordNueva;
    }
    public void setRol(String rolNuevo) {
        rol = rolNuevo;
    }

    @Override
    public String toString() {
        return nombreCompleto + "(" + rol + ")";
    }

}
