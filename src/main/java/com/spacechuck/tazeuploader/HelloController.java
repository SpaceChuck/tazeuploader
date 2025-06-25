package com.spacechuck.tazeuploader;


import javafx.fxml.FXML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

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
        ButlerStatusBar.setProgress(1);
    }

    @FXML
    protected void onUploadButtonClick(ActionEvent event) throws IOException, InterruptedException {
        if (ButlerExec == null) {
            Alert a = new Alert(AlertType.ERROR);
            a.setContentText("choose the butler executable idiot");
            a.show();
            return;
        }
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select Builds");
        List<File> list = fileChooser.showOpenMultipleDialog(stage);
        System.out.println(list);

        // double ProgressBarIncrement = (double) 1 / list.size();
        for (File file : list) {
            if (GetBuildChannel(file, GameNameBox.getText(), VersionBox.getText()) == null) {
                Alert a = new Alert(AlertType.WARNING);
                a.setContentText("Could not detect channel for " + file.getName());
                a.show();
                continue;
            }

            String[] command = {ButlerExec.getAbsolutePath()
                    ,"push", file.getAbsolutePath()
                    ,UsernameBox.getText() + "/" + GameBox.getText() + ":" + GetBuildChannel(file, GameNameBox.getText(), VersionBox.getText())
                    ,"--userversion",VersionBox.getText()};
            System.out.println("Running command:\n");
            System.out.println(Arrays.toString(command));

            Process proc = Runtime.getRuntime().exec(command);

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = "";
            while((line = reader.readLine()) != null) {
                System.out.print(line + "\n");
            }
            proc.waitFor();
        }
        Alert a = new Alert(AlertType.INFORMATION);
        a.setContentText("Done.");
        a.show();
        return;
    }
    public static String GetBuildChannel(File build, String GameName, String Version) {
        java.lang.String name = build.getName();

        // Windows
        if (name.equals(GameName + "v" + Version + "Setup.exe") || name == (GameName + "v" + Version + "Setup64.exe")) {
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

        else if (name.equals(GameName + "v" + Version + "Windows.zip") || name == (GameName + "v" + Version + "Windows64.zip")) {
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
        else if (name.equals(GameName + "v" + Version + "macOS.zip") || name == (GameName + "v" + Version + "macOSUniversal.zip")) {
            return "mac";
        }
        else if (name.equals(GameName + "v" + Version + "macOS64.zip")) {
            return "mac-64";
        }
        else if (name.equals(GameName + "v" + Version + "macOS32.zip")) {
            return "mac-32";
        }
        else if (name.equals(GameName + "v" + Version + "macOSPPC.zip") || name == (GameName + "v" + Version + "macOSPowerPC.zip")) {
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