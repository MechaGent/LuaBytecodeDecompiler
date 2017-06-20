package LuaBytecodeDecompiler.Mk06_Working.IrInstructions;

import CharList.CharList;
import LuaBytecodeDecompiler.Mk06_Working.IrInstruction;
import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.StepStage;
import LuaBytecodeDecompiler.Mk06_Working.VarAssignMap;
import LuaBytecodeDecompiler.Mk06_Working.VarCleaner;
import LuaBytecodeDecompiler.Mk06_Working.Enums.IrCodes;

public class TailcallMethod extends IrInstruction
{
	private final IrVar startRegister;
	private final int numArgs;
	private final IrVar[] Args;
	
	public TailcallMethod(StepStage inLinePosData, IrVar inStartRegister, int inNumArgs, IrVar[] inArgs)
	{
		super(inLinePosData, IrCodes.TailcallMethod, 1);
		this.startRegister = inStartRegister;
		this.numArgs = inNumArgs;
		this.Args = inArgs;
	}

	public static TailcallMethod getInstance(StepStage inLineNum, IrVar inStartRegister, int inNumArgs, IrVar[] inArgs)
	{
		final TailcallMethod result = new TailcallMethod(inLineNum, inStartRegister, inNumArgs, inArgs);
		IrInstruction.LabelsMap.put(result.mangledName, result);
		return result;
	}

	public IrVar getStartRegister()
	{
		return this.startRegister;
	}

	public int getNumArgs()
	{
		return this.numArgs;
	}
	
	public IrVar[] getArgs()
	{
		return this.Args;
	}

	@Override
	protected CharList toCharList_internal_getByteCodeVersion()
	{
		final CharList result = new CharList();
		
		result.add("start register: ");
		result.add(this.startRegister.getLabel());
		result.add(", numArgs: ");
		result.addAsString(this.numArgs);
		
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
		
		
	}

	@Override
	public void swapSteps(StepStage inSwapOutStep, IrVar inSwapIn)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void evalSwapStatus(VarAssignMap in)
	{
		final int line = this.linePosData.getLineNum();
		final int subline = this.linePosData.getSubLineNum();
		int count = 2;
		
		for(int i = 0; i < this.Args.length; i++)
		{
			in.registerNewRead(this.Args[i], this, new StepStage(line, subline, count++, "reading tailcall args"));
		}
	}

	@Override
	public void SwapOutVar(int inVarIndex, IrVar inIn)
	{
		this.Args[inVarIndex - 2] = inIn;
	}

	public boolean isVarArgs()
	{
		return this.numArgs == -1;
	}
}
