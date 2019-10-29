package controlador;

import java.net.URL;
import java.sql.SQLException;
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

	@FXML
	private Button btnNuevo;
	@FXML
	private Button btnBorrarOAceptar;
	@FXML
	private Button btnEditarOCancelar;
	@FXML
	private TextField tfID;
	@FXML
	private TextField tfNombre;
	@FXML
	private TextField tfDireccion;
	@FXML
	private Label lblInfo;

	private Cliente cli;
	private int modo;

	private static OperacionesBD operacionesBD;

	public void initialize(URL url, ResourceBundle rb) {

		tfID.setDisable(true);

		try {
			operacionesBD = new OperacionesBD(Cliente.class);
			
			if (OperacionesBD.getCon() == null) {
				Platform.exit();
			}
			
			cli = (Cliente) operacionesBD.primero();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mostrarRegistro();

	}

	// ******************************************************************************
	// ACCIONES ASOCIADAS A BOTONES
	// ******************************************************************************
	@FXML
	private void accionPrimero() {
		try {
			cli = (Cliente) operacionesBD.primero();
		} catch (SQLException e) {

			e.printStackTrace();
		}
		mostrarRegistro();
	}

	@FXML
	private void accionAtras() {

		try {
			if (!operacionesBD.esPrimero()) {
				operacionesBD.ir(operacionesBD.registroActual() - 1);
				cli = (Cliente) operacionesBD.leer();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		mostrarRegistro();
	}

	@FXML
	private void accionAdelante() {

		try {
			if (!operacionesBD.esUltimo()) {
				operacionesBD.ir(operacionesBD.registroActual() + 1);
				cli = (Cliente) operacionesBD.leer();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		mostrarRegistro();
	}

	@FXML
	private void accionUltimo() {

		try {
			cli = (Cliente) operacionesBD.ultimo();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		mostrarRegistro();
	}

	@FXML
	private void accionNuevo() {
		modo = MODO_NUEVO_REGISTRO;
		cambiarModo();

	}

	@FXML
	private void accionEditarOCancelar() {
		if (modo == MODO_NAVEGACION) { // accion editar
			modo = MODO_EDITA_REGISTRO;
		} else {
			modo = MODO_NAVEGACION;
		}
		cambiarModo();
	}

	@FXML
	private void accionBorrarOAceptar() {
		Cliente cliente = new Cliente(tfNombre.getText(), tfDireccion.getText());
		try {

			switch (modo) {
			case MODO_NAVEGACION:

				String mensaje = "¿Estás seguro de borrar el registro [" + tfID.getText() + "]?";
				Alert d = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
				d.setTitle("Borrado de registro");
				d.showAndWait();

				if (d.getResult() == ButtonType.YES) {
					operacionesBD.ir(operacionesBD.registroActual());
					cli = (Cliente) operacionesBD.borrar();
				}
				break;
			case MODO_EDITA_REGISTRO:
				operacionesBD.guardar(cliente, false);
				break;
			case MODO_NUEVO_REGISTRO:
				operacionesBD.guardar(cliente, true);
				break;
			default:
				break;
			}

		} catch (Exception ex) {
			mensajeExcepcion(ex, "Borrando registro...");
		}

		modo = MODO_NAVEGACION;
		cambiarModo();
		mostrarRegistro();
	}

	private void mostrarRegistro() {
		try {
			lblInfo.setText("Registro " + operacionesBD.registroActual() + " de " + operacionesBD.numRegistros());
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
			btnBorrarOAceptar.setText("Borrar");
			btnEditarOCancelar.setText("Editar");
			tfNombre.setEditable(false);
			tfDireccion.setEditable(false);
			break;
		case MODO_NUEVO_REGISTRO:
			tfID.setText("<autonum>");
			tfNombre.setText("");
			tfDireccion.setText("");
		case MODO_EDITA_REGISTRO:
			btnNuevo.setDisable(true);
			btnBorrarOAceptar.setText("Aceptar");
			btnEditarOCancelar.setText("Cancelar");
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
