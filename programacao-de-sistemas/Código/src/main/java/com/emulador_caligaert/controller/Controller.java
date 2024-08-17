package com.emulador_caligaert.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/** Classe responsável pelo controle e ligação
 * do código backend com a interface visual
 */
public class Controller {

    @FXML
    private TextField pcField, spField, accField, mopField, riField, reField;

    @FXML
    private ListView<String> memoriaList, pilhaList;

    @FXML
    private TextArea outputArea;

    @FXML
    private TextField operacaoField;

    @FXML
    private Button clearButton, resetButton, fileButton;

    private Stage stage;

    /**
     * Neste método estão relacionadas todas as ações para botões
     * e outros componentes dispostos na interface visual.
     */
    @FXML
    public void initialize() {
        // Adicione ações aqui para os botões e outros componentes
        clearButton.setOnAction(e -> outputArea.clear());
        resetButton.setOnAction(e -> resetFields());
        fileButton.setOnAction(e -> selectFile());
    }

    /**
     * Lógica para processar a operação.
     */
    private void processOperation() {
        String operacao = operacaoField.getText();
        outputArea.appendText("Operação: " + operacao + "\n");
        operacaoField.clear();
    }

    /**
     * Limpa todos os campos da interface visual.
     */
    private void resetFields() {
        pcField.clear();
        spField.clear();
        accField.clear();
        mopField.clear();
        riField.clear();
        reField.clear();
        memoriaList.getItems().clear();
        pilhaList.getItems().clear();
        outputArea.clear();
    }

    /**Método que abre a seleção do arquivo a ser carregado
     *na máquina virtual através do JavaFX
     * @author Filhos do Alan
     * @param -
     * @return void
     *
     */
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione um Arquivo");
        // Filtrar para apenas arquivos de texto
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Mostrar o diálogo de seleção de arquivos
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            outputArea.appendText("Arquivo selecionado: " + selectedFile.getAbsolutePath() + "\n");
            // Aqui você pode adicionar lógica para processar o arquivo, se necessário
        }
    }
}
