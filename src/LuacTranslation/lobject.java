package LuacTranslation;

public class lobject
{
	public static interface GCObject extends Value
	{
		
	}
	
	public static interface lua_Number extends Value
	{
		
	}
	
	public static class CommonHeader
	{
		private GCObject next;
		private byte tt;
		private byte marked;
	}
	
	public static interface Value
	{
		public static VoidValue getValue_Void(Object inP)
		{
			return new VoidValue(inP);
		}
		
		public static IntValue getValue_Int(int inValue)
		{
			return new IntValue(inValue);
		}
	}
	
	public static class VoidValue implements Value
	{
		private Object p;

		private VoidValue(Object inP)
		{
			this.p = inP;
		}

		public Object getP()
		{
			return this.p;
		}
	}
	
	public static class IntValue implements Value
	{
		private int value;

		private IntValue(int inValue)
		{
			this.value = inValue;
		}

		public int getValue()
		{
			return this.value;
		}
	}
	
	public static class TValue
	{
		private Value value;
		private int tt;
		
		public TValue(Value inValue, int inTt)
		{
			this.value = inValue;
			this.tt = inTt;
		}

		public Value getValue()
		{
			return this.value;
		}

		public int getTt()
		{
			return this.tt;
		}
	}
	
	public static class Proto
	{
		/*
		 * masks for new-style vararg
		 */
		private static final byte VARARG_HASARG = 1;
		private static final byte VARARG_ISVARARG = 2;
		private static final byte VARARG_NEEDSARG = 4;
		
		private TValue[] k;	//constants used by the function
		private Instruction[] code;
		private Proto[] p;	//functions defined inside the function
		private int[] lineInfo;	//map from opcodes to source lines
		private LocVar[] locVars;	//information about local variables
		private String[] upvalues;	//upvalue names
		private String[] source;
		private int sizeupvalues;
		private int linedefined;
		private int lastlinedefined;
		private byte nups;
		private byte numparams;
		private byte is_vararg;
		private byte maxstacksize;
		
		public Proto(TValue[] inK, Instruction[] inCode, Proto[] inP, int[] inLineInfo, LocVar[] inLocVars, String[] inUpvalues, String[] inSource, int inSizeupvalues, int inLinedefined, int inLastlinedefined, byte inNups, byte inNumparams, byte inIs_vararg, byte inMaxstacksize)
		{
			this.k = inK;
			this.code = inCode;
			this.p = inP;
			this.lineInfo = inLineInfo;
			this.locVars = inLocVars;
			this.upvalues = inUpvalues;
			this.source = inSource;
			this.sizeupvalues = inSizeupvalues;
			this.linedefined = inLinedefined;
			this.lastlinedefined = inLastlinedefined;
			this.nups = inNups;
			this.numparams = inNumparams;
			this.is_vararg = inIs_vararg;
			this.maxstacksize = inMaxstacksize;
		}

		public static byte getVarargHasarg()
		{
			return VARARG_HASARG;
		}

		public static byte getVarargIsvararg()
		{
			return VARARG_ISVARARG;
		}

		public static byte getVarargNeedsarg()
		{
			return VARARG_NEEDSARG;
		}

		public TValue[] getK()
		{
			return this.k;
		}

		public Instruction[] getCode()
		{
			return this.code;
		}

		public Proto[] getP()
		{
			return this.p;
		}

		public int[] getLineInfo()
		{
			return this.lineInfo;
		}

		public LocVar[] getLocVars()
		{
			return this.locVars;
		}

		public String[] getUpvalues()
		{
			return this.upvalues;
		}

		public String[] getSource()
		{
			return this.source;
		}

		public int getSizeupvalues()
		{
			return this.sizeupvalues;
		}

		public int getLinedefined()
		{
			return this.linedefined;
		}

		public int getLastlinedefined()
		{
			return this.lastlinedefined;
		}

		public byte getNups()
		{
			return this.nups;
		}

		public byte getNumparams()
		{
			return this.numparams;
		}

		public byte getIs_vararg()
		{
			return this.is_vararg;
		}

		public byte getMaxstacksize()
		{
			return this.maxstacksize;
		}
	}
	
	public static class LocVar
	{
		private String varname;
		private int startpc;	//first point where variable is active
		private int endpc;		//first point where variable is dead
		
		public LocVar(String inVarname, int inStartpc, int inEndpc)
		{
			this.varname = inVarname;
			this.startpc = inStartpc;
			this.endpc = inEndpc;
		}

		public String getVarname()
		{
			return this.varname;
		}

		public int getStartpc()
		{
			return this.startpc;
		}

		public int getEndpc()
		{
			return this.endpc;
		}
	}
	
	public static class UpVal
	{
		private CommonHeader header;
		private TValue v;	//points to stack or to itss own value
		
	}
}
