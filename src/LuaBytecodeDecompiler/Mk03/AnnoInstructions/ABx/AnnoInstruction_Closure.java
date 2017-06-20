package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABx;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Function;

public class AnnoInstruction_Closure extends AnnoInstruction
{
	private final AnnoVar target;
	private AnnoVar_Function source;
	private final AnnoVar[] args; // does not include target

	public AnnoInstruction_Closure(int inRaw, int inI, AnnoVar inTarget, AnnoVar_Function inSource, AnnoVar[] inArgs)
	{
		super(inRaw, inI, InstructionTypes.iABx, AbstractOpCodes.Closure);
		this.target = inTarget;
		this.source = inSource;
		this.args = inArgs;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		final String targetString = this.target.getAdjustedName();
		result.add(targetString);
		result.add(" = ");
		result.add(MiscAutobot.GlobalMethods_createClosure);
		result.add(this.source.getAdjustedName());
		result.add(", ");
		result.add(targetString);

		for (int i = 0; i < this.args.length; i++)
		{
			result.add(", ");
			result.add(this.args[i].getAdjustedName());
		}

		result.add(')');

		return result;
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public AnnoVar_Function getSource()
	{
		return this.source;
	}

	public AnnoVar[] getArgs()
	{
		return this.args;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();

		in.addVarRead(this.source, this, place, 0);
		int i = 0;

		for (; i < this.args.length; i++)
		{
			in.addVarRead(this.args[i], this, place, i + 1);
		}

		in.addVarWrite(this.target, this, place, i + 1);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();

		result.add(MiscAutobot.GlobalMethods_createClosure);
		result.add(this.source.getAdjustedName());
		result.add(", ");
		result.add(this.target.getAdjustedName());

		for (int i = 0; i < this.args.length; i++)
		{
			result.add(", ");
			result.add(this.args[i].getAdjustedName());
		}

		result.add(')');

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		if (inSubstep == 0)
		{
			this.source = (AnnoVar_Function) inReplacement;
		}
		else
		{
			this.args[inSubstep - 1] = inReplacement;
		}

		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		return this.toPseudoJava_unhandled(inOffset);
	}
}
