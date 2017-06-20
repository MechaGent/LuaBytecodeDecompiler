package LuaBytecodeDecompiler.LuacWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;

import HandyStuff.FileParser;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk01.v5_1.OpCodeMapper_v5_1;
import LuaBytecodeDecompiler.Mk02.Chunk;
import LuaBytecodeDecompiler.Mk03.AnnoFunction;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class wrapper
{
	private static final String generalPath = "C:/Users/MechaGent/Documents/Lua Decompiler/5.1.3/";
	private static final String luac_exe_path = generalPath + "luac.exe";
	private static final String luac_inputPath = generalPath + "input.lua";
	private static final String luac_outputPath = generalPath + "wackadoodle.out";
	
	public static void main(String[] args)
	{
		//test_remoteCompilation();
		//test_remoteCompilation_ThenReading();
		test_remoteCompilation_fromJava_ThenReading();
	}
	
	public static BytesStreamer compileLuaSourceToLuaBytecode(String source)
	{
		helper_test_remoteCompilation_fromJava_ThenReading_writeInputs(source);
		
		final Process process;
		final int result;
		
		try
		{
			process = new ProcessBuilder(luac_exe_path, "-o", luac_outputPath, luac_inputPath).start();
			result = process.waitFor();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			throw new IllegalArgumentException();
		}
		
		if(result == 0)
		{
			return BytesStreamer.getInstance(Paths.get(luac_outputPath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		}
		else
		{
			throw new IllegalArgumentException("failed to fetch compiled code");
		}
	}
	
	private static void test_remoteCompilation_fromJava_ThenReading()
	{
		final String testLuaCode;
		//testLuaCode = "foo:bar(\"baz\")";
		//testLuaCode = "foo.bar(foo, \"baz\")";
		//testLuaCode = "foo:bar(v)";
		//testLuaCode = "Account = {balance=0, withdraw = function (self, v) self.balance = selfbalance - v end } function Account:deposit(v) self.balance = self.balance + v end Account.deposit(Account, 200.00) Account:withdraw(100.00)";
		//testLuaCode = "local u; function p() return u end";
		//testLuaCode = "local u; function p(v) return u+v end";
		//testLuaCode = "local q = {1,2,3,4,5,}";
		//testLuaCode = "module(\"\", package.seeall)\r\nfunction foo() -- create it as if it's a global function\r\nprint(\"Hello World!\")\r\nend";
		testLuaCode = "local x = {value = 5} -- creating local table x containing one key,value of value,5\r\n\r\nlocal mt = {\r\n  __add = function (lhs, rhs) -- \"add\" event handler\r\n    return { value = lhs.value + rhs.value }\r\n  end\r\n}\r\n\r\nsetmetatable(x, mt) -- use \"mt\" as the metatable for \"x\"\r\n\r\nlocal y = x + x\r\n\r\nprint(y.value) --> 10  -- Note: print(y) will just give us the table code i.e table: <some tablecode>\r\n\r\nlocal z = y + y -- error, y doesn't have our metatable. this can be fixed by setting the metatable of the new object inside the metamethod";
		helper_test_remoteCompilation_fromJava_ThenReading_writeInputs(testLuaCode);
		
		try
		{
			final Process process = new ProcessBuilder(luac_exe_path, "-l", "-o", luac_outputPath, luac_inputPath).start();
			final int result = process.waitFor();
			
			if(result == 0)
			{
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
				
				String s = null;
				
				while((s = stdInput.readLine()) != null)
				{
					System.out.println(s);
				}
				
				final BytesStreamer out = BytesStreamer.getInstance(Paths.get(luac_outputPath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
				
				/*
				while(out.hasNextByte())
				{
					System.out.println(Integer.toHexString(out.getNextByte()));
				}
				*/
				
				final OpCodeMapper mapper = OpCodeMapper_v5_1.getInstance();
				
				final Chunk chunky = Chunk.parseNextChunk(out, mapper);
				
				//System.out.println(chunky.getRootFunction().disAssemble().toString());
				final AnnoFunction testOut = AnnoFunction.convert(chunky.getRootFunction());
				
				System.out.println(testOut.toString());
			}
			else
			{
				System.err.println("failed to fetch compiled code");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void helper_test_remoteCompilation_fromJava_ThenReading_writeInputs(String input)
	{
		final PrintWriter printy = FileParser.getPrintWriter(luac_inputPath);
		printy.println(input);
		printy.flush();
		printy.close();
	}
	
	@SuppressWarnings("unused")
	private static void test_remoteCompilation_ThenReading()
	{
		try
		{
			final Process process = new ProcessBuilder(luac_exe_path, "-o", luac_outputPath, luac_inputPath).start();
			final int result = process.waitFor();
			
			if(result == 0)
			{
				final BytesStreamer out = BytesStreamer.getInstance(Paths.get(luac_outputPath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
				
				while(out.hasNextByte())
				{
					System.out.println(Integer.toHexString(out.getNextByte()));
				}
			}
			else
			{
				System.err.println("failed to fetch compiled code");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private static void test_remoteCompilation()
	{
		try
		{
			final Process process = new ProcessBuilder(luac_exe_path, "-o", luac_outputPath, luac_inputPath).start();
			final int result = process.waitFor();
			final String output;
			
			if(result == 0)
			{
				output = "success";
			}
			else
			{
				output = "failure: " + result;
			}
			
			System.out.println(output);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
