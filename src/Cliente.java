//Rafael Maestro dos Santos, 201021137

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;


public class Cliente {
		private JFrame frame = new JFrame("Pong Game by Rafael Maestro");

		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		private Timer t;
		private Pingpong a;
		private int score1 = 0;
		private int score2 = 0;
		
		public Cliente()  {
			try {
				
				socket = new Socket("127.0.0.1", 5500);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				a = new Pingpong(out, score1, score2); 
				frame.add(a);
				frame.addKeyListener(a);
				frame.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						out.println("GO");
						
						if(a.ballx == 390) {
							t = new Timer(10, new TimerListener(a.getBall()));
							t.start();
						}
					}
				});
			} catch (UnknownHostException e1) {
				JOptionPane.showMessageDialog(null, "Host not found");
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Error in connection");
				
			}
		}

		public static void main(String args[])  {
			while (true) {
				
				Cliente client = new Cliente();
				client.frame.setSize(800, 550);
				client.frame.setLocationRelativeTo(null);
				client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				client.frame.setVisible(true);
				client.frame.setResizable(false);
				
				try {
					client.play();
					if (!client.wantsToPlayAgain()) {
						break;
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error in connection");
					
				}
				
			}
		}

		
		class TimerListener implements ActionListener {
			private Pingpong.Ball b;

			public TimerListener(Pingpong.Ball b) {
				this.b = b;
			}

			public void actionPerformed(ActionEvent e) {
					b.ballMove();
					out.println("Ball Move: " + b.getX() + " " + b.getY());
					if (b.getX() > 750)
						{
						out.println("Player1 scored");
						t.stop();
					
						}
					if (b.getX() < 50)
						{
						out.println("Player2 scored");
						t.stop();
						
						}
				
				frame.repaint();
					
			}

		}
		public void play()  {
			String response;
			try {
				response = in.readLine();
				if (response.startsWith("WELCOME")) {
					char mark = response.charAt(8);
					out.println("PLAYER: " + mark );
					System.out.println(response);
					frame.setTitle("Ping-Pong Game Player " + mark);
				}
				
				while (true) {
					response = in.readLine();
					
					if (response.startsWith("UP ")) {
						String player = response.substring(3);
						a.moveUp(player);	
						System.out.println(response);
						System.out.println(player);
					}else if (response.startsWith("DOWN ")) {
						String player = response.substring(5);
						a.moveDown(player);
						System.out.println(response);
						System.out.println(player);
					}else if (response.equals("GO")) {
							Pingpong.message = "";
							frame.repaint();
							
					
					}else if (response.startsWith("Paddle1 Move: ")) {
						String paddle1 = response.substring(14, response.length());
						
						int py1=  Integer.parseInt(paddle1);
						a.updateController1(py1);
					} else if (response.startsWith("Paddle2 Move: ")) {
						String paddle2 = response.substring(14, response.length());
						
						int py2=  Integer.parseInt(paddle2);
						a.updateController2(py2);
						
						
					} else if (response.startsWith("Ball Move: ")) {
						
						String ball = response.substring(11);
						String[] s = ball.split(" ");
						int ballx=Integer.parseInt(s[0]);
						int bally=Integer.parseInt(s[1]);
						
						a.updateBall(ballx,bally);
						
						
					} else if (response.startsWith("MESSAGE")) {
						Pingpong.message = response.substring(8);
						frame.repaint();
					} else if(response.startsWith("Player1: ")) {
						score1 = Integer.parseInt(response.substring(9));
						
						a.updateScore1(score1);
						a.getBall().setX(390);
						a.getBall().setY(210);
						a.updateBall(390, 210);
						Pingpong.message = "Click your mouse to start!";
						frame.repaint();
					} else if(response.startsWith("Player2: ")) {
						score2 = Integer.parseInt(response.substring(9));
						
						a.updateScore2(score2);
						a.getBall().setX(390);
						a.getBall().setY(210);
						a.updateBall(390, 210);
						Pingpong.message = "Click your mouse to start!";
						frame.repaint();
					} else if (response.equals("You Win!")) {
						Pingpong.message = response;
						frame.repaint();
						break;
					}else if (response.equals("You Lose!")) {
						Pingpong.message = response;
						frame.repaint();
						break;
					}
				}
				out.println("QUIT");

			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
				}
			}
		}

		
		private boolean wantsToPlayAgain() {
			int response = JOptionPane.showConfirmDialog(frame, "Want to try again?", "Pong",
					JOptionPane.YES_NO_OPTION);
			frame.dispose();
			return response == JOptionPane.YES_OPTION;
		}

	}
class Pingpong extends JPanel implements KeyListener {
	Image pongball;
	Image background;
	private Ball ball = new Ball();
	public static String message = "";
	private Font mFont = (new Font("Consolas", Font.BOLD, 30));
	private Font sFont = (new Font("Consolas", Font.BOLD, 30));
	private PrintWriter out; 
	int score1;
	int score2;
	String player1 = "";
	String player2 = "";
	int py1;
	int py2;
	int ballx;
	int bally;
	public Pingpong(PrintWriter pw, int s1, int s2) {
		out = pw;
		score1 = s1;
		score2 = s2;
		py1=200;
		py2=200;
		ballx=ball.getX();
		bally=ball.getY();
		
		try {
			background = ImageIO.read(new File("table.jpg"));
			pongball=ImageIO.read(new File("ball.png"));
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
	}
}
	public int getPY1(){
		return py1;
	}
	public int getPY2(){
		return py2;
	}
	public void setPY1(int py1) {
		this.py1=py1;
	}
	public void setPY2(int py2) {
		this.py2=py2;
	}

	public Ball getBall() {
		return ball;
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_UP) {
			out.println("UP");	
		}
		if (key == KeyEvent.VK_DOWN) {
			out.println("DOWN");	
		}		
	}

	public void keyReleased(KeyEvent e) {

	}

	
	public void paint(Graphics g) {
		super.paint(g);

		g.setFont(mFont);
		g.drawString(message, 160, 500);
		g.setColor(Color.black);
		
		g.fillRect(50, 50, 700, 400);
		g.drawImage(background, 50, 50, 700, 400, this);
	    g.setColor(Color.white);
		g.setFont(sFont);
		g.drawString(Integer.toString(score1), 355, 80);
		g.drawString(Integer.toString(score2), 425, 80);
		g.setColor(Color.BLACK);
		
	    this.getX();
	    this.getY();
	    g.drawImage(pongball, ballx, bally, 20,20,this);
	    this.getPY1();
	    this.getPY2();
	    g.setColor(Color.WHITE);
        g.fillRect(50,py1, 15, 80);
        g.setColor(Color.WHITE);
		g.fillRect(735, py2, 15, 80);
		Toolkit.getDefaultToolkit().sync();
		this.repaint();
		
		
		
	}
	public void updateController1(int py1) {
		this.py1=py1;
		this.setPY1(py1);
		Toolkit.getDefaultToolkit().sync();
		this.repaint();
	}
	public void updateController2(int py2) {
		this.py2=py2;
		this.setPY2(py2);
		Toolkit.getDefaultToolkit().sync();
		this.repaint();
	}
	

	public void updateBall(int x,int y) {
	
		ballx=x;
		bally=y;
		Toolkit.getDefaultToolkit().sync();
		this.repaint();
	}
	
	public void moveUp(String mark) {
		if(mark.equals("1")) {
			Toolkit.getDefaultToolkit().sync();
			this.repaint();
			py1=py1-50;
			if(py1<=50)
				py1=50;
			
			out.println("Paddle1 Move: " + py1);
			System.out.println("Paddle1 Move: " + py1);
		} else {
			Toolkit.getDefaultToolkit().sync();
			this.repaint();
			py2=py2-50;
			if(py2<=50)
				py2=50;
			
			out.println("Paddle2 Move: " + py2);
			System.out.println("Paddle2 Move: " + py2);
		}
	}
	
	public void moveDown(String mark) {
		if(mark.equals("1")) {
			Toolkit.getDefaultToolkit().sync();
			this.repaint();
			py1=py1+50;
			if(py1>=370)
				py1=370;
			
			out.println("Paddle1 Move: " + py1);
		} else {
			Toolkit.getDefaultToolkit().sync();
			this.repaint();
			py2=py2+50;
			if(py2>=370)
				py2=370;
			
			out.println("Paddle2 Move: " + py2);
		}
	}
	
	
	public void updateScore1(int s1) {
		score1 = s1;
	}
	public void updateScore2(int s2) {
		score2 = s2;
	}
	
class Ball {
		
		private int x;
		private int y;
		int m = 3;
		int n = 3;

		public Ball() {
			this.x = 390;
			this.y = 210;
		}

		public String toString() {
			return "(" + x + ". " + y + ")";
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public void ballMove() {
			x += m;
			y += n;
				
			if(x <= 55 && y >= getPY1() && y <= getPY1()+80 )
					m = -m;
			
			
			if(x >= 720 && y >= getPY2() && y <= getPY2()+80)
					m = -m;
			
			if ( y >= 430) {
				n = -n;
			}
			
			if ( y <= 50) {
				n = -n;
			}
		}
	}
}

class Point {
	private int x;
	private int y;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public String toString() {   
		return  "" + y; 
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y; 
	}

	public void setY(int y) {
		this.y = y;
	}
}





