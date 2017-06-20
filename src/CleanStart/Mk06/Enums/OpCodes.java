package CleanStart.Mk06.Enums;

public enum OpCodes
{
	/**
	 * MOVE
	 * <br>
	 * A B
	 * <br>
	 * R(A) := R(B)
	 * <br>
	 * Copies the value of register R(B) into register R(A).
	 * If R(B) holds a table, function or userdata, then the reference to that object is copied.
	 * MOVE is often used for moving values into place for the next operation.
	 * The opcode for MOVE has a second purpose – it is also used in creating closures,
	 * always appearing after the CLOSURE instruction;
	 * see CLOSURE for more information.
	 */
	Move(RawInstructionTypes.iABC),

	/**
	 * LOADK
	 * <br>
	 * A Bx
	 * <br>
	 * R(A) := Kst(Bx)
	 * <br>
	 * Loads constant number Bx into register R(A). Constants are usually
	 * numbers or strings. Each function has its own constant list,
	 * or pool.
	 */
	LoadK(RawInstructionTypes.iABx),

	/**
	 * LOADBOOL
	 * <br>
	 * A B C
	 * <br>
	 * R(A) := (Bool)B; if (C) PC++
	 * <br>
	 * Loads a boolean value ( true or false ) into register R(A).
	 * true is usually encoded as an integer 1, false is always 0.
	 * If C is non-zero, then the next instruction is skipped
	 * (this is used when you have an assignment statement where the expression uses relational operators, e.g. M = K>5.)
	 * You can use any non-zero value for the boolean true in field B,
	 * but since you cannot use booleans as numbers in Lua, it’s best to stick to 1 for true .
	 */
	LoadBool(RawInstructionTypes.iABC),

	/**
	 * LOADNIL
	 * <br>
	 * A B
	 * <br>
	 * R(A) := ... := R(B) := nil
	 * <br>
	 * Sets a range of registers from R(A) to R(B) to
	 * nil
	 * . If a single register is to
	 * be assigned to, then R(A) = R(B). When two or more consecutive locals
	 * need to be assigned
	 * nil
	 * values, only a single LOADNIL is needed.
	 */
	LoadNull(RawInstructionTypes.iABC),

	/**
	 * GETUPVAL
	 * <br>
	 * A B
	 * <br>
	 * R(A) := UpValue[B]
	 * <br>
	 * Copies the value in upvalue number B into register R(A ).
	 * Each function may have its own upvalue list.
	 * This upvalue list is internal to the virtual machine;
	 * the list of upvalue name strings in a prototype is not mandatory.
	 * The opcode for GETUPVAL has a second purpose – it is also used in creating closures,
	 * always appearing after the CLOSURE instruction; see CLOSURE for more information.
	 */
	GetUpvalue(RawInstructionTypes.iABC),

	/**
	 * GETGLOBAL
	 * <br>
	 * A Bx
	 * <br>
	 * R(A) := Gbl[Kst(Bx)]
	 * <br>
	 * Copies the value of the global variable (whose name is given in constant number Bx) into register R(A).
	 * The name constant must b a string.
	 */
	GetGlobal(RawInstructionTypes.iABx),

	/**
	 * GETTABLE
	 * <br>
	 * A B C
	 * <br>
	 * R(A) := R(B)[RK(C)]
	 * <br>
	 * Copies the value from a table element into register R(A). The table is referenced by register R(B),
	 * while the index to the table is given by RK(C), which may be the value of register R(C) or a constant number.
	 */
	GetTable(RawInstructionTypes.iABC),

	/**
	 * SETGLOBAL
	 * <br>
	 * A Bx
	 * <br>
	 * Gbl[Kst(Bx)] := R(A)
	 * <br>
	 * Copies the value from register R(A) into the global variable (whose name is given in constant number Bx).
	 * The name constant must be a string.
	 */
	SetGlobal(RawInstructionTypes.iABx),

	/**
	 * SETUPVAL
	 * <br>
	 * A B
	 * <br>
	 * UpValue[B] := R(A)
	 * <br>
	 * Copies the value from register R(A) into the upvalue number B, in the upvalue list for that function.
	 */
	SetUpvalue(RawInstructionTypes.iABC),

	/**
	 * SETTABLE
	 * A B C
	 * R(A)[RK(B)] := RK(C)
	 * Copies the value from register R(C) or a constant into a table element.
	 * The table is referenced by register R(A), while the index to the table is given by RK(B),
	 * which may be the value of register R(B) or a constant number.
	 */
	SetTable(RawInstructionTypes.iABC),

	/**
	 * NEWTABLE
	 * A B C
	 * R(A) := {} (size = B,C)
	 * Creates a new empty table at register R(A). B and C are the encoded size information for the array part and the hash part of the table,
	 * respectively. Appropriate values for B and C are set in order to avoid rehashing when initially populating
	 * the table with array values or hash key-value pairs. Operand B and C are both encoded as a “floating point byte” (so named in lobject.c )
	 * which is eeeeexxx in binary, where x is the mantissa and e is the exponent. The actual value is calculated as 1xxx*2^(eeeee-1)
	 * if eeeee is greater than 0 (a range of 8 to 15*2^30.) If eeeee is 0, the actual value is xxx (a range of 0 to 7.)
	 * If an empty table is created, both sizes are zero. If a table is created with a number of objects,
	 * the code generator counts the number of array elements and the number of hash elements.
	 * Then, each size value is rounded up and encoded in B and C using the floating point byte format.
	 */
	NewTable(RawInstructionTypes.iABC),

	/**
	 * SELF
	 * A B C
	 * R(A+1) := R(B); R(A) := R(B)[RK(C)]
	 * For object-oriented programming using tables. Retrieves a function reference from a table element and places it in register R(A),
	 * then a reference to the table itself is placed in the next register, R(A+1).
	 * This instruction saves some messy manipulation when setting up a method call.
	 * R(B) is the register holding the reference to the table with the method.
	 * The method function itself is found using the table index RK(C), which may be the value of register R(C) or a constant number.
	 */
	Self(RawInstructionTypes.iABC),

	/**
	 * ADD
	 * A B C
	 * R(A) = RK(B) + RK(C)
	 */
	Add(RawInstructionTypes.iABC),

	/**
	 * SUB
	 * A B C
	 * R(A) = RK(B) - RK(C)
	 */
	Subtract(RawInstructionTypes.iABC),

	/**
	 * MUL
	 * A B C
	 * R(A) = RK(B) * RK(C)
	 */
	Multiply(RawInstructionTypes.iABC),

	/**
	 * DIV
	 * A B C
	 * R(A) = RK(B) / RK(C)
	 */
	Divide(RawInstructionTypes.iABC),

	/**
	 * MOD
	 * A B C
	 * R(A) = RK(B) % RK(C)
	 */
	Modulo(RawInstructionTypes.iABC),

	/**
	 * POW
	 * A B C
	 * R(A) = RK(B) ^ RK(C)
	 */
	Power(RawInstructionTypes.iABC),

	/**
	 * UNM
	 * A B
	 * R(A) := -R(B)
	 * Unary minus (arithmetic operator with one input.)
	 * R(B) is negated and the value placed in R(A). R(A) and R(B) are always registers.
	 */
	ArithmeticNegation(RawInstructionTypes.iABC),

	/**
	 * NOT
	 * A B
	 * R(A) := not R(B)
	 * Applies a boolean NOT to the value in R(B) and places the result in R(A).
	 * R(A) and R(B) are always registers.
	 */
	LogicalNegation(RawInstructionTypes.iABC),

	/**
	 * LEN
	 * A B
	 * R(A) := length of R(B)
	 * Returns the length of the object in R(B). For strings, the string length is returned,
	 * while for tables, the table size (as defined in Lua) is returned.
	 * For other objects, the metamethod is called. The result, which is a number, is placed in R(A).
	 * 
	 * uses the '#' operator
	 */
	Length(RawInstructionTypes.iABC),

	/**
	 * CONCAT
	 * A B C
	 * R(A) := R(B).. ... ..R(C)
	 * Performs concatenation of two or more strings. In a Lua source,
	 * this is equivalent to one or more concatenation operators (‘ .. ’)
	 * between two or more expressions. The source registers must be consecutive,
	 * and C must always be greater than B. The result is placed in R(A).
	 */
	Concatenation(RawInstructionTypes.iABC),

	/**
	 * JMP
	 * sBx
	 * PC += sBx
	 * Performs an unconditional jump, with sBx as a signed displacement.
	 * sBx is added to the program counter (PC), which points to the next instruction to be executed.
	 * E.g., if sBx is 0, the VM will proceed to t he next instruction. JMP is used in loops,
	 * conditional statements, and in expressions when a boolean true / false need to be generated.
	 */
	Jump(RawInstructionTypes.iAsBx),

	/**
	 * EQ
	 * A B C
	 * if ((RK(B) == RK(C)) ~= A) then PC++
	 */
	TestIfEqual(RawInstructionTypes.iABC),

	/**
	 * EQ
	 * A B C
	 * if ((RK(B) < RK(C)) ~= A) then PC++
	 */
	TestIfLessThan(RawInstructionTypes.iABC),

	/**
	 * LE
	 * A B C
	 * if ((RK(B) <= RK(C)) ~= A) then PC++
	 */
	TestIfLessThanOrEqual(RawInstructionTypes.iABC),

	/**
	 * TEST
	 * A C
	 * if not (R(A) <=> C) then PC++
	 */
	TestLogicalComparison(RawInstructionTypes.iABC),

	/**
	 * TESTSET
	 * A B C
	 * if (R(B) <=> C) then R(A) := R(B) else PC++
	 */
	TestAndSaveLogicalComparison(RawInstructionTypes.iABC),

	/**
	 * CALL
	 * A B C
	 * R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1))
	 * Performs a function call, with register R(A) holding the reference to the function object to be called.
	 * Parameters to the function are placed in the registers following R(A). If B is 1, the function has no parameters.
	 * If B is 2 or more, there are (B-1) parameters. If B is 0, the function parameters range from R(A+1) to the top of the stack.
	 * This form is used when the last expression in the parameter list is a function call,
	 * so the number of actual parameters is indeterminate. Results returned by the function call is placed
	 * in a range of registers starting from R(A). If C is 1, no return results are saved.
	 * If C is 2 or more, (C-1) return values are saved. If C is 0, then multiple return results are saved,
	 * depending on the called function. CALL always updates the top of stack value.
	 * CALL, RETURN, VARARG and SETLIST can use multiple values (up to the top of the stack.)
	 */
	Call(RawInstructionTypes.iABC),

	/**
	 * TAILCALL
	 * A B C
	 * return R(A)(R(A+1), ... ,R(A+B-1))
	 * Performs a tail call, which happens when a return statement has a single function call as the expression,
	 * e.g. return foo(bar) . A tail call is effectively a goto , and avoids nesting calls another level deeper.
	 * Only Lua functions can be tailcalled. Like CALL, register R(A) holds the reference to the function object to be called.
	 * B encodes the number of parameters in the same manner as a CALL instruction.
	 * C isn’t used by TAILCALL, since all return results are significant. In any case, Lua always generates a 0 for C,
	 * to denote multiple return results.
	 */
	TailCall(RawInstructionTypes.iABC),

	/**
	 * RETURN
	 * A B
	 * return R(A), ... ,R(A+B-2)
	 * Returns to the calling function, with optional return values. If B is 1, there are no return values.
	 * If B is 2 or more, there are (B- 1) return values, located in consecutive registers from R(A) onwards.
	 * If B is 0, the set of values from R(A) to the top of the stack is returned.
	 * This form is used when the last expression in the return list is a function call,
	 * so the number of actual values returned is indeterminate . RETURN also closes any open upvalues,
	 * equivalent to a CLOSE instruction. See the CLOSE instruction for more information.
	 */
	Return(RawInstructionTypes.iABC),

	/**
	 * FORLOOP
	 * A sBx
	 * R(A) += R(A+2)
	 * if R(A) <?= R(A+1) then {PC += sBx; R(A+3) = R(A)}
	 */
	IterateNumericForLoop(RawInstructionTypes.iAsBx), // OP_FORLOOP

	/**
	 * FORPREP
	 * A sBx
	 * R(A) -= R(A+2); PC += sBx
	 */
	InitNumericForLoop(RawInstructionTypes.iAsBx), // OP_FORPREP

	/**
	 * TFORLOOP
	 * A C
	 * R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));
	 * if R(A+3) ~= nil then { R(A+2) = R(A+3); } else {PC++; }
	 */
	IterateGenericForLoop(RawInstructionTypes.iABC), // OP_TFORLOOP

	/**
	 * SETLIST
	 * A B C
	 * R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B
	 * Sets the values for a range of array elements in a table referenced by R(A). Field B is the number of elements to set.
	 * Field C encodes the block number of the table to be initialized. The values used to initialize the table are
	 * located in registers R(A+1), R(A+2), and so on. The block size is denoted by FPF. FPF is “fields per flush ”,
	 * defined as LFIELDS_PER_FLUSH in the source file lopcodes.h , with a value of 50. For example, for array locations 1 to 20,
	 * C will be 1 and B will be 20. If B is 0, the table is set with a variable number of array elements,
	 * from register R(A+1) up to the top of the stack. This happens when the last element in the table constructor is a
	 * function call or a vararg operator. If C is 0, the next instruction is cast as an integer, and used as the C value.
	 * This happens only when operand C is unable to encode the block number,
	 * i.e. when C > 511, equivalent to an array index greater than 25550.
	 */
	SetList(RawInstructionTypes.iABC),

	/**
	 * CLOSE
	 * A
	 * close all variables in the stack up to (>=) R(A)
	 * Closes all local variables in the stack from register R(A ) onwards. 
	 * This instruction is only generated if there is an upvalue present within those local variables. 
	 * It has no effect if a local isn’t used as an upvalue. If a local is used as an upvalue, 
	 * then the local variable need to be placed somewhere, otherwise it will go out of scope and disappear when a lexical 
	 * block enclosing the local variable ends. CLOSE performs this operation for all affected local 
	 * variables for do end blocks or loop blocks. RETURN also does an implicit CLOSE when a function returns.
	 */
	Close(RawInstructionTypes.iABC),

	/**
	 * CLOSURE
	 * A Bx
	 * R(A) := closure(KPROTO[Bx], R(A), ... ,R(A+n))
	 * Creates an instance (or closure) of a function. Bx is the function number of the function to be instantiated
	 * in the table of function prototypes. This table is located after the constant table for each function in a binary chunk.
	 * The first function prototype is numbered 0. Register R(A) is assigned the reference to the instantiated function object.
	 * For each upvalue used by the instance of the function KPROTO[Bx], there is a pseudo-instruction that follows CLOSURE.
	 * Each upvalue corresponds to either a MOVE or a GETUPVAL pseudo-instruction.
	 * Only the B field on either of these pseudo-instructions are significant.
	 * A MOVE corresponds to local variable R(B) in the current lexical block, which will be used as an upvalue in the instantiated function.
	 * A GETUPVAL corresponds upvalue number B in the current lexical block. The VM uses these pseudo-instructions to manage upvalues.
	 */
	Closure(RawInstructionTypes.iABx),

	/**
	 * VARARG
	 * A B
	 * R(A), R(A+1), ..., R(A+B-1) = vararg
	 * VARARG implements the vararg operator ‘ ... ’ in expressions. VARARG copies B-1 parameters into a number of registers starting from R(A),
	 * padding with nil s if there aren’t enough values. If B is 0, VARARG copies as many values as it can,
	 * based on the number of parameters passed. If a fixed number of values is required, B is a value greater than 1.
	 * If any number of values is required, B is 0.
	 */
	VarArg(RawInstructionTypes.iABC);
	
	private static final OpCodes[] All = OpCodes.values();
	private static final OpCodes[] IntToCodesMap = initMap();
	
	private final RawInstructionTypes rawInstructionType;
	
	private OpCodes(RawInstructionTypes inRawInstructionType)
	{
		this.rawInstructionType = inRawInstructionType;
	}

	private static final OpCodes[] initMap()
	{
		return new OpCodes[] {
																	OpCodes.Move,
																	OpCodes.LoadK,
																	OpCodes.LoadBool,
																	OpCodes.LoadNull,
																	OpCodes.GetUpvalue,
																	OpCodes.GetGlobal,
																	OpCodes.GetTable,
																	OpCodes.SetGlobal,
																	OpCodes.SetUpvalue,
																	OpCodes.SetTable,
																	OpCodes.NewTable,
																	OpCodes.Self,
																	OpCodes.Add,
																	OpCodes.Subtract,
																	OpCodes.Multiply,
																	OpCodes.Divide,
																	OpCodes.Modulo,
																	OpCodes.Power,
																	OpCodes.ArithmeticNegation,
																	OpCodes.LogicalNegation,
																	OpCodes.Length,
																	OpCodes.Concatenation,
																	OpCodes.Jump,
																	OpCodes.TestIfEqual,
																	OpCodes.TestIfLessThan,
																	OpCodes.TestIfLessThanOrEqual,
																	OpCodes.TestLogicalComparison,
																	OpCodes.TestAndSaveLogicalComparison,
																	OpCodes.Call,
																	OpCodes.TailCall,
																	OpCodes.Return,
																	OpCodes.IterateNumericForLoop,
																	OpCodes.InitNumericForLoop,
																	OpCodes.IterateGenericForLoop,
																	OpCodes.SetList,
																	OpCodes.Close,
																	OpCodes.Closure,
																	OpCodes.VarArg };
	}

	public RawInstructionTypes getRawInstructionType()
	{
		return this.rawInstructionType;
	}

	public static OpCodes[] getAll()
	{
		return All;
	}
	
	public static int getNumElements()
	{
		return All.length;
	}
	
	public static OpCodes parse(int in)
	{
		return IntToCodesMap[in & 0x3f];
	}

	public static interface OpCodeMapper
	{
		public int getByteCodeFor(OpCodes in);

		public OpCodes getOpCodeFor(int ByteCode);

		public RawInstructionTypes getTypeOf(OpCodes op);

		public RawInstructionTypes getTypeOf(byte op);
	}
}
