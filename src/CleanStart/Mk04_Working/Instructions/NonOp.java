package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;

public class NonOp implements Instruction
{
	private boolean isExpired;
	
	private Instruction next;
	private Instruction prev;
	
	private NonOp()
	{
		this.isExpired = false;
		this.next = null;
		this.prev = null;
	}
	
	public static NonOp getInstance()
	{
		return new NonOp();
	}

	@Override
	public IrCodes getIrCode()
	{
		return IrCodes.NonOp;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.No;
	}

	@Override
	public Variable getInlinedForm()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isExpired()
	{
		return this.isExpired;
	}

	@Override
	public void setExpirationState(boolean inNewExpirationState)
	{
		this.isExpired = inNewExpirationState;
	}

	@Override
	public String getMangledName()
	{
		return "NonOp";
	}

	@Override
	public CharList getVerboseCommentForm()
	{
		return new CharList("NonOp");
	}

	@Override
	public void revVarTypes()
	{
		
	}

	@Override
	public Instruction getNext()
	{
		return this.next;
	}
	
	@Override
	public void setNext(Instruction in)
	{
		this.setNext(in, true);
	}
	
	@Override
	public void setNext(Instruction in, boolean linkBoth)
	{
		this.next = in;
		
		if(linkBoth)
		{
			in.setPrev(this, false);
		}
	}
	
	@Override
	public Instruction getPrev()
	{
		return this.prev;
	}
	
	@Override
	public void setPrev(Instruction in)
	{
		this.setPrev(in, true);
	}
	
	@Override
	public void setPrev(Instruction in, boolean linkBoth)
	{
		this.prev = in;
		
		if(linkBoth)
		{
			in.setNext(this, false);
		}
	}
	
	@Override
	public Instruction getFirstSharedInstructionWith(Instruction other)
	{
		return Instruction.getFirstSharedInstructionBetween(this, other);
	}

	@Override
	public int getInstanceNumber()
	{
		return -69;
	}
}
