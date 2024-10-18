package com.emulador_caligaert.model.assembler;

import com.emulador_caligaert.model.linker.Linker;
import com.emulador_caligaert.model.tables.Tables;
import com.emulador_caligaert.model.virtual_machine.ErrorMessage;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Assembler {
    private HashMap<String, Integer> mnemonics;
    private HashMap<String, Integer> assemblerInstructions;

    Tables tables;
    Linker linker;

    private HashMap<String, Integer> symbolsTable;
    private HashMap<String, Integer> definitionTable;
    private HashMap<String, Integer> usageTable;
    private HashMap<String, ArrayList<Integer>> signalTable;
    private HashMap<String, Integer> valueTable;

    private ArrayList<Integer> offset;
    private int PC = 0;
    private int stkSize = 0;
    private int MAX_INSTRUCTION_LENGTH = 80;
    private int MAX_INSTRUCTION_ITEMS = 5;
    private boolean started = false;
    private ArrayList<String> errorMessages;
    private ArrayList<String> instructionList;       // Montado
    private ArrayList<String> originalList;          // Original
    private ErrorMessage errorMessage;
    private TextArea outputArea;

    public Assembler(TextArea outputArea){
        this.mnemonics = new HashMap<>();
        this.assemblerInstructions = new HashMap<>();
        this.symbolsTable = new HashMap<>();
        this.definitionTable = new HashMap<>();
        this.usageTable = new HashMap<>();
        this.signalTable = new HashMap<>();
        this.valueTable = new HashMap<>();
        this.errorMessages = new ArrayList<>();
        this.instructionList = new ArrayList<>();
        this.originalList = new ArrayList<>();         // Inicializando para armazenar o original
        this.outputArea = outputArea;
        this.errorMessage = new ErrorMessage();
    
        this.tables = new Tables();

        this.offset = new ArrayList<>();

        mnemonics.put("ADD",2);
        mnemonics.put("SUB",6);
        mnemonics.put("MULT",14);
        mnemonics.put("CALL",15);
        mnemonics.put("RET",16);
        mnemonics.put("STOP",11);
        mnemonics.put("COPY",13);
        mnemonics.put("LOAD",3);
        mnemonics.put("STORE",7);
        mnemonics.put("READ",12);
        mnemonics.put("WRITE",8);
        mnemonics.put("BR",0);
        mnemonics.put("BRNEG",5);
        mnemonics.put("BRPOS",1);
        mnemonics.put("BRZERO",4);
        mnemonics.put("DIVIDE",10);

        assemblerInstructions.put("SPACE",2);
        assemblerInstructions.put("START",3);
        assemblerInstructions.put("END",1);
        assemblerInstructions.put("CONST",3);
        assemblerInstructions.put("INTDEF",2);
        assemblerInstructions.put("INTUSE",2);
        assemblerInstructions.put("STACK",2);
    }

    public boolean mount(String filepath){
        try {
            File program = new File(filepath);
            Scanner fileReader = new Scanner(program);

            while (fileReader.hasNextLine()) {
                String instruction = fileReader.nextLine().trim();
                String[] instructionParts = instruction.split("\\s+");
                if (instruction.isBlank())
                    continue;

                if (!validateInstruction(instruction, instructionParts.length)) {
                    fileReader.close();
                    return false;
                }

                if (instructionParts.length == 0 || instructionParts[0].startsWith("*"))    // linha em branco ou comentário
                    continue;

                String firstSymbol = instructionParts[0];
                int instructionStart = 0;

                if (isLabel(firstSymbol)){
                    if (!validateLabel(firstSymbol)){
                        outputArea.appendText(errorMessage.getErrorMessage(6));
                        errorMessages.add("Erro: Label inválido: " + firstSymbol);
                        fileReader.close();
                        return false;
                    }

                    addNewSymbol(firstSymbol);

                    instructionStart = firstSymbol.length();
                }

                if (handleMnemonicCode(instruction, instructionStart))
                    continue;

                if (handleAssemblerInstruction(instruction, instructionStart))
                    continue;
                    
                originalList.add(instruction);
                restartTables();
                break;
            }
            fileReader.close();
            if (started){
                errorMessages.add(errorMessage.getErrorMessage(10));
                outputArea.setText(errorMessage.getErrorMessage(10));
            }
            tables.setOffset(offset);
            writeOnOutputFile(filepath);
            //printTables();
            instructionList.clear();
            originalList.clear();
            errorMessages.clear();
        } catch (FileNotFoundException e) {
            outputArea.appendText(errorMessage.getErrorMessage(11));
            errorMessages.add("Erro: Arquivo não encontrado.");
            return false;
        }
        if (errorMessages.size() == 0)
            return true;
        return false;
    }

    private boolean handleExpression(String operand, String operands){
        String[] labels = operand.split("[+-]");
        int startIndex=0;
        String newOperand = "";

        for (int j=0; j<labels.length; j++){
            String label = labels[j];
            if (label.isBlank())
                continue;
            int begin = operand.indexOf(label, startIndex);
            int signal = 1;
            int signalIndex = begin - 1;
            int endIndex = begin+label.length();
            
            if (signalIndex >= 0){
                if (operand.charAt(signalIndex) == '-')
                    signal = -1;
            }               

            if (label.matches("\\d+"))
                continue;
            

            if (isLabel(label)) {
                if (!validateLabel(label)){
                    outputArea.appendText(errorMessage.getErrorMessage(6));
                    errorMessages.add("Erro: Label inválido: " + operand);
                    return false;
                }
                foundLabel(label, signal);
                continue;
            }
        }
        return true;
    }

    private boolean handleMnemonicCode(String instruction, int instructionStart){
        String[] instructionParts = instruction.substring(instructionStart).trim().split("\\s+");
        String firstSymbol = instructionParts[0];

        if (mnemonics.containsKey(firstSymbol)){
            int opCode = mnemonics.get(firstSymbol);
            String instructionCode;
            String operands = "";

            PC++;
            for (int i=1; i<instructionParts.length; i++){
                String operand = instructionParts[i];
                
                // comentário
                if (operand.charAt(0) == '*')
                    break;
                PC++;
                
                // mais do que 2 operandos
                if (i > 3){
                    return false;
                }

                // expressão
                if (operand.split("[+-]").length > 1)
                    if (handleExpression(operand, operands)){
                        operands = operands.concat(operand + " ");
                        int bitIndex = 7 + 1 - i;
                        opCode = opCode | (int) Math.pow(2, bitIndex);
                        continue;
                    }
                // literal
                if (operand.charAt(0) == '@'){
                    String literal = operand.substring(1);

                    if (literal.length() == 1){
                        
                        operands = operands.concat(literal + " ");
                        int bitIndex = 7 + 1 - i;
                        opCode = opCode | (int) Math.pow(2, bitIndex);
                        continue;
                    }
                }

                // operando em hexadecimal
                if ((operand.charAt(0) == 'H') && (operand.split("'").length == 2)){
                    int hexCode = Integer.parseInt(operand.split("\'")[1], 16);
                    operands.concat(hexCode + " ");

                    int bitIndex = 7 + 1 - i;
                    opCode = opCode | (int) Math.pow(2, bitIndex);
                    continue;
                }

                // endereçamento imediato
                if (operand.charAt(0) == '#'){
                    String immediateOperand = operand.substring(1);
                    if (immediateOperand.matches("\\d+")) {
                        operands = operands.concat(immediateOperand + " ");

                        int bitIndex = 7 + 1 - i;
                        opCode = opCode | (int) Math.pow(2, bitIndex);
                        continue;
                    }
                }

                // enderecamento indireto
                if (operand.charAt(0) == 'I'){
                    String indirectOperand = operand.substring(1);
                    if (indirectOperand.matches("\\d+")) {
                        operands = operands.concat(indirectOperand + " ");

                        int bitIndex = 5 - 1 + i;
                        opCode = opCode | (int) Math.pow(2, bitIndex);
                        continue;
                    }
                }
                
                // endereçamento direto
                if (operand.matches("\\d+")){
                    operands = operands.concat(operand + " ");
                    continue;
                }

                // label
                if (isLabel(operand)) {
                    if (!validateLabel(operand)){
                        outputArea.appendText(errorMessage.getErrorMessage(6));
                        errorMessages.add("Erro: Label inválido: " + operand);
                        return false;
                    }
                    foundLabel(operand, 1);
                    operands = operands.concat(operand + " ");
                }
    
            }
            instructionCode = opCode + " " + operands;
            instructionList.add(instructionCode);  // Adicionando instrução montada
            originalList.add(instruction);
            this.linker = new Linker(tables);
            return true;
        }
        return false;
    }

    private boolean handleAssemblerInstruction(String instruction, int instructionStart){
        String[] instructionParts = instruction.split("\\*+")[0].split("\\s+");
        int numOfElements = instructionParts.length;

        int opIndex = 0;
        if (instructionStart != 0)
            opIndex = 1;
        String operation = instructionParts[opIndex];
        int requiredElements = 0;
        try{
            requiredElements = assemblerInstructions.get(operation);
        } catch (Exception e){
            outputArea.appendText(errorMessage.getErrorMessage(9));
            errorMessages.add("Erro: Instrução não reconhecida: " + instruction);
            return false;
        }

        if (numOfElements != requiredElements) {
            outputArea.appendText(errorMessage.getErrorMessage(6));
            errorMessages.add("Erro: Número inválido de elementos para operação: " + operation);
            return false;
        }

        if (operation.equals("START")) {
            if (started == true){
                outputArea.appendText(errorMessage.getErrorMessage(10));
                errorMessages.add("Erro: Não há diretiva END antes de: " + instruction);
                return false;
            }
            String op = "";
            int lastOffset = 0;
            if (offset.size() > 0)
                lastOffset = offset.getLast();
            try{
                op = Integer.toString(Integer.parseInt(instructionParts[opIndex+1])+lastOffset+2);
            } catch (Exception e){
                op = instructionParts[opIndex+1];
            }
            
            instructionList.add(mnemonics.get("BR") + " " + op);
            originalList.add("N/A");
            PC = PC + 2;
            started = true;
            return true;
        }

        if (operation.equals("END")){
            restartTables();
            started = false;
            return true;
        }

        if (operation.equals("CONST")) {
            if (opIndex > 0){
                String operand = instructionParts[opIndex+1];
                String label = instructionParts[opIndex-1];

                if (operand.matches("\\d+")){
                    instructionList.add(operand);
                    originalList.add(instruction);
                    valueTable.put(label, Integer.parseInt(operand));
                } else {
                    outputArea.appendText(errorMessage.getErrorMessage(3));
                    errorMessages.add("Erro: Operando Inválido: " + operand);
                    return false;
                }
            }
            PC++;
            return true;
        }

        if (operation.equals("SPACE")){
            if (opIndex > 0){
                String label = instructionParts[opIndex-1];

                instructionList.add("0");
                originalList.add(instruction);
                valueTable.put(label, 0);
            }
            PC++;
            return true;
        }

        if (operation.equals("INTDEF")){
            String label = instructionParts[opIndex+1];
            return intdef(label);
        }

        if (operation.equals("INTUSE")){
            if (opIndex > 0) {
                String label = instructionParts[opIndex - 1];
                return intuse(label);
            }
            return false;
        }

        if (operation.equals("STACK")){
            String operand = instructionParts[opIndex+1];
            stkSize = Integer.parseInt(operand);
            return true;
        }
        
        return false;
    }

    private HashMap<String, ArrayList<Integer>> buildOcurrenceTable(){
        HashMap<String, ArrayList<Integer>> ocurrenceTable = new HashMap<>();
        for (String key: signalTable.keySet()){
            ArrayList<Integer> ocurrences = new ArrayList<>();
            int size = signalTable.get(key).size();
            for (int i=0; i<size; i++){
                int signal = signalTable.get(key).get(i);
                int value = valueTable.get(key);

                ocurrences.add(signal * value);
                ocurrenceTable.put(key, ocurrences);
            }
        }

        return ocurrenceTable;
    }

    private void restartTables(){
        /*
        symbolsTables.add((HashMap<String, Integer>) symbolsTable.clone());
        definitionTables.add((HashMap<String, Integer>) definitionTable.clone());
        usageTables.add((HashMap<String, Integer>) usageTable.clone());*/
        HashMap<String, ArrayList<Integer>> ocurrenceTable = buildOcurrenceTable();
        tables.addSymbolTable((HashMap<String, Integer>) symbolsTable.clone());
        tables.addDefinitionTable((HashMap<String, Integer>) definitionTable.clone());
        tables.addUsageTable((HashMap<String, Integer>) usageTable.clone());
        tables.addOcurrenceTable(ocurrenceTable);

        symbolsTable.clear();
        definitionTable.clear();
        usageTable.clear();
        signalTable.clear();
        valueTable.clear();

        offset.add(PC);
        PC=0;
    }

    private void writeOnOutputFile(String filepath) {
        try {
            int index = filepath.lastIndexOf(".");
            String outputFileName = filepath.substring(0, index);

            FileWriter objFileWriter = new FileWriter(outputFileName + ".obj");
            FileWriter lstFileWriter = new FileWriter(outputFileName + ".lst");

            // Escreve o código objeto (.OBJ)
            for (String line : instructionList) {
                objFileWriter.write(line + "\n");
            }

            // Escreve a listagem (.LST) - código original e montado lado a lado
            for (int i = 0; i < originalList.size(); i++) {
                String original = originalList.get(i);
                String mounted = (i < instructionList.size()) ? instructionList.get(i) : "N/A";
                lstFileWriter.write(String.format("%-40s | %s\n", original, mounted));  // Alinhamento lado a lado
            }

            // Escreve a lista de erros, se houver
            if (!errorMessages.isEmpty()) {
                lstFileWriter.write("\nErros encontrados:\n");
                for (String error : errorMessages) {
                    lstFileWriter.write(error + "\n");
                }
            } else {
                lstFileWriter.write("\nNenhum erro detectado.\n");
            }

            objFileWriter.close();
            lstFileWriter.close();

        } catch (IOException e) {
            outputArea.appendText(errorMessage.getErrorMessage(13));
        }
    }

    public boolean validateInstruction(String instruction, int numOfComponents){
        if ((instruction.length() > MAX_INSTRUCTION_LENGTH) || (numOfComponents > MAX_INSTRUCTION_ITEMS)){
            outputArea.appendText(errorMessage.getErrorMessage(9));
            errorMessages.add("Erro: Instrução inválida na linha: " + instruction);
            return false;
        }

        return true;
    }

    public boolean validateLabel(String label){
        if (label.length() > 8)
            return false;
        if (!Character.isLetter(label.charAt(0)))
            return false;
        return true;
    }

    private boolean isLabel(String symbol){
        return !mnemonics.containsKey(symbol) && !assemblerInstructions.containsKey(symbol);
    }

    private boolean isDeclared(String label){
        return definitionTable.containsKey(label) || usageTable.containsKey(label) || symbolsTable.containsKey(label);
    }

    private boolean addNewSymbol(String label){
        if (definitionTable.containsKey(label)){
            if (definitionTable.get(label) == -1)
                definitionTable.replace(label, PC);
            else {
                outputArea.appendText(errorMessage.getErrorMessage(7));
                errorMessages.add("Erro: Variável já inicializada: " + label);
                return false;
            }
            if (!valueTable.containsKey(label))
                valueTable.put(label, PC);
            return true;
        }

        if (usageTable.containsKey(label))
            return true;

        if (symbolsTable.containsKey(label)){
            if (symbolsTable.get(label) == -1)
                symbolsTable.replace(label, PC);
            else {
                outputArea.appendText(errorMessage.getErrorMessage(7));
                errorMessages.add("Erro: Variável já inicializada: " + label);
                return false;
            }
            if (!valueTable.containsKey(label))
                valueTable.put(label, PC);
            return true;
        }
        symbolsTable.put(label, PC);
        if (!valueTable.containsKey(label))
            valueTable.put(label, PC);
        return true;
    }

    private void foundLabel(String label, int signal) {
        
        if (!isDeclared(label))
            symbolsTable.put(label, -1);

        if (!signalTable.containsKey(label)){
            ArrayList<Integer> occurrences = new ArrayList<>();
            occurrences.add(signal);

            signalTable.put(label, occurrences);
        } else {
            signalTable.get(label).add(signal);
        }
        if (!valueTable.containsKey(label))
            valueTable.put(label, -1);
            
    }
    

    private boolean intuse(String label){
        if (usageTable.containsKey(label)){
            outputArea.appendText(errorMessage.getErrorMessage(7));
            errorMessages.add("Erro: Variável já inicializada: " + label);
            return false; 
        }
        symbolsTable.remove(label);
        usageTable.put(label, -1);
        return true;
    }

    private boolean intdef(String label){
        int operand;

        if (definitionTable.containsKey(label)){
            outputArea.appendText(errorMessage.getErrorMessage(7));
            errorMessages.add("Erro: Variável já inicializada: " + label);
            return false; 
        }

        if (symbolsTable.containsKey(label)){
            operand = symbolsTable.get(label);

            symbolsTable.remove(label);
            definitionTable.put(label, operand);
            return true;
        }

        definitionTable.put(label, -1);
        return true;
    }

    private void printTables(){
        int segment = 0;

        ArrayList<HashMap<String, ArrayList<Integer>>> ocurrTable = tables.getAllOcurrenceTables();
        for (HashMap<String, ArrayList<Integer>> table: ocurrTable) {
            System.out.println("-----------------OT"+segment+"----------------------");
            for (String key : table.keySet())
                System.out.println(key + " " + table.get(key).toString());
            segment++;
        }

        ArrayList<HashMap<String, Integer>> definitionTables = tables.getAllDefinitionTables();
        for (HashMap<String, Integer> table: definitionTables) {
            System.out.println("-----------------DT"+segment+"----------------------");
            for (String key : table.keySet())
                System.out.println(key + " " + table.get(key));
            segment++;
        }

        segment = 0;
        ArrayList<HashMap<String, Integer>> symbolsTables = tables.getAllSymbolsTables();
        for (HashMap<String, Integer> table: symbolsTables) {
            System.out.println("-----------------ST"+segment+"----------------------");
            for (String key : table.keySet())
                System.out.println(key + " " + table.get(key));
            segment++;
        }

        segment = 0;
        ArrayList<HashMap<String, Integer>> usageTables = tables.getAllUsageTables();
        for (HashMap<String, Integer> table: usageTables) {
            System.out.println("-----------------UT"+segment+"----------------------");
            for (String key : table.keySet())
                System.out.println(key + " " + table.get(key));
            segment++;
        }
        System.out.println("---------------------------------------");

    }

    public Tables getLinkerInfo(){
        return tables;
    }

    public int getStackSize(){
        return stkSize;
    }
}
