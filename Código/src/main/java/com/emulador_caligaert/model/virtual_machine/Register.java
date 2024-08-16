package com.emulador_caligaert.model.virtual_machine;

import static java.lang.Math.pow;

/**Classe responsável pela implementação dos registradores da máquina virtual
 * @author rboeira
 */
class Register {
    private int data;
    private int size;

    /**Método construtor para inicializar o registrador com o tamanho em bits
     * @param sizeBits
     */
    public Register(int sizeBits) {
        this.size = sizeBits;
        this.data = 0; // Inicialmente, o registrador contém 0
    }

    /**Método responsável por definir o valor do registrador
     * @param data - valor do registrador
     * @return void
     */
    public void setData(int data) {
        this.data = data & ((int)pow(2, size));
    }

    /**Método responsávle por obter o valor do registrador
     * @param -
     * @return valor do registrador em int (decimal)
     */
    public int getData() {
        return this.data;
    }
}