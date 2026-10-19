package com.stockflow.controller;

import javafx.scene.control.Alert;

final class ControllerSupport {
    private ControllerSupport() { }

    static void mostrarError(String titulo, Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("StockFlow");
        alert.setHeaderText(titulo);
        alert.setContentText(error.getMessage() == null ? "Ocurrió un error inesperado." : error.getMessage());
        alert.showAndWait();
    }

    static void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("StockFlow");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    static boolean confirmar(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("StockFlow");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        return alert.showAndWait().filter(button -> button == javafx.scene.control.ButtonType.OK).isPresent();
    }
}
