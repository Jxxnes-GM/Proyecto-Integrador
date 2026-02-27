package Proyecto.Modelo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    // Datos de conexión
    private static final String CONTROLADOR = "com.mysql.cj.jdbc.Driver";
    private static final String URL_BASEDATOS = "jdbc:mysql://localhost:3306/Techzone";
    private static final String USUARIO = "root";
    private static final String CLAVE = "Martinez26*";

    // Método para obtener la conexión desde otras clases
    public static Connection getConnection() {
        Connection conexion = null;
        try {
            Class.forName(CONTROLADOR);
            conexion = DriverManager.getConnection(URL_BASEDATOS, USUARIO, CLAVE);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    // Método para probar la conexión directamente
    public static void main(String[] args) {
        System.out.println("Inicio de programa!\n");

        try (Connection conexion = getConnection()) {
            if (conexion != null) {
                System.out.println("Conexión exitosa a la base de datos.");
            } else {
                System.out.println("No se pudo establecer la conexión.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
