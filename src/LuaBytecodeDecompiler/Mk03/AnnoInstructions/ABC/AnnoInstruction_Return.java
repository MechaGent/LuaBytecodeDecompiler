package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_Return extends AnnoInstruction
{
	final AnnoVar[] basket;

	public AnnoInstruction_Return(int inRaw, int inI, AnnoVar[] inBasket)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.Return);
		this.basket = inBasket;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add("Returning");

		if (this.basket.length > 0)
		{
			result.add(" with a basket containing: ");
			result.add(this.basket[0].getAdjustedName());

			for (int i = 1; i < this.basket.length; i++)
			{
				result.add(", ");
				result.add(this.basket[i].getAdjustedName());
			}
		}

		return result;
	}

	public AnnoVar[] getBasket()
	{
		return this.basket;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();

		for (int i = 0; i < this.basket.length; i++)
		{
			in.addVarRead(this.basket[i], this, place, i);
		}
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();

		result.add('{');

		if (this.basket.length > 0)
		{
			result.add(this.basket[0].getAdjustedName());

			for (int i = 1; i < this.basket.length; i++)
			{
				result.add(", ");
				result.add(this.basket[i].getAdjustedName());
			}
		}

		result.add('}');
		
		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		this.basket[inSubstep] = inReplacement;
		
		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		return this.toPseudoJava_unhandled(inOffset);
	}
}
