package com.emulador_caligaert.model.virtual_machine;

/**Classe responsável pela implementação da memória na máquina virtual
 * @author rboeira
 */
public class Memory {
    private String[] memory; // Array para armazenar as palavras de memória (16 bits cada)
    private int size; // Tamanho da memória em palavras
    private int sizeBits;

    /**Método construtor para inicializar a memória com um tamanho especificado
     * @param sizeKB - tamanho da memória (em KB), será convertido
     *               para o tamanho de palavra dentro do construtor
     */
    public Memory(int sizeKB, int sizeBits) {
        // Convertendo o tamanho de KB para palavras (1 KB = 1024 palavras de 16 bits)
        this.size = sizeKB * 1024;
        this.memory = new String[size];
        this.sizeBits = sizeBits;
        
        for (int i=0; i<size; i++)
            memory[i] = "0";
    }

    private int correctOverflow(int value){
        int maxValue = (int) (Math.pow(2, sizeBits-1) - 1);
        int minValue = (int) (-Math.pow(2, sizeBits-1));
        if (value > maxValue)
            return maxValue;
        if (value < minValue)
            return minValue;
        return value;
    }

    /**Método para escrever uma palavra na memória em um endereço específico
     * @param address - endereço específico da memória
     * @param data - palavra a ser escrita
     * @return void
     */
    public boolean write(int address, String data) {
        int value = 0;
        try{
            value = correctOverflow(Integer.parseInt(data));
            data = Integer.toString(value);
        } catch (Exception e){}

        if (address >= 0 && address < size)
            memory[address] = data; 
        else
            return false;
        return true;
    }

    /**Método para ler uma palavra da memória em um endereço específico
     * @param address - endereço específico da memória
     * @return palavra de memória em int
     */
    public String read(int address) {
        if (address >= 0 && address < size) {
            return memory[address];
        } else {
            System.out.println("Erro: Endereço fora dos limites da memória.");
            return "NaN"; // Retorna um valor inválido se o endereço for fora dos limites
        }
    }

    public int getSize(){
        return this.size;
    }

    public void clear(){
        for (int i=0; i<size; i++)
            memory[i] = "0";
    }

    public void printMemory(){
        for (int i=0; i<size; i++){
            System.out.println(memory[i]);
        }
    }
}

