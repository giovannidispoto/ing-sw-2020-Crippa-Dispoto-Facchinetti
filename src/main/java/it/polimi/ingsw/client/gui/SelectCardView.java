package it.polimi.ingsw.client.gui;

import it.polimi.ingsw.client.controller.ExceptionMessages;
import it.polimi.ingsw.client.controller.GameState;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * SelectCard view of the game
 */
public class SelectCardView extends Scene {
    private Map<String,Boolean> map;

    /**
     * Create view
     * @param root parent
     * @param builder GUIBuilder
     * @param god true if is view of the god player, false otherwise
     * @throws IOException IOException
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    public SelectCardView(Parent root, GUIBuilder builder, boolean god) throws IOException, ExecutionException, InterruptedException {
        super(root);
        Task<Void> wait = null;
        Task<Void> wait1 = null;
        Task<Void> wait2 = null;
        Thread t = null;
        Button selectButton = ((Button) root.lookup("#selectButton"));
        Button infoButton = ((Button) root.lookup("#infoButton"));
        //default disabled button
        selectButton.setDisable(true);


        ExecutorService executor = Executors.newFixedThreadPool(1);

        /* Interrupt exception handler */
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    try{
                        wait();
                    }catch( InterruptedException e){
                        //If the user if winner show relative view
                       if(GUIController.getController().getGameState().equals(GameState.ERROR)) {
                           Platform.runLater(() -> builder.showErrorPicker());
                           executor.shutdownNow();
                           Thread.currentThread().interrupt();
                       }
                    }
                }
            }
        });
        /* Register thread to controller for error handling*/
        GUIController.getController().registerControllerThread(t);

        t.start();

        /* If player is god player start picking card  */
        if(god){
            ObservableList<String> deck =  FXCollections.emptyObservableList();

            map = new HashMap<>();

            ListView<String> listView = ((ListView<String>) root.lookup("#listView"));

            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                    if (map.values().stream().filter(e -> e == true).count() < GUIController.getController().getCurrentLobbySize()) {
                        map.put(t1, true);
                    }
                    if(map.values().stream().filter(e -> e == true).count() ==  GUIController.getController().getCurrentLobbySize()){
                        selectButton.setDisable(false);
                    }
                    listView.refresh();
                }
            });


            selectButton.setOnMouseClicked(
                    e->{
                        List<String> cards = new LinkedList<>();
                        for(String card : map.keySet()){
                            if(map.get(card))
                                cards.add(card);
                        }
                        GUIController.getController().setPickedCardsRequest(cards);
                        builder.changeView(Optional.empty());
                    });

            wait = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    GUIController.getController().getDeckRequest();
                    return null;
                }
            };

            wait.setOnSucceeded(s->{
                /* Show cards inside ListView*/
                listView.setItems(FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(GUIController.getController().getCardsDeck().getAllCards().stream().map(card->card.getCardName()).collect(Collectors.toList()))));
                for (int i = 0; i < listView.getItems().size(); i++)
                    map.put(listView.getItems().get(i), false);
                listView.setCellFactory(param -> new BuildCell());
            });

            executor.submit(wait);
        }else{
            map = new HashMap<>();
            // showWait();
            ListView<String> listView = ((ListView<String>) root.lookup("#listView"));
            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                    if (map.values().stream().filter(e -> e == true).count() == 0) {
                        map.put(t1, true);
                        selectButton.setDisable(false);
                    }
                    //refresh list
                    listView.refresh();
                }
            });

            Thread finalT = t;
            selectButton.setOnMouseClicked(
                    e->{
                        List<String> cards = new LinkedList<>();
                        for(String card : map.keySet()){
                            if(map.get(card))
                                cards.add(card);
                        }
                        GUIController.getController().setPlayerCardRequest(cards.get(0));
                        builder.changeView(Optional.empty());
                        finalT.interrupt();
                        //t2.start();
                    });

            wait1 = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // showWait();
                    GUIController.getController().waitSetPlayerCard();
                    return null;
                }
            };

            wait1.setOnSucceeded(e->{
                // hideWait();
                root.lookup("#blurResult").setVisible(false);
                hideWait();
                listView.setItems(FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(GUIController.getController().getGodCards())));
                for (int i = 0; i < listView.getItems().size(); i++)
                    map.put(listView.getItems().get(i), false);
                listView.setCellFactory(param -> new BuildCell());
            });


            executor.submit(wait1);
            root.lookup("#blurResult").setVisible(true);
            showWait();
        }

        /* On CLick on info, show cards*/
        infoButton.setOnMouseClicked(event->{
            try {
                Parent view = FXMLLoader.load(getClass().getResource("/GodInformationView.fxml"));
                Scene scene = new CardsInfoView(view);

                Stage newWindow = new Stage();
                newWindow.setScene(scene);
                newWindow.setResizable(false);
                newWindow.show();

            } catch (IOException e) {
                e.printStackTrace();
            }

        });

    }

    /**
     * Hide wait message
     */
    public void hideWait() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/WaitMessage.fxml"));
            Platform.runLater (()->((StackPane)  lookup("#resultPane")).getChildren().remove(view));
            Platform.runLater (()-> ((StackPane) lookup("#resultPane")).setVisible(false));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show wait message
     */
    public void showWait() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/WaitMessage.fxml"));
            Platform.runLater (()-> ((StackPane) lookup("#resultPane")).getChildren().add(view));
            Platform.runLater (()-> ((StackPane) lookup("#resultPane")).setVisible(true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* Create custom ListView for showing cards*/

    private class BuildCell extends ListCell<String> {
        private ImageView imageView = new ImageView();
        private Parent root;

        protected void update(String item){
            super.updateItem(item, false);
            try {
                root = FXMLLoader.load(getClass().getResource("/CardTemplate.fxml"));
                ((ImageView) root.lookup("#cardImage")).setImage(new Image(getClass().getResource("/Images/Cards/"+item+".png").toString()));

            } catch (IOException e) {
                e.printStackTrace();
            }
            setGraphic(root);
        }
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                imageView.setImage(null);

                setGraphic(null);
                setText(null);
            } else {
                try {
                    root = FXMLLoader.load(getClass().getResource("/CardTemplate.fxml"));
                    ((ImageView) root.lookup("#cardImage")).setImage(new Image(getClass().getResource("/Images/Cards/"+item+".png").toString()));

                    if(map.get(item) == true)
                        ((ImageView) root.lookup("#playerPawn")).setImage(new Image(getClass().getResource("/Images/Cards/SelectedCard.png").toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                setGraphic(root);

            }
        }

    }
}
