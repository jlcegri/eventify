package UI.Controllers;

import UI.Services.NavigationService;
import javafx.fxml.FXML;

public class LandingPageController {

    @FXML
    private void handleGetStarted() {
        // Navega para a página de registo
        NavigationService.getInstance().navigateTo("Register");
    }

    @FXML
    private void handleSignIn() {
        NavigationService.getInstance().navigateTo("Login");
    }
}