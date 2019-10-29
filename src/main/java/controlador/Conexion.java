package controlador;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;

public class Conexion {
	
	static final String ip_maquina = "127.0.0.1";
	static final String puerto = "3306";
	static final String base_de_datos = "empresa_ad";
	static final String usuario = "root";
	static final String contrasenya = "1234";
	
	private static final String url = "jdbc:mysql://" + ip_maquina + ":" + puerto + "/" + base_de_datos
			+ "?serverTimezone=" + TimeZone.getDefault().getID();
	
	private static Connection conexion = null;

	public static Connection getConnection() throws SQLException, Exception {

		if (conexion == null) {

			Properties pc = new Properties();
			pc.put("passwd", contrasenya);
			pc.put("user", usuario);

			conexion = DriverManager.getConnection(url, pc.getProperty("user"), pc.getProperty("passwd"));

		}

		return conexion;

	}

	public static void cerrar() {

		if (conexion != null) {
			try {
				conexion.close();

			} catch (SQLException ex) {
				System.err.println("Error " + ex.getMessage());
			} catch (Exception ex) {
				System.err.println("Error " + ex.getMessage());
			}
		}

	}

}
