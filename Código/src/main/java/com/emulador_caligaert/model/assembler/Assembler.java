package com.emulador_caligaert.model.assembler;

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

    private ArrayList<HashMap<String, Integer>> symbolsTables;
    private ArrayList<HashMap<String, Integer>> definitionTables;
    private ArrayList<HashMap<String, Integer>> usageTables;

    private HashMap<String, Integer> symbolsTable;
    private HashMap<String, Integer> definitionTable;
    private HashMap<String, Integer> usageTable;
    private HashMap<String, ArrayList<Integer>> ocurrenceTable;

    private ArrayList<Integer> offset;
    private int PC = 0;
    private int stkSize;
    private int MAX_INSTRUCTION_LENGTH = 80;
    private int MAX_INSTRUCTION_ITEMS = 5;
    private ArrayList<String> errorMessages;
    private ArrayList<String> instructionList;
    private ErrorMessage errorMessage;
    private TextArea outputArea;

    public Assembler(TextArea outputArea){
        this.mnemonics = new HashMap<>();
        this.assemblerInstructions = new HashMap<>();
        this.symbolsTable = new HashMap<>();
        this.definitionTable = new HashMap<>();
        this.usageTable = new HashMap<>();
        this.ocurrenceTable = new HashMap<>();
        this.errorMessages = new ArrayList<>();
        this.instructionList = new ArrayList<>();
        this.outputArea = outputArea;
        this.errorMessage = new ErrorMessage();
        this.symbolsTables = new ArrayList<>();
        this.definitionTables = new ArrayList<>();
        this.usageTables = new ArrayList<>();
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
        stkSize = 0;
        try {
            File program = new File(filepath);
            Scanner fileReader = new Scanner(program);

            while (fileReader.hasNextLine()) {
                String instruction = fileReader.nextLine().trim();
                String[] instructionParts = instruction.split("\\s+");

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
                outputArea.appendText(errorMessage.getErrorMessage(9));
                errorMessages.add("Erro: Instrução não reconhecida: " + instruction);
                break;
            }
            fileReader.close();
            secondeStep();
            writeOnOutputFile(filepath);
            printTables();
        } catch (FileNotFoundException e) {
            outputArea.appendText(errorMessage.getErrorMessage(11));
            errorMessages.add("Erro: Arquivo não encontrado.");
            return false;
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
                    operands = operands.concat(operand);
                    continue;
                }

                // label
                if (isLabel(operand)) {
                    if (!validateLabel(operand)){
                        outputArea.appendText(errorMessage.getErrorMessage(6));
                        errorMessages.add("Erro: Label inválido: " + operand);
                        return false;
                    }
                    foundLabel(operand);
                    operands = operands.concat(operand + " ");
                }
            }
            instructionCode = opCode + " " + operands;
            instructionList.add(instructionCode);
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
        int requiredElements = assemblerInstructions.get(operation);

        if (numOfElements != requiredElements) {
            outputArea.appendText(errorMessage.getErrorMessage(6));
            errorMessages.add("Erro: Número inválido de elementos para operação: " + operation);
            return false;
        }

        if (operation.equals("START")) {
            return true;
        }

        if (operation.equals("END")){
            symbolsTables.add((HashMap<String, Integer>) symbolsTable.clone());
            definitionTables.add((HashMap<String, Integer>) definitionTable.clone());
            usageTables.add((HashMap<String, Integer>) usageTable.clone());

            symbolsTable.clear();
            definitionTable.clear();
            usageTable.clear();

            offset.add(PC);
            PC=0;
            return true;
        }

        if (operation.equals("CONST")) {
            if (opIndex > 0){
                String operand = instructionParts[opIndex+1];

                if (operand.matches("\\d+"))
                    instructionList.add(operand);
                else
                    return false;
            }
            PC++;
            return true;
        }

        if (operation.equals("SPACE")){
            if (opIndex > 0)
                instructionList.add("0");
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

    private void writeOnOutputFile(String filepath) {
        try {
            FileWriter objFileWriter = new FileWriter(filepath + ".obj");
            FileWriter lstFileWriter = new FileWriter(filepath + ".lst");

            // Escreve o código objeto (.OBJ)
            for (String line : instructionList) {
                objFileWriter.write(line + "\n");
            }

            // Escreve a listagem (.LST)
            for (String line : instructionList) {
                lstFileWriter.write(line + "\n");
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
            return true;
        }
        symbolsTable.put(label, PC);
        return true;
    }

    private void foundLabel(String label) {
        if (!isDeclared(label))
            symbolsTable.put(label, -1);
        
        if (!ocurrenceTable.containsKey(label)){
            ArrayList<Integer> occurrences = new ArrayList<>();
            occurrences.add(PC);

            ocurrenceTable.put(label, occurrences);
        } else {
            ocurrenceTable.get(label).add(PC);
        }
    }

    private boolean intuse(String label){
        if (usageTable.containsKey(label))
            return false;   // variável já definida

        symbolsTable.remove(label);
        usageTable.put(label, -1);
        return true;
    }

    private boolean intdef(String label){
        int operand;

        if (definitionTable.containsKey(label))
            return false;   // variável já definida

        if (symbolsTable.containsKey(label)){
            operand = symbolsTable.get(label);

            symbolsTable.remove(label);
            definitionTable.put(label, operand);
            return true;
        }

        definitionTable.put(label, -1);
        return true;
    }

    private HashMap<String, Integer> unifyDefinitionTables(){
        int offset = 0;
        int programIndex = 0;

        HashMap<String, Integer> unifiedDefinitionTable = new HashMap<>();
        for (HashMap<String, Integer> table: definitionTables){
            for (String key: table.keySet()){
                int address = table.get(key) + offset;
                unifiedDefinitionTable.put(key, address);
            }
            offset = offset+this.offset.get(programIndex);
            programIndex = programIndex + 1;
        }

        return unifiedDefinitionTable;
    }

    private void secondeStep(){
        ArrayList<String> objCode = new ArrayList<>();
        int currentInstruction = 0;
        int programIndex = 0;
        int offset = 0;

        for (String instruction: instructionList){
            String[] instructionParts = instruction.split(" ");
            String instructionCode = "";
            HashMap<String, Integer> unifiedDefinitionTable = unifyDefinitionTables();

            for (String code: instructionParts){
                String address = code;

                if (unifiedDefinitionTable.containsKey(code))
                    address = Integer.toString(unifiedDefinitionTable.get(code));

                if (symbolsTables.get(programIndex).containsKey(code))
                    address = Integer.toString(symbolsTables.get(programIndex).get(code)+offset);

                instructionCode = instructionCode.concat(address+" ");
                currentInstruction++;
            }
        
            if (currentInstruction == this.offset.get(programIndex)){
                currentInstruction = 0;
                offset = offset + this.offset.get(programIndex);
                programIndex++;
            }

            objCode.add(instructionCode);
        }

        instructionList = objCode;
    }

    private void printTables(){
        int segment = 0;
        for (HashMap<String, Integer> table: definitionTables) {
            System.out.println("-----------------DT"+segment+"----------------------");
            for (String key : table.keySet())
                System.out.println(key + " " + table.get(key));
            segment++;
        }

        segment = 0;
        for (HashMap<String, Integer> table: symbolsTables) {
            System.out.println("-----------------ST"+segment+"----------------------");
            for (String key : table.keySet())
                System.out.println(key + " " + table.get(key));
            segment++;
        }

        segment = 0;
        for (HashMap<String, Integer> table: usageTables) {
            System.out.println("-----------------UT"+segment+"----------------------");
            for (String key : table.keySet())
                System.out.println(key + " " + table.get(key));
            segment++;
        }
        System.out.println("---------------------------------------");

    }
}
