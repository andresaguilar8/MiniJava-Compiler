package SyntaxAnalyzer;

import LexicalAnalyzer.LexicalAnalyzer;
import LexicalAnalyzer.Token;
import LexicalAnalyzer.LexicalException;
import java.io.IOException;
import java.util.Arrays;

public class SyntaxAnalyzer {

    private LexicalAnalyzer lexicalAnalyzer;
    private Token currentToken;

    public SyntaxAnalyzer(LexicalAnalyzer lexicalAnalyzer) throws LexicalException, IOException, SyntaxException {
        this.lexicalAnalyzer = lexicalAnalyzer;
        this.currentToken = this.lexicalAnalyzer.nextToken();
        this.inicial();
    }

    public void match(String tokenId) throws SyntaxException, IOException, LexicalException {
        if (tokenId.equals(this.currentToken.getTokenId())) {
            this.currentToken = this.lexicalAnalyzer.nextToken();
        } else
            throw new SyntaxException(this.currentToken, tokenId);
    }

    public void inicial() throws LexicalException, IOException, SyntaxException {
        this.listaClases();
        match("EOF");
    }

    public void listaClases() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_class", "pr_interface").contains(this.currentToken.getTokenId())) {
            this.clase();
            this.listaClases();
        } else {
            //epsilon, no hago nada
        }
    }

    public void clase() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("pr_class")) {
            this.claseConcreta();
        } else if (this.currentToken.getTokenId().equals("pr_interface")) {
            this.interface_();
        } else
            throw new SyntaxException(this.currentToken, "class o interface");
    }

    public void claseConcreta() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("pr_class")) {
            this.match("pr_class");
            this.match("idClase");
            this.heredaDe();
            this.implementaA();
            match("{");
            this.listaMiembros();
            match("}");
        } else
            throw new SyntaxException(this.currentToken, "pr_class");
    }

    public void interface_() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("pr_interface")) {
            this.match("pr_interface");
            this.match("idClase");
            this.extiendeA();
            match("{");
            this.listaEncabezados();
            match("}");
        } else
            throw new SyntaxException(this.currentToken, "interface");
    }

    public void heredaDe() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("pr_extends")) {
            match("pr_extends");
            match("idClase");
        } else {
            // epsilon, no hago nada
        }
    }

    public void implementaA() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("pr_implements")) {
            this.match("pr_implements");
            this.listaTipoReferencia();
        } else {
            // epsilon, no hago nada
        }
    }

    public void extiendeA() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("pr_extends")) {
            this.match("pr_extends");
            this.listaTipoReferencia();
        } else {
            //epsilon, no hago nada
        }
    }

    public void listaTipoReferencia() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("idClase")) {
            this.match("idClase");
            this.listaTipoReferenciaPrima();
        } else
            throw new SyntaxException(this.currentToken, "idClase");
    }

    public void listaTipoReferenciaPrima() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals(",")) {
            this.match(",");
            this.listaTipoReferencia();
        } else {
            //epsilon, no hago nada
        }
    }

    public void listaMiembros() throws LexicalException, SyntaxException, IOException {
        if (Arrays.asList("pr_public", "pr_private", "pr_static", "pr_void", "idClase", "pr_boolean", "pr_char", "pr_int").contains(this.currentToken.getTokenId())) {
            this.miembro();
            this.listaMiembros();
        } else {
            //epsilon, no hago nada
        }
    }

    public void listaEncabezados() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_static", "pr_void", "idClase", "pr_boolean", "pr_char", "pr_int").contains(this.currentToken.getTokenId())) {
            this.encabezadoMetodo();
            this.match(";");
            this.listaEncabezados();
        }
        else {
            //epsilon, no hago nada
            }
    }

    public void miembro() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_public", "pr_private").contains(this.currentToken.getTokenId()))
            this.atributo();
        else
            if (Arrays.asList("pr_static", "pr_void", "idClase", "pr_boolean", "pr_char", "pr_int").contains(this.currentToken.getTokenId()))
                this.metodo();
        else
            throw new SyntaxException(this.currentToken, "pr_public, pr_private, pr_static"); //todo terminar
    }

    public void atributo() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_public", "pr_private").contains(this.currentToken.getTokenId())) {
            this.visibilidad();
            this.tipo();
            this.listaDecAtrs();
            this.match(";");
        } else
            throw new SyntaxException(this.currentToken, "pr_public, pr_private");
    }

    public void metodo() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_static", "pr_void", "idClase", "pr_boolean", "pr_char", "pr_int").contains(this.currentToken.getTokenId())) {
            this.encabezadoMetodo();
            this.bloque();
        }
         else
            throw new SyntaxException(this.currentToken, "pr_public"); //todo terminar
    }

    private void encabezadoMetodo() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_static", "pr_void", "idClase", "pr_boolean", "pr_char", "pr_int").contains(this.currentToken.getTokenId())) {
            this.estaticoOpt();
            this.tipoMetodo();
            this.match("idMV");
            this.argsFormales();
        } else
            throw new SyntaxException(this.currentToken, "pr_static");         //todo acomodar excep
    }

    private void visibilidad() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("pr_public")) {
            this.match("pr_public");
        } else if (this.currentToken.getTokenId().equals("pr_private")) {
            this.match("pr_private");
        } else
            throw new SyntaxException(this.currentToken, "pr_public o pr_private");
    }

    private void tipo() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_boolean", "pr_char", "pr_int").contains(this.currentToken.getTokenId())) {
            this.tipoPrimitivo();
        } else
            if (this.currentToken.getTokenId().equals("idClase"))
                this.match("idClase");
            else
                throw new SyntaxException(this.currentToken, "pr_boolean, pr_char, pr_int o idClase");
    }

    private void tipoPrimitivo() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("pr_boolean"))
            this.match("pr_boolean");
        else if (this.currentToken.getTokenId().equals("pr_char"))
            this.match("pr_char");
        else if (this.currentToken.getTokenId().equals("pr_int"))
            this.match("pr_int");
        else
            throw new SyntaxException(this.currentToken, "pr_boolean, pr_char o pr_int");
    }

    private void listaDecAtrs() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("idMV")) {
            this.match("idMV");
            this.listaDecAtrsPrima();
        } else
            throw new SyntaxException(this.currentToken, "idMV");
    }

    private void listaDecAtrsPrima() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals(",")) {
            this.match(",");
            this.listaDecAtrs();
        } else {
            // epsilon, no hago nada
        }
    }

    private void estaticoOpt() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("pr_static"))
            this.match("pr_static");
        else {
            // epsilon, no hago nada
        }
    }

    private void tipoMetodo() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("idClase", "pr_boolean", "pr_char", "pr_int").contains(this.currentToken.getTokenId()))
            this.tipo();
        else if (this.currentToken.getTokenId().equals("pr_void"))
            this.match("pr_void");
        else
            throw new SyntaxException(this.currentToken, "pr_boolean, pr_char, pr_int o pr_void"); //todo revisar
    }

    private void argsFormales() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals("(")) {
            this.match("(");
            this.listaArgsFormalesOpt();
            this.match(")");
        } else
            throw new SyntaxException(this.currentToken, "(");
    }

    private void listaArgsFormalesOpt() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_boolean", "pr_char", "pr_int", "idClase").contains(this.currentToken.getTokenId()))
            this.listaArgsFormales();
        else {
            // epsilon, no hago nada
        }
    }

    private void listaArgsFormales() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_boolean", "pr_char", "pr_int", "idClase").contains(this.currentToken.getTokenId())) {
            this.argFormal();
            this.listaArgsFormalesPrima();
        } else
            throw new SyntaxException(this.currentToken, "pr_boolean, pr_char o pr_int");
    }

    private void listaArgsFormalesPrima() throws LexicalException, IOException, SyntaxException {
        if (this.currentToken.getTokenId().equals(",")) {
            this.match(",");
            this.listaArgsFormales();
        } else {
            // epsilon, no hago nada
        }
    }

    private void argFormal() throws LexicalException, IOException, SyntaxException {
        if (Arrays.asList("pr_boolean", "pr_char", "pr_int", "idClase").contains(this.currentToken.getTokenId())) {
            this.tipo();
            this.match("idMV");
        } else
            throw new SyntaxException(this.currentToken, "pr_boolean, pr_char, pr_int o idClase");
    }

    private void bloque() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("{")) {
            this.match("{");
            this.listaSentencias();
            this.match("}");
        } else
            throw new SyntaxException(this.currentToken, "{");
    }

    private void sentenciaPrima() throws LexicalException, SyntaxException, IOException {
        if (Arrays.asList("=", "+=", "-=").contains(this.currentToken.getTokenId())) {
            this.tipoAsignacion();
            this.expresion();
        }
        else {
            //epsilon, no hago nada
        }
    }

    private void listaSentencias() throws LexicalException, SyntaxException, IOException {
        if (Arrays.asList(";", "idMV", "pr_this", "pr_new", "idClase", "(", "pr_return", "pr_if", "pr_while", "{", "pr_var").contains(this.currentToken.getTokenId())) {
            this.sentencia();
            this.listaSentencias();
        } else {
            // epsilon, no hago nada
        }
    }

    private void sentencia() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals(";"))
            this.match(";");
        else if (Arrays.asList("idMV", "pr_this", "pr_new", "idClase", "(").contains(this.currentToken.getTokenId())) {
            this.acceso();
            this.sentenciaPrima();
            this.match(";");
        } else if (this.currentToken.getTokenId().equals("pr_var")) {
            this.varLocal();
            this.match(";");
        } else if (this.currentToken.getTokenId().equals("pr_return")) {
            this.noTerminalReturn();
            this.match(";");
        } else if (this.currentToken.getTokenId().equals("pr_if"))
            this.noTerminalIf();
        else if (this.currentToken.getTokenId().equals("pr_while"))
            this.noTerminalWhile();
        else if (this.currentToken.getTokenId().equals("{"))
            this.bloque();
        else                                                        //todo poner todos los posibles aca en la excep
            throw new SyntaxException(this.currentToken, "pr_var, pr_return, pr_if, pr_while o {");
    }

    private void tipoAsignacion() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("="))
            this.match("=");
        else if (this.currentToken.getTokenId().equals("+="))
            this.match("+=");
        else if (this.currentToken.getTokenId().equals("-="))
            this.match("-=");
        else
            throw new SyntaxException(this.currentToken, "=, += o -=");
    }

    private void varLocal() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("pr_var")) {
            this.match("pr_var");
            this.match("idMV");
            this.match("=");
            this.expresion();
        } else
            throw new SyntaxException(this.currentToken, "pr_var");
    }

    private void noTerminalReturn() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("pr_return")) {
            this.match("pr_return");
            this.expresionOpt();
        } else
            throw new SyntaxException(this.currentToken, "pr_return");
    }

    private void expresionOpt() throws SyntaxException, LexicalException, IOException {
        if (Arrays.asList("+", "-", "!", "pr_null", "pr_true", "pr_false", "intLiteral", "charLiteral", "stringLiteral", "idMV", "pr_this", "pr_new", "idClase", "(").contains(this.currentToken.getTokenId()))
            this.expresion();
        else {
            //epsilon, no hago nada
        }
    }

    private void noTerminalIf() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("pr_if")) {
            this.match("pr_if");
            this.match("(");
            this.expresion();
            this.match(")");
            this.sentencia();
            this.noTerminalIfPrima();
        } else
            throw new SyntaxException(this.currentToken, "pr_if");
    }

    private void noTerminalIfPrima() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("pr_else")) {
            this.match("pr_else");
            this.sentencia();
        } else {
            // epsilon, no hago nada
        }
    }

    private void noTerminalWhile() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("pr_while")) {
            this.match("pr_while");
            this.match("(");
            this.expresion();
            this.match(")");
            this.sentencia();
        } else
            throw new SyntaxException(this.currentToken, "pr_while");
    }

    private void expresion() throws SyntaxException, LexicalException, IOException {
        if (Arrays.asList("+", "-", "!", "pr_null", "pr_true", "pr_false", "intLiteral", "charLiteral", "stringLiteral", "idMV", "pr_this", "pr_new", "idClase", "(").contains(this.currentToken.getTokenId())) {
            this.expresionUnaria();
            this.expresionPrima();
        }
        else
            throw new SyntaxException(this.currentToken, "+, -, !, pr_null, pr_true, pr_false, intLiteral, charLiteral o stringLiteral");
    }

    private void expresionPrima() throws LexicalException, SyntaxException, IOException {
        if (Arrays.asList("||", "&&", "==", "!=", "<", ">", "<=", ">=", "+", "-", "*", "/", "%").contains(this.currentToken.getTokenId())) {
            this.operadorBinario();
            this.expresionUnaria();
            this.expresionPrima();
        } else {
            // epsilon, no hago nada
        }
    }

    private void operadorBinario() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("||"))
            this.match("||");
        else if (this.currentToken.getTokenId().equals("&&"))
            this.match("&&");
        else if (this.currentToken.getTokenId().equals("=="))
            this.match("==");
        else if (this.currentToken.getTokenId().equals("!="))
            this.match("!=");
        else if (this.currentToken.getTokenId().equals("<"))
            this.match("<");
        else if (this.currentToken.getTokenId().equals(">"))
            this.match(">");
        else if (this.currentToken.getTokenId().equals("<="))
            this.match("<=");
        else if (this.currentToken.getTokenId().equals(">="))
            this.match(">=");
        else if (this.currentToken.getTokenId().equals("+"))
            this.match("+");
        else if (this.currentToken.getTokenId().equals("-"))
            this.match("-");
        else if (this.currentToken.getTokenId().equals("*"))
            this.match("*");
        else if (this.currentToken.getTokenId().equals("/"))
            this.match("/");
        else if (this.currentToken.getTokenId().equals("%"))
            this.match("%");
        else
            throw new SyntaxException(this.currentToken, "+, - o !"); //todo poner todo
    }

    private void expresionUnaria() throws SyntaxException, LexicalException, IOException {
        if (Arrays.asList("+", "-", "!").contains(this.currentToken.getTokenId())) {
            this.operadorUnario();
            this.operando();
        } else if (Arrays.asList("pr_null", "pr_true", "pr_false", "intLiteral", "charLiteral", "stringLiteral", "pr_this", "idMV", "pr_new", "idClase", "(").contains(this.currentToken.getTokenId()))
            this.operando();
        else
            throw new SyntaxException(this.currentToken, "+, - o !"); //todo poner todo
    }

    private void operadorUnario() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("+"))
            this.match("+");
        else if (this.currentToken.getTokenId().equals("-"))
            this.match("-");
        else if (this.currentToken.getTokenId().equals("!"))
            this.match("!");
        else
            throw new SyntaxException(this.currentToken, "+, - o !");
    }

    private void operando() throws SyntaxException, LexicalException, IOException {
        if (Arrays.asList("pr_null", "pr_true", "pr_false", "intLiteral", "charLiteral", "stringLiteral").contains(this.currentToken.getTokenId()))
            this.literal();
        else if (Arrays.asList("idMV", "pr_this", "pr_new", "idClase", "(").contains(this.currentToken.getTokenId()))
            this.acceso();
        else
            throw new SyntaxException(this.currentToken, "+, - o !"); //todo poner todo
    }

    private void literal() throws SyntaxException, LexicalException, IOException {
        if (this.currentToken.getTokenId().equals("pr_null"))
            this.match("pr_null");
        else if (this.currentToken.getTokenId().equals("pr_true"))
            this.match("pr_true");
        else if (this.currentToken.getTokenId().equals("pr_false"))
            this.match("pr_false");
        else if (this.currentToken.getTokenId().equals("intLiteral"))
            this.match("intLiteral");
        else if (this.currentToken.getTokenId().equals("pr_char"))
            this.match("pr_char");
        else if (this.currentToken.getTokenId().equals("stringLiteral"))
            this.match("stringLiteral");
        else
            throw new SyntaxException(this.currentToken, "+, - o !"); //todo poner todo
    }

    private void acceso() throws SyntaxException, LexicalException, IOException {
        if (Arrays.asList("pr_this", "idMV", "pr_new", "idClase", "(").contains(this.currentToken.getTokenId())) {
            this.primario();
            this.encadenadoOpt();
        } else
            throw new SyntaxException(this.currentToken, "this, idMV, pr_new, idClase o (");
    }

    private void primario() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("idMV")) {
            this.match("idMV");
            this.primarioPrima();
        } else if (this.currentToken.getTokenId().equals("pr_this"))
            this.accesoThis();
        else if (this.currentToken.getTokenId().equals("pr_new"))
            this.accesoConstructor();
        else if (this.currentToken.getTokenId().equals("idClase"))
            this.accesoMetodoEstatico();
        else if (this.currentToken.getTokenId().equals("("))
            this.expresionParentizada();
        else
            throw new SyntaxException(this.currentToken, "pr_this, idMV, pr_new, idClase o (");
    }

    private void primarioPrima() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("("))
            this.argsActuales();
        else {
            // epsilon, no hago nada
        }
    }

    private void accesoThis() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("pr_this"))
            this.match("pr_this");
        else
            throw new SyntaxException(this.currentToken, "pr_this");
    }

    //todo verificar
//    private void accesoVar() {
//        if (this.currentToken.getTokenId().equals("pr_this"))
//            this.match("pr_this");
//        else
//            throw new SyntaxException(this.currentToken, "pr_this");
//    }

    private void accesoConstructor() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("pr_new")) {
            this.match("pr_new");
            this.match("idClase");
            this.match("(");
            this.match(")");
        }
        else
            throw new SyntaxException(this.currentToken, "pr_new");
    }

    private void expresionParentizada() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("(")) {
            this.match("(");
            this.expresion();
            this.match(")");
        } else
            throw new SyntaxException(this.currentToken, "(");
    }

    //todo verificar
//    private void accesoMetodo() {
//
//    }

    private void accesoMetodoEstatico() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("idClase")) {
            this.match("idClase");
            this.match(".");
            this.match("idMV");
            this.argsActuales();
        } else
            throw new SyntaxException(this.currentToken, "idClase");
    }

    private void argsActuales() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("(")) {
            this.match("(");
            this.listaExpsOpt();
            this.match(")");
        } else
            throw new SyntaxException(this.currentToken, "(");
    }

    private void listaExpsOpt() throws LexicalException, SyntaxException, IOException {
        if (Arrays.asList("+", "-", "!", "pr_null", "pr_true", "pr_false", "pr_int", "pr_char", "stringLiteral", "idMV", "pr_this", "pr_new", "idClase", "(").contains(this.currentToken.getTokenId()))
            this.listaExps();
        else {
            // epsilon, no hago nada
        }
    }

    private void listaExps() throws LexicalException, SyntaxException, IOException {
        if (Arrays.asList("+", "-", "!", "pr_null", "pr_true", "pr_false", "pr_int", "pr_char", "stringLiteral", "idMV", "pr_this", "pr_new", "idClase", "(").contains(this.currentToken.getTokenId())) {
            this.expresion();
            this.listaExpsPrima();
        }
        else
            throw new SyntaxException(this.currentToken, "+"); //todo terminar
    }

    private void listaExpsPrima() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals(",")) {
            this.match(",");
            this.listaExps();
        } else
            throw new SyntaxException(this.currentToken, ",");
    }

    private void encadenadoOpt() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals(".")) {
            this.match(".");
            this.match("idMV");
            this.encadenadoOptPrima();
        }
        else {
            // epsilon, no hago nada
        }
    }

    private void encadenadoOptPrima() throws LexicalException, SyntaxException, IOException {
        if (this.currentToken.getTokenId().equals("."))
            this.encadenadoOpt();
        else
            if (this.currentToken.getTokenId().equals("(")) {
                this.argsActuales();
                this.encadenadoOpt();
            }
            else {
                // epsilon,  no hago nada
            }
    }

}