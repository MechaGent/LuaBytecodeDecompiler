package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class PrepLoop_For extends IrInstruction
{
	private IrVar initialIndexValue;
	private IrVar indexVar;
	private IrVar limitVar;

	private PrepLoop_For(StepStage inLineNum, IrVar inIntialIndexValue, IrVar inIndexVar, IrVar inLimitVar)
	{
		super(inLineNum, IrCodes.PrepLoop_For, 2);
		this.initialIndexValue = inIntialIndexValue;
		this.indexVar = inIndexVar;
		this.limitVar = inLimitVar;
	}

	public static PrepLoop_For getInstance(StepStage inLineNum, IrVar inInitialIndexValue, IrVar inIndexVar, IrVar inLimitVar)
	{
		final PrepLoop_For result = new PrepLoop_For(inLineNum, inInitialIndexValue, inIndexVar, inLimitVar);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	@Override
	public void setNext(IrInstruction in, int index)
	{
		switch (index)
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

	public void setNext_EndOfLoop(IrInstruction in)
	{
		super.setNext(in, 1);
	}

	public void setNext_FirstInstructionInLoop(IrInstruction in)
	{
		super.setNext(in, 0);
	}

	public IrInstruction getNext_EndOfLoop()
	{
		return super.getNext(1);
	}

	public IrInstruction getNext_FirstInstructionInLoop()
	{
		return super.getNext(0);
	}

	public IrVar getIntialIndexValue()
	{
		return this.initialIndexValue;
	}

	public IrVar getIndexVar()
	{
		return this.indexVar;
	}

	public IrVar getLimitVar()
	{
		return this.limitVar;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();

		result.add("initial index value: ");
		result.add(this.initialIndexValue.toWordyVersion(), true);
		result.add(", indexVar: ");
		result.add(this.indexVar.toWordyVersion(), true);
		result.add(", limitVar: ");
		result.add(this.limitVar.toWordyVersion(), true);

		return result;
	}

	@Override
	protected CharList toCharList_internal_getNaturalLanguageVersion()
	{
		final CharList result = new CharList();

		return result;
	}

	public CharList getHeader()
	{
		final CharList result = new CharList();

		result.add(this.indexVar.valueToString());
		result.add(" = ");
		result.add(this.initialIndexValue.valueToString());
		result.add("; ");
		result.add(this.indexVar.valueToString());
		result.add(" < ");
		result.add(this.limitVar.valueToString());
		result.add("; ");
		// final IrInstruction jumpLabel = this.getNext_EndOfLoop();
		// System.out.println(jumpLabel.getDirectPrev().getDirectPrev().getOpCode().toString());
		final String stepVar = ((EvalLoop_For) this.getNext_EndOfLoop().getDirectPrev()).getStepVar().valueToString();

		result.add(this.indexVar.valueToString());
		result.add(" += ");
		result.add(stepVar);

		return result;
	}

	@Override
	public void registerAllReadsAndWrites(VarCleaner in)
	{
		int currentStageNum = 2;
		final int lineNum = this.linePosData.getLineNum();
		final int subLine = this.linePosData.getSubLineNum();

		in.addVarRead(this.initialIndexValue, this, new StepStage(lineNum, subLine, currentStageNum++, "reading initialIndexValueVar"));
		in.addVarRead(this.limitVar, this, new StepStage(lineNum, subLine, currentStageNum++, "reading limitVar"));
		in.addVarWrite(this.indexVar, this, new StepStage(lineNum, subLine, currentStageNum++, "writing indexVar"));
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int swap = inSwapOutStep.getStepNum();

		switch (swap)
		{
			case 2:
			{
				this.initialIndexValue = inSwapIn;
				break;
			}
			case 3:
			{
				this.limitVar = inSwapIn;
				break;
			}
			case 4:
			{
				if (StepStage.allowWriteSwaps())
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

		if (test != this.indexVar)
		{
			this.indexVar = test;
		}

		test = in.checkVarForReassign(this.limitVar);

		if (test != this.limitVar)
		{
			this.limitVar = test;
		}
		*/
		
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		in.registerNewRead(this.initialIndexValue, this, new StepStage(line, subline, count++, "reading initialIndexValue"));
		in.registerNewWrite(this.indexVar, this.initialIndexValue, this, new StepStage(line, subline, count++, "writing indexValue"));
		in.registerNewRead(this.limitVar, this, new StepStage(line, subline, count++, "reading limitVar"));
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		switch(inVarIndex)
		{
			case 2:
			{
				this.initialIndexValue = inIn;
				break;
			}
			case 3:
			{
				this.indexVar = inIn;
				break;
			}
			case 4:
			{
				this.limitVar = inIn;
				break;
			}
			default:
			{
				throw new IndexOutOfBoundsException();
			}
		}
	}
}
