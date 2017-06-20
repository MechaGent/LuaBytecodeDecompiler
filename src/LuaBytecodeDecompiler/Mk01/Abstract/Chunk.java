package LuaBytecodeDecompiler.Mk01.Abstract;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.DisAssemblable;
import LuaBytecodeDecompiler.Mk01.Abstract.FunctionBlock.RootFunction;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk01.Enums.LuaVersions;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class Chunk implements DisAssemblable
{
	private final Header header;
	private final RootFunction rootFunction;
	
	public Chunk(BytesStreamer in, OpCodeMapper mapper)
	{
		this.header = new Header(in);
		System.out.println(this.header.toString());
		this.rootFunction = RootFunction.parseNextRootFunction(in, this.header.SizeTLength, this.header.luaNumLength, mapper);
	}
	
	private static class Header
	{
		private final int signature;
		private final LuaVersions version;
		private final byte format;	//0 == official, apparently
		private final EndianSettings endianness;
		
		/*
		 * Lengths are in bytes
		 */
		private final byte IntLength;
		private final byte SizeTLength;
		private final byte InstructionLength;
		private final byte luaNumLength;
		private final boolean luaNumsAreIntegers;
		
		/*
		 * default header for x86 platform:
		 * 1B 4C 75 61
		 * 51 00 01 04
		 * 04 04 08 00
		 */
		public Header(BytesStreamer in)
		{
			this.signature = in.getNextInt();
			this.version = LuaVersions.getVersion(in.getNextByte());
			this.format = (byte) in.getNextByte();
			this.endianness = (in.getNextByte() == 0)? EndianSettings.LeftBitLeftByte : EndianSettings.LeftBitRightByte;	//0 == big, 1 == little, 1 is default
			this.IntLength = (byte) in.getNextByte();	//default 4
			this.SizeTLength = (byte) in.getNextByte();	//default 4
			this.InstructionLength = (byte) in.getNextByte(); //default 4
			this.luaNumLength =(byte)  in.getNextByte();	//default 8
			this.luaNumsAreIntegers = in.getNextByte() == 1;
			
		}

		@Override
		public String toString()
		{
			return "Header [signature=" + this.signature + ", version=" + this.version + ", format=" + this.format + ", endianness=" + this.endianness + ", IntLength=" + this.IntLength + ", SizeTLength=" + this.SizeTLength + ", InstructionLength=" + this.InstructionLength + ", luaNumLength=" + this.luaNumLength + ", luaNumsAreIntegers=" + this.luaNumsAreIntegers + "]";
		}
	}

	public CharList disAssemble()
	{
		return this.disAssemble(0, "");
	}

	public CharList disAssemble(int offset)
	{
		return this.disAssemble(offset, "");
	}

	public CharList disAssemble(String label)
	{
		return this.disAssemble(0, label);
	}

	@Override
	public CharList disAssemble(int inOffset, String inLabel)
	{
		return this.rootFunction.disAssemble(0, "rootFunction");
	}
}
