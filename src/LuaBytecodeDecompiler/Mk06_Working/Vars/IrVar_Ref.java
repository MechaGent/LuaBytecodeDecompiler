package LuaBytecodeDecompiler.Mk06_Working.Vars;

import LuaBytecodeDecompiler.Mk06_Working.IrVar;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Enums.VarTypes;

public class IrVar_Ref extends IrVar
{
	private static int instanceCounter = 0;
	
	//private final TreeMap<StepStage, IrInstruction> reads;
	//private final TreeMap<StepStage, IrInstruction> writes;
	
	public IrVar_Ref(ScopeLevels inScope, boolean inIsFromMethodReturn)
	{
		this("IrVar_Ref_" + instanceCounter++, inScope, inIsFromMethodReturn);
	}
	
	public IrVar_Ref(String inLabel, ScopeLevels inScope, boolean inIsFromMethodReturn)
	{
		super(inLabel, VarTypes.Ref, inScope, inIsFromMethodReturn);
		
		if(inLabel == null)
		{
			throw new NullPointerException();
		}
		//this.reads = new TreeMap<StepStage, IrInstruction>();
		//this.writes = new TreeMap<StepStage, IrInstruction>();
	}

	@Override
	public String valueToString()
	{
		return this.label;
	}
	
	/*
	public IrVar getMostRecentValue(StepStage recent)
	{
		final Entry<StepStage, IrInstruction> result = this.writes.floorEntry(recent);
		return result.getValue().getTargetValueFromStep(result.getKey());
	}
	
	public void addWrite(StepStage step, IrInstruction write)
	{
		this.writes.put(step, write);
	}
	
	public void addRead(StepStage step, IrInstruction read)
	{
		this.reads.put(step, read);
	}
	*/
}
