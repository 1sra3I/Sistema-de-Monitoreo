/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.sistemamonitoreo.Cliente;

import java.io.*;
import java.util.Random;

public class SimuladorArduino {

    private Thread hiloSimulador;
    private volatile boolean ejecutando = false;
    private Random random;
    private PuertoSerialSimulado puertoSimulado;

    public SimuladorArduino() {
        random = new Random();
        puertoSimulado = new PuertoSerialSimulado();
    }

    public void iniciar() {
        if (ejecutando) {
            return;
        }

        ejecutando = true;

        hiloSimulador = new Thread(() -> {
            System.out.println(" Simulador de Arduino iniciado");

            while (ejecutando) {
                try {
                    int x = random.nextInt(101);
                    int y = random.nextInt(101);
                    int z = random.nextInt(101);

                    String datos = String.format("x:%d,y:%d,z:%d", x, y, z);

                    puertoSimulado.escribir(datos + "\n");

                    System.out.println(" Datos generados: " + datos);

                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    System.out.println(" Simulador interrumpido");
                    break;
                } catch (Exception e) {
                    System.err.println("Error en simulador: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println(" Simulador de Arduino detenido");
        });

        hiloSimulador.setDaemon(true);
        hiloSimulador.start();
    }

    public void detener() {
        ejecutando = false;

        if (hiloSimulador != null) {
            hiloSimulador.interrupt();
            try {
                hiloSimulador.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        puertoSimulado.cerrar();
    }

    public InputStream getInputStream() {
        return puertoSimulado.getInputStream();
    }

    public boolean estaEjecutando() {
        return ejecutando;
    }

    private class PuertoSerialSimulado {

        private PipedOutputStream outputStream;
        private PipedInputStream inputStream;

        public PuertoSerialSimulado() {
            try {
                outputStream = new PipedOutputStream();
                inputStream = new PipedInputStream(outputStream, 8192);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void escribir(String datos) {
            try {
                if (outputStream != null) {
                    outputStream.write(datos.getBytes());
                    outputStream.flush();
                }
            } catch (IOException e) {
                System.err.println("Error escribiendo en puerto simulado: " + e.getMessage());
            }
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public void cerrar() {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
