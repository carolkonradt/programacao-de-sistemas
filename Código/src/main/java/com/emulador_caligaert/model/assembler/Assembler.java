package com.emulador_caligaert.model.assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Assembler {
    private HashMap<String, Integer> mnemonics;
    private HashMap<String, Integer> assemblerInstructions;

    private HashMap<String, Integer> symbolsTable;
    private HashMap<String, Integer> definitionTable;
    private HashMap<String, Integer> usageTable;
    private HashMap<String, ArrayList<Integer>> ocurrenceTable;

    private int offset = 0;
    private int PC = 0;
    private int stkSize;
    private int MAX_INSTRUCTION_LENGHT = 80;
    private int MAX_INSTRUCTION_ITEMS = 5;
    
    public Assembler(){
        this.mnemonics = new HashMap<>();
        this.assemblerInstructions = new HashMap<>();
        this.symbolsTable = new HashMap<>();
        this.definitionTable = new HashMap<>();
        this.usageTable = new HashMap<>();
        this.ocurrenceTable = new HashMap<>();

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
        boolean start = false;
        stkSize = 0;
        try {
            File program = new File(filepath);
            Scanner fileReader = new Scanner(program);

            while (fileReader.hasNextLine()) {
                String instruction = fileReader.nextLine().trim();
                String[] instructionParts = instruction.split("\\s+");
                //System.out.println(instruction);
                if (!validateInstruction(instruction.length(), instructionParts.length))
                    return false;

                if (instructionParts.length == 0 || instructionParts[0].startsWith("*"))    // linha em branco ou comentário
                    continue;

                String firstSymbol = instructionParts[0];
                int instructionStart = 0;

                if (isLabel(firstSymbol)){
                    if (validateLabel(firstSymbol))
                        return false;

                    addNewSymbol(firstSymbol);
                    instructionStart = firstSymbol.length();
                }

                if (handleMnemonicCode(instruction, instructionStart))
                    continue;

                if (handleAssemblerInstruction(instruction, instructionStart))
                    continue;

                return false;
            }
            fileReader.close();
            printTables();
        } catch (FileNotFoundException e) {
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
                    if (validateLabel(operand))
                        return false;
                    foundLabel(operand);
                    operands = operands.concat(operand + " ");
                }
            }
            instructionCode = opCode + " " + operands;
            System.out.println(instructionCode);
            // escreve a instruçao no arquivo
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

        if (numOfElements != requiredElements)
            return false;

        if (operation.equals("START")) {
            return true;
        }
        if (operation.equals("END")){
            offset = PC;
            PC=0;
            return true;
        }

        if (operation.equals("CONST")) {
            if (opIndex > 0){
                String label = instructionParts[0];
                String operand = instructionParts[opIndex+1];

                if (definitionTable.containsKey(label)) {
                    symbolsTable.remove(label);
                    if (definitionTable.get(label) == -1)
                        definitionTable.replace(label, PC);
                    else
                        return false;   // variável já inicializada
                }
                else
                    addNewSymbol(label);
                //escreve operando no arquivo
                System.out.println(operand);
            }
            PC++;
            return true;
        }
        if (operation.equals("SPACE")){
            if (opIndex > 0) {
                String label = instructionParts[0];

                if (definitionTable.containsKey(label)) {
                    symbolsTable.remove(label);

                    if (definitionTable.get(label) == -1)
                        definitionTable.replace(label, PC);
                    else
                        return false;   // variável já inicializada
                }
                else
                    addNewSymbol(label);
            }
            PC++;
            //escreve 0 no arquivo
            System.out.println(0);
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

    public boolean validateInstruction(int instructionLength, int numOfComponents){
        if (instructionLength > MAX_INSTRUCTION_LENGHT)
            return false;

        if (numOfComponents > MAX_INSTRUCTION_ITEMS)
            return false;

        return true;
    }

    public boolean validateLabel(String label){
        if (label.length() > 8)
            return true;
        if (!Character.isLetter(label.charAt(0)))
            return true;

        return false;
    }

    private boolean isLabel(String symbol){
        return !mnemonics.containsKey(symbol) && !assemblerInstructions.containsKey(symbol);
    }

    private boolean addNewSymbol(String label){
        if (definitionTable.containsKey(label) || usageTable.containsKey(label))
            return true;

        if (!symbolsTable.containsKey(label))
            symbolsTable.put(label, PC);
        else
            return false;       // variavel já inicializada

        return true;
    }

    private void foundLabel(String label) {
        if (!ocurrenceTable.containsKey(label)){
            ArrayList<Integer> occurrences = new ArrayList<>();
            occurrences.add(PC);

            ocurrenceTable.put(label, occurrences);
        } else {
            ocurrenceTable.get(label).add(PC);
        }
    }

    private void writeOnOutputFile(String instructionCode, String filepath){

    }

    private void printTables(){

        System.out.println("-----------------DT----------------------");
        for (String key: definitionTable.keySet())
            System.out.println(key+" "+definitionTable.get(key));
        System.out.println("-----------------ST----------------------");

        for (String key: symbolsTable.keySet())
            System.out.println(key+" "+symbolsTable.get(key));
        System.out.println("-----------------UT----------------------");
        for (String key: usageTable.keySet())
            System.out.println(key+" "+usageTable.get(key));
        System.out.println("---------------------------------------");

    }
    
}
