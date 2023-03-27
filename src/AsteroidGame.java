import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JFrame;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class AsteroidGame extends JFrame{
	
	public static int boardWidth = 1000;
	public static int boardHeight = 800;
	
	public static boolean keyHeld = false;
	public static int keyHeldCode;
	
	public static ArrayList<PhotonTorpedo> torpedos = new ArrayList<PhotonTorpedo>();
	
	String thrustFile = "file:./src/thrust.au";
	String laserFile = "file:./src/laser.aiff";
	
	public static void main(String[] args) {
		
		new AsteroidGame();
		
	}
	
	public AsteroidGame() {
		
		this.setSize(boardWidth, boardHeight);
		this.setTitle("Java Asteroid Game");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == 87) { // W
					System.out.println("Forward");
					playSoundEffect(thrustFile);
					keyHeldCode = e.getKeyCode();
					keyHeld = true;
				}else if(e.getKeyCode() == 65) { //A
					System.out.println("Rotate Left");
					keyHeldCode = e.getKeyCode();
					keyHeld = true;
				}else if(e.getKeyCode() == 83) { //S
					System.out.println("BackwardBackward");
					keyHeldCode = e.getKeyCode();
					keyHeld = true;
				}else if(e.getKeyCode() == 68) { //D
					System.out.println("Rotate Right");
					keyHeldCode = e.getKeyCode();
					keyHeld = true;
				}else if(e.getKeyCode() == KeyEvent.VK_ENTER) { //enter
				
					playSoundEffect(laserFile);
					torpedos.add(new PhotonTorpedo(GameDrawingPanel.theShip.getShipNoseX(), GameDrawingPanel.theShip.getShipNoseY(),GameDrawingPanel.theShip.getRotationAngle()));
					System.out.println("RotationAngle " + GameDrawingPanel.theShip.getRotationAngle());
					
				}
				
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				
				keyHeld = false;
				
			}
			
		});
		
		GameDrawingPanel gamePanel = new GameDrawingPanel();
		
		this.add(gamePanel, BorderLayout.CENTER);
		
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
		
		executor.scheduleAtFixedRate(new RepaintTheBoard(this), 0L, 20L, TimeUnit.MILLISECONDS);
		
		this.setVisible(true);

	}
	
	public static void playSoundEffect(String soundToPlay) {
		
		URL soundLocation;
		
		try {
			
			soundLocation = new URL(soundToPlay);
			Clip clip = null;
			clip = AudioSystem.getClip();
			AudioInputStream inputStream;
			inputStream = AudioSystem.getAudioInputStream(soundLocation);
			
			clip.open(inputStream);
			clip.loop(0);
			clip.start();
			
		}catch(MalformedURLException e1){
			e1.printStackTrace();	
		}catch(UnsupportedAudioFileException | IOException e1){
			e1.printStackTrace();
		}catch(LineUnavailableException e1){
			e1.printStackTrace();
		}
		
	}
	
}

class RepaintTheBoard implements Runnable{
	
	AsteroidGame theBoard;
	
	public RepaintTheBoard(AsteroidGame theBoard) {
		
		this.theBoard = theBoard;
	}

	@Override
	public void run() {
		
		theBoard.repaint();
		
	}
}


class GameDrawingPanel extends JComponent{
	
	public ArrayList<Rock> rocks = new ArrayList<Rock>();
	
	int[] polyXArray = Rock.sPolyXArray;
	int[] polyYArray = Rock.sPolyYArray;
	
	static SpaceShip theShip = new SpaceShip();
	
	int width = AsteroidGame.boardWidth;
	int height = AsteroidGame.boardHeight;
	
	public GameDrawingPanel() {
		
		for(int i = 0; i < 10; i++) {
			int randomStartXPos = (int) (Math.random() * (AsteroidGame.boardWidth - 40) + 1);
			int randomStartYPos = (int) (Math.random() * (AsteroidGame.boardHeight - 40) + 1);
			
			rocks.add(new Rock(Rock.getPolyXArray(randomStartXPos), Rock.getPolyYArray(randomStartYPos), 13, randomStartXPos, randomStartYPos));
			Rock.rocks = rocks; 
		}
		
	}
	
	public void paint(Graphics g) {
		
		Graphics2D graphicSettings = (Graphics2D)g;
		
		AffineTransform identity = new AffineTransform();
		
		graphicSettings.setColor(Color.BLACK);
		graphicSettings.fillRect(0, 0, getWidth(), getHeight());

		graphicSettings.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphicSettings.setPaint( Color.WHITE);
		
		for(Rock rock : rocks){
			if(rock.onScreen) {
				rock.move(theShip, AsteroidGame.torpedos);
				graphicSettings.draw(rock);
			}
			
		}
		
		if(AsteroidGame.keyHeld == true && AsteroidGame.keyHeldCode == 68) {
			
			theShip.increaseRotationAngle();
			System.out.println("Ship Angle: " + theShip.getRotationAngle());

		} else if(AsteroidGame.keyHeld == true && AsteroidGame.keyHeldCode == 65) {
			
			theShip.decreaseRotationAngle();
			System.out.println("Ship Angle: " + theShip.getRotationAngle());

		} else if(AsteroidGame.keyHeld == true && AsteroidGame.keyHeldCode == 87) {
			
			theShip.setMovingAngle(theShip.getRotationAngle());
			theShip.increaseXVelocity(theShip.shipXMoveAngle(theShip.getMovingAngle())*0.1);
			theShip.increaseYVelocity(theShip.shipYMoveAngle(theShip.getMovingAngle())*0.1);
		}
		else if(AsteroidGame.keyHeld == true && AsteroidGame.keyHeldCode == 83) {
			
			theShip.setMovingAngle(theShip.getRotationAngle());
			theShip.decreaseXVelocity(theShip.shipXMoveAngle(theShip.getMovingAngle())*0.1);
			theShip.decreaseYVelocity(theShip.shipYMoveAngle(theShip.getMovingAngle())*0.1);
		}else if(AsteroidGame.keyHeld == true && AsteroidGame.keyHeldCode == KeyEvent.VK_ENTER) {
			
		}
		
		theShip.move();
		
		graphicSettings.setTransform(identity);
		graphicSettings.translate(theShip.getXCenter(),theShip.getYCenter());
		graphicSettings.rotate(Math.toRadians(theShip.getRotationAngle()));
		graphicSettings.draw(theShip);
		
		for(PhotonTorpedo torpedo : AsteroidGame.torpedos) {
			
			torpedo.move();
			if(torpedo.onScreen) {
				graphicSettings.setTransform(identity);
				graphicSettings.translate(torpedo.getXCenter(), torpedo.getYCenter());
				graphicSettings.draw(torpedo);
			}
		}
	}
}