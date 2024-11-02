package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;

@Deprecated public interface ArrayAggregateVisitor<TArg, TResult> {

    TResult visitMultipleArrayAggregate(MultipleArrayAggregate ast, TArg arg);

    TResult visitSingleArrayAggregate(SingleArrayAggregate ast, TArg arg);

}
