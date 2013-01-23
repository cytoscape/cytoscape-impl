package org.cytoscape.filter.internal.widgets.autocomplete.index;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


/**
 * <p>A Trie can be used for fast prefix matching of strings.  The word
 * "trie", pronounced "try", comes from the middle of the word retrieval.
 * A Trie can be thought of as a container of strings, but in the
 * implementation, a Trie is a node of a tree structure, where each node
 * has a reference to a character, and 0 or more Trie children.
 * The following figure shows an example of two Trie tree structures:
 * <p/>
 * <p><center><img align="center" src="doc-files/trie.gif"/>
 * <br><font size="1">Figure 1 - Two example Trie structures</font>
 * </center>
 * <p/>
 * <p>The following code can be used to create the above tree structures:
 * <p/>
 * <pre>
 * Trie trie1 = new Trie();    Trie trie2 = new Trie();
 * trie1.add("rain");        trie2.add("bat");
 * trie1.add("rainbow");    trie2.add("bed");
 * trie1.add("apple");        trie2.add("bear");
 * trie1.add("apples");        trie2.add("boat");
 * trie2.add("boats");
 * trie2.add("cat");
 * </pre>
 * <p/>
 * <p>The nodes with a green dot beside them indicate that the isWord flag is
 * true for that node, and nodes  with no green dot have the isWord flag
 * set to false.  To get all the words beginning with the prefix "be"
 * from trie2, calling trie2.getWords("be") will return an array containing
 * bed and bear.  To see if trie1 contains any words beginning with "app",
 * calling trie1.hasPrefix("app") will return true.
 * <p/>
 * <p>The speed of a prefix match depends on the number of children each node
 * in the search path has.  If  the maximum number of children is bound
 * by a constant, then the time to perform a query is O(m), where m is the
 * length of the input string. The following data* represents statistics
 * for the English language:
 * <p/>
 * <p><center><table border="1">
 * <tr><td><b>Number of Children</b></td><td><b>Number of Nodes</b></td>
 * <td><b>Number of Children</b></td><td><b>Number of Nodes</b></td></tr>
 * <tr align="right"><td>0</td><td>101808</td><td>14</td><td>84</td></tr>
 * <tr align="right"><td>1</td><td>199377</td><td>15</td><td>67</td></tr>
 * <tr align="right"><td>2</td><td>31032</td><td>16</td><td>40</td></tr>
 * <tr align="right"><td>3</td><td>10538</td><td>17</td><td>38</td></tr>
 * <tr align="right"><td>4</td><td>4272</td><td>18</td><td>30</td></tr>
 * <tr align="right"><td>5</td><td>2125</td><td>19</td><td>22</td></tr>
 * <tr align="right"><td>6</td><td>1178</td><td>20</td><td>17</td></tr>
 * <tr align="right"><td>7</td><td>739</td><td>21</td><td>13</td></tr>
 * <tr align="right"><td>8</td><td>424</td><td>22</td><td>16</td></tr>
 * <tr align="right"><td>9</td><td>329</td><td>23</td><td>10</td></tr>
 * <tr align="right"><td>10</td><td>242</td><td>24</td><td>16</td></tr>
 * <tr align="right"><td>11</td><td>163</td><td>25</td><td>9</td></tr>
 * <tr align="right"><td>12</td><td>117</td><td>26</td><td>9</td></tr>
 * <tr align="right"><td>13</td><td>106</td></tr>
 * </table>
 * <font size="1">Table 1 - Node statistics for the English language</font>
 * </center>
 * <p/>
 * <p>The "Number of Nodes" column is a count of number of nodes with the
 * specified number of children.  Thus, about 99% of the nodes in the Trie
 * structure have 5 or less children.  The maximum number of children is 26
 * since that is the size of the English alphabet.  The above data was
 * collected using a word list of 128,617 words obtained from an online
 * dictionary site.  Results will vary depending on the word list used.
 * <p/>
 * <p>The methods of a Trie fall into two broad categories, tree level
 * operations and string operations.  Tree level operations provide control of
 * the tree structure, such as adding or removing children.  String level
 * operations are higher-level methods that do the work of adding or removing
 * nodes from the Trie.  For example, the add(String), contains(String),
 * remove(String) and removeAll(String) are higher-level operations.
 * <p/>
 * <p>When words are removed using the remove(String) or removeAll(String)
 * then the trie is left in a state where all leaf nodes have the isWord flag
 * set to true.  This is achieved by repeatedly removing any leaf
 * node on the remove path that has the isWord flag set to false.  However,
 * tree level operations do not enforce this property.
 * <p/>
 * <P>This code has been placed in the public domain under a FreeBSD license
 * (courtesy of Randall Kippen).
 *
 * @author GraphBuilder.com
 */
public class Trie {
	protected Trie parent = null;
	protected Trie[] child = new Trie[1];
	protected int numChildren = 0;
	protected char ch;
	protected boolean isWord = false;

	/**
	 * Creates a Trie using the root symbol as the character.
	 */
	public Trie() {
		this((char) 251);
	}

	/**
	 * Creates a Trie using the specified character.
	 *
	 * @param c Character.
	 */
	public Trie(char c) {
		ch = c;
	}

	/**
	 * Returns the character of this trie.
	 *
	 * @return char.
	 */
	public char getChar() {
		return ch;
	}

	/**
	 * Sets the character of this trie.
	 *
	 * @param c Character.
	 * @throws IllegalArgumentException if this trie has a sibling with
	 *                                  the same character.
	 */
	public void setChar(char c) throws IllegalArgumentException {
		if (c == ch) {
			return;
		}

		if ((parent != null) && parent.hasChar(c)) {
			throw new IllegalArgumentException("duplicate chars not allowed");
		}

		ch = c;
	}

	/**
	 * Returns the value of the isWord flag.
	 *
	 * @return true or false.
	 */
	public boolean isWord() {
		return isWord;
	}

	/**
	 * Sets the value of the isWord flag.
	 *
	 * @param b true or false.
	 */
	public void setWord(boolean b) {
		isWord = b;
	}

	/**
	 * Used to create the trie nodes when a string is added to a trie.
	 *
	 * @param c Character.
	 * @return Trie Object.
	 */
	protected Trie createNode(char c) {
		return new Trie(c);
	}

	/**
	 * Inserts the trie as the last child.
	 *
	 * @param t Trie Object.
	 * @see #insertChild(Trie, int).
	 */
	public void addChild(Trie t) {
		insertChild(t, numChildren);
	}

	/**
	 * Inserts the trie at the specified index.  If successful, the parent
	 * of the specified trie
	 * is updated to be this trie.
	 *
	 * @param t     Trie Object.
	 * @param index Index integer value.
	 * @throws IllegalArgumentException if index < 0 or index > numChildren.
	 * @throws IllegalArgumentException if the specified trie is null.
	 * @throws IllegalArgumentException if the specified trie is still a
	 *                                  child of another trie.
	 * @throws IllegalArgumentException if this trie already has a trie
	 *                                  child with the same character.
	 * @throws IllegalArgumentException if this trie is a descendent of
	 *                                  the specified trie.
	 */
	public void insertChild(Trie t, int index) throws IllegalArgumentException {
		if ((index < 0) || (index > numChildren)) {
			throw new IllegalArgumentException("required: (index >= 0 && index <= numChildren) "
			                                   + "but: (index = " + index + ", numChildren = "
			                                   + numChildren + ")");
		}

		if (t == null) {
			throw new IllegalArgumentException("cannot add null child");
		}

		if (t.parent != null) {
			throw new IllegalArgumentException("specified child still belongs to parent");
		}

		if (hasChar(t.ch)) {
			throw new IllegalArgumentException("duplicate chars not allowed");
		}

		if (isDescendent(t)) {
			throw new IllegalArgumentException("cannot add cyclic reference");
		}

		t.parent = this;

		if (numChildren == child.length) {
			Trie[] arr = new Trie[2 * (numChildren + 1)];

			for (int i = 0; i < numChildren; i++) {
				arr[i] = child[i];
			}

			child = arr;
		}

		for (int i = numChildren; i > index; i--) {
			child[i] = child[i - 1];
		}

		child[index] = t;
		numChildren++;
	}

	/**
	 * Removes the specified trie from the child array.  Does nothing if the
	 * specified trie is not a child of this trie.  Otherwise the parent of
	 * the trie is set to null.
	 *
	 * @param t Trie Object.
	 */
	public void removeChild(Trie t) {
		for (int i = 0; i < numChildren; i++) {
			if (t == child[i]) {
				for (int j = i + 1; j < numChildren; j++) {
					child[j - 1] = child[j];
				}

				numChildren--;
				child[numChildren] = null;
				t.parent = null;

				break;
			}
		}
	}

	/**
	 * Returns the number of children this trie has.
	 *
	 * @return number of children.
	 */
	public int numChildren() {
		return numChildren;
	}

	/**
	 * Returns the child at the specified index.
	 *
	 * @param index Integer index value.
	 * @return Trie object.
	 * @throws IllegalArgumentException if index < 0 or index >= numChildren.
	 */
	public Trie child(int index) throws IllegalArgumentException {
		if ((index < 0) || (index >= numChildren)) {
			throw new IllegalArgumentException("required: (index >= 0 && index < numChildren) "
			                                   + "but: (index = " + index + ", numChildren = "
			                                   + numChildren + ")");
		}

		return child[index];
	}

	/**
	 * Returns the parent node.
	 *
	 * @return Trie Object.
	 */
	public Trie getParent() {
		return parent;
	}

	/**
	 * Returns true if this node is a descendent of the specified node or
	 * this node and the specified node are the same node, false otherwise.
	 *
	 * @param t Trie Object.
	 * @return true or false.
	 */
	public boolean isDescendent(Trie t) {
		Trie r = this;

		while (r != null) {
			if (r == t) {
				return true;
			}

			r = r.parent;
		}

		return false;
	}

	//-End of tree-level operations.  Start of string operations. -------

	/**
	 * Adds the string to the trie.  Returns true if the string is added or
	 * false if the string is already contained in the trie.
	 *
	 * @param s String.
	 * @return true or false.
	 */
	public boolean add(String s) {
		return add(s, 0);
	}

	private boolean add(String s, int index) {
		if (index == s.length()) {
			if (isWord) {
				return false;
			}

			isWord = true;

			return true;
		}

		char c = s.charAt(index);

		for (int i = 0; i < numChildren; i++) {
			if (child[i].ch == c) {
				return child[i].add(s, index + 1);
			}
		}

		// this code adds from the bottom to the top because the addChild method
		// checks for cyclic references.  This prevents quadratic runtime.
		int i = s.length() - 1;
		Trie t = createNode(s.charAt(i--));
		t.isWord = true;

		while (i >= index) {
			Trie n = createNode(s.charAt(i--));
			n.addChild(t);
			t = n;
		}

		addChild(t);

		return true;
	}

	/**
	 * Returns the child that has the specified character or null if no
	 * child has the specified character.
	 *
	 * @param c Character.
	 * @return Trie Object.
	 */
	public Trie getNode(char c) {
		for (int i = 0; i < numChildren; i++) {
			if (child[i].ch == c) {
				return child[i];
			}
		}

		return null;
	}

	/**
	 * Returns the last trie in the path that prefix matches the
	 * specified prefix string rooted at this node, or null if there is no
	 * such prefix path.
	 *
	 * @param prefix String prefix.
	 * @return Trie Object.
	 */
	public Trie getNode(String prefix) {
		return getNode(prefix, 0);
	}

	private Trie getNode(String prefix, int index) {
		if (index == prefix.length()) {
			return this;
		}

		char c = prefix.charAt(index);

		for (int i = 0; i < numChildren; i++) {
			if (child[i].ch == c) {
				return child[i].getNode(prefix, index + 1);
			}
		}

		return null;
	}

	/**
	 * Removes the specified string from the trie.  Returns true if the
	 * string was removed or false if the string was not a word in the trie.
	 *
	 * @param s String.
	 * @return true or false.
	 */
	public boolean remove(String s) {
		Trie t = getNode(s);

		if ((t == null) || !t.isWord) {
			return false;
		}

		t.isWord = false;

		while ((t != null) && (t.numChildren == 0) && !t.isWord) {
			Trie p = t.parent;

			if (p != null) {
				p.removeChild(t);
			}

			t = p;
		}

		return true;
	}

	/**
	 * Removes all words from the trie that begin with the specified prefix.
	 * Returns true if the trie contained the prefix, false otherwise.
	 *
	 * @param prefix String.
	 * @return true or false.
	 */
	public boolean removeAll(String prefix) {
		Trie t = getNode(prefix);

		if (t == null) {
			return false;
		}

		if (t.parent == null) {
			if ((t.numChildren == 0) && !t.isWord) {
				return false;
			}

			for (int i = 0; i < t.numChildren; i++) {
				t.child[i].parent = null;
				t.child[i] = null;
			}

			t.numChildren = 0;
			t.isWord = false;

			return true;
		}

		Trie p = t.parent;
		p.removeChild(t);
		t = p;

		while ((t != null) && (t.numChildren == 0) && !t.isWord) {
			p = t.parent;

			if (p != null) {
				p.removeChild(t);
			}

			t = p;
		}

		return true;
	}

	/**
	 * Returns the number of nodes that define isWord as true, starting at
	 * this node and including all of its descendents.  This operation requires
	 * traversing the tree rooted at this node.
	 *
	 * @return integer size.
	 */
	public int size() {
		int size = 0;

		if (isWord) {
			size++;
		}

		for (int i = 0; i < numChildren; i++) {
			size += child[i].size();
		}

		return size;
	}

	/**
	 * Returns all of the words in the trie rooted at this node.
	 *
	 * @return Array of Strings.
	 */
	public String[] getWords() {
		return getWords("");
	}

	/**
	 * Returns all of the words in the trie that begin with the specified
	 * prefix rooted at this node.  An array of length 0 is returned if there
	 * are no words that begin with the specified prefix.
	 *
	 * @param prefix String prefix.
	 * @return Array of Strings.
	 */
	public String[] getWords(String prefix) {
		Trie n = getNode(prefix);

		if (n == null) {
			return new String[0];
		}

		String[] arr = new String[n.size()];
		n.getWords(arr, 0);

		return arr;
	}

	private int getWords(String[] arr, int x) {
		if (isWord) {
			arr[x++] = toString();
		}

		for (int i = 0; i < numChildren; i++) {
			x = child[i].getWords(arr, x);
		}

		return x;
	}

	/**
	 * Returns true if the specified string has a prefix path, starting at
	 * this node, where the last node on the path has the isWord flag set to
	 * true.  Otherwise false is returned.
	 *
	 * @param s String.
	 * @return true or false.
	 */
	public boolean hasWord(String s) {
		return contains(s, false);
	}

	/**
	 * Returns true if the specified string has a prefix path starting at
	 * this node. Otherwise false is returned.
	 *
	 * @param s String.
	 * @return true or false.
	 */
	public boolean hasPrefix(String s) {
		return contains(s, true);
	}

	/**
	 * This method is the same as the hasWord(String) method.
	 *
	 * @param s String.
	 * @return true or false.
	 * @see #hasWord(String)
	 */
	public boolean contains(String s) {
		return contains(s, false);
	}

	private boolean contains(String s, boolean prefix) {
		Trie t = getNode(s);

		if (t == null) {
			return false;
		}

		if (prefix) {
			return true;
		}

		return t.isWord;
	}

	/**
	 * Returns true if this node has a child with the specified character.
	 *
	 * @param c Character.
	 * @return true or false.
	 */
	public boolean hasChar(char c) {
		for (int i = 0; i < numChildren; i++) {
			if (child[i].ch == c) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the number of nodes from this node up to the root node.
	 * The root node has height 0.
	 *
	 * @return height integer.
	 */
	public int getHeight() {
		int h = -1;

		Trie t = this;

		while (t != null) {
			h++;
			t = t.parent;
		}

		return h;
	}

	/**
	 * Returns a string containing the characters on the path from this node to
	 * the root, but not including the root character.  The last character in
	 * the returned string is the character at this node.
	 *
	 * @return String.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getHeight());
		Trie t = this;

		while (t.parent != null) {
			sb.append(t.ch);
			t = t.parent;
		}

		return sb.reverse().toString();
	}
}
