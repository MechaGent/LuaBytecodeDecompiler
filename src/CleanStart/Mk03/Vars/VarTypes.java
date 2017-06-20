package CleanStart.Mk03.Vars;

public enum VarTypes
{
	Ref,
	Table,
	TableElementPointer,
	NewTable,	//can only be inlined if read once
	String,
	Int,
	Float,
	Bool,
	Indefinite;
}
