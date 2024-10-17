package com.emulador_caligaert.controller;

import com.emulador_caligaert.model.assembler.Assembler;
import com.emulador_caligaert.model.linker.Linker;
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
import java.util.List;
import java.util.LinkedList;

/** Classe responsável pelo controle e ligação
 * do código backend com a interface visual
 */
public class Controller {

    private boolean fileAvailable = false;
    private boolean loaded = false;
    private Machine virtualMachine;
    private Assembler assembler;
    private MacroProcessor macroProcessor;

    @FXML
    private ComboBox<String> cbSelector;
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
    private List<File> selectedFiles;
    private LinkedList<String> input = new LinkedList<>();
    private LinkedList<String> macroOutput = new LinkedList<>();
    private LinkedList<String> asmOutput = new LinkedList<>();

    String outputPath = "";
    String lastMainProject = "";

    private boolean clicked = false;        // variável pra impedir spam de clique em botões

    /**
     * Neste método estão relacionadas todas as ações para botões
     * e outros componentes dispostos na interface visual.
     */
    @FXML
    public void initialize() {
        virtualMachine = new Machine(1, 16, outputArea);

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
        cbSelector.getItems().clear();

        fileAvailable = false;
        input.clear();
        macroOutput.clear();
        asmOutput.clear();
        lastMainProject = "";
        loaded = false;
        clicked = false;

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

    private String saveFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione um Arquivo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Decimal Files", "*.hex")
        );

        // Mostrar o diálogo de seleção de arquivos
        File file = fileChooser.showSaveDialog(stage);
        if (file == null)
            return null;

        return file.getAbsolutePath();
    }

    private void selectFile() {
        if (clicked)
            return;
        clicked = true;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione um Arquivo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Mostrar o diálogo de seleção de arquivos
        selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {
            input.clear();
            macroOutput.clear();
            asmOutput.clear();
            virtualMachine.restartMachine();
            virtualMachine.setMOP(0);
            for (File selectedFile: selectedFiles)
                outputArea.appendText("Arquivo selecionado: " + selectedFile.getAbsolutePath() + "\n");
            fileAvailable = true;
            refreshComboBox();
            loaded = false;
        } else {
            fileAvailable = false;
        }
        clicked = false;
        
    }

    private void refreshComboBox(){
        cbSelector.getItems().clear();
        for (File file: selectedFiles){
            cbSelector.getItems().add(file.getName());
            input.add(file.getAbsolutePath());
        }
        cbSelector.getSelectionModel().select(0);
        lastMainProject = input.get(0);
    }

    /**
     * Executa o programa na máquina virtual e exibe o conteúdo do arquivo .lst (código original e montado).
     */
    private void run() {
        if (clicked)
            return;
        clicked = true;

        if (!fileAvailable) {
            outputArea.appendText("Por favor, selecione um arquivo.\n");
            clicked = false;
            return;
        }

        if (cbSelector.getSelectionModel().getSelectedItem() != null){
            int index = cbSelector.getSelectionModel().getSelectedIndex();
            String mainProject = selectedFiles.get(index).getAbsolutePath();
            if (!mainProject.equals(lastMainProject)){
                lastMainProject = mainProject;
                input.remove(lastMainProject);
                input.addFirst(mainProject);
                loaded = false;
            }
        }
        //System.out.println(input.toString());

        if (!loaded){
            if (!processMacro()){
                clicked = false;
                return;
            }
            if (!runAssembler()){
                clicked = false;
                return;
            }
        
            outputPath = saveFile();
            if (outputPath == null){
                clicked = false;
                return;
            }
            if (!runLinker(outputPath)){
                clicked = false;
                return;
            }
        }
        
        virtualMachine.restartMachine();
        virtualMachine.setStackSize(assembler.getStackSize());
        virtualMachine.loadProgram(outputPath);
        virtualMachine.setMOP(1);
        mopField.setText("1");
        loaded = true;
        updateView();
        if (!virtualMachine.runProgram())
            outputArea.appendText("Erro durante a execução do programa.\n");
            
        updateView();
        clicked = false;
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
        if (clicked)
            return;
        clicked = true;

        if (!fileAvailable) {
            outputArea.appendText("Por favor, selecione um arquivo.\n");
            clicked = false;
            return;
        }

        if (cbSelector.getSelectionModel().getSelectedItem() != null){
            int index = cbSelector.getSelectionModel().getSelectedIndex();
            String mainProject = selectedFiles.get(index).getAbsolutePath();
            if (!mainProject.equals(lastMainProject)){
                lastMainProject = mainProject;
                input.remove(lastMainProject);
                input.addFirst(mainProject);
                loaded = false;
            }
        }

        if (!loaded){
            if (!processMacro()){
                clicked = false;
                return;
            }
            if (!runAssembler()){
                clicked = false;
                return;
            }
        
            outputPath = saveFile();
            if (outputPath == null){
                clicked = false;
                return;
            }
            if (!runLinker(outputPath)){
                clicked = false;
                return;
            }
        }

        if (virtualMachine.getMOP() != 2) {
            virtualMachine.restartMachine();
            virtualMachine.setStackSize(assembler.getStackSize());
            virtualMachine.loadProgram(outputPath);
            loaded = true;
            virtualMachine.setMOP(2);
            mopField.setText("2");
        }

        if (!virtualMachine.runProgram()) {
            virtualMachine.setMOP(0);
            outputArea.appendText("Erro durante a execução do programa.\n");
        }

        updateView();
        clicked = false;
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
            reg.setText(registers.get(key).getData());
        }

        Memory mem = virtualMachine.getMemory();
        int memSize = mem.getSize();
        int programStart = 3+assembler.getStackSize();
        for (int i = 3+assembler.getStackSize(); i < memSize; i++)
            memItems.add((i-programStart) + String.format(":\t\t") + mem.read(i));

        memoriaList.setItems(memItems);
        memoriaList.refresh();

        stkItems.add(String.format("Tamanho:\t\t") + mem.read(2));
        for (int i = 0; i<virtualMachine.getStackSize(); i++) 
            stkItems.add(i+String.format(":\t\t") + mem.read(3+i));

        pilhaList.setItems(stkItems);
        pilhaList.refresh();
    }

    // Configura o Stage para ser utilizado pelo FileChooser
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private boolean processMacro(){
        macroProcessor = new MacroProcessor();  
        macroOutput = new LinkedList<>();
        for (String file: input){
            try {
                String filepath = macroProcessor.processMacros(file);   
                //System.out.println(filepath + " macro");
                macroOutput.add(filepath);  
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean runAssembler(){
        // Chamar o montador para gerar o arquivo .lst
        assembler = new Assembler(outputArea);
        asmOutput = new LinkedList<>();
        for (String filepath: macroOutput){
            //System.out.println(filepath+" assembler");
            int index = filepath.lastIndexOf(".");
            String outputFileName = filepath.substring(0, index) + ".obj";
            asmOutput.add(outputFileName);

            if (assembler.mount(filepath)) {
                outputArea.appendText("Montagem completada com sucesso!\n");
            } else {
                outputArea.appendText("Erro durante a montagem. Verifique o arquivo.\n");
                displayLstFile(filepath + ".lst");
                return false;
            }
        }
        return true;
    }

    private boolean runLinker(String outputPath){
        Linker linker = new Linker(assembler.getLinkerInfo());
        boolean wasLinked = false;

        try{
            wasLinked = linker.linkPrograms(asmOutput, outputPath);
        } catch (Exception e){
            wasLinked = false;
            outputArea.appendText("Não foi possível abrir algum dos arquivos!");
        }
        return wasLinked;
    }
}
