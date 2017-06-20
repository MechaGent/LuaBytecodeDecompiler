package CleanStart.Mk02.Variables.Finals;

import CleanStart.Mk02.Variables.InlineStates;
import CleanStart.Mk02.Variables.VarTypes;

public class NewTableVar extends FinalVar
{
	private static int instanceCounter = 0;
	
	private final int tableSize;
	private final int hashSize;

	public NewTableVar(int inTableSize, int inHashSize)
	{
		super(instanceCounter++, VarTypes.NewTable);
		this.tableSize = inTableSize;
		this.hashSize = inHashSize;
	}
	
	@Override
	public InlineStates getReadState()
	{
		return InlineStates.Inline_Once;
	}

	@Override
	public InlineStates getWriteState()
	{
		return InlineStates.Inline_Once;
	}

	public int getTableSize()
	{
		return this.tableSize;
	}

	public int getHashSize()
	{
		return this.hashSize;
	}

	@Override
	public String getCargo_AsString()
	{
		return "new Table(" + this.tableSize + ", " + this.hashSize + ");";
	}
}
