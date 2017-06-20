package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_Close extends AnnoInstruction
{
	private final AnnoVar[] closers;

	public AnnoInstruction_Close(int inRaw, int inI, AnnoVar[] inCloseTargets)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.Close);
		this.closers = inCloseTargets;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();
		
		result.add(MiscAutobot.GlobalMethods_close);
		result.add(this.closers[0].getAdjustedName());
		
		for(int i = 0; i < this.closers.length; i++)
		{
			result.add(", ");
			result.add(this.closers[i].getAdjustedName());
		}
		result.add(')');
		
		return result;
	}

	public AnnoVar[] getClosers()
	{
		return this.closers;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		
		for(int i = 0; i < this.closers.length; i++)
		{
			in.addVarRead(this.closers[i], this, place, i);
		}
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
