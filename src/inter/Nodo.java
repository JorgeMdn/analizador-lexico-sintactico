/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inter;
import analizadorLexico.*;
/**
 *
 * @author jarvis
 */
public class Nodo {
    int linealex= 0;
    Nodo(){linealex = AnalizadorLexico.linea;}
    void error(String s){
        throw new Error("Cerca de la linea "+ linealex +": "+s);
    }
    static int etiquetas = 0;
    public int nuevaEtiqueta(){
        return ++etiquetas;
    }
    public void emitirEtiqueta(int i){
        System.out.print("L" + i + ":");
    }
    public void emitir(String s){
        System.out.print("\t" + s);
    }
            
}
