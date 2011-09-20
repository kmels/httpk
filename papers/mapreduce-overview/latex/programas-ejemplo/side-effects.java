class EjecutorDeTransacciones extends Thread{       
    void depositar(Cuenta cuenta, int deposito){
	cuenta.balanceGeneral += deposito;
    }
    void descontar(Cuenta cuenta, int descuento){
	cuenta.balanceGeneral -= descuento;
    }
}

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