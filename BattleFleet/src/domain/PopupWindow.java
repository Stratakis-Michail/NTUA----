package domain;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.util.List;

public class PopupWindow {
    public static void display(String title, List<String> info) {
        Alert popup = new Alert(Alert.AlertType.INFORMATION);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Information Window");
        if(title != null)
            popup.setHeaderText(title);
        if(!info.isEmpty())
            popup.setContentText(infoToString(info));
        else
            popup.setContentText("No Information Available");
        popup.showAndWait();
    }

    private static String infoToString(List<String> info) {
        String infoToList = "";
        for (String s : info) {
            infoToList += "--" + s + '\n';
        }
        return infoToList;
    }
}
