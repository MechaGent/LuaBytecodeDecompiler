package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class EvalLoop_For extends IrInstruction
{
	private IrVar indexVar;
	private IrVar limitVar;
	private IrVar stepVar;
	
	private EvalLoop_For(StepStage inLineNum, IrVar inIndexVar, IrVar inLimitVar, IrVar inStepVar)
	{
		super(inLineNum, IrCodes.EvalLoop_For, 2);
		this.indexVar = inIndexVar;
		this.limitVar = inLimitVar;
		this.stepVar = inStepVar;
	}
	
	public static EvalLoop_For getInstance(StepStage inLineNum, IrVar inIndexVar, IrVar inLimitVar, IrVar inStepVar)
	{
		final EvalLoop_For result = new EvalLoop_For(inLineNum, inIndexVar, inLimitVar, inStepVar);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}
	
	@Override
	public void setNext(IrInstruction in, int index)
	{
		switch(index)
		{
			case 0:
			{
				super.setNext(in, 0);
				break;
			}
			case 1:
			{
				super.setNext(in, 0);
				break;
			}
			default:
			{
				throw new IllegalArgumentException();
			}
		}
	}
	
	@Override
	public IrInstruction getNext()
	{
		throw new UnsupportedOperationException();
	}
	
	public void setNext_StartOfLoop(IrInstruction in)
	{
		super.setNext(in, 0);
	}
	
	public void setNext_FirstInstructionAfterLoop(IrInstruction in)
	{
		super.setNext(in, 1);
	}
	
	public IrInstruction getNext_StartOfLoop()
	{
		return IrInstruction.LabelsMap.get(0);
	}
	
	public IrInstruction getNext_FirstInstructionAfterLoop()
	{
		return IrInstruction.LabelsMap.get(1);
	}

	public IrVar getIndexVar()
	{
		return this.indexVar;
	}

	public IrVar getLimitVar()
	{
		return this.limitVar;
	}

	public IrVar getStepVar()
	{
		return this.stepVar;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("indexVar: ");
		result.add(this.indexVar.toWordyVersion(), true);
		result.add(", limitVar: ");
		result.add(this.limitVar.toWordyVersion(), true);
		result.add(", stepVar: ");
		result.add(this.stepVar.toWordyVersion(), true);
		
		return result;
	}

	@Override
	protected CharList toCharList_internal_getNaturalLanguageVersion()
	{
		final CharList result = new CharList();

		return result;
	}

	@Override
	public void registerAllReadsAndWrites(VarCleaner in)
	{
		int currentStageNum = 2;
		final int lineNum = this.linePosData.getLineNum();
		final int subLine = this.linePosData.getSubLineNum();
		
		in.addVarRead(this.indexVar, this, new StepStage(lineNum, subLine, currentStageNum++, "reading indexVar"));
		in.addVarRead(this.limitVar, this, new StepStage(lineNum, subLine, currentStageNum++, "reading limitVar"));
		in.addVarRead(this.stepVar, this, new StepStage(lineNum, subLine, currentStageNum++, "reading stepVar"));
		in.addVarWrite(this.indexVar, this, new StepStage(lineNum, subLine, currentStageNum++, "writing indexVar"));
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int swap = inSwapOutStep.getStepNum();
		
		switch(swap)
		{
			case 2:
			{
				this.indexVar = inSwapIn;
				break;
			}
			case 3:
			{
				this.limitVar = inSwapIn;
				break;
			}
			case 4:
			{
				this.stepVar = inSwapIn;
				break;
			}
			case 5:
			{
				if(StepStage.allowWriteSwaps())
				{
					this.indexVar = inSwapIn;
					break;
				}
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}
	}

	@Override
	public void evalSwapStatus(VarAssignMap in)
	{
		/*
		IrVar test = in.checkVarForReassign(this.indexVar);
		
		if(test != this.indexVar)
		{
			this.indexVar = test;
		}
		
		test = in.checkVarForReassign(this.limitVar);
		
		if(test != this.limitVar)
		{
			this.limitVar = test;
		}
		
		test = in.checkVarForReassign(this.stepVar);
		
		if(test != this.stepVar)
		{
			this.stepVar = test;
		}
		*/
		
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		in.registerNewRead(this.indexVar, this, new StepStage(line, subline, count++, "reading indexVar"));
		in.registerNewRead(this.limitVar, this, new StepStage(line, subline, count++, "reading limitVar"));
		in.registerNewRead(this.stepVar, this, new StepStage(line, subline, count++, "reading stepVar"));
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		switch(inVarIndex)
		{
			case 2:
			{
				this.indexVar = inIn;
				break;
			}
			case 3:
			{
				this.limitVar = inIn;
				break;
			}
			case 4:
			{
				this.stepVar = inIn;
				break;
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}
	}
}
