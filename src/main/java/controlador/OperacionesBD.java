package controlador;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import modelo.Cliente;

public class OperacionesBD {
	private static final Map<String, String> MAPEO = new HashMap<String, String>() 
    {
        {
            put("Cliente", "clientes");
            put("Articulo", "articulos");
            put("Grupo", "grupos");
            // ...
        }
    };
    
    private static Connection con = null;
   

	private final Statement stmt;
	private final ResultSet rs;
    private final Class clase;

    public OperacionesBD(Class clase) throws Exception {
        this.clase = clase;
        con = Conexion.getConnection();
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        rs = stmt.executeQuery("select * from " + MAPEO.get(this.clase.getSimpleName()));
    }

    public Object primero() throws SQLException {
        if (rs.first()) {
            return leer();
        }
        else {
            return null;
        }
    }

    public Object anterior() throws SQLException {
        rs.previous();
        if (rs.isBeforeFirst()) {
            rs.first();
        }
        return leer();
    }

    public Object siguiente() throws SQLException {
        rs.next();
        if (rs.isAfterLast()) {
            rs.last();
        }
        return leer();
    }

    public Object ultimo() throws SQLException {
        rs.last();
        return leer();
    }

    public Object ir(int r) throws SQLException {
        rs.absolute(r);
        return leer();
    }

    public boolean esPrimero() throws SQLException {
        return rs.isFirst();
    }

    public boolean esUltimo() throws SQLException {
        return rs.isLast();
    }
    
    public void irAntesPrimero() throws SQLException {
        rs.beforeFirst();
    }

    public void guardar(Object o, boolean nuevoReg) throws SQLException {
        if (nuevoReg) {
            rs.moveToInsertRow();
        }        
        // if (o instanceof Cliente) {
        String tabla = o.getClass().getSimpleName();
        switch (tabla) {
            case "Cliente":
                Cliente cli = (Cliente) o;
                rs.updateString(2, cli.getNombre());
                rs.updateString(3, cli.getDireccion());
                break;
            // case "..."
        }
        if (nuevoReg) {
            rs.insertRow();
        } else {
            rs.updateRow();
        }
    }

    public Object leer() throws SQLException {
        Object obj;
        switch (clase.getSimpleName()) {
            case "Cliente":
                obj = new Cliente(rs.getInt(1), rs.getString(2), rs.getString(3));
                break;
            case "Articulo":
                obj = null;
                break;
            default:
                obj = null;
        }
        return obj;
    }

    public Object borrar() throws SQLException {
        // devuelve registro a mostrar despuÃ©s de borrar
        Object obj = null;
        rs.deleteRow();
        if (rs.isBeforeFirst()) {
            rs.first();
        }
        if (posValida()) {
            obj = leer();
        }
        return obj;
    }

    public void cerrar() throws SQLException {
        stmt.close();
        rs.close();
    }

    public int registroActual() throws SQLException {
        return rs.getRow();
    }

    public int numRegistros() throws SQLException {
        int bak = rs.getRow();
        rs.last();
        int num = rs.getRow();
        rs.absolute(bak);
        return num;
    }
    
    public static Connection getCon() {
		return con;
	}

	public static void setCon(Connection con) {
		OperacionesBD.con = con;
	}
	
	 public ResultSet getRs() {
			return rs;
		}

    public boolean hayRegistros() throws SQLException {
        return rs.first();
    }
    
    public boolean posValida() throws SQLException {
        return rs.getRow() > 0;
    }
}
