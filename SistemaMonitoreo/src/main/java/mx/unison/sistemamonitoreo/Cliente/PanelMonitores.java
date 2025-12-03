/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mx.unison.sistemamonitoreo.Cliente;

import com.fazecast.jSerialComm.SerialPort;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import com.toedter.calendar.JDateChooser;
import mx.unison.sistemamonitoreo.Servidor.EncriptacionUtil;

/**
 *
 * @author rober
 */
public class PanelMonitores extends javax.swing.JPanel {

    private MainFrame mainFrame;
    private SerialPort puertoSerial;
    private SimuladorArduino simulador;
    private Thread lecturaThread;
    private volatile boolean leyendo = false;
    private boolean usarSimulador = false;

    private XYSeries seriesX, seriesY, seriesZ;
    private ChartPanel chartPanel;
    private int contadorDatos = 0;
    private static final int MAX_DATOS_GRAFICA = 50;
    

    /**
     * Creates new form PanelMonitores
     */
    public PanelMonitores() {
        initComponents();
        personalizarComponentes();
    }

    public PanelMonitores(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
        configurarGrafica();
        actualizarPuertos();
        personalizarComponentes();
    }

    private void personalizarComponentes() {
        // Estilos de botones
        estilizarBoton(btnVolver, new Color(1, 82, 148), 4);
        estilizarBoton(btnActualizar, new Color(217, 158, 48), 4);
        estilizarBoton(btnIniciar, new Color(40, 167, 69), 4);
        btnVolver.setFocusPainted(false);
        btnActualizar.setFocusPainted(false);
        btnIniciar.setFocusPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setPreferredSize(new Dimension(100, 35));
        btnActualizar.setPreferredSize(new Dimension(110, 35));
        btnIniciar.setPreferredSize(new Dimension(100, 35));
        Font fuenteBoton = new Font("Segoe UI", Font.BOLD, 13);
        btnVolver.setFont(fuenteBoton);
        btnActualizar.setFont(fuenteBoton);
        btnIniciar.setFont(fuenteBoton);
        btnVolver.setForeground(Color.WHITE);
        btnActualizar.setForeground(Color.WHITE);
        btnIniciar.setForeground(Color.WHITE);

        comboPuertos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    private void estilizarBoton(JButton boton, Color colorFondo, int radio) {
        boton.setBackground(colorFondo);
        boton.setForeground(Color.WHITE);
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);

        boton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                JButton button = (JButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(button.getBackground());
                g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), radio * 2, radio * 2);

                g2.dispose();

                super.paint(g, c);
            }
        });

        boton.setBorder(BorderFactory.createEmptyBorder(radio + 5, radio + 10, radio + 5, radio + 10));
    }

    private void agregarEfectoHover(JButton boton, Color colorNormal, Color colorHover) {
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorHover);
                boton.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorNormal);
                boton.repaint();
            }
        });
    }

    private void configurarGrafica() {
        seriesX = new XYSeries("X");
        seriesY = new XYSeries("Y");
        seriesZ = new XYSeries("Z");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Monitoreo en Tiempo Real", "Tiempo (s)", "Valor", dataset);

        chart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        plot.getRenderer().setSeriesPaint(0, Color.RED);
        plot.getRenderer().setSeriesPaint(1, Color.GREEN);
        plot.getRenderer().setSeriesPaint(2, Color.BLUE);

        chartPanel = new ChartPanel(chart);
        panelGrafica.removeAll();
        panelGrafica.setLayout(new BorderLayout());
        panelGrafica.add(chartPanel, BorderLayout.CENTER);
        panelGrafica.revalidate();
        panelGrafica.repaint();

        System.out.println(" Gráfica configurada");
    }

    private void actualizarPuertos() {
        comboPuertos.removeAllItems();
        comboPuertos.addItem("SIMULADOR (Sin Arduino)");

        SerialPort[] puertos = SerialPort.getCommPorts();
        if (puertos.length == 0) {
            comboPuertos.addItem("Sin puertos disponibles");
        } else {
            for (SerialPort puerto : puertos) {
                comboPuertos.addItem(puerto.getSystemPortName());
            }
        }
    }

    private void toggleLectura() {
        if (!leyendo) {
            iniciarLectura();
        } else {
            detenerLectura();
        }
    }

    private void iniciarLectura() {
        if (comboPuertos.getSelectedItem() == null
                || comboPuertos.getSelectedItem().toString().equals("Sin puertos disponibles")) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un puerto válido",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String puertoSeleccionado = comboPuertos.getSelectedItem().toString();

        if (puertoSeleccionado.equals("SIMULADOR (Sin Arduino)")) {
            usarSimulador = true;
            iniciarSimulador();
            return;
        }

        usarSimulador = false;
        puertoSerial = SerialPort.getCommPort(puertoSeleccionado);
        puertoSerial.setBaudRate(9600);
        puertoSerial.setNumDataBits(8);
        puertoSerial.setNumStopBits(1);
        puertoSerial.setParity(SerialPort.NO_PARITY);

        puertoSerial.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                1000,
                0
        );

        if (puertoSerial.openPort()) {
            System.out.println(" Puerto " + puertoSeleccionado + " abierto");

            try {
                System.out.println(" Esperando inicialización del Arduino (2 seg)...");
                Thread.sleep(2000);

                InputStream input = puertoSerial.getInputStream();
                int bytesDescartados = 0;
                while (input.available() > 0) {
                    input.read();
                    bytesDescartados++;
                }
                System.out.println(" Buffer limpiado (" + bytesDescartados + " bytes descartados)");

            } catch (Exception e) {
                e.printStackTrace();
            }

            leyendo = true;
            btnIniciar.setText(" Detener");
            comboPuertos.setEnabled(false);

            seriesX.clear();
            seriesY.clear();
            seriesZ.clear();
            contadorDatos = 0;

            lecturaThread = new Thread(this::leerDatos);
            lecturaThread.start();

            lblEstado.setText("Leyendo en " + puertoSeleccionado + "...");
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir el puerto. Cierra Arduino IDE si está abierto.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void iniciarSimulador() {
        leyendo = true;
        btnIniciar.setText(" Detener");
        comboPuertos.setEnabled(false);

        seriesX.clear();
        seriesY.clear();
        seriesZ.clear();
        contadorDatos = 0;

        simulador = new SimuladorArduino();
        simulador.iniciar();

        lecturaThread = new Thread(this::leerDatosSimulador);
        lecturaThread.start();

        lblEstado.setText("Modo simulación activo");
    }

    public void detenerLectura() {
        if (leyendo) {
            leyendo = false;

            if (usarSimulador && simulador != null) {
                simulador.detener();
                simulador = null;
            } else if (puertoSerial != null && puertoSerial.isOpen()) {
                puertoSerial.closePort();
            }

            btnIniciar.setText(" Iniciar");
            comboPuertos.setEnabled(true);
            lblEstado.setText("Lectura detenida");

            System.out.println(" Lectura detenida");
        }
    }

    private void leerDatos() {
        System.out.println(" Hilo de lectura optimizado iniciado");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(puertoSerial.getInputStream())
        );

        long ultimoUpdate = System.currentTimeMillis();

        while (leyendo) {
            try {
                String linea = reader.readLine();

                if (linea != null && !linea.trim().isEmpty()) {
                    procesarDato(linea);
                }

                if (System.currentTimeMillis() - ultimoUpdate >= 100) {
                    SwingUtilities.invokeLater(() -> {
                        chartPanel.repaint();
                    });
                    ultimoUpdate = System.currentTimeMillis();
                }

            } catch (Exception e) {
                if (leyendo) {
                    System.err.println(" Error leyendo datos: " + e.getMessage());
                }
            }
        }

        try {
            reader.close();
        } catch (Exception ignored) {
        }
        System.out.println(" Hilo de lectura optimizado terminado");
    }

    private void leerDatosSimulador() {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(simulador.getInputStream())
        );

        while (leyendo && simulador != null) {
            try {
                String linea = reader.readLine();
                if (linea != null && !linea.trim().isEmpty()) {
                    System.out.println(" Simulador → [" + linea + "]");
                    procesarDato(linea);
                }
                Thread.sleep(50);
            } catch (Exception e) {
                if (leyendo) {
                    e.printStackTrace();
                }
            }
        }

        try {
            reader.close();
        } catch (Exception e) {
        }
    }

    private void procesarDato(String dato) {
        try {
            dato = dato.trim().replaceAll("\\s+", "");

            System.out.println(" Limpiado: [" + dato + "]");

            if (!dato.contains("x:") || !dato.contains("y:") || !dato.contains("z:")) {
                System.err.println(" Formato inválido");
                return;
            }

            String[] partes = dato.split(",");

            if (partes.length != 3) {
                System.err.println(" Se esperaban 3 partes, se obtuvieron " + partes.length);
                return;
            }

            int x = Integer.parseInt(partes[0].substring(partes[0].indexOf(":") + 1));
            int y = Integer.parseInt(partes[1].substring(partes[1].indexOf(":") + 1));
            int z = Integer.parseInt(partes[2].substring(partes[2].indexOf(":") + 1));

            System.out.println(" X=" + x + ", Y=" + y + ", Z=" + z);

            final int xFinal = x, yFinal = y, zFinal = z;
            SwingUtilities.invokeLater(() -> {
                seriesX.add(contadorDatos, xFinal);
                seriesY.add(contadorDatos, yFinal);
                seriesZ.add(contadorDatos, zFinal);

                if (seriesX.getItemCount() > MAX_DATOS_GRAFICA) {
                    seriesX.remove(0);
                    seriesY.remove(0);
                    seriesZ.remove(0);
                }

                lblEstado.setText(String.format("X=%d, Y=%d, Z=%d", xFinal, yFinal, zFinal));
            });

            contadorDatos++;

            enviarAlServidor(x, y, z);

        } catch (Exception e) {
            System.err.println(" Error procesando: [" + dato + "]");
            e.printStackTrace();
        }
    }

    private void enviarAlServidor(int x, int y, int z) {
    new Thread(() -> {
        try (Socket socket = ConexionServidor.conectar(); 
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String datos = "GUARDAR|" + x + "|" + y + "|" + z;
            String datosEncriptados = EncriptacionUtil.encriptar(datos);
            out.println(datosEncriptados);

            System.out.println("✓ Datos enviados al servidor");

        } catch (Exception e) {
            System.err.println("✗ Error enviando al servidor: " + e.getMessage());
        }
    }).start();
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelControles = new javax.swing.JPanel();
        btnVolver = new javax.swing.JButton();
        lblPuerto = new javax.swing.JLabel();
        comboPuertos = new javax.swing.JComboBox<>();
        btnActualizar = new javax.swing.JButton();
        btnIniciar = new javax.swing.JButton();
        lblEstado = new javax.swing.JLabel();
        panelEstado = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        panelGrafica = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));

        panelControles.setBackground(new java.awt.Color(248, 187, 0));
        panelControles.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 40, 30));

        btnVolver.setBackground(new java.awt.Color(1, 82, 148));
        btnVolver.setText("Volver");
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });
        panelControles.add(btnVolver);

        lblPuerto.setForeground(new java.awt.Color(0, 0, 0));
        lblPuerto.setText("Puerto COM:");
        panelControles.add(lblPuerto);

        comboPuertos.setBackground(new java.awt.Color(1, 82, 148));
        panelControles.add(comboPuertos);

        btnActualizar.setBackground(new java.awt.Color(1, 82, 148));
        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });
        panelControles.add(btnActualizar);

        btnIniciar.setBackground(new java.awt.Color(1, 82, 148));
        btnIniciar.setText("Lectura");
        btnIniciar.setPreferredSize(new java.awt.Dimension(80, 25));
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });
        panelControles.add(btnIniciar);

        lblEstado.setForeground(new java.awt.Color(0, 0, 0));
        lblEstado.setText("Esperando datos...");
        panelControles.add(lblEstado);

        panelEstado.setBackground(new java.awt.Color(1, 82, 148));
        panelEstado.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 35));

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ubicacionesv.png"))); // NOI18N
        jLabel6.setText("Blvd. Luis Encinas J, Calle Av. Rosales &, Centro, 83000 Hermosillo, Son.");
        panelEstado.add(jLabel6);

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Gmail-Email-PNG-Pic (1).png"))); // NOI18N
        jLabel2.setText("  comunicacion@unison.mx");
        panelEstado.add(jLabel2);

        panelGrafica.setBackground(new java.awt.Color(255, 255, 255));
        panelGrafica.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelControles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelEstado, javax.swing.GroupLayout.DEFAULT_SIZE, 839, Short.MAX_VALUE)
            .addComponent(panelGrafica, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelControles, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelGrafica, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        // TODO add your handling code here:
        mainFrame.mostrarPanel("INICIO");
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        // TODO add your handling code here:
        actualizarPuertos();
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarActionPerformed
        // TODO add your handling code here:
        toggleLectura();
    }//GEN-LAST:event_btnIniciarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JComboBox<String> comboPuertos;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblPuerto;
    private javax.swing.JPanel panelControles;
    private javax.swing.JPanel panelEstado;
    private javax.swing.JPanel panelGrafica;
    // End of variables declaration//GEN-END:variables
}
