package Parsing;

public class Parser {

    private Analex analex;
    private ErrorMgr errorMgr;
    private String MSE = "Se esperaba: ";
    private String MF = "Falta: ";

    public Parser() {
        errorMgr = new ErrorMgr();
        analex = new Analex(errorMgr);
    }

    public void init(String progFuente) {
        errorMgr.init();
        analex.init(progFuente);

        programa();     //Llamar al símbolo inicial de la BNF.
    }

    public boolean hayError() {
        return errorMgr.hayError();
    }

    public void comunicarError() {
        onComunicar(errorMgr.getPosLexema(), errorMgr.getLexema().length(), errorMgr.getErrorMsj());
    }

    public void onComunicar(int posLexema, int longitud, String errorMsg) {
        //Overridable (Para el front-end)       
    }

//---------    
    private void programa() { //Símbolo inicial. programa->Header Cuerpo Main
        //Seguir única sección
        header();
        cuerpo();
        main();
    }

    private void header() {  //header -> PROGRAM ID; | lambda
        if (analex.preNom() == Token.PROGRAM) {
            //Seguir 1era seccion: PROGRAM ID ;
            match(Token.PROGRAM, "Se espera PROGRAM");
            match(Token.ID, "Se espera un ID");
            match(Token.PTOCOMA, "Falta ;");
        } else //Seguir 2da sección: lambda
          ;
    }

    // ---------------------------------------------------------------------------------
    private void cuerpo() {
        if (analex.preNom() == Token.VAR) {
            decl();
            cuerpo();
        } else if (analex.preNom() == Token.PROCEDURE) {
            proc();
            cuerpo();
        } else
            ;
    }

    /*  
     BNF DECLARACION
     decl -> VAR decl1 decl | L
     decl1 -> ID varMas : TIPO ; decl1 | L
     varMas -> , ID varMas | L
     */
    private void decl() {
        if (analex.preNom() == Token.VAR) {
            match(Token.VAR, MSE + "VAR");
            decl1();
            decl();
        } else 
            ;
    }

    private void decl1() {
        if (analex.preNom() == Token.ID) {
            match(Token.ID, MSE + "ID");
            varMas();
            match(Token.DOSPTOS, MSE + ":");
            match(Token.TIPO, MSE + "Tipo");
            match(Token.PTOCOMA, MF + ";");
            decl1();
        } else
            ;
    }

    private void varMas() {
        if (analex.preNom() == Token.COMA) {
            match(Token.COMA, MSE + ",");
            match(Token.ID, MSE + "ID");
            varMas();
        } else ;
    }

    /* 
     PROCEDIMIENTOS
     proc -> PROCEDURE ID ; BEGIN sent1 END ;
     sent1 -> sentencias | L
     */
    private void proc() {
        match(Token.PROCEDURE, MSE + "PROCEDURE");
        match(Token.ID, MSE + "ID");
        match(Token.PTOCOMA, MF + ";");
        match(Token.BEGIN, MSE + "BEGIN");
        sent1();
        match(Token.END, MSE + "END");
        match(Token.PTOCOMA, MF + ";");
    }

    // ---------------------------------------------------------------------------------
    private void main() {
        match(Token.BEGIN, MSE + "BEGIN");
        sent1();
        match(Token.END, MSE + "END");
        match(Token.PTO, MF + ".");
    }

    /*
     SENTENCIAS
     sent1 -> sentencias sent1 | L 
     */
    private void sent1() {
        if (analex.preNom() == Token.ID || analex.preNom() == Token.IF || analex.preNom() == Token.FOR
                || analex.preNom() == Token.WHILE || analex.preNom() == Token.REPEAT || analex.preNom() == Token.READLN
                || analex.preNom() == Token.WRITELN) {
            sentencias();
            sent1();
        } else ;
    }

    /*
     SENTENCIAS
     sentencias -> masAsig | condicional | BucleFor | BucleWhile | BucleRepeat | lectura | impresion
     */
    private void sentencias() {
        if (analex.preNom() == Token.ID) {
            masAsig();
        } else if (analex.preNom() == Token.IF) {
            condicional();
        } else if (analex.preNom() == Token.FOR) {
            BucleFor();
        } else if (analex.preNom() == Token.WHILE) {
            BucleWhile();
        } else if (analex.preNom() == Token.REPEAT) {
            BucleRepeat();
        } else if (analex.preNom() == Token.READLN) {
            lectura();
        } else {
            impresion();
        }
    }


    /*
     SENTENCIAS DE UNA SOLA LINEA
     sentLine -> impresion | lectura | masAsig
     */
    private void sentLine() {
        if (analex.preNom() == Token.WRITELN) {
            impresion();
        } else if (analex.preNom() == Token.READLN) {
            lectura();
        } else {
            masAsig();
        }
    }

    /*
     ASIGNACION Y LLAMADA
     masAsig -> ID llamAsig1 ;
     llamAsig1 ->  := Expr | ( )
     */
    private void masAsig() {
        match(Token.ID, MSE + "ID");
        llamAsig1();
        match(Token.PTOCOMA, MF + ";");

    }

    private void llamAsig1() {
        if (analex.preNom() == Token.ASSIGN) {
            match(Token.ASSIGN, MSE + ":=");
            Expr();
        } else {
            match(Token.PA, MSE + "(");
            match(Token.PC, MSE + ")");
        }
    }

    /*
     IMPRESION
     impresion -> WRITELN ( val masVal ) ;
     masVal -> , val masVal | L
     val -> STRINGctte | Expr
     */
    private void impresion() {
        match(Token.WRITELN, MSE + "WRITELN");
        match(Token.PA, MSE + "(");
        val();
        masVal();
        match(Token.PC, MSE + ")");
        match(Token.PTOCOMA, MF + ";");
    }

    private void val() {
        if (analex.preNom() == Token.STRINGctte) {
            match(Token.STRINGctte, MSE + "STRINGctte");
        } else {
            Expr();
        }
    }

    ;
    private void masVal() {
        if (analex.preNom() == Token.COMA) {
            match(Token.COMA, MSE + ",");
            val();
            masVal();
        } else ;
    }

    /*
     LECTURA
     lectura -> READLN ( ID masID ) ;
     masID -> , ID masID | L
     */
    private void lectura() {
        match(Token.READLN, MSE + "READLN");
        match(Token.PA, MSE + "(");
        match(Token.ID, MSE + "ID");
        masID();
        match(Token.PC, MSE + ")");
        match(Token.PTOCOMA, MF + ";");
    }

    private void masID() {
        if (analex.preNom() == Token.COMA) {
            match(Token.COMA, MSE + ",");
            match(Token.ID, MSE + "ID");
            masID();
        } else ;
    }

    /* 
     CONDICIONAL
     condicional -> IF ExpreBoole THEN cond1
     cond1 -> BEGIN sentencias sent1 END ifelse | sentLine ifelse2
     ifelse -> ELSE ifelse1 | ;
     ifelse2 -> ELSE ifelse1 | L
     ifelse1 -> BEGIN sentencias END ; | sentLine
     */
    private void condicional() {
        match(Token.IF, MSE + "IF");
        ExpreBoole();
        match(Token.THEN, MSE + "THEN");
        cond1();
    }

    private void cond1() {
        if (analex.preNom() == Token.BEGIN) {
            match(Token.BEGIN, MSE + "BEGIN");
            sentencias();
            sent1();
            match(Token.END, MSE + "END");
            ifelse();
        } else {
            sentLine();
            ifelse2();
        }
    }

    private void ifelse() {
        if (analex.preNom() == Token.ELSE) {
            match(Token.ELSE, MSE + "ELSE");
            ifelse1();
        } else {
            match(Token.PTOCOMA, MF + ";");
        }
    }

    private void ifelse2() {
        if (analex.preNom() == Token.ELSE) {
            match(Token.ELSE, MSE + "ELSE");
            ifelse1();
        } else ;
    }

    private void ifelse1() {
        if (analex.preNom() == Token.BEGIN) {
            match(Token.BEGIN, MSE + "BEGIN");
            sentencias();
            sent1();
            match(Token.END, MSE + "END");
            match(Token.PTOCOMA, MF + ";");
        } else {
            sentLine();
        }
    }

    /*
     BUCLEFOR
     BucleFor -> FOR ID := Expr tipo Expr DO BF1
     tipo -> TO | DOWNTO
     BF1 -> BEGIN sentencias sent1 END | sentLine
     */
    private void BucleFor() {
        match(Token.FOR, MSE + "FOR");
        match(Token.ID, MSE + "ID");
        match(Token.ASSIGN, MSE + ":=");
        Expr();
        tipo();
        Expr();
        match(Token.DO, MSE + "DO");
        BF1();
    }

    private void tipo() {
        if (analex.preNom() == Token.TO) {
            match(Token.TO, MSE + "TO");
        } else {
            match(Token.DOWNTO, MSE + "DOWNTO");
        }
    }

    private void BF1() {
        if (analex.preNom() == Token.BEGIN) {
            match(Token.BEGIN, MSE + "BEGIN");
            sentencias();
            sent1();
            match(Token.END, MSE + "END");
            match(Token.PTOCOMA, MF + ";");
        } else {
            sentLine();
        }
    }

    /*
     BUCLEWHILE
     BucleWhile -> WHILE ExpreBoole DO BW1
     BW -> BEGIN sentencias sent1 END ; | sentLine
     */
    private void BucleWhile() {
        match(Token.WHILE, MSE + "WHILE");
        ExpreBoole();
        match(Token.DO, MSE + "DO");
        BW1();
    }

    private void BW1() {
        if (analex.preNom() == Token.BEGIN) {
            match(Token.BEGIN, MSE + "BEGIN");
            sentencias();
            sent1();
            match(Token.END, MSE + "END");
            match(Token.PTOCOMA, MF + ";");
        } else {
            sentLine();
        }
    }

    /*
     BUCLEREPEAT
     BucleRepeat -> REPEAT sent1 UNTIL ExpreBoole ;
     */
    private void BucleRepeat() {
        match(Token.REPEAT, MSE + "REPEAT");
        sent1();
        match(Token.UNTIL, MSE + "UNTIL");
        ExpreBoole();
        match(Token.PTOCOMA, MF + ";");
    }

    // EXPRESIONES ARITMETICAS
    private void Expr() {
        Termino();
        expr1();
    }

    private void expr1() {
        if (analex.preNom() == Token.MAS || analex.preNom() == Token.MENOS) {
            expr2();
            expr1();
        } else ;
    }

    private void expr2() {
        if (analex.preNom() == Token.MAS) {
            match(Token.MAS, MSE + "+");
            Termino();
        } else {
            match(Token.MENOS, MSE + "-");
            Termino();
        }
    }

    private void Termino() {
        Factor();
        term1();
    }

    private void term1() {
        if (analex.preNom() == Token.POR || analex.preNom() == Token.DIV || analex.preNom() == Token.MOD) {
            term2();
            term1();
        } else 
            ;
    }

    private void term2() {
        if (analex.preNom() == Token.POR) {
            match(Token.POR, MSE + "*");
            Factor();
        } else if (analex.preNom() == Token.DIV) {
            match(Token.DIV, MSE + "/");
            Factor();
        } else {
            match(Token.MOD, MSE + "MOD");
            Factor();
        }
    }

    private void Factor() {
        if (analex.preNom() == Token.ID) {
            match(Token.ID, MSE + "ID");
        } else if (analex.preNom() == Token.NUM) {
            match(Token.NUM, MSE + "NUM");
        } else if (analex.preNom() == Token.MENOS) {
            match(Token.MENOS, MSE + "-");
            Factor();
        } else if (analex.preNom() == Token.MAS) {
            match(Token.MAS, MSE + "+");
            Factor();
        } else {
            match(Token.PA, MSE + "(");
            Expr();
            match(Token.PC, MSE + ")");
        }
    }

    // EXPRESIONES BOOLEANAS
    private void ExpreBoole() {
        TermBoole();
        ExprB1();
    }

    private void ExprB1() {
        if (analex.preNom() == Token.OR) {
            match(Token.OR, MSE + "OR");
            TermBoole();
            ExprB1();
        } else ;
    }

    private void TermBoole() {
        FactorBoole();
        termB1();
    }

    private void termB1() {
        if (analex.preNom() == Token.AND) {
            match(Token.AND, MSE + "AND");
            FactorBoole();
            termB1();
        } else ;
    }

    private void FactorBoole() {
        if (analex.preNom() == Token.NOT) {
            match(Token.NOT, MSE + "NOT");
            FactorBoole();
        } else {
            Expr();
            match(Token.OPREL, MSE + "OPREL");
            Expr();
        }
    }

    // ---------------------------------------------------------------------------------
    private void match(int tokenNom, String errorMsj) {
        if (analex.preNom() == tokenNom) {
            analex.avanzar();
        } else {
            errorMgr.setError(errorMsj, analex.getPosLexema(), analex.lexema());
        }
    }
}
