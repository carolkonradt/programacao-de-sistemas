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

    public Linker(Tables tables){
        this.tables = tables;
    }
  
    public boolean linkPrograms(LinkedList<String> files, String outputPath) throws IOException{
        ArrayList<Integer> offsetList = tables.getOffset();
        
        int currentInstruction = 0;
        int programIndex = 0;
        int offset = 0;
        System.out.println(offset);

        BufferedWriter objWriter = new BufferedWriter(new FileWriter(outputPath));
        BufferedReader objReader;

        for (String filepath: files){
            try{
                File program = new File(filepath);
                String instruction;
                System.out.println(filepath);
                
                objReader = new BufferedReader(new FileReader(program));
                HashMap<String, Integer> unifiedDefinitionTable = unifyDefinitionTables(offsetList);
                System.out.println(unifiedDefinitionTable);

                while ((instruction = objReader.readLine()) != null){
                    String[] instructionParts = instruction.split(" ");
                    String instructionCode = "";
                    
                    for (String code: instructionParts){
                        String address = code;
        
                        if (unifiedDefinitionTable.containsKey(code))
                            address = Integer.toString(unifiedDefinitionTable.get(code));
        
                        //if (symbolsTables.get(programIndex).containsKey(code))
                        if (tables.symbolExistsInTable(programIndex, code)){
                            address = Integer.toString(tables.getSymbolFromTable(programIndex, code) + offset);
                            System.out.println(programIndex+" prog " + code);
                        }
        
                        instructionCode = instructionCode.concat(address+" ");
                        currentInstruction++;
                    }

                    if (currentInstruction == offsetList.get(programIndex)){
                        currentInstruction = 0;
                        offset = offset + offsetList.get(programIndex);
                        programIndex++;
                    }
        
                    objWriter.write(instructionCode);
                    objWriter.newLine();
                }
                objReader.close();
            } catch (Exception e){
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
