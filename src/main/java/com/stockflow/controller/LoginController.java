package com.stockflow.controller;

import com.stockflow.App;
import com.stockflow.service.AutenticacionService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    private final AutenticacionService autenticacionService = new AutenticacionService();

    @FXML
    private void iniciarSesion() throws IOException {
        lblError.setText("");
        try {
            autenticacionService.iniciarSesion(txtUsuario.getText(), txtPassword.getText().toCharArray());
            App.setRoot("inicio");
        } catch (RuntimeException e) {
            lblError.setText(e.getMessage());
            txtPassword.clear();
        }
    }
}
