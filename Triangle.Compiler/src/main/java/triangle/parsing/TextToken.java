package triangle.parsing;

final class TextToken extends Token {

    private final String text;

    TextToken(final Kind kind, final int line, final int column, String text) {
        super(kind, line, column);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override public String toString() {
        return text;
    }

}
