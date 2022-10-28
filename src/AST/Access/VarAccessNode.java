package AST.Access;

import LexicalAnalyzer.Token;
import SemanticAnalyzer.*;

public class VarAccessNode extends AccessNode {

    public VarAccessNode(Token token) {
        super(token);
        this.isAssignable = true;
    }

    @Override
    public boolean isAssignable() {
        return true;
    }

    @Override
    public boolean isCallable() {
        return false;
    }

    @Override
    public Type check() throws SemanticExceptionSimple {
        Type varType;
        String varName = this.token.getLexeme();
        Method currentMethod = SymbolTable.getInstance().getCurrentMethod();
        if (SymbolTable.getInstance().isMethodParameter(varName, currentMethod))
            varType = SymbolTable.getInstance().retrieveParameterType(varName, currentMethod);
        else
            if (SymbolTable.getInstance().isCurrentBlockLocalVar(varName))
                varType = SymbolTable.getInstance().retrieveLocalVarType(varName);
            else {
                ConcreteClass methodClass = currentMethod.getMethodClass();
                    if (SymbolTable.getInstance().isAttribute(varName, methodClass)) {
                        Attribute attribute = methodClass.getAttributes().get(this.token.getLexeme());
                        if (attribute.isInherited())
                            if (!SymbolTable.getInstance().isPublicAttribute(this.token.getLexeme(), methodClass))
                                throw new SemanticExceptionSimple(this.token, this.token.getLexeme() + " tiene visibilidad privada y es un atributo heredado");
                        if (!SymbolTable.getInstance().getCurrentMethod().getStaticHeader().equals("static"))
                            varType = SymbolTable.getInstance().retrieveAttribute(varName, methodClass);
                        else
                            throw new SemanticExceptionSimple(this.token, "un metodo estatico no puede acceder a un atributo");
                    }
                    else
                        if (!SymbolTable.getInstance().getCurrentMethod().getStaticHeader().equals("static"))
                            throw new SemanticExceptionSimple(this.token, this.token.getLexeme() + " no es una variable local ni un parametro del metodo " + "\"" + currentMethod.getMethodName() + "\"" + " ni un atributo de la clase " + methodClass.getClassName());
                        else
                            throw new SemanticExceptionSimple(this.token, this.token.getLexeme() + " no es una variable local ni un parametro del metodo " + "\"" + currentMethod.getMethodName() + "\"" );
            }
        if (this.encadenado != null) {
            if (!varType.isPrimitive())
                return this.encadenado.check(varType);
            else
                throw new SemanticExceptionSimple(this.token, "el lado izquierdo del encadenado es un tipo primitivo");
        }
        return varType;
    }

}
