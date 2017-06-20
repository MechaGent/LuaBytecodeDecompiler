package LuaBytecodeDecompiler.Mk03.AnnoInstructions.AsBx;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk02.MiscAutobot;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler.VarTouchy;
import LuaBytecodeDecompiler.Mk03.AnnoVars.AnnoVar;

public class AnnoInstruction_ForPrep extends AnnoInstruction
{
	private AnnoVar internalLoopVar;		//R(A)
	private AnnoVar stepValue;			//R(A+2)
	private AnnoVar externalLoopVar;	//R(A+3)
	private final int jumpOffset;				//sBx
	
	public AnnoInstruction_ForPrep(int inRaw, int inI, AnnoVar inInternalLoopVar, AnnoVar inStepValue, AnnoVar inExternalLoopVar, int inJumpOffset)
	{
		super(inRaw, inI, InstructionTypes.iAsBx, AbstractOpCodes.InitNumericForLoop);
		this.internalLoopVar = inInternalLoopVar;
		this.stepValue = inStepValue;
		this.externalLoopVar = inExternalLoopVar;
		this.jumpOffset = inJumpOffset;
	}

	@Override
	protected CharList toCharList_internal(boolean inIsVerbose)
	{
		final CharList result = new CharList();
		
		result.add(this.externalLoopVar.getAdjustedName());
		result.add(" = ");
		result.add(this.internalLoopVar.getAdjustedName());
		result.add("; ");
		result.add(this.internalLoopVar.getAdjustedName());
		result.add(" -= ");
		result.add(this.stepValue.getAdjustedName());
		result.add("; ");
		result.add(MiscAutobot.programCounter);
		result.add(" += ");
		result.add(Integer.toString(this.jumpOffset));
		
		return result;
	}

	public AnnoVar getInternalLoopVar()
	{
		return this.internalLoopVar;
	}

	public AnnoVar getStepValue()
	{
		return this.stepValue;
	}

	public int getJumpOffset()
	{
		return this.jumpOffset;
	}

	@Override
	public void addAllReadsAndWrites(VarFlowHandler in)
	{
		final int place = this.getInstructionAddress();
		
		in.addVarRead(this.stepValue, this, place, 0);
		in.addVarWrite(this.externalLoopVar, this, place, 1);
		//in.addVarRead(this.internalLoopVar, this, place, 2);
		in.addVarWrite(this.internalLoopVar, this, place, 3);
	}
	
	public CharList toCharList_InlineForm()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public CharList toCharList_InlineForm(VarTouchy target)
	{
		final CharList result = new CharList();
		
		switch(target.getSubline())
		{
			case 1:
			{
				result.add(this.internalLoopVar.getAdjustedName());
				break;
			}
			default:
			{
				throw new IllegalArgumentException("bad index: " + target.getSubline());
			}
		}
		
		return result;
	}

	@Override
	public AnnoInstruction replaceStep(int inSubstep, AnnoVar inReplacement)
	{
		switch(inSubstep)
		{
			case 0:
			{
				this.stepValue = inReplacement;
				break;
			}
			case 1:
			{
				this.externalLoopVar = inReplacement;
				break;
			}
			case 2:
			{
				this.internalLoopVar = inReplacement;
				break;
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
