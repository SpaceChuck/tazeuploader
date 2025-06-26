package com.spacechuck.tazeuploader;


import javafx.fxml.FXML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.File;
import javafx.scene.control.Alert.AlertType;

public class HelloController {
    @FXML
    private ProgressBar UploadProgressBar;
    @FXML
    private ProgressBar ButlerStatusBar;
    @FXML
    private Button BrowseButton;
    @FXML
    private TextField VersionBox;
    @FXML
    private TextField UsernameBox;
    @FXML
    private TextField GameBox;
    @FXML
    private TextField GameNameBox;

    private File ButlerExec;

    @FXML
    protected void onBrowseButlerButtonClick(ActionEvent event) {
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Butler Executable");
        ButlerExec = fileChooser.showOpenDialog(stage);
        System.out.println(ButlerExec);
        if (ButlerExec == null) {ButlerStatusBar.setProgress(0);return;}
        try {
            Process proc = Runtime.getRuntime().exec(new String[] {ButlerExec.getPath()});
        } catch (Exception e) {
            Alert a = new Alert(AlertType.ERROR);
            a.setContentText(e.toString());
            a.setHeaderText("that file doesnt work idiot");
            a.setTitle("TazeUploader");
            a.show();
            ButlerExec = null;
            ButlerStatusBar.setProgress(0);
            return;
        }
        ButlerStatusBar.setProgress(1);
    }

    @FXML
    protected void onUploadButtonClick(ActionEvent event) throws IOException, InterruptedException {
        if (UsernameBox.getText().isBlank()) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("enter your username idiot");
            a.setTitle("TazeUploader");
            a.show();
            return;
        }
        if (GameBox.getText().isBlank()) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("enter the game id idiot");
            a.setTitle("TazeUploader");
            a.show();
            return;
        }
        if (GameNameBox.getText().isBlank()) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("enter the game name idiot");
            a.setTitle("TazeUploader");
            a.show();
            return;
        }
        if (VersionBox.getText().isBlank()) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("enter the version idiot");
            a.setTitle("TazeUploader");
            a.show();
            return;
        }
        if (ButlerExec == null) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("choose the butler executable idiot");
            a.setTitle("TazeUploader");
            a.show();
            return;
        }

        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select Builds");
        List<File> list = fileChooser.showOpenMultipleDialog(stage);
        if (list == null) {return;}
        System.out.println(list);
        List<String> strlist = new ArrayList<String>();
        for (File file : list) {
            strlist.add(file.getName());
        }
        Alert a1 = new Alert(AlertType.INFORMATION);
        a1.setHeaderText("Uploading builds...");
        a1.setContentText("> " + String.join(", ", strlist) + "\n");
        a1.setResizable(true);
        a1.show();

        String commandOutput = "Command Output:\n\n";
        // double ProgressBarIncrement = (double) 1 / list.size();
        for (File file : list) {
            if (GetBuildChannel(file, GameNameBox.getText(), VersionBox.getText()) == null) {
                Alert a = new Alert(AlertType.WARNING);
                a.setHeaderText("Could not detect channel for " + file.getName());
                a.show();
                continue;
            }

            String[] command = {ButlerExec.getAbsolutePath()
                    ,"push", file.getAbsolutePath()
                    ,UsernameBox.getText() + "/" + GameBox.getText() + ":" + GetBuildChannel(file, GameNameBox.getText(), VersionBox.getText())
                    ,"--userversion",VersionBox.getText()};
            System.out.println("Running command:\n");
            System.out.println(Arrays.toString(command));

            try {
                Process proc = Runtime.getRuntime().exec(command);
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(proc.getInputStream()));

                String line = "";

                commandOutput += "### Uploading " + file.getName() + "\n";
                commandOutput += "~$ " + String.join(" ", command) + "\n";
                while((line = reader.readLine()) != null) {
                    System.out.print(line + "\n");
                    commandOutput += line + "\n";
                }
            } catch (Exception e) {
                Alert a = new Alert(AlertType.ERROR);
                a.setContentText(e.toString());
                a.setHeaderText("Could not upload builds!");
                a.setTitle("TazeUploader");
                a.show();
                return;
            }
        }
        a1.hide();
        Alert a2 = new Alert(AlertType.INFORMATION);
        a2.setResizable(true);
        a2.setHeight(250);
        a2.setTitle("TazeUploader");
        a2.setHeaderText("Done.");
        a2.setContentText(commandOutput);
        a2.show();
        return;
    }
    public static String GetBuildChannel(File build, String GameName, String Version) {
        java.lang.String name = build.getName();

        // Windows
        if (name.equals(GameName + "v" + Version + "Setup.exe") || name.equals(GameName + "v" + Version + "Setup64.exe")) {
            return "windows-64-installer";
        }
        else if (name.equals(GameName + "v" + Version + "Setup32.exe")) {
            return "windows-32-installer";
        }
        else if (name.equals(GameName + "v" + Version + "SetupARM.exe") || name.equals(GameName + "v" + Version + "SetupARM64.exe")) {
            return "windows-arm-installer";
        }
        else if (name.equals(GameName + "v" + Version + "SetupARM32.exe")) {
            return "windows-arm32-installer";
        }

        else if (name.equals(GameName + "v" + Version + "Windows.zip") || name.equals(GameName + "v" + Version + "Windows64.zip")) {
            return "windows-64";
        }
        else if (name.equals(GameName + "v" + Version + "Windows32.zip")) {
            return "windows-32";
        }
        else if (name.equals(GameName + "v" + Version + "WindowsARM.zip")) {
            return "windows-arm";
        }
        else if (name.equals(GameName + "v" + Version + "WindowsARM32.zip")) {
            return "windows-arm32";
        }

        // macOS
        else if (name.equals(GameName + "v" + Version + "macOS.zip") || name.equals(GameName + "v" + Version + "macOSUniversal.zip")) {
            return "mac";
        }
        else if (name.equals(GameName + "v" + Version + "macOS64.zip")) {
            return "mac-64";
        }
        else if (name.equals(GameName + "v" + Version + "macOS32.zip")) {
            return "mac-32";
        }
        else if (name.equals(GameName + "v" + Version + "macOSPPC.zip") || name.equals(GameName + "v" + Version + "macOSPowerPC.zip")) {
            return "mac-ppc";
        }
        else if (name.equals(GameName + "v" + Version + "macOSARM.zip")) {
            return "mac-arm";
        }

        // Linux
        else if (name.equals(GameName + "v" + Version + "Linux.zip") || name == (GameName + "v" + Version + "Linux64.zip")) {
            return "linux";
        }
        else if (name.equals(GameName + "v" + Version + "Linux32.zip")) {
            return "linux-32";
        }
        else if (name.equals(GameName + "v" + Version + "LinuxPPC.zip") || name == (GameName + "v" + Version + "LinuxPowerPC.zip")) {
            return "linux-ppc";
        }
        else if (name.equals(GameName + "v" + Version + "LinuxARM.zip")) {
            return "linux-arm";
        }
        else if (name.equals(GameName + "v" + Version + "LinuxARM32.zip")) {
            return "linux-arm32";
        }


        // WebGL
        else if (name.equals(GameName + "v" + Version + "WebGL.zip") || name == (GameName + "v" + Version + "Web.zip")) {
            return "webgl";
        }

        // Android
        else if (name.equals(GameName + "v" + Version + "Android.apk")) {
            return "android";
        }

        // iOS
        else if (name.equals(GameName + "v" + Version + "iOS.ipa")) {
            return "ios";
        }

        // tvOS
        else if (name.equals(GameName + "v" + Version + "tvOS.ipa")) {
            return "tvos";
        }

        // Samsung Smart Fridge (not supported, unfortunately)
        else {return null;}
    }
}