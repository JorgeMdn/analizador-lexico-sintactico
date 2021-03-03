public class Main {

    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        AnalizadorLexico lex = new AnalizadorLexico();
        Analizador analizar = new Analizador(lex);
        analizar.programa();
        System.out.write('\n');
    }
}

// Analizador lexico
public class AnalizadorLexico {

    public static int linea = 1;
    char preanalisis = ' ';
    Hashtable palabras = new Hashtable();

    void reservar(Palabra w) {
        palabras.put(w.lexema, w);
    }

    public AnalizadorLexico() {
        reservar(new Palabra("if", Etiqueta.IF));
        reservar(new Palabra("else", Etiqueta.IF));
        reservar(new Palabra("while", Etiqueta.IF));
        reservar(new Palabra("do", Etiqueta.IF));
        reservar(new Palabra("break", Etiqueta.IF));
        reservar(Palabra.True);
        reservar(Palabra.False);
        reservar(Tipo.Int);
        reservar(Tipo.Char);
        reservar(Tipo.Bool);
        reservar(Tipo.Float);
    }
    
    // se utiliza para leer el siguiente caracter de entrada y colocarlo en el preanalisis
    void readch()  
     throws IOException { preanalisis = (char) System.in.read();}
    
    
    // reconoce tokens compuestos
    boolean readch(char c) throws IOException {
        readch();
        if (preanalisis != c) {
            return false;
        }
        preanalisis = ' ';
        return true;
    }
    
    public Token explorar() throws IOException {
        for( ; ; readch()) {
            if (preanalisis == ' ' || preanalisis == '\t') continue;
            else if(preanalisis == '\n') linea = linea +1;
            else break;
        }
        switch( preanalisis ){
            case '&':
            if( readch('&') ) return Palabra.and; else return new Token('&');
            case '|':
            if( readch('|') ) return Palabra.or; else return new Token('|');
            case '=':
            if( readch('=') ) return Palabra.eq; else return new Token('=');
            case '!':
            if( readch('=') ) return Palabra.ne; else return new Token('!');
            case '<':
            if( readch('=') ) return Palabra.le; else return new Token('<');
            case '>':
            if( readch('=') ) return Palabra.ge; else return new Token('>');
        }
        if(Character.isDigit(preanalisis)){
            int v= 0;
            do{
                v = 10* v + Character.digit(preanalisis,10); readch();
            }while(Character.isDigit(preanalisis));
            if (preanalisis != '.') return new Num(v);
            float x = v; float d = 10;
            for(;;){
                readch();
                if(! Character.isDigit(preanalisis)) break;
                x = x + Character.digit(preanalisis, 10) / d; d = d*10;
            }
            return new Real(x);
        }
        if(Character.isLetterOrDigit(preanalisis)){
            StringBuffer b = new StringBuffer();
            do{
                b.append(preanalisis); readch();
            }while(Character.isLetterOrDigit(preanalisis));
            String s = b.toString();
            Palabra w = (Palabra)palabras.get(s);
            if(w != null)return w;
            w = new Palabra(s,Etiqueta.ID);
            palabras.put(s, w);
            return w;
        }
        Token tok = new Token(preanalisis); preanalisis = ' ';
        return tok;
        
        
    }
}
public class Etiqueta {

    public final static int 
            AND = 256, BASIC = 257, BREAK = 258, DO = 259, ELSE = 260,
            EQ = 261, FALSE = 262, GE = 263, ID = 264, IF = 265,
            INDEX = 266, LE = 267, MINUS = 268, NE = 269, NUM = 270, 
            OR = 271, REAL = 272, TEMP = 273, TRUE = 274, WHILE = 275;
}
public class Num extends Token {

    public final int valor;

    public Num(int v) {
        super(Etiqueta.NUM);
        valor = v;
    }

    public String toString() {
        return "" + valor;
    }
}
public class Palabra extends Token {

    public String lexema = "";

    public Palabra(String s, int etiqueta) {
        super(etiqueta);
        lexema = s;
    }
    public static final Palabra 
            and = new Palabra("&&", Etiqueta.AND), or = new Palabra("||", Etiqueta.OR), eq = new Palabra("==", Etiqueta.EQ), 
            ne = new Palabra("!=", Etiqueta.NE), le = new Palabra("<=", Etiqueta.LE), ge = new Palabra(">=", Etiqueta.GE), 
            minus = new Palabra("minus", Etiqueta.MINUS), True = new Palabra("true", Etiqueta.TRUE), 
            False = new Palabra("false", Etiqueta.FALSE), temp = new Palabra("t", Etiqueta.TEMP);
}
public class Real extends Token {

    public final float valor;

    public Real(float v) {
        super(Etiqueta.REAL);
        valor = v;
    }

    public String toString() {
        return "" + valor;
    }
}
public class Token {

    public final int etiqueta;

    public Token(int t) {
        etiqueta = t;
    }

    public String toString() {
        return "" + (char) etiqueta;
    }
}
// Analizados sintactico
public class Analizador {

    private AnalizadorLexico lex; //analizador lexico para este analizador sintactico
    private Token busca; // marca de busqueda por adelantado
    Ent sup = null; // tabla de simbolos actual o superior
    int usado = 0; // almacenamiento usado para las declaraciones

    public Analizador(AnalizadorLexico analizadorLexico) throws IOException {
        lex = analizadorLexico;
        mover();
    }

    void mover() throws IOException {
        busca = lex.explorar();
    }

    void error(String s) {
        throw new Error("cerca de linea " + lex.linea + ": " + s);
    }

    void coincidir(int t) throws IOException {
        if (busca.etiqueta == t) {
            mover();
        } else {
            error("error de sintaxis");
        }

    }

    public void programa() throws IOException { // programa -> bloque
        Instr s = bloque();
        int inicio = s.nuevaEtiqueta();
        int despues = s.nuevaEtiqueta();
        s.emitirEtiqueta(inicio);
        s.gen(inicio, despues);
        s.emitirEtiqueta(despues);
    }

    Instr bloque() throws IOException { // bloque -> { decls instrs }
        coincidir(
                '{');
        Ent entGuardado = sup;
        sup = new Ent(sup);
        decls();
        Instr s = instrs();
        coincidir(
                '}');
        sup = entGuardado;
        return s;
    }

    void decls() throws IOException {
        while (busca.etiqueta == Etiqueta.BASIC) { // D -> tipo ID;
            Tipo p = tipo();
            Token tok = busca;
            coincidir(Etiqueta.ID);
            coincidir(
                    ';');
            Id id = new Id((Palabra) tok, p, usado);
            sup.put(tok, id);
            usado = usado + p.anchura;
        }
    }

    Tipo tipo() throws IOException {
        Tipo p = (Tipo) busca; // espera busca.etiqueta == Etiqueta.BASIC
        coincidir(Etiqueta.BASIC);
        if (busca.etiqueta != '[') {
            return p;
        } else {
            return dims(p); // devuelve el tipo del arreglo
        }
    }

    Tipo dims(Tipo p) throws IOException {
        coincidir(
                '[');
        Token tok = busca;
        coincidir(Etiqueta.NUM);
        coincidir(
                ']');
        if (busca.etiqueta == '[') {
            p = dims(p);
        }
        return new Arreglo(((Num) tok).valor, p);
    }

    Instr instrs() throws IOException {
        if (busca.etiqueta == '}') {
            return Instr.Null;
        } else {
            return new Sec(instr(), instrs());
        }
    }

    Instr instr() throws IOException {
        Expr x;
        Instr s, s1, s2;
        Instr instrGuardada; // guarda ciclo circundante p/instrucciones break
        switch (busca.etiqueta) {
            case ';':
                mover();
                return Instr.Null;
            case Etiqueta.IF:
                coincidir(Etiqueta.IF);
                coincidir('(');
                x = bool();
                coincidir(')');
                s1 = instr();
                if (busca.etiqueta != Etiqueta.ELSE) {
                    return new If(x, s1);
                }
                coincidir(Etiqueta.ELSE);
                s2 = instr();
                return new Else(x, s1, s2);
            case Etiqueta.WHILE:
                While nodowhile = new While();
                instrGuardada = Instr.Circundante;
                Instr.Circundante = nodowhile;
                coincidir(Etiqueta.WHILE);
                coincidir('(');
                x = bool();
                coincidir(')');
                s1 = instr();
                nodowhile.inic(x, s1);
                Instr.Circundante = instrGuardada; // restablece Instr.Circundante
                return nodowhile;
            case Etiqueta.DO:
                Do nododo = new Do();
                instrGuardada = Instr.Circundante;
                Instr.Circundante = nododo;
                coincidir(Etiqueta.DO);
                s1 = instr();
                coincidir(Etiqueta.WHILE);
                coincidir('(');
                x = bool();
                coincidir(')');
                coincidir(';');
                nododo.inic(s1, x);
                Instr.Circundante = instrGuardada; // restablece Instr.Circundante
                return nododo;
            case Etiqueta.BREAK:
                coincidir(Etiqueta.BREAK);
                coincidir(';');
                return new Break();
            case '{':
                return bloque();
            default:
                return asignar();
        }
    }

    Instr asignar() throws IOException {
        Instr instr;
        Token t = busca;
        coincidir(Etiqueta.ID);
        Id id = sup.get(t);
        if (id == null) {
            error(t.toString() + " no declarado");
        }
        if (busca.etiqueta == '=') { // S -> id = E ;
            mover();
            instr = new Est(id, bool());
        } else { // S -> L = E ;
            Acceso x = desplazamiento(id);
            coincidir('=');
            instr = new EstElem(x, bool());
        }
        coincidir(';');
        return instr;
    }

    Expr bool() throws IOException {
        Expr x = unir();
        while (busca.etiqueta == Etiqueta.OR) {
            Token tok = busca;
            mover();
            x = new Or(tok, x, unir());
        }
        return x;
    }

    Expr unir() throws IOException {
        Expr x = igualdad();
        while (busca.etiqueta == Etiqueta.AND) {
            Token tok = busca;
            mover();
            x = new And(tok, x, igualdad());
        }
        return x;
    }

    Expr igualdad() throws IOException {
        Expr x = rel();
        while (busca.etiqueta == Etiqueta.EQ || busca.etiqueta == Etiqueta.NE) {
            Token tok = busca;
            mover();
            x = new Rel(tok, x, rel());
        }
        return x;
    }

    Expr rel() throws IOException {
        Expr x = expr();
        switch (busca.etiqueta) {
            case '<':
            case Etiqueta.LE:
            case Etiqueta.GE:
            case '>':
                Token tok = busca;
                mover();
                return new Rel(tok, x, expr());
            default:
                return x;
        }
    }

    Expr expr() throws IOException {
        Expr x = term();
        while (busca.etiqueta == '+' || busca.etiqueta == '-') {
            Token tok = busca;
            mover();
            x = new Arit(tok, x, term());
        }
        return x;
    }

    Expr term() throws IOException {
        Expr x = unario();
        while (busca.etiqueta == '*' || busca.etiqueta == '/') {
            Token tok = busca;
            mover();
            x = new Arit(tok, x, unario());
        }
        return x;
    }

    Expr unario() throws IOException {
        if (busca.etiqueta == '-') {
            mover();
            return new Unario(Palabra.minus, unario());
        } else if (busca.etiqueta == '!') {
            Token tok = busca;
            mover();
            return new Not(tok, unario());
        } else {
            return factor();
        }
    }

    Expr factor() throws IOException {
        Expr x = null;
        switch (busca.etiqueta) {
            case '(':
                mover();
                x = bool();
                coincidir(')');
                return x;
            case Etiqueta.NUM:
                x = new Constante(busca, Tipo.Int);
                mover();
                return x;
            case Etiqueta.REAL:
                x = new Constante(busca, Tipo.Float);
                mover();
                return x;
            case Etiqueta.TRUE:
                x = Constante.True;
                mover();
                return x;
            case Etiqueta.FALSE:
                x = Constante.False;
                mover();
                return x;
            default:
                error("error de sintaxis");
                return x;
            case Etiqueta.ID:
                String s = busca.toString();
                Id id = sup.get(busca);
                if (id == null) {
                    error(busca.toString() + " no declarado");
                }
                mover();
                if (busca.etiqueta != '[') {
                    return id;
                } else {
                    return desplazamiento(id);
                }
        }
    }

    Acceso desplazamiento(Id a) throws IOException { // I -> [E] | [E] I
        Expr i;
        Expr w;
        Expr t1, t2;
        Expr ubic; // hereda id
        Tipo tipo = a.tipo;
        coincidir('[');
        i = bool();
        coincidir(']'); // primer indice, I -> [ E ]
        tipo = ((Arreglo) tipo).de;
        w = new Constante(tipo.anchura);
        t1 = new Arit(new Token('*'), i, w);
        ubic = t1;
        while (busca.etiqueta == '[') { // multi-dimensional I -> [ E ] I
            coincidir('[');
            i = bool();
            coincidir('[');
            tipo = ((Arreglo) tipo).de;
            w = new Constante(tipo.anchura);
            t1 = new Arit(new Token('*'), i, w);
            t2 = new Arit(new Token('+'), ubic, t1);
            ubic = t2;
        }
        return new Acceso(a, ubic, tipo);
    }

}

public class And extends Logica {

    public And(Token tok, Expr x1, Expr x2) {
        super(tok, x1, x2);
    }

    public void salto(int t, int f) {
        int etiqueta = f != 0 ? f : nuevaEtiqueta();
        expr1.salto(etiqueta, 0);
        expr2.salto(t, f);
        if (f == 0) {
            emitirEtiqueta(etiqueta);
        }
    }
}

public class Arit extends Op {

    public Expr expr1, expr2;

    public Arit(Token tok, Expr x1, Expr x2) {
        super(tok, null);
        expr1 = x1;
        expr2 = x2;
        tipo = Tipo.max(expr1.tipo, expr2.tipo);
        if (tipo == null) {
            error("error de tipo");
        }
    }

    public Expr gen() {
        return new Arit(op, expr1.reducir(), expr2.reducir());
    }

    public String toString() {
        return expr1.toString() + " " + op.toString() + " " + expr2.toString();
    }
}

public class Break extends Instr {

    Instr instr;

    public Break() {
        if (Instr.Circundante == null) {
            error("break no encerrada");
        }
        instr = Instr.Circundante;
    }

    public void gen(int b, int a) {
        emitir("goto L" + instr.despues);
    }
}

public class Constante extends Expr {

    public Constante(Token tok, Tipo p) {
        super(tok, p);
    }

    public Constante(int i) {
        super(new Num(i), Tipo.Int);
    }
    public static final Constante True = new Constante(Palabra.True, Tipo.Bool),
            False = new Constante(Palabra.False, Tipo.Bool);

    public void salto(int t, int f) {
        if (this == True && t != 0) {
            emitir("goto L" + t);
        } else if (this == False && f != 0) {
            emitir("goto L" + f);
        }
    }
}

public class Do extends Instr {

    Expr expr;
    Instr instr;

    public Do() {
        expr = null;
        instr = null;
    }

    public void inic(Instr s, Expr x) {
        expr = x;
        instr = s;
        if (expr.tipo != Tipo.Bool) {
            expr.error("se requiere booleano en do");
        }
    }

    public void gen(int b, int a) {
        despues = a;
        int etiqueta = nuevaEtiqueta(); // etiqueta para expr
        instr.gen(b, etiqueta);
        emitirEtiqueta(etiqueta);
        expr.salto(b, 0);
    }
}

public class Else extends Instr {

    Expr expr;
    Instr instr1, instr2;

    public Else(Expr x, Instr s1, Instr s2) {
        expr = x;
        instr1 = s1;
        instr2 = s2;
        if (expr.tipo != Tipo.Bool) {
            expr.error("se requiere booleano en if");
        }
    }

    public void gen(int b, int a) {
        int etiqueta1 = nuevaEtiqueta(); // etiqueta1 para instr1
        int etiqueta2 = nuevaEtiqueta(); // etiqueta2 para instr2
        expr.salto(0, etiqueta2); // pasa hacia instr1 en true
        emitirEtiqueta(etiqueta1);
        instr1.gen(etiqueta1, a);
        emitir("goto L" + a);
        emitirEtiqueta(etiqueta2);
        instr2.gen(etiqueta2, a);
    }
}

public class Est extends Instr {

    public Id id;
    public Expr expr;

    public Est(Id i, Expr x) {
        id = i;
        expr = x;
        if (comprobar(id.tipo, expr.tipo) == null) {
            error("error de tipo");
        }
    }

    public Tipo comprobar(Tipo p1, Tipo p2) {
        if (Tipo.numerico(p1) && Tipo.numerico(p2)) {
            return p2;
        } else if (p1 == Tipo.Bool && p2 == Tipo.Bool) {
            return p2;
        } else {
            return null;
        }
    }

    public void gen(int b, int a) {
        emitir(id.toString() + " = " + expr.gen().toString());
    }
}

public class EstElem extends Instr {

    public Id arreglo;
    public Expr indice;
    public Expr expr;

    public EstElem(Acceso x, Expr y) {
        arreglo = x.arreglo;
        indice = x.indice;
        expr = y;
        if (comprobar(x.tipo, expr.tipo) == null) {
            error("error de tipo");
        }
    }

    public Tipo comprobar(Tipo p1, Tipo p2) {
        if (p1 instanceof Arreglo || p2 instanceof Arreglo) {
            return null;
        } else if (p1 == p2) {
            return p2;
        } else if (Tipo.numerico(p1) && Tipo.numerico(p2)) {
            return p2;
        } else {
            return null;
        }
    }

    public void gen(int b, int a) {
        String s1 = indice.reducir().toString();
        String s2 = expr.reducir().toString();
        emitir(arreglo.toString() + " [ " + s1 + " ] = " + s2);
    }
}

public class Expr extends Nodo {
    public Token op;
    public Tipo tipo;
    Expr(Token tok, Tipo p){
        op = tok;
        tipo = p;
    }
    
    public Expr gen() {
        return this;
    }
    public Expr reducir(){
        return this;
    }
    public void salto(int t, int f){
        emitirsaltos(toString(),t,f);
    }
    public void emitirsaltos(String prueba, int t, int f){
        if(t != 0 && f != 0){
            emitir("if" + prueba + " goto L"+t);
            emitir("goto L" + f);
        }
        else if (t != 0) emitir("if " + prueba + " goto L" + t);
        else if (f != 0) emitir("iffalse " + prueba + " goto L" + f);
        else; // nada ya que tanto t como f pasan directo
    }
    public String toString() {
        return op.toString();
    }
}

public class Id extends Expr {
    public int desplazamiento;
    public Id(Palabra id, Tipo p, int b) {
        super(id,p); 
        desplazamiento = b;
    }
}

public class If extends Instr {

    Expr expr;
    Instr instr;

    public If(Expr x, Instr s) {
        expr = x;
        instr = s;
        if (expr.tipo != Tipo.Bool) {
            expr.error("se requiere booleano en if");
        }
    }

    public void gen(int b, int a) {
        int etiqueta = nuevaEtiqueta(); // etiqueta para el codigo de instr
        expr.salto(0, a); // pasa por alto en true, va hacia a en false
        emitirEtiqueta(etiqueta);
        instr.gen(etiqueta, a);
    }
}

public class Instr extends Nodo {

    public Instr() {
    }
    public static Instr Null = new Instr();

    public void gen(int b, int a) {
    } // se llama con etiquetas inicio y despues
    int despues = 0; // almacena la etiqueta despues
    public static Instr Circundante = Instr.Null; // se utiliza para instrs break
}

public class Logica extends Expr {

    public Expr expr1, expr2;

    Logica(Token tok, Expr x1, Expr x2) {
        super(tok, null);
        expr1 = x1;
        expr2 = x2;
        tipo = comprobar(expr1.tipo, expr2.tipo);
        if (tipo == null) {
            error("error de tipo");
        }
    }

    public Tipo comprobar(Tipo p1, Tipo p2) {
        if (p1 == Tipo.Bool && p2 == Tipo.Bool) {
            return Tipo.Bool;
        } else {
            return null;
        }
    }

    public Expr gen() {
        int f = nuevaEtiqueta();
        int a = nuevaEtiqueta();
        Temp temp = new Temp(tipo);
        this.salto(0, f);
        emitir(temp.toString() + " = true");
        emitir("goto L" + a);
        emitirEtiqueta(f);
        emitir(temp.toString() + " = false");
        emitirEtiqueta(a);
        return temp;
    }

    public String toString() {
        return expr1.toString() + " " + op.toString() + " " + expr2.toString();
    }
}

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

public class Not extends Logica {

    public Not(Token tok, Expr x2) {
        super(tok, x2, x2);
    }

    public void salto(int t, int f) {
        expr2.salto(f, t);
    }

    public String toString() {
        return op.toString() + " " + expr2.toString();
    }
}

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

public class Or extends Logica {

    public Or(Token tok, Expr x1, Expr x2) {
        super(tok, x1, x2);
    }

    public void salto(int t, int f) {
        int etiqueta = t != 0 ? t : nuevaEtiqueta();
        expr1.salto(etiqueta, 0);
        expr2.salto(t, f);
        if (t == 0) {
            emitirEtiqueta(etiqueta);
        }
    }
}

public class Rel extends Logica {

    public Rel(Token tok, Expr x1, Expr x2) {
        super(tok, x1, x2);
    }

    public Tipo comprobar(Tipo p1, Tipo p2) {
        if (p1 instanceof Arreglo || p2 instanceof Arreglo) {
            return null;
        } else if (p1 == p2) {
            return Tipo.Bool;
        } else {
            return null;
        }
    }

    public void salto(int t, int f) {
        Expr a = expr1.reducir();
        Expr b = expr2.reducir();
        String prueba = a.toString() + " " + op.toString() + " " + b.toString();
        emitirsaltos(prueba, t, f);
    }
}

public class Sec extends Instr {

    Instr instr1;
    Instr instr2;

    public Sec(Instr s1, Instr s2) {
        instr1 = s1;
        instr2 = s2;
    }

    public void gen(int b, int a) {
        if (instr1 == Instr.Null) {
            instr2.gen(b, a);
        } else if (instr2 == Instr.Null) {
            instr1.gen(b, a);
        } else {
            int etiqueta = nuevaEtiqueta();
            instr1.gen(b, etiqueta);
            emitirEtiqueta(etiqueta);
            instr2.gen(etiqueta, a);
        }
    }
}

public class Temp extends Expr {

    static int conteo = 0;
    int numero = 0;

    public Temp(Tipo p) {
        super(Palabra.temp, p);
        numero = ++conteo;
    }

    public String toString() {
        return "t" + numero;
    }

}
public class Unario extends Op {

    public Expr expr;

    public Unario(Token tok, Expr x) { // maneja el menos, para ! vea Not
        super(tok, null);
        expr = x;
        tipo = Tipo.max(Tipo.Int, expr.tipo);
        if (tipo == null) {
            error("error de tipo");
        }
    }

    public Expr gen() {
        return new Unario(op, expr.reducir());
    }

    public String toString() {
        return op.toString() + " " + expr.toString();
    }
}

public class While extends Instr {

    Expr expr;
    Instr instr;

    public While() {
        expr = null;
        instr = null;
    }

    public void inic(Expr x, Instr s) {
        expr = x;
        instr = s;
        if (expr.tipo != Tipo.Bool) {
            expr.error("se requiere booleano en while");
        }
    }

    public void gen(int b, int a) {
        despues = a; // guarda la etiqueta a
        expr.salto(0, a);
        int etiqueta = nuevaEtiqueta(); // etiqueta para instr
        emitirEtiqueta(etiqueta);
        instr.gen(etiqueta, b);
        emitir("goto L" + b);
    }
}

public class Arreglo extends Tipo {

    public Tipo de;
    public int tamanio = 1;

    public Arreglo(int tm, Tipo p) {
        super("[]", Etiqueta.INDEX, tm * p.anchura);
        tamanio = tm;
        de = p;
    }

    public String toString() {
        return "[" + tamanio + "]" + de.toString();
    }
}

public class Ent {

    private Hashtable tabla;
    protected Ent ant;

    public Ent(Ent n) {
        tabla = new Hashtable();
        ant = n;
    }

    public void put(Token w, Id i) {
        tabla.put(w, i);
    }

    public Id get(Token w) {
        for (Ent e = this; e != null; e = e.ant) {
            Id encontro = (Id) (e.tabla.get(w));
            if (encontro != null) {
                return encontro;
            }
        }
        return null;
    }
}

public class Tipo extends Palabra {

    public int anchura = 0; // anchura se usa para asingación de almacenamiento

    public Tipo(String s, int etiqueta, int w) {
        super(s, etiqueta);
        anchura = w;
    }
    public static final Tipo Int = new Tipo("int", Etiqueta.BASIC, 4),
            Float = new Tipo("float", Etiqueta.BASIC, 8),
            Char = new Tipo("char", Etiqueta.BASIC, 1),
            Bool = new Tipo("bool", Etiqueta.BASIC, 1);

    // se utiliza para las conversiones de tipos (char, int y float)
    public static boolean numerico(Tipo p) {
        if (p == Tipo.Char || p == Tipo.Int || p == Tipo.Float) {
            return true;
        } else {
            return false;
        }
    }

    public static Tipo max(Tipo p1, Tipo p2) {
        if (!numerico(p1) || !numerico(p2)) {
            return null;
        } else if (p1 == Tipo.Float || p2 == Tipo.Float) {
            return Tipo.Float;
        } else if (p1 == Tipo.Int || p2 == Tipo.Int) {
            return Tipo.Int;
        } else {
            return Tipo.Char;
        }
    }
}












