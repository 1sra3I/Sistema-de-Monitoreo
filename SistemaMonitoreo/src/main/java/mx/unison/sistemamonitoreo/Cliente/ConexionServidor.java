/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.sistemamonitoreo.Cliente;

import java.io.*;
import java.net.Socket;

public class ConexionServidor {
    private static final String SERVER_HOST = "localhost";
    private static final int PUERTO_INICIAL = 5000;
    private static final int PUERTO_FINAL = 5010;
    private static Integer puertoServidor = null;

    public static int detectarPuertoServidor() {
        if (puertoServidor != null) {
            return puertoServidor;
        }

        for (int puerto = PUERTO_INICIAL; puerto <= PUERTO_FINAL; puerto++) {
            try (Socket socket = new Socket(SERVER_HOST, puerto)) {
                puertoServidor = puerto;
                System.out.println(" Servidor detectado en puerto: " + puerto);
                return puerto;
            } catch (IOException e) {
               
            }
        }

        System.err.println(" No se pudo conectar al servidor");
        return -1;
    }

    public static Socket conectar() throws IOException {
        int puerto = detectarPuertoServidor();
        if (puerto == -1) {
            throw new IOException("Servidor no disponible");
        }
        return new Socket(SERVER_HOST, puerto);
    }
}
