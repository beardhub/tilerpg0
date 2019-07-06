//set path=%path%;C:\Program Files\Java\jdk1.8.0_25\bin
//set path=%path%;D:\Java\JDK\jdk1.8.0_25\bin
//set path=%path%;E:\Java\JDK\jdk1.8.0_25\bin
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.Random;
import java.util.ArrayList;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
class Main implements ActionListener, KeyListener {
	public static Renderer renderer;
	public static Player player;
	public static GraphicsHandler graphics;
	public static Main main;
	public static JFrame frame;
	public static Board board;
	public static String title, saveFolder;
	public static int width, height;
	public static Random rand;
	public static Game game;
	public static ArrayList<DisplayBox> boxes;
	public static GodTools Godtools;
	public static DisplayBox godtools, playerdisp, boarddisp, inventorybox, minimapbox, itembox, borders, rightclickbox;
	public static RightClickMenu rightclick;
	public static MiniMap minimap;
	public static boolean GodMode;
	public static Point mousept;
	public static DroppedItems items;
	public static MouseReader mousereader;
	

	private Timer timer;
	public Main(){
		title = "Game";
		width = 1094+384;//-900;
		height = 1128;//-500;
		graphics = new GraphicsHandler();
		mousereader = new MouseReader();
		rand = new Random();
		mousept = new Point(0,0);


		GodMode = false;



		boolean newGame = true;
		boolean newPlayer = false;

		if (newGame) deleteSave("GameSave");
		game = (Game)load("SaveFiles//"+saveFolder+"//World//GameSave.dat");
		if (game==null) game = new Game();

		player = (Player)load("SaveFiles//"+saveFolder+"//Entities//PlayerSave.dat");
		if (player==null||newPlayer) player = new Player();

		minimap = new MiniMap();

		board = (Board)load("SaveFiles//"+saveFolder+"//World//Board.dat");
		if (board!=null)board.loadBoard(player.location);
		if (board==null) board = new Board(new WorldLocation("surface",player.location.worldX,player.location.worldY,
			player.location.chunkX,player.location.chunkY));
		
		if (load("SaveFiles//"+saveFolder+"//Entities//PlayerSave.dat")==null||newPlayer)
			player.spawnIn();

		Godtools = new GodTools();
		godtools = new DisplayBox(0,0,Godtools,width,height);
		playerdisp = new DisplayBox(0,0,player,64*17,64*17);
		boarddisp = new DisplayBox(0,0,board,64*17,64*17);
		inventorybox = new DisplayBox(1088,height-682,player.inventory,384,768);
		minimapbox = new DisplayBox(1088,64,minimap,64*6,64*6);
		items = new DroppedItems();
		itembox = new DisplayBox(0,0,items,64*17,64*17);
		borders = new DisplayBox(0,0,new Borders(),width,height);
		rightclick = new RightClickMenu();
		rightclickbox = new DisplayBox(0,0,rightclick,width,height);
		boxes = new ArrayList<DisplayBox>();
		boxes.add(boarddisp);
		boxes.add(inventorybox);
		boxes.add(minimapbox);
		boxes.add(itembox);
		boxes.add(playerdisp);
		boxes.add(godtools);
		boxes.add(borders);
		boxes.add(rightclickbox);
		//for (int i = 0; i < boxes.size(); i++)
		//	boxes.get(i).moveRelative(-900,-500);

		setup();
	}
	public static void main(String[] args){
		if (args.length>1&&args[0].equals("delete")){
			deleteSave(args[1]);
			System.exit(0);
		}
		else if (args.length>0) saveFolder = args[0];
		else saveFolder = "GameSave";
		if (!(args.length>1&&args[0].equals("delete"))) main = new Main();
	}
	private void setup(){
		renderer = new Renderer();
		renderer.setBackground(Color.white);
		renderer.addMouseListener(mousereader);
		renderer.addMouseMotionListener(mousereader);
		timer = new Timer(15,this);
		frame = new JFrame(title);
		frame.setSize(width,height);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				exitProcedure();
			}
		});
		frame.setResizable(false);
		frame.setVisible(true);
		frame.add(renderer);
		frame.addKeyListener(this);
		timer.start();
	}
	public void activateGodMode(){
		if (!GodMode)	GodMode = true;
	}
	private static void deleteSave(String savefolder){
		try{	removeRecursive(Paths.get("SaveFiles//"+savefolder+"//"));
		}catch (Exception e){}
	}
	public static void removeRecursive(Path path) throws IOException{
		Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
			@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException{
				Files.delete(file);
				return FileVisitResult.CONTINUE;}
			@Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException{
				Files.delete(file);
				return FileVisitResult.CONTINUE;}
			@Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException{
				if (exc == null){Files.delete(dir);return FileVisitResult.CONTINUE;}
				else{throw exc;}
			}
		});
	}
	public static String[] getFolderFileNames(String folder){
		ArrayList<String> filenames = new ArrayList<String>();
		try{
			Files.walk(Paths.get(folder)).forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					filenames.add(filePath.toString());
				}
			});
		}catch(Exception e){}
		String[] result = new String[filenames.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = "";
		
		for (int i = 0; i < filenames.size(); i++)
			for (int j = 0; j < filenames.get(i).length(); j++){
				if (filenames.get(i).substring(j,j+1).equals("\\")) result[i]+="//";
				else 	result[i]+=""+filenames.get(i).charAt(j);
			}
		return result;
	}
	public static void save(Object obj, String filename){
		new File(filename.substring(0,filename.lastIndexOf("//"))).mkdirs();
		try{
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(obj);
			out.flush();
			out.close();
		} catch(Exception e) {System.out.println(e);} 
	}
	public static Object load(String filename){
		new File(filename.substring(0,filename.lastIndexOf("//"))).mkdirs();
		Object obj = null;
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fis);
			obj = in.readObject();
			in.close();
		}catch(Exception e){return null;}
		return obj;
	}
	public void exitProcedure(){
		save(player,"SaveFiles//"+saveFolder+"//Entities//PlayerSave.dat");
		save(board,"SaveFiles//"+saveFolder+"//World//Board.dat");
		save(game,"SaveFiles//"+saveFolder+"//World//GameSave.dat");
		board.saveBoard();
		frame.dispose();
		System.exit(0);
	}
	public static String toB36(long b10){
		return Long.toString(b10,36);
	}
	public static Piece getGround(WorldLocation location){
		if (location.dimension.indexOf("room")!=-1)
			return new RoomFloor();
		switch(location.dimension){
			case "surface": return new Grass();
			case "underground": return new DirtFloor();
		}
		return null;
	}
	public static WorldLocation pointToLocation(Point pos){
		WorldLocation result = new WorldLocation(player.location);
		int x = pos.x, y = pos.y, dx = x/64-8, dy = y/64-8;
		result.chunkX+=dx;
		result.chunkY+=dy;
		result.legalize();
		return result;
	}
	public static Point locationToPoint(WorldLocation location){
		WorldLocation player = new WorldLocation(Main.player.location);
		return new Point(64+64*7+64*((int)(location.worldX-player.worldX)*8+location.chunkX-player.chunkX),
			64+64*7+64*((int)(location.worldY-player.worldY)*8+location.chunkY-player.chunkY));
	}
	public static void setWorldPiece(Piece piece, WorldLocation location){
		String middle = (location.dimension.indexOf("room")!=-1 ? "Rooms//" : ""),
		folder = "SaveFiles//"+saveFolder+"//Chunks//"+middle+Character.toUpperCase(location.dimension.charAt(0))
			 +""+location.dimension.substring(1)+"//";
		Chunk chunk = null;
		long lx, ly;
		lx = location.worldX-player.location.worldX;
		ly = location.worldY-player.location.worldY;
		int x=0,y=0;
		if (Math.abs(lx)<=1) x = (int)lx+1;
		if (Math.abs(ly)<=1) y = (int)ly+1;
		if (Math.abs(lx)>1||Math.abs(ly)>1||!player.location.dimension.equals(location.dimension))
			chunk = (Chunk) load(folder+toB36(location.worldX)+"_"+toB36(location.worldY)+".dat");
		else 	chunk = board.board[y][x];
		if (chunk==null)chunk = new Chunk(location.dimension,location);
		chunk.chunk[location.chunkY][location.chunkX] = piece;
		chunk.setChunkCoords();
		chunk.save();
		if (Math.abs(lx)>1||Math.abs(ly)>1||!player.location.dimension.equals(location.dimension));
		else board.board[y][x] = chunk;
	}
	public static Piece getWorldPiece(WorldLocation location){
		String middle = (location.dimension.indexOf("room")!=-1 ? "Rooms//" : ""),
		folder = "SaveFiles//"+saveFolder+"//Chunks//"+middle+Character.toUpperCase(location.dimension.charAt(0))
			 +""+location.dimension.substring(1)+"//";
		Chunk chunk = null;
		long lx, ly;
		lx = location.worldX-player.location.worldX;
		ly = location.worldY-player.location.worldY;
		int x=0,y=0;
		if (Math.abs(lx)<=1) x = (int)lx+1;
		if (Math.abs(ly)<=1) y = (int)ly+1;
		if (Math.abs(lx)>1||Math.abs(ly)>1||!player.location.dimension.equals(location.dimension))
			chunk = (Chunk) load(folder+toB36(location.worldX)+"_"+toB36(location.worldY)+".dat");
		else chunk = board.board[y][x];
		if (chunk==null)return null;
		return chunk.chunk[location.chunkY][location.chunkX];
	}
	private void update(){
		for (int i = 0; i < boxes.size(); i++)
			boxes.get(i).update();
	}
	private void render(){
		renderer.repaint();
	}
	public void keyPressed(KeyEvent e){
		player.processPress(e);}
	public void keyReleased(KeyEvent e){
		player.processRelease(e);}
	public void keyTyped(KeyEvent e){}   
	public void actionPerformed(ActionEvent e){
		game.tick();
		update();
		render();
	}
}
class MouseReader implements MouseListener, MouseMotionListener{
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e))
			Main.rightclick.processRightClick(e);
		else if (SwingUtilities.isLeftMouseButton(e))
			Main.rightclick.processLeftClick(e);
		if (Main.GodMode){
			if (SwingUtilities.isLeftMouseButton(e))
				Main.Godtools.processLeftClick(e);
			else if (SwingUtilities.isRightMouseButton(e))
				Main.Godtools.processRightClick(e);
		}
	}
	public void mouseMoved(MouseEvent e){
		if (Main.GodMode)
			Main.Godtools.processMove(e);
		Main.rightclick.processMove(e);
	}
	public void mouseDragged(MouseEvent e){
		if (Main.GodMode)
			Main.Godtools.processDrag(e);
	}
}
class RightClickMenu implements Displayable{
	private int x, y, height, width, dist, select;
	private Point mouse;
	private BufferedImage back;
	private boolean open;
	private String[] boardmenu, inventorymenu, minimapmenu, currentmenu;
	public RightClickMenu(){
		select = -1;
		mouse = new Point(0,0);
		open = false;
		makeBacking(1);
		dist = 100;
		currentmenu = new String[]{""};
		//if (Main.GodMode)
			boardmenu = new String[]{
				"Teleport"
			};
		inventorymenu = new String[]{
			"Use",
			"Drop"
		};
	}
	public void processRightClick(MouseEvent e){
		x = e.getX();
		y = e.getY();
		if (Main.boarddisp.inBounds(x,y))
			openMenu(getPiece(new Point(this.x,this.y)).getActions());
		else if (onInventory(x,y))
			openMenu(inventorymenu);
	}
	public void processLeftClick(MouseEvent e){
		int x = e.getX(),
		y = e.getY();
		if (onMenu(x,y)&&select!=-1&&open)
			getPiece(new Point(this.x,this.y)).doAction(currentmenu[select].toLowerCase());
		closeMenu();
	}
	public void processMove(MouseEvent e){
		mouse = new Point(e.getX(),e.getY());
		int dx = e.getX()-x,
		dy = e.getY()-y;
		
		if (dx>dist+width||dx<-dist||dy>dist+height||dy<-dist)
			closeMenu();
	}
	private void makeBacking(int height){
		back = new BufferedImage(128,24*height+16,BufferedImage.TYPE_INT_ARGB);
		Graphics g = back.getGraphics();
		g.drawImage(Main.graphics.getImg("rightclicktop"),0,0,null);
		for (int i = 0; i < 24*height; i+=24)
			g.drawImage(Main.graphics.getImg("rightclickmiddle"),0,i+8,null);
		g.drawImage(Main.graphics.getImg("rightclickbottom"),0,24*height+8,null);
		g.dispose();
		this.height = back.getHeight();
		this.width = back.getWidth();
	}
	private void openMenu(String[] menu){
		currentmenu = menu;
		if (currentmenu!=null && currentmenu[0].length()>0)
			for (int i = 0; i < currentmenu.length; i++)
				currentmenu[i] = Character.toUpperCase(currentmenu[i].charAt(0))+currentmenu[i].substring(1);
		if (Main.player.carrying!=null&&getPiece(new Point(x,y)).isEmpty){
			menu = new String[currentmenu.length+1];
			for (int i = 0; i < currentmenu.length; i++)
				menu[i] = currentmenu[i];
			if (menu[currentmenu.length-1].equals("Delete")){
				menu[currentmenu.length-1] = "Place";
				menu[currentmenu.length] = "Delete";
			}
			else 
				menu[currentmenu.length] = "Place";
			currentmenu = menu;
		}
		open = true;
		makeBacking(menu.length);
	}
	private void closeMenu(){
		select = -1;
		open = false;
	}
	private Piece getPiece(Point pos){
		return Main.getWorldPiece(Main.pointToLocation(pos));


		/*
		int dx, dy;
		dx = x/64-8;
		dy = y/64-8;
		WorldLocation result = new WorldLocation(Main.player.location);
		result.chunkX+=dx;
		result.chunkY+=dy;
		result.legalize();
		return Main.getWorldPiece(result);*/
	}
	private boolean onBoard(int x, int y){
		x/=64; y/=64;
		return x>=1 && x<=15 && y>=1 && y<=15;
	}
	private boolean onInventory(int x, int y){
		x-=1118; y-=24+Main.height-682;
		x/=64; y/=64;
		return x>=0&&y>=0&&x<=3&&y<=7;
	}
	private boolean onMinimap(int x, int y){
		return false;
	}
	private boolean onMenu(int x, int y){
		return x>this.x+6&&x<this.x+122&&y>this.y+8&&y<this.y+8+currentmenu.length*24;
		//return x>this.x&&x<this.x+width&&y-8>this.y&&y-8<this.y+currentmenu.length*24
		//&&(this.y-y)/24>=0&&(this.y-y)/24<currentmenu.length;
	}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		g.setFont(g.getFont().deriveFont(17f));
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		g.setColor(Color.black);
		g.setStroke(new BasicStroke(2));
		if (open){
			g.drawImage(back,null,x,y);
			for (int i = 0; i < currentmenu.length; i++)
				g.drawString(currentmenu[i],x+8,24*(i+1)+y+2);
			if (select!=-1)
				g.drawRect(x+6,24*select+y+8,116,24);
		}
	}
	public void update(){
		if (onMenu(mouse.x,mouse.y))//&&(mouse.y+8-y)/24<currentmenu.length)
			select = (mouse.y-8-y)/24;
		else select = -1;
	}
}
class GodTools /*extends MouseAdapter*/ implements Displayable{
	private boolean active;
	private String[][] tools;
	private String tool;
	private Point mouseref, selection;
	private DisplayBox toolbox;
	public GodTools(){
		active = true;
		mouseref = new Point(0,0);
		selection = new Point(-1,-1);
		tools = new String[][]{
			{"grass","dirt","darkdirt","roomfloor"},
			{"seedling","sapling","tree","stump"},
			{"woodblock","ladderbelow","portal","roomportal"},
			{"void","deleteicon","",""},
			{"","","",""},
			{"","","",""},
			{"","","",""},
			{"","","",""}
		};
		tool = "";
	}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		if (Main.GodMode){
			int x = mouseref.x,
			y = mouseref.y;
			if (onBoard(x,y)&&isSelection(selection))
				g.drawImage(Main.graphics.getImg(tools[selection.y][selection.x]),null,64*(int)(x/64),64*(int)(y/64));
			renderTools(g);
		}
	}
	private void renderTools(Graphics2D g){
		g.translate(1088,Main.height-680);
		g.drawImage(Main.graphics.getImg("inventoryback"),null,0,0);
		g.translate(30,24);
		for (int i = 0; i < tools.length; i++)
			for (int j = 0; j < tools[0].length; j++)
				if (tools[i][j].length()>0)
					g.drawImage(Main.graphics.getImg(tools[i][j]), null, 66*j,66*i);
		if (selection.x!=-1&&selection.y!=-1)
			g.drawImage(Main.graphics.getImg("toolselection"),null,selection.x*66-2,selection.y*66-2);
	}
	public void processRightClick(MouseEvent e){
		
	}
	public void processLeftClick(MouseEvent e){
		if (Main.GodMode){
			int x = e.getX(),
			y = e.getY();
			if (onInventory(x,y)){
				selection = toInventory(x,y);
				if (tools[selection.y][selection.x].length()==0)
					selection = new Point(-1,-1);
			}
			if (onBoard(x,y)&&isSelection(selection)){
				x/=64;y/=64;
				int dx = x-8, dy = y-8;
				setPiece(tools[selection.y][selection.x],dx,dy);
			}
		}
	}
	public void processMove(MouseEvent e){
		mouseref = new Point(e.getX(),e.getY());
	}
	public void processDrag(MouseEvent e){
		if (Main.GodMode){
			int x = e.getX(),
			y = e.getY();
			mouseref = new Point(x,y);
			if (onInventory(x,y)){
				selection = toInventory(x,y);
				if (tools[selection.y][selection.x].length()==0)
					selection = new Point(-1,-1);
			}
			if (onBoard(x,y)&&isSelection(selection)){
				x/=64;y/=64;
				int dx = x-8, dy = y-8;
				setPiece(tools[selection.y][selection.x],dx,dy);
			}
		}
	}
	private void setPiece(String type, int dx, int dy){
		WorldLocation location = new WorldLocation(Main.player.location.dimension,
			Main.player.location.worldX,Main.player.location.worldY,
			Main.player.location.chunkX,Main.player.location.chunkY);
		if (location.chunkX+dx<0){
			location.worldX--;
			dx+=8;
		}
		if (location.chunkX+dx>7){
			location.worldX++;
			dx-=8;
		}
		if (location.chunkY+dy<0){
			location.worldY--;
			dy+=8;
		}
		if (location.chunkY+dy>7){
			location.worldY++;
			dy-=8;
		}
		location.chunkX+=dx;
		location.chunkY+=dy;
		switch(type){
			case "seedling":
				if (Main.getWorldPiece(location).appearance.equals("grass")
					&&!Main.getWorldPiece(location).appearance.equals("roomportal"))
					Main.setWorldPiece(new Tree("seedling"),location);
				break;
			case "sapling":
				if (Main.getWorldPiece(location).appearance.equals("grass")
					&&!Main.getWorldPiece(location).appearance.equals("roomportal"))
					Main.setWorldPiece(new Tree("sapling"),location);
				break;
			case "tree":
				if (Main.getWorldPiece(location).appearance.equals("grass")
					&&!Main.getWorldPiece(location).appearance.equals("roomportal"))
					Main.setWorldPiece(new Tree("tree"),location);
				break;
			case "stump":
				if (Main.getWorldPiece(location).appearance.equals("grass")
					&&!Main.getWorldPiece(location).appearance.equals("roomportal"))
					Main.setWorldPiece(new Tree("stump"),location);
				break;
			case "woodblock":
				if (!Main.getWorldPiece(location).appearance.equals("roomportal"))
					Main.setWorldPiece(new WoodBlock(),location);
				break;
			case "ladderbelow":
				if (location.dimension.indexOf("room")==-1)
					Main.setWorldPiece(new Ladder(),location);
				break;
			case "portal":
				if (!Main.getWorldPiece(location).appearance.equals("roomportal"))
					Main.setWorldPiece(new Portal(location),location);
				break;
			case "roomportal":
				if (!Main.getWorldPiece(location).appearance.equals("roomportal"))
					Main.setWorldPiece(new RoomEntryPortal(),location);
				break;
			case "dirt":
				if (!Main.getWorldPiece(location).appearance.equals("roomportal")&&
					location.dimension.equals("underground"))
					Main.setWorldPiece(new DirtFloor(),location);
				break;
			case "darkdirt":
				if (!Main.getWorldPiece(location).appearance.equals("roomportal")&&
					location.dimension.equals("underground"))
					Main.setWorldPiece(new Dirt(),location);
				break;
			case "grass":
				if (!Main.getWorldPiece(location).appearance.equals("roomportal")&&
					location.dimension.equals("surface"))
					Main.setWorldPiece(new Grass(), location);
				break;
			case "roomfloor":
				if (!Main.getWorldPiece(location).appearance.equals("roomportal")&&
					location.dimension.indexOf("room")!=-1)
					Main.setWorldPiece(new RoomFloor(),location);
				break;
			case "void":
				if (!Main.getWorldPiece(location).appearance.equals("roomportal")&&
					location.dimension.indexOf("room")!=-1)
					Main.setWorldPiece(new Void(),location);
				break;
			case "deleteicon":
				if (!Main.getWorldPiece(location).appearance.equals("roomportal"))
					Main.setWorldPiece(Main.getGround(location),location);
				break;
		}
	}
	private boolean isSelection(Point selection){
		int x = selection.x, y = selection.y;
		return x>=0&&y>=0&&x<=3&&y<=7;
	}
	private Point toInventory(int x, int y){
		x-=1118; y-=24+Main.height-682;
		x/=64; y/=64;
		return new Point(x,y);
	}
	private boolean onBoard(int x, int y){
		x/=64; y/=64;
		return x>=1 && x<=15 && y>=1 && y<=15;
	}
	private boolean onInventory(int x, int y){
		x-=1118; y-=24+Main.height-682;
		x/=64; y/=64;
		return x>=0&&y>=0&&x<=3&&y<=7;
	}
	private boolean onMinimap(int x, int y){
		return false;
	}
	public void update(){}
	public void Enable(){active = true;}
	public void Disable(){active = false;}
	public boolean isActive(){return active;}
}
class Renderer extends JPanel {
	public void paintComponent(Graphics g0){
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		for (int i = 0; i < Main.boxes.size(); i++)
			Main.boxes.get(i).render(g);
	}
}
class Player implements Serializable, Displayable{
	public int dx, dy;
	public String appearance, action;
	public Piece carrying, actingon;
	private Counter walk, cut, busy;
	public WorldLocation location;
	public Inventory inventory;
	private boolean up, down, left, right, shift, 
		trycut, tryplant, trydig, walking, running, 
		superspeed, ctrl, alt;
	public Player(){
		location = new WorldLocation("surface",0,0,4,4);
		dx = 0;
		dy = 0;
		action = "";
		//busy = false;
		busy = new Counter(1);
		shift = false;
		walking = false;
		running = false;
		superspeed = false;
		appearance = "south";
		up = false;
		down = false;
		left = false;
		right = false;
		trycut = false;
		tryplant = false;
		trydig = false;
		walk = new Counter(512);
		//cut = new Counter(20);
		//busy = new Counter(1);
		//cut.loop = true;
		walk.loop = false;
		walk.start();
		//cut.start();
		//cut.jump(20);
		inventory = new Inventory(384,768);
	}
	public void spawnIn(){
		int x,y;
		do{
			x = Main.rand.nextInt(8);
			y = Main.rand.nextInt(8);
		}while(!Main.board.board[1][1].chunk[y][x].isEmpty&&!Main.GodMode);
		location.chunkX = x;
		location.chunkY = y;
	}
	public Piece standing(){
		return Main.getWorldPiece(location);
	}
	public Piece facing(){
		WorldLocation temp = new WorldLocation(location.dimension,location.worldX, 
					location.worldY,location.chunkX,location.chunkY);
		Piece result;
		switch(appearance){
			case "north":
				temp = new WorldLocation(location.dimension,location.worldX, 
					location.worldY,location.chunkX,location.chunkY-1);
				break;
			case "south":
				temp = new WorldLocation(location.dimension,location.worldX, 
					location.worldY,location.chunkX,location.chunkY+1);
				break;
			case "west":
				temp = new WorldLocation(location.dimension,location.worldX, 
					location.worldY,location.chunkX-1,location.chunkY);
				break;
			case "east":
				temp = new WorldLocation(location.dimension,location.worldX, 
					location.worldY,location.chunkX+1,location.chunkY);
				break;
		}
		temp.legalize();
		result = Main.getWorldPiece(temp);
		if (result!=null)
			return result;
		return Main.getGround(location);
	}
	public void teleport(WorldLocation location){
		if (this.location.isEqual(location));
		else if (this.location.sameChunk(location))
			this.location = new WorldLocation(location);
		else if (this.location.isAdjacent(location)){
			Main.board.loadAdjacentBoard(this.location.getAdjacent(location).x,this.location.getAdjacent(location).y);
			this.location = new WorldLocation(location);}
		else {
			Main.board.saveBoard();
			this.location = new WorldLocation(location);
			Main.board.switchToChunk(location);
		}
	}
	public void setFacing(Piece piece){
		Main.setWorldPiece(piece, facing().location);
	}
	public void switchUnderground(){
		WorldLocation flipped = new WorldLocation(location.dimension,location.worldX,
						location.worldY,location.chunkX,location.chunkY);
		flipped.change(new String[]{"flipdimension"}, new String[]{""});
		Piece other = Main.getWorldPiece(flipped);
		if (other==null||other.isEmpty||other.hasAction("climb")||other.appearance.equals("darkdirt")||Main.GodMode){
			Main.setWorldPiece(new Ladder(),location);
			location.change(new String[]{"flipdimension"}, new String[]{""});
			Main.board.switchToChunk(location);
			Main.setWorldPiece(new Ladder(),location);
		}
	}
	public void pickup(Piece piece){
		if (carrying==null){
			carrying = piece;
			Main.setWorldPiece(Main.getGround(piece.location),piece.location);
		}
	}
	public void putdown(WorldLocation location){
		if (carrying!=null){
			Main.setWorldPiece(carrying,location);
			carrying = null;
		}
	}
	public void tryPickup(){
		if (facing().canLift){
			carrying = facing();
			facing().remove();
		}
	}
	public void tryPutdown(){
		if (carrying!=null && (facing().isEmpty||Main.GodMode) && !walking){
			place(carrying);
			carrying = null;
		}
	}
	public void place(Piece piece){
		if (!walking)
			setFacing(piece);
	}/*
	public void doActionOn(String action, Piece piece){
		actingon = piece;
		busy = piece.getCounter(action);
		this.action = action;
	}*/
	public void update(){
		walk.update();
		/*
		if (busy!=null)
			busy.update();
		if (busy.ready()) {actingon.doAction(action); actingon = null; 
		action = ""; busy.consume();}
		
		if (action.length()>0)
			doActionOn(action,facing());
		if (busy&&facing().getActionReady()){
			facing().consumeAction();
			busy = false;
			action = "";
		}*/
		superspeed = Main.GodMode&&ctrl;
		if (!walking&&(up||down||left||right)){
			if (right&&!left)	right();
			else if (left&&!right)	left();
			else if (up&&!down)	up();
			else if (down&&!up)	down();
		}
		if (running&&carrying==null) walk.setIncrement(64);
		else walk.setIncrement(16);
		if (superspeed){
			location.chunkX+=2*dx;
			location.chunkY+=2*dy;
			location.legalize();
			if (location.isAdjacent(Main.board.center))
				Main.board.loadAdjacentBoard(dx,dy);
			dx = 0; dy = 0;
			Main.getWorldPiece(location).walkOn();
			walking = false;
		}
		else if (walk.ready()&&walking&&(dy!=0||dx!=0)){
			location.chunkX+=dx;
			location.chunkY+=dy;
			location.legalize();
			if (location.isAdjacent(Main.board.center))
				Main.board.loadAdjacentBoard(dx,dy);
			dx = 0; dy = 0;
			Main.getWorldPiece(location).walkOn();
			walk.consume();
			walking = false;
		}
		if (trycut)	tryAction("cut");
		if (tryplant)	tryAction("plant");
		if (trydig)	tryAction("dig");
	}
	public void tryAction(String action){
		if (facing().hasAction(action))
			facing().startAction(action);
	}
	public double partial(){
		return walk.progress();
	}
	private void up(){
		appearance = "north";
		if (!walking&&(facing().canWalkthrough||Main.GodMode)&&!shift){		
			walk.start();
			dy=-1;
			walking = true;
		}
	}
	private void down(){
		appearance = "south";
		if (!walking&&(facing().canWalkthrough||Main.GodMode)&&!shift){		
			walk.start();
			dy=1;
			walking = true;
		}
	}
	private void left(){
		appearance = "west";
		if (!walking&&(facing().canWalkthrough||Main.GodMode)&&!shift){
			walk.start();
			dx=-1;
			walking = true;	
		}
	}
	private void right(){
		appearance = "east";
		if (!walking&&(facing().canWalkthrough||Main.GodMode)&&!shift){		
			walk.start();
			dx=1;
			walking = true;
		}
	}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		g.setFont(g.getFont().deriveFont(32f));
		g.drawString(location.write(),66,96);
		//g.drawString("X:"+location.worldX+"  Y:"+location.worldY+"  x:"+location.chunkX+"  y:"+location.chunkY,66, 96);
		g.translate(64+64*7,64+64*7);
		g.drawImage(Main.graphics.getImg(appearance),null,0,0);
		if (carrying!=null)
			g.drawImage(Main.graphics.getImg(carrying.appearance),null,0,-44);
	}
	public void processPress(KeyEvent e){
		facing().stopAction();
		switch(e.getKeyCode()){
			case KeyEvent.VK_W:
				up = true;
				break;
			case KeyEvent.VK_A:
				left = true;
				break;
			case KeyEvent.VK_S:
				down = true;
				break;
			case KeyEvent.VK_D:
				right = true;
				break;
			case KeyEvent.VK_O:
				trydig = true;
				break;
			case KeyEvent.VK_K:
				trycut = true;
				break;
			case KeyEvent.VK_M:
				tryplant = true;
				break;
			case KeyEvent.VK_SHIFT:
				shift = true;
				break;
			case KeyEvent.VK_SPACE:
				running = true;
				break;
			case KeyEvent.VK_L:
				if (facing().isEmpty||Main.GodMode)place(new WoodBlock());
				break;
			case KeyEvent.VK_P:
				if (standing().isEmpty||Main.GodMode)Main.setWorldPiece(new Portal(standing().location),standing().location);
				break;
			case KeyEvent.VK_U:
				switchUnderground();
				break;
			case KeyEvent.VK_T:
				if (standing().hasAction("teleport"))standing().doAction("teleport");
				break;
			case KeyEvent.VK_Y:
				if (standing().isEmpty||Main.GodMode)Main.setWorldPiece(new RoomEntryPortal(),standing().location);
				break;
			case KeyEvent.VK_G:
				if (ctrl&&alt){ Main.GodMode = !Main.GodMode;}
				break;
			case KeyEvent.VK_CONTROL:
				ctrl = true; 
				break;
			case KeyEvent.VK_ALT:
				alt= true;
				break;
			case KeyEvent.VK_COMMA:
				if (carrying==null)
					tryPickup();
				else  tryPutdown();
				break;
			case KeyEvent.VK_DELETE:
				if (!facing().hasAction("permanent"))
					facing().remove();
				break;
		}
	}
	public void processRelease(KeyEvent e){
		switch(e.getKeyCode()){
			case KeyEvent.VK_W:
				up = false;
				break;
			case KeyEvent.VK_A:
				left = false;
				break;
			case KeyEvent.VK_S:
				down = false;
				break;
			case KeyEvent.VK_D:
				right = false;
				break;
			case KeyEvent.VK_O:
				trydig = false;
				break;
			case KeyEvent.VK_K:
				trycut = false;
				break;
			case KeyEvent.VK_M:
				tryplant = false;
				break;
			case KeyEvent.VK_SHIFT:
				shift = false;
				break;
			case KeyEvent.VK_SPACE:
				running = false;
				break;
			case KeyEvent.VK_CONTROL:
				ctrl = false; 
				break;
			case KeyEvent.VK_ALT:
				alt= false;
				break;
		}
	}
}
class Mapping implements Serializable{
	private String[][] apprs;
	public WorldLocation location;
	public transient BufferedImage appearance;
	public Mapping(Chunk chunk){
		apprs = new String[8][8];
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				apprs[i][j] = chunk.chunk[i][j].appearance;
		updateAppearance();
		this.location = chunk.location;
		save();
	}
	private void readObject(ObjectInputStream in)    throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		updateAppearance();
	}
	public void save(){
		String middle = (location.dimension.indexOf("room")!=-1 ? "Rooms//" : ""),
		folder = "SaveFiles//"+Main.saveFolder+"//World//Minimap//"+middle+Character.toUpperCase(location.dimension.charAt(0)) 
			+""+location.dimension.substring(1)+"//";
		Main.save(this,folder+Main.toB36(location.worldX)
			+"_"+Main.toB36(location.worldY)+".dat");
	}
	public void updateAppearance(){
		appearance = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
		Graphics g = appearance.getGraphics();
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++){
				//g.drawImage(Main.graphics.getMapping(Main.getGround(location).appearance),4*j,4*i,null);
				g.drawImage(Main.graphics.getMapping(apprs[i][j]),4*j,4*i,null);
			}
		g.dispose();	
	}
}
class MiniMap implements Displayable, Serializable{
	private int refresh;
	private WorldLocation location;
	private Mapping[][] mappings;
	public MiniMap(){
		mappings = new Mapping[12][12];
		location = new WorldLocation(Main.player.location);
		initMappings();
		refresh = 0;
	}
	public void initMappings(){
		WorldLocation location = new WorldLocation(Main.player.location);
		String middle = (location.dimension.indexOf("room")!=-1 ? "Rooms//" : ""),
		folder = "SaveFiles//"+Main.saveFolder+"//World//Minimap//"+middle+Character.toUpperCase(location.dimension.charAt(0)) 
			+""+location.dimension.substring(1)+"//";
		mappings = new Mapping[12][12];
		for (int i = -6; i < 6; i++)
			for (int j = -6; j < 6; j++){
				Mapping map = (Mapping)Main.load(folder+""+Main.toB36(location.worldX+j)
					+"_"+Main.toB36(location.worldY+i)+".dat");
				if (map!=null)	mappings[i+6][j+6] = map;
			}
		this.location = new WorldLocation(location);
	}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		for (int i = 0; i < 12; i++)
			for (int j = 0; j < 12; j++)
				if (mappings[i][j]!=null)
					g.drawImage(mappings[i][j].appearance,null,32*(j-6)+158-4*this.location.chunkX,
						32*(i-6)+158-4*this.location.chunkY);
				else g.drawImage(Main.graphics.getImg("void"),null,32*(j-6)+158-4*this.location.chunkX,
						32*(i-6)+158-4*this.location.chunkY);
		
		g.drawImage(Main.graphics.getImg("playerdot"),null, 158,158);
	}
	public void addMapping(Mapping map){
		int dx = (int)(map.location.worldX-this.location.worldX),
		dy = (int)(map.location.worldY-this.location.worldY);
		if (dx>=-6&&dx<=6&&dy>=-6&&dy<=6)
			mappings[6+dy][6+dx] = map;
	}
	private void shift(WorldLocation location){
		String middle = (location.dimension.indexOf("room")!=-1 ? "Rooms//" : ""),
		folder = "SaveFiles//"+Main.saveFolder+"//World//Minimap//"+middle+Character.toUpperCase(location.dimension.charAt(0)) 
			+""+location.dimension.substring(1)+"//";
		WorldLocation p = new WorldLocation(this.location);
		int dx = (int)(location.worldX);
		int dy = (int)(location.worldY);

		for (int i = 0; i < 12; i++){
			if (dx==-1&&mappings[i][0]!=null) mappings[i][0].save();
			else if (dy==-1&&mappings[0][i]!=null) mappings[0][i].save();
			else if (dx==1&&mappings[i][11]!=null) mappings[i][11].save();
			else if (dy==1&&mappings[11][i]!=null) mappings[11][i].save();
		}

		for (int i = 1; i < 12; i++)
			for (int j = 0; j < 12; j++){
				if (dx==-1)	mappings[j][i-1] = mappings[j][i];
				else if (dy==-1)	mappings[i-1][j] = mappings[i][j];}

		for (int i = 10; i > 0; i--)
			for (int j = 0; j < 12; j++){
				if (dx==1)	mappings[j][i+1] = mappings[j][i];
				else if (dy==1)	mappings[i+1][j] = mappings[i][j];}

		for (int i = 0; i < 12; i++){
			if (dx==-1)	mappings[i][11] = (Mapping)Main.load(folder+""+Main.toB36(p.worldX+6)+"_"+Main.toB36(p.worldY-6+i)+".dat");
			else if (dx==1)	mappings[i][0] = (Mapping)Main.load(folder+""+Main.toB36(p.worldX-6)+"_"+Main.toB36(p.worldY-6+i)+".dat");
			else if (dy==-1)mappings[11][i] = (Mapping)Main.load(folder+""+Main.toB36(p.worldX-6+i)+"_"+Main.toB36(p.worldY+6)+".dat");
			else if (dy==1)mappings[0][i] = (Mapping)Main.load(folder+""+Main.toB36(p.worldX-6+i)+"_"+Main.toB36(p.worldY-6)+".dat");
		}
		this.location = new WorldLocation(Main.player.location);
	}
	public void update(){
		if (!location.dimension.equals(Main.player.location.dimension)){
			initMappings();}
		else {
			WorldLocation p = new WorldLocation(Main.player.location);
			WorldLocation differ = new WorldLocation(p.dimension,location.worldX-p.worldX,location.worldY-p.worldY,0,0);	
			shift(differ);
		}
	}
}
class Inventory implements Serializable, Displayable {
	private Item[][] items;
	private int width, height;
	public Inventory(int width, int height){
		items = new Item[7][4];
		for (int i = 0; i < 7; i++)
			for (int j = 0; j < 4; j++)
				items[i][j] = new Item("empty");
		this.width = width;
		this.height = height;
	}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		g.setFont(g.getFont().deriveFont(32f));
		g.drawImage(Main.graphics.getImg("inventoryback"),null,0,0);
		g.translate(30,24);
		g.setColor(Color.white);
		for (int i = 0; i < items.length; i++)
			for (int j = 0; j < items[0].length; j++)
				if (items[i][j]!=null)
					if (!items[i][j].appearance.equals("empty")){
						g.drawImage(Main.graphics.getImg(items[i][j].appearance),
							null, 66*j,66*i);
						if (items[i][j].stackAmount>1) g.drawString(""+items[i][j].stackAmount,66*j,66*i+64);
					}
	}
	public boolean addItem(Item item){
		boolean success = false;
		for (int i = 0; i < 7&&!success; i++)
			for (int j = 0; j < 4&&!success; j++){
				if (item.type.equals(items[i][j].type))
					success = items[i][j].addToStack();
				if (!success&&items[i][j].type.equals("empty")){
					 items[i][j] = item;success = true;}
			}
		return success;
	}
	public Item getItem(int x, int y){
		if (inBounds(x,y))
			return items[y][x];
		return new Item("empty");
	}
	public void update(){

	}
	private boolean inBounds(int x, int y){return x>=0&&x<4&&y>=0&&y<7;}
}
class WorldLocation implements Serializable{
	public String dimension;
	public long worldX, worldY;
	public int chunkX, chunkY;
	public WorldLocation(String dim, long wx, long wy, int cx, int cy){
		dimension = dim;
		worldX = wx;
		worldY = wy;
		chunkX = cx;
		chunkY = cy;
	}
	public WorldLocation (WorldLocation location){
		dimension = location.dimension;
		worldX = location.worldX;
		worldY = location.worldY;
		chunkX = location.chunkX;
		chunkY = location.chunkY;
	}
	public String write(){
		return "("+dimension + ", "+worldX+", "+worldY+", "+chunkX+", "+chunkY+")";
	}
	public boolean illegal(){
		return (chunkX>7||chunkX<0||chunkY>7||chunkY<0);
	}
	public void legalize(){
			if (chunkX>7) {
				chunkX-=8;
				worldX++;
			}
			if (chunkX<0){
				chunkX+=8;
				worldX--;
			}
			if (chunkY>7) {
				chunkY-=8;
				worldY++;
			}
			if (chunkY<0) {
				chunkY+=8;
				worldY--;
			}
	}
	public Point getAdjacent(WorldLocation location){
		if (isAdjacent(location))
			return new Point((int)(location.worldX-this.worldX),(int)(location.worldY-this.worldY));
		else return new Point(0,0);
	}
	public boolean isAdjacent(WorldLocation location){
		long x1,x2,y1,y2;
		x1 = this.worldX;
		x2 = location.worldX;
		y1 = this.worldY;
		y2 = location.worldY;
		return (x1-x2==0&&(y1-y2==1||y1-y2==-1))
			||(y1-y2==0&&(x1-x2==1||x1-x2==-1));
	}
	public boolean sameChunk(WorldLocation location){
		return location.worldX==this.worldX&&location.worldY==this.worldY&&this.dimension.equals(location.dimension);
	}
	public boolean isEqual(WorldLocation location){
		return this.dimension.equals(location.dimension)&&this.worldX==location.worldX&&this.worldY==location.worldY
			&&this.chunkX==location.chunkX&&this.chunkY==location.chunkY;
	}
	public void set(String dim, long wx, long wy, int cx, int cy){
		dimension = dim;
		worldX = wx;
		worldY = wy;
		chunkX = cx;
		chunkY = cy;
		if (illegal())legalize();
	}
	public void change(String[] actions, String[] amounts){
		for (int i = 0; i < actions.length; i++)
			switch(actions[i]){
				case "setdimension":
					this.dimension = amounts[i];
					break;
				case "flipdimension":
					if (this.dimension.equals("surface")) this.dimension = "underground";
					else if (this.dimension.equals("underground")) this.dimension = "surface";
					break;
				case "setworldx":
					this.worldX = Long.parseLong(amounts[i]);
					break;
				case "setworldy":
					this.worldY = Long.parseLong(amounts[i]);
					break;
				case "moveworldx":
					this.worldX += Long.parseLong(amounts[i]);
					break;
				case "moveworldy":
					this.worldY += Long.parseLong(amounts[i]);
					break;
				case "setchunkx":
					this.chunkX = Integer.parseInt(amounts[i]);
					break;
				case "setchunky":
					this.chunkY = Integer.parseInt(amounts[i]);
					break;
				case "movechunkx":
					this.chunkX += Integer.parseInt(amounts[i]);
					break;
				case "movechunky":
					this.chunkY += Integer.parseInt(amounts[i]);
					break;
			}
	}
}
class Board implements Serializable, Displayable{
	public static Chunk[][] board;
	public WorldLocation center;
	public Board(WorldLocation center){
		this.center = center;
		loadBoard(center);
	}
	public boolean needLoad(WorldLocation location){
		return center.worldX!=location.worldX||center.worldY!=location.worldY||!center.dimension.equals(location.dimension);
	}
	public void loadAdjacentBoard(int dx, int dy){
		String middle = (center.dimension.indexOf("room")!=-1 ? "Rooms//" : ""),
		folder = "SaveFiles//"+Main.saveFolder+"//Chunks//"+middle+Character.toUpperCase(center.dimension.charAt(0)) 
			+""+center.dimension.substring(1)+"//";
		String joined = ""+dx+""+dy;
		Chunk[][] moves = new Chunk[3][2];
		Chunk[] loads = new Chunk[3];
		switch(joined){
			case "-10":
				for (int i = 0; i < 3; i++)
					board[i][2].save();
				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 2; j++)
						moves[i][j] = board[i][j];
				for (int i = 0; i < 3; i++){
					loads[i] = (Chunk)Main.load(folder+
						Main.toB36(center.worldX-2)+"_"+Main.toB36(center.worldY-1+i)+".dat");
					if (loads[i]==null) loads[i] = new Chunk(center.dimension, new WorldLocation(
						center.dimension,center.worldX-2,center.worldY-1+i,center.chunkX,center.chunkY));
				}
				for (int i = 0; i < 3; i++){
					for (int j = 0; j < 2; j++)
						board[i][j+1] = moves[i][j];
					board[i][0] = loads[i];
				}
				center.worldX+=dx;
				break;
			case "10":
				for (int i = 0; i < 3; i++)
					board[i][0].save();
				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 2; j++)
						moves[i][j] = board[i][j+1];
				for (int i = 0; i < 3; i++){
					loads[i] = (Chunk)Main.load(folder+
						Main.toB36(center.worldX+2)+"_"+Main.toB36(center.worldY-1+i)+".dat");
					if (loads[i]==null) loads[i] = new Chunk(center.dimension, new WorldLocation(
						center.dimension,center.worldX+2,center.worldY-1+i,center.chunkX,center.chunkY));
				}
				for (int i = 0; i < 3; i++){
					for (int j = 0; j < 2; j++)
						board[i][j] = moves[i][j];
					board[i][2] = loads[i];
				}
				center.worldX+=dx;
				break;
			case "0-1":
				for (int i = 0; i < 3; i++)
					board[2][i].save();
				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 2; j++)
						moves[i][j] = board[j][i];
				for (int i = 0; i < 3; i++){
					loads[i] = (Chunk)Main.load(folder+
						Main.toB36(center.worldX-1+i)+"_"+Main.toB36(center.worldY-2)+".dat");
					if (loads[i]==null) loads[i] = new Chunk(center.dimension, new WorldLocation(
						center.dimension,center.worldX-1+i,center.worldY-2,center.chunkX,center.chunkY));
				}
				for (int i = 0; i < 3; i++){
					for (int j = 0; j < 2; j++)
						board[j+1][i] = moves[i][j];
					board[0][i] = loads[i];
				}
				center.worldY+=dy;
				break;
			case "01":
				for (int i = 0; i < 3; i++)
					board[0][i].save();
				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 2; j++)
						moves[i][j] = board[j+1][i];
				for (int i = 0; i < 3; i++){
					loads[i] = (Chunk)Main.load(folder+
						Main.toB36(center.worldX-1+i)+"_"+Main.toB36(center.worldY+2)+".dat");
					if (loads[i]==null) loads[i] = new Chunk(center.dimension, new WorldLocation(
						center.dimension,center.worldX-1+i,center.worldY+2,center.chunkX,center.chunkY));
				}
				for (int i = 0; i < 3; i++){
					for (int j = 0; j < 2; j++)
						board[j][i] = moves[i][j];
					board[2][i] = loads[i];
				}
				center.worldY+=dy;
				break;
		}
	}
	public void loadBoard(WorldLocation location){
		board = new Chunk[3][3];
		String middle = (location.dimension.indexOf("room")!=-1 ? "Rooms//" : ""),
		folder = "SaveFiles//"+Main.saveFolder+"//Chunks//"+middle+Character.toUpperCase(location.dimension.charAt(0))
			 +""+location.dimension.substring(1)+"//";
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++){
				board[i][j] = (Chunk)Main.load(folder+Main.toB36(location.worldX-1+j)
					+"_"+Main.toB36(location.worldY-1+i)+".dat");
				if (board[i][j]==null)	board[i][j]=new Chunk(location.dimension, new WorldLocation(
						location.dimension,location.worldX-1+j,location.worldY-1+i,location.chunkX,location.chunkY));
				board[i][j].save();
			}
		saveBoard();
	}
	public void saveBoard(){
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				board[i][j].save();
	}
	public void switchToChunk(WorldLocation location){
		saveBoard();
		center = new WorldLocation(location);
		loadBoard(center);
		Main.minimap.initMappings();
	}
	public void update(){
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				board[i][j].update();
	}
	public boolean onBoard(int i, int j){
		return i>=-1&&i<=16&&j>=-1&&j<=17;
	}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		String floor = Main.getGround(Main.player.location).appearance;
		WorldLocation playerloc = Main.player.location;
		double partial = 64*Main.player.partial();
		int pr =  64+64*7, xoff = (int)(partial*Main.player.dx), yoff = (int)(partial*Main.player.dy);
		for (int i = -8; i <= 8; i++)
			for (int j = -8; j <= 8; j++){
				WorldLocation temp = new WorldLocation(playerloc.dimension,playerloc.worldX,
					playerloc.worldY,playerloc.chunkX+j,playerloc.chunkY+i);
				temp.legalize();
				Main.getWorldPiece(temp).render(g,pr+64*j-xoff,pr+64*i-yoff);
				//g.drawImage(Main.graphics.getImg(floor),null,pr+64*j-xoff,pr+64*i-yoff);
				//g.drawImage(Main.graphics.getImg(Main.getWorldPiece(temp).appearance),null,pr+64*j-xoff,pr+64*i-yoff);
				//Main.getWorldPiece(temp).renderProgress(g,pr+64*j-xoff,pr+64*i-yoff);
			}
	}
}
class Borders implements Displayable {
	private BufferedImage border;
	public Borders(){border = Main.graphics.getImg("boundary");}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		for (int i = 0; i < 23; i++){
			g.drawImage(border,null,64*i,0);
			g.drawImage(border,null,64*i,64*16);
		}
		for (int i = 0; i < 17; i++){;
			g.drawImage(border,null,0,64*i);
			g.drawImage(border,null,64*16,64*i);
			g.drawImage(border,null,64*22,64*i);
		}
		for (int i = 0; i < 6; i++)
			g.drawImage(border,null,64*(16+i),64*6);
	}
	public void update(){}
}
interface Displayable {
	public void render(Graphics2D g);
	public void update();
}
class DisplayBox implements Serializable{
	private Displayable display;
	private int rx, ry, height, width;
	private Rectangle2D clip;
	public DisplayBox(int rx, int ry, Displayable display, int width, int height){
		this.display = display;
		this.rx = rx;
		this.ry = ry;
		this.height = height;
		this.width = width;
		clip = new Rectangle2D.Float(0,0,width,height);
	}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		g.translate(rx,ry);
		g.clip(clip);
		display.render(g);
	}
	public void update(){	display.update();}
	public void moveTo(int x, int y){	rx = x; ry = y;}
	public void moveRelative(int dx, int dy){rx+=dx;ry+=dy;}
	public boolean inBounds(int x, int y){return x>rx&&x<rx+width&&y>ry&&y<ry+height;}
	public Point toDisplay(Point pos){return new Point(pos.x-rx,pos.y-ry);}
	public Point getOrigin(){return new Point(rx,ry);}
	public Point getBounds(){return new Point(width,height);}
}
class GraphicsHandler {
	private BufferedImage[] images, imagemappings, items;
	private String[] imgrefs, mappingrefs, itemrefs, imgnames, mappingnames, itemnames;
	private String path, type;
	public GraphicsHandler(){
		path = "Assets//";
		type = ".png";
		imgrefs = new String[]{
			"Void",
			"Apple",
			"Boulder",
			"BoulderCracked",
			"Boundary",
			"DeadSeedling",
			"Fire",
			"Grass",
			"Hole",
			"PlayerE",
			"PlayerN",
			"PlayerS",
			"PlayerW",
			"Portal",
			"Rock",
			"Sapling",
			"Seedling",
			"Stump",
			"Tile",
			"Tree",
			"TreeApple",
			"Water",
			"InactivePortal",
			"WoodBlock",
			"PartialHole",
			"Dirt",
			"DarkDirt",
			"RoomPortal",
			"LadderAboveGround",
			"LadderBelowGround",
			"RoomFloor",
			"InventoryBackground",
			"PlayerDot",
			"ToolSelection",
			"DeleteIcon",
			"Logs",
			"AppleSeeds",
			"RightClickTop",
			"RightClickBottom",
			"RightClickMiddle",
			"RightClickMiddleBig"
		};
		imgnames = change(imgrefs,
		new String[]{
			"playere",
			"playern",
			"players",
			"playerw",
			"ladderaboveground",
			"ladderbelowground",
			"inventorybackground"},
		new String[]{
			"east",
			"north",
			"south",
			"west",
			"ladderabove",
			"ladderbelow",
			"inventoryback"},
		true);
		images = new BufferedImage[imgrefs.length];
		for (int i = 0; i < imgrefs.length; i++)
			try{images[i] = ImageIO.read(getClass().getResource(path+imgrefs[i]+type));
			} catch(IOException e){e.printStackTrace();}
		mappingrefs = new String[]{
			"Void",
			"Grass",
			"Portal",
			"Sapling",
			"Tree",
			"InactivePortal",
			"WoodBlock",
			"Dirt",
			"DarkDirt",
			"RoomPortal",
			"LadderAboveGround",
			"LadderBelowGround",
			"RoomFloor"
		};
		mappingnames = change(mappingrefs,new String[]{""},new String[]{""},true);
		imagemappings = new BufferedImage[mappingrefs.length];
		for (int i = 0; i < mappingrefs.length; i++)
			try{imagemappings[i] = ImageIO.read(getClass().getResource(path+mappingrefs[i]+"Mapping"+type));
			} catch(IOException e){e.printStackTrace();}
		itemrefs = new String[]{
			"Empty",
			"Logs",
			"Apple",
			"AppleSeeds",
			"Dirt"
		};
		itemnames = change(itemrefs,new String[1],new String[1],true);
		items = new BufferedImage[itemnames.length];
		for (int i = 0; i < itemrefs.length; i++)
			try{items[i] = ImageIO.read(getClass().getResource(path+itemrefs[i]+"Item"+type));
			} catch(IOException e){e.printStackTrace();}
	}
	private String[] change(String[] full, String[] find, String[] change, boolean lowercase){
		String[] result = new String[full.length];
		for (int i = 0; i < result.length; i++) result[i] = full[i];
		for (int i = 0; i < result.length; i++){
			if (lowercase)	result[i] = result[i].toLowerCase();
			for (int j = 0; j < find.length; j++)
				if (result[i].equals(find[j]))	result[i] = change[j];
		}
		return result;
	}
	public BufferedImage getImg(String name){
		for (int i = 0; i < images.length; i++)
			if (name.equals(imgnames[i]))
				return images[i];
		return Main.graphics.getImg(Main.getGround(Main.player.location).appearance);
	}
	public BufferedImage getMapping(String name){
		for (int i = 0; i < imagemappings.length; i++)
			if (name.equals(mappingrefs[i].toLowerCase()))
				return imagemappings[i];
		return Main.graphics.getMapping(Main.getGround(Main.player.location).appearance);
	}
	public BufferedImage getItem(String name){
		for (int i = 0; i < itemnames.length; i++)
			if (name.equals(itemnames[i]))
				return items[i];
		return items[0];
	}
}
class DroppedItems implements Displayable {
	private ArrayList<Item> items, displayItems;
	public DroppedItems(){
		items = new ArrayList<Item>();
		displayItems = new ArrayList<Item>();
	}
	public void addItem(Item item){items.add(item);if(item.onBoard())displayItems.add(item);}
	public void update(){
		for (int i = 0; i < items.size(); i++){
			if (items.get(i).remove)
				items.remove(i);
			if (displayItems.get(i).remove)
				displayItems.remove(i);}
		for (int i = 0; i < items.size(); i++)
			if (items.get(i).onBoard())
				displayItems.add(items.get(i));
		for (int i = 0; i < displayItems.size(); i++)
			if (!displayItems.get(i).onBoard())
				displayItems.remove(i);
		for (int i = 0; i < displayItems.size(); i++)
			displayItems.get(i).update();
	}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		for (int i = 0; i < displayItems.size(); i++)
			displayItems.get(i).render(g);
	}
}
class Item implements Displayable, Serializable{
	public String type, appearance;
	public int stackAmount, maxStack, lifetime, age;
	private WorldLocation location;
	private Point screenpos;
	private int x, y;
	public boolean remove, dropped;
	public Item(String type){
		age = 0;
		stackAmount = 1;
		this.type = type; // logs apple dirt 
		switch(type){
			case "logs":
				lifetime = 20000;
				maxStack = 128;
				break;
			case "apple":
				lifetime = 7000;
				maxStack = 32;
				break;
			case "darkdirt":
				lifetime = 4000;
				maxStack = 64;
				break;
		}
		appearance = type;
		remove = false;
		dropped = false;
	}
	public void drop(WorldLocation location){
		this.location = new WorldLocation(location);
		Point pos = Main.locationToPoint(location);
		do{
			x = Main.rand.nextInt(96)-16;
			y = Main.rand.nextInt(96)-16;
			pos = Main.locationToPoint(location);
			pos.x+=x;pos.y+=y;
		}while(!Main.getWorldPiece(Main.pointToLocation(pos)).isEmpty);
		dropped = true;
		Main.items.addItem(this);
	}
	public boolean addToStack(){
		if (stackAmount<maxStack){
			stackAmount++;
			return true;}
		else return false;
	}
	public boolean takeFromStack(){
		if (stackAmount > 0){
			stackAmount--;
			return true;}
		else return false;
	}
	public boolean fullStack(){
		return stackAmount>=maxStack;
	}
	public void pickUp(){
		remove = Main.player.inventory.addItem(this);
	}
	public boolean onBoard(){
		screenpos = Main.locationToPoint(location);
		int x = screenpos.x/64,
		y = screenpos.y/64;
		return x>0&&x<16&&y>0&&y<16;
	}
	public void update(){
		if (dropped) age++;
		if (age>=lifetime)remove = true;
		screenpos = Main.locationToPoint(location);
		double partial = 64*Main.player.partial();
		int xoff = (int)(partial*Main.player.dx), yoff = (int)(partial*Main.player.dy);
		if (Math.abs(96+64*7-(screenpos.x+x+16-xoff))<48
			&&Math.abs(96+64*7-(screenpos.y+y+16-yoff))<64&&!remove)
			pickUp();
		dropped = !remove;
		
	}
	public void render(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		double partial = 64*Main.player.partial();
		int xoff = (int)(partial*Main.player.dx), yoff = (int)(partial*Main.player.dy);
		if (onBoard()&&dropped)
			g.drawImage(Main.graphics.getItem(appearance),null,screenpos.x+x-xoff-16,screenpos.y+y-yoff-16);
	}
}
class Game implements Serializable{
	public int portalNumber, roomNumber;
	public boolean firstPortal;
	private long time;
	public Game(){
		time = 0;
		portalNumber = 0;
		roomNumber = 0;
		firstPortal = true;
	}
	public void nextPortal(){
		if (!firstPortal) portalNumber++;
		firstPortal = !firstPortal;
	}
	public void nextRoom(){roomNumber++;}
	public void tick(){time++;}
	public long getTime(){return time;}
}
class Chunk implements Serializable {
	public WorldLocation location;
	public Piece[][] chunk;
	public String type;
	public transient BufferedImage mappingimg;
	public Chunk(String type, WorldLocation location){
		chunk = new Piece[8][8];
		this.type = type;
		this.location = new WorldLocation(location);
		location.chunkX = 0;
		location.chunkY = 0;
		genChunk(type);
		save();
		genMapping();
	}
	private void readObject(ObjectInputStream in)    throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		genMapping();
	}
	public void save(){
		String middle = (location.dimension.indexOf("room")!=-1 ? "Rooms//" : ""),
		folder = "SaveFiles//"+Main.saveFolder+"//Chunks//"+middle+Character.toUpperCase(location.dimension.charAt(0)) 
			+""+location.dimension.substring(1)+"//";
		genMapping();
		Main.save(this,folder+Main.toB36(location.worldX)+"_"+Main.toB36(location.worldY)+".dat");
	}
	public void setChunkCoords(){
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++){
				chunk[i][j].setCoords(new WorldLocation(location.dimension,location.worldX,location.worldY,j,i));
			}
	}
	private boolean isAdjacentTo(Piece piece, int x, int y, int num){
		boolean up, down, left, right;
		if (x!=0) left = chunk[y][x-1].appearance.equals(piece.appearance);
		else left = false;
		if (x!=7) right = chunk[y][x+1].appearance.equals(piece.appearance);
		else right = false;
		if (y!=0) up = chunk[y-1][x].appearance.equals(piece.appearance);
		else up = false;
		if (y!=7) down = chunk[y+1][x].appearance.equals(piece.appearance);
		else down = false;
		switch(num){
			case 0:
				return !(up||down||left||right);
			case 1:
				return up||down||left||right;
			case 2:
				return (up&&down)||(down&&left)||(left&&right)||(right&&up)||(up&&left)||(down&&right);
			case 3:
				return (up&&down&&left)||(up&&down&&right)||(up&&left&&right)||(down&&left&&right);
			case 4:
				return up&&down&&left&&right;
			default: return false;
		}
	}
	private void genChunk(String type){
		if (type.indexOf("room")!=-1) type = "room";
		for (int i = 0; i < chunk.length; i++)
			for (int j = 0; j < chunk[0].length; j++)
				chunk[i][j] = Main.getGround(location);
		switch(type){
			case "surface":
				for (int i = 0; i < Main.rand.nextInt(10)+20; i++)
					chunk[Main.rand.nextInt(chunk.length)]
					[Main.rand.nextInt(chunk[0].length)] = new Tree("tree");
				break;
			case "underground":
				for (int i = 0; i < 8; i++)
					for (int j = 0; j < 8; j++)
						chunk[i][j] = new Dirt();
				break;
			case "room":
				for (int i = 0; i < 8; i++)
					for (int j = 0; j < 8; j++)
						chunk[i][j] = new Void();
				if (location.worldX==0&&location.worldY==0)
					for (int i = 3; i < 6; i++)
						for (int j = 3; j < 6; j++)
							chunk[i][j] = new RoomFloor();
		}
		setChunkCoords();
	}
	private void genMapping(){
		Mapping map = new Mapping(this);
		Main.minimap.addMapping(map);
	}
	public BufferedImage getMapping(){
		return mappingimg;
	}
	public void update(){for (int i = 0; i < 8; i++) 
		for (int j = 0; j < 8; j++) chunk[i][j].update();}
	public String getPA(int x, int y){ return chunk[y][x].appearance;}
}
class Counter implements Serializable{
	public int length, increment;
	public int count;
	public boolean loop, pauseable, remove;
	private boolean running;
   	private Arc2D.Float arc;
	private Color color;
	public Counter(int length){
		remove = false;
		this.length = length;
		pauseable = false;
		increment = 1;
		loop = false;
		arc = new Arc2D.Float(Arc2D.PIE);
		color = new Color(210,210,210,169);
	}
	public void start(){
		count = 0;
		running = true;
	}
	public boolean ready(){return count >= length;}
	public void finish(){count = length;}
	public void jump(int amount){if (running) count+=amount;}
	public void setIncrement(int amount){increment = amount;}
	public void update(){if (running && count < length)count+=increment;}
	public void brake(){if (pauseable)running = false;else consume();}
	public void resume(){running = true;if (count==0) start();}
	public void consume(){if (!loop)running = false;count = 0;}
	public void drawProgress(Graphics2D g){
		arc.setFrame(16, 16, 32, 32);
		arc.setAngleStart(90+360*(1-progress()));
		arc.setAngleExtent(360*progress());
		g.setColor(color);
		g.fill(arc);}
	public double progress(){
		return (double)count/(double)length;}
}
class Piece implements Serializable{
	public WorldLocation location;
	public long age;
	public String appearance, currentAction;
	public String[] actions;
	public boolean canWalkthrough, isEmpty, canLift, showGround;
	public Piece(String appearance){
		this.appearance = appearance;
		canWalkthrough = true;
		age = Main.game.getTime();
		isEmpty = false;
		canLift = false;
		currentAction = "";
		showGround = true;
		actions = new String[]{""};
	}
	public void render(Graphics2D g0, int x, int y){
		Graphics2D g = (Graphics2D)g0.create();
		g.translate(x,y);
		if (showGround)
			g.drawImage(Main.graphics.getImg(Main.getGround(location).appearance),null,0,0);
		g.drawImage(Main.graphics.getImg(appearance),null,0,0);
		renderCounter(g);
	}
	public void startAction(String action){}
	public void renderCounter(Graphics2D g0){}
	public void setCoords(WorldLocation location){this.location = new WorldLocation(location);}
	public void update(){age = Main.game.getTime();}
	public void doAction(String action){}
	public void stopAction(){}
	public boolean hasAction(String action){
		for (int i = 0; i < actions.length; i++)
			if (actions[i].equals(action))return true;
		return false;
	}
	public void walkOn(){};
	public void remove(){Main.setWorldPiece(Main.getGround(location),location);}
	public boolean isType(String type){return type.equals(appearance);}
	public boolean hasTrait(String trait){return false;}
	public String[] getActions(){return actions;}
	/*public void renderProgress(Graphics2D g, int x, int y){}
	public boolean getActionReady(){return false;}
	public void consumeAction(){}
	public Counter getCounter(String action){return new Counter(1);}
	*/
}
class Grass extends Piece {
	private Counter dig;
	public Grass(){
		super("grass");
		canWalkthrough = true;
		isEmpty = true;
		dig = new Counter(60);
		actions = new String[]{"dig"};
		showGround = false;
	}
	@Override
	public void renderCounter(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		if (!dig.ready())
			dig.drawProgress(g);
	}
	@Override 
	public void update(){
		if (currentAction.equals("dig")){
			dig.update();
			if (dig.ready())
				doAction("dig");
		}
	}
	@Override
	public void startAction(String action){
		if (action.equals("dig")){
			currentAction = "dig";
		}
	}
	@Override 
	public void stopAction(){
		dig.consume();
	}
	@Override 
	public void doAction(String action){
		if (action.equals("dig")&&location.dimension.equals("surface")){
			Main.setWorldPiece(new Hole(),location);
			currentAction = "";
			dig.consume();}
		if (action.equals("place")) Main.player.putdown(location);
	}
	@Override
	public boolean hasTrait(String trait){return trait.equals("irremovable");}
}
class Tree extends Piece {
	public Counter growth, cut, dig;
	public Tree(String stage){
		super(stage);
		age = Main.game.getTime();
		canWalkthrough = false;
		growth = new Counter(512);//2048);
		cut = new Counter(80);
		dig = new Counter(40);
		changeAppearance(appearance);
		isEmpty = false;
	}
	private void catchUp(long ticks){
		for (long l = 0; l < ticks; l++){
			//growth.update();
			update();
		}
	}
	@Override 
	public void stopAction(){
		cut.consume();
		dig.consume();
	}
	@Override
	public void update(){
		if (Main.game.getTime()-age>20){
			long missed = Main.game.getTime()-age;
			age = Main.game.getTime();
			catchUp(missed);
		}
		else	age = Main.game.getTime();


		growth.update();
		if (currentAction.equals("cut")){
			cut.update();
			if (cut.ready())
				doAction("cut");
		}
		if (currentAction.equals("dig")){
			dig.update();
			if (dig.ready())
				doAction("dig");
		}


		if (appearance.equals("seedling") && growth.ready()){
			changeAppearance("sapling");
			canWalkthrough = false;
			growth.start();
		}
		if (appearance.equals("sapling") && growth.ready()){
			changeAppearance("tree");
			actions = new String[]{"cut"};
			growth.consume();
		}
	}
	@Override
	public void renderCounter(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		if (!growth.ready())
			growth.drawProgress(g);
		if (!cut.ready())
			cut.drawProgress(g);
		if (!dig.ready())
			dig.drawProgress(g);
	}
	@Override
	public void startAction(String action){
		switch(action){
			case "cut":
				if (appearance.equals("tree")){
					cut.start(); currentAction = "cut";}
				break;
			case "dig":
				if ("deadseedling stump".indexOf(appearance)!=-1){
					dig.start(); currentAction = "dig";}
				break;
		}
	}
	private void changeAppearance(String appr){
		appearance = appr;
		switch(appearance){
			case "seedling":
				actions = new String[]{"instagrow","kill"};
				canWalkthrough = true;
				growth.setIncrement(4);
				growth.start();
				break;
			case "sapling":
				actions = new String[]{"instagrow"};
				canWalkthrough = false;
				growth.setIncrement(2);
				growth.start();
				break;
			case "tree":
				actions = new String[]{"cut"};
				canWalkthrough = false;
				growth.consume();
				break;
			case "stump":
				actions = new String[]{"dig"};
				canWalkthrough = true;
				break;
		}
		//Main.setWorldPiece(this,location);
	}
	@Override
	public void doAction(String action){
		if (action.equals("instagrow")) growth.finish();
		if (action.equals("kill")&&appearance.equals("seedling"))walkOn();
		if (action.equals("cut") && appearance.equals("tree")){
			changeAppearance("stump");
			canWalkthrough = true;
			dropItems();
			actions = new String[]{"dig"};
			cut.consume();
		}
		if (action.equals("dig") && (appearance.equals("stump") || appearance.equals("deadseedling"))){
			Main.setWorldPiece(new Hole(),location);
			dig.consume();
		}
	}/*
	@Override 
	public void consumeAction(){
		if (cut.ready()) cut.consume();
		if (dig.ready()) dig.consume();
	}
	@Override
	public boolean getActionReady(){
		if (cut.progress()>0)
			return cut.ready();
		if (dig.progress()>0)
			return dig.ready();
		return false;
	}*/
	@Override
	public void walkOn(){
		if (appearance.equals("seedling")){
			changeAppearance("deadseedling");
			actions = new String[]{"dig"};}
	}
	private void dropItems(){
		if (Main.rand.nextBoolean()){
			new Item("logs").drop(location);
		}
		else if (Main.rand.nextBoolean()&&Main.rand.nextBoolean()){
			new Item("apple").drop(location);
		}
	}
}
class Hole extends Piece {
	private Counter fill;
	public Hole(){
		super("hole");
		canWalkthrough = true;
		isEmpty = true;
		fill = new Counter(200);
		fill.start();
		actions = new String[]{"plant"};
	}
	@Override
	public void renderCounter(Graphics2D g0){
		Graphics2D g = (Graphics2D) g0.create();
		if (!fill.ready())
			fill.drawProgress(g);
	}
	@Override
	public void startAction(String action){
		if (action.equals("plant"))
			doAction("plant");
	}
	@Override
	public void update(){
		fill.update();
		if (fill.ready()&&appearance.equals("hole")){
			appearance = "partialhole";
			fill.start();
		}
		else if (fill.ready())
			Main.setWorldPiece(new Grass(),location);
	}
	@Override 
	public void doAction(String action){
		if (action.equals("plant")) Main.setWorldPiece(new Tree("seedling"),location);
		if (action.equals("place")) Main.player.putdown(location);
	}
}
class Rock extends Piece {
	public Rock(){
		super("rock");
		canWalkthrough = false;
		canLift = true;
	}
}
class Water extends Piece {
	public Water(){
		super("water");
		canWalkthrough = false;
		showGround = false;
	}
}
class Portal extends Piece implements Serializable{
	public int number;
	private Portal partner;
	public boolean first;
	public boolean linked;
	public Portal(WorldLocation location){
		super("inactiveportal");
		this.location = new WorldLocation(location);
		number = Main.game.portalNumber;
		first = Main.game.firstPortal;
		if (first) 
			Main.save(this,"SaveFiles//"+Main.saveFolder+"//World//Portals//"+Main.toB36(number)+"_1.dat");
		else if (Main.load("SaveFiles//"+Main.saveFolder+"//World//Portals//"+Main.toB36(number)+"_1.dat")==null){
			Main.game.firstPortal = true;
			Main.save(this,"SaveFiles//"+Main.saveFolder+"//World//Portals//"+Main.toB36(number)+"_1.dat");
		}
		else { partner = (Portal)Main.load("SaveFiles//"+Main.saveFolder+"//World//Portals//"+Main.toB36(number)+"_1.dat");
			linked = true;actions = new String[]{"teleport"}; appearance = "portal";
			partner.link(this); Main.setWorldPiece(this,location);
			Main.save(this,"SaveFiles//"+Main.saveFolder+"//World//Portals//"+Main.toB36(number)+"_2.dat");}
		Main.game.nextPortal();
		canWalkthrough = true;
	}
	@Override
	public void remove(){breakLink();}
	public void breakLink(){
		if (first) Main.game.firstPortal = true;
		if (linked){
			linked = false;
			actions = new String[]{""};
			partner.breakLink();}
		linked = false;
		Main.setWorldPiece(Main.getGround(location),location);
		
	}
	private void link(Portal partner){
		this.partner = partner;
		linked = true;
		appearance = "portal";
		actions = new String[]{"teleport"};
		Main.setWorldPiece(this,location);
		Main.save(this,"SaveFiles//"+Main.saveFolder+"//World//Portals//"+Main.toB36(number)+"_1.dat");
	}
	@Override
	public void doAction(String action){
		if (action.equals("teleport")){
			Main.board.saveBoard();
			if (first) Main.save(this,"SaveFiles//"+Main.saveFolder+"//World//Portals//"+Main.toB36(number)+"_1.dat");
			else Main.save(this,"SaveFiles//"+Main.saveFolder+"//World//Portals//"+Main.toB36(number)+"_2.dat");
			if (linked)
				Main.player.teleport(new WorldLocation(partner.location));
		}
	}
	@Override
	public boolean hasTrait(String trait){return trait.equals("teleporter");}
}
class Ladder extends Piece {
	public Ladder(){
		super("ladderabove");
		location = Main.player.location;
		if (location.dimension.equals("underground"))
			appearance = "ladderbelow";
		canWalkthrough = true;
		actions = new String[]{"climb"};
	}
	@Override 
	public void doAction(String action){if (action.equals("climb")) Main.player.switchUnderground();}
	@Override
	public void remove(){
		Main.setWorldPiece(Main.getGround(location),location);
		location.change(new String[]{"flipdimension"},new String[]{""});
		Main.setWorldPiece(Main.getGround(location),location);
	}
}
class DirtFloor extends Piece {
	public DirtFloor(){
		super("dirt");
		canWalkthrough = true;
		isEmpty = true;
		showGround = false;
	}
	@Override
	public boolean hasTrait(String trait){return trait.equals("irremovable");}
	@Override 
	public void doAction(String action){
		if (action.equals("place")) Main.player.putdown(location);
	}
}
class Dirt extends Piece {
	public Dirt(){
		super("darkdirt");
		canWalkthrough = false;
		actions = new String[]{"dig"};
		showGround = false;
	}
	@Override
	public void doAction(String action){
		if (action.equals("dig")){
			Main.setWorldPiece(new DirtFloor(), location);
			dropItems();}
	}
	private void dropItems(){new Item("darkdirt").drop(location);}
}
class WoodBlock extends Piece {
	public WoodBlock(){
		super("woodblock");
		canWalkthrough = false;
		canLift = true;
		actions = new String[]{"pickup"};
	}
	@Override
	public void doAction(String action){
		if (action.equals("pickup")){
			Main.player.pickup(this);
			Main.setWorldPiece(Main.getGround(location),location);}
	}
}
class RoomEntryPortal extends Piece {
	public int roomNumber;
	private Room room;
	public boolean roomExists;
	public RoomEntryPortal(){
		super("roomportal");
		location = Main.player.standing().location;
		canWalkthrough = true;
		roomNumber = Main.game.roomNumber;
		Main.game.nextRoom();
		roomExists = false;
		actions = new String[]{"teleport"};
	}
	@Override 
	public void doAction(String action){
		if(action.equals("teleport")){if (!roomExists)
			{room = new Room(this);room.create();roomExists = true;} room.enter();}
		if(action.equals("remove"))Main.setWorldPiece(Main.getGround(location),location);
	}
	@Override
	public boolean hasTrait(String trait){return trait.equals("teleporter");}
	@Override
	public void remove(){
		if (roomExists)
			if (room.isEmpty())
				room.remove();
		if (!roomExists) Main.setWorldPiece(Main.getGround(location),location);
	}
	@Override 
	public boolean isType(String type){return type.equals("roomentry");}
}
class RoomExitPortal extends Piece{
	private Room room;
	public RoomExitPortal(Room room){
		super("roomportal");
		this.room = room;
		location = new WorldLocation(room.dimension,0,0,4,4);
		Main.setWorldPiece(this,location);
		canWalkthrough = true;
		actions = new String[]{"teleport"};
		actions = new String[]{"teleport"};
	}
	@Override 
	public void doAction(String action){
		if(action.equals("teleport")) room.exit();
	}
	@Override 
	public boolean hasTrait(String trait){return trait.equals("teleporter");}
	@Override
	public void remove(){
		if (room.isEmpty())
			room.remove();
	}
	@Override 
	public boolean isType(String type){return type.equals("roomexit");}
}
class RoomFloor extends Piece {
	public RoomFloor(){
		super("roomfloor");
		canWalkthrough = true;
		isEmpty = true;
		showGround = false;
	}
	@Override 
	public boolean hasTrait(String trait){return trait.equals("irremovable");}
}
class Room implements Serializable{
	private int roomNumber;
	private RoomEntryPortal entry;
	private RoomExitPortal exit;
	public String dimension;
	public WorldLocation location;
	public Piece ground;
	public boolean exists;
	public Room(RoomEntryPortal entry){
		exists = false;
		this.entry = entry;
		roomNumber = entry.roomNumber;
		dimension = "room_"+roomNumber;
		ground = new RoomFloor();
		location = new WorldLocation(dimension,0,0,4,4);
		Chunk chunk = new Chunk(dimension, location);
	}
	public void create(){
		exit = new RoomExitPortal(this);
		exit.setCoords(location);
	}
	public void enter(){
		Main.player.teleport(new WorldLocation(dimension,0,0,4,4));
		if (!entry.roomExists)
			Main.setWorldPiece(exit,location);
	}
	public void exit(){
		Main.board.saveBoard();
		Main.player.teleport(entry.location);
	}
	public void remove(){
		if (Main.player.location.dimension.equals(dimension)) exit();
		Main.setWorldPiece(Main.getGround(entry.location),entry.location);
		try{	Main.removeRecursive(Paths.get("SaveFiles//"+Main.saveFolder+"//Chunks//Rooms//"
		+Character.toUpperCase(dimension.charAt(0))+dimension.substring(1)));
		}catch (Exception e){}
	}
	public boolean isEmpty(){
		String[] chunkpaths = Main.getFolderFileNames("SaveFiles//"+Main.saveFolder+"//Chunks//Rooms//"
			+Character.toUpperCase(dimension.charAt(0))+dimension.substring(1));
		Chunk[] roomchunks = new Chunk[chunkpaths.length];
		for (int i = 0; i < chunkpaths.length; i++)
			roomchunks[i] = (Chunk)Main.load(chunkpaths[i]);
		for (int i = 0; i < roomchunks.length; i++)
			for (int j = 0; j < 8; j++)
				for (int k = 0; k < 8; k++)
					if (!(roomchunks[i].chunk[j][k].isType("roomfloor")
						||roomchunks[i].chunk[j][k].isType("void")
						||roomchunks[i].chunk[j][k].isType("roomexit")))
						return false;
		return true;
	}
}
class Void extends Piece {
	public Void(){
		super("void");
		canWalkthrough = false;
		showGround = false;
	}
	@Override
	public boolean hasAction(String action){
		return action.equals("invincible");
	}
}