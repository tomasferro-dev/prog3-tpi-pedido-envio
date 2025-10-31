package prog2int.Models;

import java.util.Objects;

/**
 * Entidad que representa un envío en el sistema.
 * Hereda de Base para obtener id y eliminado.
 *
 * Relación con Pedido:
 * - Un Envío puede estar asociado a múltiples pedidos (relación N:1 desde Pedido)
 *
 * Tabla BD: envios
 * Campos:
 * - id: INT AUTO_INCREMENT PRIMARY KEY (heredado de Base)
 * - direccion: VARCHAR(200) NOT NULL (RN-009)
 * - fecha_envio: DATE NOT NULL (RN-009)
 * - transportista: VARCHAR(100) NULL
 * - eliminado: BOOLEAN DEFAULT FALSE (heredado de Base)
 */
public class Envio extends Base {
    private String direccion;
    private String fechaEnvio;
    private String transportista;

    /** Constructor completo para reconstruir un Envío desde la BD. */
    public Envio(int id, String direccion, String fechaEnvio, String transportista) {
        super(id, false);
        this.direccion = direccion;
        this.fechaEnvio = fechaEnvio;
        this.transportista = transportista;
    }

    /** Constructor por defecto para crear un envío nuevo. */
    public Envio() {
        super();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getTransportista() {
        return transportista;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    @Override
    public String toString() {
        return "Envio{" +
                "id=" + getId() +
                ", direccion='" + direccion + '\'' +
                ", fechaEnvio='" + fechaEnvio + '\'' +
                ", transportista='" + transportista + '\'' +
                ", eliminado=" + isEliminado() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Envio)) return false;
        Envio envio = (Envio) o;
        return Objects.equals(direccion, envio.direccion) &&
               Objects.equals(fechaEnvio, envio.fechaEnvio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(direccion, fechaEnvio);
    }
}