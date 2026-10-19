package com.stockflow.controller;

import com.stockflow.App;
import com.stockflow.model.Transferencia;
import com.stockflow.service.AutenticacionService;
import com.stockflow.service.TransferenciaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TransferenciasController {
    @FXML private TableView<Transferencia> tablaTransferencias;
    @FXML private TableColumn<Transferencia, String> colProducto;
    @FXML private TableColumn<Transferencia, String> colOrigen;
    @FXML private TableColumn<Transferencia, String> colDestino;
    @FXML private TableColumn<Transferencia, Integer> colCantidad;
    @FXML private TableColumn<Transferencia, String> colEstado;
    @FXML private TableColumn<Transferencia, String> colResponsable;
    @FXML private Label lblProductoDetalle;
    @FXML private Label lblOrigen;
    @FXML private Label lblDestino;
    @FXML private Label lblCantidad;
    @FXML private Label lblEstado;
    @FXML private Label lblResponsable;
    @FXML private Label lblPlaceholder;
    @FXML private VBox boxDetalles;
    @FXML private Button btnIniciar;
    @FXML private Button btnCompletar;
    @FXML private Button btnCancelar;

    private final TransferenciaService service = new TransferenciaService();

    @FXML
    public void initialize() {
        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colOrigen.setCellValueFactory(new PropertyValueFactory<>("origen"));
        colDestino.setCellValueFactory(new PropertyValueFactory<>("destino"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        tablaTransferencias.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccion) -> actualizarDetalle(seleccion));
        cargar();
    }

    private void cargar() {
        try {
            tablaTransferencias.setItems(FXCollections.observableArrayList(service.listar()));
            actualizarDetalle(null);
        } catch (RuntimeException e) {
            ControllerSupport.mostrarError("No se pudieron cargar las transferencias", e);
        }
    }

    private void actualizarDetalle(Transferencia transferencia) {
        boolean seleccionada = transferencia != null;
        boxDetalles.setVisible(seleccionada);
        boxDetalles.setManaged(seleccionada);
        lblPlaceholder.setVisible(!seleccionada);
        lblPlaceholder.setManaged(!seleccionada);
        if (seleccionada) {
            lblProductoDetalle.setText(transferencia.getProductoNombre());
            lblOrigen.setText(transferencia.getOrigen());
            lblDestino.setText(transferencia.getDestino());
            lblCantidad.setText(transferencia.getCantidad() + " unidades");
            lblResponsable.setText(transferencia.getResponsable());
            actualizarBadge(lblEstado, transferencia.getEstado());
        }
        btnIniciar.setDisable(!seleccionada || !"PENDIENTE".equals(transferencia.getEstado()));
        btnCompletar.setDisable(!seleccionada || !"EN_TRANSITO".equals(transferencia.getEstado()));
        btnCancelar.setDisable(!seleccionada || !("PENDIENTE".equals(transferencia.getEstado())
                || "EN_TRANSITO".equals(transferencia.getEstado())));
    }

    private void actualizarBadge(Label badge, String estado) {
        badge.getStyleClass().removeAll("status-pending", "status-approved", "status-rejected",
                "status-transit", "status-completed", "status-cancelled");
        badge.setText(estado);
        String clase = switch (estado) {
            case "PENDIENTE" -> "status-pending";
            case "EN_TRANSITO" -> "status-transit";
            case "COMPLETADA" -> "status-completed";
            case "CANCELADA" -> "status-cancelled";
            default -> "status-pending";
        };
        badge.getStyleClass().add(clase);
    }

    @FXML
    private void iniciar() {
        Transferencia transferencia = tablaTransferencias.getSelectionModel().getSelectedItem();
        if (transferencia == null || !ControllerSupport.confirmar("Iniciar transferencia",
                "Se registrará el traslado de " + transferencia.getOrigen() + " a " + transferencia.getDestino() + ".")) return;
        ejecutar(() -> service.iniciar(transferencia), "Transferencia iniciada");
    }

    @FXML
    private void completar() {
        Transferencia transferencia = tablaTransferencias.getSelectionModel().getSelectedItem();
        if (transferencia == null || !ControllerSupport.confirmar("Completar transferencia",
                "Esta acción actualizará ambos inventarios de forma transaccional.")) return;
        ejecutar(() -> service.completar(transferencia), "Transferencia completada e inventarios actualizados");
    }

    @FXML
    private void cancelar() {
        Transferencia transferencia = tablaTransferencias.getSelectionModel().getSelectedItem();
        if (transferencia == null) return;
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Cancelar transferencia");
        dialogo.setHeaderText("Motivo de cancelación");
        dialogo.setContentText("Motivo:");
        dialogo.showAndWait().ifPresent(motivo -> ejecutar(
                () -> service.cancelar(transferencia, motivo), "Transferencia cancelada"));
    }

    private void ejecutar(Runnable accion, String mensaje) {
        try {
            accion.run();
            ControllerSupport.mostrarInformacion("Operación registrada", mensaje + ".");
            cargar();
        } catch (RuntimeException e) {
            ControllerSupport.mostrarError("No se pudo actualizar la transferencia", e);
        }
    }

    @FXML private void navInicio() throws IOException { App.setRoot("inicio"); }
    @FXML private void navInventario() throws IOException { App.setRoot("inventario"); }
    @FXML private void navRecomendaciones() throws IOException { App.setRoot("recomendaciones"); }
    @FXML private void navTransferencias() { }
    @FXML private void navSalir() throws IOException {
        new AutenticacionService().cerrarSesion();
        App.setRoot("login");
    }
}
