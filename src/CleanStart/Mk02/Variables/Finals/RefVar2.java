package CleanStart.Mk02.Variables.Finals;

import CleanStart.Mk02.Instructions.Instruction;
import CleanStart.Mk02.Variables.BaseVar;
import CleanStart.Mk02.Variables.VarTypes;
import SingleLinkedList.Mk01.SingleLinkedList;

public class RefVar2 extends FinalVar
{
	private static int instanceCounter = 0;
	private static final SingleLinkedList<RefVar2> instances = new SingleLinkedList<RefVar2>();
	
	private BaseVar cargo;
	private boolean cargoIsResolved;
	private final Instruction writeInstruction;
	
	private RefVar2(BaseVar inCargo, Instruction inWriteInstruction)
	{
		super(instanceCounter++, VarTypes.Ref);
		this.cargo = inCargo;
		this.cargoIsResolved = false;
		this.writeInstruction = inWriteInstruction;
	}
	
	public static RefVar2 getInstance(BaseVar inCargo, Instruction inWriteInstruction)
	{
		final RefVar2 result = new RefVar2(inCargo, inWriteInstruction);
		instances.add(result);
		return result;
	}

	@Override
	public String getCargo_AsString()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void resolveCargo()
	{
		if(this.getNumReads() != 0)
		{
			this.cargo.incrementNumReads(this.getNumReads());
		}
	}
}
