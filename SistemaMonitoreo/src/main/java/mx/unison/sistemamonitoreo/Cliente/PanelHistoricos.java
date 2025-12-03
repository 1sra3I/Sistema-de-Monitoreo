/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package mx.unison.sistemamonitoreo.Cliente;

import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import mx.unison.sistemamonitoreo.Servidor.EncriptacionUtil;


public class PanelHistoricos extends javax.swing.JPanel {

    private MainFrame mainFrame;
    private XYSeries seriesX, seriesY, seriesZ;
    private ChartPanel chartPanel;
    
    public PanelHistoricos() {
        initComponents();
        personalizarComponentes();
    }

    public PanelHistoricos(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
        configurarGrafica();
        configurarFechasIniciales();
        personalizarComponentes();
    }

    private void personalizarComponentes() {
        // Botones
        estilizarBoton(btnVolver, new Color(1, 82, 148), 4);
        estilizarBoton(btnConsultar, new Color(1, 82, 148), 4);
        estilizarBoton(btnLimpiar, new Color(217, 158, 48), 4);
        btnVolver.setFocusPainted(false);
        btnConsultar.setFocusPainted(false);
        btnLimpiar.setFocusPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConsultar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setForeground(Color.WHITE);
        btnConsultar.setForeground(Color.WHITE);
        btnLimpiar.setForeground(Color.WHITE);

        dateChooserInicio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));
        dateChooserFin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));
        spinnerHoraInicio.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        spinnerHoraFin.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
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
        boton.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        boton.setPreferredSize(new Dimension(90, 28)); // aquí ajustas el tamaño final
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
                "Datos Históricos", "Registro", "Valor", dataset);

        chart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        plot.getRenderer().setSeriesPaint(0, Color.RED);
        plot.getRenderer().setSeriesPaint(1, Color.GREEN);
        plot.getRenderer().setSeriesPaint(2, Color.BLUE);

        chartPanel = new ChartPanel(chart);
        panelGrafica.setLayout(new BorderLayout());
        panelGrafica.add(chartPanel, BorderLayout.CENTER);
        panelGrafica.revalidate();
    }

    private void configurarFechasIniciales() {
        Date hoy = new Date();
        dateChooserInicio.setDate(hoy);
        dateChooserFin.setDate(hoy);

        dateChooserInicio.setDateFormatString("yyyy-MM-dd");
        dateChooserFin.setDateFormatString("yyyy-MM-dd");

        SpinnerDateModel modelInicio = new SpinnerDateModel();
        spinnerHoraInicio.setModel(modelInicio);
        JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(
                spinnerHoraInicio, "HH:mm:ss");
        spinnerHoraInicio.setEditor(editorInicio);

        SpinnerDateModel modelFin = new SpinnerDateModel();
        spinnerHoraFin.setModel(modelFin);
        JSpinner.DateEditor editorFin = new JSpinner.DateEditor(
                spinnerHoraFin, "HH:mm:ss");
        spinnerHoraFin.setEditor(editorFin);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            spinnerHoraInicio.setValue(sdf.parse("00:00:00"));
            spinnerHoraFin.setValue(sdf.parse("23:59:59"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void consultarDatos() {
        lblEstado.setText("Cargando datos de la base de datos...");
        lblEstado.setForeground(MainFrame.AZUL_UNISON);
        btnConsultar.setEnabled(false);

        Thread consultaThread = new Thread(() -> {
            try {
                SimpleDateFormat sdfFecha = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss");

                Date fechaInicioDate = dateChooserInicio.getDate();
                Date fechaFinDate = dateChooserFin.getDate();

                if (fechaInicioDate == null || fechaFinDate == null) {
                    SwingUtilities.invokeLater(() -> {
                        lblEstado.setText("Seleccione fechas válidas");
                        lblEstado.setForeground(Color.RED);
                    });
                    return;
                }

                String fechaInicio = sdfFecha.format(fechaInicioDate);
                String horaInicio = sdfHora.format(spinnerHoraInicio.getValue());
                String fechaFin = sdfFecha.format(fechaFinDate);
                String horaFin = sdfHora.format(spinnerHoraFin.getValue());

                String consulta = String.format("CONSULTAR|%s|%s|%s|%s",
                        fechaInicio, horaInicio, fechaFin, horaFin);

                String respuesta = consultarServidor(consulta);

                if (respuesta != null && !respuesta.isEmpty()) {
                    procesarRespuesta(respuesta);
                    SwingUtilities.invokeLater(() -> {
                        lblEstado.setText("Datos de la base de datos cargados");
                        lblEstado.setForeground(new Color(0, 150, 0));
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        lblEstado.setText("No se encontraron datos en el rango");
                        lblEstado.setForeground(Color.RED);
                        limpiarGrafica();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    lblEstado.setText("Error al consultar datos: " + e.getMessage());
                    lblEstado.setForeground(Color.RED);
                });
            } finally {
                SwingUtilities.invokeLater(() -> btnConsultar.setEnabled(true));
            }
        });
        consultaThread.start();
    }

    private String consultarServidor(String consulta) {
    try (Socket socket = ConexionServidor.conectar();
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

        String consultaEnc = EncriptacionUtil.encriptar(consulta);
        out.println(consultaEnc);

        String respuestaEnc = in.readLine();
        if (respuestaEnc != null) {
            return EncriptacionUtil.desencriptar(respuestaEnc);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

    private void procesarRespuesta(String respuesta) {
        SwingUtilities.invokeLater(() -> {
            seriesX.clear();
            seriesY.clear();
            seriesZ.clear();

            String[] registros = respuesta.split(";");
            int contador = 0;

            for (String registro : registros) {
                if (!registro.isEmpty()) {
                    String[] valores = registro.split("\\|");
                    if (valores.length >= 3) {
                        try {
                            int x = Integer.parseInt(valores[0]);
                            int y = Integer.parseInt(valores[1]);
                            int z = Integer.parseInt(valores[2]);
                            seriesX.add(contador, x);
                            seriesY.add(contador, y);
                            seriesZ.add(contador, z);
                            contador++;
                        } catch (NumberFormatException e) {
                            System.err.println("Error parseando valores: " + registro);
                        }
                    }
                }
            }
        });
    }

    private void limpiarGrafica() {
        seriesX.clear();
        seriesY.clear();
        seriesZ.clear();
        lblEstado.setText("Gráfica limpiada");
        lblEstado.setForeground(Color.GRAY);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        panelGrafica = new javax.swing.JPanel();
        panelFiltros = new javax.swing.JPanel();
        panelFecha = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        dateChooserInicio = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        spinnerHoraInicio = new javax.swing.JSpinner();
        panelFechaFin = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        dateChooserFin = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        spinnerHoraFin = new javax.swing.JSpinner();
        panel1 = new java.awt.Panel();
        btnConsultar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        panelCabeza = new javax.swing.JPanel();
        btnVolver = new javax.swing.JButton();
        lblTitulo = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        panelGrafica.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelGraficaLayout = new javax.swing.GroupLayout(panelGrafica);
        panelGrafica.setLayout(panelGraficaLayout);
        panelGraficaLayout.setHorizontalGroup(
            panelGraficaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelGraficaLayout.setVerticalGroup(
            panelGraficaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 345, Short.MAX_VALUE)
        );

        panelFiltros.setBackground(new java.awt.Color(255, 255, 255));
        panelFiltros.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(248, 187, 0)), "Filtros de Consulta", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11), new java.awt.Color(1, 82, 148))); // NOI18N
        panelFiltros.setLayout(new java.awt.GridLayout(3, 1, 5, 2));

        panelFecha.setBackground(new java.awt.Color(255, 255, 255));
        panelFecha.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 30, 5));

        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Fecha Inicio:");
        panelFecha.add(jLabel1);
        panelFecha.add(dateChooserInicio);

        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Hora:");
        panelFecha.add(jLabel2);
        panelFecha.add(spinnerHoraInicio);

        panelFiltros.add(panelFecha);

        panelFechaFin.setBackground(new java.awt.Color(255, 255, 255));
        panelFechaFin.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 30, 5));

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Fecha Fin:");
        panelFechaFin.add(jLabel3);
        panelFechaFin.add(dateChooserFin);

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Hora:");
        panelFechaFin.add(jLabel4);
        panelFechaFin.add(spinnerHoraFin);

        panelFiltros.add(panelFechaFin);

        panel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 5));

        btnConsultar.setBackground(new java.awt.Color(1, 82, 148));
        btnConsultar.setText("Consultar");
        btnConsultar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsultarActionPerformed(evt);
            }
        });
        panel1.add(btnConsultar);

        btnLimpiar.setBackground(new java.awt.Color(1, 82, 148));
        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });
        panel1.add(btnLimpiar);

        panelFiltros.add(panel1);

        panelCabeza.setBackground(new java.awt.Color(248, 187, 0));
        panelCabeza.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 40, 25));

        btnVolver.setBackground(new java.awt.Color(1, 82, 148));
        btnVolver.setText("Volver");
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });
        panelCabeza.add(btnVolver);

        lblTitulo.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(0, 0, 0));
        lblTitulo.setText("HISTORICO DE DATOS");
        panelCabeza.add(lblTitulo);

        lblEstado.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(0, 0, 0));
        lblEstado.setText("Seleccione fechas y presione Consultar");
        panelCabeza.add(lblEstado);

        jPanel5.setBackground(new java.awt.Color(1, 82, 148));
        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 40, 20));

        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ubicacionesv.png"))); // NOI18N
        jLabel6.setText("Blvd. Luis Encinas J, Calle Av. Rosales &, Centro, 83000 Hermosillo, Son.");
        jPanel5.add(jLabel6);

        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Gmail-Email-PNG-Pic (1).png"))); // NOI18N
        jLabel7.setText("comunicacion@unison.mx");
        jPanel5.add(jLabel7);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCabeza, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelGrafica, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 909, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelFiltros, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(panelCabeza, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47)
                .addComponent(panelGrafica, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        // TODO add your handling code here:
        consultarDatos();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
        limpiarGrafica();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        // TODO add your handling code here:
        mainFrame.mostrarPanel("INICIO");
    }//GEN-LAST:event_btnVolverActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnVolver;
    private com.toedter.calendar.JDateChooser dateChooserFin;
    private com.toedter.calendar.JDateChooser dateChooserInicio;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblTitulo;
    private java.awt.Panel panel1;
    private javax.swing.JPanel panelCabeza;
    private javax.swing.JPanel panelFecha;
    private javax.swing.JPanel panelFechaFin;
    private javax.swing.JPanel panelFiltros;
    private javax.swing.JPanel panelGrafica;
    private javax.swing.JSpinner spinnerHoraFin;
    private javax.swing.JSpinner spinnerHoraInicio;
    // End of variables declaration//GEN-END:variables
}
