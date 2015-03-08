import java.awt.Graphics;


public class Stone {

	private char color;
	private int liberties = 4;	//will also need position of the liberties
	private int number;
	private Group group;
	private int row;
	private int column;

	public Stone() {
		color = 'B';
	}
	
	public Stone(char color, int row, int column) {
		this.color = color;
		this.row = row;
		this.column = column;
	}
	
	public char getColor() {
		return color;
	}
	
	public int getLiberties() {
		return liberties;
	}
	
	public void setLiberties(int liberties) {
		this.liberties = liberties;
		
		if (liberties < 0)
			liberties = 0;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public Group getGroup() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}
}
