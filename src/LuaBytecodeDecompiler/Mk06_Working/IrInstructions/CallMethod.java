package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Ref;

public class CallMethod extends IrInstruction
{
	private IrVar start;
	private final IrVar[] Args;
	private final boolean isVarArgs;
	private final IrVar[] Returns;
	private final boolean isVarReturns;

	public CallMethod(StepStage inLinePosData, IrVar inStart, IrVar[] inArgs, boolean inIsVarArgs, IrVar[] inReturns, boolean inIsVarReturns)
	{
		super(inLinePosData, IrCodes.CallMethod, 1);
		this.start = inStart;
		this.Args = inArgs;
		this.isVarArgs = inIsVarArgs;
		this.Returns = inReturns;
		this.isVarReturns = inIsVarReturns;

		/*
		for (int i = 0; i < inArgs.length; i++)
		{
			if (inArgs[i] == null)
			{
				throw new NullPointerException(i + " of " + inArgs.length);
			}
		}
		
		for (int i = 0; i < inReturns.length; i++)
		{
			if (inReturns[i] == null)
			{
				throw new NullPointerException(i + " of " + inReturns.length);
			}
		}
		*/
	}

	public static CallMethod getInstance(int inLineNum, int inSubLine, IrVar inStart, IrVar[] inArgs, boolean inIsVarArgs, IrVar[] inReturns, boolean inIsVarReturns)
	{
		return getInstance(new StepStage(inLineNum, inSubLine, 0, "instruction"), inStart, inArgs, inIsVarArgs, inReturns, inIsVarReturns);
	}

	public static CallMethod getInstance(StepStage inLinePosData, IrVar inStart, IrVar[] inArgs, boolean inIsVarArgs, IrVar[] inReturns, boolean inIsVarReturns)
	{
		final CallMethod result = new CallMethod(inLinePosData, inStart, inArgs, inIsVarArgs, inReturns, inIsVarReturns);

		IrInstruction.LabelsMap.put(result.mangledName, result);

		// result.logAllReadsAndWrites();

		return result;
	}

	public IrVar getStart()
	{
		return this.start;
	}

	public IrVar[] getArgs()
	{
		return this.Args;
	}

	public boolean isVarArgs()
	{
		return this.isVarArgs;
	}

	public IrVar[] getReturns()
	{
		return this.Returns;
	}

	public boolean isVarReturns()
	{
		return this.isVarReturns;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();

		result.add("startRegister: ");
		result.add(this.start.valueToString());
		result.add(" numArgs: ");

		if (this.isVarArgs)
		{
			result.addAsString(this.Args.length);
		}
		else
		{
			result.add("variable");
		}

		result.add(" numReturns: ");

		if (this.isVarReturns)
		{
			result.addAsString(this.Returns.length);
		}
		else
		{
			result.add("variable");
		}

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
		/*
		 * TODO
		final int lineNum = this.linePosData.getLineNum();
		final int subLineNum = this.linePosData.getSubLineNum();
		int stageNum = 2;
		in.addVarRead(this.start, this, new StepStage(lineNum, subLineNum, stageNum++, "read function prototype"));
		
		final int paramOffset = this.startRegister + 1;
		final int paramLimit;
		
		if(this.numArgs == -1)
		{
			paramLimit = this.Args.length;
		}
		else
		{
			paramLimit = this.numArgs + paramOffset;
		}
		
		for(int i = paramOffset; i < paramLimit; i++)
		{
			in.addVarRead(this.Args[i], this, new StepStage(lineNum, subLineNum, stageNum++, "reading paramRegister_" + (i - paramOffset)));
		}
		
		final int returnsOffset = this.startRegister + 1;
		final int returnsLimit;
		
		if(this.numReturns == -1)
		{
			returnsLimit = this.Args.length;
		}
		else
		{
			returnsLimit = this.numReturns + returnsOffset;
		}
		
		for(int i = returnsOffset; i < returnsLimit; i++)
		{
			in.addVarWrite(this.Args[i], this, new StepStage(lineNum, subLineNum, stageNum++, "writing returnRegister_" + (i - returnsOffset)), this.Args[i]);
		}
		
		*/
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		/*
		 * TODO
		final int stageNum = inSwapOutStep.getStepNum();
		
		if(stageNum == 2)
		{
			this.Args[0].logWriteToAtTime(inSwapOutStep, inSwapIn);
			//this.registers[0] = (IrVar) inSwapIn;
		}
		else if(stageNum <= 2 + this.numArgs)
		{
			//this.registers[stageNum - 2] = (IrVar) inSwapIn;
			this.Args[stageNum - 2].logWriteToAtTime(inSwapOutStep, inSwapIn);
		}
		else
		{
			throw new IndexOutOfBoundsException("bad step: " + inSwapOutStep);
		}
		*/
	}

	@Override
	public void evalSwapStatus(VarAssignMap in)
	{
		/*
		final IrVar tempStart = in.checkVarForReassign(this.start);

		if (tempStart != this.start)
		{
			this.start = tempStart;
		}

		for (int i = 0; i < this.Args.length; i++)
		{
			final IrVar temp = in.checkVarForReassign(this.Args[i]);

			if (temp != this.Args[i])
			{
				this.Args[i] = (IrVar) temp;
			}
		}
		*/
		
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		in.registerNewRead(this.start, this, new StepStage(line, subline, count++, "reading sourceFunction"));
		
		for (int i = 0; i < this.Args.length; i++)
		{
			in.registerNewRead(this.Args[i], this, new StepStage(line, subline, count++, "reading arg"));
		}

		if (this.Returns.length == 1)
		{
			//System.out.println("test: " + this.start.valueToString() + ".run()");
			//in.registerNewWrite(this.Returns[0], new IrVar_Ref(this.start.valueToString() + ".run()", ScopeLevels.Temporary, true), this, new StepStage(line, subline, count++, "singleton return"));
			in.registerNewWrite(this.Returns[0], this.Returns[0], this, new StepStage(line, subline, count++, "writing return"));
		}
		else
		{
			for (int i = 0; i < this.Returns.length; i++)
			{
				in.registerNewWrite(this.Returns[i], this.Returns[i], this, new StepStage(line, subline, count++, "writing return"));
			}
		}
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		//System.out.println("index is: " + inVarIndex + ", with argsNum: " + this.Args.length + ", and returnsNum: " + this.Returns.length);
		if(inVarIndex == 2)
		{
			this.start = inIn;
		}
		else if((inVarIndex -= 3) >= 0 && this.Args.length != 0 && inVarIndex < this.Args.length)
		{
			this.Args[inVarIndex] = inIn;
		}
		else if((inVarIndex -= this.Args.length) >= 0 && this.Returns.length != 0 && inVarIndex < this.Returns.length)
		{
			this.Returns[inVarIndex] = inIn;
		}
		else
		{
			throw new IndexOutOfBoundsException(inVarIndex + "");
		}
	}

	/*
	@Override
	protected void logAllReadsAndWrites()
	{
		/*
		 * Steps:
		 * functionRegister is read
		 * paramRegisters are read
		 * returnsRegisters are written
		 *
		
		final int lineNum = this.linePosData.getLineNum();
		final int subLine = this.linePosData.getSubLineNum();
		
		int curSubStep = 1;
		StepStage curStage = new StepStage(lineNum, subLine, curSubStep++, "reading functionRegister");
		this.logRead(curStage, this.registers[this.startRegister]);
		final int paramOffset = this.startRegister + 1;
		final int paramLimit;
		
		if(this.numArgs == -1)
		{
			paramLimit = this.registers.length;
		}
		else
		{
			paramLimit = this.numArgs + paramOffset;
		}
		
		for(int i = paramOffset; i < paramLimit; i++)
		{
			curStage = new StepStage(lineNum, subLine, curSubStep++, "reading paramRegister_" + (i - paramOffset));
			this.logRead(curStage, this.registers[i]);
		}
		
		final int returnsOffset = this.startRegister + 1;
		final int returnsLimit;
		
		if(this.numReturns == -1)
		{
			returnsLimit = this.registers.length;
		}
		else
		{
			returnsLimit = this.numReturns + returnsOffset;
		}
		
		for(int i = returnsOffset; i < returnsLimit; i++)
		{
			curStage = new StepStage(lineNum, subLine, curSubStep++, "writing returnsRegister_" + (i - returnsOffset));
			this.logWrite(curStage, this.registers[i]);
		}
	}
	*/
}
