package LuaBytecodeDecompiler.Mk02;

import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction;

public class InstructionCounter
{
	private final int[] counters;
	
	public InstructionCounter()
	{
		this.counters = new int[AbstractOpCodes.getNumElements()];
	}
	
	public void countInstruction(Instruction in)
	{
		this.countInstruction(in.getOpCode());
	}
	
	public void countInstruction(AbstractOpCodes code)
	{
		this.counters[code.ordinal()]++;
	}
	
	public int getCountFor(AbstractOpCodes code)
	{
		return this.counters[code.ordinal()];
	}
}
