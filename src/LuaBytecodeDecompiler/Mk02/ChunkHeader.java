package LuaBytecodeDecompiler.Mk02;

import DataStructures.Linkages.CharList.Mk03.CharList;
import LuaBytecodeDecompiler.Mk01.Enums.LuaVersions;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class ChunkHeader
{
	private final int signature;
	private final LuaVersions version;
	private final byte Format;
	private final EndianSettings SigEnd;
	private final int size_Int;
	private final int size_t;
	private final int size_Instruction;
	private final int size_luaNum;
	private final boolean luaNumsAreFloats;

	private ChunkHeader(int inSignature, LuaVersions inVersion, byte inFormat, EndianSettings inSigEnd, int inSize_Int, int inSize_t, int inSize_Instruction, int inSize_luaNum, boolean inLuaNumsAreFloats)
	{
		this.signature = inSignature;
		this.version = inVersion;
		this.Format = inFormat;
		this.SigEnd = inSigEnd;
		this.size_Int = inSize_Int;
		this.size_t = inSize_t;
		this.size_Instruction = inSize_Instruction;
		this.size_luaNum = inSize_luaNum;
		this.luaNumsAreFloats = inLuaNumsAreFloats;
	}

	public static ChunkHeader parseNextChunkHeader(BytesStreamer in)
	{
		final int inSignature = in.getNextInt();
		final LuaVersions inVersion = LuaVersions.getVersion((byte) in.getNextByte());
		final byte inFormat = (byte) in.getNextByte();
		final EndianSettings inSigEnd = (in.getNextByte() == 1)? EndianSettings.LeftBitRightByte : EndianSettings.LeftBitLeftByte;
		final int inSize_Int = in.getNextByte();
		final int inSize_t = in.getNextByte();
		final int inSize_Instruction = in.getNextByte();
		final int inSize_luaNum = in.getNextByte();
		final boolean inLuaNumsAreFloats = in.getNextByte() == 0;
		final ChunkHeader result = new ChunkHeader(inSignature, inVersion, inFormat, inSigEnd, inSize_Int, inSize_t, inSize_Instruction, inSize_luaNum, inLuaNumsAreFloats);
		
		//System.out.println(result.toString());
		
		return result;
	}

	public int getSignature()
	{
		return this.signature;
	}

	public LuaVersions getVersion()
	{
		return this.version;
	}

	public byte getFormat()
	{
		return this.Format;
	}

	public EndianSettings getSigEnd()
	{
		return this.SigEnd;
	}

	public int getSize_Int()
	{
		return this.size_Int;
	}

	public int getSize_t()
	{
		return this.size_t;
	}

	public int getSize_Instruction()
	{
		return this.size_Instruction;
	}

	public int getSize_luaNum()
	{
		return this.size_luaNum;
	}

	public boolean isLuaNumsAreFloats()
	{
		return this.luaNumsAreFloats;
	}
	
	public CharList disAssemble()
	{
		final CharList result = new CharList();
		
		result.add("***Chunk Header Start***");
		result.addNewLine();
		
		result.add("Signature: ");
		result.add(Integer.toHexString(this.signature));
		result.addNewLine();
		
		result.add("version: ");
		result.add(this.version.toString());
		result.addNewLine();
		
		result.add("Format: ");
		result.add(Integer.toHexString(this.Format));
		result.addNewLine();
		
		result.add("SigEnd: ");
		result.add(this.SigEnd.toString());
		result.addNewLine();
		
		result.add("size_Integer: ");
		result.add(Integer.toString(this.size_Int));
		result.addNewLine();
		
		result.add("size_t: ");
		result.add(Integer.toString(this.size_t));
		result.addNewLine();
		
		result.add("size_Instruction: ");
		result.add(Integer.toString(this.size_Instruction));
		result.addNewLine();
		
		result.add("size_luaNum: ");
		result.add(Integer.toString(this.size_luaNum));
		result.addNewLine();
		
		result.add("luaNumsAreFloats: ");
		result.add(Boolean.toString(this.luaNumsAreFloats));
		result.addNewLine();
		
		result.add("***Chunk Header End***");
		
		return result;
	}

	@Override
	public String toString()
	{
		return "ChunkHeader [signature=" + this.signature + ", version=" + this.version + ", Format=" + this.Format + ", SigEnd=" + this.SigEnd + ", size_Int=" + this.size_Int + ", size_t=" + this.size_t + ", size_Instruction=" + this.size_Instruction + ", size_luaNum=" + this.size_luaNum + ", luaNumsAreFloats=" + this.luaNumsAreFloats + "]";
	}
}
