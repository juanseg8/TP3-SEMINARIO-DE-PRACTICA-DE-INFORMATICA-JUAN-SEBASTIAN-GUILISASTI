package com.stockflow.controller;

import com.stockflow.App;
import com.stockflow.model.ResumenInicio;
import com.stockflow.model.Usuario;
import com.stockflow.service.AutenticacionService;
import com.stockflow.service.InicioService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class InicioController {
    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblDeficit;
    @FXML private Label lblSobrestock;
    @FXML private Label lblRecomendaciones;
    @FXML private Label lblTransferencias;

    private final InicioService service = new InicioService();

    @FXML
    public void initialize() {
        try {
            Usuario usuario = service.obtenerUsuarioActual();
            ResumenInicio resumen = service.obtenerResumen();
            lblBienvenida.setText("Bienvenido, " + usuario.getNombre());
            lblRol.setText(formatearRol(usuario.getRol()));
            lblDeficit.setText(String.valueOf(resumen.getDeficit()));
            lblSobrestock.setText(String.valueOf(resumen.getSobrestock()));
            lblRecomendaciones.setText(String.valueOf(resumen.getRecomendacionesPendientes()));
            lblTransferencias.setText(String.valueOf(resumen.getTransferenciasEnTransito()));
        } catch (RuntimeException e) {
            ControllerSupport.mostrarError("No se pudo cargar el inicio", e);
        }
    }

    private String formatearRol(String rol) {
        String[] palabras = rol.toLowerCase().split("_");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (resultado.length() > 0) resultado.append(' ');
            resultado.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1));
        }
        return resultado.toString();
    }

    @FXML private void verInventario() throws IOException { App.setRoot("inventario"); }
    @FXML private void revisarRecomendaciones() throws IOException { App.setRoot("recomendaciones"); }
    @FXML private void navInicio() { }
    @FXML private void navInventario() throws IOException { App.setRoot("inventario"); }
    @FXML private void navRecomendaciones() throws IOException { App.setRoot("recomendaciones"); }
    @FXML private void navTransferencias() throws IOException { App.setRoot("transferencias"); }
    @FXML private void navSalir() throws IOException {
        new AutenticacionService().cerrarSesion();
        App.setRoot("login");
    }
}
