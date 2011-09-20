class EjecutorDeTransacciones extends Thread{       
    void depositar(Cuenta cuenta, int deposito){
	cuenta.balanceGeneral += deposito;
    }
    void descontar(Cuenta cuenta, int descuento){
	cuenta.balanceGeneral -= descuento;
    }
}
