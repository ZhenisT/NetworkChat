package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;


public class Controller {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public Button btnSend;
    @FXML
    public HBox upperPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox bottomPanel;
    @FXML
    public ListView<String> clientList;

    private Stage stage ;
    RegController regController = null;

//    добавил ник
    private String nick;

    private boolean isAuthorized;
    private File msgHistory;

    Socket socket;
    DataOutputStream out;
    DataInputStream in;

    final String IP_ADRESS = "localhost";
    final int PORT = 8189;

    public void setAuthorized(boolean isAuthorized){
        this.isAuthorized = isAuthorized;
        if(isAuthorized){
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientList.setVisible(true);
            clientList.setManaged(true);
        }else {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
        }
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{
                    try {
                        //цикл авторизации
                        while(true){
                            String str = in.readUTF();
                            if (str.startsWith("/authok")) {
                                setAuthorized(true);
                                // получим ник
                                nick = str.split(" ")[1];

                                //задаем путь к файлу
                                msgHistory = new File("src\\client\\history\\" + "history_" + nick + ".txt");
                                //чтение из файла играниченное числом строк
                                int liner = 0;
                            if (msgHistory.exists()){
                                try (FileReader fr = new FileReader(msgHistory)) {
                                    LineNumberReader lnr = new LineNumberReader(fr);
                                    while (lnr.readLine() != null) {
                                        liner++;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try (BufferedReader br = new BufferedReader(new FileReader(msgHistory))) {
                                    for (int i = 0; i < liner; i++) {
                                        String line = br.readLine();
                                        if (i >= liner - 10) {
                                            textArea.appendText(line + "\n");
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                //создаем файл если его нет
                                try {
                                    msgHistory.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                                break;
                            }
                            if (str.equals("/end")) {
                                System.out.println("Клиент отключился по бездействию");
                                throw new RuntimeException("Клиент отключился по бездействию");
                            }

                            textArea.appendText(str +"\n");

                        }

                        // добавим в заголовок nick пользователя
                        Platform.runLater(() ->{
                            getStage().setTitle("Супер чат. "+ nick);
                        } );

                        //цикл работы
                        while(true){
                            String str = in.readUTF();
                            if (str.startsWith("/")){
                                if (str.equals("/end")) {
                                    System.out.println("Клиент отключился");
                                    break;
                                }
                                if (str.startsWith("/clientlist")) {
                                    String[] tokens = str.split(" ");
                                    //
                                    Platform.runLater(()->{
                                        clientList.getItems().clear();
                                        for (int i = 1; i <tokens.length ; i++) {
                                            clientList.getItems().add(tokens[i]);
                                        }
                                    });
                                }
                            }else {

                //запись в файл

                                try (FileWriter fw = new FileWriter(msgHistory, true)) {
                                    fw.append(str + "\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                textArea.appendText(str +"\n");
                            }
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthorized(false);
                    }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMSG(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.setText("");
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if(socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/auth "+loginField.getText()+" "
                    + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickClientList(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText("/w "+receiver+" ");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public void tryToReg(ActionEvent actionEvent) {
        if(socket == null || socket.isClosed()){
            connect();
        }
        if (regController==null){
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("regsample.fxml"));
                Parent root1 = (Parent)fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);

                RegController regController = fxmlLoader.getController();
                regController.controller = this;

                this.regController = regController;

                stage.setTitle("Регистрация");
                stage.setScene(new Scene(root1));
                stage.show();

                System.out.println(root1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
            Stage stage = (Stage)regController.btnClose.getScene().getWindow();
            stage.show();
        }
    }

}
