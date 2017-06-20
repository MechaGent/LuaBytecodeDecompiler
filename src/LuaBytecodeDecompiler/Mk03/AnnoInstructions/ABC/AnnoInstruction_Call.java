package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Function;

public class AnnoInstruction_Call extends AnnoInstruction
{
	/**
	 * locals in the current function, which will store the returned values (if any)
	 */
	protected final AnnoVar[] targets;

	/**
	 * function being called
	 */
	protected AnnoVar function;

	/**
	 * parameters passed to the function being called
	 */
	protected final AnnoVar[] parameters;

	public AnnoInstruction_Call(int inRaw, int inI, AnnoVar[] inTargets, AnnoVar inFunction, AnnoVar[] inParameters)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.Call);
		this.targets = inTargets;
		this.function = inFunction;
		this.parameters = inParameters;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add("Function:");
		result.add(this.function.getAdjustedName());
		result.add('(');

		if (this.parameters.length > 0)
		{
			result.add(this.parameters[0].getAdjustedName());

			for (int i = 1; i < this.parameters.length; i++)
			{
				result.add(", ");
				result.add(this.parameters[i].getAdjustedName());
			}
		}

		result.add(") is called, and returns; ");
		if (this.function instanceof AnnoVar_Function)
		{
			final AnnoVar_Function castFunction = (AnnoVar_Function) this.function;

			if (this.targets.length > 0)
			{
				final AnnoVar[] payload = castFunction.getReturns(this.targets.length);
				result.add(this.targets[0].getAdjustedName());
				result.add(" = returnValues:");
				result.add(payload[0].getAdjustedName());

				for (int i = 1; i < this.targets.length; i++)
				{
					result.add(", ");
					result.add(this.targets[i].getAdjustedName());
					result.add(" = returnValues:");
					result.add(payload[i].getAdjustedName());
				}
			}
		}
		else
		{
			result.add("CALL <could not resolve function>");
		}

		return result;
	}

	public AnnoVar[] getTargets()
	{
		return this.targets;
	}

	public AnnoVar getFunction()
	{
		return this.function;
	}

	public AnnoVar[] getParameters()
	{
		return this.parameters;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int instructNum = this.getInstructionAddress();
		int i = 1;
		in.addVarRead(this.function, this, instructNum, 0);

		for (; i < this.parameters.length + 1; i++)
		{
			in.addVarRead(this.parameters[i - 1], this, instructNum, i);
		}

		for (int j = 0; j < this.targets.length; j++)
		{
			if (this.targets[j] != null)
			{
				in.addVarWrite(this.targets[j], this, instructNum, i++);
			}
			else
			{
				throw new IllegalArgumentException("bad result on index: " + j + " vs length: " + this.targets.length);
			}
		}
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		/*
		 * anticipating that only things being replaced are parameters
		 */

		if (inSubstep == 0)
		{
			this.function = inReplacement;
		}
		else if (inSubstep > 0 && (inSubstep - 1) < this.parameters.length)
		{
			this.parameters[inSubstep - 1] = inReplacement;
		}

		return this;
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();

		result.add("Function:");
		result.add(this.function.getAdjustedName());
		result.add('(');

		if (this.parameters.length > 0)
		{
			result.add(this.parameters[0].getAdjustedName());

			for (int i = 1; i < this.parameters.length; i++)
			{
				result.add(", ");
				result.add(this.parameters[i].getAdjustedName());
			}
		}

		result.add(')');

		return result;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		result.add("calling Function:");
		result.add(this.function.getAdjustedName());
		result.add('(');

		if (this.parameters.length > 0)
		{
			result.add(this.parameters[0].getAdjustedName());

			for (int i = 1; i < this.parameters.length; i++)
			{
				result.add(", ");
				result.add(this.parameters[i].getAdjustedName());
			}
		}

		result.add(')');

		return result;
	}
}
