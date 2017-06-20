package CleanStart.Mk01;

import CharList.CharList;
import CleanStart.Mk01.Enums.IrCodes;
import CleanStart.Mk01.Interfaces.Instruction;
import HalfByteArray.HalfByteArray;
import HalfByteArray.HalfByteArrayFactory;

public abstract class BaseInstruction implements Instruction
{
	//protected Instruction previous;	//can get away with being linear here, because all joins are handled in JumpLabels
	private static int instanceCounter = 0;
	
	protected final IrCodes OpCode;
	protected final String mangledName;
	//protected final StepStage timeOfCreation;
	protected final StepStage[] sigTimes;	//[0] == timeOfCreation
	protected boolean isExpired;
	
	/**
	 * 
	 * @param inOpCode
	 * @param line
	 * @param subLine
	 * @param startStep
	 * @param numAdditionalSteps should not include initial creation time
	 */
	public BaseInstruction(IrCodes inOpCode, int line, int subLine, int startStep, int numAdditionalSteps)
	{
		this(inOpCode, StepStage.requestSequence(line, subLine, startStep, numAdditionalSteps + 1));
	}
	
	public BaseInstruction(IrCodes inOpCode, StepStage[] inSigTimes)
	{
		this.OpCode = inOpCode;
		this.mangledName = inOpCode.toString() + instanceCounter++;
		this.sigTimes = inSigTimes;
		this.isExpired = false;
	}
	
	@Override
	public HalfByteArray getHalfByteHash()
	{
		return HalfByteArrayFactory.convertIntoArray(this.mangledName);
	}
	
	public boolean isExpired()
	{
		return this.isExpired;
	}
	
	public void setExpiredState(boolean in)
	{
		this.isExpired = in;
	}
	
	@Override
	public IrCodes getIrCode()
	{
		return this.OpCode;
	}
	
	protected abstract void handleInstructionIo();
	
	@Override
	public CharList translateIntoCommentedByteCode(int offset)
	{
		return this.translateIntoCommentedByteCode(offset, new CharList());
	}
	
	@Override
	public CharList translateIntoCommentedByteCode(int inOffset, CharList in)
	{
		in.add('\t', inOffset);
		in.add("<>: ");
		in.add(this.toString());
		
		//System.out.println(this.toString());
		
		return in;
	}
	
	@Override
	public CharList translateIntoCommentedByteCode(int offset, CharList in, StepStage time)
	{
		in.add('\t', offset);
		in.add("<>: ");
		in.add(this.toString());
		
		//System.out.println(this.toString());
		
		return in;
	}
}
