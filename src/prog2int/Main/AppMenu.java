package prog2int.Main;

import java.util.Scanner;
import prog2int.Dao.EnvioDAO;
import prog2int.Dao.PedidoDAO;
import prog2int.Service.EnvioServiceImpl;
import prog2int.Service.PedidoServiceImpl;

public class AppMenu {

    private final Scanner scanner;
    private final MenuHandler menuHandler;
    private boolean running;

    public AppMenu() {
        this.scanner = new Scanner(System.in);
        PedidoServiceImpl pedidoService = createPedidoService();
        this.menuHandler = new MenuHandler(scanner, pedidoService);
        this.running = true;
    }

    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }

    public void run() {
        while (running) {
            try {
                MenuDisplay.mostrarMenuPrincipal();
                int opcion = Integer.parseInt(scanner.nextLine());
                processOption(opcion);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
            }
        }
        scanner.close();
    }

    private void processOption(int opcion) {
        switch (opcion) {
            case 1 -> menuHandler.crearPedido();
            case 2 -> menuHandler.listarPedidos();
            case 3 -> menuHandler.actualizarPedido();
            case 4 -> menuHandler.eliminarPedido();
            case 5 -> menuHandler.crearEnvioIndependiente();
            case 6 -> menuHandler.listarEnvios();
            case 7 -> menuHandler.actualizarEnvioPorId();
            case 8 -> menuHandler.eliminarEnvioPorId();
            case 9 -> menuHandler.actualizarEnvioPorPedido();
            case 10 -> menuHandler.eliminarEnvioPorPedido();
            case 0 -> {
                System.out.println("Saliendo...");
                running = false;
            }
            default -> System.out.println("Opción no válida.");
        }
    }

    private PedidoServiceImpl createPedidoService() {
        EnvioDAO envioDAO = new EnvioDAO();
        PedidoDAO pedidoDAO = new PedidoDAO(envioDAO);
        EnvioServiceImpl envioService = new EnvioServiceImpl(envioDAO);
        return new PedidoServiceImpl(pedidoDAO, envioService);
    }
}