package LuaBytecodeDecompiler.Mk02;

import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction;
import LuaBytecodeDecompiler.Mk02.Instructions.Instruction_iABC;

public class RegistersFlowTimeline
{
	/*
	 * Instructions that effect RegisterState
	 * Move, LoadNil, LoadBool, LoadK
	 */

	private final RegFlow[] regA;
	private final RegFlow[] regB;
	private final RegFlow[] regC;

	private RegistersFlowTimeline(RegFlow[] inRegA, RegFlow[] inRegB, RegFlow[] inRegC)
	{
		this.regA = inRegA;
		this.regB = inRegB;
		this.regC = inRegC;
	}

	public static RegistersFlowTimeline parseRegisterFlowTimeline(Function rootFunction)
	{
		final SingleLinkedList<RegFlow> regA = new SingleLinkedList<RegFlow>();
		final SingleLinkedList<RegFlow> regB = new SingleLinkedList<RegFlow>();
		final SingleLinkedList<RegFlow> regC = new SingleLinkedList<RegFlow>();

		final Instruction[] instructions = rootFunction.getInstructions();
		int pc = 0;

		while (pc < instructions.length)
		{
			switch (instructions[pc].getOpCode())
			{
				case LoadNull:
				{
					final Instruction_iABC curr = (Instruction_iABC) instructions[pc];
					
					break;
				}
				default:
				{
					break;
				}
			}

			pc++; // ensure this is always last line
		}

		return new RegistersFlowTimeline(regA.toArray(new RegFlow[regA.getSize()]), regB.toArray(new RegFlow[regB.getSize()]), regC.toArray(new RegFlow[regC.getSize()]));
	}

	private static class RegFlow
	{

	}

	private static enum RegFlowState
	{
		Null,
		LocalVar,
		GlobalVar;
	}
}
