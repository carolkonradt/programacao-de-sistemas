package com.emulador_caligaert.model.virtual_machine;

/**Classe responsável pela implementação da memória na máquina virtual
 * @author rboeira
 */
public class Memory {
    private int[] memory; // Array para armazenar as palavras de memória (16 bits cada)
    private int size; // Tamanho da memória em palavras

    /**Método construtor para inicializar a memória com um tamanho especificado
     * @param sizeKB - tamanho da memória (em KB), será convertido
     *               para o tamanho de palavra dentro do construtor
     */
    public Memory(int sizeKB) {
        // Convertendo o tamanho de KB para palavras (1 KB = 1024 palavras de 16 bits)
        this.size = sizeKB * 1024;
        this.memory = new int[size];
        
        for (int i=0; i<size; i++)
            memory[i] = 0;
    }
    /**Método para escrever uma palavra na memória em um endereço específico
     * @param address - endereço específico da memória
     * @param data - palavra a ser escrita
     * @return void
     */
    public void write(int address, int data) {
        if (address >= 0 && address < size)
            memory[address] = data & 0xFFFF; // Garantindo que o valor seja de 16 bits
        else
            System.out.println("Erro: Endereço fora dos limites da memória.");
    }

    /**Método para ler uma palavra da memória em um endereço específico
     * @param address - endereço específico da memória
     * @return palavra de memória em int
     */
    public int read(int address) {
        if (address >= 0 && address < size) {
            return memory[address];
        } else {
            System.out.println("Erro: Endereço fora dos limites da memória.");
            return -1; // Retorna um valor inválido se o endereço for fora dos limites
        }
    }
}

