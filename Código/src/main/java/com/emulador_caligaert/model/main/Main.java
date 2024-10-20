package com.emulador_caligaert.model.main;

import java.io.IOException;

import javafx.application.Application;
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

        // Configura o Stage
        primaryStage.setTitle("Simple CPU Interface");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

   
}
