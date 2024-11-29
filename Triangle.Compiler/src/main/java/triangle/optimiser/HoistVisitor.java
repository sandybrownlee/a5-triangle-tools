package triangle.optimiser;

import java.util.HashMap;
import java.util.LinkedHashMap;

import triangle.StdEnvironment;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.actuals.ConstActualParameter;
import triangle.abstractSyntaxTrees.actuals.EmptyActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.FuncActualParameter;
import triangle.abstractSyntaxTrees.actuals.MultipleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.ProcActualParameter;
import triangle.abstractSyntaxTrees.actuals.SingleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.VarActualParameter;
import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.CallCommand;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.LoopWhileCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.declarations.BinaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.FuncDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcDeclaration;
import triangle.abstractSyntaxTrees.declarations.SequentialDeclaration;
import triangle.abstractSyntaxTrees.declarations.UnaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.VarDeclaration;
import triangle.abstractSyntaxTrees.expressions.ArrayExpression;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.CallExpression;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.EmptyExpression;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.RecordExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.EmptyFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.MultipleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.SingleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.types.AnyTypeDenoter;
import triangle.abstractSyntaxTrees.types.ArrayTypeDenoter;
import triangle.abstractSyntaxTrees.types.BoolTypeDenoter;
import triangle.abstractSyntaxTrees.types.CharTypeDenoter;
import triangle.abstractSyntaxTrees.types.ErrorTypeDenoter;
import triangle.abstractSyntaxTrees.types.IntTypeDenoter;
import triangle.abstractSyntaxTrees.types.MultipleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.RecordTypeDenoter;
import triangle.abstractSyntaxTrees.types.SimpleTypeDenoter;
import triangle.abstractSyntaxTrees.types.SingleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.visitors.ActualParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.ActualParameterVisitor;
import triangle.abstractSyntaxTrees.visitors.ArrayAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.visitors.DeclarationVisitor;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.abstractSyntaxTrees.visitors.FormalParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.IdentifierVisitor;
import triangle.abstractSyntaxTrees.visitors.LiteralVisitor;
import triangle.abstractSyntaxTrees.visitors.OperatorVisitor;
import triangle.abstractSyntaxTrees.visitors.ProgramVisitor;
import triangle.abstractSyntaxTrees.visitors.RecordAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.TypeDenoterVisitor;
import triangle.abstractSyntaxTrees.visitors.VnameVisitor;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;

@SuppressWarnings("StringBufferMayBeStringBuilder")

public class HoistVisitor implements ActualParameterVisitor<Void, AbstractSyntaxTree>,
        ActualParameterSequenceVisitor<Void, AbstractSyntaxTree>, ArrayAggregateVisitor<Void, AbstractSyntaxTree>,
        CommandVisitor<Void, AbstractSyntaxTree>, DeclarationVisitor<Void, AbstractSyntaxTree>,
        ExpressionVisitor<Void, AbstractSyntaxTree>, FormalParameterSequenceVisitor<Void, AbstractSyntaxTree>,
        IdentifierVisitor<Void, AbstractSyntaxTree>, LiteralVisitor<Void, AbstractSyntaxTree>,
        OperatorVisitor<Void, AbstractSyntaxTree>, ProgramVisitor<Void, AbstractSyntaxTree>,
        RecordAggregateVisitor<Void, AbstractSyntaxTree>, TypeDenoterVisitor<Void, AbstractSyntaxTree>,
        VnameVisitor<Void, AbstractSyntaxTree> {

    // flag to whether while code block has been reached
    boolean isInWhile = false;

    // hashmap to store all assignments occurring and a boolean to determine whether
    // the assignment should be hoisted
    LinkedHashMap<AssignCommand, Boolean> hoistables = new LinkedHashMap<>();

    @Override
    public AbstractSyntaxTree visitConstFormalParameter(ConstFormalParameter ast, Void arg) {
        ast.I.visit(this);
        ast.T.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {
        ast.I.visit(this);
        ast.T.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitProcFormalParameter(ProcFormalParameter ast, Void arg) {
        ast.I.visit(this);
        ast.FPS.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitVarFormalParameter(VarFormalParameter ast, Void arg) {
        ast.I.visit(this);
        ast.T.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void arg) {
        ast.FT.visit(this);
        ast.I.visit(this);
        ast.T.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void arg) {
        ast.I.visit(this);
        ast.T.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitDotVname(DotVname ast, Void arg) {
        ast.I.visit(this);
        ast.V.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSimpleVname(SimpleVname ast, Void arg) {
        ast.I.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSubscriptVname(SubscriptVname ast, Void arg) {
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        ast.V.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitAnyTypeDenoter(AnyTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitArrayTypeDenoter(ArrayTypeDenoter ast, Void arg) {
        ast.IL.visit(this);
        ast.T.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitBoolTypeDenoter(BoolTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharTypeDenoter(CharTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitErrorTypeDenoter(ErrorTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void arg) {
        ast.I.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntTypeDenoter(IntTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitRecordTypeDenoter(RecordTypeDenoter ast, Void arg) {
        ast.FT.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void arg) {
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        ast.I.visit(this);
        ast.RA.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleRecordAggregate(SingleRecordAggregate ast, Void arg) {
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        ast.I.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitProgram(Program ast, Void arg) {
        ast.C.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitOperator(Operator ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharacterLiteral(CharacterLiteral ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntegerLiteral(IntegerLiteral ast, Void arg) {
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitIdentifier(Identifier ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void arg) {
        ast.FP.visit(this);
        ast.FPS.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void arg) {
        ast.FP.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitArrayExpression(ArrayExpression ast, Void arg) {
        ast.AA.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
        AbstractSyntaxTree replacement1 = ast.E1.visit(this);
        AbstractSyntaxTree replacement2 = ast.E2.visit(this);
        ast.O.visit(this);

        // if visiting a child node returns something, it's either the original constant
        // (IntegerLiteral) or a folded version replacing the expression at that child
        // node
        // If both child nodes are not null; return a folded version of this
        // BinaryExpression
        // Otherwise, at least one child node isn't constant (foldable) so just replace
        // the
        // foldable child nodes with their folded equivalent and return null
        if (replacement1 != null && replacement2 != null) {
            return foldBinaryExpression(replacement1, replacement2, ast.O);
        } else if (replacement1 != null) {
            ast.E1 = (Expression) replacement1;
        } else if (replacement2 != null) {
            ast.E2 = (Expression) replacement2;
        }

        // if we get here, we can't fold any higher than this level
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCallExpression(CallExpression ast, Void arg) {
        ast.APS.visit(this);
        ast.I.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharacterExpression(CharacterExpression ast, Void arg) {
        ast.CL.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyExpression(EmptyExpression ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIfExpression(IfExpression ast, Void arg) {
        AbstractSyntaxTree replacement1 = ast.E1.visit(this);
        if (replacement1 != null) {
            ast.E1 = (Expression) replacement1;
        }
        AbstractSyntaxTree replacement2 = ast.E2.visit(this);
        if (replacement2 != null) {
            ast.E2 = (Expression) replacement2;
        }
        AbstractSyntaxTree replacement3 = ast.E3.visit(this);
        if (replacement3 != null) {
            ast.E3 = (Expression) replacement3;
        }

        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntegerExpression(IntegerExpression ast, Void arg) {
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitLetExpression(LetExpression ast, Void arg) {
        ast.D.visit(this);
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        return null;
    }

    @Override
    public AbstractSyntaxTree visitRecordExpression(RecordExpression ast, Void arg) {
        ast.RA.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitUnaryExpression(UnaryExpression ast, Void arg) {
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }

        ast.O.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitVnameExpression(VnameExpression ast, Void arg) {
        ast.V.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {
        ast.ARG1.visit(this);
        ast.ARG2.visit(this);
        ast.O.visit(this);
        ast.RES.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitConstDeclaration(ConstDeclaration ast, Void arg) {
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        ast.I.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitFuncDeclaration(FuncDeclaration ast, Void arg) {
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        ast.FPS.visit(this);
        ast.I.visit(this);
        ast.T.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitProcDeclaration(ProcDeclaration ast, Void arg) {
        ast.C.visit(this);
        ast.FPS.visit(this);
        ast.I.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
        ast.D1.visit(this);
        ast.D2.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitTypeDeclaration(TypeDeclaration ast, Void arg) {
        ast.I.visit(this);
        ast.T.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void arg) {
        ast.ARG.visit(this);
        ast.O.visit(this);
        ast.RES.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitVarDeclaration(VarDeclaration ast, Void arg) {
        ast.I.visit(this);
        ast.T.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitAssignCommand(AssignCommand ast, Void arg) {
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        // add all assignments to the map if their not assignments in a while loop
        if (!isInWhile)
            addToVarMap(ast);
        ast.V.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCallCommand(CallCommand ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyCommand(EmptyCommand ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
        ast.C1.visit(this);
        ast.C2.visit(this);
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        return null;
    }

    @Override
    public AbstractSyntaxTree visitLetCommand(LetCommand ast, Void arg) {
        ast.C.visit(this);
        ast.D.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSequentialCommand(SequentialCommand ast, Void arg) {
        ast.C1.visit(this);
        ast.C2.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
        this.isInWhile = true; // note that while loop is entered
        collectVars(ast.C); // collect all variables in the loop
        hoist(ast.C); // start the hoisting process
        ast.C.visit(this); // visit after the values have been hoisted
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        return null;
    }

    @Override
    public AbstractSyntaxTree visitLoopWhileCommand(LoopWhileCommand ast, Void arg) {
        ast.C1.visit(this);
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        ast.C2.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void arg) {
        ast.AA.visit(this);
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleArrayAggregate(SingleArrayAggregate ast, Void arg) {
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Void arg) {
        ast.AP.visit(this);
        ast.APS.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleActualParameterSequence(SingleActualParameterSequence ast, Void arg) {
        ast.AP.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitConstActualParameter(ConstActualParameter ast, Void arg) {
        AbstractSyntaxTree replacement = ast.E.visit(this);
        if (replacement != null) {
            ast.E = (Expression) replacement;
        }
        return null;
    }

    @Override
    public AbstractSyntaxTree visitFuncActualParameter(FuncActualParameter ast, Void arg) {
        ast.I.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitProcActualParameter(ProcActualParameter ast, Void arg) {
        ast.I.visit(this);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitVarActualParameter(VarActualParameter ast, Void arg) {
        ast.V.visit(this);
        return null;
    }

    public AbstractSyntaxTree foldBinaryExpression(AbstractSyntaxTree node1, AbstractSyntaxTree node2, Operator o) {
        // the only case we know how to deal with for now is two IntegerExpressions
        if ((node1 instanceof IntegerExpression) && (node2 instanceof IntegerExpression)) {
            int int1 = (Integer.parseInt(((IntegerExpression) node1).IL.spelling));
            int int2 = (Integer.parseInt(((IntegerExpression) node2).IL.spelling));
            Object foldedValue = null;

            if (o.decl == StdEnvironment.addDecl) {
                foldedValue = int1 + int2;
            }
            // check decl is true or false
            else if (o.decl == StdEnvironment.trueDecl) {
                foldedValue = true;
            } else if (o.decl == StdEnvironment.falseDecl) {
                foldedValue = false;
            }

            if (foldedValue instanceof Integer) {
                IntegerLiteral il = new IntegerLiteral(foldedValue.toString(), node1.getPosition());
                IntegerExpression ie = new IntegerExpression(il, node1.getPosition());
                ie.type = StdEnvironment.integerType;
                return ie;
            } else if (foldedValue instanceof Boolean) {
                String spelling = (boolean) foldedValue ? "true" : "false"; // set spelling based on condition
                Identifier bool = new Identifier(spelling, node1.getPosition()); // create the identifier
                bool.decl = (boolean) foldedValue ? StdEnvironment.trueDecl : StdEnvironment.falseDecl; // set the decl
                                                                                                        // based on
                                                                                                        // condition

                SimpleVname sVname = new SimpleVname(bool, node1.getPosition()); // wrap in simple v name
                VnameExpression ve = new VnameExpression(sVname, node1.getPosition()); // wrap sVname in vName
                                                                                       // expression

                return ve;

            }
        }

        // any unhandled situation (i.e., not foldable) is ignored
        return null;
    }

    /**
     * Method converts AST to Sequential Command and passes
     * to recursive parser
     *
     * @param C the AST in the while loop
     * @return null
     */
    public AbstractSyntaxTree collectVars(AbstractSyntaxTree C) {
        SequentialCommand sq = (SequentialCommand) C;
        recursiveParse(sq);
        return null;
    }

    /**
     * recursively traverses the sq tree and adds all AssignCommands to the map
     *
     * @param sq the Sequential Command to start parsing
     * @return null
     */
    public AbstractSyntaxTree recursiveParse(SequentialCommand sq) {
        // recurse if not assignCommand
        if (sq.C1 instanceof SequentialCommand sequentialCommand) {
            recursiveParse(sequentialCommand);
        }
        // cast and add to map
        if (sq.C2 instanceof AssignCommand a) {
            addToHoistableMap(a);
        }
        return null;
    }

    /**
     * adds the assign command with a value as false to the hoistables map
     *
     * @param a Assignment command
     */
    public void addToHoistableMap(AssignCommand a) {
        // swap order so variable always comes first in a binary expression
        // if both are variables swapping won't have any affect on result
        if (a.E instanceof BinaryExpression b && b.E2 instanceof VnameExpression) {
            Expression tmp = b.E1;
            b.E1 = b.E2;
            b.E2 = tmp;
        }
        this.hoistables.put(a, false);
    }

    /**
     * adds all variables before while loop to a map to store value
     *
     * @param A the assign command
     */
    public void addToVarMap(AssignCommand A) {
        this.hoistables.put(A, false);
    }

    /**
     * gets SimpleVname from Vname of an assignment command
     *
     * @param A assignment command
     * @return simple vname
     */
    public SimpleVname getSimpleVname(AssignCommand A) {
        return (SimpleVname) A.V;
    }

    /**
     * Gets a Binary or Integer expression from an assignment command
     *
     * @param A assignment command
     * @return Binary or Integer Expression
     */
    public Expression getExpression(AssignCommand A) {
        if (A.E instanceof BinaryExpression binaryExpression)
            return binaryExpression;
        return (IntegerExpression) A.E;
    }

    /**
     * Returns a string representation of the expression
     *
     * @param B binary expression
     * @return
     */
    public String getBinaryExpressionString(BinaryExpression B) {
        StringBuffer expressionStringBuilder = new StringBuffer();
        String expressionVname = ((SimpleVname) ((VnameExpression) B.E1).V).I.spelling;
        if (B.E2 instanceof VnameExpression)
            return null;
        String i = ((IntegerExpression) B.E2).IL.spelling;
        expressionStringBuilder.append(expressionVname); // the variable
        expressionStringBuilder.append(B.O.spelling); // the operator
        expressionStringBuilder.append(i); // the integer
        return expressionStringBuilder.toString();
    }

    /**
     * uses both maps (hoistables and assignedVars) to select candidates for
     * hoisting and stores in canHoist ArrayList to be hoisted
     */
    public void verifyHoistables() {
        // check which ones can be hoisted
        for (HashMap.Entry<AssignCommand, Boolean> e : hoistables.entrySet()) {

            AssignCommand A = e.getKey();
            // continue if this is not an assignment in the loop
            if (getExpression(A) instanceof IntegerExpression)
                continue;

            // get the lhs variable as a string
            String variable = this.getSimpleVname(A).I.spelling;

            // convert the binary expression to a string

            String stringExpression = this.getBinaryExpressionString((BinaryExpression) getExpression(e.getKey()));
            if (stringExpression == null)
                continue;

            // if binary expression doesn't contain the variable on the LHS of assignment
            if (!stringExpression.contains(variable)) {
                // if a variable in the binary expression is assigned twice, don't fold
                // (variable can be assigned once before while loop and once after. This is fine
                // for hoisting but more means it will not be hoisted since the value is
                // changing)
                boolean flag = false;
                for (AssignCommand a : hoistables.keySet()) {
                    if (stringExpression.contains(getSimpleVname(a).I.spelling)) {
                        if (flag) {
                            e.setValue(false);
                            break;
                        }
                        flag = true;
                    }
                    e.setValue(true);
                }

            }
        }
    }

    // get the value of the variable during initialisation
    public AssignCommand getAssignmentValue(String variable) {
        for (HashMap.Entry<AssignCommand, Boolean> e : hoistables.entrySet()) {
            if (getSimpleVname(e.getKey()).I.spelling.equals(variable))
                return e.getKey();
        }
        return null;
    }

    // set the new value of the variable during initialising (before while loop)
    public void setAssignmentValue(IntegerExpression i, String variable) {
        for (HashMap.Entry<AssignCommand, Boolean> e : hoistables.entrySet()) {
            if (getSimpleVname(e.getKey()).I.spelling.equals(variable)) {
                e.getKey().E = i;
                break;
            }
        }
    }

    /**
     * verify hoistables and add hoist them by converting the hoisted variable to a
     * integer exprssion
     *
     * @param code the block of code in the while loop
     * @return null
     */
    public AbstractSyntaxTree hoist(AbstractSyntaxTree code) {

        verifyHoistables();

        // converts a variable to its value and stores it as Integer expression in the
        // binary expression
        for (HashMap.Entry<AssignCommand, Boolean> e : hoistables.entrySet()) {

            AssignCommand A = e.getKey();
            if (getExpression(A) instanceof IntegerExpression || e.getValue() == false)
                continue;

            BinaryExpression b = (BinaryExpression) getExpression(A);

            SimpleVname lhsName = getSimpleVname(A);

            // if both are numbers
            if (b.E1 instanceof IntegerExpression && b.E2 instanceof IntegerExpression) {
                IntegerExpression i = (IntegerExpression) b.visit(this);
                A.E = i;
                setAssignmentValue(i, lhsName.I.spelling);
                continue;
            }
            // if both are variables
            else if (b.E1 instanceof VnameExpression && b.E2 instanceof VnameExpression) {
                // get spellings
                String rhsVariable1 = ((SimpleVname) ((VnameExpression) b.E1).V).I.spelling;
                String rhsVariable2 = ((SimpleVname) ((VnameExpression) b.E2).V).I.spelling;
                // get the values during initialization (before while)
                AssignCommand rhsAssignCommand1 = getAssignmentValue(rhsVariable1);
                AssignCommand rhsAssignCommand2 = getAssignmentValue(rhsVariable2);
                // convert values to integer expression
                IntegerExpression rhsVariableValue1 = (IntegerExpression) rhsAssignCommand1.E;
                IntegerExpression rhsVariableValue2 = (IntegerExpression) rhsAssignCommand2.E;
                // set them as expressions
                b.E1 = rhsVariableValue1;
                b.E2 = rhsVariableValue2;
                IntegerExpression i = (IntegerExpression) b.visit(this);
                A.E = i;
                setAssignmentValue(i, lhsName.I.spelling);
                continue;
            }

            // if number and variable
            String rhsVariable = ((SimpleVname) ((VnameExpression) b.E1).V).I.spelling;
            AssignCommand rhsAssignCommand = getAssignmentValue(rhsVariable);
            IntegerExpression rhsVariableValue = (IntegerExpression) rhsAssignCommand.E;
            b.E1 = rhsVariableValue;
            IntegerExpression i = (IntegerExpression) b.visit(this);
            A.E = i;
            setAssignmentValue(i, lhsName.I.spelling);
        }

        return null;
    }

}
