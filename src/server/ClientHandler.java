package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private Server server;
    private Socket socket;
    DataOutputStream out;
    DataInputStream in;
    private String nick;
    private ExecutorService executorService;

    public ClientHandler(Server server, Socket socket) {

        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            executorService = Executors.newCachedThreadPool();

            executorService.submit(() -> {
                try {
                    // ставим таймаут на сокет.
                    socket.setSoTimeout(100000);

                    // поток проверки лимита времени
//                    new Thread(() -> {
//                        try {
//                            Thread.sleep(5000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        if(!server.isNickAuthorized(nick)){
//                            try {
//                                in.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
////                            throw new RuntimeException("Клиент отключен за бездействие");
//                        }
//                    }).start();


                    // цикл авторизации.
                    while (true) {
                        String str = in.readUTF();
                        if(str.startsWith("/auth")){
                            String[] token = str.split(" ");
                            String newNick =
                                    AuthService.getNickByLoginAndPass(token[1],token[2]);
                            if(newNick != null){
                                if(!server.isNickAuthorized(newNick)){
                                    //   sendMsg("/authok");
                                    // сразу передадим ник клиенту
                                    sendMsg("/authok "+newNick);

                                    nick = newNick;
                                    server.subscribe(this);

                                    try {
                                        socket.setSoTimeout(0);
                                    } catch (SocketException e1) {
                                        e1.printStackTrace();
                                    }

                                    break;
                                }else{
                                    sendMsg("Учетная запись уже используется");
                                }
                            }else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                        //регистрация
                        if(str.startsWith("/reg ")){
                            String[] token = str.split(" ");
                            if(AuthService.registration(token[1], token[2], token[3])) {
                                sendMsg("Регистрация прошла успешно");
                            }else{
                                sendMsg("Регистрация окончилась сбоем");
                            }
                        }



                    }

                    //Цикл для работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            System.out.println("Клиент отключился");
                            break;
                        }
                        if(str.startsWith("/w ")){
                            String[] token = str.split(" +",3);
                            server.broadcastMsg(token[2], getNick(), token[1]);
                        }else{
                            System.out.println(str);
                            server.broadcastMsg(str,getNick());
                        }
                        //смена ника, путем отправки из поля ввода сообщений с префиксом
                        if (str.startsWith("/cN ")){
                            String[] token = str.split(" ");
                            try {
                                AuthService.changeNick(token[1], token[2]);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (SocketTimeoutException e){
                    System.out.println(" таймаут истек socket.hashCode() " + this.hashCode());
                    sendMsg("/end");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    executorService.shutdown();
                    server.unsubscribe(this);
                    System.out.println("Клиент оключился");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String str) {
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
