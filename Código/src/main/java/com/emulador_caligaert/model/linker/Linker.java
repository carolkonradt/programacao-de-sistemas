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
        if (tables.symbolExistsInTable(programIndex, code)){
            address = Integer.toString(tables.getSymbolFromTable(programIndex, code) + offset);
        }

        return address;
    }

    private String handleExpression(String code, String[] labels, int programIndex, int offset){
        HashMap<String, ArrayList<Integer>> ocurrenceTable = tables.getAllOcurrenceTables().get(0);
        int startIndex = 0;
        int sum = 0;
        for (String label: labels){
            if (label.isEmpty())
                continue;
            int begin = code.indexOf(label, startIndex);
            int signal = 1;
            int signalIndex = begin - 1;
            int endIndex = begin+label.length();
        
            if (signalIndex >= 0){
                if (code.charAt(signalIndex) == '-')
                    signal = -1;
            }                
            startIndex = endIndex;

            if (tables.getOcurrenceFromTable(programIndex, label) != null){
                int value = ocurrenceTable.get(label).removeFirst() + offset;
                sum = sum + value;
                continue;
            }

            if (label.matches("\\d+")){
                sum = sum + (Integer.parseInt(label)*signal);
                continue;
            }

            if (unifiedDefinitionTable.containsKey(label)){
                sum = sum + unifiedDefinitionTable.get(label);
                continue;
            }
            if (tables.symbolExistsInTable(programIndex, label)){
                sum = sum + tables.getSymbolFromTable(programIndex, label) + offset;
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

        BufferedWriter objWriter = new BufferedWriter(new FileWriter(outputPath));
        BufferedReader objReader;

        for (String filepath: files){
            try{
                File program = new File(filepath);
                String instruction;
                
                objReader = new BufferedReader(new FileReader(program));
                unifiedDefinitionTable = unifyDefinitionTables(offsetList);

                while ((instruction = objReader.readLine()) != null){
                    String[] instructionParts = instruction.split(" ");
                    String instructionCode = "";
                    
                    for (String code: instructionParts){
                        String address = code;
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
}
