package prog2int.Service;

import prog2int.Models.Pedido;
import prog2int.Models.Envio;
import prog2int.Dao.PedidoDAO;

import java.util.List;

public class PedidoServiceImpl implements GenericService<Pedido> {

    private final PedidoDAO pedidoDAO;
    private final EnvioServiceImpl envioServiceImpl;

    public PedidoServiceImpl(PedidoDAO pedidoDAO, EnvioServiceImpl envioServiceImpl) {
        if (pedidoDAO == null) {
            throw new IllegalArgumentException("PedidoDAO no puede ser null");
        }
        if (envioServiceImpl == null) {
            throw new IllegalArgumentException("EnvioServiceImpl no puede ser null");
        }
        this.pedidoDAO = pedidoDAO;
        this.envioServiceImpl = envioServiceImpl;
    }

    @Override
    public void insertar(Pedido pedido) throws Exception {
        validatePedido(pedido);
        validateNroPedidoUnique(pedido.getNroPedido(), null);

        if (pedido.getEnvio() != null) {
            if (pedido.getEnvio().getId() == 0) {
                envioServiceImpl.insertar(pedido.getEnvio());
            } else {
                envioServiceImpl.actualizar(pedido.getEnvio());
            }
        }

        pedidoDAO.insertar(pedido);
    }

    @Override
    public void actualizar(Pedido pedido) throws Exception {
        validatePedido(pedido);
        if (pedido.getId() <= 0) {
            throw new IllegalArgumentException("El ID del pedido debe ser mayor a 0 para actualizar");
        }
        validateNroPedidoUnique(pedido.getNroPedido(), pedido.getId());
        pedidoDAO.actualizar(pedido);
    }

    @Override
    public void eliminar(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        pedidoDAO.eliminar(id);
    }

    @Override
    public Pedido getById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a 0");
        }
        return pedidoDAO.getById(id);
    }

    @Override
    public List<Pedido> getAll() throws Exception {
        return pedidoDAO.getAll();
    }

    public EnvioServiceImpl getEnvioService() {
        return this.envioServiceImpl;
    }

    public List<Pedido> buscarPorNroPedidoDescripcion(String filtro) throws Exception {
        if (filtro == null || filtro.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }
        return pedidoDAO.buscarPorNroPedidoDescripcion(filtro);
    }

    public void eliminarEnvioDePedido(int pedidoId, int envioId) throws Exception {
        if (pedidoId <= 0 || envioId <= 0) {
            throw new IllegalArgumentException("Los IDs deben ser mayores a 0");
        }

        Pedido pedido = pedidoDAO.getById(pedidoId);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido no encontrado con ID: " + pedidoId);
        }

        if (pedido.getEnvio() == null || pedido.getEnvio().getId() != envioId) {
            throw new IllegalArgumentException("El envío no pertenece a este pedido");
        }

        pedido.setEnvio(null);
        pedidoDAO.actualizar(pedido);
        envioServiceImpl.eliminar(envioId);
    }

    private void validatePedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser null");
        }
        if (pedido.getNroPedido() == null || pedido.getNroPedido().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de pedido no puede estar vacío");
        }
        if (pedido.getDescripcion() == null || pedido.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        if (pedido.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (pedido.getPrecioUnitario() <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
    }

    private void validateNroPedidoUnique(String nroPedido, Integer pedidoId) throws Exception {
        List<Pedido> existentes = pedidoDAO.buscarPorNroPedidoDescripcion(nroPedido);
        for (Pedido existente : existentes) {
            if (existente.getNroPedido().equals(nroPedido)) {
                if (pedidoId == null || existente.getId() != pedidoId) {
                    throw new IllegalArgumentException("Ya existe un pedido con el número: " + nroPedido);
                }
            }
        }
    }
}
