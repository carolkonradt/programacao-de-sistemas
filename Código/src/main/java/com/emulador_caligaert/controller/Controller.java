package com.emulador_caligaert.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    private Button clearButton, inputButton, resetButton;

    @FXML
    public void initialize() {
        // Adicione ações aqui para os botões e outros componentes
        clearButton.setOnAction(e -> outputArea.clear());
        inputButton.setOnAction(e -> processOperation());
        resetButton.setOnAction(e -> resetFields());
    }

    private void processOperation() {
        // Lógica para processar a operação
        String operacao = operacaoField.getText();
        outputArea.appendText("Operação: " + operacao + "\n");
        operacaoField.clear();
    }

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
}
