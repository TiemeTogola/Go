import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GoBoard extends JPanel {
	
	private char turn = 'B';
	private Stone[][] stones = new Stone[19][19];
	private Point[][] interPos = new Point[19][19];
	private ArrayList<Stone[][]> history = new ArrayList<>();
	private ArrayList<Group> groups = new ArrayList<>();
	private ArrayList<Stone> orderedStoneList = new ArrayList<>();
	private int current;
	
	private int edgeCounter = 0;

	private int capturedBlack;	//captured by black (white prisoners)
	private int territoryBlack;
	private int scoreBlack;
	
	private int capturedWhite;	//..
	private int territoryWhite;
	private int scoreWhite;	
	
	
	private final int CELLWIDTH = 30;
	private final int CELLHEIGHT = 30;
	private final int RADIUS = 15;
	private final int X = 120; 
	private final int Y = 70;

	private boolean displayNumbers = false;
	
	/*
	 * 
	 * listener for board size change
	 * 
	 * implement all rules
	 * 
	 * score system
	 * 
	 * hints, tools (saving system, timer, )
	 * 
	 * AI (include some graph theory??)
	 * 
	 */

	public GoBoard() {
		
		setBackground(Color.LIGHT_GRAY);
		initInterPos();
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				
				/*
				 * a turn is adding a stone
				 * not allowed to place where it has no liberties except if ko
				 * review/improve snapshot thing
				 */
				if (e.getClickCount() == 2) {	//locate 
					for (int i = 0; i < interPos.length; i++) {
						for (int j = 0; j < interPos[0].length; j++) {
							
							if (getMousePosition().distance(interPos[i][j]) < RADIUS && stones[i][j] == null) {
								stones[i][j] = new Stone(turn, i, j);
								orderedStoneList.add(stones[i][j]);
								history.add(snapShot(stones));
								turn = (turn == 'B') ? 'W' : 'B';
								stones[i][j].setNumber(current);
								current++;
							}
						}
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON3 && e.isControlDown()) {
					for (int i = 0; i < interPos.length; i++) {
						for (int j = 0; j < interPos[0].length; j++) {
							
							if (getMousePosition().distance(interPos[i][j]) < RADIUS && stones[i][j] != null) {
								orderedStoneList.set(stones[i][j].getNumber(), null);
								stones[i][j] = null;
								//deleted from the ordered stone list (deal with current var to account for gaps in succession)
							}
						}
					}
				}
				
				updateGroups();
				updateLiberties();
				checkRules();
				updateScores();
				repaint();
			}
		});
		
		setFocusable(true);
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					turn = (turn == 'B') ? 'W' : 'B';
				}
				else if (e.getKeyCode() == KeyEvent.VK_LEFT && current > 0) {
					stones = history.get(--current);
				}
				else if (e.getKeyCode() == KeyEvent.VK_RIGHT && current < history.size()-1) {
					stones = history.get(++current);
				}
				repaint();
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == 'n')
					displayNumbers = (displayNumbers) ? false : true;
			}
		});
		
		history.add(snapShot(stones));
	}
	
	protected void paintComponent(Graphics g0) {
		super.paintComponent(g0);
			
		Graphics2D g = (Graphics2D)g0;        
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHints(rh);
        
		if (turn == 'W') 
			g.setColor(Color.WHITE);
		
		g.fillOval(0, 0, RADIUS*2, RADIUS*2);
		g.setColor(new Color(255, 153, 51));
		g.fillRect(X, Y, (stones[0].length-1)*CELLWIDTH, (stones.length-1)*CELLHEIGHT);
		
		g.setColor(Color.BLACK);
		
		for (int i = 0; i < stones.length; i++) {
			g.drawLine((int)interPos[i][0].getX(), (int)interPos[i][0].getY(), (int)interPos[i][interPos[i].length-1].getX(), (int)interPos[i][interPos[i].length-1].getY());
		}
				
		for (int i = 0; i < stones.length; i++) {
			g.drawLine((int)interPos[0][i].getX(), (int)interPos[0][i].getY(), (int)interPos[interPos.length-1][i].getX(), (int)interPos[interPos.length-1][i].getY());
		}
		
		//use ordered stone list instead for this and other stuff too?
		for (int i = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[0].length; j++) {
				
				if (stones[i][j] == null)
					continue;
				
				if (stones[i][j].getColor() == 'W')
					g.setColor(Color.WHITE);
				else g.setColor(Color.BLACK);
				
				g.fillOval((int)interPos[i][j].getX()-RADIUS, (int)interPos[i][j].getY()-RADIUS, RADIUS*2, RADIUS*2);
				
				if (displayNumbers) {
					
			        g.setColor(Color.BLACK);
			        g.drawString("captured "+capturedBlack, getWidth()/2-50, 15);
			        g.drawString("territory "+territoryBlack, getWidth()/2-50, 35);
			        g.drawString("score "+scoreBlack, getWidth()/2-50, 55);

			        g.setColor(Color.WHITE);
			        g.drawString("captured "+capturedWhite, getWidth()/2+50, 15);
			        g.drawString("territory "+territoryWhite, getWidth()/2+50, 35);
			        g.drawString("score "+scoreWhite, getWidth()/2+50, 55);
			        
					if (stones[i][j].getColor() == 'W')
						g.setColor(Color.BLACK);
					else g.setColor(Color.WHITE);

					int adjust = 3;

					if (stones[i][j].getNumber() >= 99)
						adjust = 9;
					else if (stones[i][j].getNumber() >= 9)
						adjust = 7;

					g.drawString(""+(stones[i][j].getNumber()+1), (int)interPos[i][j].getX()-adjust, (int)interPos[i][j].getY()+4);
				}
			}
		}
	}
	
	//count deleted stones for scores
	private void checkRules() {
		
		//check individual stones
		for (int i = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[0].length; j++) {
				
				if (stones[i][j] == null)
					continue;
				
				if (stones[i][j].getLiberties() == 0 && stones[i][j].getGroup() == null) {
					
					if (stones[i][j].getColor() == 'B')
						capturedWhite++;
					else capturedBlack++;
					
					orderedStoneList.set(stones[i][j].getNumber(), null);	
					stones[i][j] = null;
					history.add(snapShot(stones));
//					current++;	FIND ANOTHER WAY FOR SNAPSHOTS
				}
			}
		}
		
		//now check groups
		for (int i = 0; i < groups.size(); i++) {
			
			boolean dead = true;
			
			for (int j = 0; j < groups.get(i).size(); j++) {
				if (groups.get(i).get(j).getLiberties() != 0) {
					dead = false;
					break;
				}
			}
			
			if (dead) {
				
				if (groups.get(i).getColor() == 'B')
					capturedWhite += groups.get(i).size();
				else capturedBlack += groups.get(i).size();
				
				for (int j = 0; j < groups.get(i).size(); j++) {
					orderedStoneList.set(groups.get(i).get(j).getNumber(), null);
					stones[groups.get(i).get(j).getRow()][groups.get(i).get(j).getColumn()] = null;
				}
				
				groups.remove(i);
			}
		}
		
//		System.out.println(groups.size());
	}
	
	private void updateGroups() {
		
		for (int i = 0; i < orderedStoneList.size(); i++) {
						
			if (orderedStoneList.get(i) == null)
				continue;
			
			Stone currentStone = orderedStoneList.get(i);
			
			for (int j = 0; j < orderedStoneList.size(); j++) {
				
				if (orderedStoneList.get(j) == null)
					continue;
				
				Stone otherStone = orderedStoneList.get(j);

				//check all other stones if abs(j-j) == 1 or abs(i-i) == 1
				int rowDis = Math.abs(currentStone.getRow()-otherStone.getRow());
				int columnDis = Math.abs(currentStone.getColumn()-otherStone.getColumn());
				
				if (rowDis <= 1 && columnDis <= 1 && rowDis != columnDis) {
					
					/*when same color
					 * 
					 * if both no groups, create one including both
					 * 
					 * else if current has group but other doesnt, add other to the group
					 * 
					 * else if other has group but current doesnt, add current to other's group
					 * 
					 * else both have groups, merge the groups into one new group
					 */
					
					if (currentStone.getColor() != otherStone.getColor())
						continue;
					
					if (currentStone.getGroup() == null && otherStone.getGroup() == null) {
						
						Group group = new Group(currentStone, otherStone);
						groups.add(group);
					}
					else if (currentStone.getGroup() != null && otherStone.getGroup() == null) {
						
						currentStone.getGroup().add(otherStone);
					}
					else if (otherStone.getGroup() != null && currentStone.getGroup() == null) {
						
						otherStone.getGroup().add(currentStone);
					}
					else {
						
						if (currentStone.getGroup() != otherStone.getGroup()) {
							
							groups.remove(otherStone.getGroup());
							currentStone.getGroup().merge(otherStone.getGroup());
						}
					}
				}
			}
		}
		
//		for (int i = 0; i < groups.size(); i++) {
//			for (int j = 0; j < groups.get(i).size(); j++) {
//				if (groups.get(i).get(j) == null) {
//					groups.get(i).remove(j);
//				}
//			}
//		}
		
		for (int i = 0; i < groups.size(); i++) {
				if (groups.get(i).size() < 2)
					groups.remove(i);
		}
	}
	
	private void updateLiberties() {
		
		for (int i = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[0].length; j++) {

				int libs = 4;

				//above
				try {
					if (stones[i][j].getGroup() == null) {
						if (stones[i-1][j].getColor() != stones[i][j].getColor())
							libs--;
					}
					else {
						if (stones[i-1][j] != null)
							libs--;
					}	
				}
				catch (NullPointerException ne) {
				}
				catch (ArrayIndexOutOfBoundsException ae) {
					libs--;
				}

				//below
				try {
					if (stones[i][j].getGroup() == null) {
						if (stones[i+1][j].getColor() != stones[i][j].getColor())
							libs--;
					}
					else {
						if (stones[i+1][j] != null)
							libs--;
					}	
				}
				catch (NullPointerException ne) {
				}
				catch (ArrayIndexOutOfBoundsException ae) {
					libs--;
				}

				//left
				try {
					if (stones[i][j].getGroup() == null) {
						if (stones[i][j-1].getColor() != stones[i][j].getColor())
							libs--;
					}
					else {
						if (stones[i][j-1] != null)
							libs--;
					}	
				}
				catch (NullPointerException ne) {
				}
				catch (ArrayIndexOutOfBoundsException ae) {
					libs--;
				}

				//right
				try {
					if (stones[i][j].getGroup() == null) {
						if (stones[i][j+1].getColor() != stones[i][j].getColor())
							libs--;
					}
					else {
						if (stones[i][j+1] != null)
							libs--;
					}	
				}
				catch (NullPointerException ne) {
				}
				catch (ArrayIndexOutOfBoundsException ae) {
					libs--;
				}

				if (stones[i][j] != null) {
					if (libs == 0) {
						if (stones[i][j].getNumber() != current-1) 
							stones[i][j].setLiberties(libs);
					}
					else 
						stones[i][j].setLiberties(libs);
				}
			}
		}
	}
	
	/** NOT THE RIGHT WAY TO COMPUTE TERRITORY, A PATCH OF TERRITORY DOESNT HAVE TO BE SURROUNDED BY THE SAME GROUP (SAME COLOR THAT COUNTS) */
	
	/*
	 * number of empty points only your stones surround	<<-- 
	 * minus empty points surrounded in seki
	 * minus number of your stones that have been captured 
	 */
	private int countTerritory(Group g) {
		
		int count = 0;
		
		/*
		 * counts territory enclosed by the given group
		 * 
		 * check every null tile
		 * if enclosed in 4 dir, +1 to territory of that group 
		 * if enclosed by stones of that group (parameter g)
		 */
		for (int i = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[0].length; j++) {
				
				if (stones[i][j] != null)
					continue;
				
				edgeCounter = 0;
							
//				System.out.println(projectDir(i, j, -1, 0, g) + " " + projectDir(i, j, 1, 0, g) + " " +
//						projectDir(i, j, 0, 1, g) + " " + projectDir(i, j, 0, -1, g));
				
				if (projectDir(i, j, -1, 0, g) && projectDir(i, j, 1, 0, g) && projectDir(i, j, 0, 1, g) && projectDir(i, j, 0, -1, g)) {
					
					if (edgeCounter <= 2)
						count++;
				}
			}
		}
		
		return count;
	}
	
	//tells if the null position is enclosed by the given group in that direction
	//r: increment amount for rows, c: increment amount for columns
	private boolean projectDir(int i, int j, int r, int c, Group enclosingGroup) {
		
		while (i >= 0 && i < stones.length && j >= 0 && j < stones[0].length) {
			
//			System.out.println("i " + i + " j " + j);

			if (stones[i][j] != null) {

//				System.out.println("HEREEEEEEEEEEEEEEEEEEEEE");
				if (stones[i][j].getGroup() == enclosingGroup)	/** doesnt have to be same enclosing group, needs to be same color */
					return true;
				else
					return false;
			}
				
			i += r;
			j += c;
		}
		
//		System.out.println("EDGE");
		//edge of board was reached
		edgeCounter++;
		return true;
	}
	
	private void updateTerritoryCounts() {
	
		territoryBlack = 0;
		territoryWhite = 0;
		
		for (int i = 0; i < groups.size(); i++) {
			
			if (groups.get(i).getColor() == 'B')
				territoryBlack += countTerritory(groups.get(i));
			else territoryWhite += countTerritory(groups.get(i));
		}
	}
	
	private void updateScores() {
		updateTerritoryCounts();
		scoreBlack = territoryBlack - capturedWhite;
		scoreWhite = territoryWhite - capturedBlack;
	}
	
	private void initInterPos() {
		
		for (int i = 0, y = Y; i < stones.length; i++, y += CELLHEIGHT) {
			for (int j = 0, x = X; j < stones[0].length; j++, x += CELLWIDTH) {
				interPos[i][j] = new Point(x, y);
			}
		}
	}
	
	private Stone[][] snapShot(Stone[][] stones) {
		
		Stone[][] temp = new Stone[stones.length][stones[0].length];
		
		for (int i = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[0].length; j++) {
				temp[i][j] = stones[i][j];
			}
		}
		
		return temp;
	}
}
