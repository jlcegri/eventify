package UI.Controllers;

import Model.Entities.User;
import Model.Services.UserService;
import UI.Services.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // --- Configuração Visual (CSS Foco) ---
    @FXML
    public void initialize() {
        setupFocusStyle(emailField);
        setupFocusStyle(passwordField);
    }

    private void setupFocusStyle(TextField field) {
        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (field.getParent() instanceof HBox) {
                HBox parent = (HBox) field.getParent();
                if (isNowFocused) {
                    parent.getStyleClass().add("input-field-container-focused");
                } else {
                    parent.getStyleClass().remove("input-field-container-focused");
                }
            }
        });
    }
    // --------------------------------------

    @FXML
    private void handleLogin() {
        // 1. Validar campos vazios
        if (emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("Error", "Please enter both email and password.");
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // 2. Autenticar com o Serviço
        User user = UserService.getInstance().login(email, password);

        if (user != null) {
            // 3. Sucesso: Definir utilizador atual e navegar
            UserService.getInstance().setCurrentUser(user);
            System.out.println("Login Successful: " + user.getName());

            if (user.isFirstEntry()) {
                NavigationService.getInstance().navigateTo("EditProfile", true);
            } else {
                NavigationService.getInstance().navigateTo("DiscoverEvents", true);
            }
        } else {
            // 4. Falha
            showAlert("Login Failed", "Invalid email or password. Please try again.");
        }
    }

    @FXML
    private void handleJoinLink() {
        // Redireciona para o Registo
        NavigationService.getInstance().navigateTo("Register");
    }

    protected void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}