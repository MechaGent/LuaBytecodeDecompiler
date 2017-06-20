package CleanStart.Mk01.Instructions;

import CharList.CharList;
import CleanStart.Mk01.BaseInstruction;
import CleanStart.Mk01.Enums.IrCodes;

public class NonOp extends BaseInstruction
{
	private static final NonOp sharedInstance = new NonOp();
	
	private NonOp()
	{
		super(IrCodes.NonOp, -1, -1, -1, 0);
	}

	public static NonOp getSharedInstance()
	{
		return sharedInstance;
	}

	@Override
	protected void handleInstructionIo()
	{
		
	}
	
	@Override
	public CharList translateIntoCommentedByteCode(int inOffset, CharList in)
	{
		in.add('\t', inOffset);
		in.add("<NonOp>");
		return in;
	}
}
