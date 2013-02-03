package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {
	
	/**
	 * Root node
	 */
	TagNode root=null;
	
	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;
	
	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	
	
	private boolean isTag(String s, String t){
		if(t.equals("open")){
			if(s.startsWith("<") && s.endsWith(">") && !s.contains("/"))
				return true;
			return false;
		}
		if(t.equals("close")){
			if(s.startsWith("</") && s.endsWith(">"))
				return true;
			return false;
		}
		return true; //shouldn't get here
	}
	
	/**
	 * Builds the DOM tree from input HTML file
	 */
	public void build() {
		String curr;
		TagNode ptr = null;
		Stack<TagNode> stk = new Stack<TagNode>();
		
		while(sc.hasNextLine()){
			curr = sc.nextLine();
			if(isTag(curr, "open")){
				curr = curr.replace("<" , "");
				curr = curr.replace(">" , "");
				if(root == null){
					root = new TagNode(curr, null, null);
					stk.push(root);
					ptr = root;
					continue;
				}
				if(ptr.firstChild == null){
					ptr.firstChild = new TagNode(curr, null, null);
					stk.push(ptr.firstChild);
					ptr = stk.peek();
					continue;
				}
				ptr = ptr.firstChild;
				while(ptr.sibling!=null){
					ptr = ptr.sibling;
				}
				ptr.sibling = new TagNode(curr, null, null);
				stk.push(ptr.sibling);
				ptr = stk.peek();
				continue;
			}
			if(isTag(curr, "close")){
				ptr = stk.pop();
				if(stk.isEmpty())
					continue;
				ptr = stk.peek();
				continue;
			}
			if(!isTag(curr, "open") && !isTag(curr, "close")){ //if not a tag
				if(ptr.firstChild == null){
					ptr.firstChild = new TagNode(curr, null, null);
					continue;
				}
				else{
					ptr = ptr.firstChild;
					while(ptr.sibling!=null){
						ptr = ptr.sibling;
					}
					ptr.sibling = new TagNode(curr, null, null);
					ptr = stk.peek();
					continue;
				}
			}
		}
	}
	//helper for replaceTag
	private void Diot(TagNode root, ArrayList<TagNode> outList, String oldTag) {
		if (root == null) {
			return;
		}
		Diot(root.firstChild, outList, oldTag);
		if(root.tag.equals(oldTag))
			outList.add(root);
		Diot(root.sibling, outList, oldTag);
	}
		
	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		ArrayList<TagNode> tags = new ArrayList<TagNode>();
		Diot(root, tags, oldTag);
		for(int i = 0; i < tags.size(); i++){
			tags.get(i).tag = newTag;
		}
	}


	//boldRow helper method
	private void Biot(TagNode root, ArrayList<TagNode> outList, String oldTag) {
		if (root == null) {
			return;
		}
		Biot(root.firstChild, outList, oldTag);
		if(root.tag.equals(oldTag))
			outList.add(root);
		Biot(root.sibling, outList, oldTag);
	}
	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */

	
	public void boldRow(int row) {
		ArrayList<TagNode> rowlist = new ArrayList<TagNode>();
		Biot(root, rowlist, "tr");
		if(rowlist.isEmpty() || rowlist.size() < row)
			return;
		TagNode ptr = rowlist.get(0);	//set ptr to first row
		for(int i = 1; i < row; i++){	//ptr traverses to target row
			ptr = ptr.sibling;
		}
		ptr = ptr.firstChild;			//ptr gets set to first col
		TagNode temp;
		while(ptr != null){				//ptr traverses through cols
			temp = new TagNode("b", ptr.firstChild, null);
			temp.firstChild = ptr.firstChild;
			ptr.firstChild = temp;
			ptr = ptr.sibling;
		}
	}

	//removeTag helper method for p, em, b
	private void Riot(TagNode root, String target) {
		if (root == null) 
			return;
		Riot(root.firstChild, target);
		if(root.sibling != null && root.sibling.tag.equals(target)){
			TagNode ptr = root.firstChild;
			while(ptr.sibling != null){
				ptr = ptr.sibling;		//sets ptr to last sibling
			}
			ptr.sibling = root.sibling.sibling;
			root.sibling = root.sibling.firstChild;	
		}
		if(root.firstChild != null && root.firstChild.tag.equals(target)){
			root.firstChild = root.firstChild.firstChild;
		}
		Riot(root.sibling, target);
	}
	//removeTag helper method for ol, ul
	private void oliot(TagNode root, String target){
		if(root == null)
			return;
		oliot(root.firstChild, target);
		if(root.sibling != null && root.sibling.tag.equals(target)){
			TagNode ptr = root.sibling.firstChild;
			while(ptr.sibling != null){
				ptr.tag = "p";
				ptr = ptr.sibling;
			}
			ptr.tag = "p";
			ptr.sibling = root.sibling.sibling;
			root.sibling = root.sibling.firstChild;
		}
		if(root.firstChild != null && root.firstChild.tag.equals(target)){
			TagNode ptr = root.firstChild.firstChild;
			while(ptr.sibling != null){
				ptr.tag = "p";
				ptr = ptr.sibling;
			}
			ptr.tag = "p";
			ptr.sibling = root.firstChild.sibling;
			root.firstChild = root.firstChild.firstChild;
		}
		oliot(root.sibling, target);
	}
	
	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		if(tag.equals("p") || tag.equals("em") || tag.equals("b"))
			Riot(root, tag);
		if(tag.equals("ol") || tag.equals("ul"))
			oliot(root, tag);
	}
		
	
	
	//addTag helper method
	private void Adiot(TagNode root, String word, String tag){
		if(root == null)
			return;
		Adiot(root.firstChild, word, tag);
		if(root.firstChild != null && root.firstChild.tag.contains(word)){
			String[] words = root.firstChild.tag.split(word);
			if(words.length == 2){
				TagNode rightside = new TagNode(words[1], null, root.firstChild.sibling);
				TagNode leftside = new TagNode(words[0], null , null);
				TagNode tagged = new TagNode(word, null, null);
				TagNode tagger = new TagNode(tag, tagged, rightside);
				root.firstChild = leftside;
				leftside.sibling = tagger;
				tagger.sibling = rightside;
				if(words[0].equals(""))
					root.firstChild = tagger;
			}
			else if(words.length == 0){
				TagNode tagger = new TagNode(tag, root.firstChild, root.sibling);
				root.firstChild = tagger;
				System.out.println("length 0");
			}
			else{	//target is the first word
				if(words[0].charAt(0)==' '){
					TagNode tagged = new TagNode(word, null, null);
					TagNode tagger = new TagNode(tag, tagged, null);
					TagNode right = new TagNode(words[0], null, root.firstChild.sibling);
					tagger.sibling = right;
					root.firstChild = tagger;
					
				} //target is the last word
				else{
					TagNode tagged = new TagNode(word, null, null);
					TagNode tagger = new TagNode(tag, tagged, root.firstChild.sibling);
					TagNode left = new TagNode(words[0], null, tagger);
					root.firstChild = left;
				}
			}
			
		}
		if(root.sibling != null && root.sibling.tag.contains(word)){
			String[] words = root.sibling.tag.split(word);
			if(words.length > 0){
				TagNode rightside = new TagNode(words[1], null, root.sibling.sibling);
				TagNode leftside = new TagNode(words[0], null, null);
				TagNode tagged = new TagNode(word, null, null);
				TagNode tagger = new TagNode(tag, tagged, rightside);
				root.sibling = leftside;
				leftside.sibling = tagger;
				tagger.sibling = rightside;
			}
			else if(words.length == 0){
				TagNode tagger = new TagNode(tag, root.sibling, root.sibling.sibling);
				root.sibling = tagger;
				System.out.println("length 0");
			}
			else{
				TagNode tagged = new TagNode(word, null, null);
				TagNode tagger = new TagNode(tag, tagged, null);
				if(words[0].charAt(0) == ' '){//target is first word
					TagNode right = new TagNode(words[0], null, root.sibling.sibling);
					tagger.sibling = right;
					root.sibling = tagger;
				}//target is last word
				else{
					TagNode left = new TagNode(words[0], null, tagger);
					root.sibling = left;
				}
			}
		}
		Adiot(root.sibling, word, tag);
	}
	
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */
	
	public void addTag(String word, String tag) {
		Adiot(root, word, tag);
	}
	
	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}
	
	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}
	
}
