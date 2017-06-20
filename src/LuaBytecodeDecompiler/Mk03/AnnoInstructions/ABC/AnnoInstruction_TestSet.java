package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.BoolBrancher;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_TestSet extends AnnoInstruction implements BoolBrancher
{
	private final AnnoVar target;
	private AnnoVar source;
	private final boolean C;

	public AnnoInstruction_TestSet(int inRaw, int inI, AnnoVar inTarget, AnnoVar inSource, boolean inC)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.TestAndSaveLogicalComparison);
		this.target = inTarget;
		this.source = inSource;
		this.C = inC;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add("if (");

		if (!this.C)
		{
			result.add('!');
		}

		result.add(MiscAutobot.GlobalMethods_toBool);
		final String sourceString = this.source.getAdjustedName();
		result.add(sourceString);
		result.add(")), then ");
		result.add(this.target.getAdjustedName());
		result.add(" = ");
		result.add(sourceString);
		result.add(", else ");
		result.add(MiscAutobot.programCounter);
		result.add("++");

		return result;
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public AnnoVar getSource()
	{
		return this.source;
	}

	public boolean getC()
	{
		return this.C;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();

		in.addVarRead(this.source, this, place, 0);
		in.addVarWrite(this.target, this, place, 1);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		if (inSubstep == 0)
		{
			this.source = inReplacement;
		}
		else
		{
			throw new IllegalArgumentException("bad index: " + inSubstep);
		}

		return this;
	}

	/**
	 * more will have to be done, besides just calling this method, because of the way the setting behavior is distributed
	 */
	public CharList toBooleanStatement(boolean invertStatement)
	{
		final CharList result = new CharList();

		//result.add("if (");

		/*
		 * this is equivalent to:
		 * 
		 * boolean temp;
		 * 
		 * if(invertStatement)
		 * {
		 * 		temp = !this.C;
		 * }
		 * else
		 * {
		 * 		temp = this.C;
		 * }
		 * 
		 * if(!temp)
		 * {
		 * 		result.add('!');
		 * }
		 */
		if (invertStatement ^ !this.C)
		{
			result.add('!');
		}

		result.add(MiscAutobot.GlobalMethods_toBool);
		final String sourceString = this.source.getAdjustedName();
		result.add(sourceString);
		result.add(')');

		return result;
	}
	
	@Override
	public CharList toPseudoJava(int inOffset)
	{
		throw new UnsupportedOperationException();	//handled elsewhere
	}
}
