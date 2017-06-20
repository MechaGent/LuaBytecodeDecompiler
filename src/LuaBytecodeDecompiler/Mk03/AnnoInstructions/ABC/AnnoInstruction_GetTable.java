package LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_GetTable extends AnnoInstruction
{
	private final AnnoVar target;
	private AnnoVar sourceTable;
	private AnnoVar sourceIndex;

	public AnnoInstruction_GetTable(int inRaw, int inI, AnnoVar inTarget, AnnoVar inSourceTable, AnnoVar inSourceIndex)
	{
		super(inRaw, inI, InstructionTypes.iABC, AbstractOpCodes.GetTable);
		this.target = inTarget;
		this.sourceTable = inSourceTable;
		this.sourceIndex = inSourceIndex;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();

		result.add(this.target.getAdjustedName());
		result.add(" = ");
		MiscAutobot.prettify_TableGetMethod(result, this.sourceTable.getAdjustedName(), this.sourceIndex.getAdjustedName());
		
		return result;
	}

	public AnnoVar getTarget()
	{
		return this.target;
	}

	public AnnoVar getSourceTable()
	{
		return this.sourceTable;
	}

	public AnnoVar getSourceIndex()
	{
		return this.sourceIndex;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		in.addVarRead(this.sourceTable, this, place, 0);
		in.addVarRead(this.sourceIndex, this, place, 1);
		in.addVarWrite(this.target, this, place, 2);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		final CharList result = new CharList();
		MiscAutobot.prettify_TableGetMethod(result, this.sourceTable.getAdjustedName(), this.sourceIndex.getAdjustedName());
		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		switch(inSubstep)
		{
			case 0:
			{
				this.sourceTable = inReplacement;
				break;
			}
			case 1:
			{
				this.sourceIndex = inReplacement;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("bad index: " + inSubstep);
			}
		}
		
		return this;
	}

	@Override
	public CharList toPseudoJava(int inOffset)
	{
		final CharList result = new CharList();

		result.add('\t', inOffset);
		result.add(this.target.getAdjustedName());
		result.add(" = ");
		MiscAutobot.prettify_TableGetMethod(result, this.sourceTable.getAdjustedName(), this.sourceIndex.getAdjustedName());
		
		return result;
	}
}
