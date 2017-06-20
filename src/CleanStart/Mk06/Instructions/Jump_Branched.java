package CleanStart.Mk06.Instructions;

import CharList.CharList;
import CleanStart.Mk06.InstructionIndex;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Enums.IrCodes;
import CleanStart.Mk06.StepNum.ForkBehaviors;
import CleanStart.Mk06.VarFormula.Formula;

public class Jump_Branched extends Jump_Straight
{
	private static int InstanceCounter = 0;
	
	protected final Formula test;
	protected final Label jumpTo_ifFalse;
	protected final boolean falseJumpIsNegative;
	
	private BranchTypes branchType;
	private InstructionList_Locked precedingLines;
	
	private Jump_Branched(StepNum inStartTime, Label inJumpTo, boolean inIsNegativeJump, Formula inTest, Label inJumpTo_ifFalse, boolean inFalseJumpIsNegative)
	{
		this(IrCodes.Jump_Branched, InstanceCounter++, inStartTime, inJumpTo, inIsNegativeJump, inTest, inJumpTo_ifFalse, inFalseJumpIsNegative);
	}
	
	protected Jump_Branched(IrCodes inIrCode, int inInstanceNumber_Specific, StepNum inStartTime, Label inJumpTo, boolean inIsNegativeJump, Formula inTest, Label inJumpTo_ifFalse, boolean inFalseJumpIsNegative)
	{
		super(inIrCode, inInstanceNumber_Specific, inStartTime, inJumpTo, inIsNegativeJump);
		this.test = inTest;
		this.jumpTo_ifFalse = inJumpTo_ifFalse;
		this.falseJumpIsNegative = inFalseJumpIsNegative;
		this.branchType = BranchTypes.IfElse;
	}

	public static Jump_Branched getInstance(StepNum inStartTime, Label inJumpTo, boolean inIsNegativeJump, Formula inTest, Label inJumpTo_ifFalse, boolean inFalseJumpIsNegative)
	{
		final Jump_Branched result = new Jump_Branched(inStartTime, inJumpTo, inIsNegativeJump, inTest, inJumpTo_ifFalse, inFalseJumpIsNegative);
		result.jumpTo.setAsIncoming(InstructionIndex.getInstance(result, 0));
		result.jumpTo_ifFalse.setAsIncoming(InstructionIndex.getInstance(result, 1));
		result.logAllReadsAndWrites();
		return result;
	}
	
	public Formula getTest()
	{
		return this.test;
	}

	public Label getJumpTo_ifFalse()
	{
		return this.jumpTo_ifFalse;
	}

	@Override
	public void logAllReadsAndWrites()
	{
		StepNum current = this.startTime.fork(ForkBehaviors.ForkToSubstage);
		
		//log test
		current = current.fork(ForkBehaviors.IncrementLast);
	}
	
	@Override
	protected CharList toVerboseCommentForm_internal(boolean inAppendMetadata)
	{
		final CharList result = new CharList();

		result.add("if ");
		result.add(this.test.getValueAsString());
		result.add(", then jump to ");
		result.add(this.jumpTo.getMangledName(inAppendMetadata));
		
		if (this.isNegativeJump)
		{
			result.add(" (Negative)");
		}
		else
		{
			result.add(" (Positive)");
		}
		
		result.add(". Otherwise, jump to ");
		result.add(this.jumpTo_ifFalse.getMangledName(inAppendMetadata));
		
		if (this.falseJumpIsNegative)
		{
			result.add(" (Negative)");
		}
		else
		{
			result.add(" (Positive)");
		}

		return result;
	}
	
	public void setBranchType(BranchTypes in)
	{
		this.branchType = in;
	}
	
	public BranchTypes getBranchType()
	{
		return this.branchType;
	}
	
	public InstructionList_Locked getPrecedingLines()
	{
		return this.precedingLines;
	}

	public void setPrecedingLines(InstructionList_Locked inPrecedingLines)
	{
		this.precedingLines = inPrecedingLines;
	}
	
	@Override
	protected CharList toDecompiledForm_internal(int offset)
	{
		final CharList result = new CharList();

		result.add('\t', offset);
		result.add("if (");
		result.add(this.test.getValueAsString());
		result.add(')');

		return result;
	}

	public static enum BranchTypes
	{
		While,
		IfElse,
		If,
		Else,
		For,
		ForEach;
	}
}
