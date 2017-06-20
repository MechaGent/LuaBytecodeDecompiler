package CleanStart.Mk01.Instructions;

import CharList.CharList;
import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Interfaces.JumpingInstruction;

public class JumpStraight extends BaseInstruction implements JumpingInstruction
{
	private JumpLabel jumpTarget;

	private JumpStraight(int line, int subLine, int startStep, JumpLabel inJumpTarget)
	{
		super(IrCodes.Jump_Straight, line, subLine, startStep, 0);
		this.jumpTarget = inJumpTarget;
	}
	
	public static JumpStraight getInstance(int line, int subLine, int startStep, JumpLabel inJumpTarget)
	{
		final JumpStraight result = new JumpStraight(line, subLine, startStep, inJumpTarget);
		inJumpTarget.addIncomingJump(result, 0);
		//result.handleInstructionIo(inCreationTime);
		return result;
	}

	public JumpLabel getJumpTarget()
	{
		return this.jumpTarget;
	}

	@Override
	public void setIndexedJumpLabelTo(int inIndex, JumpLabel inIn)
	{
		if(inIndex == 0)
		{
			this.jumpTarget = inIn;
		}
		else
		{
			throw new IndexOutOfBoundsException();
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
		result.add("jumps to: ");
		result.add(this.jumpTarget.getLabel());
		return result;
	}

	@Override
	protected void handleInstructionIo()
	{
		
	}
}
