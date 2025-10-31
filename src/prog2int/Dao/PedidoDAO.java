package prog2int.Dao;

import prog2int.Models.Pedido;
import prog2int.Models.Envio;
import prog2int.Config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO implements GenericDAO<Pedido> {

    private static final String INSERT_SQL = "INSERT INTO pedidos (nro_pedido, descripcion, cantidad, precio_unitario, estado, envio_id) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE pedidos SET nro_pedido = ?, descripcion = ?, cantidad = ?, precio_unitario = ?, estado = ?, envio_id = ? WHERE id = ?";
    private static final String DELETE_SQL = "UPDATE pedidos SET eliminado = TRUE WHERE id = ?";
    private static final String SELECT_BY_ID_SQL = "SELECT p.id, p.nro_pedido, p.descripcion, p.cantidad, p.precio_unitario, p.estado, p.envio_id, e.id AS envio_id, e.direccion, e.fecha_envio, e.transportista FROM pedidos p LEFT JOIN envios e ON p.envio_id = e.id WHERE p.id = ? AND p.eliminado = FALSE";
    private static final String SELECT_ALL_SQL = "SELECT p.id, p.nro_pedido, p.descripcion, p.cantidad, p.precio_unitario, p.estado, p.envio_id, e.id AS envio_id, e.direccion, e.fecha_envio, e.transportista FROM pedidos p LEFT JOIN envios e ON p.envio_id = e.id WHERE p.eliminado = FALSE";
    private static final String SEARCH_BY_NRO_DESC_SQL = "SELECT p.id, p.nro_pedido, p.descripcion, p.cantidad, p.precio_unitario, p.estado, p.envio_id, e.id AS envio_id, e.direccion, e.fecha_envio, e.transportista FROM pedidos p LEFT JOIN envios e ON p.envio_id = e.id WHERE p.eliminado = FALSE AND (p.nro_pedido = ? OR LOWER(p.descripcion) LIKE ?)";

    private final EnvioDAO envioDAO;

    public PedidoDAO(EnvioDAO envioDAO) {
        if (envioDAO == null) {
            throw new IllegalArgumentException("EnvioDAO no puede ser null");
        }
        this.envioDAO = envioDAO;
    }

    @Override
    public void insertar(Pedido pedido) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setPedidoParameters(stmt, pedido);
            stmt.executeUpdate();
            setGeneratedId(stmt, pedido);
        }
    }

    @Override
    public void insertTx(Pedido pedido, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setPedidoParameters(stmt, pedido);
            stmt.executeUpdate();
            setGeneratedId(stmt, pedido);
        }
    }

    @Override
    public void actualizar(Pedido pedido) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, pedido.getNroPedido());
            stmt.setString(2, pedido.getDescripcion());
            stmt.setInt(3, pedido.getCantidad());
            stmt.setDouble(4, pedido.getPrecioUnitario());
            stmt.setString(5, pedido.getEstado());
            setEnvioId(stmt, 6, pedido.getEnvio());
            stmt.setInt(7, pedido.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el pedido con ID: " + pedido.getId());
            }
        }
    }

    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró pedido con ID: " + id);
            }
        }
    }

    @Override
    public Pedido getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPedido(rs);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener pedido por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Pedido> getAll() throws Exception {
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                pedidos.add(mapResultSetToPedido(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener todos los pedidos: " + e.getMessage(), e);
        }
        return pedidos;
    }

    public List<Pedido> buscarPorNroPedidoDescripcion(String filtro) throws SQLException {
        if (filtro == null || filtro.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_NRO_DESC_SQL)) {
            stmt.setString(1, filtro.trim());
            stmt.setString(2, "%" + filtro.trim().toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapResultSetToPedido(rs));
                }
            }
        }
        return pedidos;
    }

    private void setPedidoParameters(PreparedStatement stmt, Pedido pedido) throws SQLException {
        stmt.setString(1, pedido.getNroPedido());
        stmt.setString(2, pedido.getDescripcion());
        stmt.setInt(3, pedido.getCantidad());
        stmt.setDouble(4, pedido.getPrecioUnitario());
        stmt.setString(5, pedido.getEstado());
        setEnvioId(stmt, 6, pedido.getEnvio());
    }

    private void setEnvioId(PreparedStatement stmt, int parameterIndex, Envio envio) throws SQLException {
        if (envio != null && envio.getId() > 0) {
            stmt.setInt(parameterIndex, envio.getId());
        } else {
            stmt.setNull(parameterIndex, Types.INTEGER);
        }
    }

    private void setGeneratedId(PreparedStatement stmt, Pedido pedido) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                pedido.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción del pedido falló, no se obtuvo ID generado");
            }
        }
    }

    private Pedido mapResultSetToPedido(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setId(rs.getInt("id"));
        pedido.setNroPedido(rs.getString("nro_pedido"));
        pedido.setDescripcion(rs.getString("descripcion"));
        pedido.setCantidad(rs.getInt("cantidad"));
        pedido.setPrecioUnitario(rs.getDouble("precio_unitario"));
        pedido.setEstado(rs.getString("estado"));
        int envioId = rs.getInt("envio_id");
        if (envioId > 0 && !rs.wasNull()) {
            Envio envio = new Envio();
            envio.setId(rs.getInt("envio_id"));
            envio.setDireccion(rs.getString("direccion"));
            envio.setFechaEnvio(rs.getString("fecha_envio"));
            envio.setTransportista(rs.getString("transportista"));
            pedido.setEnvio(envio);
        }
        return pedido;
    }
}