package CleanStart.Mk02.Variables.Finals;

import CleanStart.Mk02.Variables.BaseVar;
import CleanStart.Mk02.Variables.VarTypes;

public class TableElemVar extends FinalVar
{
	private final BaseVar table;
	private final BaseVar index;
	
	public TableElemVar(int inInstanceNumber, BaseVar inTable, BaseVar inIndex)
	{
		super(inInstanceNumber, VarTypes.TableElem);
		this.table = inTable;
		this.index = inIndex;
	}

	@Override
	public String getCargo_AsString()
	{
		return this.table.getMangledName() + '[' + this.index.toString() + ']';
	}
}
