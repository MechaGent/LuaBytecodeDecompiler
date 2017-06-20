package CleanStart.Mk04_Working.Variables;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Instruction;
import CleanStart.Mk04_Working.Variable;

public class TableElementVar extends BaseVar
{
	private MystVar table;
	private Variable index;
	
	private TableElementVar(String inNamePrefix, Instruction inAssignmentInstruction, MystVar inTable, Variable inIndex)
	{
		super(inNamePrefix, VarTypes.TableElement, inAssignmentInstruction);
		this.table = inTable;
		this.index = inIndex;
	}
	
	public static TableElementVar getInstance(String inNamePrefix, Instruction inAssignmentInstruction, MystVar inTable, Variable inIndex)
	{
		return new TableElementVar(inNamePrefix, inAssignmentInstruction, inTable, inIndex);
	}
	
	@Override
	public String getMangledName(boolean showInferredTypes)
	{
		final CharList result = new CharList();
		
		result.add(this.table.getMostRelevantIdValue(showInferredTypes));
		result.add(".[");
		result.add(this.index.getMostRelevantIdValue(showInferredTypes));
		result.add(']');
		
		return result.toString();
	}

	@Override
	public CharList getValueAsCharList()
	{
		final CharList result = new CharList();
		
		result.add(this.table.getMostRelevantIdValue());
		result.add(".[");
		result.add(this.index.getMostRelevantIdValue());
		result.add(']');
		
		return result;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		//might be able to switch to 'Yes'
		return CanBeInlinedTypes.No;
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Override
	public String valueToString()
	{
		return this.getValueAsCharList().toString();
	}
}
