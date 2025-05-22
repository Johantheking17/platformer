package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
				else if (values[x][y] == 19)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
				else if (values[x][y] == 20)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
				else if (values[x][y] == 21)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
			}

		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
					else
						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}

			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}
	
	
	//#############################################################################################################
	//Your code goes here! 
	//Please make sure you read the rubric/directions carefully and implement the solution recursively!
	
	//precondition: there is a map and a flower is hit
	//postcondition: There is a flow of water coming from the flower

	    private void water(int col, int row, Map map, int fullness) {
        // If the current tile is already water and the existing water is the same or fuller, return
        if (map.getTiles()[col][row] instanceof Water) {
            Water existingWater = (Water) map.getTiles()[col][row];
            if (existingWater.getFullness() >= fullness) return;
        }

        // Determine the appropriate water image based on fullness
        String imageName;
        switch (fullness) {
            case 3:
                imageName = "Full_water";
                break;
            case 2:
                imageName = "Half_water";
                break;
            case 1:
                imageName = "Quarter_water";
                break;
            default:
                imageName = "Falling_water";
                break;
        }

        // Create and add the water block to the map
        Water w = new Water(col, row, tileSize, tileset.getImage(imageName), this, fullness);
        map.addTile(col, row, w);

        // Check if water can flow down
        if (row + 1 < map.getTiles()[col].length && !(map.getTiles()[col][row + 1] instanceof Water) && !map.getTiles()[col][row + 1].isSolid()) {
            water(col, row + 1, map, 0); // Flow down as falling water
        } else if (fullness == 0 && row + 1 < map.getTiles()[col].length && map.getTiles()[col][row + 1].isSolid()) {
            // If falling water reaches a flat platform, turn into full water block and spread
            map.addTile(col, row, new Water(col, row, tileSize, tileset.getImage("Full_water"), this, 3));
            // Spread as a full water block
            spreadAsFullWater(col, row, map, 3);
        } else {
            // If water can't flow down, it should flow left and right
            spreadAsFullWater(col, row, map, fullness);
        }
    }

	//spread as water that is full and on the ground
    private void spreadAsFullWater(int col, int row, Map map, int fullness) {
        // Flow left
        if (col - 1 >= 0 && !(map.getTiles()[col - 1][row] instanceof Water) && !map.getTiles()[col - 1][row].isSolid()) {
            water(col - 1, row, map, fullness > 1 ? fullness - 1 : 1);
        }

        // Flow right
        if (col + 1 < map.getTiles().length && !(map.getTiles()[col + 1][row] instanceof Water) && !map.getTiles()[col + 1][row].isSolid()) {
            water(col + 1, row, map, fullness > 1 ? fullness - 1 : 1);
        }
    }

	//Adds gas tiles until the requisite number of squares are filled or there is no more room 

	//precondition: a flower is hit by the player
	//postcondition: there is gas created in a rectagle around the flower for as many blocks as chosen
	private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound) {
		
		Gas g = new Gas(col, row, tileSize, tileset.getImage("GasOne"), this, 0);
		map.addTile(col, row, g);
		numSquaresToFill--;
		placedThisRound.add(g);

		while(numSquaresToFill > 0 && placedThisRound.size() > 0){
			Gas checked = placedThisRound.get(0);

			int ro = checked.getRow();
			int co = checked.getCol();
			if (ro-1 >=0 && !map.getTiles()[co][ro-1].isSolid() && !(map.getTiles()[co][ro-1] instanceof Gas)){
				Gas temp = new Gas(co, ro-1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(co,ro-1, temp);
				numSquaresToFill--;
				placedThisRound.add(temp);
			}
			
			if (ro-1 >=0 && co+1 < map.getTiles().length && !map.getTiles()[co+1][ro-1].isSolid() && numSquaresToFill > 0 && !(map.getTiles()[co+1][ro-1] instanceof Gas)){
				Gas temp = new Gas(co+1, ro-1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(co+1,ro-1, temp);
				numSquaresToFill--;
				placedThisRound.add(temp);
			}

			if (ro-1 >=0 && co-1 >= 0 && !map.getTiles()[co-1][ro-1].isSolid() && numSquaresToFill > 0 && !(map.getTiles()[co-1][ro-1] instanceof Gas)){
				Gas temp = new Gas(co-1, ro-1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(co-1,ro-1, temp);
				numSquaresToFill--;
				placedThisRound.add(temp);
			}

			if (co+1 < map.getTiles().length && !map.getTiles()[co+1][ro].isSolid() && numSquaresToFill > 0 && !(map.getTiles()[co+1][ro] instanceof Gas)){
				Gas temp = new Gas(co+1, ro, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(co+1,ro, temp);
				numSquaresToFill--;
				placedThisRound.add(temp);
			}

			if (co-1 >= 0 && !map.getTiles()[co-1][ro].isSolid() && numSquaresToFill > 0 && !(map.getTiles()[co-1][ro] instanceof Gas)){
				Gas temp = new Gas(co-1, ro, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(co-1,ro, temp);
				numSquaresToFill--;
				placedThisRound.add(temp);
			}

			if (ro+1 < map.getTiles()[co].length && !map.getTiles()[co][ro+1].isSolid() && numSquaresToFill > 0 && !(map.getTiles()[co][ro+1] instanceof Gas)){
				Gas temp = new Gas(co, ro+1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(co, ro+1, temp);
				numSquaresToFill--;
				placedThisRound.add(temp);
			}

			if (ro+1 < map.getTiles()[co].length && co+1 < map.getTiles().length && !map.getTiles()[co+1][ro+1].isSolid() && numSquaresToFill > 0 && !(map.getTiles()[co+1][ro+1] instanceof Gas)){
				Gas temp = new Gas(co+1, ro+1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(co+1,ro+1, temp);
				numSquaresToFill--;
				placedThisRound.add(temp);
			}

			if (ro+1 < map.getTiles()[co].length && co-1 >= 0 && !map.getTiles()[co-1][ro+1].isSolid() && numSquaresToFill > 0 && !(map.getTiles()[co-1][ro+1] instanceof Gas)){
				Gas temp = new Gas(co-1, ro+1, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(co-1,ro+1, temp);
				numSquaresToFill--;
				placedThisRound.add(temp);
			}

			placedThisRound.remove(0);

			
			//consider a  spot thats in placedThisRound.get(0)
			//and try to add gas all around it
			//add any new gass you create to placedThisRound
			//remove the gass tile just processed from placedThisRound

		}

	}	

	public void draw(Graphics g) {
	   	 g.translate((int) -camera.getX(), (int) -camera.getY());
	   	 // Draw the map
	   	 for (int x = 0; x < map.getWidth(); x++) {
	   		 for (int y = 0; y < map.getHeight(); y++) {
	   			 Tile tile = map.getTiles()[x][y];
	   			 if (tile == null)
	   				 continue;
	   			 if(tile instanceof Gas) {
	   				
	   				 int adjacencyCount =0;
	   				 for(int i=-1; i<2; i++) {
	   					 for(int j =-1; j<2; j++) {
	   						 if(j!=0 || i!=0) {
	   							 if((x+i)>=0 && (x+i)<map.getTiles().length && (y+j)>=0 && (y+j)<map.getTiles()[x].length) {
	   								 if(map.getTiles()[x+i][y+j] instanceof Gas) {
	   									 adjacencyCount++;
	   								 }
	   							 }
	   						 }
	   					 }
	   				 }
	   				 if(adjacencyCount == 8) {
	   					 ((Gas)(tile)).setIntensity(2);
	   					 tile.setImage(tileset.getImage("GasThree"));
	   				 }
	   				 else if(adjacencyCount >5) {
	   					 ((Gas)(tile)).setIntensity(1);
	   					tile.setImage(tileset.getImage("GasTwo"));
	   				 }
	   				 else {
	   					 ((Gas)(tile)).setIntensity(0);
	   					tile.setImage(tileset.getImage("GasOne"));
	   				 }
	   			 }
	   			 if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
	   				 tile.draw(g);
	   		 }
	   	 }
	

	   	 // Draw the enemies
	   	 for (int i = 0; i < enemies.length; i++) {
	   		 enemies[i].draw(g);
	   	 }


	   	 // Draw the player
	   	 player.draw(g);




	   	 // used for debugging
	   	 if (Camera.SHOW_CAMERA){
	   		 camera.draw(g);
	   	 g.translate((int) +camera.getX(), (int) +camera.getY());
	    }


		// Draw the enemies
		for (int i = 0; i < enemies.length; i++) {
			enemies[i].draw(g);
		}

		// Draw the player
		player.draw(g);

		// used for debugging
		if (Camera.SHOW_CAMERA)
			camera.draw(g);

		g.translate((int) +camera.getX(), (int) +camera.getY());
	}

	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
}