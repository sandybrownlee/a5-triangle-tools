package triangle.codegen;

import triangle.abstractMachine.Register;
import triangle.repr.Declaration;
import triangle.repr.Expression;
import triangle.repr.Expression.BinaryOp;
import triangle.repr.Expression.Identifier.BasicIdentifier;
import triangle.repr.Expression.LitInt;
import triangle.repr.Expression.SequenceExpression;
import triangle.repr.Statement;
import triangle.repr.Statement.AssignStatement;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static triangle.repr.Type.INT_TYPE;

// TODO: split optimization and writing out
// TODO: parameterize CodeGen with DataOutputStream
public final class CodeGen {

    public static void write(final String objName, final List<Instruction.TAMInstruction> instructions) throws IOException {
        try (DataOutputStream fw = new DataOutputStream(new FileOutputStream(objName))) {
            for (Instruction.TAMInstruction i : instructions) {
                fw.writeInt(i.op());
                fw.writeInt(i.r());
                fw.writeInt(i.n());
                fw.writeInt(i.d());
            }
        }
    }

    // TODO: maybe do some optimizations before backpatching? eg. any label immediately followed by a jump can just edit all jumps
    //  to that label
    // backpatch the instruction list to resolve all labels, etc.
    public static List<Instruction.TAMInstruction> backpatch(final List<Instruction> instructions) {
        Map<Instruction.LABEL, Integer> labelLocations = new HashMap<>();

        int offset = 0;
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            if (instruction instanceof Instruction.LABEL label) {
                labelLocations.put(label, i - offset);
                offset += 1;
            }
        }

        // @formatter:off
        // so I don't have to type 'new Address ...' repeatedly
        Function<Instruction.LABEL, Instruction.Address> toCodeAddress = label -> new Instruction.Address(
                Register.CB,
                labelLocations.get(label)
        );
        // @formatter:on

        List<Instruction.TAMInstruction> patchedInstructions = new ArrayList<>();
        for (Instruction instruction : instructions) {
            switch (instruction) {
                case Instruction.LABEL _ -> { }
                case Instruction.CALL_LABEL(Register staticLink, Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.CALL(staticLink, toCodeAddress.apply(label)));
                case Instruction.JUMPIF_LABEL(int value, Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.JUMPIF(value, toCodeAddress.apply(label)));
                case Instruction.JUMP_LABEL(Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.JUMP(toCodeAddress.apply(label)));
                case Instruction.LOADA_LABEL(Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.LOADA(toCodeAddress.apply(label)));
                case Instruction.TAMInstruction tamInstruction -> patchedInstructions.add(tamInstruction);
            }
        }

        return patchedInstructions;
    }

    public static List<Instruction> generate(Statement statement) {
        ConstantFolder constantFolder = new ConstantFolder();
        IRGenerator irGenerator = new IRGenerator();
        Hoister hoister = new Hoister();
        // small class to rewrite ++ and ** unary operations, must be done before hoisting
        // this is incredibly ad-hoc, a real compiler would convert the AST to some appropriate IR before attempting these
        // optimizations, but we are operating directly on the AST so this just does some ad-hoc rewriting for some of the
        // unary operations
        // TODO: this really should be done after SemanticAnalzer and before TypeChecking
        RewriteStage incrementRewriter = new RewriteStage() {

            private int fresh = 0;

            @Override public Expression rewrite(final Expression expression) {
                Expression rewritten = RewriteStage.super.rewrite(expression);

                //@formatter:off
                // if "++"
                if (rewritten instanceof Expression.UnaryOp unaryOp && unaryOp.operator().name().equals("++")) {
                    if (unaryOp.operand() instanceof BasicIdentifier identifier) {
                        // after identifier := identifier + 1 return identifier
                        Expression hoisted = new BinaryOp(new BasicIdentifier("+"), identifier, new LitInt(1)).withType(INT_TYPE);
                        return new SequenceExpression(new AssignStatement(identifier, hoisted), identifier).withType(INT_TYPE);
                    }

                    String generatedName = "rw_increment_" +
                                           unaryOp.operand().sourcePosition().lineNo() + "_" +
                                           unaryOp.operand().sourcePosition().colNo();
                    // let const <generated_name> ~ unaryop.operand() + 1 in <generated_name>
                    Expression hoisted = new BinaryOp(new BasicIdentifier("+"),
                                                      unaryOp.operand(),
                                                      new LitInt(1)).withType(INT_TYPE);
                    return new Expression.LetExpression(List.of(new Declaration.ConstDeclaration(generatedName, hoisted)),
                                                        new BasicIdentifier(generatedName).withType(INT_TYPE));
                }
                //@formatter:on

                //@formatter:off
                // if "**"
                if (rewritten instanceof Expression.UnaryOp unaryOp && unaryOp.operator().name().equals("**")) {
                    if (unaryOp.operand() instanceof BasicIdentifier identifier) {
                        // after identifier := identifier * identifier return identifier
                        Expression hoisted = new BinaryOp(new BasicIdentifier("*"),
                                                          identifier,
                                                          identifier).withType(INT_TYPE);
                        return new SequenceExpression(new AssignStatement(identifier, hoisted), identifier).withType(INT_TYPE);
                    }

                    String generatedName = "rw_square_" + fresh++;
                    // must duplicate expression in const declaration, in case it has side-effects4
                    // let const <generated_name> ~ unaryop.operand() * unaryop.operand() in <generated_name>
                    Expression hoisted = new BinaryOp(new BasicIdentifier("*"),
                                                      unaryOp.operand(),
                                                      unaryOp.operand()).withType(INT_TYPE);
                    return new Expression.LetExpression(List.of(new Declaration.ConstDeclaration(generatedName, hoisted)),
                                                        new BasicIdentifier(generatedName).withType(INT_TYPE));
                }
                //@formatter:on

                return rewritten;
            }
        };

        return irGenerator.generateIR(hoister.hoist(incrementRewriter.rewrite(constantFolder.fold(statement))));
    }


}
