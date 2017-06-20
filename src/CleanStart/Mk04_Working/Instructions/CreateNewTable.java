package CleanStart.Mk04_Working.Instructions;

import CharList.CharList;
import CleanStart.Mk04_Working.CanBeInlinedTypes;
import CleanStart.Mk04_Working.Variable;
import CleanStart.Mk04_Working.Instructions.Enums.IrCodes;
import CleanStart.Mk04_Working.Variables.MystVar;
import CleanStart.Mk04_Working.Variables.NewTableVar;

public class CreateNewTable extends BaseInstruct
{
	private static int instanceCounter = 0;
	
	private final MystVar target;
	private final NewTableVar NewTable;
	
	private CreateNewTable(MystVar inTarget, NewTableVar inNewTable)
	{
		super(IrCodes.CreateNewTable, instanceCounter++);
		this.target = inTarget;
		this.NewTable = inNewTable;
	}
	
	public static CreateNewTable getInstance(MystVar inTarget, NewTableVar inNewTable)
	{
		return new CreateNewTable(inTarget, inNewTable);
	}

	public MystVar getTarget()
	{
		return this.target;
	}

	public NewTableVar getNewTable()
	{
		return this.NewTable;
	}

	@Override
	public CanBeInlinedTypes canBeInlined()
	{
		return CanBeInlinedTypes.Once;
	}

	@Override
	public Variable getInlinedForm()
	{
		return this.NewTable;
	}
	
	@Override
	public CharList getVerboseCommentForm()
	{
		final CharList result = new CharList();
		
		result.add(this.target.getMostRelevantIdValue());
		result.add(" = ");
		result.add("new LuaTable();");
		
		return result;
	}

	@Override
	public void revVarTypes()
	{
		this.target.revVarType();
	}
}
