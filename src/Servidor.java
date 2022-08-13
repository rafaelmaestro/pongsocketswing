//Rafael Maestro dos Santos, 201021137

import java.net.Socket;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.ServerSocket;
import java.io.PrintWriter;

public class Servidor {

    static ServerSocket server=null;
    static int port;
    
    public static void main(String[] args) {

        try {
            port = 5500;
            System.out.println("Server running on port " + port);
            server = new ServerSocket(port);
            while (true) {
                ServerConfigs pingpong = new ServerConfigs();
                ServerConfigs.Player player1 = pingpong.new Player(server.accept(), '1', 0);
                ServerConfigs.Player player2 = pingpong.new Player(server.accept(), '2', 0);
                
                
                player1.setOpponent(player2);
                player2.setOpponent(player1);
                pingpong.currentPlayer = player1;
                
                player1.start();
                player2.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ServerConfigs{
	

    Player currentPlayer;
    
    class Player extends Thread {
    
        char mark;
        int score;
        BufferedReader in;
        PrintWriter out;
        Player opponent;
        Socket socket;

        public Player(Socket socket, char mark, int score) {
            this.socket = socket;
            this.mark = mark;
            this.score = score;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("WELCOME " + mark);
                out.println("MESSAGE Waiting for opponent to connect");
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }
        
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        public void updateOpponent(String message) {
            currentPlayer = this.opponent;
            currentPlayer.otherOpponent(message);
        }

        public void otherOpponent(String message) {
            out.println(message);
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
                        
                    }
                    if (cmd.equals("DOWN")) {
                        out.println("DOWN " + this.mark);
                        
                    }
                    if(cmd.equals("GO")) {
                        updateOpponent(cmd);
                        out.println(cmd);
                        
                    }
                    if (cmd.startsWith("Ball Move: ")) {
                        out.println(cmd);
                        updateOpponent(cmd);
                    }
                    if (cmd.startsWith("Paddle1 Move: ")) {
                        updateOpponent(cmd);
                    }
                    if (cmd.startsWith("Paddle2 Move: ")) {
                        updateOpponent(cmd);
                    }
                    if (cmd.startsWith("GAME OVER: ")) {
                        out.println(cmd);
                        updateOpponent(cmd);
                    }
                    if (cmd.equals("Player1 scored")) {
                        
                        if(this.mark == '1') {
                            this.score++;
                            updateOpponent("Player1: " + this.score);
                            out.println("Player1: " + this.score);
                        } else {
                            this.opponent.score++;
                            updateOpponent("Player1: " + this.opponent.score);
                            out.println("Player1: " + this.opponent.score);
                        }
                        checkIfHaveGameWinner();
                    }
                    if (cmd.equals("Player2 scored")) {

                        if(this.mark == '2') {
                            this.score++;
                            updateOpponent("Player2: " + this.score);
                            out.println("Player2: " + this.score);
                        } else {
                            this.opponent.score++;
                            updateOpponent("Player2: " + this.opponent.score);
                            out.println("Player2: " + this.opponent.score);
                        }
                        checkIfHaveGameWinner();
                    }
                    if (cmd.startsWith("QUIT"))
                        break;
                }
            } catch (IOException e) {
                System.out.println("GAME OVER ");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
