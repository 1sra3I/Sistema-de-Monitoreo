/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.unison.sistemamonitoreo.Servidor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {
    
    private static final String DB_URL = "jdbc:sqlite:monitorBD.db";
    private Connection connection;
    
    public DatabaseManager() throws SQLException {
        inicializarBaseDatos();
    }
    
    private void inicializarBaseDatos() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        
        String sqlCrearTabla = """
            CREATE TABLE IF NOT EXISTS datos_sensor (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL,
                z INTEGER NOT NULL,
                fecha_de_captura TEXT NOT NULL,
                hora_de_captura TEXT NOT NULL
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlCrearTabla);
        }
    }
    
    public void insertarDato(int x, int y, int z, String fecha, String hora) throws SQLException {
        String sql = """
            INSERT INTO datos_sensor (x, y, z, fecha_de_captura, hora_de_captura)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            pstmt.setInt(3, z);
            pstmt.setString(4, fecha);
            pstmt.setString(5, hora);
            
            pstmt.executeUpdate();
        }
    }
    
    public List<RegistroSensor> consultarDatos(String fechaInicio, String horaInicio, 
                                                String fechaFin, String horaFin) throws SQLException {
        List<RegistroSensor> registros = new ArrayList<>();
        
        String sql = """
            SELECT id, x, y, z, fecha_de_captura, hora_de_captura
            FROM datos_sensor
            WHERE (fecha_de_captura || ' ' || hora_de_captura) >= (? || ' ' || ?)
              AND (fecha_de_captura || ' ' || hora_de_captura) <= (? || ' ' || ?)
            ORDER BY id ASC
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, fechaInicio);
            pstmt.setString(2, horaInicio);
            pstmt.setString(3, fechaFin);
            pstmt.setString(4, horaFin);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                RegistroSensor registro = new RegistroSensor();
                registro.id = rs.getInt("id");
                registro.x = rs.getInt("x");
                registro.y = rs.getInt("y");
                registro.z = rs.getInt("z");
                registro.fecha = rs.getString("fecha_de_captura");
                registro.hora = rs.getString("hora_de_captura");
                
                registros.add(registro);
            }
        }
        
        return registros;
    }
    
    public List<RegistroSensor> consultarTodosLosDatos() throws SQLException {
        List<RegistroSensor> registros = new ArrayList<>();
        
        String sql = """
            SELECT id, x, y, z, fecha_de_captura, hora_de_captura
            FROM datos_sensor
            ORDER BY id ASC
        """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                RegistroSensor registro = new RegistroSensor();
                registro.id = rs.getInt("id");
                registro.x = rs.getInt("x");
                registro.y = rs.getInt("y");
                registro.z = rs.getInt("z");
                registro.fecha = rs.getString("fecha_de_captura");
                registro.hora = rs.getString("hora_de_captura");
                
                registros.add(registro);
            }
        }
        
        return registros;
    }
     
    public void cerrarConexion() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    public static class RegistroSensor {
        public int id;
        public int x;
        public int y;
        public int z;
        public String fecha;
        public String hora;
    }
}
