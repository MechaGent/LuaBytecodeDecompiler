package LuaBytecodeDecompiler.Mk05;

import CustomExceptions.UnhandledEnum.Mk01.UnhandledEnumException;
import DataStructures.Linkages.CharList.Mk03.CharList;
import HalfByteRadixMap.HalfByteRadixMap;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.BoolBrancher;
import LuaBytecodeDecompiler.Mk03.AnnoInstructions.ABC.AnnoInstruction_TestSet;
import LuaBytecodeDecompiler.Mk04.CodeBlock;
import LuaBytecodeDecompiler.Mk04.CodeBlockTags;

public class Interpreter
{
	private final HalfByteRadixMap<String> visitedItNums;

	public Interpreter()
	{
		this.visitedItNums = new HalfByteRadixMap<String>();
	}

	public String recompose(CodeBlock entry)
	{
		// System.out.println("*****\r\n" + entry.toCharList(true).toString());

		final parseResults parsedFirst = parseNextLinear(entry);

		SimpleBlock firstResult = parsedFirst.resultingFirst;
		SimpleBlock currentResult = parsedFirst.resultingCurrent;
		CodeBlock current = entry;

		while (!current.isNull() && current.getTag() != CodeBlockTags.Return && current.trueNextIsAdvancing())
		{
			current = current.getTrueNext();
			// System.out.println("parsing: " + current.getMangledName());
			final parseResults parsed = parseNextLinear(current);

			if(currentResult.rawLines == parsed.resultingFirst.rawLines)
			{
				throw new IllegalArgumentException();
			}
			
			currentResult.nextLeft = parsed.resultingFirst;

			currentResult = parsed.resultingCurrent;
			current = parsed.current;
		}

		return firstResult.toPseudoJava(0).toString();
		//return firstResult.toString();
	}

	private parseResults parseNextLinear(CodeBlock in)
	{
		/*
		if (in.getTag() == CodeBlockTags.ItNum)
		{
			if (visitedItNums.contains(in.getBlockNum()))
			{
				in = in.getFalseNext();
			}
			else
			{
				visitedItNums.put(in.getBlockNum(), "");
			}
		}
		*/

		// System.out.println("in 1arg linear, on: " + in.getMangledName() + " and next of: " + in.getTrueNext().getMangledName());

		CodeBlock currentCodeBlock = in;
		SimpleBlock firstSimpleBlock = null;
		SimpleBlock currentSimpleBlock = SimpleBlock.NullBlock;
		boolean isOnFirst = true;

		final SimpleBlock locSimpleBlock;

		switch(currentCodeBlock.getTag())
		{
			case Test:
			{
				final CodeBlock stopper = currentCodeBlock.getTrueNext().findLowestCommonAncestorWith(currentCodeBlock.getFalseNext());

				//final parseResults parsed;
				final parseResults parsed = this.parseNextBranch(currentCodeBlock, stopper);

				if (!stopper.isNull())
				{
					//parsed = this.parseNextBranch(currentCodeBlock, stopper);
				}
				else
				{
					//parsed = parseResults.NullResult;
					//parsed = this.parseNextTerminatingLinear(in);
				}

				locSimpleBlock = parsed.resultingFirst;

				if (isOnFirst)
				{
					isOnFirst = false;
					firstSimpleBlock = locSimpleBlock;
				}
				
				currentSimpleBlock = parsed.resultingCurrent;

				currentCodeBlock = stopper;
				
				if(currentSimpleBlock.isNull())
				{
					throw new IllegalArgumentException();
				}
				
				break;
			}
			case InNum:
			{
				final parseResults parsed = this.parseNextLoop(in);
				
				locSimpleBlock = parsed.resultingFirst;

				if (isOnFirst)
				{
					isOnFirst = false;
					firstSimpleBlock = locSimpleBlock;
				}
				
				currentSimpleBlock = parsed.resultingCurrent;
				
				currentCodeBlock = parsed.current;

				break;
			}
			default:
			{
				// System.out.println("here");
				locSimpleBlock = new LinearSimpleBlock(currentCodeBlock);
				break;
			}
		}

		if (isOnFirst)
		{
			isOnFirst = false;
			firstSimpleBlock = locSimpleBlock;
		}

		currentSimpleBlock = locSimpleBlock;

		/*
		if (!currentCodeBlock.isNull() && currentCodeBlock.getTrueNext().getTag() == CodeBlockTags.InNum)
		{
			// final CodeBlock var1 = currentCodeBlock.getTrueNext();
		
			System.out.println(currentCodeBlock.getTrueNext().getMangledName() + ", " + currentCodeBlock.getTrueNext().getTrueNext().getMangledName());
		}
		*/

		if (currentCodeBlock.getTag() == CodeBlockTags.ItNum)
		{
			if (visitedItNums.contains(currentCodeBlock.getBlockNum()))
			{
				currentCodeBlock = currentCodeBlock.getFalseNext();
			}
			else
			{
				visitedItNums.put(currentCodeBlock.getBlockNum(), "");
			}
		}

		return new parseResults(firstSimpleBlock, currentSimpleBlock, currentCodeBlock);
	}

	private parseResults parseNextLinear(CodeBlock in, CodeBlock stopper)
	{
		/*
		if (in.getTag() == CodeBlockTags.ItNum)
		{
			if (visitedItNums.contains(in.getBlockNum()))
			{
				in = in.getFalseNext();
			}
			else
			{
				visitedItNums.put(in.getBlockNum(), "");
			}
		}
		*/

		// System.out.println("in 2arg linear, on: " + in.getMangledName() + ", with stopper: " + stopper.getMangledName());

		CodeBlock current = in;
		final SimpleBlock firstResult;
		SimpleBlock currentResult;

		final parseResults currentFirstResults = parseNextLinear(in);
		firstResult = currentFirstResults.resultingFirst;
		currentResult = currentFirstResults.resultingCurrent;
		int lastBlockNum = in.getBlockNum();

		if (!currentFirstResults.current.isNull())
		{
			current = currentFirstResults.current.getTrueNext();

			while (current != stopper && current.getBlockNum() > lastBlockNum)
			{
				final parseResults currentResults = parseNextLinear(current);
				
				if(currentResult.rawLines == currentResults.resultingFirst.rawLines)
				{
					throw new IllegalArgumentException();
				}
				
				currentResult.nextLeft = currentResults.resultingFirst;
				currentResult = currentResults.resultingCurrent;
				current = currentResults.current.getTrueNext();
			}
		}

		return new parseResults(firstResult, currentResult, current);
	}
	
	private parseResults parseNextLoop(CodeBlock in)
	{
		final CodeBlock rawIterator = in.getTrueNext();
		final CodeBlock innerLoop = rawIterator.getTrueNext();
		final CodeBlock firstAfterLoop = rawIterator.getFalseNext();
		
		final parseResults parsedInnerLoop = this.parseNextLinear(innerLoop, rawIterator);
		final parseResults parsedAfterLoop = this.parseNextLinear(firstAfterLoop);
		
		final BranchingSimpleBlock_For result = new BranchingSimpleBlock_For(in, rawIterator, parsedInnerLoop.resultingFirst, parsedAfterLoop.resultingFirst);
		
		return new parseResults(result, parsedAfterLoop.resultingCurrent, parsedAfterLoop.current);
	}

	private parseResults parseNextBranch(CodeBlock in, CodeBlock stopper)
	{
		final parseResults result;

		if (!stopper.isNull())
		{
			if (stopper.getBlockNum() <= in.getBlockNum())
			{
				throw new IllegalArgumentException("stopper: " + stopper.getMangledName() + " in: " + in.getMangledName());
			}

			final parseResults parsedIf;
			final parseResults parsedElse;
			boolean indirectlyNull_If = false;
			boolean indirectlyNull_Else = false;

			final parseResults parsedShared;
			
			if(stopper.getTag() == CodeBlockTags.ItNum)
			{
				parsedShared = parseNextLinear(stopper.getFalseNext());
			}
			else
			{
				System.out.println("parsing stopper: " + stopper.getMangledName());
				
				if(stopper.trueNextIsAdvancing())
				{
					parsedShared = parseNextLinear(stopper);
				}
				else
				{
					
				}
			}

			if (in.getTrueNext() != stopper)
			{
				parsedIf = parseNextLinear(in.getTrueNext(), stopper);
				indirectlyNull_If = parsedIf.resultingFirst.isNull();
			}
			else
			{
				parsedIf = parsedShared;
				// parsedIf = parseResults.NullResult;
			}

			if (in.getFalseNext() != stopper)
			{
				parsedElse = parseNextLinear(in.getFalseNext(), stopper);
				indirectlyNull_Else = parsedElse.resultingFirst.isNull();
				
				//System.out.println("triggering on: " + in.getMangledName() + " with first result: " + parsedElse.resultingCurrent.rawLines.getMangledName());
			}
			else
			{
				parsedElse = parsedShared;
				// parsedElse = parseResults.NullResult;
			}

			if (parsedIf.resultingFirst.isNull() && parsedElse.resultingFirst.isNull())
			{
				if (indirectlyNull_If && indirectlyNull_Else)
				{
					throw new IllegalArgumentException();
				}
				else
				{
					System.out.println("if: " + indirectlyNull_If + ", else: " + indirectlyNull_Else);
				}
			}

			final BranchingSimpleBlock_If firstResult = new BranchingSimpleBlock_If(in, parsedIf.resultingFirst, parsedElse.resultingFirst);

			if (!firstResult.ifBlock.isNull())
			{
				if(firstResult.ifBlock.rawLines == parsedShared.resultingFirst.rawLines)
				{
					firstResult.ifBlock = SimpleBlock.NullBlock;
				}
				else
				{
					firstResult.ifBlock.nextLeft = parsedShared.resultingFirst;
				}
			}

			if (!firstResult.elseBlock.isNull())
			{
				if(firstResult.elseBlock.rawLines == parsedShared.resultingFirst.rawLines)
				{
					firstResult.elseBlock = SimpleBlock.NullBlock;
				}
				else
				{
					firstResult.elseBlock.nextLeft = parsedShared.resultingFirst;
				}
			}

			result = new parseResults(firstResult, parsedShared.resultingCurrent, parsedShared.current);
		}
		else
		{
			final SimpleBlock firstIf;

			if (!in.getTrueNext().isNull() && in.trueNextIsAdvancing())
			{
				firstIf = parseNextTerminatingLinear(in.getTrueNext());
			}
			else
			{
				firstIf = SimpleBlock.NullBlock;
			}

			final SimpleBlock firstElse;

			if (in.hasFalseNext() && in.falseNextIsAdvancing())
			{
				firstElse = parseNextTerminatingLinear(in.getFalseNext());
			}
			else
			{
				firstElse = SimpleBlock.NullBlock;
			}

			final BranchingSimpleBlock_If term = new BranchingSimpleBlock_If(in, firstIf, firstElse);

			result = new parseResults(term, term, CodeBlock.getNullCodeBlock());
		}

		return result;
	}

	private parseResults parseNextBranch2(CodeBlock in, CodeBlock stopper)
	{
		if (in.getTag() == CodeBlockTags.ItNum)
		{
			if (visitedItNums.contains(in.getBlockNum()))
			{
				in = in.getFalseNext();
			}
			else
			{
				visitedItNums.put(in.getBlockNum(), "");
			}
		}

		System.out.println("in 2arg branch, on: " + in.getMangledName());

		if (!stopper.isNull())
		{
			final parseResults parsedIf;

			if (in.getTrueNext() != stopper)
			{
				parsedIf = parseNextLinear(in.getTrueNext(), stopper);
			}
			else
			{
				parsedIf = new parseResults(null, null, null);
			}

			final parseResults parsedShared = parseNextLinear(stopper);
			parsedIf.resultingCurrent.nextLeft = parsedShared.resultingFirst;
			final BranchingSimpleBlock_If resultFirst;

			if (in.getFalseNext() != stopper)
			{
				final parseResults parsedElse = parseNextLinear(in.getFalseNext(), stopper);
				parsedElse.resultingCurrent.nextLeft = parsedShared.resultingFirst;
				resultFirst = new BranchingSimpleBlock_If(in, parsedIf.resultingFirst, parsedElse.resultingFirst);
			}
			else
			{
				resultFirst = new BranchingSimpleBlock_If(in, parsedIf.resultingFirst, null);
			}

			return new parseResults(resultFirst, parsedShared.resultingCurrent, parsedShared.current);
		}
		else
		{
			final SimpleBlock firstIf = parseNextTerminatingLinear(in.getTrueNext());
			final SimpleBlock firstElse;

			if (in.hasFalseNext())
			{
				firstElse = parseNextTerminatingLinear(in.getFalseNext());
			}
			else
			{
				firstElse = null;
			}

			final BranchingSimpleBlock_If term = new BranchingSimpleBlock_If(in, firstIf, firstElse);

			return new parseResults(term, SimpleBlock.NullBlock, CodeBlock.getNullCodeBlock());
		}
	}

	private SimpleBlock parseNextTerminatingLinear(CodeBlock in)
	{
		// System.out.println("in 1arg terminator, on: " + in.getMangledName());

		if (in.getTag() == CodeBlockTags.ItNum)
		{
			if (visitedItNums.contains(in.getBlockNum()))
			{
				in = in.getFalseNext();
			}
			else
			{
				visitedItNums.put(in.getBlockNum(), "");
			}
		}

		CodeBlock current = in;
		final SimpleBlock firstResult;
		SimpleBlock currentResult;

		if (!in.isNull())
		{
			final parseResults currentFirstResults = parseNextLinear(in);
			firstResult = currentFirstResults.resultingFirst;
			currentResult = currentFirstResults.resultingCurrent;

			if (current == currentFirstResults.current.getTrueNext())
			{
				throw new IllegalArgumentException();
			}

			current = currentFirstResults.current.getTrueNext();

			while (!current.isNull() && current.getTag() != CodeBlockTags.Return)
			{
				final parseResults currentResults = parseNextLinear(current);
				currentResult.nextLeft = currentResults.resultingFirst;
				currentResult = currentResults.resultingCurrent;

				if (current == currentResults.current)
				{
					current = current.getTrueNext();

					if (current.isNull())
					{
						throw new IllegalArgumentException();
					}
				}
				else
				{
					current = currentResults.current;
				}
			}

			return firstResult;
		}
		else
		{
			return null;
		}
	}

	private static class parseResults
	{
		private static final parseResults NullResult = new parseResults(SimpleBlock.NullBlock, SimpleBlock.NullBlock, CodeBlock.getNullCodeBlock());

		private final SimpleBlock resultingFirst;
		private final SimpleBlock resultingCurrent;
		private final CodeBlock current;

		public parseResults(SimpleBlock inResultingFirst, SimpleBlock inResultingCurrent, CodeBlock inCurrent)
		{
			this.resultingFirst = inResultingFirst;
			this.resultingCurrent = inResultingCurrent;
			this.current = inCurrent;

			// System.out.println("new parseResults created!: " + this.resultingFirst.toString() + ", " + this.resultingCurrent.toString() + ", " + this.current.getMangledName());
		}
	}

	private static abstract class SimpleBlock
	{
		protected static final SimpleBlock NullBlock = new NullSimpleBlock(CodeBlock.getNullCodeBlock());

		protected SimpleBlock nextLeft;
		protected SimpleBlock nextRight;

		protected final CodeBlock rawLines;

		public SimpleBlock(CodeBlock inRawLines)
		{
			this.nextLeft = NullBlock;
			this.nextRight = NullBlock;
			this.rawLines = inRawLines;
		}

		public boolean isNull()
		{
			return false;
		}

		@Override
		public abstract String toString();

		public abstract CharList toPseudoJava(int offset);
	}

	private static class LinearSimpleBlock extends SimpleBlock
	{
		public LinearSimpleBlock(CodeBlock inRawLines)
		{
			super(inRawLines);
			
			if(inRawLines.getMangledName().startsWith("ItNum"))
			{
				throw new IllegalArgumentException();
			}
		}

		@Override
		public String toString()
		{
			return this.rawLines.toString();
		}

		@Override
		public CharList toPseudoJava(int inOffset)
		{
			final CharList result = new CharList();

			for (AnnoInstruction cur : this.rawLines.getLines())
			{
				result.addNewLine();
				result.add(cur.toPseudoJava(inOffset), true);
				result.add(';');
			}
			
			if(!this.nextLeft.isNull())
			{
				result.addNewLine();
				System.out.println("from " + this.rawLines.getMangledName() + ", adding: " + this.nextLeft.rawLines.getMangledName() + " of class " + this.nextLeft.getClass());
				result.add(this.nextLeft.toPseudoJava(inOffset), true);
			}

			return result;
		}
	}

	private static class NullSimpleBlock extends SimpleBlock
	{
		public NullSimpleBlock(CodeBlock inRawLines)
		{
			super(inRawLines);
		}

		@Override
		public String toString()
		{
			return "Null Simple Block!";
		}

		@Override
		public boolean isNull()
		{
			return true;
		}

		@Override
		public CharList toPseudoJava(int inOffset)
		{
			return new CharList("<NULL SIMPLE BLOCK>");
		}
	}

	private static class BranchingSimpleBlock_If extends SimpleBlock
	{
		private SimpleBlock ifBlock;
		private SimpleBlock elseBlock;

		public BranchingSimpleBlock_If(CodeBlock inRawLines, SimpleBlock inIfBlock, SimpleBlock inElseBlock)
		{
			super(inRawLines);

			if (inIfBlock.isNull() && inElseBlock.isNull())
			{
				throw new IllegalArgumentException();
			}

			this.ifBlock = inIfBlock;
			this.elseBlock = inElseBlock;
		}

		@Override
		public String toString()
		{
			final CharList result = new CharList();
			final AnnoInstruction[] lines = this.rawLines.getLines();

			for (int i = 0; i < lines.length - 2; i++)
			{
				result.add(lines[i].toCharList(true), true);
				result.addNewLine();
			}

			result.addNewLine();

			result.add("if (");

			final AnnoInstruction boolStatement = lines[lines.length - 2];

			switch (boolStatement.getOpCode())
			{
				case TestIfLessThan:
				case TestIfEqual:
				case TestIfLessThanOrEqual:
				case TestLogicalComparison:
				{
					final BoolBrancher cast = (BoolBrancher) boolStatement;
					result.add(cast.toBooleanStatement(false), true);

					result.add(")\r\n{\r\n");
					result.add(this.ifBlock.toString());
					result.add("\r\n}\r\nelse\r\n{\r\n");
					break;
				}
				case TestAndSaveLogicalComparison:
				{
					final AnnoInstruction_TestSet cast = (AnnoInstruction_TestSet) boolStatement;

					result.add(")\r\n{\r\n");
					result.add(this.ifBlock.toString());
					result.add("\r\n}\r\nelse\r\n{\r\n");

					result.add(cast.getTarget().getAdjustedName());
					result.add(" = ");
					result.add(cast.getSource().getAdjustedName());
					result.add(';');
					result.addNewLine();
					break;
				}
				default:
				{
					throw new UnhandledEnumException(boolStatement.getOpCode());
				}
			}

			// result.add(lines[lines.length - 2].toCharList(true), true);

			if (!this.elseBlock.isNull())
			{
				result.add(this.elseBlock.toString());
			}

			result.add("\r\n}\r\n");

			return result.toString();
		}

		@Override
		public CharList toPseudoJava(int inOffset)
		{
			final CharList result = new CharList();
			final AnnoInstruction[] lines = this.rawLines.getLines();

			for (int i = 0; i < lines.length - 2; i++)
			{
				result.add(lines[i].toPseudoJava(inOffset), true);
				result.add(';');
				result.addNewLine();
			}

			result.addNewLine();
			result.add("should go to: ");
			result.add(this.rawLines.getTrueNext().getMangledName());
			result.add(" | ");
			result.add(this.rawLines.getFalseNext().getMangledName());
			result.addNewLine();

			result.add("if (");

			final AnnoInstruction boolStatement = lines[lines.length - 2];

			switch (boolStatement.getOpCode())
			{
				case TestIfLessThan:
				case TestIfEqual:
				case TestIfLessThanOrEqual:
				case TestLogicalComparison:
				{
					final BoolBrancher cast = (BoolBrancher) boolStatement;
					result.add(cast.toBooleanStatement(false), true);

					result.add(")\r\n{\r\n");
					result.add(this.ifBlock.toPseudoJava(inOffset + 1), true);
					result.add("\r\n}\r\nelse\r\n{\r\n");
					break;
				}
				case TestAndSaveLogicalComparison:
				{
					final AnnoInstruction_TestSet cast = (AnnoInstruction_TestSet) boolStatement;

					result.add(")\r\n{\r\n");
					result.add(this.ifBlock.toPseudoJava(inOffset + 1), true);
					result.add("\r\n}\r\nelse\r\n{\r\n");

					result.add(cast.getTarget().getAdjustedName());
					result.add(" = ");
					result.add(cast.getSource().getAdjustedName());
					result.add(';');
					result.addNewLine();
					break;
				}
				default:
				{
					throw new UnhandledEnumException(boolStatement.getOpCode());
				}
			}

			// result.add(lines[lines.length - 2].toCharList(true), true);

			if (!this.elseBlock.isNull())
			{
				result.add(this.elseBlock.toPseudoJava(inOffset + 1), true);
			}

			result.add("\r\n}\r\n");

			return result;
		}
	}
	
	private static class BranchingSimpleBlock_For extends SimpleBlock
	{
		private final CodeBlock Iterator;
		private final SimpleBlock InnerLoop;
		private final SimpleBlock afterLoop;
		
		public BranchingSimpleBlock_For(CodeBlock init, CodeBlock inIterator, SimpleBlock inInnerLoop, SimpleBlock inAfterLoop)
		{
			super(init);
			this.Iterator = inIterator;
			this.InnerLoop = inInnerLoop;
			this.afterLoop = inAfterLoop;
		}

		@Override
		public String toString()
		{
			return this.toPseudoJava(0).toString();
		}

		@Override
		public CharList toPseudoJava(int inOffset)
		{
			final CharList result = new CharList();
			
			result.add("InitRawLines:\r\n");
			result.add(this.rawLines.toCharList(true), true);	//temporary
			result.addNewLine();
			result.add("Inner Loop:\r\n");
			result.add(this.InnerLoop.toPseudoJava(inOffset + 1), true);
			result.addNewLine();
			result.add("AfterLoop:\r\n");
			result.add(this.afterLoop.toPseudoJava(inOffset + 1), true);
			
			return result;
		}
	}
}
