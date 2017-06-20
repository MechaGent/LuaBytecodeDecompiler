package LuaBytecodeDecompiler.Mk01.LuaJTesting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.luaj.vm2.Print;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.LuaC;

import DataStructures.Linkages.CharList.Mk03.CharList;
import HandyStuff.BitTwiddling;
import HandyStuff.MiscToStrings;
import LuaBytecodeDecompiler.Mk01.Instruction;
import LuaBytecodeDecompiler.Mk01.Instruction.RawInstruction_iABC;
import LuaBytecodeDecompiler.Mk01.Instruction.RawInstruction_iABx;
import LuaBytecodeDecompiler.Mk01.Instruction.RawInstruction_iAsBx;
import LuaBytecodeDecompiler.Mk01.Abstract.Chunk;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk01.v5_1.OpCodeMapper_v5_1;
import LuaBytecodeDecompiler.Mk02.Instructions.InstructionTypes;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;
import Streams.BytesStreamer.Mk03w.StringParser;

public class tester
{
	public static void main(String[] args)
	{
		// testFromStringToCompile_Formatted();
		//testFromStringToCompiledByteCode();
		//test_BytesStreamerFunctionality();
		test_compareByteCodes();
		//test_canParseCompiledBytecode();
		
		//System.out.println(Integer.toHexString(0b11110));
	}

	private static InputStream convertStringToInputStream(String raw)
	{
		return new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8));
	}

	private static int[] compileFromRawString(String raw)
	{
		return compileFromStream(convertStringToInputStream(raw));
	}

	private static int[] compileFromStream(InputStream in)
	{
		try
		{
			final Prototype p = LuaC.instance.compile(in, "proto");

			Print.print(p);
			// System.out.println(proto.toString()); //this line implies that hexOut should be 0x3A 0x00 0x2D 0x0

			//Globals globals = JsePlatform.standardGlobals();
			//Prototype p = globals.compilePrototype(new StringReader("print('hello, world')"), "main.lua");
			ByteArrayOutputStream o = new ByteArrayOutputStream();
			org.luaj.vm2.compiler.DumpState.dump(p, o, true);
			//o.flush();
			byte[] lua_binary_file_bytes = o.toByteArray();
			final int[] result = new int[lua_binary_file_bytes.length / 4];
			
			for(int i = 0; i < lua_binary_file_bytes.length; i++)
			{
				if((i-1)%4 == 0)
				{
					//System.out.println();
				}
				
				System.out.println("0x" + MiscToStrings.toHexString(lua_binary_file_bytes[i]) + ",\t");
			}
			
			System.out.println();
			
			for(int i = 0; i < result.length; i++)
			{
				int index = i * 4;
				result[i] = BitTwiddling.parseInt(EndianSettings.LeftBitRightByte, lua_binary_file_bytes[index], lua_binary_file_bytes[index+1], lua_binary_file_bytes[index+2], lua_binary_file_bytes[index+3]);
				//System.out.println(Integer.toHexString(result[i]));
			}

			return result;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
	}

	private static CharList toCharList(int[] compiled)
	{
		final CharList result = new CharList();

		for (int i = 0; i < compiled.length; i++)
		{
			result.addNewLine();
			result.add(MiscToStrings.toHexString(compiled[i]));
		}

		return result;
	}
	
	@SuppressWarnings("unused")
	private static void test_canParseCompiledBytecode()
	{
		final int[] raw = compileFromRawString(TestCases.test1.getCode());
		final BytesStreamer stream = BytesStreamer.getInstance(raw, EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte, StringParser.getInstance_delimitedStringParser((char) 0));
		final OpCodeMapper mapper = OpCodeMapper_v5_1.getInstance();
		
		final Chunk testChunk = new Chunk(stream, mapper);
		
		System.out.println(testChunk.disAssemble().toString());
	}
	
	private static void test_compareByteCodes()
	{
		compileFromRawString("do end");
		compileFromRawString("print( 'hello, world' )");
	}
	
	@SuppressWarnings("unused")
	private static void test_BytesStreamerFunctionality()
	{
		final byte[] test = new byte[]{0x10, 0x11, 0x13, 0x20, 0x54, 0x72, 0x19, 0x66};
		
		final BytesStreamer stream = BytesStreamer.getInstance(test, EndianSettings.LeftBitLeftByte, EndianSettings.LeftBitLeftByte);
		
		while(stream.hasNextByte())
		{
			System.out.println(Integer.toHexString(stream.getNextInt()));
		}
	}

	@SuppressWarnings("unused")
	private static void testFromStringToCompiledByteCode()
	{
		// final String test = "print( 'hello, world' )";
		final String test = "do end";
		final int[] raw = compileFromRawString(test);
		final OpCodeMapper mapper = OpCodeMapper_v5_1.getInstance();

		for (int i = raw.length - 1; i < raw.length; i++)
		{
			final int ByteCode = raw[i] & 0x3f;
			final AbstractOpCodes OpType = mapper.getOpCodeFor(ByteCode);
			final InstructionTypes instType = mapper.getTypeOf(OpType);
			final Instruction curInstruct;

			switch (instType)
			{
				case iABC:
				{
					curInstruct = RawInstruction_iABC.parseFromRaw(raw[i], mapper);
					break;
				}
				case iABx:
				{
					curInstruct = RawInstruction_iABx.parseFromRaw(raw[i], mapper);
					break;
				}
				case iAsBx:
				{
					curInstruct = RawInstruction_iAsBx.parseFromRaw(raw[i], mapper);
					break;
				}
				default:
				{
					throw new IllegalArgumentException();
				}
			}

			//System.out.println(curInstruct.toString() + "\r\n\r\n");
		}
	}

	@SuppressWarnings("unused")
	private static void testFromStringToCompile_Formatted()
	{
		final String test = "print( 'hello, world' )";
		// final String test = "do end";
		final int[] raw = compileFromRawString(test);
		System.out.println(toCharList(raw).toString());
	}

	@SuppressWarnings("unused")
	private static void testFromStringToCompile()
	{
		final String testLuaString = "print( 'hello, world' )";
		final String examplePath = "H:/Users/Thrawnboo/workspace - Java - Compiled/Compiled Libraries/Not Mine/LuaJ/Examples/lua/hello.lua";
		final FileInputStream stream;

		try
		{
			stream = new FileInputStream(examplePath);

			final Prototype bucket = LuaC.instance.compile(stream, "");

			stream.close();
			System.out.println(Arrays.toString(bucket.code));
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
			throw new IllegalArgumentException();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
