package com.example.checkersguifinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;


public class Main extends Application {
    final int BOARD_SIZE =
            800;
    final int SQUARE_SIZE = BOARD_SIZE / 8;
    final int PIECE_SIZE = (95 * SQUARE_SIZE) / 100;

    private Stage primaryStage;

    private Board board;
    private Controller controller;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            this.primaryStage = stage;

            this.controller = new Controller();

            // Pass the main application to the controller
            // Seems blind to jumps to becoming a king right now.
            Parent root = FXMLLoader.load((Objects.requireNonNull(Main.class.getResource("board.fxml"))));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}