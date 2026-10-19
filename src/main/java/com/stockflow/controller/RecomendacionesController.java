package com.stockflow.controller;

import com.stockflow.App;
import com.stockflow.model.Inventario;
import com.stockflow.model.Recomendacion;
import com.stockflow.service.AutenticacionService;
import com.stockflow.service.RecomendacionService;
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

public class RecomendacionesController {
    @FXML private TableView<Recomendacion> tablaRecomendaciones;
    @FXML private TableColumn<Recomendacion, String> colProducto;
    @FXML private TableColumn<Recomendacion, String> colOrigen;
    @FXML private TableColumn<Recomendacion, String> colDestino;
    @FXML private TableColumn<Recomendacion, Integer> colCantidad;
    @FXML private TableColumn<Recomendacion, String> colEstado;
    @FXML private Label lblStockOrigen;
    @FXML private Label lblMaximo;
    @FXML private Label lblExcedente;
    @FXML private Label lblStockDestino;
    @FXML private Label lblMinimo;
    @FXML private Label lblNecesidad;
    @FXML private Label lblRecomendada;
    @FXML private Button btnAprobar;
    @FXML private Button btnRechazar;
    @FXML private VBox boxDetalles;
    @FXML private Label lblPlaceholder;
    @FXML private Label lblEstadoDetalle;
    @FXML private Label lblProductoDetalle;
    @FXML private Label lblOrigenDetalle;
    @FXML private Label lblDestinoDetalle;

    private final RecomendacionService service = new RecomendacionService();

    @FXML
    public void initialize() {
        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colOrigen.setCellValueFactory(new PropertyValueFactory<>("origen"));
        colDestino.setCellValueFactory(new PropertyValueFactory<>("destino"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tablaRecomendaciones.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccion) -> actualizarDetalle(seleccion));
        cargar();
    }

    private void cargar() {
        try {
            tablaRecomendaciones.setItems(FXCollections.observableArrayList(service.listar()));
            actualizarDetalle(null);
        } catch (RuntimeException e) {
            ControllerSupport.mostrarError("No se pudieron cargar las recomendaciones", e);
        }
    }

    private void actualizarDetalle(Recomendacion recomendacion) {
        boolean seleccionada = recomendacion != null;
        boxDetalles.setVisible(seleccionada);
        boxDetalles.setManaged(seleccionada);
        lblPlaceholder.setVisible(!seleccionada);
        lblPlaceholder.setManaged(!seleccionada);
        if (!seleccionada) return;
        Inventario origen = recomendacion.getInvOrigen();
        Inventario destino = recomendacion.getInvDestino();
        lblProductoDetalle.setText(recomendacion.getProductoNombre());
        lblOrigenDetalle.setText(recomendacion.getOrigen());
        lblDestinoDetalle.setText(recomendacion.getDestino());
        actualizarBadge(lblEstadoDetalle, recomendacion.getEstado());
        lblStockOrigen.setText(String.valueOf(origen.getStockActual()));
        lblMaximo.setText(String.valueOf(origen.getStockMaximo()));
        lblExcedente.setText(String.valueOf(origen.getStockActual() - origen.getStockMaximo()));
        lblStockDestino.setText(String.valueOf(destino.getStockActual()));
        lblMinimo.setText(String.valueOf(destino.getStockMinimo()));
        lblNecesidad.setText(String.valueOf(destino.getStockMinimo() - destino.getStockActual()));
        lblRecomendada.setText(String.valueOf(recomendacion.getCantidad()));
        boolean pendiente = "PENDIENTE".equals(recomendacion.getEstado());
        btnAprobar.setDisable(!pendiente);
        btnRechazar.setDisable(!pendiente);
    }

    private void actualizarBadge(Label badge, String estado) {
        badge.getStyleClass().removeAll("status-pending", "status-approved", "status-rejected",
                "status-transit", "status-completed", "status-cancelled");
        badge.setText(estado);
        String clase = switch (estado) {
            case "PENDIENTE" -> "status-pending";
            case "APROBADA" -> "status-approved";
            case "RECHAZADA" -> "status-rejected";
            default -> "status-pending";
        };
        badge.getStyleClass().add(clase);
    }

    @FXML
    private void aprobar() {
        Recomendacion recomendacion = tablaRecomendaciones.getSelectionModel().getSelectedItem();
        if (recomendacion == null) return;
        TextInputDialog dialogo = new TextInputDialog(String.valueOf(recomendacion.getCantidad()));
        dialogo.setTitle("Aprobar recomendación");
        dialogo.setHeaderText("Cantidad a transferir");
        dialogo.setContentText("Cantidad aprobada:");
        dialogo.showAndWait().ifPresent(valor -> {
            try {
                int cantidad = Integer.parseInt(valor.trim());
                service.aprobar(recomendacion, cantidad, "Aprobada desde StockFlow");
                ControllerSupport.mostrarInformacion("Recomendación aprobada",
                        "Se creó una transferencia pendiente por " + cantidad + " unidades.");
                cargar();
            } catch (NumberFormatException e) {
                ControllerSupport.mostrarError("Cantidad inválida", new IllegalArgumentException("Ingrese un número entero."));
            } catch (RuntimeException e) {
                ControllerSupport.mostrarError("No se pudo aprobar", e);
            }
        });
    }

    @FXML
    private void rechazar() {
        Recomendacion recomendacion = tablaRecomendaciones.getSelectionModel().getSelectedItem();
        if (recomendacion == null) return;
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Rechazar recomendación");
        dialogo.setHeaderText("Motivo del rechazo");
        dialogo.setContentText("Motivo:");
        dialogo.showAndWait().ifPresent(motivo -> {
            try {
                service.rechazar(recomendacion, motivo);
                ControllerSupport.mostrarInformacion("Recomendación rechazada", "La decisión quedó registrada.");
                cargar();
            } catch (RuntimeException e) {
                ControllerSupport.mostrarError("No se pudo rechazar", e);
            }
        });
    }

    @FXML private void navInicio() throws IOException { App.setRoot("inicio"); }
    @FXML private void navInventario() throws IOException { App.setRoot("inventario"); }
    @FXML private void navRecomendaciones() { }
    @FXML private void navTransferencias() throws IOException { App.setRoot("transferencias"); }
    @FXML private void navSalir() throws IOException {
        new AutenticacionService().cerrarSesion();
        App.setRoot("login");
    }
}
