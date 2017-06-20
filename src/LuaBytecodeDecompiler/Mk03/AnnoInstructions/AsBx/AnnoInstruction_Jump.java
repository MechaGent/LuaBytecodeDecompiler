package LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_Jump extends AnnoInstruction
{
	private int offset;
	private final String jumpTo;

	public AnnoInstruction_Jump(int inRaw, int inI, int rawOffset, String inJumpToLabel)
	{
		super(inRaw, inI, InstructionTypes.iAsBx, AbstractOpCodes.Jump, true);
		this.offset = rawOffset;
		this.jumpTo = inJumpToLabel;
	}

	/*
	public AnnoInstruction_Jump(int inRaw, int inI, int inOffset)
	{
		super(inRaw, inI, InstructionTypes.iAsBx, AbstractOpCodes.Jump, true);
		this.offset = inOffset;
		this.jumpTo = "";
	}
	*/

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add("jump to: ");
		result.add(this.jumpTo);
		result.add('(');
		
		if(this.offset > 0)
		{
			result.add('+');
		}
		
		result.add(Integer.toString(this.offset));
		result.add(')');

		return result;
	}

	public int getOffset()
	{
		return this.offset;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{

	}

	@Override
	public CharList toCharList_InlineForm()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		return this.toPseudoJava_unhandled(inOffset);
	}
}
