package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar_Function;

public class AnnoInstruction_TailCall extends AnnoInstruction
{
	private AnnoVar_Function function;
	private final AnnoVar[] basket;

	public AnnoInstruction_TailCall(int inRaw, int inI, AnnoVar_Function inFunction, AnnoVar[] inBasket)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.TailCall);
		this.function = inFunction;
		this.basket = inBasket;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add("TailCalling function ");
		result.add(this.function.getAdjustedName());
		result.add('(');

		if (this.basket.length > 0)
		{
			result.add(this.basket[0].getAdjustedName());

			for (int i = 1; i < this.basket.length; i++)
			{
				result.add(", ");
				result.add(this.basket[i].getAdjustedName());
			}
		}

		result.add(')');

		return result;
	}

	public AnnoVar_Function getFunction()
	{
		return this.function;
	}

	public AnnoVar[] getBasket()
	{
		return this.basket;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		int i = 0;
		in.addVarRead(this.function, this, place, i++);

		for (; i < this.basket.length + 1; i++)
		{
			in.addVarRead(this.basket[i - 1], this, place, i);
		}
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();

		result.add("TailCall:function:");
		result.add(this.function.getAdjustedName());
		result.add('(');

		if (this.basket.length > 0)
		{
			result.add(this.basket[0].getAdjustedName());

			for (int i = 1; i < this.basket.length; i++)
			{
				result.add(", ");
				result.add(this.basket[i].getAdjustedName());
			}
		}

		result.add(')');

		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		if (inSubstep == 0)
		{
			this.function = (AnnoVar_Function) inReplacement;
		}
		else
		{
			this.basket[inSubstep - 1] = inReplacement;
		}

		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		return this.toPseudoJava_unhandled(inOffset);
	}
}
