package triangle.codegen;

import triangle.repr.Argument;
import triangle.repr.Declaration;
import triangle.repr.Expression;
import triangle.repr.Expression.Identifier.BasicIdentifier;
import triangle.repr.SourcePosition;
import triangle.repr.Statement;
import triangle.util.ASTPrinter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

// hoists (some) loop-invariant expressions
// for each loop-statement,
//      builds a MutationRecord for it which maintains a set of all identifiers which are mutated in that loop
//          the MutationRecord exposes a class InvarianceChecker which decides if a given expression is invariant wrt the mutation
//              record
//      builds a ExpressionHoister, parameterized by the MutationRecord, which hoists expressions -- i.e, replaces them with
//          basic identifier accesses. ExpressionHoister provides a list of declarations that must be performed, in a scope
//          enclosing the hoisted loop, to make the hoisting valid
class Hoister implements RewriteStage {

    //@formatter:off
    // TODO: StdEnv should store purity annotations
    private static final Set<String> PURE_OPS = Set.of(
            "\\/",
            "/\\",
            "<=",
            ">=",
            ">",
            "<",
            "\\",
            "-",
            "+",
            "*",
            "/",
            "//",
            "|"
    );
    //@formatter:on

    // takes a SourcePosition and creates a label prefix that can uniquely identify the hoisted expressions upto a single loop
    // statement
    private static final Function<SourcePosition, String> freshName = sourcePos -> sourcePos.lineNo() + "_" + sourcePos.colNo();

    //@formatter:off
    @Override public Statement rewrite(final Statement statement) {
        return switch (statement) {
            case Statement.WhileStatement ws ->
                    hoistLoop(freshName.apply(ws.sourcePosition()), ws.condition(), ws.body()).hoist(ws);
            case Statement.RepeatUntilStatement rus ->
                    hoistLoop(freshName.apply(rus.sourcePosition()), rus.condition(), rus.body()).hoist(rus);
            case Statement.RepeatWhileStatement rws ->
                hoistLoop(freshName.apply(rws.sourcePosition()), rws.condition(), rws.body()).hoist(rws);
            case Statement.LoopWhileStatement lws ->
                hoistLoop(freshName.apply(lws.sourcePosition()), lws.condition(), lws.loopBody(), lws.doBody()).hoist(lws);
            default -> RewriteStage.super.rewrite(statement);
        };
    }
    //@formatter:on

    Statement hoist(Statement program) {
        return rewrite(program);
    }

    private ExpressionHoister hoistLoop(String freshName, Expression condition, Statement... bodies) {
        MutationRecord mutationRecord = new MutationRecord();
        mutationRecord.addMutationsOf(condition);
        for (Statement body : bodies) {
            mutationRecord.addMutationsOf(body);
        }

        return new ExpressionHoister(mutationRecord, freshName + "_hoisted");
    }

    // given an (arbitrarily complex) AST node, builds a record of all the identifiers that are mutated (assigned to, or passed
    // as a var argument) in that node
    private class MutationRecord implements RewriteStage {

        private final Set<String> mutated = new HashSet<>();

        @Override public Statement rewrite(final Statement statement) {
            if (statement instanceof Statement.AssignStatement assignStatement) {
                // have to assume, for complex identifiers, that if any part of the identifier is mutated, then the whole thing is
                // TODO: there may be a way to only hoist the parts of complex expressions that are actually modified
                mutated.add(assignStatement.identifier().root().name());
            }

            return RewriteStage.super.rewrite(statement);
        }

        @Override public Expression rewrite(final Expression expression) {
            // assume any argument passed as var is mutated
            if (expression instanceof Expression.FunCall funCall) {
                for (Argument argument : funCall.arguments()) {
                    if (argument instanceof Argument.VarArgument varArgument) {
                        mutated.add(varArgument.var().root().name());
                    }
                }
            }

            return RewriteStage.super.rewrite(expression);
        }

        private void addMutationsOf(Statement statement) {
            rewrite(statement);
        }

        private void addMutationsOf(Expression expression) {
            rewrite(expression);
        }

        private InvarianceChecker usageChecker() {
            return new InvarianceChecker();
        }

        // given an (arbitrarily complex) AST node, calculates and stores whether or not the node is loop-invariant wrt the parent
        // MutationRecord is stateful (for performance reasons) -- i.e can be reset and run again on another expression
        private class InvarianceChecker implements RewriteStage {

            private boolean loopInvariant = true;

            @Override public BasicIdentifier rewrite(final BasicIdentifier identifier) {
                if (mutated.contains(identifier.name())) {
                    loopInvariant = false;
                }

                return RewriteStage.super.rewrite(identifier);
            }

            private void check(Expression expression) {
                rewrite(expression);
            }

            private void reset() {
                loopInvariant = true;
            }

        }

    }

    // given a InvarianceChecker and a (arbitrarily complex) AST node, replaces all hoistable sub-nodes with references to basic
    // identifiers and builds a record of all Declarations which need to be produced in an enclosing scope to make the hoisted
    // sub-nodes valid
    private class ExpressionHoister implements RewriteStage {

        private final MutationRecord.InvarianceChecker invarianceChecker;
        private final String                           labelPrefix;

        private final List<Declaration> hoistedExpressions = new ArrayList<>();
        private final Supplier<String>  generateIdentifier = new Supplier<>() {
            int i = 0;

            @Override public String get() {
                i++;
                return labelPrefix + "_" + i;
            }
        };

        private ExpressionHoister(MutationRecord mutationRecord, String labelPrefix) {
            this.invarianceChecker = mutationRecord.usageChecker();
            this.labelPrefix = labelPrefix;
        }

        @Override public Expression rewrite(final Expression expression) {
            Expression hoisted = RewriteStage.super.rewrite(expression);

            return switch (hoisted) {
                case Expression.BinaryOp binaryOp -> {
                    // we want early returns, to minimize unused computations
                    if (!PURE_OPS.contains(binaryOp.operator().name())) {
                        yield binaryOp;
                    }

                    invarianceChecker.reset();
                    invarianceChecker.check(binaryOp.leftOperand());
                    if (!invarianceChecker.loopInvariant) {
                        yield binaryOp;
                    }

                    invarianceChecker.reset();
                    invarianceChecker.check(binaryOp.rightOperand());
                    if (!invarianceChecker.loopInvariant) {
                        yield binaryOp;
                    }

                    String newName = generateIdentifier.get();
                    hoistedExpressions.add(new Declaration.ConstDeclaration(newName, binaryOp));
                    yield new BasicIdentifier(newName).withType(binaryOp.getType());
                }
                case Expression.UnaryOp unaryOp -> {
                    if (!PURE_OPS.contains(unaryOp.operator().name())) {
                        yield unaryOp;
                    }

                    invarianceChecker.reset();
                    invarianceChecker.check(unaryOp.operand());
                    if (!invarianceChecker.loopInvariant) {
                        yield unaryOp;
                    }

                    String newName = generateIdentifier.get();
                    hoistedExpressions.add(new Declaration.ConstDeclaration(newName, unaryOp));
                    yield new BasicIdentifier(newName).withType(unaryOp.getType());
                }
                case Expression.IfExpression ifExpression -> {
                    invarianceChecker.reset();
                    invarianceChecker.check(ifExpression.condition());
                    if (!invarianceChecker.loopInvariant) {
                        yield ifExpression;
                    }

                    invarianceChecker.reset();
                    invarianceChecker.check(ifExpression.consequent());
                    if (!invarianceChecker.loopInvariant) {
                        yield ifExpression;
                    }

                    invarianceChecker.reset();
                    invarianceChecker.check(ifExpression.alternative());
                    if (!invarianceChecker.loopInvariant) {
                        yield ifExpression;
                    }

                    String newName = generateIdentifier.get();
                    hoistedExpressions.add(new Declaration.ConstDeclaration(newName, ifExpression));
                    yield new BasicIdentifier(newName).withType(ifExpression.getType());
                }
                default -> hoisted;
            };
        }

        private Statement hoist(Statement statement) {
            Statement hoisted = RewriteStage.super.rewrite(statement);

            return new Statement.LetStatement(hoistedExpressions, hoisted);
        }

    }

}
