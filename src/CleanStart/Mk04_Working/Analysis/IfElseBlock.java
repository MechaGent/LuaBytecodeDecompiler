package CleanStart.Mk04_Working.Analysis;

import CleanStart.Mk04_Working.Instructions.Jump_Branched;

public class IfElseBlock implements MetaCodeBlock
{
	private final Jump_Branched expression;
	private final MetaCodeBlock[] BlockIfTrue;
	private final MetaCodeBlock[] BlockIfFalse;
	private final MetaCodeBlock JoinBlock;
	
	private IfElseBlock(Jump_Branched inExpression, MetaCodeBlock[] inBlockIfTrue, MetaCodeBlock[] inBlockIfFalse, MetaCodeBlock inJoinBlock)
	{
		this.expression = inExpression;
		this.BlockIfTrue = inBlockIfTrue;
		this.BlockIfFalse = inBlockIfFalse;
		this.JoinBlock = inJoinBlock;
	}
	
	public static IfElseBlock getInstance(Jump_Branched inExpression, MetaCodeBlock[] inBlockIfTrue, MetaCodeBlock[] inBlockIfFalse, MetaCodeBlock inJoinBlock)
	{
		return new IfElseBlock(inExpression, inBlockIfTrue, inBlockIfFalse, inJoinBlock);
	}
}
