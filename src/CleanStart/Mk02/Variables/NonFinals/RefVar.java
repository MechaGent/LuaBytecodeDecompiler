package CleanStart.Mk02.Variables.NonFinals;

import CleanStart.Mk02.Instructions.Instruction;
import CleanStart.Mk02.Instructions.NoOp;
import CleanStart.Mk02.Variables.BaseVar;
import CleanStart.Mk02.Variables.InlineStates;
import CleanStart.Mk02.Variables.VarTypes;
import CleanStart.Mk02.Variables.Finals.FinalVar;
import CleanStart.Mk02.Variables.Finals.IndetRefVar;

public class RefVar extends NonFinalVar
{
	private static int instanceCounter = 0;
	
	private BaseVar cargo;
	private Instruction responsible;
	
	public RefVar()
	{
		this(NonFinalVar.IndetVar);
	}
	
	public RefVar(BaseVar inCargo)
	{
		super(instanceCounter++, VarTypes.Ref);
		this.cargo = inCargo;
		this.responsible = new NoOp();
	}

	public BaseVar getCargo()
	{
		return this.cargo;
	}
	
	@Override
	public InlineStates getReadState()
	{
		return this.cargo.getReadState();
	}

	@Override
	public InlineStates getWriteState()
	{
		return this.cargo.getWriteState();
	}

	public void assignValueAs(BaseVar in, Instruction responsible)
	{
		final BaseVar result;
		
		switch(in.getVarType())
		{
			case Ref:
			{
				final RefVar cast = (RefVar) in;
				
				if(cast.cargo.isIndeterminate())
				{
					result = IndetRefVar.getInstance(cast);
				}
				else
				{
					result = cast.cargo;
				}
				
				break;
			}
			default:
			{
				result = (FinalVar) in;
				break;
			}
		}
		
		if(this.cargo == result)	//if true, assignment is superfluous
		{
			responsible.setExpireState(true);
		}
		else
		{
			this.cargo = result;
		}
	}
}
