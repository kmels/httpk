class Cuenta{
    public int balanceGeneral;
    public Cuenta(int balanceInicial){
	this.balanceGeneral = balanceInicial;
    }
    void depositar(int deposito){
	this.balanceGeneral += deposito;
    }
    void descontar(int descuento){
	this.balanceGeneral -= descuento;
    }
}