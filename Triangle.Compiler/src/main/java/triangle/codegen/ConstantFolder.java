package triangle.codegen;

import triangle.abstractMachine.Machine;
import triangle.repr.Expression;
import triangle.repr.Expression.LitBool;
import triangle.repr.Expression.LitChar;
import triangle.repr.Expression.LitInt;
import triangle.repr.RewriteStage;
import triangle.repr.Statement;

// OCaml equivalents of rewrite rules are provided as //// comments for ease of understanding, with
//// type Ex = ... Bop of string * Ex * Ex | Op of string * Ex ...

// ConstantFolder maintains type annotations, but may discard source position annotations
// ConstantFolder does not fold across function calls
@SuppressWarnings("DanglingJavadoc") class ConstantFolder implements RewriteStage {

    // number of folds above which to try another folding pass since we want to perform multiple passes of constant folding
    // the key observation is that if the number of folds performed in a single pass is quite high, it is more likely that
    // another pass of the constant folder will be worth it, since the resultant program (after the first pass) will have a
    // higher "density" of constants
    // the only way to choose a sensible value for this is to get the (approximate) size of the AST node, this is not currently
    // offered by Parser.
    private static final int FOLD_THRESHOLD = 5;

    // hard limit on total folds per program, to prevent users from borking the compiler with malicious input
    private static final int FOLD_LIMIT = 800;

    // need to ensure that folded constants don't overflow
    private static boolean inRange(int n) {
        return n <= Machine.maxintRep && n >= Machine.maxintRep * -1;
    }

    private int foldCount = 0;

    //// open Char in
    @Override public Expression rewrite(final Expression expression) {
        Expression folded = RewriteStage.super.rewrite(expression);

        return switch (RewriteStage.super.rewrite(expression)) {
            case Expression.BinaryOp binaryOp -> foldBinaryOp(binaryOp);
            case Expression.FunCall funCall -> {
                //// ("chr", LitInt x) -> LitChar (code x)
                if (funCall.func().name().equals("chr") && funCall.arguments().getFirst() instanceof LitInt litInt) {
                    foldCount++;
                    yield new LitChar((char) litInt.value());
                }

                //// ("ord", LitChar c) -> LitInt (chr c)
                if (funCall.func().name().equals("ord") && funCall.arguments().getFirst() instanceof LitChar litChar) {
                    foldCount++;
                    //noinspection RedundantCast
                    yield new LitInt((int) litChar.value());
                }

                yield folded;
            }
            case Expression.UnaryOp unaryOp -> foldUnaryOp(unaryOp);
            default -> folded;
        };
    }

    Statement fold(Statement statement) {
        int totalFolds = 0;

        // repeatedly fold while the number of folds performed in last pass is above threshold, reset foldCount after each pass
        do {
            foldCount = 0;
            statement = RewriteStage.super.rewrite(statement);
            totalFolds += foldCount;
        } while (foldCount >= FOLD_THRESHOLD && totalFolds <= FOLD_LIMIT);

        return statement;
    }

    //// open Bool in
    private Expression foldBinaryOp(final Expression.BinaryOp binaryOp) {
        // optimistically bump fold count, we can undo this later if we didn't actually get to fold
        foldCount++;

        // Java lacks real tuples, so T lets us mimic ML-style tuple deconstruction (T as in tuple)
        record T(String op, Expression left, Expression right) { }

        //@formatter:off
        //// match binaryOp with
        return switch (new T(binaryOp.operator(), binaryOp.leftOperand(), binaryOp.rightOperand())) {
            //// Bop ("+", e, LitInt 0) -> e
            case T(String op, Expression e, LitInt litInt) when op.equals("+") && litInt.value() == 0 -> e;
            //// Bop ("+", LitInt 0, e) -> e
            case T(String op, LitInt litInt, Expression e) when op.equals("+") && litInt.value() == 0 -> e;
            //// Bop ("*", LitInt _, LitInt 0) -> LitInt 0
            case T(String op, LitInt _, LitInt litInt) when op.equals("*") && litInt.value() == 0 -> litInt;
            //// Bop ("*", LitInt 0, LitInt _) -> LitInt 0
            case T(String op, LitInt litInt, LitInt _) when op.equals("*") && litInt.value() == 0 -> litInt;
            //// Bop ("*", e, LitInt 1) -> e
            case T(String op, Expression e, LitInt litInt) when op.equals("*") && litInt.value() == 1 -> e;
            //// Bop ("*", LitInt 1, e) -> e
            case T(String op, LitInt litInt, Expression e) when op.equals("*") && litInt.value() == 1 -> e;
            //// Bop ("-", e, LitInt 0) -> e
            case T(String op, Expression e, LitInt litInt) when op.equals("-") && litInt.value() == 0 -> e;
            //// Bop ("/", e, LitInt 1) -> e
            case T(String op, Expression e, LitInt litInt) when op.equals("/") && litInt.value() == 1 -> e;
            //// Bop ("+", LitInt x, LitInt y) when inRange (x + y) -> LitInt (x + y)
            case T(String op, LitInt x, LitInt y) when op.equals("+") && inRange(x.value() + y.value()) ->
                    new LitInt(x.value() + y.value());
            //// Bop ("*", LitInt x, LitInt y) when inRange (x * y) -> LitInt (x * y)
            case T(String op, LitInt x, LitInt y) when op.equals("*") && inRange(x.value() * y.value()) ->
                    new LitInt(x.value() * y.value());
            //// Bop ("-", LitInt x, LitInt y) when inRange (x - y) -> LitInt (x - y)
            case T(String op, LitInt x, LitInt y) when op.equals("-") && inRange(x.value() - y.value()) ->
                    new LitInt(x.value() - y.value());
            //// Bop ("/", LitInt x, LitInt y) when y <> 0 -> LitInt (x / y)
            case T(String op, LitInt x, LitInt y) when op.equals("/") && y.value() != 0 -> new LitInt(x.value() / y.value());
            //// Bop ("//", LitInt x, LitInt y) when y <> 0 -> LitInt (x mod y)
            case T(String op, LitInt x, LitInt y) when op.equals("//") && y.value() != 0 -> new LitInt(x.value() % y.value());

            //// Bop (">", LitInt x, LitInt y) when x > y -> LitBool true
            case T(String op, LitInt x, LitInt y) when op.equals(">") && x.value() > y.value() -> new LitBool(true);
            //// Bop (">=", LitInt x, LitInt y) when x >= y -> LitBool true
            case T(String op, LitInt x, LitInt y) when op.equals(">=") && x.value() >= y.value() -> new LitBool(true);
            //// Bop ("<", LitInt x, LitInt y) when x < y -> LitBool true
            case T(String op, LitInt x, LitInt y) when op.equals("<") && x.value() < y.value() -> new LitBool(true);
            //// Bop ("<=", LitInt x, LitInt y) when x <= y -> LitBool true
            case T(String op, LitInt x, LitInt y) when op.equals("<=") && x.value() <= y.value() -> new LitBool(true);

            //// Bop ("/\\", (LitBool a), (LitBool b)) -> LitBool (a && b)
            case T(String op, LitBool a, LitBool b) when op.equals("/\\") -> new LitBool(a.value() && b.value());
            //// Bop ("\\/", (LitBool a), (LitBool b)) -> LitBool (a || b)
            case T(String op, LitBool a, LitBool b) when op.equals("\\/") -> new LitBool(a.value() || b.value());

            //// Bop ("=", LitBool a, LitBool b) -> LitBool (a == b)
            case T(String op, LitBool a, LitBool b) when op.equals("=") -> new LitBool(a.value() == b.value());
            //// Bop ("\=", LitBool a, LitBool b) -> LitBool (a != b)
            case T(String op, LitBool a, LitBool b) when op.equals("\\=") -> new LitBool(a.value() != b.value());
            //// Bop ("=", LitInt x, LitInt y) -> LitBool (x == y)
            case T(String op, LitInt x, LitInt y) when op.equals("=") -> new LitBool(x.value() == y.value());
            //// Bop ("\=", LitInt x, LitInt y) -> LitBool (x != y)
            case T(String op, LitInt x, LitInt y) when op.equals("\\=") -> new LitBool(x.value() != y.value());
            //// Bop ("=", LitChar p, LitChar q) -> LitBool (p == q)
            case T(String op, LitChar p, LitChar q) when op.equals("=") -> new LitBool(p.value() == q.value());
            //// Bop ("\=", LitChar p, LitChar q) -> LitBool (p != q)
            case T(String op, LitChar p, LitChar q) when op.equals("\\=") -> new LitBool(p.value() != q.value());

            default -> {
                foldCount--;
                yield binaryOp;
            }
        };
    }

    //// open Bool in
    private Expression foldUnaryOp(final Expression.UnaryOp unaryOp) {
        // see foldBinaryOp()
        foldCount++;
        record T(String op, Expression e) { }

        //// match unaryOp with
        return switch (new T(unaryOp.operator(), unaryOp.operand())) {
            //// Op ("++", LitInt x) when inRange (x + 1) -> LitInt (x + 1)
            case T(String op, LitInt x) when op.equals("++") && inRange(x.value() + 1) -> new LitInt(x.value() + 1);
            //// Op ("--", LitInt x) when inRange (x - 1) -> LitInt (x - 1)
            case T(String op, LitInt x) when op.equals("--") && inRange(x.value() - 1) -> new LitInt(x.value() - 1);
            //// Op ("|", LitInt x) when inRange (x * 100) -> LitInt (x * 100)
            case T(String op, LitInt x) when op.equals("|") && inRange(x.value() * 100) -> new LitInt(x.value() * 100);
            //// Op ("\\", LitBool b) -> LitBool (not b)
            case T(String op, LitBool a) when op.equals("\\") -> new LitBool(!a.value());

            default -> {
                foldCount--;
                yield unaryOp;
            }
        };
    }
    //@formatter:on

}
