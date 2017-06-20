package CleanStart.Mk01.Interfaces;

import CleanStart.Mk01.Instructions.JumpLabel;

public interface JumpingInstruction extends Instruction
{
	public void setIndexedJumpLabelTo(int index, JumpLabel in);
}
