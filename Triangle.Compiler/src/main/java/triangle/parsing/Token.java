package triangle.parsing;

class Token {

    private final int  line;
    private final int  column;
    private final Kind kind;

    Token(final Kind kind, final int line, final int column) {
        this.kind = kind;
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public Kind getKind() {
        return kind;
    }

    @Override public String toString() {
        return kind.toString();
    }

    public enum Kind {
        // literals, identifiers, operators...
        INTLITERAL, CHARLITERAL, IDENTIFIER, OPERATOR, TRUE, FALSE,

        // reserved words - keep in alphabetical order for ease of maintenance...
        AFTER, ARRAY, BEGIN, CONST, DO, ELSE, END, FUNC, IF, IN, LET, LOOP, OF, PROC, RECORD, REPEAT,
        RETURN, THEN, TYPE, UNTIL, VAR, WHILE,

        // punctuation...
        DOT, COLON, SEMICOLON, COMMA, BECOMES, IS,

        // brackets...
        LPAREN, RPAREN, LBRACK, RBRACK, LBRACE, RBRACE,

        // special tokens...
        EOT, ERROR
    }

}
