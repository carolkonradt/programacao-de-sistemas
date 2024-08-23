package com.emulador_caligaert.controller;

import com.emulador_caligaert.model.virtual_machine.Machine;
import com.emulador_caligaert.model.virtual_machine.Memory;
import com.emulador_caligaert.model.virtual_machine.Register;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Stack;

/** Classe responsável pelo controle e ligação
 * do código backend com a interface visual
 */
public class Controller {

    private String filepath;

    private boolean fileAvailable = false;

    private Machine virtualMachine;

    @FXML
    private TextField pcField, spField, accField, mopField, riField, reField;

    @FXML
    private GridPane gdRegs;

    @FXML
    private ListView<String> memoriaList, pilhaList;

    @FXML
    private TextArea outputArea;

    @FXML
    private TextField operacaoField;

    @FXML
    private Button clearButton, resetButton, fileButton, runButton, stepButton;

    private Stage stage;
    private File selectedFile;
    /**
     * Neste método estão relacionadas todas as ações para botões
     * e outros componentes dispostos na interface visual.
     */
    @FXML
    public void initialize() {
        // Adicione ações aqui para os botões e outros componentes
        virtualMachine = new Machine(1, 10, outputArea);

        clearButton.setOnAction(e -> outputArea.clear());
        resetButton.setOnAction(e -> resetFields());
        fileButton.setOnAction(e -> selectFile());
        runButton.setOnAction(e -> run());
        stepButton.setOnAction(e -> step());
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
        virtualMachine.restartMachine();
        outputArea.clear();
        updateView();
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
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            outputArea.appendText("Arquivo selecionado: " + selectedFile.getAbsolutePath() + "\n");
            filepath = selectedFile.getAbsolutePath();
            fileAvailable = true;
        }
    }

    private void run(){
        if (!fileAvailable)     // mostrar janela dizendo para upar arquivo
            return;

        virtualMachine.restartMachine();
        virtualMachine.loadProgram(filepath);
        virtualMachine.setMOP(1);
        mopField.setText("1");
        if (virtualMachine.runProgram())
            outputArea.appendText("Programa executado com sucesso!\n");
        updateView();
    }

    private void step(){
        if (!fileAvailable)     // mostrar janela dizendo para upar arquivo
            return;

        if (virtualMachine.getMOP() != 2) {
            virtualMachine.restartMachine();
            virtualMachine.loadProgram(filepath);
            virtualMachine.setMOP(2);
            mopField.setText("2");
        }
        if (!virtualMachine.runProgram()) {
            virtualMachine.setMOP(0);
            step();
        }
        updateView();
    }

    private void updateView() {
        HashMap<String, Register> registers = virtualMachine.getRegisters();
        ObservableList<String> memItems = FXCollections.observableArrayList();
        ObservableList<String> stkItems = FXCollections.observableArrayList();

        for (String key : registers.keySet()) {
            TextField reg = (TextField) gdRegs.lookup("#" + key.toLowerCase() + "Field");

            reg.setText(Integer.toString(registers.get(key).getData()));
        }

        Memory mem = virtualMachine.getMemory();
        int memSize = mem.getSize();

        for (int i=0; i<memSize; i++)
            memItems.add(Integer.toHexString(mem.read(i)));

        memoriaList = new ListView<>(memItems);
        memoriaList.refresh();
        Stack stack = virtualMachine.getStack();

        for (int num: (Stack<Integer>) stack)
            stkItems.add(Integer.toHexString(num));

        pilhaList = new ListView<>(stkItems);
        System.out.println("------------------------------------");
        virtualMachine.getMemory().printMemory();
    }
}
