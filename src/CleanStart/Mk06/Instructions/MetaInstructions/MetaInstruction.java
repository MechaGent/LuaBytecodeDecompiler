package CleanStart.Mk06.Instructions.MetaInstructions;

import CharList.CharList;
import CleanStart.Mk06.Instruction;
import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Enums.IrCodes;

public abstract class MetaInstruction extends Instruction
{
	protected final MetaIrCodes MetaIrCode;
	
	protected MetaInstruction(MetaIrCodes inIrCode, int inInstanceNumber_Specific, StepNum inStartTime)
	{
		super(IrCodes.MetaOp, inInstanceNumber_Specific, inStartTime);
		this.MetaIrCode = inIrCode;
	}
	
	public MetaIrCodes getMetaIrCode()
	{
		return this.MetaIrCode;
	}

	@Override
	public CharList toVerboseCommentForm(boolean appendMetadata)
	{
		final CharList result = this.toVerboseCommentForm_internal(appendMetadata);

		result.push('\t');
		result.push(this.MetaIrCode.toString());
		result.push('m');
		result.push('\t');
		result.push(this.startTime.getInDiagForm(), true);

		if (this.isExpired)
		{
			result.push('\t');
			result.push("//");
		}

		return result;
	}
}
