package UI.Controllers;

import Model.Entities.User;
import Model.Services.UserService;
import UI.Services.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.ArrayList;

public class RegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox termsCheckBox;

    @FXML
    public void initialize() {
        setupFocusStyle(firstNameField);
        setupFocusStyle(lastNameField);
        setupFocusStyle(emailField);
        setupFocusStyle(passwordField);
    }

    // Adiciona/removes a CSS class ao container HBox quando o TextField ganha/perde foco
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

    @FXML
    private void handleRegister() {
        // 1. Validação simples
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("Validation Error", "Please fill in all fields.");
            return;
        }

        if (!termsCheckBox.isSelected()) {
            showAlert("Validation Error", "You must agree to the Terms and Privacy Policy.");
            return;
        }

        // 2. Lógica de criação de User
        String fullName = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Criar objeto User
        // ID 0 é placeholder, será gerado pelo UserService
        User newUser = new User(
                0,
                fullName,
                email,
                password,
                "", // Localização vazia
                new ArrayList<>(), // Interesses vazios
                new ArrayList<>(), // EventTypes vazios
                "" // Bio vazia
        );

        // 3. Salvar e Autenticar
        UserService.getInstance().addUserCurrent(newUser);
        System.out.println("User Registered: " + fullName);

        // 4. Navegar para a app principal
        NavigationService.getInstance().navigateTo("EditProfile", true);
    }

    @FXML
    private void handleSignInLink() {
        NavigationService.getInstance().navigateTo("Login");
    }

    protected void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}