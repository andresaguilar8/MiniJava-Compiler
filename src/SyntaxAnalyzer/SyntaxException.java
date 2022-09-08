package SyntaxAnalyzer;

import LexicalAnalyzer.Token;

public class SyntaxException extends Exception {

    private Token currentToken;
    private String tokenId;

    public SyntaxException(Token currentToken, String tokenId) {
        this.currentToken = currentToken;
        this.tokenId = tokenId;
    }

    public String getMessage() {
        return "Error Sintactico en linea "
                + this.currentToken.getLineNumber()
                + ": se esperaba "
                + this.tokenId
                + " y se encontro "
                + this.currentToken.getTokenId()
                + this.generateStringError();
    }

    private String generateStringError() {
        return "\n\n[Error:" +
                this.currentToken.getTokenId()
                + "|"
                + this.currentToken.getLineNumber()
                + "]\n\n";
    }

}
