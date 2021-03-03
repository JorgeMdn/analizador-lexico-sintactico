/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inter;

import analizadorLexico.*;
import simbolos.*;

/**
 *
 * @author jarvis
 */
public class Op extends Expr {

    public Op(Token tok, Tipo p) {
        super(tok, p);
    }

    public Expr reducir() {
        Expr x = gen();
        Temp t = new Temp(tipo);
        emitir(t.toString() + " = " + x.toString());
        return t;
    }
}
