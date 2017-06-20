package LuaBytecodeDecompiler.Mk02.Instructions;

public class VarCode
{
	private static final VarCode NullVarCode = new VarCode(null, -1, null);
	private static final VarCode EmptyVarCode = new VarCode(null, -1, null);
	
	private final ScopeTypes type;
	private final int index;
	private final boolean isArrayVar;
	
	private final VarCode next;
	
	public VarCode(ScopeTypes inType, int inIndex)
	{
		this(inType, inIndex, null);
	}
	
	public VarCode(ScopeTypes inType, int inIndex, VarCode inNext)
	{
		this(inType, inIndex, false, inNext);
	}
	
	public VarCode(ScopeTypes inType, int inIndex, boolean inIsArrayVar, VarCode inNext)
	{
		this.type = inType;
		this.index = inIndex;
		this.isArrayVar = inIsArrayVar;
		this.next = inNext;
	}

	public static VarCode getNullVarCode()
	{
		return NullVarCode;
	}
	
	public static VarCode getEmptyVarCode()
	{
		return EmptyVarCode;
	}

	public ScopeTypes getType()
	{
		return this.type;
	}

	public int getIndex()
	{
		return this.index;
	}

	public VarCode getNext()
	{
		return this.next;
	}
	
	public boolean isArrayVar()
	{
		return this.isArrayVar;
	}

	public boolean equals(VarCode in)
	{
		if(this == NullVarCode || this == EmptyVarCode)
		{
			return this == in;
		}
		else
		{
			if(in != NullVarCode && in != EmptyVarCode)
			{
				return this.type == in.type && this.index == in.index;
			}
			else
			{
				return false;
			}
		}
	}
	
	public static enum ScopeTypes
	{
		Local, Global, Upvalue, Constant, Hardcoded_Bool, Temp;
	}
}
