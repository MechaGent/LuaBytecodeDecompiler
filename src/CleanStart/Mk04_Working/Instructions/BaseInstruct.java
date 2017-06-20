package CleanStart.Mk04_Working.Instructions;

import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;

public abstract class BaseInstruct implements Instruction
{
	protected final IrCodes IrCode;
	protected final int instanceNumber;
	protected boolean isExpired;

	protected Instruction prev;
	protected Instruction next;

	public BaseInstruct(IrCodes inIrCode, int inInstanceNumber)
	{
		this.IrCode = inIrCode;
		this.instanceNumber = inInstanceNumber;
		this.isExpired = false;
		this.prev = null;
		this.next = null;
	}

	@Override
	public boolean isExpired()
	{
		return this.isExpired;
	}

	@Override
	public void setExpirationState(boolean newExpirationState)
	{
		this.isExpired = newExpirationState;
	}

	@Override
	public IrCodes getIrCode()
	{
		return this.IrCode;
	}

	@Override
	public String getMangledName()
	{
		return this.IrCode.toString() + '_' + this.instanceNumber;
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

		if (linkBoth)
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

		if (linkBoth)
		{
			in.setNext(this, false);
		}
	}

	@Override
	public int getInstanceNumber()
	{
		return this.instanceNumber;
	}

	@Override
	public Instruction getFirstSharedInstructionWith(Instruction other)
	{
		return Instruction.getFirstSharedInstructionBetween(this, other);
	}

	public static void link(BaseInstruct node1, BaseInstruct node2)
	{
		node1.next = node2;
		node2.prev = node1;
	}
}
