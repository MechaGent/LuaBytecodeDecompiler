package LuaBytecodeDecompiler.Mk06_Working.Enums;

public enum VarTypes
{
	Ref,
	String,
	Int,
	Boolean,
	Null,	//difference between this and NullValued is that this indicates lack of initialization
	NullValued,
	Indexed,
	Mapped,
	Formula,
	Function,
	FunctionInstance;
}
