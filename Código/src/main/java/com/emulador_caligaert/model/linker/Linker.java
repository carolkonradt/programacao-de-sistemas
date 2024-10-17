package com.emulador_caligaert.model.linker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.emulador_caligaert.model.tables.Tables;

public class Linker {
    Tables tables;
    HashMap<String, Integer> unifiedDefinitionTable;

    public Linker(Tables tables){
        this.tables = tables;
    }

    private String handleLabel(String code, int programIndex, int offset){
        String address = "";
        if (unifiedDefinitionTable.containsKey(code)){
            address = Integer.toString(unifiedDefinitionTable.get(code));
        }
        //if (symbolsTables.get(programIndex).containsKey(code))
        if (tables.symbolExistsInTable(programIndex, code)){
            address = Integer.toString(tables.getSymbolFromTable(programIndex, code) + offset);
            //System.out.println(programIndex+" prog " + code);
        }

        return address;
    }

    private String handleExpression(String code, String[] labels, int programIndex, int offset){
        HashMap<String, ArrayList<Integer>> ocurrenceTable = tables.getAllOcurrenceTables().get(0);
        int startIndex = 0;
        int sum = 0;
        for (String label: labels){
            //System.out.println("lab:" + label);
            //System.out.println("sum: " + sum);
            if (label.isEmpty())
                continue;
            int begin = code.indexOf(label, startIndex);
        
            //System.out.println("beg " + begin);
            int signal = 1;
            int signalIndex = begin - 1;
            int endIndex = begin+label.length();
        
            if (signalIndex >= 0){
                if (code.charAt(signalIndex) == '-')
                    signal = -1;
            }                
            startIndex = endIndex;

            if (tables.getOcurrenceFromTable(programIndex, label) != null){
                //System.out.println("label usage");
                int value = ocurrenceTable.get(label).removeFirst() + offset;
                sum = sum + value;
                continue;
            }
            //System.out.println("d");

            if (label.matches("\\d+")){
                //System.out.println("digit");
                sum = sum + (Integer.parseInt(label)*signal);
                //System.out.println(sum);
                continue;
            }

            if (unifiedDefinitionTable.containsKey(label)){
                //System.out.println("defin");
                sum = sum + unifiedDefinitionTable.get(label);
                continue;
            }
            //if (symbolsTables.get(programIndex).containsKey(code))
            if (tables.symbolExistsInTable(programIndex, label)){
                //System.out.println("symbols");
                sum = sum + tables.getSymbolFromTable(programIndex, label) + offset;
                //System.out.println(programIndex+" prog " + label);
                continue;
            }
        }
        return Integer.toString(sum);
    }
  
    public boolean linkPrograms(LinkedList<String> files, String outputPath) throws IOException{
        ArrayList<Integer> offsetList = tables.getOffset();
        
        int currentInstruction = 0;
        int programIndex = 0;
        int offset = 0;
        //System.out.println(offset);

        BufferedWriter objWriter = new BufferedWriter(new FileWriter(outputPath));
        BufferedReader objReader;

        for (String filepath: files){
            try{
                File program = new File(filepath);
                String instruction;
                //System.out.println(filepath);
                
                objReader = new BufferedReader(new FileReader(program));
                unifiedDefinitionTable = unifyDefinitionTables(offsetList);
                //System.out.println(unifiedDefinitionTable);

                while ((instruction = objReader.readLine()) != null){
                    String[] instructionParts = instruction.split(" ");
                    String instructionCode = "";
                    //System.out.println("inst:"+instruction);
                    
                    for (String code: instructionParts){
                        String address = code;
                        //System.out.println("code:"+code);
                        String[] labels = code.split("[+-]");

                        if (labels.length > 1){
                            address = handleExpression(code, labels, programIndex, offset);
                            instructionCode = instructionCode.concat(address+" ");
                            currentInstruction++;
                            continue;
                        }
                        
                        address = handleLabel(code, programIndex, offset);
                        if (address.isBlank())
                            address = code;
                        instructionCode = instructionCode.concat(address+" ");
                        currentInstruction++;
                    }        
                    if (currentInstruction == offsetList.get(programIndex)){
                        currentInstruction = 0;
                        offset = offset + offsetList.get(programIndex);
                        programIndex++;
                    }
                    //System.out.println("ic:"+instructionCode);
                    objWriter.write(instructionCode);
                    objWriter.newLine();
                }
                objReader.close();
            } catch (Exception e){
                objWriter.close();
                return false;
            }
        }
        objWriter.close();
        return true;
    }
        /* 
        for (String instruction: instructionList){
            String[] instructionParts = instruction.split(" ");
            String instructionCode = "";
            HashMap<String, Integer> unifiedDefinitionTable = unifyDefinitionTables(offsetList);

            for (String code: instructionParts){
                String address = code;

                if (unifiedDefinitionTable.containsKey(code))
                    address = Integer.toString(unifiedDefinitionTable.get(code));

                //if (symbolsTables.get(programIndex).containsKey(code))
                if (tables.symbolExistsInTable(programIndex, code))
                    address = Integer.toString(tables.getSymbolFromTable(programIndex, code) + offset);

                instructionCode = instructionCode.concat(address+" ");
                currentInstruction++;
            }

            if (currentInstruction == offsetList.get(programIndex)){
                currentInstruction = 0;
                offset = offset + offsetList.get(programIndex);
                programIndex++;
            }

            objCode.add(instructionCode);
        }

        instructionList = objCode;*/


    private HashMap<String, Integer> unifyDefinitionTables(ArrayList<Integer> offsetList){
        int offset = 0;
        int programIndex = 0;

        HashMap<String, Integer> unifiedDefinitionTable = new HashMap<>();

        ArrayList<HashMap<String, Integer>> definitionTables = tables.getAllDefinitionTables();

        for (HashMap<String, Integer> table: definitionTables){
            for (String key: table.keySet()){
                int address = table.get(key) + offset;
                unifiedDefinitionTable.put(key, address);
            }
            offset = offset + offsetList.get(programIndex);
            programIndex = programIndex + 1;
        }

        return unifiedDefinitionTable;
    }

    /*private Map<String, Integer> tabelaDeSimbolos;
    private List<Module> modules;

    public Linker(List<Module> modules) {
        this.modules = modules;
        this.tabelaDeSimbolos = new HashMap<>();
    }*/


/* 
    public void primeiraPassagem() {
        int enderecoAtual = 0;
        for (Module module : modules) {
            for (String simbolo : module.getDefinicoes()) {
                tabelaDeSimbolos.put(simbolo, enderecoAtual + module.getEnderecoBase(simbolo));
            }
            enderecoAtual += module.getTamanho();
        }
    }

    public void segundaPassagem() {
        for (Module module : modules) {
            for (Referencia referencia : module.getReferencias()) {
                int endereco = tabelaDeSimbolos.get(referencia.getSimbolo());
                module.relocate(referencia, endereco);
            }
        }
        gerarCodigoFinal();
    }

    private void gerarCodigoFinal() {
        // Lógica para gerar o código final a partir dos módulos ligados
    }*/
}
