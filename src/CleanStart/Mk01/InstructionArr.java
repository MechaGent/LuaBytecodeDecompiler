package CleanStart.Mk01;

import CleanStart.Mk01.Interfaces.Instruction;
import HalfByteRadixMap.HalfByteRadixMap;

public class InstructionArr
{
	private final Instruction[] core;
	private final HalfByteRadixMap<Integer> jumpLabels;
	
	public InstructionArr(Instruction[] inCore, HalfByteRadixMap<Integer> inJumpLabels)
	{
		this.core = inCore;
		this.jumpLabels = inJumpLabels;
	}
}
