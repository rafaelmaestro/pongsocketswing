//Rafael Maestro dos Santos, 201021137

import java.net.Socket;

import javax.swing.JOptionPane;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.io.PrintWriter;

// defining the Server class that will hold the main method and will be
//the server that will be listening for the clients

public class Servidor {

    //creating the ServerSocket instance already with the null value because i wanted to start the
    //server directly from the main method

    static ServerSocket server=null;
    //const holding the port value that will be used to start the server
    // i used 5505 because i used to it
    static int port;
    
    public static void main(String[] args) {

        try {
            port = 5500;
            System.out.println("Server running on port " + port);
            server = new ServerSocket(port);
            while (true) {
                //creating a serverConfigs instace that will be used to hold the server configuration
                ServerConfigs pingpong = new ServerConfigs();
                //setting the client instances
                ServerConfigs.Player player1 = pingpong.new Player(server.accept(), '1', 0);
                ServerConfigs.Player player2 = pingpong.new Player(server.accept(), '2', 0);
                
                
                pingpong.currentPlayer = player1;
                player1.setOpponent(player2);
                player2.setOpponent(player1);
                
                player1.start();
                player2.start();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error closing the server");
            }
        }
    }
}

class ServerConfigs {
	//class ServerConfigs will hold the server configurations (|??????|)
    // setting the data out and in variables to be used in the server

    Player currentPlayer;
    
    class Player extends Thread{
        //client thread init 
    
        char mark;
        int score;
        BufferedReader in;
        PrintWriter out;
        Player opponent;
        Socket socket;

        public Player(Socket socket, char mark, int score) {
            this.socket = socket;
            this.mark = mark; // setting the mark of the player connected (1 or 2)
            this.score = score;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // i chose the printWriter class because it seems to be easier and have autoFlush
                // standard
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("WELCOME " + mark);
                out.println("MESSAGE Waiting for opponent to connect");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }
        
        //setting the opponent for the player 
        // player 1 will have player 2 as opponent and vice versa
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
            System.out.println("setOpponent" + opponent);
        }

        public void otherOpponent(String message) {
            out.println(message);
            System.out.println("other opponent" + message);
        }

        public void updateOpponent(String message) {
            currentPlayer = this.opponent;
            currentPlayer.otherOpponent(message);
            System.out.println("update opponent" + message);
        }

        public void checkIfHaveGameWinner() {
            if(this.score == 4) {
                updateOpponent("You Lose!");
                out.println("You Win!");
            }else if(this.opponent.score == 4) {
                updateOpponent("You Win!");
                out.println("You Lose!");
            }
        }
        
        public void run() {
            
            try {
                out.println("MESSAGE All players connected");	
                out.println("MESSAGE Click Your Mouse to Start");
                
                
                
                while (true) {
                    String cmd = in.readLine();

                    if (cmd.equals("UP")) {
                        out.println("UP " + this.mark);
                        System.out.println("UP " + this.mark);
                        
                    }
                    if (cmd.equals("DOWN")) {
                        out.println("DOWN " + this.mark);
                        System.out.println("DOWN " + this.mark);
                    }
                    if(cmd.equals("GO")) {
                        updateOpponent(cmd);
                        out.println(cmd);
                        System.out.println(cmd);
                    }
                    if (cmd.startsWith("Ball Move: ")) {
                        out.println(cmd);
                        updateOpponent(cmd);
                        System.out.println(cmd);
                    }
                    if (cmd.startsWith("Paddle1 Move: ")) {
                        updateOpponent(cmd);
                        System.out.println(cmd);
                    }
                    if (cmd.startsWith("Paddle2 Move: ")) {
                        updateOpponent(cmd);
                        System.out.println(cmd);
                    }
                    if (cmd.startsWith("GAME OVER: ")) {
                        out.println(cmd);
                        updateOpponent(cmd);
                        System.out.println(cmd);
                    }
                    if (cmd.equals("Player1 scored")) {
                        
                        if(this.mark == '1') {
                            this.score++;
                            updateOpponent("Player1: " + this.score);
                            out.println("Player1: " + this.score);
                            System.out.println("Player1: " + this.score);
                        } else {
                            this.opponent.score++;
                            updateOpponent("Player1: " + this.opponent.score);
                            out.println("Player1: " + this.opponent.score);
                            System.out.println("Player1: " + this.opponent.score);
                        }
                        checkIfHaveGameWinner();
                    }
                    if (cmd.equals("Player2 scored")) {

                        if(this.mark == '2') {
                            this.score++;
                            updateOpponent("Player2: " + this.score);
                            out.println("Player2: " + this.score);
                            System.out.println("Player2: " + this.score);
                        } else {
                            this.opponent.score++;
                            updateOpponent("Player2: " + this.opponent.score);
                            out.println("Player2: " + this.opponent.score);
                            System.out.println("Player2: " + this.opponent.score);
                        }
                        checkIfHaveGameWinner();
                    }
                    if (cmd.startsWith("QUIT")){
                        out.println(cmd);
                        updateOpponent(cmd);
                        System.out.println(cmd);
                        break;
                    }

                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
                }
            }
        }
    }
}
