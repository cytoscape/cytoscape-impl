package org.ivis.layout.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;

/**
 * GraphMLWriter class is used for saving the topology of 
 * the given graph manager on the hard-disk
 * 
 * @author Alper Karacelik
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class GraphMLWriter
{
	private String filePath;
	private FileWriter fstream;
	private BufferedWriter out;
	
	// mapping between nodes and their string equivalents in the graphml file.
	private HashMap map;
	
	// nodes at the first level will be written 
	// with a 6-length indentation
	private final short INITIAL_INDENTATION = 4;
	
	private final String FILE_HEADER = "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n" +
									   "  <key id=\"x\" for=\"node\" attr.name=\"x\" attr.type=\"int\"/>\n" +
									   "  <key id=\"y\" for=\"node\" attr.name=\"y\" attr.type=\"int\"/>\n" +
									   "  <key id=\"height\" for=\"node\" attr.name=\"height\" attr.type=\"int\"/>\n" +
									   "  <key id=\"width\" for=\"node\" attr.name=\"width\" attr.type=\"int\"/>\n" +
									   "  <key id=\"shape\" for=\"node\" attr.name=\"shape\" attr.type=\"string\"/>\n" +
									   "  <key id=\"clusterID\" for=\"node\" attr.name=\"clusterID\" attr.type=\"string\"/>\n" +
									   "  <key id=\"margin\" for=\"graph\" attr.name=\"margin\" attr.type=\"int\"/>\n" +
									   "  <key id=\"style\" for=\"edge\" attr.name=\"style\" attr.type=\"string\"/>\n" +
									   "  <key id=\"arrow\" for=\"edge\" attr.name=\"arrow\" attr.type=\"string\"/>\n" +
									   "  <key id=\"bendpoint\" for=\"edge\" attr.name=\"bendpoint\" attr.type=\"string\"/>\n" +
									   "  <key id=\"color\" for=\"all\" attr.name=\"color\" attr.type=\"string\"/>\n" +
									   "  <key id=\"borderColor\" for=\"all\" attr.name=\"borderColor\" attr.type=\"string\"/>\n" +
									   "  <key id=\"text\" for=\"all\" attr.name=\"text\" attr.type=\"string\"/>\n" +
									   "  <key id=\"textFont\" for=\"all\" attr.name=\"textFont\" attr.type=\"string\"/>\n" +
									   "  <key id=\"textColor\" for=\"all\" attr.name=\"textColor\" attr.type=\"string\"/>\n" +
									   "  <key id=\"highlightColor\" for=\"all\" attr.name=\"highlightColor\" attr.type=\"string\"/>\n";
	
	private final String[] NODE_DATA_1 = {"<data key=\"color\">14 112 130</data>\n",
      								      "<data key=\"borderColor\">14 112 130</data>\n"};
	
	private final String[] NODE_DATA_2 = {"<data key=\"textFont\">1|Arial|8.25|0|WINDOWS|1|-11|0|0|0|0|0|0|0|1|0|0|0|0|Arial</data>\n",
      								      "<data key=\"textColor\">0 0 0</data>\n",
      								      "<data key=\"clusterID\">0</data>\n"};
	
	private final String[] EDGE_DATA = {"<data key=\"color\">0 0 0</data>\n",
      								    "<data key=\"text\"/>\n",
      								    "<data key=\"textFont\">1|Arial|8|0|WINDOWS|1|-11|0|0|0|0|0|0|0|1|0|0|0|0|Arial</data>\n",
      								    "<data key=\"textColor\">0 0 0</data>\n",
      								    "<data key=\"style\">Solid</data>\n",
      								    "<data key=\"arrow\">None</data>\n",
      								    "<data key=\"width\">1</data>\n"};
	
	private final String FILE_FOOTER = "    <data key=\"margin\">-1</data>\n" +
									  "  </graph>\n" +
									  "</graphml>";
	
	/**
	 * initializes variables 
	 * 
	 * @param _filePath
	 */
	public GraphMLWriter (String _filePath)
	{
		this.filePath = _filePath;
		this.map = new HashMap();
		
		try
		{
			this.fstream = new FileWriter(this.filePath);
			this.out = new BufferedWriter(fstream);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Saves given graph into specified .graphml file
	 * 
	 * @param lgm
	 */
	public void saveGraph ( LGraphManager lgm )
	{
		try
		{
			// write the header
			out.write(this.FILE_HEADER);
			
			// map the nodes with their string equivalents
			this.mapNodes(lgm.getRoot(), (short) 0, "");
			
			// write the nodes 
			this.writeNodes(lgm.getRoot(), (short) 0, "");
			
			// write the edges
			this.writeEdges(lgm);
			
			// write the footer and close file
			out.write(this.FILE_FOOTER);
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * A recursive function for writing nodes of the specified LGraphManager 
	 * into the specified file 
	 * 
	 * @param root
	 * @param level
	 * @param parentStr
	 */
	private void mapNodes (LGraph root, short level, String parentStr)
	{
		String currNodeStr;
		LNode node;
		
		// for each node of the root graph...
		for (int i = 0; i < root.getNodes().size(); i++)
		{
			node = (LNode) root.getNodes().get(i);
			
			// check if the current root graph is the root graph of the LGraphManager
			if (level != 0)
				currNodeStr = parentStr + ":n" + i;
			else
				currNodeStr = "n" + i;
			
			// add new node to the node-string map
			this.map.put(node, currNodeStr);
			
			// if current node is a compound one, 
			// then make a recursive call with current node's child graph 
			if (node.getChild() != null)
			{
				this.mapNodes(node.getChild(), (short)(level+1), currNodeStr);
			}
		}
	}
	
	/**
	 * A recursive function that writes the nodes of the specified LGraphManager
	 * 
	 * @param root
	 * @param level
	 * @param parentStr
	 */
	private void writeNodes (LGraph root, short level, String parentStr)
	{
		short currIndentation = (short) (this.INITIAL_INDENTATION + (level * 2));
		
		LNode node;
		LNode parent = root.getParent();
		
		int x, y;
		
		String currNodeStr;
		try 
		{
			this.writeSpaces((short)(currIndentation - 2));
			
			if (level == 0)
				out.write("<graph id=\"\" edgedefault=\"undirected\">\n");
			else
				out.write("  <graph id=\""+ parentStr + ":\" edgedefault=\"undirected\">\n");
			
			for ( int i = 0; i < root.getNodes().size(); i++ )
			{	
				node = (LNode) root.getNodes().get(i);
				
				// find the top left point of the current node
				// if it is the root graph
				if (parent == null)
				{
					x = (int) node.getRect().x;
					y = (int) node.getRect().y;
				}
				else
				{
					x = (int) (node.getRect().x - parent.getRect().x);
					y = (int) (node.getRect().y - parent.getRect().y);
				}
				
				// write the node data
				this.writeSpaces(currIndentation);
				out.write("<node id=\"");
				currNodeStr = (String)this.map.get(node);
				out.write(currNodeStr + "\">\n");
				
				this.writeSpaces((short)(currIndentation + 2));
				out.write("<data key=\"x\">" + x + "</data>\n");

				this.writeSpaces((short)(currIndentation + 2));
				out.write("<data key=\"y\">" + y + "</data>\n");
				
				this.writeSpaces((short)(currIndentation + 2));
				out.write("<data key=\"height\">" + (int) node.getRect().height + "</data>\n");
				
				this.writeSpaces((short)(currIndentation + 2));
				out.write("<data key=\"width\">" + (int) node.getRect().width + "</data>\n");
				
				this.writeToFile(NODE_DATA_1, (short)(currIndentation + 2)); 
				
				this.writeSpaces((short)(currIndentation + 2));
				out.write("<data key=\"text\">" + currNodeStr + "</data>\n");
				
				this.writeToFile(NODE_DATA_2, (short)(currIndentation + 2));
				
				// if current node is a compound, then make a recursive call
				if (node.getChild() != null)
				{
					this.writeNodes(node.getChild(), (short)(level+1), currNodeStr);
				}
				else
				{
					this.writeSpaces((short)(currIndentation + 2));
					out.write("<data key=\"shape\">Rectangle</data>\n");
				}
				
				this.writeSpaces(currIndentation);
				out.write("</node>\n");
			}
			
			if ( level != 0 )
			{
				this.writeSpaces(currIndentation);
				out.write("<data key=\"margin\">10</data>\n");
				this.writeSpaces((short)(currIndentation - 2));
				out.write("</graph>\n");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This function writes the all edges of the specified LGraphManager
	 * 
	 * @param lgm
	 */
	private void writeEdges (LGraphManager lgm)
	{
		String sourceStr, targetStr;
		LEdge edge;
		
		try
		{
			// for each edge in the graph manager...
			for ( int i = 0; i < lgm.getAllEdges().length; i++ )
			{
				edge = (LEdge) lgm.getAllEdges()[i];
				
				// get the string equivalents of the source and target nodes
				sourceStr = (String)this.map.get(edge.getSource());
				targetStr = (String)this.map.get(edge.getTarget());
				
				// write the edge data
				out.write("    <edge id=\"e" + i + "\" source=\"" + sourceStr + 
					"\" target=\"" + targetStr + "\">\n");
				this.writeToFile(this.EDGE_DATA, (short)6);
				out.write("    </edge>\n");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This function puts n spaces on the specified graphml file
	 * 
	 * @param n
	 */
	private void writeSpaces (short n)
	{
		try
		{
			for (int i = 0; i < n; i++)
			{
				out.write(" ");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This function writes given string array (inp) 
	 * to the specified graphml file 
	 * Each element in the given array is written on a separate line
	 * with n spaces
	 * 
	 * @param inp
	 * @param n
	 */
	private void writeToFile (String[] inp, short n)
	{
		try
		{
			for (int i = 0; i < inp.length; i++)
			{
				this.writeSpaces(n);
				this.out.write(inp[i]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
