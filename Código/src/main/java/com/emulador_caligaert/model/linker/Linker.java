package com.emulador_caligaert.model.linker;

import java.util.ArrayList;
import java.util.HashMap;

import com.emulador_caligaert.model.tables.Tables;

public class Linker {
    Tables tables;

    public Linker(String [] files){
        if(files == null || files.length == 0){

        } else {
            this.tables = new Tables();

        }
    }

  
    private void secondStep(ArrayList<String> instructionList, ArrayList<Integer> offsetList){
        ArrayList<String> objCode = new ArrayList<>();
        int currentInstruction = 0;
        int programIndex = 0;
        int offset = 0;

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

        instructionList = objCode;
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
