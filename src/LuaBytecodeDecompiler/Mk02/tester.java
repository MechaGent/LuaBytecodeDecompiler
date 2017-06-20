package LuaBytecodeDecompiler.Mk02;

import java.nio.file.Paths;

import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk01.v5_1.OpCodeMapper_v5_1;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class tester
{
	public static void main(String[] args)
	{
		final String generalBinPath = "C:/Users/MechaGent/Documents/Lua Decompiler/5.1.3/";
		final String binPath = generalBinPath + "targets/KubrowCharge.lua";
		//final String binPath = generalBinPath + "luac_ManyLocals.out";
		
		final BytesStreamer stream = BytesStreamer.getInstance(Paths.get(binPath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		final OpCodeMapper mapper = OpCodeMapper_v5_1.getInstance();
		
		final Chunk chunky = Chunk.parseNextChunk(stream, mapper);
		
		//System.out.println(chunky.disAssemble().toString());
		System.out.println(chunky.disAssemble(0).toString());
	}
}
