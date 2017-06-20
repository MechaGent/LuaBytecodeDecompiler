package LuaBytecodeDecompiler.Mk06_Working;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import CharList.CharList;
import HalfByteRadixMap.HalfByteRadixMap;
import HandyStuff.FileParser;
import LuaBytecodeDecompiler.Mk01.Abstract.Enums.AbstractOpCodes.OpCodeMapper;
import LuaBytecodeDecompiler.Mk01.v5_1.OpCodeMapper_v5_1;
import LuaBytecodeDecompiler.Mk02.Chunk;
import LuaBytecodeDecompiler.Mk02.Function;
import LuaBytecodeDecompiler.Mk06_Working.Enums.ScopeLevels;
import LuaBytecodeDecompiler.Mk06_Working.Vars.IrVar_Ref;
import Mk02.BufferedByteWriter;
import Mk02.DecompressorStream;
import Mk02.FileHeader;
import Random.XorShiftStar.Mk02.XorShiftStar;
import Random.XorShiftStar.Mk02.XorShiftStar.Generators;
import SingleLinkedList.Mk01.SingleLinkedList;
import Streams.BytesStreamer.EndianSettings;
import Streams.BytesStreamer.Mk03w.BytesStreamer;

public class Mk06Tester
{
	public static void main(String[] args)
	{
		test_disassembleSingleFile();
		// test_Translator2_simpleParseRaws();
		// test_Translator2_reinsertJumpLabels();
		//test_InstructionsToPseudoCode();
		// test_InstructionsCommonAncestorLookup();
		//stressTest_Decompiler();
	}

	private static void stressTest_Decompiler()
	{
		final DirectoryStream<Path> itsy;

		try
		{
			itsy = Files.newDirectoryStream(Paths.get("C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles"));
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException();
		}

		final OpCodeMapper mapper = OpCodeMapper_v5_1.getInstance();
		final PrintWriter printy = FileParser.getPrintWriter("C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/parsedLuaFiles.txt");
		int failed = 0;
		int total = 0;
		for (Path path : itsy)
		{
			//System.out.println("current path: " + path.toString());

			total++;
			
			final Chunk chunky = Chunk.parseNextChunk(BytesStreamer.getInstance(path, EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte), mapper);
			//test_InstructionsToPseudoCode(chunky.getRootFunction());
			
			printy.println("+++++New File: " + path.getFileName().toString());
			//System.out.println("currently testing: " + path.toString());
			printy.println(test_InstructionsToPseudoCode(chunky.getRootFunction()).toString());
			
			/*
			try
			{
				final Chunk chunky = Chunk.parseNextChunk(BytesStreamer.getInstance(path, EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte), mapper);
				//test_InstructionsToPseudoCode(chunky.getRootFunction());
				
				printy.println("+++++New File: " + path.getFileName().toString());
				printy.println(test_InstructionsToPseudoCode(chunky.getRootFunction()).toString());
			}
			catch (IndexOutOfBoundsException e)
			{
				System.out.println("(" + ++failed + "/" + total + ") parsing suffered an IndexOutOfBoundsException on: " + path.toString().replace('\\', '/'));
			}
			catch (BufferUnderflowException e)
			{
				System.out.println("(" + ++failed + "/" + total + ") parsing suffered an BufferUnderflowException on: " + path.toString().replace('\\', '/'));
			}
			catch (IllegalArgumentException e)
			{
				if(e.getMessage().equals("mismatched lookup!"))
				{
					System.out.println("(" + ++failed + "/" + total + ") parsing suffered an IllegalArgumentException on: " + path.toString().replace('\\', '/'));
				}
				else
				{
					throw new IllegalArgumentException();
				}
			}
			*/
		}
	}

	private static void test_InstructionsCommonAncestorLookup()
	{
		final SingleLinkedList<Function> queue = new SingleLinkedList<Function>();
		final HalfByteRadixMap<IrVar> Globals = new HalfByteRadixMap<IrVar>();
		queue.add(helper_getTestFunction());
		final CharList out = new CharList();
		final XorShiftStar randy = XorShiftStar.getNewInstance(Generators.XorShiftStar1024);

		while (!queue.isEmpty())
		{
			final Function current = queue.pop();

			for (Function func : current.getFunctionPrototypes())
			{
				queue.add(func);
			}

			final Translator2 curTest = new Translator2(current, Globals);

			out.addNewLine();
			out.add("*****New Test Beginning*****");
			final SingleLinkedList<IrInstruction> parsed = curTest.parseAndTidyUp();
			final int index1 = randy.nextInt(parsed.getSize());
			final int index2 = randy.nextInt(parsed.getSize());
			final IrInstruction check1 = parsed.getAt(index1);
			final IrInstruction check2 = parsed.getAt(index2);
			final IrInstruction check3 = check1.getFirstCommonInstructionWith(check2);
			out.addNewLine();
			out.add("check1: ");
			out.add(check1.mangledName);
			out.addNewLine();
			out.add("check2: ");
			out.add(check2.mangledName);
			out.addNewLine();
			out.add("check3: ");

			if (check3 != null)
			{
				out.add(check3.mangledName);
			}
			else
			{
				out.add("null result");
			}

			out.addNewLine();
			out.addNewLine();
			out.add("full listing: ");
			final Stringifier stringy = new Stringifier();
			out.addNewLine();
			// out.add(stringy.translateAllInstructions(parsed), true);

			while (!parsed.isEmpty())
			{
				out.addNewLine();
				out.add(parsed.pop().toCharList(false, false), true);
				// out.add(stringy.translateNextInstruction(in));
			}

			out.addNewLine();
			out.add("****Test Ending*****");
		}

		System.out.println(out.toString());
	}

	private static void test_InstructionsToPseudoCode()
	{
		System.out.println(test_InstructionsToPseudoCode(helper_getTestFunction()).toString());
	}

	private static CharList test_InstructionsToPseudoCode(Function in)
	{
		final SingleLinkedList<Function> queue = new SingleLinkedList<Function>();
		final HalfByteRadixMap<IrVar> Globals = new HalfByteRadixMap<IrVar>();
		queue.add(in);
		final CharList out = new CharList();

		//System.out.println("currently parsing: " + in.getFunctionName());
		
		while (!queue.isEmpty())
		{
			final Function current = queue.pop();
			
			for (Function func : current.getFunctionPrototypes())
			{
				queue.add(func);
			}
			
			//System.out.println("numUpvalues: " + current.getNumUpvalues());

			final Translator2 curTest = new Translator2(current, Globals);

			out.addNewLine();
			final Stringifier stringy = new Stringifier();
			out.add("*****");
			out.add(stringy.translateAllInstructions(curTest.parseAndTidyUp()), true);
		}

		//System.out.println(out.toString());
		System.out.println("end of diagnostics");
		return out;
	}

	private static void test_Translator2_reinsertJumpLabels()
	{
		final SingleLinkedList<Function> queue = new SingleLinkedList<Function>();
		final HalfByteRadixMap<IrVar> Globals = new HalfByteRadixMap<IrVar>();
		queue.add(helper_getTestFunction());
		final CharList out = new CharList();

		while (!queue.isEmpty())
		{
			final Function current = queue.pop();

			for (Function func : current.getFunctionPrototypes())
			{
				queue.add(func);
			}

			// final IrVar[] Locals = generateBankOfVars(current.getLocalsDebugData().length, "LocalVar_", ScopeLevels.Local_Variable);
			// final IrVar[] Constants = generateBankOfVars(current.getConstants().length, "Constant_", ScopeLevels.Local_Constant);
			// final IrVar[] Upvalues = generateBankOfVars(current.getNumUpvalues(), "Upvalue_", ScopeLevels.UpValue);

			// final Translator2 curTest = new Translator2(current.getInstructions(), Locals, Constants, Upvalues, Globals);
			final Translator2 curTest = new Translator2(current, Globals);
			final SingleLinkedList<IrInstruction> roughDraft = curTest.parseAndTidyUp();

			out.addNewLine();
			out.add("*****StartListingForFunction ");
			out.add(current.getFunctionName());
			out.add(':');

			for (IrInstruction curInst : roughDraft)
			{
				out.addNewLine();
				out.add(curInst.toCharList(false, false), true);
				// out.add(curInst.toCharList(false, false), true);
			}

			out.addNewLine();
			out.add("*****End");
		}

		System.out.println(out.toString());
	}

	private static void test_Translator2_simpleParseRaws()
	{
		final SingleLinkedList<Function> queue = new SingleLinkedList<Function>();
		final HalfByteRadixMap<IrVar> Globals = new HalfByteRadixMap<IrVar>();
		queue.add(helper_getTestFunction());
		final CharList out = new CharList();

		while (!queue.isEmpty())
		{
			final Function current = queue.pop();

			for (Function func : current.getFunctionPrototypes())
			{
				queue.add(func);
			}

			// final IrVar[] Locals = generateBankOfVars(current.getLocalsDebugData().length, "LocalVar_", ScopeLevels.Local_Variable);
			// final IrVar[] Constants = generateBankOfVars(current.getConstants().length, "Constant_", ScopeLevels.Local_Constant);
			// final IrVar[] Upvalues = generateBankOfVars(current.getNumUpvalues(), "Upvalue_", ScopeLevels.UpValue);

			// final Translator2 curTest = new Translator2(current.getInstructions(), Locals, Constants, Upvalues, Globals);
			final Translator2 curTest = new Translator2(current, Globals);
			final SingleLinkedList<IrInstruction> roughDraft = curTest.simpleParseRaws();

			out.addNewLine();
			out.add("*****Start:");

			for (IrInstruction curInst : roughDraft)
			{
				out.addNewLine();
				out.add(curInst.toCharList(true, false), true);
			}

			out.addNewLine();
			out.add("*****End");
		}

		System.out.println(out.toString());
	}
	
	private static void test_disassembleSingleFile()
	{
		final Function test = helper_getTestFunction();
		System.out.println(test.disAssemble().toString());
	}

	private static IrVar[] generateBankOfVars(int length, String labelPrefix, ScopeLevels level)
	{
		final IrVar[] result = new IrVar[length];

		for (int i = 0; i < length; i++)
		{
			result[i] = new IrVar_Ref(labelPrefix + i, level, false);
		}

		return result;
	}

	private static BytesStreamer[] getAllLuaFiles()
	{
		final File[] files = new File("C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles").listFiles();
		// System.out.println("length: " + files.length);
		final BytesStreamer[] result = new BytesStreamer[files.length];
		// If this pathname does not denote a directory, then listFiles() returns null.

		for (int i = 0; i < files.length; i++)
		{
			final File file = files[i];
			result[i] = BytesStreamer.getInstance(file.toPath());
		}

		return result;
	}

	private static void helper_updateAllLuaFiles()
	{
		final String genFilePath = "C:/Steam/steamapps/common/Warframe/Cache.Windows/";
		final String cacheTocName = "B.Font";
		final String genHeadersPath = genFilePath + cacheTocName;
		final String tocPath = genHeadersPath + ".toc";
		final BytesStreamer tocStream = BytesStreamer.getInstance(Paths.get(tocPath), EndianSettings.LeftBitLeftByte, EndianSettings.LeftBitLeftByte);

		final String[] suffixes = new String[] {
													"lua" };

		final FileHeader[] headers = FileHeader.parseAllWithSelectedSuffix(suffixes, tocStream, false);
		final String outputPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/" + "LuaFiles/";

		for (int i = 0; i < headers.length; i++)
		{
			final FileHeader cur = headers[i];
			final String curPath = outputPath + cur.getFileName();// + ".body";
			final DecompressorStream pump = DecompressorStream.getInstance(genHeadersPath + ".cache", cur);
			final BufferedByteWriter writey = new BufferedByteWriter(100, curPath);

			while (!pump.isDone())
			{
				writey.write(pump.getNextByte());
			}

			writey.flush();
			writey.close();
		}
	}

	private static Function helper_getTestFunction()
	{
		final String generalBinPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/";
		final String binPath;
		// final String generalBinPath = "C:/Users/wrighc4/Documents/Eclipse tempWorkspace/Lua Decompiler/";
		//binPath = generalBinPath + "KubrowCharge.lua";
		//binPath = generalBinPath + "luac_ManyLocals.out";
		//binPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/AbilitySelector.lua";
		//binPath = "C:/Users/MechaGent/Documents/GAME STUFF/Warframe/DataOut/StreamedDump/out/LuaFiles/AddAscarisNegator.lua";
		//binPath = generalBinPath + "ApexFuntions.lua";
		binPath = generalBinPath + "Background.lua";

		final BytesStreamer stream = BytesStreamer.getInstance(Paths.get(binPath), EndianSettings.LeftBitRightByte, EndianSettings.LeftBitLeftByte);
		final OpCodeMapper mapper = OpCodeMapper_v5_1.getInstance();

		final Chunk chunky = Chunk.parseNextChunk(stream, mapper);

		return chunky.getRootFunction();
	}
}
