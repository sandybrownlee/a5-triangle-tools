package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;

@Deprecated public interface RecordAggregateVisitor<TArg, TResult> {

    TResult visitMultipleRecordAggregate(MultipleRecordAggregate ast, TArg arg);

    TResult visitSingleRecordAggregate(SingleRecordAggregate ast, TArg arg);

}
