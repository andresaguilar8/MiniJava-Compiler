package AST.Access;

import AST.Expression.ExpressionNode;
import LexicalAnalyzer.Token;
import SemanticAnalyzer.SemanticExceptionSimple;
import SemanticAnalyzer.Type;

public class ParenthesizedExpressionNode extends AccessNode {

    protected ExpressionNode expression;

    public ParenthesizedExpressionNode(Token token, ExpressionNode expression) {
        super(token);
        this.expression = expression;
    }

    @Override
    public Type check() throws SemanticExceptionSimple {
        Type expressionType = this.expression.check();
        return expressionType;
    }

    @Override
    public void printExpression() {
        this.expression.printExpression();
    }

    @Override
    public void setType() {

    }

    @Override
    public void setEncadenado(Encadenado encadenado) {
        this.encadenado = encadenado;
    }
}
