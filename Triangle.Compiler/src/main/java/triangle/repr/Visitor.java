package triangle.repr;

// A Visitor represents a stage of rewriting the AST -- used for constant folding, hoisting, etc.
// Interface Visitor provides default methods that creates new instances of te provided AST node while recursively calling
// rewrite on its subtrees. This allows implementers to only write rewriting definitions for the nodes that they are concerned
// about
// Implementers of Visitor must ensure that they maintain any annotations (that they intend to maintain), the interface
// default methods maintain all annotations
//@formatter:off
public interface Visitor {

    default Statement visit(Statement statement) {
        return switch (statement) {
            case Statement.AssignStatement assignStatement ->
                    new Statement.AssignStatement(visit(assignStatement.identifier()),
                                                  visit(assignStatement.expression()))
                                 .withAnnotationsOf(assignStatement);
            case Statement.ExpressionStatement expressionStatement ->
                    new Statement.ExpressionStatement(visit(expressionStatement.expression()))
                                 .withAnnotationsOf(expressionStatement);
            case Statement.IfStatement ifStatement ->
                    new Statement.IfStatement(visit(ifStatement.condition()),
                                              ifStatement.consequent().map(this::visit),
                                              ifStatement.alternative().map(this::visit))
                                 .withAnnotationsOf(ifStatement);
            case Statement.LetStatement letStatement ->
                    new Statement.LetStatement(letStatement.declarations().stream().map(this::visit).toList(),
                                               visit(letStatement.statement()))
                                 .withAnnotationsOf(letStatement);
            case Statement.LoopWhileStatement loopWhileStatement ->
                    new Statement.LoopWhileStatement(visit(loopWhileStatement.condition()),
                                                     visit(loopWhileStatement.loopBody()),
                                                     visit(loopWhileStatement.doBody()))
                                 .withAnnotationsOf(loopWhileStatement);
            case Statement.RepeatUntilStatement repeatUntilStatement ->
                    new Statement.RepeatUntilStatement(visit(repeatUntilStatement.condition()),
                                                       visit(repeatUntilStatement.body()))
                                 .withAnnotationsOf(repeatUntilStatement);
            case Statement.RepeatWhileStatement repeatWhileStatement ->
                    new Statement.RepeatWhileStatement(visit(repeatWhileStatement.condition()),
                                                       visit(repeatWhileStatement.body()))
                                 .withAnnotationsOf(repeatWhileStatement);
            case Statement.StatementBlock statementBlock ->
                    new Statement.StatementBlock(statementBlock.statements().stream().map(this::visit).toList())
                                 .withAnnotationsOf(statementBlock);
            case Statement.WhileStatement whileStatement ->
                    new Statement.WhileStatement(visit(whileStatement.condition()),
                                                 visit(whileStatement.body()))
                                 .withAnnotationsOf(whileStatement);
        case Statement.NoopStatement noopStatement -> noopStatement;
        };
    }

    default Expression visit(Expression expression) {
        return switch (expression) {
            case Expression.BinaryOp binaryOp ->
                    new Expression.BinaryOp(binaryOp.operator(),
                                            visit(binaryOp.leftOperand()),
                                            visit(binaryOp.rightOperand()))
                                  .withAnnotationsOf(binaryOp);
            case Expression.FunCall funCall ->
                    new Expression.FunCall(visit(funCall.func()),
                                           funCall.arguments().stream().map(this::visit).toList())
                                  .withAnnotationsOf(funCall);
            case Expression.Identifier identifier -> visit(identifier);
            case Expression.IfExpression ifExpression ->
                    new Expression.IfExpression(visit(ifExpression.condition()),
                                                visit(ifExpression.consequent()),
                                                visit(ifExpression.alternative()))
                                  .withAnnotationsOf(ifExpression);
            case Expression.LetExpression letExpression ->
                    new Expression.LetExpression(letExpression.declarations().stream().map(this::visit).toList(),
                                                 visit(letExpression.expression()))
                                  .withAnnotationsOf(letExpression);
            case Expression.LitArray litArray ->
                    new Expression.LitArray(litArray.elements().stream().map(this::visit).toList()).withAnnotationsOf(litArray);
            case Expression.LitBool litBool -> litBool;
            case Expression.LitChar litChar -> litChar;
            case Expression.LitInt litInt -> litInt;
            case Expression.LitRecord litRecord ->
                    new Expression.LitRecord(litRecord.fields().stream().map(f ->
                                                new Expression.LitRecord.RecordField(f.name(), visit(f.value()))).toList())
                                  .withAnnotationsOf(litRecord);
            case Expression.SequenceExpression sequenceExpression ->
                    new Expression.SequenceExpression(visit(sequenceExpression.statement()),
                                                      visit(sequenceExpression.expression()))
                                  .withAnnotationsOf(sequenceExpression);
            case Expression.UnaryOp unaryOp ->
                    new Expression.UnaryOp(unaryOp.operator(),
                                           visit(unaryOp.operand()))
                                  .withAnnotationsOf(unaryOp);
        };
    }

    default Expression.Identifier visit(Expression.Identifier identifier) {
        return switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript ->
                    new Expression.Identifier.ArraySubscript(visit(arraySubscript.array()),
                                                             visit(arraySubscript.subscript()))
                                             .withAnnotationsOf(arraySubscript);
            case Expression.Identifier.BasicIdentifier basicIdentifier -> visit(basicIdentifier);
            case Expression.Identifier.RecordAccess recordAccess ->
                    new Expression.Identifier.RecordAccess(visit(recordAccess.record()),
                                                           visit(recordAccess.field())).withAnnotationsOf(recordAccess);
        };
    }

    default Expression.Identifier.BasicIdentifier visit(Expression.Identifier.BasicIdentifier identifier) {
        return identifier;
    }

    default Declaration visit(Declaration declaration) {
        return switch (declaration) {
            case Declaration.ConstDeclaration constDeclaration ->
                    new Declaration.ConstDeclaration(constDeclaration.name(),
                                                     visit(constDeclaration.value()))
                                   .withAnnotationsOf(constDeclaration);
            case Declaration.FuncDeclaration funcDeclaration ->
                    new Declaration.FuncDeclaration(funcDeclaration.name(),
                                                    funcDeclaration.parameters().stream().map(this::visit).toList(),
                                                    funcDeclaration.returnTypeSig(),
                                                    visit(funcDeclaration.expression()))
                                   .withAnnotationsOf(funcDeclaration);
            case Declaration.ProcDeclaration procDeclaration ->
                    new Declaration.ProcDeclaration(procDeclaration.name(),
                                                    procDeclaration.parameters().stream().map(this::visit).toList(),
                                                    visit(procDeclaration.statement()))
                                   .withAnnotationsOf(procDeclaration);
            case Declaration.TypeDeclaration typeDeclaration ->
                    new Declaration.TypeDeclaration(typeDeclaration.name(),
                                                    typeDeclaration.typeSig())
                                   .withAnnotationsOf(typeDeclaration);
            case Declaration.VarDeclaration varDeclaration ->
                    new Declaration.VarDeclaration(varDeclaration.name(),
                                                   varDeclaration.declaredType()).withAnnotationsOf(varDeclaration);
        };
    }

    default Argument visit(Argument argument) {
        return switch (argument) {
            case Argument.FuncArgument funcArgument ->
                    new Argument.FuncArgument(visit(funcArgument.func()))
                                .withAnnotationsOf(funcArgument);
            case Argument.VarArgument varArgument ->
                    new Argument.VarArgument(visit(varArgument.var()))
                                .withAnnotationsOf(varArgument);
            case Expression expression -> visit(expression);
        };
    }

    default Parameter visit(Parameter parameter) {
        return switch (parameter) {
            case Parameter.FuncParameter funcParameter ->
                    new Parameter.FuncParameter(funcParameter.name(),
                                                funcParameter.parameters().stream().map(this::visit).toList(),
                                                funcParameter.declaredReturnType())
                                 .withAnnotationsOf(funcParameter);
            case Parameter.ValueParameter valueParameter ->
                    new Parameter.ValueParameter(valueParameter.name(),
                                                 valueParameter.declaredType())
                                 .withAnnotationsOf(valueParameter);
            case Parameter.VarParameter varParameter ->
                    new Parameter.VarParameter(varParameter.name(),
                                               varParameter.declaredType())
                                 .withAnnotationsOf(varParameter);
        };
    }
}
//@formatter:on
