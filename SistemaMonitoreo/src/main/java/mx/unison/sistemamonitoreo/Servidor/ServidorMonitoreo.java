/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.sistemamonitoreo.Servidor;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServidorMonitoreo {

    private static final int PUERTO_INICIAL = 5000;
    private static final int PUERTO_FINAL = 5010;
    private static DatabaseManager dbManager;
    private static int puertoActual = PUERTO_INICIAL;

    public static void main(String[] args) {
        // Suprimir warnings de SLF4J y System
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");
        System.setProperty("java.util.logging.config.file", "logging.properties");

        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("    SERVIDOR DE MONITOREO - UNIVERSIDAD DE SONORA");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();

        try {
            dbManager = new DatabaseManager();
            System.out.println("✓ Base de datos inicializada correctamente");
            System.out.println();
        } catch (Exception e) {
            System.err.println("✗ Error al inicializar la base de datos:");
            e.printStackTrace();
            return;
        }

        // Buscar puerto disponible automáticamente
        ServerSocket serverSocket = null;
        boolean puertoEncontrado = false;

        for (int puerto = PUERTO_INICIAL; puerto <= PUERTO_FINAL; puerto++) {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress("0.0.0.0", puerto));
                puertoActual = puerto;
                puertoEncontrado = true;
                break;
            } catch (IOException e) {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException ex) {
                    }
                }
                serverSocket = null;
            }
        }

        if (!puertoEncontrado) {
            System.err.println("═══════════════════════════════════════════════════");
            System.err.println("✗ ERROR: Todos los puertos están ocupados");
            System.err.println("═══════════════════════════════════════════════════");
            System.err.println();
            System.err.println("Puertos intentados: " + PUERTO_INICIAL + " - " + PUERTO_FINAL);
            System.err.println();
            System.err.println("SOLUCIÓN:");
            System.err.println("1. Cierra otros servidores");
            System.err.println("2. Ejecuta en CMD como Administrador:");
            System.err.println("   FOR /L %i IN (" + PUERTO_INICIAL + ",1," + PUERTO_FINAL + ") DO (");
            System.err.println("     netstat -ano | findstr :%i");
            System.err.println("   )");
            return;
        }

        System.out.println("✓ Servidor iniciado en puerto: " + puertoActual);
        System.out.println("✓ Esperando conexiones de clientes...");
        System.out.println("───────────────────────────────────────────────────────");
        System.out.println();

        // Agregar shutdown hook para cerrar limpiamente
        final ServerSocket finalServerSocket = serverSocket;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nCerrando servidor...");
            try {
                if (finalServerSocket != null && !finalServerSocket.isClosed()) {
                    finalServerSocket.close();
                }
                if (dbManager != null) {
                    dbManager.cerrarConexion();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Servidor cerrado correctamente");
        }));

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> manejarCliente(clientSocket));
                clientThread.setDaemon(true);
                clientThread.start();
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                System.err.println("✗ Error en el servidor:");
                e.printStackTrace();
            }
        }
    }

    private static void manejarCliente(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        System.out.println("┌─ [" + timestamp + "]");
        System.out.println("│  Nuevo cliente conectado desde: " + clientAddress);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String mensajeEncriptado = in.readLine();

            if (mensajeEncriptado != null && !mensajeEncriptado.isEmpty()) {
                System.out.println("│  Mensaje recibido (encriptado)");

                String mensaje = EncriptacionUtil.desencriptar(mensajeEncriptado);
                System.out.println("│  Mensaje desencriptado: " + mensaje.substring(0, Math.min(50, mensaje.length())) + "...");

                String[] partes = mensaje.split("\\|");

                if (partes.length > 0) {
                    String operacion = partes[0];

                    switch (operacion) {
                        case "GUARDAR":
                            manejarGuardado(partes, clientAddress);
                            break;

                        case "CONSULTAR":
                            String respuesta = manejarConsulta(partes, clientAddress);
                            String respuestaEncriptada = EncriptacionUtil.encriptar(respuesta);
                            out.println(respuestaEncriptada);
                            break;

                        default:
                            System.out.println("│  Operación desconocida: " + operacion);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("│  Error al procesar solicitud del cliente " + clientAddress);
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
            }
            System.out.println("└─ Cliente desconectado: " + clientAddress);
            System.out.println();
        }
    }

    private static void manejarGuardado(String[] datos, String clientAddress) {
        try {
            if (datos.length >= 4) {
                int x = Integer.parseInt(datos[1]);
                int y = Integer.parseInt(datos[2]);
                int z = Integer.parseInt(datos[3]);

                SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss");
                Date ahora = new Date();

                String fecha = sdfFecha.format(ahora);
                String hora = sdfHora.format(ahora);

                dbManager.insertarDato(x, y, z, fecha, hora);

                System.out.println("│  ✓ Datos guardados: X=" + x + ", Y=" + y + ", Z=" + z);
            }
        } catch (Exception e) {
            System.out.println("│  ✗ Error al guardar datos: " + e.getMessage());
        }
    }

    private static String manejarConsulta(String[] datos, String clientAddress) {
        StringBuilder resultado = new StringBuilder();
        try {
            if (datos.length >= 5) {
                String fechaInicio = datos[1];
                String horaInicio = datos[2];
                String fechaFin = datos[3];
                String horaFin = datos[4];

                System.out.println("│  Consultando: " + fechaInicio + " " + horaInicio + " → " + fechaFin + " " + horaFin);

                var registros = dbManager.consultarDatos(fechaInicio, horaInicio, fechaFin, horaFin);

                for (var registro : registros) {
                    resultado.append(registro.x).append("|")
                            .append(registro.y).append("|")
                            .append(registro.z).append("|")
                            .append(registro.fecha).append("|")
                            .append(registro.hora).append(";");
                }

                System.out.println("│  ✓ Registros encontrados: " + registros.size());
            }
        } catch (Exception e) {
            System.out.println("│  ✗ Error al consultar: " + e.getMessage());
        }

        return resultado.toString();
    }

    // Método para obtener el puerto actual (útil para el cliente)
    public static int getPuertoActual() {
        return puertoActual;
    }
}
