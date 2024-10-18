package com.emulador_caligaert.model.tables;

import java.util.ArrayList;
import java.util.HashMap;

public class Tables {
    private ArrayList<HashMap<String, Integer>> symbolsTables = new ArrayList<>();
    private ArrayList<HashMap<String, Integer>> definitionTables = new ArrayList<>();
    private ArrayList<HashMap<String, Integer>> usageTables = new ArrayList<>();
    private ArrayList<HashMap<String, ArrayList<Integer>>> ocurrenceTables = new ArrayList<>();
    private ArrayList<Integer> offsetList;

    public Tables(){

    }


    /*--------------------symbolTables---------------------*/
    
    public void addSymbolTable(HashMap<String, Integer> symbolTable) {
        symbolsTables.add(symbolTable);
    }

    public HashMap<String, Integer> getSymbolTable(int index) {
        return symbolsTables.get(index);
    }

    public Integer getSymbolFromTable(int tableIndex, String key) {
        if (tableIndex >= 0 && tableIndex < symbolsTables.size()) {
            return symbolsTables.get(tableIndex).get(key);
        }
        return null; // Retorna null se o índice da tabela for inválido
    }

    public boolean symbolExistsInTable(int tableIndex, String key) {
        if (tableIndex >= 0 && tableIndex < symbolsTables.size()) {
            return symbolsTables.get(tableIndex).containsKey(key);
        }
        return false; // Retorna false se o índice da tabela for inválido
    }

    public ArrayList<HashMap<String, Integer>> getAllSymbolsTables() {
        return new ArrayList<>(symbolsTables); 
    }


    /*--------------------definitionTables---------------------*/

    public void addDefinitionTable(HashMap<String, Integer> definitionTable) {
        definitionTables.add(definitionTable);
    }

    // Método para pegar um elemento específico na tabela de definições
    public Integer getDefinitionFromTable(int tableIndex, String key) {
        if (tableIndex >= 0 && tableIndex < definitionTables.size()) {
            return definitionTables.get(tableIndex).get(key);
        }
        return null; // Retorna null se o índice da tabela for inválido
    }

    public ArrayList<HashMap<String, Integer>> getAllDefinitionTables() {
        return new ArrayList<>(definitionTables); 
    }


    /*--------------------usageTables---------------------*/

    public void addUsageTable(HashMap<String, Integer> usageTable) {
        usageTables.add(usageTable);
    }

    // Método para pegar um elemento específico na tabela de uso
    public Integer getUsageFromTable(int tableIndex, String key) {
        if (tableIndex >= 0 && tableIndex < usageTables.size()) {
            return usageTables.get(tableIndex).get(key);
        }
        return null; // Retorna null se o índice da tabela for inválido
    }

    public ArrayList<HashMap<String, Integer>> getAllUsageTables() {
        return new ArrayList<>(usageTables); 
    }

    /*-------------------- ocurrenceTable --------------------- */
    public void addOcurrenceTable(HashMap<String, ArrayList<Integer>> ocurrenceTable) {
        ocurrenceTables.add(ocurrenceTable);
    }

    // Método para pegar um elemento específico na tabela de uso
    public ArrayList<Integer> getOcurrenceFromTable(int tableIndex, String key) {
        if (tableIndex >= 0 && tableIndex < ocurrenceTables.size()) {
            return ocurrenceTables.get(tableIndex).get(key);
        }
        return null; // Retorna null se o índice da tabela for inválido
    }

    public ArrayList<HashMap<String, ArrayList<Integer>>> getAllOcurrenceTables() {
        return new ArrayList<>(ocurrenceTables);
    }

    /*-------------------- offsetList --------------------- */
    public void setOffset(ArrayList<Integer> offsetList){
        this.offsetList = offsetList;
    }
    
    public ArrayList<Integer> getOffset(){
        return this.offsetList;
    }
}
