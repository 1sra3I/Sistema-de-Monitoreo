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
 private static final int PUERTO = 5000;
    private static DatabaseManager dbManager;
    
    public static void main(String[] args) {
        System.out.println("    SERVIDOR DE MONITOREO - UNIVERSIDAD DE SONORA");
        System.out.println();
        
        try {
            dbManager = new DatabaseManager();
            System.out.println(" Base de datos inicializada correctamente");
            System.out.println();
        } catch (Exception e) {
            System.err.println(" Error al inicializar la base de datos:");
            e.printStackTrace();
            return;
        }
        
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println(" Servidor iniciado en el puerto " + PUERTO);
            System.out.println(" Esperando conexiones de clientes...");
            System.out.println("───────────────────────────────────────────────────────");
            System.out.println();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> manejarCliente(clientSocket));
                clientThread.start();
            }
            
        } catch (IOException e) {
            System.err.println(" Error en el servidor:");
            e.printStackTrace();
        }
    }
    
    private static void manejarCliente(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        
        System.out.println("┌─ [" + timestamp + "]");
        System.out.println("│  Nuevo cliente conectado desde: " + clientAddress);
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String mensajeEncriptado = in.readLine();
            
            System.out.println("│ Mensaje ENCRIPTADO recibido:");
            System.out.println("│    " + mensajeEncriptado);
        
        if (mensajeEncriptado != null) {
            String mensaje = EncriptacionUtil.desencriptar(mensajeEncriptado);
            
            System.out.println("│ Mensaje DESENCRIPTADO:");
            System.out.println("│    " + mensaje);
            
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
                e.printStackTrace();
            }
            System.out.println(" Cliente desconectado: " + clientAddress);
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
                
                System.out.println("│  Datos guardados:");
                System.out.println("│    X=" + x + ", Y=" + y + ", Z=" + z);
                System.out.println("│    Fecha: " + fecha + " " + hora);
            }
        } catch (Exception e) {
            System.out.println("│  Error al guardar datos: " + e.getMessage());
            e.printStackTrace();
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
                
                System.out.println("│ Consulta de datos:");
                System.out.println("│    Inicio: " + fechaInicio + " " + horaInicio);
                System.out.println("│    Fin: " + fechaFin + " " + horaFin);
                
                var registros = dbManager.consultarDatos(fechaInicio, horaInicio, fechaFin, horaFin);
                
                for (var registro : registros) {
                    resultado.append(registro.x).append("|")
                            .append(registro.y).append("|")
                            .append(registro.z).append("|")
                            .append(registro.fecha).append("|")
                            .append(registro.hora).append(";");
                }
                
                System.out.println("│  Se encontraron " + registros.size() + " registros");
                System.out.println("│  Enviando datos al cliente...");
            }
        } catch (Exception e) {
            System.out.println("│  Error al consultar datos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return resultado.toString();
    }
}
