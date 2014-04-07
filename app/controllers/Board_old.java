package player;

/**
 * Represents the game board.
 */
public class Board {

    public Chip[][] board;
    public int [] numChips; 

    /**
     * Construct a board with no chips placed on it
     */
    public Board() {
        board = new Chip[8][8];
        numChips = new int[2]; //{player 0, player 1}
    }

    /**
     * Take the move for the given player.
     * Assumes that the move is valid (i.e. STEP moves don't try to move opponents' pieces,
     * nobody is trying to move into their opponent's goal, etc.)
     */
    public void makeMove(Move m, int player) {
        board[m.x1][m.y1] = new Chip(m.x1, m.y1, player);
	 numChips[player]++;

        // If this is a STEP move, then remove the old chip
        if (m.moveKind == Move.STEP) {
            board[m.x2][m.y2] = null;
            checkLinks(m.x2, m.y2);
	    numChips[player]--;
        }
        checkLinks(m.x1, m.y1);
    }
    /**
     * Undoes the given move. Assumes that the given move was the last excecuted move
     */
    public void undoMove(Move m) { 
        board[m.x1][m.y1] = null;
	 numChips[board[m.x1][m.y1].getPlayer()]--;
        // If this is a STEP move, then replace the old chip
        if (m.moveKind == Move.STEP) {
            board[m.x2][m.y2] = new Chip(m.x2, m.y2, board[m.x1][m.y1].getPlayer());
            checkLinks(m.x2, m.y2);
	    numChips[board[m.x1][m.y1].getPlayer()]++;
        }
        checkLinks(m.x1, m.y1);
    }

    /*
     * @return
     */
    private boolean checkNeighbors(Move m, int player, boolean add) {
		boolean skipDuplicates;

		Chip[] neighbors = new Chip[4];
		int len = 0;
		int iMax = m.x1+1;
		int iMin = m.x1-1;
		int jMax = m.y1+1;
		int jMin = m.y1-1;
		if (m.x1-1 < 0)
			iMin = 0;
		if (m.x1+1 > 7)
			iMax = 7;
		if (m.y1-1 < 0)
			jMin = 0;
		if (m.y1+1 > 7)
			jMax = 0;
		for (int i = iMin; i <= iMax; i++) {
			for (int j = jMin; j <= jMax; j++) {
					if (add) {
						skipDuplicates = true;
					}
					else {
						skipDuplicates = !(i == m.x2 && j == m.y2);
					}
				if (board[i][j] != null && board[i][j].getPlayer() == player && skipDuplicates) {
					//System.out.println("neighbor " + board[i][j]);
					neighbors[len] = board[i][j];
					len++;
				}
				if (len > 1) {
					//System.out.println("2.more than one immediate neighbor");
					return false; // more than one immediate neighbor
				}
			}
		}
		
		for (Chip c: neighbors) { // check neighbors of neighbors
			//System.out.println(c);
			len = 0;
			if (c != null){
			iMin = c.getX()-1;
			iMax = c.getX()+1;
			jMin = c.getY()-1;
			jMax = c.getY()+1;
			if (iMin < 0)
				iMin = 0;
			if (iMax > 7)
				iMax = 7;
			if (jMin < 0)
				jMin = 0;
			if (jMax > 7)
				jMax = 7;
			for (int i = iMin; i <= iMax; i++) {
				for (int j = jMin; j <= jMax; j++) { //waait..
					//System.out.println("i: " + i + ", j: "+j);
					if (add) {
						skipDuplicates = !(i==m.x1 && j==m.y1);
					}
					else {
						skipDuplicates = !(i == m.x2 && j == m.y2) && !(i==m.x1 && j==m.y1);
					}
					if (board[i][j] != null && board[i][j].getPlayer() == player && skipDuplicates) {
						neighbors[len] = board[i][j];
						//System.out.println(board[i][j] + " neighbor");
						len++;
					}
					if (len > 1) {
						//System.out.println("cluster " + len);
						return false; // cluster
					}
				}
			}
			}
		}
		return true;
    }

    /**
     * @return true if m does not violate any rules
     */

    public boolean isValidMove(Move m, int player) {
        if (m.x2 == 0 && m.y2 == 0 && m.x1 == 0 && m.y1 == 0) // quit
                return true;
	 else if ((m.x1 == 0 && (m.y1 == 0 || m.y1 == 7)) || (m.x1 == 7 && (m.y1 == 0 || m.y1 == 7)))
		return false; // corner
	else if (player == 1 && (m.y1 == 7 || m.y1 == 0))
		return false; //wrong goal area
	else if (player == 2 && (m.x1 == 7 || m.x1 == 0))
		return false; //wrong goal area
	else if (board[m.x1][m.y1] != null)
		return false; //not an empty square
        else if (m.x2 == 0 && m.y2 == 0) { // add
		if (numChips[player] == 0)
			return true;
		return checkNeighbors(m, player, true);
		
        }
        else { //step
		//System.out.println("step..");
		if (numChips[player] < 10) {
			System.out.println("can't step yet " + numChips[player]);
			return false;
		}
             	else if (board[m.x2][m.y2] == null){ // old position has no chip 
			//System.out.println("old position has no chip");
                    return false;
		}
		else if (m.x2 == m.x1 && m.y2 == m.y1) {
		    //System.out.println("can't stay in same place");
		    return false; // can't stay in same place
		}
		return checkNeighbors(m, player, false);
                
        }
    }


    /**
     * Returns an array of all of the given player's potential moves.
     * The array may contain null values at the end, if there are not enough valid moves to fill it up.
     */
    public Move[] validMoves(int player) {
        Move [] moves = new Move[200]; // 
        int index = 0;

        if (numChips[player] < 10) { // we need to add

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if (board[i][j] == null) {
                        Move m = new Move(i, j);
                        if (isValidMove(m, player)) {
                            moves[index] = m;
                            index++;
                        }
                    }   
                }
            }
        }

        else { // we need to step
 
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
    
                    if (board[i][j] != null) {
                        for (int a = 0; a < board.length; a++) {
                            for (int b = 0; b < board[0].length; b++) {
                                if (board[a][b] == null && !(a == i && b == j)) {
                                    Move m = new Move(a, b, i, j);
					//System.out.println(m);
                                    if (isValidMove(m, player)) {
                                        moves[index] = m;
                                        index++;
				    	 }
                                    
                                }
                            }
                        }
                    }   
                }
            }
        }
        return moves;
    }

    /*
     *returns a score for the board state
     */
    public static int score(Board b, int player) {
       int score = 0;
	if (b.winningNetwork(player) != null) {
		return Integer.MAX_VALUE;
	}
	if (b.winningNetwork((player + 1) % 2) != null) {
		return Integer.MIN_VALUE;
	}
	for (int i = 0; i < b.board.length; i++) {
	    for (int j = 0; j < b.board[0].length; j++) {
		 if (b.board.numChips[player] < 2)
               if (b.board[i][j] != null) {
                   if (b.board[i][j].getPlayer() == player) {
                       score += b.board[i][j].connections().length; //need to change because it counts more than pairs.
                   }
                   else {
                       score -= b.board[i][j].connections().length; //need to change
                   }
               }
           }
	}
	return score;
               
    }
    /**
     * Returns an array of Chips representing a winning (6-chip) network
     * or null if no such network exists
     */
    public Chip[] winningNetwork(int player) {
        // Black (0)'s starting goal zone is y = 0
        // White (1)'s starting goal zone is x = 0
        // Obviously, there's no need to check x or y = 7, since
        // a winning network has one node from each side
        Chip[] usedNodes = new Chip[10];
        if (player == 0) {
            // Skip the corners
            for (int x = 1; x < 7; x++) {
                if (board[x][0] != null) {
                    usedNodes[0] = board[x][0];
                    // Iterate through the first node's connections
                    // and try to find subnetworks of 5 nodes
                    Chip[] conns = board[x][0].connections();
                    for (int i = 0; i < 8; i++) {
                        if (conns[i] == null) {
                            continue;
                        }
                        Chip[] subnet = subNetwork(i, conns[i], 5, usedNodes, 1, player);
                        if (subnet != null) {
                            Chip[] net = new Chip[subnet.length + 1];
                            System.arraycopy(subnet, 0, net, 1, subnet.length);
                            net[0] = board[x][0];
                            return net;
                        }
                    }
                }
            }
        } else {
            // Skip the corners
            for (int y = 1; y < 7; y++) {
                if (board[0][y] != null) {
                    usedNodes[0] = board[0][y];
                    // Iterate through the first node's connections
                    // and try to find subnetworks of 5 nodes
                    Chip[] conns = board[0][y].connections();
                    for (int i = 0; i < 8; i++) {
                        if (conns[i] == null) {
                            continue;
                        }
                        Chip[] subnet = subNetwork(i, conns[i], 5, usedNodes, 1, player);
                        if (subnet != null) {
                            Chip[] net = new Chip[subnet.length + 1];
                            System.arraycopy(subnet, 0, net, 1, subnet.length);
                            net[0] = board[0][y];
                            return net;
                        }
                    }
                }
            }
        }
        // No winning network found
        return null;
    }
    // Recursively returns the part of the winning network for the given player
    // starting at the given Chip, assuming they need to use remainingNodes
    // more chips (including start)
    // previousDirection is the direction we entered start from
    // usedNodes is the list of Chips already in this network (it always has length 10, since we
    // only have 10 Chips at most; indices numUsedNodes through 9 are garbage data)
    private Chip[] subNetwork(int previousDirection, Chip start, int remainingNodes, Chip[] usedNodes, int numUsedNodes, int player) {
        if (remainingNodes == 1 && (player == 0 ? (start.getY() == 7) : (start.getX() == 7))) {
            return new Chip[] {start};
        }
        // Iterate through start's connections (excluding the one at conns[previousDirection])
        // and look for subnetworks
        Chip[] conns = start.connections();
        outer:
        for (int i = 0; i < 7; i++) {
            if (i != previousDirection) {
                if (conns[i] == null) {
                    continue;
                }
                // Make sure we haven't already used this node
                for (int j = 0; j < numUsedNodes; j++) {
                    if (usedNodes[j] == conns[i]) {
                        continue outer;
                    }
                }
                usedNodes[numUsedNodes] = conns[i];
                Chip[] subsubnet = subNetwork(i, conns[i], remainingNodes - 1, usedNodes, numUsedNodes + 1, player);
                if (subsubnet != null) {
                    // Yay!
                    Chip[] subnet = new Chip[subsubnet.length + 1];
                    System.arraycopy(subsubnet, 0, subnet, 1, subsubnet.length);
                    subnet[0] = start;
                    return subnet;
                }
            }
        }
        // Aww, much sadness.
        return null;
    }

    // Iterates vertically, horizontally, and diagonally from (x, y)
    // making sure that the middle two Chips from (x, y) are linked
    // to each other, if this is a null position, or to the chip at (x, y) if there is one
    private void checkLinks(int x, int y) {
        Chip n = null, s = null, nw = null, se = null, w = null, e = null, sw = null, ne = null;

        Chip[] surround = getSurroundings(x, y, Chip.SOUTH);
        n = surround[0];
        s = surround[1];
        surround = getSurroundings(x, y, Chip.SOUTHEAST);
        nw = surround[0];
        se = surround[1];
        surround = getSurroundings(x, y, Chip.EAST);
        w = surround[0];
        e = surround[1];
        surround = getSurroundings(x, y, Chip.NORTHEAST);
        sw = surround[0];
        ne = surround[1];

        doLinks(n, board[x][y], s, Chip.SOUTH);
        doLinks(nw, board[x][y], se, Chip.SOUTHEAST);
        doLinks(w, board[x][y], e, Chip.EAST);
        doLinks(sw, board[x][y], ne, Chip.NORTHEAST);
    }
    // Gets the first two Chips in direction and 7 - direction, other than the Chip at (x, y)
    private Chip[] getSurroundings(int x, int y, int direction) {
        int xInc, yInc;
        switch (direction) {
        case Chip.NORTH:
            xInc = 0;
            yInc = -1;
            break;
        case Chip.SOUTH:
            xInc = 0;
            yInc = 1;
            break;
        case Chip.NORTHWEST:
            xInc = -1;
            yInc = -1;
            break;
        case Chip.SOUTHEAST:
            xInc = 1;
            yInc = 1;
            break;
        case Chip.WEST:
            xInc = -1;
            yInc = 0;
            break;
        case Chip.EAST:
            xInc = 1;
            yInc = 0;
            break;
        case Chip.SOUTHWEST:
            xInc = -1;
            yInc = 1;
            break;
        case Chip.NORTHEAST:
            xInc = 1;
            yInc = -1;
            break;
        default:
            xInc = 0;
            yInc = 0;
        }
        Chip up = null, down = null;
        int i = x + xInc, j = y + yInc;
        outerdown:
        while (i > -1 && i < 8 && j > -1 && j < 8) {
            if (board[i][j] != null) {
                down = board[i][j];
                break outerdown;
            }
            i += xInc;
            j += yInc;
        }
        i = x - xInc;
        j = y - yInc;
        outerup:
        while (i > -1 && i < 8 && j > -1 && j < 8) {
            if (board[i][j] != null) {
                up = board[i][j];
                break outerup;
            }
            i -= xInc;
            j -= yInc;
        }
        return new Chip[] {up, down};
    }
    // Direction is defined in Chip
    // Links left to right if mid is null; links left to mid and right to mid otherwise
    // directin points from left to right
    private void doLinks(Chip left, Chip mid, Chip right, int direction) {
        if (left != null) {
            if (mid == null) {
                left.connect(right, direction);
            } else {
                left.connect(mid, direction);
                mid.connect(right, direction);
            }
        } else {
            if (mid != null) {
                mid.connect(right, direction);
            }
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                s.append(board[j][i]);
                s.append(' ');
            }
            s.append('\n');
        }
        return s.toString();
    }
}
