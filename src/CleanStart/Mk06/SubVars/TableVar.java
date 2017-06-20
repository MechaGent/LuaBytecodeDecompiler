package CleanStart.Mk06.SubVars;

import CleanStart.Mk06.Var;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.RawEnums;

public class TableVar extends Var
{
	private final int arrLength;
	private final int hashSize;
	
	private TableVar(String inNamePrefix, int inArrLength, int inHashSize)
	{
		super(inNamePrefix, RawEnums.WrapperVar.getValue());
		this.arrLength = inArrLength;
		this.hashSize = inHashSize;
	}
	
	public static TableVar getInstance(String inNamePrefix, int inArrLength, int inHashSize)
	{
		return new TableVar(inNamePrefix, inArrLength, inHashSize);
	}

	@Override
	public InferenceTypes getInitialType()
	{
		return InferenceTypes.TableObject;
	}

	@Override
	public String getValueAsString()
	{
		return this.namePrefix + "_Table(aL=" + this.arrLength + ", hS=" + this.hashSize + ")";
	}

	@Override
	public int getValueAsInt()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public float getValueAsFloat()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getValueAsBoolean()
	{
		throw new UnsupportedOperationException();
	}
}
