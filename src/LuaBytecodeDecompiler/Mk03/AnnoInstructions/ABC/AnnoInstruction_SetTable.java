package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_SetTable extends AnnoInstruction
{
	private AnnoVar targetArray;
	private final AnnoVar targetIndex;
	private AnnoVar source;

	public AnnoInstruction_SetTable(int inRaw, int inI, AnnoVar inTargetArray, AnnoVar inTargetIndex, AnnoVar inSource)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.SetTable);
		this.targetArray = inTargetArray;
		this.targetIndex = inTargetIndex;
		this.source = inSource;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		return this.toPseudoJava(0);
	}

	public AnnoVar getTargetArray()
	{
		return this.targetArray;
	}

	public AnnoVar getTargetIndex()
	{
		return this.targetIndex;
	}

	public AnnoVar getSource()
	{
		return this.source;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		
		in.addVarRead(this.source, this, place, 0);
		in.addVarRead(this.targetArray, this, place, 1);
		in.addVarRead(this.targetIndex, this, place, 2);
		//in.addVarWrite(this.targetArray, this, place, 3);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		return new CharList(this.source.getAdjustedName());
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		if(inSubstep == 0)
		{
			this.source = inReplacement;
		}
		else if(inSubstep == 1)
		{
			if(!inReplacement.isMethodCall())
			{
				//System.out.println("replacing " + this.targetArray.getAdjustedName() + " with " + inReplacement.getAdjustedName());
				this.targetArray = inReplacement;
			}
		}
		else
		{
			throw new IllegalArgumentException("bad index: " + inSubstep);
		}
		
		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		
		//TODO check this
		result.add(this.targetArray.getAdjustedName());
		result.add(".put(");
		result.add(this.targetIndex.getAdjustedName());
		result.add(", ");
		result.add(this.source.getAdjustedName());
		result.add(')');
		
		return result;
	}
}
