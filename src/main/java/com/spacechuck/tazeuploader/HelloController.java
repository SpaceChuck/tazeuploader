package com.spacechuck.tazeuploader;


import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.File;
import javafx.scene.control.Alert.AlertType;

import static java.util.Map.entry;

public class HelloController {
    // @FXML
    //private ProgressBar UploadProgressBar;
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

    private static Map<String,String> Platforms = Map.ofEntries(
            entry("setup.exe", "windows-64-installer"),
            entry("setup64.exe", "windows-64-installer"),
            entry("setup32.exe", "windows-32-installer"),
            entry("setuparm.exe", "windows-arm-installer"),
            entry("setuparm32.exe", "windows-arm32-installer"),

            entry("windows.zip", "windows-64"),
            entry("windows64.zip", "windows-64"),
            entry("windows32.zip", "windows-32"),
            entry("windowsarm.zip", "windows-arm"),
            entry("windowsarm32.zip", "windows-arm32"),

            entry("macos.zip", "mac-universal"),
            entry("mac.zip", "mac-universal"),
            entry("macos64.zip", "mac-64"),
            entry("mac64.zip", "mac-64"),
            entry("macos32.zip", "mac-32"),
            entry("mac32.zip", "mac-32"),
            entry("macosarm.zip", "mac-arm"),
            entry("macarm.zip", "mac-arm"),
            entry("macosppc.zip", "mac-ppc"),
            entry("macppc.zip", "mac-ppc"),
            entry("macospowerpc.zip", "mac-ppc"),
            entry("macpowerpc.zip", "mac-ppc"),

            entry("linux.zip", "linux-64"),
            entry("linux64.zip", "linux-64"),
            entry("linux32.zip", "linux-32"),
            entry("linuxarm.zip", "linux-arm"),
            entry("linuxarm32.zip", "linux-arm32"),
            entry("linuxppc.zip", "linux-ppc"),
            entry("linuxpowerpc.zip", "linux-ppc"),

            entry("web.zip", "web"),
            entry("webgl.zip", "web"),

            entry("android.apk", "android"),

            entry("ios.ipa", "ios"),

            entry("tvos.ipa", "tvos")

    );
    private File ButlerExec;
    private Boolean ButlerInPath = false;


    @FXML
    protected void initialize() {
        try {
            Process proc = Runtime.getRuntime().exec("butler");
            ButlerInPath = true;
            System.out.println("Butler detected in PATH!");
            ButlerStatusBar.setProgress(1);
        } catch (IOException e) {
            ButlerExec = null;
            return;
        }
    }
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
        if (VersionBox.getText().isBlank()) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("enter the version idiot");
            a.setTitle("TazeUploader");
            a.show();
            return;
        }
        if (ButlerExec == null && !ButlerInPath) {
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
            String butlerPath;
            if (ButlerInPath) {
                butlerPath = "butler";
            } else {
                butlerPath = ButlerExec.getAbsolutePath();
            }
            String[] command = {butlerPath
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


        for (Map.Entry<String,String> i: Platforms.entrySet()) {
            System.out.println(name);
            System.out.println(name.substring(name.length()-i.getKey().length()).toLowerCase());
            System.out.println(i.getKey());
            if (name.substring(name.length()-i.getKey().length()).toLowerCase().equals((i.getKey()))) {
                return i.getValue();
            }
        }
        return null;
    }
}