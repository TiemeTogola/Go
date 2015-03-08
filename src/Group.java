import java.util.*;


public class Group {

	private ArrayList<Stone> components = new ArrayList<>();
	private char color;
//	private int liberties;	could count them, sum of liberties of the components
	
	public Group() {
		
	}
	
	public Group(Stone s1, Stone s2) {
		components.add(s1);
		components.add(s2);
		
		s1.setGroup(this);
		s2.setGroup(this);
		
		color = s1.getColor();
	}
	
	public int size() {
		return components.size();
	}
	
	public Stone get(int i) {
		return components.get(i);
	}
	
	public void remove(int i) {
		components.remove(i);
	}
	
	public void add(Stone s) {
		components.add(s);
		s.setGroup(this);
	}
	
	public void merge(Group g) {
		
		for (int i = 0; i < g.size(); i++) 
			add(g.get(i));
	}

	public char getColor() {
		return color;
	}	
}
