package com.emulador_caligaert.model.macro_processor;

import java.io.*;
import java.util.*;

public class MacroProcessor {
    // Estrutura para armazenar as macros definidas
    private Map<String, List<String>> macroDefinitions = new HashMap<>();
    private Map<String, LinkedList<String>> prototypes = new HashMap<>();
    private BufferedReader reader;
    private BufferedWriter writer;

    private boolean insideMacro = false;
    private String macroName = "";
    private boolean flagNewMacro = false;

    private int currentLevel = 0;

    public String processMacros(String inputFileName) throws IOException {
        // Abre os arquivos de entrada e saída
        int index = inputFileName.lastIndexOf(".");
        String outputFileName = inputFileName.substring(0, index) + ".asm";

        reader = new BufferedReader(new FileReader(inputFileName));
        writer = new BufferedWriter(new FileWriter(outputFileName));
        
        String line;
        // Pilha para controlar a definição de macros aninhadas
        // Processa linha por linha
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            readingLine(line, new HashMap<String, LinkedList<String>>());
        }

        //System.out.println(macroDefinitions);

        // Fecha os leitores e escritores
        reader.close();
        writer.close();

        return new File(outputFileName).getAbsolutePath();
    }

    private void definingMacro(String macroName, String line){
        macroDefinitions.get(macroName).add(line);
    }
    
    private void readingLine(String line, HashMap<String, LinkedList<String>> globalParameters) throws IOException{
        // Pilha para controlar a definição de macros aninhadas
        Stack<String> macroDefinitionStack = new Stack<>();

        if (line.equals("MEND")) {
            currentLevel--;
            if (currentLevel == 0){
                insideMacro = false;
                return;
            }
        }

        if (line.startsWith("MACRO")) {
            currentLevel++;
            insideMacro = true;
            if (currentLevel == 1){
                flagNewMacro = true;
            //continue; // pula a linha "MACRO"
                return;
            }
        }
    
        if (insideMacro && currentLevel > 0) {
            if (flagNewMacro) {
                // A primeira linha após "MACRO" é o nome da macro
                macroName = line.split(" ")[0];
                LinkedList<String> formalParameters = getParameters(line);

                prototypes.put(macroName, formalParameters);
                            
                macroDefinitions.put(macroName, new ArrayList<>());
                macroDefinitionStack.push(macroName);
                flagNewMacro = false;
                return;
            } else {
                line = replaceRealParameters(line, globalParameters);
                definingMacro(macroName, line);
                return;
            }          
        }

        if (macroDefinitions.containsKey(line.split(" ")[0])) {
            macroName = line.split(" ")[0];
            HashMap<String, String> link = matchParameters(line,globalParameters);

                // empilha o parâmetro real na pilha do parâmetro formal correspondente
            for (String formalParameter: link.keySet()){
                if (globalParameters.containsKey(formalParameter)){
                    String realParameter = link.get(formalParameter);

                    globalParameters.get(formalParameter).add(realParameter);
                    continue;
                }
                LinkedList<String> realParameter = new LinkedList<>();
                realParameter.add(link.get(formalParameter));
                globalParameters.put(formalParameter, realParameter);
            }

            //System.out.println(globalParameters);
            
            for (String macroLine: macroDefinitions.get(macroName)){
                readingLine(macroLine, globalParameters);
            }
            
            for (String formalParameter: link.keySet())
                globalParameters.get(formalParameter).removeLast();
            return;
        }
        line = replaceRealParameters(line, globalParameters);
        writer.write(line);
        writer.newLine();
    }

    private String replaceRealParameters(String line, HashMap<String, LinkedList<String>> globalParameters){
        String macroLine = line.split(" ")[0];
        String[] instructionParts = line.split(" ");

        for (int i=1; i<instructionParts.length; i++){
            String parameter;    
            if (globalParameters.containsKey(instructionParts[i]))
                parameter = globalParameters.get(instructionParts[i]).getLast();
            else
                parameter = instructionParts[i];
            macroLine = macroLine + " " + parameter;
        }

        return macroLine;
    }

    private LinkedList<String> getParameters(String line){
        String[] instructionParts = line.split(" ");
        LinkedList<String> parameters = new LinkedList<>();

        for (int i=1; i<instructionParts.length; i++)
            parameters.add(instructionParts[i]);

        return parameters;
    }

    private HashMap<String, String> matchParameters(String line, HashMap<String, LinkedList<String>> globalParameters){
        HashMap<String, String> link = new HashMap<>();
        String[] instructionParts = line.split(" ");
        LinkedList<String> formalParameters = prototypes.get(instructionParts[0]);
        for (int i=1; i<instructionParts.length; i++){
            String formalParameter = formalParameters.get(i-1);
            String realParameter = instructionParts[i];
            if (globalParameters.containsKey(realParameter))
                link.put(formalParameter, globalParameters.get(realParameter).getLast());
            else
                link.put(formalParameters.get(i-1), instructionParts[i]);
        }
        return link;
    }
}


