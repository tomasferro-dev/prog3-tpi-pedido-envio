package prog2int.Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import prog2int.Config.DatabaseConnection;
import prog2int.Models.Envio;

public class EnvioDAO implements GenericDAO<Envio> {

    private static final String INSERT_SQL = "INSERT INTO envios (direccion, fecha_envio, transportista) VALUES (?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE envios SET direccion = ?, fecha_envio = ?, transportista = ? WHERE id = ?";
    private static final String DELETE_SQL = "UPDATE envios SET eliminado = TRUE WHERE id = ?";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM envios WHERE id = ? AND eliminado = FALSE";
    private static final String SELECT_ALL_SQL = "SELECT * FROM envios WHERE eliminado = FALSE";

    @Override
    public void insertar(Envio envio) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setEnvioParameters(stmt, envio);
            stmt.executeUpdate();
            setGeneratedId(stmt, envio);
        }
    }

    @Override
    public void insertTx(Envio envio, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setEnvioParameters(stmt, envio);
            stmt.executeUpdate();
            setGeneratedId(stmt, envio);
        }
    }

    @Override
    public void actualizar(Envio envio) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, envio.getDireccion());
            stmt.setString(2, envio.getFechaEnvio());
            stmt.setString(3, envio.getTransportista());
            stmt.setInt(4, envio.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el envío con ID: " + envio.getId());
            }
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró envío con ID: " + id);
            }
        }
    }

    @Override
    public Envio getById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEnvio(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Envio> getAll() throws SQLException {
        List<Envio> envios = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                envios.add(mapResultSetToEnvio(rs));
            }
        }
        return envios;
    }

    private void setEnvioParameters(PreparedStatement stmt, Envio envio) throws SQLException {
        stmt.setString(1, envio.getDireccion());
        stmt.setString(2, envio.getFechaEnvio());
        stmt.setString(3, envio.getTransportista());
    }

    private void setGeneratedId(PreparedStatement stmt, Envio envio) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                envio.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción del envío falló, no se obtuvo ID generado");
            }
        }
    }

    private Envio mapResultSetToEnvio(ResultSet rs) throws SQLException {
        return new Envio(
            rs.getInt("id"),
            rs.getString("direccion"),
            rs.getString("fecha_envio"),
            rs.getString("transportista")
        );
    }
}