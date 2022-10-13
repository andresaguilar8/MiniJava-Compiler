package AST.Sentence;

import AST.Expression.ExpressionNode;
import LexicalAnalyzer.Token;
import SemanticAnalyzer.Method;
import SemanticAnalyzer.SemanticExceptionSimple;
import SemanticAnalyzer.SymbolTable;
import SemanticAnalyzer.Type;

public class ReturnNode extends SentenceNode {

    private ExpressionNode expressionNode;

    public ReturnNode(Token token, ExpressionNode expressionNode) {
        super(token);
        this.expressionNode = expressionNode;
    }

    @Override
    public void printSentence() {
    if (this.expressionNode != null) {
        System.out.print(this.token.getLexeme() + " con expresion: ");
        this.expressionNode.printExpression();
        System.out.println();
    }
    else
        System.out.println(this.token.getLexeme() + " sin expresion");
    }

    @Override
    public void check() throws SemanticExceptionSimple {
        Type expressionType = this.expressionNode.check();
        Method method = SymbolTable.getInstance().getCurrentMethod();
        Type returnMethodType = method.getReturnType();
        if (!expressionType.getClassName().equals("void") && returnMethodType.getClassName().equals("void"))
            throw new SemanticExceptionSimple(this.token, "El metodo " + method.getMethodName() + " no tiene un tipo de retorno");
        if (!returnMethodType.isCompatibleWithType(expressionType))
            throw new SemanticExceptionSimple(this.token, "El metodo debe retornar una expresion de tipo " + returnMethodType.getClassName());
        //todo preguntar si el token de error esta en el retun o en la expreson
    }
}
