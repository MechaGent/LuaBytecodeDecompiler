package CleanStart.Mk04_Working;

import CleanStart.Mk04_Working.Instructions.NonOp;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;

public class InstructionList
{
	private Instruction head;
	private Instruction tail;
	private int size;
	
	public InstructionList()
	{
		this.head = NonOp.getInstance();
		this.tail = this.head;
		this.size = 0;
	}
	
	public InstructionList(Instruction head, Instruction tail, int size)
	{
		this.head = head;
		this.tail = tail;
		this.size = size;
	}
	
	public void popLeadingNonOps()
	{
		while(this.head != null && this.head.getIrCode() == IrCodes.NonOp)
		{
			this.head = this.head.getNext();
			this.size--;
		}
	}
	
	public boolean isEmpty()
	{
		return this.size == 0;
	}
	
	public Instruction getHead()
	{
		return this.head;
	}
	
	public void setHead(Instruction in)
	{
		this.head = in;
	}

	public void addAfter(Instruction before, Instruction next)
	{
		if(this.size == 0)
		{
			throw new IllegalArgumentException();
		}
		
		if (before == this.tail)
		{
			this.tail.setNext(next);
			this.tail = next;
		}
		else
		{
			final Instruction after = before.getNext();
			before.setNext(next);
			next.setNext(after);
		}
		
		this.size++;
	}
	
	public void add(Instruction next)
	{
		if(this.size == 0)
		{
			this.head.setNext(next);
			this.tail = next;
			this.size = 2;
		}
		else
		{
			this.tail.setNext(next);
			this.tail = next;
			this.size++;
		}
	}

	public void remove(Instruction in)
	{
		if(this.size == 0)
		{
			throw new IllegalArgumentException();
		}
		
		if (this.tail == in)
		{
			this.tail = this.tail.getPrev();
			this.tail.setNext(null, false);
		}
		else
		{
			in.getPrev().setNext(in.getNext());
		}
		
		this.size--;
	}

	public void addBefore(Instruction before, Instruction next)
	{
		if(this.size == 0)
		{
			throw new IllegalArgumentException();
		}
		
		if (before == this.tail)
		{
			this.tail.setPrev(next);
		}
		else
		{
			final Instruction previous = before.getPrev();
			previous.setNext(next);
			next.setNext(before);
		}

		this.size++;
	}
	
	public int getSize()
	{
		return this.size;
	}
}
