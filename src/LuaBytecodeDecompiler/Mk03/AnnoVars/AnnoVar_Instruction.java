package LuaBytecodeDecompiler.Mk03.AnnoVars;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.ScopeLevels;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.VarFlowHandler.VarTouchy;

public class AnnoVar_Instruction extends AnnoVar
{
	private final AnnoInstruction cargo;
	private final VarTouchy target;

	public AnnoVar_Instruction(String inName, ScopeLevels inScope, AnnoInstruction inCargo)
	{
		this(inName, inScope, inCargo, null);
	}
	
	public AnnoVar_Instruction(String inName, ScopeLevels inScope, AnnoInstruction inCargo, boolean isMethodCall)
	{
		super(inName, inScope, isMethodCall);
		this.cargo = inCargo;
		this.target = null;
	}
	
	public AnnoVar_Instruction(String inName, ScopeLevels inScope, AnnoInstruction inCargo, VarTouchy inTarget)
	{
		super(inName, inScope);
		this.cargo = inCargo;
		this.target = inTarget;
	}
	
	@Override
	public String getAdjustedName()
	{
		final CharList result;
		
		switch(this.cargo.getOpCode())
		{
			case InitNumericForLoop:
			{
				result = this.cargo.toCharList_InlineForm(this.target);
				break;
			}
			default:
			{
				result = this.cargo.toCharList_InlineForm();
				
				break;
			}
		}
		
		
		
		if(result == null)
		{
			throw new IllegalArgumentException("bad result: " + this.cargo.toString());
		}
		
		return result.toString();
	}
}
