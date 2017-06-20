package CleanStart.Mk02;

import CleanStart.Mk02.Variables.NonFinals.RefVar;

public class VarsDispenser
{
	private final RefVar[] locals;
	private final RefVar[] upvalues;
	
	public VarsDispenser(int numLocals, int numUpvalues)
	{
		this.locals = new RefVar[numLocals];
		this.upvalues = new RefVar[numUpvalues];
	}
	
	public RefVar getCleanedRefVar_Locals(int index, boolean forRead)
	{
		
	}
}
