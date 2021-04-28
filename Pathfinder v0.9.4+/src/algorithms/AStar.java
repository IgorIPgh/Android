package algorithms;

import java.util.ArrayList;
import java.util.HashSet;

import android.widget.Toast;
import entities.Entity;
import entities.Node;
import entities.Obstacle;
import entities.SideNode;
import math.Heap;
import math.Point;
import views.AnimatePath;

public class AStar {
	
	public static String FILE_NAME = "positions.txt";
	
	Point vector = new Point();
	Node node;
	Entity player;
	Entity target;
	ArrayList<Obstacle> o;
	Thread background;
	public ArrayList<Point> savedPositions = new ArrayList<Point>();
	int iterations;
	boolean isRunning = false;
	boolean dtype = false;
	private boolean neighbours;
	
	Point selectedNode = new Point();
	
	AnimatePath view;
	
	public int gridSize, xOffSet, x0, dd;

	public AStar(Entity target, Entity player, ArrayList<Obstacle> o, AnimatePath view, boolean neighbours, boolean d, int dd) {
		this.target = target;
		this.player = player;
		this.dd = dd;
		this.o = o;
		this.neighbours = neighbours;
		dtype = d;
		selectedNode = player.getPosition();
		this.view = view;
	}
	
	public ArrayList<Point> findPath(Point startPos, Point targetPos) {
		savedPositions.clear();
		final Node startNode = new Node((int) startPos.getX(), (int) startPos.getY());
		final Node targetNode = new Node((int) targetPos.getX(), (int) targetPos.getY());
		ArrayList<Obstacle> gridObstacles = o;
		
		Heap openSet = new Heap(view.getGridSize() * view.getGridSize());
		HashSet<Node> closedSet = new HashSet<Node>();
		openSet.add(startNode);
		iterations = 0;
		int maxIterations = 0x4000 /*(int) Math.pow(2, 14)*/;
		
		long startTime = System.currentTimeMillis();
		
		while(openSet.getCount() > 0) {
			Node currentNode = openSet.removeBottom();
			closedSet.add(currentNode);
			iterations++;
			
			if(iterations > maxIterations) {
				view.makeToast("Невозможно найти путь!", Toast.LENGTH_LONG).show();
				return null;
			}
			
			// if(Math.abs(currentNode.getX() - targetNode.getX()) == 1  && Math.abs(currentNode.getY() - targetNode.getY()) == 1) {
			if(currentNode.getX() == targetNode.getX() && currentNode.getY() == targetNode.getY()) {
				targetNode.setParent(currentNode);
				long lastTime = System.currentTimeMillis();
				int diff = (int) (lastTime - startTime);
				view.setInfo(iterations, diff);
				savedPositions = retracePath(startNode, targetNode);
				
				return savedPositions;
			}
		
			boolean foundObstacle = false;
			ArrayList<Node> neighbours = view.getNeighbours(currentNode);
			for(Node neighbour : neighbours) {
				for(Obstacle o : gridObstacles) {
					if(!neighbour.isWalkable(o) || closedSet.contains(neighbour)) {
						foundObstacle = true;
						/* if(view.checkObstacle(o)) {
							savedPositions = retracePath(startNode, lastNode);
							view.makeToast("Невозможно пройти!", Toast.LENGTH_LONG);
						} */
					}
				}
				
				if(foundObstacle) {
					foundObstacle = false;
					continue;
				}
				
				if(this.neighbours)
					view.drawSideNode(new SideNode(neighbour.getX(), neighbour.getY()));
				int distance = dtype ? getEDistance(currentNode, neighbour) : getMDistance(currentNode, neighbour);
				int newMovementCost = currentNode.getgCost() + distance;
				if(newMovementCost < neighbour.getgCost() || !openSet.contains(neighbour)) {
					neighbour.setgCost(newMovementCost);
					neighbour.sethCost(dtype ? getEDistance(neighbour, targetNode) : getMDistance(neighbour, targetNode));
					neighbour.setParent(currentNode);
					if(!openSet.contains(neighbour))
						openSet.add(neighbour);
				}
			}
		}
		
		return savedPositions;
	}
	
	public ArrayList<Point> getSavedPositions() {
		if(!savedPositions.isEmpty())
			return savedPositions;
		
		return null;
	}
	
	public ArrayList<Point> retracePath(Node startNode, Node endNode) {
		ArrayList<Node> path = new ArrayList<Node>();
		ArrayList<Node> checkpoints = new ArrayList<Node>();
		ArrayList<Point> positions = new ArrayList<Point>();
		Node currentNode = endNode;
		
		while(!currentNode.equals(startNode)) {
			path.add(currentNode);
			currentNode = currentNode.getParent();
		}
		
		for(int i = 0; i <= path.size() - 1; i++) {
			checkpoints.add(i, path.get(path.size() - 1 - i));
		}
		
		for(int j = 0; j <= checkpoints.size() - 1; j++) {
			positions.add(checkpoints.get(j).getPosition());
		}
		
		return positions;
	}
	
	public int getMDistance(Node a, Node b) {
		int distanceX = Math.abs(a.getX() - b.getX());
		int distanceY = Math.abs(a.getY() - b.getY());
		
		int d = Node.DIAGONAL_COST;
		int h = Node.CELL_COST;
		
		if(distanceX > distanceY) 
			return d * distanceY + h * (distanceX - distanceY);
		else 
			return d * distanceX + h * (distanceY - distanceX);
	}
	
	public int getEDistance(Node a, Node b) {
		int dx = Math.abs(a.getX() - b.getX());
		int dy = Math.abs(a.getY() - b.getY());
		
		int h = Node.CELL_COST;
		
		return (int) Math.sqrt(dx * dx + dy * dy) * h;
	}

	public Point getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(Point selectedNode) {
		this.selectedNode = selectedNode;
	}

}