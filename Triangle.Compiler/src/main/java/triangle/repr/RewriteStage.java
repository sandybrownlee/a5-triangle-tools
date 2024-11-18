package triangle.repr;

// A RewriteStage represents a stage of rewriting the AST -- used for constant folding, hoisting, etc.
// Interface RewriteStage provides default methods that creates new instances of te provided AST node while recursively calling
// rewrite on its subtrees. This allows implementers to only write rewriting definitions for the nodes that they are concerned
// about
// Implementers of RewriteStage must ensure that they maintain any annotations (that they intend to maintain), the interface
// default methods maintain all annotations
//@formatter:off
public interface RewriteStage {

    default Statement rewrite(Statement statement) {
        return switch (statement) {
            case Statement.AssignStatement assignStatement ->
                    new Statement.AssignStatement(rewrite(assignStatement.identifier()),
                                                  rewrite(assignStatement.expression()))
                                 .withAnnotationsOf(assignStatement);
            case Statement.ExpressionStatement expressionStatement ->
                    new Statement.ExpressionStatement(rewrite(expressionStatement.expression()))
                                 .withAnnotationsOf(expressionStatement);
            case Statement.IfStatement ifStatement ->
                    new Statement.IfStatement(rewrite(ifStatement.condition()),
                                              ifStatement.consequent().map(this::rewrite),
                                              ifStatement.alternative().map(this::rewrite))
                                 .withAnnotationsOf(ifStatement);
            case Statement.LetStatement letStatement ->
                    new Statement.LetStatement(letStatement.declarations().stream().map(this::rewrite).toList(),
                                               rewrite(letStatement.statement()))
                                 .withAnnotationsOf(letStatement);
            case Statement.LoopWhileStatement loopWhileStatement ->
                    new Statement.LoopWhileStatement(rewrite(loopWhileStatement.condition()),
                                                     rewrite(loopWhileStatement.loopBody()),
                                                     rewrite(loopWhileStatement.doBody()))
                                 .withAnnotationsOf(loopWhileStatement);
            case Statement.RepeatUntilStatement repeatUntilStatement ->
                    new Statement.RepeatUntilStatement(rewrite(repeatUntilStatement.condition()),
                                                       rewrite(repeatUntilStatement.body()))
                                 .withAnnotationsOf(repeatUntilStatement);
            case Statement.RepeatWhileStatement repeatWhileStatement ->
                    new Statement.RepeatWhileStatement(rewrite(repeatWhileStatement.condition()),
                                                       rewrite(repeatWhileStatement.body()))
                                 .withAnnotationsOf(repeatWhileStatement);
            case Statement.StatementBlock statementBlock ->
                    new Statement.StatementBlock(statementBlock.statements().stream().map(this::rewrite).toList())
                                 .withAnnotationsOf(statementBlock);
            case Statement.WhileStatement whileStatement ->
                    new Statement.WhileStatement(rewrite(whileStatement.condition()),
                                                 rewrite(whileStatement.body()))
                                 .withAnnotationsOf(whileStatement);
        };
    }

    default Expression rewrite(Expression expression) {
        return switch (expression) {
            case Expression.BinaryOp binaryOp ->
                    new Expression.BinaryOp(rewrite(binaryOp.operator()),
                                            rewrite(binaryOp.leftOperand()),
                                            rewrite(binaryOp.rightOperand()))
                                  .withAnnotationsOf(binaryOp);
            case Expression.FunCall funCall ->
                    new Expression.FunCall(rewrite(funCall.func()),
                                           funCall.arguments().stream().map(this::rewrite).toList())
                                  .withAnnotationsOf(funCall);
            case Expression.Identifier identifier -> rewrite(identifier);
            case Expression.IfExpression ifExpression ->
                    new Expression.IfExpression(rewrite(ifExpression.condition()),
                                                rewrite(ifExpression.consequent()),
                                                rewrite(ifExpression.alternative()))
                                  .withAnnotationsOf(ifExpression);
            case Expression.LetExpression letExpression ->
                    new Expression.LetExpression(letExpression.declarations().stream().map(this::rewrite).toList(),
                                                 rewrite(letExpression.expression()))
                                  .withAnnotationsOf(letExpression);
            case Expression.LitArray litArray ->
                    new Expression.LitArray(litArray.elements().stream().map(this::rewrite).toList());
            case Expression.LitBool litBool -> litBool;
            case Expression.LitChar litChar -> litChar;
            case Expression.LitInt litInt -> litInt;
            case Expression.LitRecord litRecord ->
                    new Expression.LitRecord(litRecord.fields().stream().map(f ->
                                                new Expression.LitRecord.RecordField(f.name(), rewrite(f.value()))).toList())
                                  .withAnnotationsOf(litRecord);
            case Expression.SequenceExpression sequenceExpression ->
                    new Expression.SequenceExpression(rewrite(sequenceExpression.statement()),
                                                      rewrite(sequenceExpression.expression()))
                                  .withAnnotationsOf(sequenceExpression);
            case Expression.UnaryOp unaryOp ->
                    new Expression.UnaryOp(rewrite(unaryOp.operator()),
                                           rewrite(unaryOp.operand()))
                                  .withAnnotationsOf(unaryOp);
        };
    }

    default Expression.Identifier rewrite(Expression.Identifier identifier) {
        return switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript ->
                    new Expression.Identifier.ArraySubscript(rewrite(arraySubscript.array()),
                                                             rewrite(arraySubscript.subscript()))
                                             .withAnnotationsOf(arraySubscript);
            case Expression.Identifier.BasicIdentifier basicIdentifier -> rewrite(basicIdentifier);
            case Expression.Identifier.RecordAccess recordAccess ->
                    new Expression.Identifier.RecordAccess(rewrite(recordAccess.record()),
                                                           rewrite(recordAccess.field())).withAnnotationsOf(recordAccess);
        };
    }

    default Expression.Identifier.BasicIdentifier rewrite(Expression.Identifier.BasicIdentifier identifier) {
        return identifier;
    }

    default Declaration rewrite(Declaration declaration) {
        return switch (declaration) {
            case Declaration.ConstDeclaration constDeclaration ->
                    new Declaration.ConstDeclaration(constDeclaration.name(),
                                                     rewrite(constDeclaration.value()))
                                   .withAnnotationsOf(constDeclaration);
            case Declaration.FuncDeclaration funcDeclaration ->
                    new Declaration.FuncDeclaration(funcDeclaration.name(),
                                                    funcDeclaration.parameters().stream().map(this::rewrite).toList(),
                                                    funcDeclaration.returnTypeSig(),
                                                    rewrite(funcDeclaration.expression()))
                                   .withAnnotationsOf(funcDeclaration);
            case Declaration.ProcDeclaration procDeclaration ->
                    new Declaration.ProcDeclaration(procDeclaration.name(),
                                                    procDeclaration.parameters().stream().map(this::rewrite).toList(),
                                                    rewrite(procDeclaration.statement()))
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

    default Argument rewrite(Argument argument) {
        return switch (argument) {
            case Argument.FuncArgument funcArgument ->
                    new Argument.FuncArgument(rewrite(funcArgument.func()))
                                .withAnnotationsOf(funcArgument);
            case Argument.VarArgument varArgument ->
                    new Argument.VarArgument(rewrite(varArgument.var()))
                                .withAnnotationsOf(varArgument);
            case Expression expression -> rewrite(expression);
        };
    }

    default Parameter rewrite(Parameter parameter) {
        return switch (parameter) {
            case Parameter.FuncParameter funcParameter ->
                    new Parameter.FuncParameter(funcParameter.name(),
                                                funcParameter.parameters().stream().map(this::rewrite).toList(),
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
