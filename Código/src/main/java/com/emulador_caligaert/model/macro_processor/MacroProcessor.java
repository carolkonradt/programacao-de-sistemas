package com.emulador_caligaert.model.macro_processor;

import java.io.*;
import java.util.*;

public class MacroProcessor {

    // Estrutura para armazenar as macros definidas
    private static Map<String, List<String>> macroDefinitions = new HashMap<>();
    private static Map<String, LinkedList<String>> prototypes = new HashMap<>();
    private static BufferedReader reader;
    private static BufferedWriter writer;
    
    public MacroProcessor(){
        
    }
    public static void main(String[] args) {
        String inputFileName = "/home/rboeira/Área de trabalho/PS/vs/emulador_caligaert/teste.txt";  // Nome do arquivo de entrada
        String outputFileName = "MASMAPRG.ASM";  // Nome do arquivo de saída

        try {
            processMacros(inputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String processMacros(String inputFileName) throws IOException {
        // Abre os arquivos de entrada e saída
        reader = new BufferedReader(new FileReader(inputFileName));
        writer = new BufferedWriter(new FileWriter("MASMAPRG.ASM"));

        String line;
        boolean insideMacro = false;
        List<String> macroBody = new ArrayList<>();
        String macroName = "";
        boolean flagNewMacro = false;

        // Pilha para controlar a definição de macros aninhadas
        Stack<String> macroDefinitionStack = new Stack<>();

        // Processa linha por linha
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            // Verifica se a linha contém uma definição de macro
            if (line.startsWith("MACRO")) {
                insideMacro = true;
                flagNewMacro = true;
                //continue; // pula a linha "MACRO"
            } else {
                    
                // Verifica se a linha contém o fim da definição de macro
                if (line.equals("MEND")) {
                    macroDefinitionStack.pop();
                    if(macroDefinitionStack.empty()){
                        insideMacro = false;
                    }
                } else {
                        
                    // Se estamos dentro de uma macro, armazena seu corpo
                    if (insideMacro) {
                        if (flagNewMacro) {
                            // A primeira linha após "MACRO" é o nome da macro
                            macroName = line.split(" ")[0];
                            LinkedList<String> formalParameters = getParameters(line);

                            prototypes.put(macroName, formalParameters);
                            
                            macroDefinitions.put(macroName, new ArrayList<>());
                            macroDefinitionStack.push(macroName);
                            flagNewMacro = false;
                        } else {
                            for(String key: macroDefinitionStack){
                                macroBody = macroDefinitions.get(key);
                                macroBody.add(line);
                            }
                        }
                        
                    } else {
                            // Se a linha contém uma chamada de macro, expandimos ela
                        if (macroDefinitions.containsKey(line.split(" ")[0])) {
                            HashMap<String, LinkedList<String>> globalParameters = new HashMap<>();
                            expandMacro(line, globalParameters);
                        } else {
                            writer.write(line);
                            writer.newLine();
                        }
                    }                        
                }
            }
            continue;
        }

        System.out.println(macroDefinitions);

        // Fecha os leitores e escritores
        reader.close();
        writer.close();

        return new File("MASMAPRG.ASM").getAbsolutePath();
    }

    public static void expandMacro(String line, HashMap<String, LinkedList<String>> globalParameters) throws IOException{
        String macroNameExp = line.split(" ")[0];
        List<String> macroBodyExp  = macroDefinitions.get(macroNameExp);
        // relaciona o parâmetro formal (do protótipo) com o real (da chamada)
        HashMap<String, String> link = matchParameters(line);

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
        System.out.println(globalParameters);

        // Escreve o corpo da macro no arquivo de saída
        for (String macroLine : macroBodyExp) {
            // substitui os parâmetros formais pelos reais
            macroLine = replaceRealParameters(macroLine, globalParameters);
            String[] instructionParts = macroLine.split(" ");
            String firstSymbol = instructionParts[0];
            System.out.println(macroLine);

            if (macroDefinitions.containsKey(firstSymbol))
                expandMacro(macroLine, (HashMap<String, LinkedList<String>>) globalParameters.clone());
            else{     
                writer.write(macroLine);
                writer.newLine();   
            }
        }        
    }

    public static String replaceRealParameters(String line, HashMap<String, LinkedList<String>> globalParameters){
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

    public static LinkedList<String> getParameters(String line){
        String[] instructionParts = line.split(" ");
        LinkedList<String> parameters = new LinkedList<>();

        for (int i=1; i<instructionParts.length; i++)
            parameters.add(instructionParts[i]);

        return parameters;
    }

    public static HashMap<String, String> matchParameters(String line){
        HashMap<String, String> link = new HashMap<>();
        String[] instructionParts = line.split(" ");
        LinkedList<String> formalParameters = prototypes.get(instructionParts[0]);
        for (int i=1; i<instructionParts.length; i++)
            link.put(formalParameters.get(i-1), instructionParts[i]);
        return link;
    }
}


