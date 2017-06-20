package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Ref;

public class Return_Stuff extends IrInstruction
{
	private final IrVar[] sourceTable;
	private final boolean isVarReturns;
	
	private Return_Stuff(StepStage inLineNum, IrVar[] inSourceTable, boolean inIsVarReturns)
	{
		super(inLineNum, IrCodes.Return_Stuff, 0);
		this.sourceTable = inSourceTable;
		this.isVarReturns = inIsVarReturns;
	}
	
	public static Return_Stuff getInstance(StepStage inLineNum, IrVar[] inSourceTable, boolean inIsVarReturns)
	{
		final Return_Stuff result = new Return_Stuff(inLineNum, inSourceTable, inIsVarReturns);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}
	
	public IrVar getStartRegister()
	{
		return this.sourceTable[0];
	}

	public IrVar[] getSourceTable()
	{
		return this.sourceTable;
	}

	public int getNumReturns()
	{
		return this.isVarReturns? -1 : this.sourceTable.length;
	}
	
	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add(", numReturns: ");
		result.addAsString(this.getNumReturns());
		
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
		if(this.getNumReturns() == 0)
		{
			//varArgs
		}
		else
		{
			int currentStageNum = 2;
			final int lineNum = this.linePosData.getLineNum();
			final int subLine = this.linePosData.getSubLineNum();

			for(int i = 0; i < this.sourceTable.length; i++)
			{
				in.addVarWrite(this.sourceTable[i], this, new StepStage(lineNum, subLine, currentStageNum++, "writing to source table"));
			}
		}
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int step = inSwapOutStep.getStepNum() - 2;
		
		if(this.getNumReturns() != 0 && StepStage.allowWriteSwaps() && step < this.sourceTable.length)
		{
			this.sourceTable[step] = inSwapIn;
		}
	}

	@Override
	public void evalSwapStatus(VarAssignMap in)
	{
		/*
		for(int i = 0; i < sourceTable.length; i++)
		{
			final IrVar test = in.checkVarForReassign(this.sourceTable[i]);
			
			if(test != this.sourceTable[i])
			{
				this.sourceTable[i] = test;
			}
		}
		*/
		
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		for(int i = 0; i < sourceTable.length; i++)
		{
			in.registerNewRead(this.sourceTable[i], this, new StepStage(line, subline, count++, "reading from table"));
		}
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		this.sourceTable[inVarIndex - 2] = inIn;
	}
}
