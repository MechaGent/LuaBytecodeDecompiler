package LuaBytecodeDecompiler.Mk03.ControlFlow;

import DataStructures.Linkages.SingleLinkedList.Unsorted.Mk03.SingleLinkedList;
import LuaBytecodeDecompiler.Mk03.AnnoFunction;
import LuaBytecodeDecompiler.Mk03.AnnoInstruction;

public class ControlFlowGraph
{
	private static final Node NullNode = new Node(new NodeHitbox(-1, -2), null, null);
	
	private final Node root;
	
	private ControlFlowGraph(Node inRoot)
	{
		this.root = inRoot;
	}
	
	public static ControlFlowGraph getInstance(AnnoFunction in)
	{
		
	}
	
	private static SingleLinkedList<NodeHitbox> cleanUpHitboxes(SingleLinkedList<NodeHitbox> in)
	{
		final NodeHitbox[] naive = in.toArray(new NodeHitbox[in.getSize()]);
		
		for(int startIndex = 0; startIndex < naive.length-1; startIndex++)
		{
			for(int i = startIndex; i < naive.length; i++)
			{
				
			}
		}
	}
	
	private static SingleLinkedList<NodeHitbox> getNaiveList(AnnoInstruction[] in)
	{
		final SingleLinkedList<NodeHitbox> result = new SingleLinkedList<NodeHitbox>();
		int lastStart = 0;
		int currentEndLimit = Integer.MAX_VALUE;
		boolean isProcessing = false;
		
		for(int i = 0; i < in.length; i++)
		{
			final AnnoInstruction first = in[i];
			
			switch(first.getOpCode())
			{
				case TestIfEqual:
				case TestIfLessThan:
				case TestIfLessThanOrEqual:
				case TestLogicalComparison:
				case TestAndSaveLogicalComparison:
				{
					//branch statement, followed by jump
					i++;
					final NodeHitbox next = new NodeHitbox(lastStart, i - lastStart, NodeTypes.Branch);
					result.add(next);
					lastStart = i + 1;
					break;
				}
				case Return:
				{
					final NodeHitbox next = new NodeHitbox(lastStart, i - lastStart, NodeTypes.Returns);
					result.add(next);
					lastStart = i + 1;
					break;
				}
				case InitNumericForLoop:
				{
					final NodeHitbox next = new NodeHitbox(lastStart, i - lastStart, NodeTypes.Normal);
					result.add(next);
					lastStart = i + 1;
					break;
				}
				case IterateNumericForLoop:
				{
					final NodeHitbox next = new NodeHitbox(lastStart, i - lastStart, NodeTypes.Loop);
					result.add(next);
					lastStart = i + 1;
					break;
				}
				case IterateGenericForLoop:
				{
					throw new UnsupportedOperationException();
				}
				default:
				{
					//just increment
					break;
				}
			}
		}
		
		return result;
	}
	
	private static enum NodeTypes
	{
		Branch, Returns, Loop, Normal,
	}

	private static class Node
	{
		private final AnnoInstruction[] core;
		private final int start;
		private final int length;
		private final Node next;
		
		public Node(NodeHitbox hitbox, AnnoInstruction[] inCore, Node inNext)
		{
			this.core = inCore;
			this.start = hitbox.start;
			this.length = hitbox.end - hitbox.start;
			this.next = inNext;
		}
	}
	
	private static class BranchNode extends Node
	{
		private final Node notNext;

		public BranchNode(NodeHitbox inHitbox, AnnoInstruction[] inCore, Node inNext, Node inNotNext)
		{
			super(inHitbox, inCore, inNext);
			this.notNext = inNotNext;
		}
	}
	
	private static class NodeHitbox
	{
		private final int start;
		private final int end;
		private final NodeTypes type;

		public NodeHitbox(int inStart, int inEnd, NodeTypes inType)
		{
			this.start = inStart;
			this.end = inEnd;
			this.type = inType;
		}

		public int getStart()
		{
			return this.start;
		}

		public int getEnd()
		{
			return this.end;
		}

		public NodeTypes getType()
		{
			return this.type;
		}

		public CollideStates detectCollision(NodeHitbox other)
		{
			return detectCollision(this, other);
		}
		
		/**
		 * cases:
		 * <br>nodeX.start =/= nodeY.start
		 * <br>nodeX.start =/= nodeY.end
		 * <br>nodeX.end =/= nodeY.start
		 * <br>nodeX.end == nodeY.start
		 * <br>
		 * <br>node1 is before node2: 	{1,4},{5,8}		node1.end < node2.start, 		node1.start < node2.end, node1.start < node2.start, node1.end < node2.end
		 * <br>node1 is after node2: 	{5,8},{1,4} 	node1.end > node2.start, node1.start > node2.end, 		node1.start > node2.start, node1.end > node2.end
		 * <br>
		 * <br>node1 intersects node2 (node1 is first):	 {1,5},{4,8}	node1.end > node2.start, node1.start < node2.end, node1.start < node2.start, node1.end < node2.end
		 * <br>node2 is within node1 (end isn't shared): {1,8},{4,5}	node1.end > node2.start, node1.start < node2.end, node1.start < node2.start, node1.end > node2.end
		 * <br>node2 is within node1 (end is shared):	 {1,8},{4,8}	node1.end > node2.start, node1.start < node2.end, node1.start < node2.start, node1.end = node2.end
		 * <br>node1 intersects node2 (node1 is second): {4,8},{1,5}	node1.end > node2.start, node1.start < node2.end, node1.start > node2.start, node1.end > node2.end
		 * <br>node1 is within node2 (end isn't shared): {4,5},{1,8}	node1.end > node2.start, node1.start < node2.end, node1.start > node2.start, node1.end < node2.end
		 * <br>node1 is within node2 (end is shared):	 {4,8},{1,8}	node1.end > node2.start, node1.start < node2.end, node1.start > node2.start, node1.end = node2.end
		 * @param node1
		 * @param node2
		 * @return
		 */
		private static final CollideStates detectCollision(NodeHitbox node1, NodeHitbox node2)
		{
			final CollideStates result;

			if (node1.end < node2.start)
			{
				result = CollideStates.Outside_Before;
			}
			else
			{
				if (node1.start > node2.end)
				{
					result = CollideStates.Outside_After;
				}
				else
				{
					if (node1.start < node2.start)
					{
						if (node1.end < node2.end)
						{
							result = CollideStates.Intersects_First;
						}
						else if (node1.end > node2.end)
						{
							result = CollideStates.Entirely_Surrounds;
						}
						else
						{
							result = CollideStates.Entirely_Surrounds_SharedEnd;
						}
					}
					else
					{
						if (node1.end < node2.end)
						{
							result = CollideStates.Intersects_Second;
						}
						else if (node1.end > node2.end)
						{
							result = CollideStates.Entirely_Within;
						}
						else
						{
							result = CollideStates.Entirely_Within_SharedEnd;
						}
					}
				}
			}

			return result;
		}

		public static enum CollideStates
		{
			Outside_Before,
			Outside_After,
			Intersects_First,
			Intersects_Second,
			Entirely_Within,
			Entirely_Surrounds,
			Entirely_Within_SharedEnd,
			Entirely_Surrounds_SharedEnd;
		}
	}
}
