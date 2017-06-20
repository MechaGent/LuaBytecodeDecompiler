package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Labels.Label;
import CleanStart.Mk04_Working.VarFormula.Formula;

public class Jump_Branched extends BaseInstruct
{
	private static int instanceCounter = 0;

	private final Formula test;
	private final Label jump_True;
	private final boolean isNegative_True; // if true, variable jump_True is first shared label
	private final Label jump_False;
	private final boolean isNegative_False; // if true, variable jump_False is first shared label

	private BranchTypes branchType;
	private int numPrecedingLines;

	public Jump_Branched(Formula inTest, Label inJump_True, boolean inIsNegative_True, Label inJump_False, boolean inIsNegative_False)
	{
		super(IrCodes.Jump_Conditional, instanceCounter++);
		this.test = inTest;
		this.jump_True = inJump_True;
		this.isNegative_True = inIsNegative_True;
		this.jump_False = inJump_False;
		this.isNegative_False = inIsNegative_False;

		if (inIsNegative_True | inIsNegative_False)
		{
			this.branchType = BranchTypes.DoWhile;
		}
		else
		{
			this.branchType = BranchTypes.IfElse;
		}
		
		this.numPrecedingLines = 0;
	}

	public static Jump_Branched getInstance(Formula inTest, Label inJump_True, Label inJump_False)
	{
		return getInstance(inTest, inJump_True, false, inJump_False, false);
	}

	public static Jump_Branched getInstance(Formula inTest, Label inJump_True, boolean inIsNegative_True, Label inJump_False, boolean inIsNegative_False)
	{
		final Jump_Branched result = new Jump_Branched(inTest, inJump_True, inIsNegative_True, inJump_False, inIsNegative_False);
		inJump_True.setInstructionAsIncoming(result, 0);
		inJump_False.setInstructionAsIncoming(result, 1);
		return result;
	}

	public Formula getTest()
	{
		return this.test;
	}

	public Label getJump_True()
	{
		return this.jump_True;
	}

	public boolean isNegative_True()
	{
		return this.isNegative_True;
	}

	public Label getJump_False()
	{
		return this.jump_False;
	}

	public boolean isNegative_False()
	{
		return this.isNegative_False;
	}
	
	public BranchTypes getBranchType()
	{
		return this.branchType;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.NonApplicable;
	}

	@Override
	public Variable getInlinedForm()
	{
		return null;
	}

	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add("<best guess: ");
		result.add(this.branchType.toString());
		result.add("> if (");
		result.add(this.test.getValueAsCharList(), true);
		result.add("), then jump to ");
		result.add(this.jump_True.getLabelValue());
		result.add(" (");

		if (this.isNegative_True)
		{
			result.add("Negative");
		}
		else
		{
			result.add("Positive");
		}

		result.add("); else, jump to ");
		result.add(this.jump_False.getLabelValue());

		result.add(" (");

		if (this.isNegative_False)
		{
			result.add("Negative");
		}
		else
		{
			result.add("Positive");
		}

		result.add(')');

		return result;
	}

	@Override
	public void revVarTypes()
	{
		this.test.revVarType();
	}

	public void setBranchType(BranchTypes in)
	{
		this.branchType = in;
	}
	
	public void setNumPrecedingLines(int inNumPrecedingLines)
	{
		this.numPrecedingLines = inNumPrecedingLines;
	}

	public int getNumPrecedingLines()
	{
		return this.numPrecedingLines;
	}

	public static enum BranchTypes
	{
		ElselessIf,
		IflessElse,
		IfElse,
		While,
		DoWhile;
	}
}
