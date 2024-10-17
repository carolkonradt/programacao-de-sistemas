package com.emulador_caligaert.model.virtual_machine;

/**Classe responsável pela implementação dos registradores da máquina virtual
 * @author rboeira
 */
public class Register {
    private String data;
    private int size;

    /**Método construtor para inicializar o registrador com o tamanho em bits
     * @param sizeBits
     */
    public Register(int sizeBits) {
        this.size = sizeBits;
        this.data = "0"; // Inicialmente, o registrador contém 0
    }

    private int correctOverflow(int value){
        int maxValue = (int) (Math.pow(2, size-1) - 1);
        int minValue = (int) (-Math.pow(2, size-1));
        if (value > maxValue)
            return maxValue;
        if (value < minValue)
            return minValue;
        return value;
    }

    /**Método responsável por definir o valor do registrador
     * @param data - valor do registrador
     * @return void
     */
    public void setData(String data) {
        int value = 0;
        try{
            value = correctOverflow(Integer.parseInt(data));
            data = Integer.toString(value);
        } catch (Exception e){}

        this.data = data;
    }

    /**Método responsávle por obter o valor do registrador
     * @param -
     * @return valor do registrador em int (decimal)
     */
    public String getData() {
        return this.data;
    }

    public void clear(){
        this.data = "0";
    }
}