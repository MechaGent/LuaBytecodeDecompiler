package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class SetVar extends IrInstruction
{
	private IrVar target;
	private IrVar source;
	
	protected SetVar(StepStage inLineNum, IrVar inTarget, IrVar inSource)
	{
		super(inLineNum, IrCodes.SetVar, 1);
		this.target = inTarget;
		this.source = inSource;
	}
	
	public static SetVar getInstance(StepStage inLineNum, IrVar inTarget, IrVar inSource)
	{
		if(inTarget == null)
		{
			throw new NullPointerException("line: " + inLineNum + ", target: " + inTarget + ", inSource: " + inSource);
		}
		
		if(inSource == null)
		{
			throw new NullPointerException("line: " + inLineNum + ", target: " + inTarget + ", inSource: " + inSource);
		}
		
		final SetVar result = new SetVar(inLineNum, inTarget, inSource);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	public IrVar getTarget()
	{
		return this.target;
	}

	public IrVar getSource()
	{
		return this.source;
	}
	
	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("target: ");
		result.add(this.target.toWordyVersion(), true);
		result.add(", source: ");
		result.add(this.source.toWordyVersion(), true);
		
		return result;
	}

	@Override
	protected CharList toCharList_internal_getNaturalLanguageVersion()
	{
		final CharList result = new CharList();

		return result;
	}
	
	public CharList toCode()
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
		
		in.addVarRead(this.source, this, new StepStage(lineNum, subLine, currentStageNum++, "reading source"));
		in.addVarWrite(this.target, this, new StepStage(lineNum, subLine, currentStageNum++, "writing to target"), this.source);
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		final int step = inSwapOutStep.getStepNum();
		
		switch(step)
		{
			case 2:
			{
				this.source = inSwapIn;
				break;
			}
			case 3:
			{
				if(StepStage.allowWriteSwaps())
				{
					this.target = inSwapIn;
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
		final IrVar testSource = in.checkVarForReassign(this.source);
		
		//System.out.println(this.getMangledName() + ": old source: " + this.source.getLabel() + ", new source: " + testSource.getLabel());
		
		if(testSource != this.source)
		{
			
			this.source = testSource;
		}

		final IrVar testTarget = in.checkWriteTargetVarForPartialReassign(this.target);
		
		//System.out.println(this.getMangledName() + ": old target: " + this.target.getLabel() + ", new target: " + testTarget.getLabel());
		
		if(testTarget != this.target)
		{
			
			this.target = testTarget;
		}
		
		//final boolean compare = this.source == in.checkVarForReassign(this.target);
		//System.out.println(this.getMangledName() + ": comparing: " + testTarget.getLabel() + "(" + testTarget.valueToString() + ")(t) with: " + testSource.getLabel() + "(" + testSource.valueToString() + ")(s) and got: " + compare);
		
		if(this.source == in.checkVarForReassign(this.target))
		{
			//this instruction is redundant
			this.setExpiredState(true);
		}
		else
		{
			in.registerNewWrite(this.target, this.source, this);
		}
		*/
		
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		in.registerNewRead(this.source, this, new StepStage(line, subline, count++, "reading source"));
		in.registerNewWrite(this.target, this.source, this, new StepStage(line, subline, count++, "writing to target"));
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		if(inVarIndex == 2)
		{
			this.source = inIn;
		}
		else if(inVarIndex == 3)
		{
			this.target = inIn;
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}
}
