package CleanStart.Mk03;

public abstract class Symbol
{
	private static final Symbol NullSymbol = new NullSymbol(SymbolTypes.Null);
	
	private Symbol prev;
	private Symbol next;
	
	private final SymbolTypes symbolType;

	public Symbol(SymbolTypes inSymbolType)
	{
		this.prev = NullSymbol;
		this.next = NullSymbol;
		this.symbolType = inSymbolType;
	}

	public SymbolTypes getSymbolType()
	{
		return this.symbolType;
	}
	
	private static class NullSymbol extends Symbol
	{
		public NullSymbol(SymbolTypes inSymbolType)
		{
			super(inSymbolType);
		}
	}
}
