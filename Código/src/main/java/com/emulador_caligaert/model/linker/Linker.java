package com.emulador_caligaert.model.linker;

public class Linker {
    private Map<String, Integer> tabelaDeSimbolos;
    private List<Module> modules;

    public Linker(List<Module> modules) {
        this.modules = modules;
        this.tabelaDeSimbolos = new HashMap<>();
    }

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
    }
}
