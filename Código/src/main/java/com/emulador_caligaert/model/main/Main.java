package com.emulador_caligaert.model.main;

import java.io.IOException;
import java.util.List;

//import com.emulador_caligaert.virtual_machine.Machine;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);

    }

    /**
     * Inicializa o programa.
     * @param primaryStage - primeira tela da interface
     * @throws IOException
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Carrega o arquivo FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/emulador_caligaert/view/application.fxml"));
        Parent root = fxmlLoader.load();
        
        // Cria a cena com o layout carregado do FXML
        Scene scene = new Scene(root);
    }
}
