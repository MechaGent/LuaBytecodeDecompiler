package LuaBytecodeDecompiler.Mk02.Instructions;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;

public class Instruction_Composite extends Instruction
{
	private final int numInstructions;
	
	public Instruction_Composite(int inInstructionAddress, int inNumInstructions)
	{
		super(inInstructionAddress, InstructionTypes.HighLevel, AbstractOpCodes.CompositeOpCode);
		this.numInstructions = inNumInstructions;
	}

	@Override
	public VarCode getVarCode_Left()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VarCode getVarCode_Right()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CharList disAssemble()
	{
		final CharList result = this.disAssemble_start();
		
		result.add(Integer.toString(this.numInstructions));
		result.add('}');
		
		return result;
	}
}
