package prog2int.Service;

import java.util.List;
import prog2int.Dao.GenericDAO;
import prog2int.Models.Envio;

public class EnvioServiceImpl implements GenericService<Envio> {

    private final GenericDAO<Envio> envioDAO;

    public EnvioServiceImpl(GenericDAO<Envio> envioDAO) {
        if (envioDAO == null) {
            throw new IllegalArgumentException("EnvioDAO no puede ser null");
        }
        this.envioDAO = envioDAO;
    }

    @Override
    public void insertar(Envio envio) throws Exception {
        validateEnvio(envio);
        envioDAO.insertar(envio);
    }

    @Override
    public void actualizar(Envio envio) throws Exception {
        validateEnvio(envio);
        if (envio.getId() <= 0) {
            throw new IllegalArgumentException("El ID del envío debe ser mayor a 0 para actualizar");
        }
        envioDAO.actualizar(envio);
    }

    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        envioDAO.eliminar(id);
    }

    @Override
    public Envio getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return envioDAO.getById(id);
    }

    @Override
    public List<Envio> getAll() throws Exception {
        return envioDAO.getAll();
    }

    private void validateEnvio(Envio envio) {
        if (envio == null) {
            throw new IllegalArgumentException("El envío no puede ser null");
        }
        if (envio.getDireccion() == null || envio.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección no puede estar vacía");
        }
        if (envio.getFechaEnvio() == null || envio.getFechaEnvio().trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha de envío no puede estar vacía");
        }
    }
}