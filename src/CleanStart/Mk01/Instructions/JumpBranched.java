package CleanStart.Mk01.Instructions;

import CharList.CharList;
import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Interfaces.JumpingInstruction;
import CleanStart.Mk01.VarFormula.Formula;

public class JumpBranched extends BaseInstruction implements JumpingInstruction
{
	private final Formula formula;
	private JumpLabel jumpIfTrue;
	private JumpLabel jumpIfFalse;
	
	private JumpBranched(int line, int subLine, int startStep, Formula inFormula, JumpLabel inJumpIfTrue, JumpLabel inJumpIfFalse)
	{
		super(IrCodes.Jump_Conditional, line, subLine, startStep, 0);
		this.formula = inFormula;
		this.jumpIfTrue = inJumpIfTrue;
		this.jumpIfFalse = inJumpIfFalse;
	}
	
	public static JumpBranched getInstance(int line, int subLine, int startStep, Formula inFormula, JumpLabel inJumpIfTrue, JumpLabel inJumpIfFalse)
	{
		final JumpBranched result = new JumpBranched(line, subLine, startStep, inFormula, inJumpIfTrue, inJumpIfFalse);
		inJumpIfTrue.addIncomingJump(result, 0);
		inJumpIfFalse.addIncomingJump(result, 1);
		return result;
	}

	public Formula getFormula()
	{
		return this.formula;
	}

	public JumpLabel getJumpIfTrue()
	{
		return this.jumpIfTrue;
	}

	public JumpLabel getJumpIfFalse()
	{
		return this.jumpIfFalse;
	}

	@Override
	public void setIndexedJumpLabelTo(int inIndex, JumpLabel inIn)
	{
		switch(inIndex)
		{
			case 0:
			{
				this.jumpIfTrue = inIn;
				break;
			}
			case 1:
			{
				this.jumpIfFalse = inIn;
				break;
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}
	}

	@Override
	public CharList translateIntoCommentedByteCode(int inOffset, CharList result)
	{
		result.add('\t', inOffset);
		result.add(this.OpCode.toString());
		result.add('\t');
		result.add(this.mangledName);
		result.add('\t');
		result.add("if (");
		result.add(this.formula.toCharList(this.sigTimes[0]), true);
		result.add("), then jump to ");
		result.add(this.jumpIfTrue.getLabel());
		result.add("; else, jump to ");
		result.add(this.jumpIfFalse.getLabel());
		return result;
	}

	@Override
	protected void handleInstructionIo()
	{
		
	}
}
