package LuaBytecodeDecompiler.Mk01.Enums.OpCodeRevisions;

public enum OpCodes_5_1
{
	Move,
	LoadK,
	LoadBool,
	LoadNull,
	GetUpvalue,
	GetGlobal,
	GetTable,
	SetGlobal,
	SetUpvalue,
	SetTable,
	NewTable,
	Self,
	Add,
	Subtract,
	Multiply,
	Divide,
	Modulo,
	Power,
	ArithmeticNegation,
	LogicalNegation,
	Length,
	Concatenation,
	Jump,
	TestIfEqual,
	TestIfLessThan,
	TestIfLessThanOrEqual,
	ConditionalJump,
	ConditionalJumpAndSet,
	Call,
	TailCall,
	Return,
	IterateNumericForLoop,		//OP_FORLOOP
	InitNumericForLoop,			//OP_FORPREP
	IterateGenericForLoop,		//OP_TFORLOOP
	SetList,
	Close,
	Closure,
	VarArg;
}
