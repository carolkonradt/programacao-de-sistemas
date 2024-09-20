package com.emulador_caligaert.controller;

import com.emulador_caligaert.model.assembler.Assembler;
import com.emulador_caligaert.model.macro_processor.MacroProcessor;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
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
        virtualMachine = new Machine(1, 10, outputArea);

        clearButton.setOnAction(e -> outputArea.clear());
        resetButton.setOnAction(e -> resetFields());  // Botão de reset configurado corretamente
        fileButton.setOnAction(e -> selectFile());
        runButton.setOnAction(e -> run());
        stepButton.setOnAction(e -> step());
    }

    /**
     * Limpa todos os campos da interface visual e reinicia a máquina virtual.
     */
    private void resetFields() {
        // Reinicia a máquina virtual
        virtualMachine.restartMachine();

        // Limpa a área de texto (outputArea)
        outputArea.clear();

        // Limpa os campos de registradores
        pcField.clear();
        spField.clear();
        accField.clear();
        mopField.clear();
        riField.clear();
        reField.clear();

        // Limpa as listas de memória e pilha
        memoriaList.setItems(FXCollections.observableArrayList());
        pilhaList.setItems(FXCollections.observableArrayList());

        // Atualiza a interface gráfica
        memoriaList.refresh();
        pilhaList.refresh();
    }

    /**
     * Método que abre a seleção do arquivo a ser carregado
     * na máquina virtual através do JavaFX
     */
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione um Arquivo");
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

    /**
     * Executa o programa na máquina virtual e exibe o conteúdo do arquivo .lst (código original e montado).
     */
    private void run() {
        if (!fileAvailable) {
            outputArea.appendText("Por favor, selecione um arquivo.\n");
            return;
        }

        processMacro();
        processAssembly();

        virtualMachine.restartMachine();
        virtualMachine.loadProgram(filepath);
        virtualMachine.setMOP(1);
        mopField.setText("1");
        if (virtualMachine.runProgram())
            outputArea.appendText("Programa executado com sucesso!\n");
        else
            outputArea.appendText("Erro durante a execução do programa.\n");
        updateView();
    }

    /**
     * Método para exibir o conteúdo do arquivo .lst no outputArea.
     */
    private void displayLstFile(String filepath) {
        try {
            File lstFile = new File(filepath);
            Scanner fileReader = new Scanner(lstFile);
            outputArea.appendText("\nConteúdo do arquivo .lst:\n");
            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();
                outputArea.appendText(line + "\n");
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            outputArea.appendText("Erro: Arquivo .lst não encontrado.\n");
        }
    }

    /**
     * Executa o programa passo a passo na máquina virtual.
     */
    private void step() {
        if (!fileAvailable) {
            outputArea.appendText("Por favor, selecione um arquivo.\n");
            return;
        }

        if (virtualMachine.getMOP() != 2) {
            virtualMachine.restartMachine();
            virtualMachine.loadProgram(filepath);
            virtualMachine.setMOP(2);
            mopField.setText("2");
        }

        if (!virtualMachine.runProgram()) {
            virtualMachine.setMOP(0);
        }

        updateView();
    }

    /**
     * Atualiza a interface de acordo com o estado da máquina virtual.
     */
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

        for (int i = 0; i < memSize; i++)
            memItems.add(Integer.toHexString(mem.read(i)));

        memoriaList.setItems(memItems);
        memoriaList.refresh();

        Stack<Integer> stack = virtualMachine.getStack();
        for (int num : stack) {
            stkItems.add(Integer.toHexString(num));
        }

        pilhaList.setItems(stkItems);
        pilhaList.refresh();
    }

    // Configura o Stage para ser utilizado pelo FileChooser
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void processMacro(){
        MacroProcessor macroProcessor = new MacroProcessor();        
        try {
            filepath = macroProcessor.processMacros(filepath);     
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processAssembly(){
        // Chamar o montador para gerar o arquivo .lst
        Assembler assembler = new Assembler(outputArea);
        if (assembler.mount(filepath)) {
            outputArea.appendText("Montagem completada com sucesso!\n");

            // Exibir o conteúdo do arquivo .lst gerado
            displayLstFile(filepath + ".lst");
        } else {
            outputArea.appendText("Erro durante a montagem. Verifique o arquivo.\n");
        }
    }
}
