package main;

import customExceptions.CustomException;
import customExceptions.InvalidCountException;
import customExceptions.InvalidFileParametersException;
import customExceptions.InvalidIndexException;
import domain.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.*;


public class BattleshipApp extends Application {
    private final String SCENARIO_DIRECTORY_PATH = "src/medialab/";
    Player human = new Player("Mike", false);
    Player computer = new Player("Bot_AI", true);
    private List<int[]> playerBoardParameters = new ArrayList<>();
    private List<int[]> enemyBoardParameters = new ArrayList<>();
    private boolean loadFromFile = false;
    Game game;
    Stage window;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("MediaLab Battleship");
        window.setResizable(false);
        window.setScene(new Scene(createContent(), Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE));
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Parent createContent() {
        BorderPane root = new BorderPane();

        // PLAYER BOARDS CREATION
        game = new Game(human, computer);
        human.setPlayerBoard(new Board(40, false, game.getPlacingHandler()));
        computer.setPlayerBoard(new Board(40, true, game.getShootingHandler()));
        // If placing is loaded from file place ships and start game
        if (loadFromFile) {
            for (int[] values : playerBoardParameters) {
                System.out.println(values[0] + "-" + values[2]+ "-" +  values[1]+ "-" + values[3]);
                try {
                    human.getPlayerBoard().validateAndPlaceShip(new Ship(values[0], human.getPlayerBoard()),
                            values[2], values[1], values[3] == 2, true);
                } catch (CustomException e) {
                    e.printStackTrace();
                }
            }
            for (int[] values : enemyBoardParameters) {
                System.out.println(values[0] + "," + values[1]+ "," + values[2]+ "," + values[3]);
                try {
                    computer.getPlayerBoard().validateAndPlaceShip(new Ship(values[0], computer.getPlayerBoard()),
                            values[2], values[1], values[3] == 2, true);
                } catch (CustomException e) {
                    e.printStackTrace();
                }
            }
            game.start();
        }
        HBox playerBoards = new HBox();
        playerBoards.setSpacing(10.0);
        Separator verticalBar = new Separator();
        verticalBar.setOrientation(Orientation.VERTICAL);
        verticalBar.setStyle("-fx-border-width: 5px;");
        playerBoards.getChildren().addAll(human.getPlayerBoard(), verticalBar, computer.getPlayerBoard());
        playerBoards.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
        playerBoards.setAlignment(Pos.CENTER);

        // MENU BAR CREATION
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: silver;");
        MenuItem start = new MenuItem("Start");
        MenuItem load = new MenuItem("Load");
        MenuItem exit = new MenuItem("Exit");
        start.setOnAction(event -> startSelected());
        load.setOnAction(event -> loadSelected());
        exit.setOnAction(event -> exitGame());
        Menu application = new Menu("_Application");
        application.getItems().addAll(start, load, new SeparatorMenuItem(), exit);
        MenuItem enemyShots = new MenuItem("Enemy Shots");
        MenuItem playerShots = new MenuItem("Player Shots");
        MenuItem enemyShips = new MenuItem("Enemy Ships");
        enemyShots.setOnAction(event -> showPopup(enemyShots.getText()));
        playerShots.setOnAction(event -> showPopup(playerShots.getText()));
        enemyShips.setOnAction(event -> showPopup(enemyShips.getText()));
        Menu details = new Menu("_Details");
        details.getItems().addAll(enemyShots, playerShots, enemyShips);
        menuBar.getMenus().addAll(application, details);

        Label computerStats = new Label("Computer Statistics:");
        Label playerStats = new Label("Player Statistics:");
        Label remainingShips1 = new Label("Ships Remaining:");
        Label computerShips = new Label("5");
        Label remainingShips2 = new Label("Ships Remaining:");
        Label playerShips = new Label("5");
        computerShips.textProperty().bind(computer.getPlayerBoard().shipsRemainingProperty().asString());
//        playerShips.textProperty().bind(human.getPlayerBoard().shipsRemainingProperty().asString());
        Label points1 = new Label("Total Points:");
        Label computerPoints = new Label("0");
        Label points2 = new Label("Total Points:");
        Label playerPoints = new Label("0");
//        computerPoints.textProperty().bind(computer.pointsProperty().asString());
        playerPoints.textProperty().bind(human.pointsProperty().asString());
        Label accuracy1 = new Label("Shooting Accuracy:");
        Label computerAccuracy = new Label("0.0");
        Label accuracy2 = new Label("Shooting Accuracy:");
        Label playerAccuracy = new Label("0.0");
//        computerAccuracy.textProperty().bind(computer.accuracyProperty().asString());
        playerAccuracy.textProperty().bind(human.accuracyProperty().asString());
        Label notification = new Label();
        notification.textProperty().bind(game.notificationProperty());
        HBox computerStatsBar = new HBox();
        HBox playerStatsBar = new HBox();
        HBox notificationBar = new HBox();
        computerStatsBar.getChildren().addAll(computerStats, remainingShips1, computerShips,
                points1, computerPoints, accuracy1, computerAccuracy);
        playerStatsBar.getChildren().addAll(playerStats, remainingShips2, playerShips,
                points2, playerPoints, accuracy2, playerAccuracy);
        notificationBar.getChildren().add(notification);
        computerStatsBar.setSpacing(10.0);
        playerStatsBar.setSpacing(10.0);
        computerStatsBar.setPadding(new Insets(0, 0, 0, 10));
        playerStatsBar.setPadding(new Insets(0, 0, 0, 10));
        notificationBar.setPadding(new Insets(0, 0, 0, 10));
        computerStatsBar.setStyle("-fx-background-color: palevioletred ;");
        playerStatsBar.setStyle("-fx-background-color: gold;");
        notificationBar.setStyle("-fx-background-color: lightgreen;");
        notificationBar.setAlignment(Pos.CENTER);
        notification.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        VBox layoutTop = new VBox();
        layoutTop.getChildren().addAll(menuBar, computerStatsBar, playerStatsBar, notificationBar);

        // COMMAND INPUT LINE CREATION
        Label label = new Label("Choose a point to shoot (0-99):");
        label.setAlignment(Pos.CENTER);
        label.paddingProperty().setValue(new Insets(5, 0, 0, 0));
        TextField userInput = new TextField();
        userInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (newValue.length() == 2) {
                    userInput.setEditable(false);
                }
                if (!newValue.matches("\\d{2}")) {
                    userInput.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        Button shootButton = new Button("Shoot!");
        shootButton.disableProperty().bind(userInput.textProperty().isEmpty());
        shootButton.setOnAction(event -> shootEvent(userInput));
        root.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER) && !userInput.getText().isEmpty()) {
                shootEvent(userInput);
                userInput.setEditable(true);
                userInput.setText("");
            }
        });
        HBox bottomBar = new HBox();
        bottomBar.setSpacing(20.0);
        bottomBar.setPadding(new Insets(0, 0, 0, 10));
        bottomBar.getChildren().addAll(label, userInput, shootButton);
        bottomBar.setStyle("-fx-background-color: gray;");

        root.setTop(layoutTop);
        root.setCenter(playerBoards);
        root.setBottom(bottomBar);
        return root;
    }

    private void showPopup(String title) {
        List<String> info = new ArrayList<>();
        if (title == "Enemy Shots") {
            List<Move> moves = computer.getMoves();
            if (!moves.isEmpty()) {
                ListIterator<Move> iterator = moves.listIterator(moves.size());
                int i = 5;
                while (iterator.hasPrevious() && i > 0) {
                    info.add("Move #" + (iterator.previousIndex() + 1) + ". " + iterator.previous().toString());
                    i--;
                }
            }
        }
        if (title == "Player Shots") {
            List<Move> moves = human.getMoves();
            if (!moves.isEmpty()) {
                ListIterator<Move> iterator = moves.listIterator(moves.size());
                int i = 5;
                while (iterator.hasPrevious() && i > 0) {
                    info.add("Move #" + (iterator.previousIndex() + 1) + ". " + iterator.previous().toString());
                    i--;
                }
            }
        }
        if (title == "Enemy Ships") {
            List<Ship> ships = computer.getPlayerBoard().getShips();
            for (Ship s : ships) {
                info.add(s.getType() + ": State:" + s.getState());
            }
        }
        PopupWindow.display(title + ':', info);
    }

    private void shootEvent(TextField input) {
        if (game.getState() != Game.GameState.STARTED || !human.isTurnToMove()) return;
        if (human.getMoves().size() == game.MAXIMUM_MOVES){
            System.out.println("Game has finished after " + game.MAXIMUM_MOVES + " moves!");
            game.finish();
        }
        int x, y;
        if (input.getText().length() == 1) {
            x = Integer.parseInt(input.getText().substring(0, 1));
            y = 0;
        } else {
            x = Integer.parseInt(input.getText().substring(1, 2));
            y = Integer.parseInt(input.getText().substring(0, 1));
        }
        Tile tile = computer.getPlayerBoard().getTile(x, y);

        System.out.println("Player shoots: " + tile.toString());
        if (tile.isHit()) return;
        human.addMoveToList(Move.create(human, tile, tile.shootTile()));
        if (computer.getPlayerBoard().getShipsRemaining() < game.computerShipsRemaining) {

        }
        if (computer.getPlayerBoard().getShipsRemaining() == 0) {
            System.out.println("YOU WIN");
            game.finish();
        }
        human.setTurnToMove(false);
        computer.setTurnToMove(true);
        new Timer().schedule(game.botShooting(),500);
    }

    private void exitGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setContentText("Are you sure you want to exit the application?");
        ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType cancelButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);
        alert.showAndWait().ifPresent(response -> {
            if (response == okButton)
                Platform.exit();
        });
    }

    private void loadSelected() {
        File dir = new File(SCENARIO_DIRECTORY_PATH);
        FilenameFilter filter = (dir1, name) -> {
            String lname = name.toLowerCase(Locale.ROOT);
            return lname.endsWith(".txt") &&
                    (lname.startsWith("enemy_") ||
                            lname.startsWith("player_"));
        };
        List<String> filelist = Arrays.asList((Objects.requireNonNull(dir.list(filter))));
        HashSet<String> scenarioID = new HashSet<>();
        for (String f : filelist) {
            int dotIndex = f.lastIndexOf('.');
            if (f.startsWith("enemy_"))
                scenarioID.add(f.substring(6, dotIndex));
            else scenarioID.add(f.substring(7, dotIndex));
        }
        List<String> scenarioList = new ArrayList<>(scenarioID);
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.getItems().addAll(scenarioList);
        if (!scenarioList.isEmpty())
            dialog.setSelectedItem(scenarioList.get(0));
        dialog.setTitle("Choose Scenario");
        dialog.setContentText("Choose a scenario ID from the list:");
        Optional<String> scenarioChoose = dialog.showAndWait();
        try {
            importScenario(1, scenarioChoose.get());
            importScenario(0, scenarioChoose.get());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidFileParametersException e) {
            e.printStackTrace();
        } catch (CustomException e) {
            e.printStackTrace();
        }
        loadFromFile = true;
        window.setScene(new Scene(createContent(), Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE));
    }

    private void startSelected() {
        try {
            importScenario(1, "default");
            importScenario(0, "default");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidFileParametersException e) {
            e.printStackTrace();
        } catch (CustomException e) {
            e.printStackTrace();
        }
        loadFromFile = true;
        window.setScene(new Scene(createContent(), Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE));
    }

    private void importScenario(int playerID, String scenario_ID) throws FileNotFoundException, InvalidFileParametersException, CustomException {
        String player = playerID == 1 ? "player" : "enemy";
        File file = new File(SCENARIO_DIRECTORY_PATH + player + "_" + scenario_ID + ".txt");
        System.out.println("Loading file:" + file);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            String input = scanner.nextLine();
            String[] numbers = input.split(",");

            if (numbers.length != 4) throw new InvalidFileParametersException("Exactly 4 Parameters required");
            int[] values = new int[4];
            for (int i = 0; i < 4; i++) {
                values[i] = Integer.parseInt(numbers[i]);
            }
            if (values[0] > 5 || values[0] < 1) throw new InvalidIndexException("Ship type allowed: 1 - 5");
            if (values[1] > 9 || values[1] < 0 || values[2] > 9 || values[2] < 0)
                throw new InvalidFileParametersException("Invalid coordinates! Must be between 0 and 9");
            if (values[3] != 1 && values[3] != 2)
                throw new InvalidFileParametersException("Invalid direction: 1 -> horizontal, 2 -> vertical");
            if (playerID == 1) {
                playerBoardParameters.add(values);
            } else {
                enemyBoardParameters.add(values);
            }
            System.out.println(values[0] + "," + values[1]+ "," + values[2]+ "," + values[3]);
        }
        if (playerBoardParameters.size() > 5 || enemyBoardParameters.size() > 5)
            throw new InvalidFileParametersException("No more than 5 ships allowed, one per type");
        Set<Integer> check = new HashSet<>();
        List<int[]> list = playerID == 1 ? playerBoardParameters : enemyBoardParameters;
        for (int[] v : list) {
            check.add(v[0]);
        }
        if(check.size() < list.size()) throw new InvalidCountException("One ship per type allowed!!");
        scanner.close();
    }


}

