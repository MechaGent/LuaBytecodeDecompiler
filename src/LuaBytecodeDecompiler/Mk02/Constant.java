package LuaBytecodeDecompiler.Mk02;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.MiscAutobot;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public abstract class Constant
{
	private static final NullConstant sharedNullConstant = new NullConstant();
	private static final NullConstant sharedUnknownConstant = new NullConstant();
	
	private final ConstantTypes type;

	public Constant(ConstantTypes inType)
	{
		this.type = inType;
	}

	public ConstantTypes getType()
	{
		return this.type;
	}

	@Override
	public abstract String toString();
	
	public abstract String getValAsString();
	public abstract int getValAsInt();
	public abstract boolean getValAsBool();
	public abstract double getValAsDouble();
	
	public static Constant parseNextConstant(BytesStreamer in, ChunkHeader header, OpCodeMapper mapper)
	{
		final Constant result;
		
		final int rawType = in.getNextByte() & 0xff;
		final ConstantTypes type = ConstantTypes.parse(rawType);
		
		switch(type)
		{
			case Null:
			{
				result = sharedNullConstant;
				break;
			}
			case Bool:
			{
				result = new BoolConstant(in);
				break;
			}
			case Num:
			{
				result = new NumConstant(in, header.getSize_luaNum());
				break;
			}
			case String:
			{
				result = StringConstant.getInstance(in, header.getSize_t());
				break;
			}
			case Unknown:
			{
				result = sharedUnknownConstant;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Unhandled Enum: " + type);
			}
		}
		
		return result;
	}
	
	public CharList disAssemble()
	{
		return new CharList(this.toString());
	}
	
	public static class NullConstant extends Constant
	{
		public NullConstant()
		{
			super(ConstantTypes.Null);
		}

		@Override
		public String toString()
		{
			return "NULL";
		}

		@Override
		public String getValAsString()
		{
			return "NULL";
		}

		@Override
		public int getValAsInt()
		{
			return -0;
		}

		@Override
		public boolean getValAsBool()
		{
			return false;
		}

		@Override
		public double getValAsDouble()
		{
			return Double.NaN;
		}
	}

	public static class BoolConstant extends Constant
	{
		private final boolean value;

		public BoolConstant(BytesStreamer in)
		{
			super(ConstantTypes.Bool);
			this.value = in.getNextByte() == 1;
		}

		public boolean getValue()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			final CharList result = new CharList();

			result.add("<Boolean> ");
			result.add(Boolean.toString(this.value));

			return result.toString();
		}

		@Override
		public String getValAsString()
		{
			return Boolean.toString(this.value);
		}

		@Override
		public int getValAsInt()
		{
			return this.value? 1 : 0;
		}

		@Override
		public boolean getValAsBool()
		{
			return this.value;
		}

		@Override
		public double getValAsDouble()
		{
			return this.value? 1 : 0;
		}
	}

	public static class NumConstant extends Constant
	{
		private final double value;

		public NumConstant(BytesStreamer in, int numSize)
		{
			super(ConstantTypes.Num);
			
			if(numSize == 8)
			{
			this.value = in.getNextDouble();
			}
			else if(numSize == 4)
			{
				this.value = in.getNextFloat();
			}
			else
			{
				throw new IllegalArgumentException("bad luaNum byteLength: " + numSize);
			}
		}

		public double getValue()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			final CharList result = new CharList();

			result.add("<Number> ");
			result.add(Double.toString(this.value));

			return result.toString();
		}

		@Override
		public String getValAsString()
		{
			return Double.toString(this.value);
		}

		@Override
		public int getValAsInt()
		{
			return (int) this.value;
		}

		@Override
		public boolean getValAsBool()
		{
			return this.value != 0;
		}

		@Override
		public double getValAsDouble()
		{
			return this.value;
		}
	}

	public static class StringConstant extends Constant
	{
		private final String value;

		private StringConstant(String inValue)
		{
			super(ConstantTypes.String);
			//this.value = MiscAutobot.parseNextLuaString(in, sizeTLength);
			this.value = inValue;
		}
		
		public static StringConstant getInstance(BytesStreamer in, int sizeTLength)
		{
			if(in.canSafelyConsume(sizeTLength))
			{
				final int length;
				
				switch(sizeTLength)
				{
					case 1:
					{
						length = in.getNextByte();
						break;
					}
					case 2:
					{
						length = in.getNextShort();
						break;
					}
					case 4:
					{
						length = in.getNextInt();
						break;
					}
					case 8:
					{
						final long test = in.getNextLong();
						length = (int) test;
						//System.out.println("result: " + (test == length));
						break;
					}
					default:
					{
						throw new IllegalArgumentException("need to write new case for size: " + sizeTLength);
					}
				}
				
				//System.out.println("parsing string of size " + length + " at pos " + Integer.toHexString(in.getPosition()));
				
				if(in.canSafelyConsume(length))
				{
					final String core = new String(in.getNextCharArr(length - 1, EndianSettings.LeftBitLeftByte));
					in.getNextByte();
					return new StringConstant(core);
				}
			}
			
			return null;
		}

		public String getValue()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			final CharList result = new CharList();

			result.add("<String> ");
			result.add(this.value);

			return result.toString();
		}

		@Override
		public String getValAsString()
		{
			return this.value;
		}

		@Override
		public int getValAsInt()
		{
			return Integer.parseInt(this.value);
		}

		@Override
		public boolean getValAsBool()
		{
			final boolean result;
			
			switch(this.value)
			{
				case "true":
				case "True":
				case "1":
				{
					result = true;
					break;
				}
				case "false":
				case "False":
				case "0":
				{
					result = false;
					break;
				}
				default:
				{
					throw new IllegalArgumentException(this.value);
				}
			}
			
			return result;
		}

		@Override
		public double getValAsDouble()
		{
			return Double.parseDouble(this.value);
		}
	}
}
