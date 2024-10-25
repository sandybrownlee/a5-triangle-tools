package triangle.syntacticAnalyzer;

class Token {

    private final int line;
    private final int column;
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

    @Deprecated SourcePosition getPosition() {
        return new SourcePosition(line, column);
    }

    public enum Kind {
        // literals, identifiers, operators...
        INTLITERAL, CHARLITERAL, IDENTIFIER, OPERATOR,

        // reserved words - keep in alphabetical order for ease of maintenance...
        ARRAY, BEGIN, CONST, DO, ELSE, END, FUNC, IF, IN, LET, OF, PROC, RECORD, THEN, TYPE, VAR, WHILE,

        // punctuation...
        DOT, COLON, SEMICOLON, COMMA, BECOMES, IS,

        // brackets...
        LPAREN, RPAREN, LBRACK, RBRACK, LBRACE, RBRACE,

        // special tokens...
        EOT, ERROR
    }
}
