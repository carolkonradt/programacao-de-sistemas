package com.emulador_caligaert.model.virtual_machine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Scanner;
import java.util.Stack;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;

/**Classe dedicada a implementação da máquina virtual
 * @author rboeira
 */
public class Machine {
    private HashMap<Integer, LinkedList<String>> instructions;
    private Memory mem;
    private HashMap<String, Register> registers;
    private Stack stack;
    private int stackSize;
    private int programEnd;
    private TextArea outputArea;
    private HashMap<Integer, String> errorMessages;
    private ErrorMessage errorMessage;

    /**
     * Método construtor da máquina virtual.
     * Adiciona a um HashMap todas as instruções da máquina.
     * Estas instruções serão decodificadas de acordo com o código inserido na máquina.
     * @param memSize - um int que determina o tamanho da memória
     */
    public Machine(int memSize, int stackSize, TextArea outputArea){
        this.instructions = new HashMap<>();
        this.mem = new Memory(memSize);
        this.registers = new HashMap<>();
        this.stack = new Stack();
        this.errorMessage = new ErrorMessage();
        this.stackSize = stackSize;
        this.outputArea = outputArea;


        mem.write(2, stackSize);

        LinkedList<String> instructionInfo = new LinkedList<>();

        instructionInfo.add("1");       // Numero de operandos
        instructionInfo.add("0/1/2");   // Endereçamento: 0->D; 1->In; 2->Im
        instructionInfo.add("2");       // Tamanho da instrução

        instructions.put(2, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(0, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(5, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(1, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(4, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(15, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("2");
        instructionInfo.add("0/1;0/1/2");
        instructionInfo.add("3");

        instructions.put(13, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(10, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(3, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(14, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(12, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("0");
        instructionInfo.add("");
        instructionInfo.add("1");

        instructions.put(16, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("0");
        instructionInfo.add("");
        instructionInfo.add("1");

        instructions.put(11, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1");
        instructionInfo.add("2");

        instructions.put(7, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(6, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        instructionInfo.add("1");
        instructionInfo.add("0/1/2");
        instructionInfo.add("2");

        instructions.put(8, (LinkedList<String>) instructionInfo.clone());
        instructionInfo.clear();

        registers.put("ACC", new Register(16));
        registers.put("MOP", new Register(8));
        registers.put("RI", new Register(16));
        registers.put("RE", new Register(16));
        registers.put("PC", new Register(16));
        registers.put("SP", new Register(16));
    }

    /**Método que seleciona o modo de acesso à memória.
     * @param numOfOperands - número de operandos;
     * @param accessConfig - modo como estão arranjados os bits que definem o tipo de endereçamento;
     * @return modo de acesso à memória pela instrução: 0->D; 1->In; 2->Im.
     */
    public String selectAccessMode(int numOfOperands, int accessConfig){
        if (accessConfig > 5)
            return "";
        
        if (numOfOperands == 1){
            if (accessConfig == 0)
                return "0";
            if (accessConfig == 1)
                return "1";
            if (accessConfig == 4)
                return "2";
        }
        
        if (numOfOperands == 2){
            if (accessConfig == 0)
                return "0/0";
            if (accessConfig == 1)
                return "1/0";
            if (accessConfig == 2)
                return "0/1";
            if (accessConfig == 3)
                return "1/1";
            if (accessConfig == 4)
                return "0/2";
            if (accessConfig == 5)
                return "1/2";
        }
        
        return "";
    }

    public boolean runProgram(){
        if (getMOP() == 1){
            registers.get("PC").setData(3+stackSize);
            mem.write(2, stackSize);
            while (registers.get("PC").getData() < programEnd) {
                if (!runInstruction())
                    return false;
            }
            return true;
        }

        if (getMOP() == 2 && registers.get("PC").getData() < programEnd) {
            if (registers.get("PC").getData() == 0)
                registers.get("PC").setData(3 + stackSize);
            return runInstruction();
        }

        return false;
    }

    private boolean runInstruction(){
        int currentPC = registers.get("PC").getData();
        int operation = mem.read(currentPC) & 31;
        LinkedList<String> instructionInfo;

        try{
            instructionInfo = instructions.get(operation);
        } catch(Exception e) {
            outputArea.appendText(errorMessage.getErrorMessage(9));
            return false;
        }

        int instructionSize = Integer.parseInt(instructionInfo.get(2));

        String instruction = Integer.toString(mem.read(currentPC));
        for (int i=1; i<instructionSize; i++){
            String operand = Integer.toString(mem.read(currentPC + i));
            instruction = instruction + " " + operand;
        }
        //outputArea.appendText("new pc " + registers.get("PC").getData()+"\n");

        return decode(instruction);
    }

    public boolean loadProgram(String filePath){
        int i = 0;
        int programStart = 3+stackSize;

        try {
            File program = new File(filePath);
            Scanner fileReader = new Scanner(program);

            while (fileReader.hasNextLine()) {
                String instruction = fileReader.nextLine();

                for (String code: instruction.split(" ")){
                    //outputArea.appendText(Integer.toString(programStart+i)+"\n");
                    //outputArea.appendText(code+"\n");
                    mem.write(programStart+i, Integer.parseInt(code));
                    i++;
                }
            }
            programEnd = programStart+i;
            fileReader.close();
        } catch (FileNotFoundException e) {
            outputArea.appendText(errorMessage.getErrorMessage(11));
            return false;
        }
        return true;
    }

    /**Método que decodifica a instrução, descobre quantos operandos tem, e utiliza
     * outros métodos como o selectAccessMode para ajudar na tarefa
     * @param instruction - a instrução a ser decodificada
     * @return uma chamada ao método run com parâmetros operation, numOfOperands,
     *          operands, instructionAccessMode.
     */
    public boolean decode(String instruction){
        //outputArea.appendText("inst " + instruction +"\n");
        LinkedList<String> instructionInfo;

        ArrayList<Integer> operands = new ArrayList<>();
        String[] instructionParts = instruction.split(" ");

        String allowedAccessModes;

        int operation = Integer.parseInt(instructionParts[0]) & 31;

        //outputArea.appendText("op " + operation+"\n");
        int bitFive = (Integer.parseInt(instructionParts[0]) & 32) >> 5;
        //outputArea.appendText("5 " + bitFive+"\n");
        int bitSix = (Integer.parseInt(instructionParts[0]) & 64) >> 6;
        //outputArea.appendText("6 " + bitSix+"\n");
        int bitSeven = (Integer.parseInt(instructionParts[0]) & 128) >> 7;
        //outputArea.appendText("7 " + bitSeven+"\n");

        int accessConfig = (Integer.parseInt(instructionParts[0]) & 224) >> 5;  //Valor dos ultimos 3 bits
        //outputArea.appendText("ac "+accessConfig+"\n");
        int numOfOperands = instructionParts.length - 1;
        //outputArea.appendText("opr "+numOfOperands+"\n");

        int requiredOperands;

        instructionInfo = instructions.get(operation);

        requiredOperands = Integer.parseInt(instructionInfo.get(0));
        //outputArea.appendText("req "+requiredOperands+"\n");

        allowedAccessModes = instructionInfo.get(1);
        //outputArea.appendText("all "+ allowedAccessModes+"\n");

        if (numOfOperands != requiredOperands){
            outputArea.appendText(errorMessage.getErrorMessage(6));
            return false;
        }

        if (((bitFive + bitSix + bitSeven) != requiredOperands) && (accessConfig != 0)){
            outputArea.appendText(errorMessage.getErrorMessage(12));
            return false;
        }

        String[] instructionAccessMode = selectAccessMode(numOfOperands, accessConfig).split("/");
        for (int i=0; i<numOfOperands; i++){
            String mode = instructionAccessMode[i];
            //outputArea.appendText("md "+ mode+"\n");

            if (!(allowedAccessModes.contains(mode)) || mode.equals("")){
                outputArea.appendText(errorMessage.getErrorMessage(12));
                return false;
            }
        }
        int newPC = registers.get("PC").getData() + Integer.parseInt(instructionInfo.get(2));

        if (requiredOperands == 0)
            return execute(operation, 0, null, null, newPC);

        for (int i=0; i<requiredOperands; i++)
            operands.add(Integer.parseInt(instructionParts[i+1]));

        return execute(operation, numOfOperands, operands, instructionAccessMode, newPC);
    }

    /**
     *
     * @param operation - define qual operação será executada
     * @param numOfOperands - número de operandos da operação
     * @param operands - quais os operandos da operação
     * @param instructionAccessMode - modo de acesso à memória pela operação
     * @return boolean
     */

    public boolean execute(int operation, int numOfOperands, ArrayList<Integer> operands, String[] instructionAccessMode, int newPC){
        ArrayList<Integer> data = new ArrayList<>();

        for (int i=0; i<numOfOperands; i++){
            int operand = operands.get(i);
            int mode = Integer.parseInt(instructionAccessMode[i]);

            data.add(searchOperand(mode, operand));
            //outputArea.appendText("opr: "+operands.get(i)+"\n");
        }

        switch(operation){
            case 0:
                return br(operands.get(0), newPC);
            case 1:
                return brpos(operands.get(0), newPC);
            case 2:
                return add(data.get(0), newPC);
            case 3:
                return load(operands.get(0), newPC);
            case 4:
                return brzero(operands.get(0), newPC);
            case 5:
                return brneg(operands.get(0), newPC);
            case 6:
                return sub(data.get(0), newPC);
            case 7:
                return store(operands.get(0), newPC);
            case 8:
                return write(operands.get(0), newPC);
            case 10:
                return divide(data.get(0), newPC);
            case 11:
                return stop(newPC);
            case 12:
                return read(operands.get(0), newPC);
            case 13:
                return copy(operands.get(0), data.get(1), newPC);
            case 14:
                return mult(data.get(0), newPC);
            case 15:
                return call(operands.get(0), newPC);
            case 16:
                return ret();
            default:
                break;
        }
        return true;
    }


    /**
     * Implementação do modo de endereçamento direto
     * @param operand - operando
     * @return um inteiro que representa a palavra que está dentro do endereço
     */
    private int directAddress(int operand) {
        return mem.read(operand); // O operando é o próprio endereço na memória
    }

    /**
     * Implementação do modo de endereçamento indireto
     * @param operand - operando
     * @return um inteiro que representa a palavra que está dentro do endereço apontado pelo endereço
     *         do operando.
     */
    private int indirectAddress(int operand) {
        // O operando é um endereço que aponta para outro endereço na memória
        return mem.read(mem.read(operand));
    }

    /**
     *
     * @param mode - modo de acesso à memória
     * @param operand - operando a ser utilizado na operação
     * @return um int que representa a palavra buscada.
     */
    private int searchOperand(int mode, int operand) {
        switch (mode) {
            case 0: // Direto
                return directAddress(operand);
            case 1: // Indireto
                return indirectAddress(operand);
            case 2: // Imediato
                return operand;
            default:
                throw new IllegalArgumentException("Modo de endereçamento inválido");
        }
    }

    private boolean add(int operand, int newPC){
        int currentACC = registers.get("ACC").getData();

        registers.get("ACC").setData(currentACC + operand);
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean sub(int operand, int newPC){
        int currentACC = registers.get("ACC").getData();

        registers.get("ACC").setData(currentACC - operand);
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean mult(int operand, int newPC){
        int currentACC = registers.get("ACC").getData();

        registers.get("ACC").setData(currentACC * operand);
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean divide(int operand, int newPC){
        int currentACC = registers.get("ACC").getData();

        registers.get("ACC").setData(currentACC / operand);
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean br(int operand, int newPC){
        registers.get("PC").setData(operand);
        return true;
    }

    private boolean brneg(int operand, int newPC){
        if (registers.get("ACC").getData() < 0)
            registers.get("PC").setData(operand);
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean brpos(int operand, int newPC){
        if (registers.get("ACC").getData() > 0)
            registers.get("PC").setData(operand);
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean brzero(int operand, int newPC){
        if (registers.get("ACC").getData() == 0)
            registers.get("PC").setData(operand);
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean load(int memPosition, int newPC){
        int memData = mem.read(memPosition);
        registers.get("ACC").setData(memData);
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean store(int memPosition, int newPC){
        //outputArea.appendText("mem "+memPosition+"\n");
        mem.write(memPosition, registers.get("ACC").getData());
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean copy(int memPosition, int data, int newPC){
        mem.write(memPosition, data);
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean read(int memPosition, int newPC){
        TextInputDialog td = new TextInputDialog("Insira um valor");

        td.setHeaderText("Ler Entrada");
        Optional<String> input = td.showAndWait();

        if (input.isEmpty()){   // mostrar erro
            return false;
        }

        mem.write(memPosition, Integer.parseInt(input.get()));
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean write(int data, int newPC){
        //outputArea.appendText(Integer.toString(data)+'\n');
        registers.get("PC").setData(newPC);
        return true;
    }

    private boolean call(int operand, int newPC){
        if (stack.size() == stackSize){
            outputArea.appendText("Stack Overflow\n");
            return false;
        }

        stack.push(newPC);
        registers.get("PC").setData(operand);
        return true;
    }

    private boolean ret(){
        registers.get("PC").setData((int) stack.pop());
        return true;
    }

    private boolean stop(int newPC){
        registers.get("PC").setData(newPC);
        return true;
    }

    public void setMOP(int opMode){
        registers.get("MOP").setData(opMode);
    }

    public int getMOP(){
        return registers.get("MOP").getData();
    }

    public void restartMachine(){
        mem.clear();
        stack.clear();

        for (String key: registers.keySet())
            registers.get(key).clear();
    }

    public Memory getMemory(){
        return mem;
    }

    public HashMap<String, Register> getRegisters() {
        return registers;
    }

    public Stack getStack(){
        return stack;
    }
}

