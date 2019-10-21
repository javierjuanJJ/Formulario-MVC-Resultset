package controlador;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import modelo.Cliente;

public class ControllerCliente implements Initializable {

    private final static int MODO_NAVEGACION = 0;
    private final static int MODO_NUEVO_REGISTRO = 1;
    private final static int MODO_EDITA_REGISTRO = 2;
    private final static int ACTUAL = 0;
    private final static int PRIMERO = 1;
    private final static int ATRAS = 2;
    private final static int ADELANTE = 3;
    private final static int ULTIMO = 4;

    @FXML
    private Button btnNuevo;
    @FXML
    private Button btnBorrar;
    @FXML
    private Button btnEditar;
    @FXML
    private TextField tfID;
    @FXML
    private TextField tfNombre;
    @FXML
    private TextField tfDireccion;
    @FXML
    private Label lblInfo;

    private Connection con;
    private ResultSet rs;
    private Cliente cli;
    private int modo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tfID.setDisable(true);
        crearConexionBD();
        if (con == null) {
            Platform.exit();
        }
        cargarClientes();
        leerRegistro(PRIMERO);
        mostrarRegistro();

    }

    // ******************************************************************************
    // ACCIONES ASOCIADAS A BOTONES
    // ******************************************************************************
    @FXML
    private void accionPrimero() {
        leerRegistro(PRIMERO);
        mostrarRegistro();
    }

    @FXML
    private void accionAtras() {
        leerRegistro(ATRAS);
        mostrarRegistro();
    }

    @FXML
    private void accionAdelante() {
        leerRegistro(ADELANTE);
        mostrarRegistro();
    }

    @FXML
    private void accionUltimo() {
        leerRegistro(ULTIMO);
        mostrarRegistro();
    }

    @FXML
    private void accionNuevo() {
        modo = MODO_NUEVO_REGISTRO;
        try {
            rs.moveToInsertRow();
        } catch (SQLException ex) {
            mensajeExcepcion(ex, "Creando nuevo registro...");
        }
        cambiarModo();

    }

    @FXML
    private void accionEditarOCancelar() {
        if (modo == MODO_NAVEGACION) {          // accion editar
            modo = MODO_EDITA_REGISTRO;
            cambiarModo();
        } else {                                // accion cancelar
            if (modo == MODO_NUEVO_REGISTRO) {
                try {
                    rs.moveToCurrentRow();
                } catch (SQLException ex) {
                    mensajeExcepcion(ex, "Cancelando nuevo registro...");
                }
            }
//            else {
//                rs.cancelRowUpdates();  // no necesario
//            }
            mostrarRegistro();
            modo = MODO_NAVEGACION;
            cambiarModo();
        }

    }

    @FXML
    private void accionBorrarOAceptar() {
        if (modo == MODO_NAVEGACION) {      // accion borrar
            String mensaje = "¿Estás seguro de borrar el registro [" + tfID.getText() + "]?";
            Alert d = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
            d.setTitle("Borrado de registro");
            d.showAndWait();
            if (d.getResult() == ButtonType.YES) {
                try {
                    rs.deleteRow();
                    if (rs.isBeforeFirst()) {
                        leerRegistro(PRIMERO);
                    } else {
                        leerRegistro(ACTUAL);
                    }
                    mostrarRegistro();
                } catch (SQLException ex) {
                    mensajeExcepcion(ex, "Borrando registro...");
                }
            }

        } else {                            // accion aceptar
            try {
                rs.updateString(2, tfNombre.getText());
                rs.updateString(3, tfDireccion.getText());
                if (modo == MODO_NUEVO_REGISTRO) {
                    rs.insertRow();
                    leerRegistro(ULTIMO);
                } else {
                    rs.updateRow();
                    leerRegistro(ACTUAL);
                }
                mostrarRegistro();
                modo = MODO_NAVEGACION;
                cambiarModo();
            } catch (SQLException ex) {
                mensajeExcepcion(ex, "Actualizando registro...");
            }

        }
    }

    // ******************************************************************************
    // METODOS RELACIONADOS CON BD 
    // ******************************************************************************
    private void crearConexionBD() {
        try {
            // Registrar el Driver: no necesario a partir de la JDBC 4.0
//            String driver = "com.mysql.jdbc.Driver";
//            String driver = "org.mariadb.jdbc.Driver";
//            Class.forName(driver).newInstance();

            String jdbcURL = "jdbc:mariadb://localhost:3306/empresa_ad";
            // String jdbcURL = "jdbc:mysql://localhost:3306/empresa_ad";
            Properties pc = new Properties();
            pc.put("user", "batoi");
            pc.put("password", "1234");
            con = DriverManager.getConnection(jdbcURL, pc);
        } catch (SQLException ex) {
            mensajeExcepcion(ex, "Conectando a la base de datos...");
        }
    }

    private void cargarClientes() {
        try (Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            rs = stmt.executeQuery("select * from clientes");
        } catch (SQLException ex) {
            mensajeExcepcion(ex, "Leyendo clientes de la bd...");
        }

    }

    private void leerRegistro(int pos) {
        try {
            switch (pos) {
                case PRIMERO:
                    rs.first();
                    break;
                case ATRAS:
                    if (!rs.isFirst()) {
                        rs.previous();
                    }
                    break;
                case ADELANTE:
                    if (!rs.isLast()) {
                        rs.next();
                    }
                    break;
                case ULTIMO:
                    rs.last();
                    break;
                case ACTUAL:
                default:
            }
            cli = new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("direccion"));
        } catch (SQLException ex) {
            mensajeExcepcion(ex, "Navegando a través de registros...");
        }

    }

    private int totalRegistros() throws SQLException {
        int r = rs.getRow();
        rs.last();
        int tot = rs.getRow();
        rs.absolute(r);
        return tot;
    }

    private void mostrarRegistro() {
        try {
            lblInfo.setText("Registro " + rs.getRow() + " de " + totalRegistros());
        } catch (SQLException ex) {
            System.err.println("error");
        }
        tfID.setText(String.valueOf(cli.getId()));
        tfNombre.setText(cli.getNombre());
        tfDireccion.setText(cli.getDireccion());
    }

    // ******************************************************************************
    // OTROS MÉTODOS
    // ******************************************************************************
    private void cambiarModo() {
        switch (modo) {
            case MODO_NAVEGACION:
                btnNuevo.setDisable(false);
                btnBorrar.setText("Borrar");
                btnEditar.setText("Editar");
                tfNombre.setEditable(false);
                tfDireccion.setEditable(false);
                break;
            case MODO_NUEVO_REGISTRO:
                tfID.setText("<autonum>");
                tfNombre.setText("");
                tfDireccion.setText("");
            case MODO_EDITA_REGISTRO:
                btnNuevo.setDisable(true);
                btnBorrar.setText("Aceptar");
                btnEditar.setText("Cancelar");
                tfNombre.setEditable(true);
                tfDireccion.setEditable(true);
                tfNombre.requestFocus();

        }

    }

    private void mensajeExcepcion(Exception ex, String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error de excepción");
        alert.setHeaderText(msg);
        alert.setContentText(ex.getMessage());

        String exceptionText = "";
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (StackTraceElement ste : stackTrace) {
            exceptionText = exceptionText + ste.toString() + System.getProperty("line.separator");
        }

        Label label = new Label("La traza de la excepción ha sido: ");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

}
