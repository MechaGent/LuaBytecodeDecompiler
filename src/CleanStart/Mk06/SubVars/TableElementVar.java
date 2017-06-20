package CleanStart.Mk06.SubVars;

import CleanStart.Mk06.StepNum;
import CleanStart.Mk06.Var;
import CleanStart.Mk06.VarRef;
import CleanStart.Mk06.Enums.InferenceTypes;
import CleanStart.Mk06.Enums.RawEnums;

public class TableElementVar extends Var
{
	private final Var tableRef;
	private final Var keyRef;
	private final InferenceTypes initialType;

	private TableElementVar(String inNamePrefix, Var inTableRef, Var inKeyRef, InferenceTypes inInitialType)
	{
		super(inNamePrefix, RawEnums.WrapperVar.getValue());
		this.tableRef = inTableRef;
		this.keyRef = inKeyRef;
		this.initialType = inInitialType;
	}

	private TableElementVar(String inNamePrefix, VarRef inTableRef, StepNum inTimeOfAccess_tableRef, VarRef inKeyRef, StepNum inTimeOfAccess_keyRef, InferenceTypes inInitialType)
	{
		super(inNamePrefix, RawEnums.WrapperVar.getValue());
		this.tableRef = inTableRef.getValueAtTime(inTimeOfAccess_tableRef);
		this.keyRef = inKeyRef.getValueAtTime(inTimeOfAccess_keyRef);
		this.initialType = inInitialType;
	}
	
	public static TableElementVar getInstance(String inNamePrefix, Var inTableRef, Var inKeyRef, InferenceTypes inInitialType)
	{
		return new TableElementVar(inNamePrefix, inTableRef, inKeyRef, inInitialType);
	}
	
	public static TableElementVar getInstance(String inNamePrefix, VarRef inTableRef, StepNum inTimeOfAccess_tableRef, VarRef inKeyRef, StepNum inTimeOfAccess_keyRef, InferenceTypes inInitialType)
	{
		return new TableElementVar(inNamePrefix, inTableRef, inTimeOfAccess_tableRef, inKeyRef, inTimeOfAccess_keyRef, inInitialType);
	}

	@Override
	public InferenceTypes getInitialType()
	{
		return this.initialType;
	}

	@Override
	public String getValueAsString()
	{
		return this.tableRef.getValueAsString() + ".[" + this.keyRef.getValueAsString() + ']';
		//return this.tableRef.getMangledName(false) + ".[" + this.keyRef.getMangledName(false) + ']';
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
