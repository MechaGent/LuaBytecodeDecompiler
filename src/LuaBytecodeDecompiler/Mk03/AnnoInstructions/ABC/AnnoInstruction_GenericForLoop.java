package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Function;

public class AnnoInstruction_GenericForLoop extends AnnoInstruction
{
	final AnnoVar_Function iteratorFunction;	//r(A)
	final AnnoVar state;						//r(A+1)
	final AnnoVar enumIndex;					//r(A+2)
	final AnnoVar[] internalLoopVars;			//r(A + 3),..., r(A + C + 2)
	
	public AnnoInstruction_GenericForLoop(int inRaw, int inI, AnnoVar_Function inIteratorFunction, AnnoVar inState, AnnoVar inEnumIndex, AnnoVar[] inInternalLoopVars)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.IterateGenericForLoop);
		this.iteratorFunction = inIteratorFunction;
		this.state = inState;
		this.enumIndex = inEnumIndex;
		this.internalLoopVars = inInternalLoopVars;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();
		
		final String firstInternal = this.internalLoopVars[0].getAdjustedName();
		result.add(firstInternal);
		
		for(int i = 1; i < this.internalLoopVars.length; i++)
		{
			result.add(", ");
			result.add(this.internalLoopVars[i].getAdjustedName());
		}
		
		result.add(" = ");
		result.add(this.iteratorFunction.getAdjustedName());
		result.add('(');
		result.add(this.state.getAdjustedName());
		result.add(", ");
		final String rA2 = this.enumIndex.getAdjustedName();
		result.add(rA2);
		result.add("); if (");
		result.add(firstInternal);
		result.add(" != null), then ");
		result.add(rA2);
		result.add(" = ");
		result.add(firstInternal);
		result.add(", else ");
		result.add(MiscAutobot.programCounter);
		result.add("++");
		
		return result;
	}

	public AnnoVar_Function getIteratorFunction()
	{
		return this.iteratorFunction;
	}

	public AnnoVar getState()
	{
		return this.state;
	}

	public AnnoVar getEnumIndex()
	{
		return this.enumIndex;
	}

	public AnnoVar[] getInternalLoopVars()
	{
		return this.internalLoopVars;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		
		in.addVarRead(this.iteratorFunction, this, place, 0);
		in.addVarRead(this.state, this, place, 1);
		in.addVarRead(this.enumIndex, this, place, 2);
		
		for(int i = 0; i < this.internalLoopVars.length; i++)
		{
			in.addVarWrite(this.internalLoopVars[i], this, place, i + 3);
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
