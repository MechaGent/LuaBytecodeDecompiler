package LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_ForLoop extends AnnoInstruction
{
	private AnnoVar internalLoopVar;	//R(A)
	private AnnoVar limit;			//R(A+1)
	private AnnoVar stepValue;		//R(A+2)
	private final AnnoVar externalLoopVar;	//R(A+3)
	private final int offset;
	
	public AnnoInstruction_ForLoop(int inRaw, int inI, AnnoVar inInternalLoopVar, AnnoVar inLimit, AnnoVar inStepValue, AnnoVar inExternalLoopVar, int inOffset)
	{
		super(inRaw, inI, InstructionTypes.iAsBx, AbstractOpCodes.IterateNumericForLoop);
		this.internalLoopVar = inInternalLoopVar;
		this.limit = inLimit;
		this.stepValue = inStepValue;
		this.externalLoopVar = inExternalLoopVar;
		this.offset = inOffset;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();
		
		final String rA = this.internalLoopVar.getAdjustedName();
		result.add(rA);
		result.add(" += ");
		result.add(this.stepValue.getAdjustedName());
		result.add("; if (");
		result.add(rA);
		result.add(" <?= ");		//TODO figure out how this changes
		result.add(this.limit.getAdjustedName());
		result.add("), then { ");
		result.add(MiscAutobot.programCounter);
		result.add(" += ");
		result.add(Integer.toString(this.offset));
		result.add("; ");
		result.add(this.externalLoopVar.getAdjustedName());
		result.add(" = ");
		result.add(rA);
		result.add("; }");
		
		return result;
	}

	public AnnoVar getInternalLoopVar()
	{
		return this.internalLoopVar;
	}

	public AnnoVar getLimit()
	{
		return this.limit;
	}

	public AnnoVar getStepValue()
	{
		return this.stepValue;
	}

	public AnnoVar getExternalLoopVar()
	{
		return this.externalLoopVar;
	}
	
	public int getOffset()
	{
		return this.offset;
	}

	public boolean isEmpty()
	{
		return this.offset == -1;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		
		//in.addVarRead(this.internalLoopVar, this, place, 0);
		in.addVarRead(this.limit, this, place, 1);
		in.addVarRead(this.stepValue, this, place, 2);
		in.addVarWrite(this.internalLoopVar, this, place, 3);
		in.addVarWrite(this.externalLoopVar, this, place, 4);
	}

	@Override
	public CharList toCharList_InlineForm()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		switch(inSubstep)
		{
			case 0:
			{
				this.internalLoopVar = inReplacement;
				break;
			}
			case 1:
			{
				this.limit = inReplacement;
				break;
			}
			case 2:
			{
				this.stepValue = inReplacement;
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
		throw new UnsupportedOperationException();	//handled elsewhere
	}
}
