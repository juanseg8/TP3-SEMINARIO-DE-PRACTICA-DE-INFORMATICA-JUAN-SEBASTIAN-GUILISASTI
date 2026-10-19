package com.stockflow.controller;

import com.stockflow.App;
import com.stockflow.model.Inventario;
import com.stockflow.model.Producto;
import com.stockflow.model.Sucursal;
import com.stockflow.service.AutenticacionService;
import com.stockflow.service.InventarioService;
import com.stockflow.service.RecomendacionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

public class InventarioController {
    @FXML private ComboBox<String> cmbProducto;
    @FXML private ComboBox<String> cmbSucursal;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TableView<Inventario> tablaInventario;
    @FXML private TableColumn<Inventario, String> colProducto;
    @FXML private TableColumn<Inventario, String> colSucursal;
    @FXML private TableColumn<Inventario, Integer> colActual;
    @FXML private TableColumn<Inventario, Integer> colMinimo;
    @FXML private TableColumn<Inventario, Integer> colMaximo;
    @FXML private TableColumn<Inventario, String> colEstado;

    private final InventarioService inventarioService = new InventarioService();
    private final RecomendacionService recomendacionService = new RecomendacionService();

    @FXML
    public void initialize() {
        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colSucursal.setCellValueFactory(new PropertyValueFactory<>("sucursalNombre"));
        colActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colMaximo.setCellValueFactory(new PropertyValueFactory<>("stockMaximo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        try {
            var productos = FXCollections.observableArrayList("Todos");
            for (Producto producto : inventarioService.listarProductos()) productos.add(producto.getNombre());
            cmbProducto.setItems(productos);
            cmbProducto.getSelectionModel().selectFirst();

            var sucursales = FXCollections.observableArrayList("Todos");
            for (Sucursal sucursal : inventarioService.listarSucursales()) sucursales.add(sucursal.getNombre());
            cmbSucursal.setItems(sucursales);
            cmbSucursal.getSelectionModel().selectFirst();
            cmbEstado.setItems(FXCollections.observableArrayList("Todos", "Normal", "Déficit", "Sobrestock"));
            cmbEstado.getSelectionModel().selectFirst();
            buscar();
        } catch (RuntimeException e) {
            ControllerSupport.mostrarError("No se pudo cargar el inventario", e);
        }
    }

    @FXML
    private void buscar() {
        try {
            tablaInventario.setItems(FXCollections.observableArrayList(inventarioService.buscar(
                    cmbProducto.getValue(), cmbSucursal.getValue(), cmbEstado.getValue())));
        } catch (RuntimeException e) {
            ControllerSupport.mostrarError("No se pudo buscar el inventario", e);
        }
    }

    @FXML
    private void generarRecomendaciones() {
        try {
            int generadas = recomendacionService.generarPendientes();
            String mensaje = generadas == 0
                    ? "No hay nuevas oportunidades. Las recomendaciones pendientes ya están registradas."
                    : "Se generaron " + generadas + " recomendaciones nuevas.";
            ControllerSupport.mostrarInformacion("Análisis completado", mensaje);
        } catch (RuntimeException e) {
            ControllerSupport.mostrarError("No se pudieron generar recomendaciones", e);
        }
    }

    @FXML private void navInicio() throws IOException { App.setRoot("inicio"); }
    @FXML private void navInventario() { }
    @FXML private void navRecomendaciones() throws IOException { App.setRoot("recomendaciones"); }
    @FXML private void navTransferencias() throws IOException { App.setRoot("transferencias"); }
    @FXML private void navSalir() throws IOException {
        new AutenticacionService().cerrarSesion();
        App.setRoot("login");
    }
}
