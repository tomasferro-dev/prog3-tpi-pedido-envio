package prog2int.Models;

import java.util.Objects;

/**
 * Entidad que representa un pedido en el sistema.
 * Hereda de Base para obtener id y eliminado.
 *
 * Relación con Envío:
 * - Un Pedido puede tener 0 o 1 Envío (relación opcional)
 * - Se relaciona mediante FK envio_id en la tabla pedidos
 *
 * Tabla BD: pedidos
 * Campos:
 * - id: INT AUTO_INCREMENT PRIMARY KEY (heredado de Base)
 * - nro_pedido: VARCHAR(30) NOT NULL UNIQUE (RN-002)
 * - descripcion: VARCHAR(200) NOT NULL (RN-001)
 * - cantidad: INT NOT NULL
 * - precio_unitario: DECIMAL(12,2) NOT NULL
 * - estado: VARCHAR(20) DEFAULT 'PENDIENTE'
 * - envio_id: INT NULL (FK a envios)
 * - eliminado: BOOLEAN DEFAULT FALSE (heredado de Base)
 */
public class Pedido extends Base {
    private String nroPedido;
    private String descripcion;
    private int cantidad;
    private double precioUnitario;
    private String estado;
    private Envio envio;

    /** Constructor completo para reconstruir un Pedido desde la BD. */
    public Pedido(int id, String nroPedido, String descripcion, int cantidad, double precioUnitario, String estado) {
        super(id, false);
        this.nroPedido = nroPedido;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.estado = estado;
    }

    /** Constructor por defecto para crear un pedido nuevo sin ID. */
    public Pedido() {
        super();
        this.estado = "PENDIENTE";
    }

    public String getNroPedido() {
        return nroPedido;
    }

    public void setNroPedido(String nroPedido) {
        this.nroPedido = nroPedido;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Envio getEnvio() {
        return envio;
    }

    public void setEnvio(Envio envio) {
        this.envio = envio;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + getId() +
                ", nroPedido='" + nroPedido + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", estado='" + estado + '\'' +
                ", envio=" + envio +
                ", eliminado=" + isEliminado() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pedido)) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(nroPedido, pedido.nroPedido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nroPedido);
    }
}