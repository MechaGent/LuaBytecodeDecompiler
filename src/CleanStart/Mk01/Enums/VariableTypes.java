package CleanStart.Mk01.Enums;

public enum VariableTypes
{
	Constant_String(true),
	Constant_Int(true),
	Constant_Float(true),
	Constant_Null(true),
	Constant_Bool(true),
	Reference(false),
	InstancedReference(true),
	InstancedReference_Indexed(true),
	TableOfReferences(false),
	InstancedTableOfReferences(true),
	Formula(false),
	InstancedFormula(false),
	FunctionPrototype(false);
	
	private final boolean isConstantType;

	private VariableTypes(boolean inIsConstantType)
	{
		this.isConstantType = inIsConstantType;
	}
	
	public boolean isConstantType()
	{
		return this.isConstantType;
	}
}
