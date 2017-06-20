package CleanStart.Mk03.Vars;

import HalfByteRadixMap.HalfByteRadixMap;

/**
 * Proof-of-concept and testbed for exploring variables data
 *
 */
public class MystVar
{
	private static MystVar NullVar = new MystVar("NullMystVar");
	
	private MystVar prev;
	
	/*
	 * Facts
	 */
	private final String varName;
	private VarTypes knownType;
	private final HalfByteRadixMap<MystVar> reads;	//keyed by varName
	
	private MystVar(String inVarName)
	{
		this.prev = NullVar;
		this.varName = inVarName;
		this.knownType = VarTypes.Indefinite;
		this.reads = new HalfByteRadixMap<MystVar>();
	}
	
	public static MystVar getInstance(String inVarName)
	{
		return new MystVar(inVarName);
	}

	public MystVar getPrev()
	{
		return this.prev;
	}

	public void setPrev(MystVar inPrev)
	{
		this.prev = inPrev;
	}

	public VarTypes getKnownType()
	{
		return this.knownType;
	}

	public void setKnownType(VarTypes inKnownType)
	{
		this.knownType = inKnownType;
	}

	public String getVarName()
	{
		return this.varName;
	}
	
	public int getNumReads()
	{
		return this.reads.size();
	}
}
