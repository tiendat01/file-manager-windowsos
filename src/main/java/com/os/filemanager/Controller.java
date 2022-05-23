package com.os.filemanager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public TreeView<File> treeView;
    public TreeItem<File> root = new TreeItem<File>();
    private File file;
    @FXML
    private TextField inputArea;
    @FXML
    private TextArea Info;

    public Controller() {};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.treeView.setCellFactory(new FileCellFactory());

        this.treeView.setEditable(true);
        this.treeView.setCellFactory(new Callback <TreeView<File>, TreeCell<File>>() {
            @Override
            public TreeCell<File> call(TreeView<File> p) {
                return new FileCell(); // lop ke thua TreeCell<File>
            }
        });
    }



    @FXML
    private void info(ActionEvent event) {
        System.out.println("You clicked properties Button!");
        TreeItem<File> selectedItem = treeView.getSelectionModel().getSelectedItem();
        Info.setText(fileInfo(selectedItem.getValue()));
    }

    private String fileInfo(File f) {
        try {
            String name = f.getName();
            return  "Name: " + name + "\n" +
                    "Absolute path: " + f.getAbsolutePath() + "\n" +
                    "Relative path: " + f.getPath() + "\n" +
                    "Type: " + (f.isDirectory() ? "Folder File" : name.substring(name.indexOf('.')) +  " File") + "\n" +
                    "Size: " + Files.size(f.toPath()) / 1024 + "KB" + "\n" +
                    "Created: " + time(Files.readAttributes(f.toPath(), BasicFileAttributes.class).creationTime().toMillis()) + "\n" +
                    "Last Modified: " + time(f.lastModified());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "Time created error!";
    }

    private String time(long l) {
        Instant instant = Instant.ofEpochMilli(l);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd. MMMM yyyy. HH:mm:ss");
        return dateTime.format(dateTimeFormatter);
    }




    @FXML
    void move(ActionEvent event) {
        System.out.println("You clicked move Button!");
        TreeItem<File> selectedItem = treeView.getSelectionModel().getSelectedItem();
        DirectoryChooser targetDr = new DirectoryChooser();
        targetDr.setTitle("Select Target Folder");
        File source = selectedItem.getValue();
        File target = targetDr.showDialog((Window)null);

        if (source.isDirectory())
            try {
                File targetCopyName = new File(target, source.getName());
                Files.copy(source.toPath(), targetCopyName.toPath());
                copyDirToDir(source, targetCopyName);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        else
            try {
                copyFileToDir(source, target);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        // delete file cu
        if (source.isDirectory()) {
            removeInside(selectedItem);
        } else {
            source.delete();
        }

    }



    @FXML
    void copy(ActionEvent event) {
        System.out.println("You clicked copy Button!");
        TreeItem<File> selectedItem = treeView.getSelectionModel().getSelectedItem();
        DirectoryChooser targetDr = new DirectoryChooser();
        targetDr.setTitle("Select Target Folder");
        File source = selectedItem.getValue();
        File target = targetDr.showDialog((Window)null);

        if (source.isDirectory())
            try {
                File targetCopyName = new File(target, source.getName());
                Files.copy(source.toPath(), targetCopyName.toPath());
                copyDirToDir(source, targetCopyName);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        else
            try {
                copyFileToDir(source, target);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
    private void copyDirToDir(File source, File target) throws IOException {
        // hai dong nay la thua vi nguoi dung chon file theo DirectoryChooser nen chac chan ton tai
        // neu target ko ton tai
        if (!target.exists()) target.mkdir();
        // neu source ko ton tai
        if (!source.exists()) throw new IllegalArgumentException("sourceDir does not exist");

        // neu 2 file dua vao ko la thu muc folder
        if (source.isFile() || target.isFile()) throw new IllegalArgumentException("Either source or target is not a dir");


        File[] items = source.listFiles();
        for (File item : items) {
            if (item.isDirectory()) {
                File newDir = new File(target, item.getName());
                System.out.println("CREATED DIR: " + newDir.getAbsolutePath());
                newDir.mkdir();
                copyDirToDir(item, newDir);
            }
            else {
                copyFileToDir(item, target);
            }
        }
    }
    private void copyFileToDir(File source, File target) throws IOException {
        File desFile = new File(target, source.getName());
        Files.copy(source.toPath(), desFile.toPath());
    }





    @FXML
    private void refresh(ActionEvent event) {
        System.out.println("You clicked refresh Button!");
        try {
            if (this.file.exists()) {
                this.root = readChild(this.file);
                this.treeView.setRoot(this.root);
            }
        } catch (NullPointerException e) {
            AlertFunction("Warning!", "You haven't choose any file?", "Choose directory first and try again!");
        }

    }

    @FXML
    private void load(ActionEvent event) {
        System.out.println("You clicked load Button!");
        DirectoryChooser DC = new DirectoryChooser();
        this.file = DC.showDialog((Window)null);

        if(this.file != null) {
            this.root = readChild(this.file);
            this.treeView.setRoot(this.root);
            // this.readChild(this.root, this.file);
        } else {
            this.treeView.setRoot(null);
        }

    }

    //    private void readChild(TreeItem<File> root, File file) {
//        root.setValue(file);
//        root.getChildren().clear();
//        File[] children = file.listFiles();
//        int indexer = 0;
//
//        for(int i =  0; i < children.length; i++) {
//            File child = children[i];
//
//            if(child.isDirectory()) {
//
//                root.getChildren().add(new TreeItem<File>(child));
//                this.readChild((TreeItem<File>)root.getChildren().get(indexer), child);
//
//            } else {
//                root.getChildren().add(new TreeItem<File>(child));
//            }
//
//            indexer++;
//        }
//
//    }
// goi phuong thuc de quy duyet cay
// https://docs.oracle.com/javafx/2/api/index.html
    private TreeItem<File> readChild(File f) {
        return new TreeItem<File>(f) {
            private boolean isLeaf = false;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            // override cac phuong thuc trong TreeItem Class
            @Override
            public ObservableList<TreeItem<File>> getChildren() { // lay cac node con cho directory f
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() { // chech xem f co la file
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    File f = (File)getValue();
                    isLeaf = f.isFile();
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
                File f = TreeItem.getValue();
                if (f == null) {
                    return FXCollections.emptyObservableList();
                }
                if (f.isFile()) {
                    return FXCollections.emptyObservableList();
                }
                File[] files = f.listFiles();
                if (files != null) {
                    ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
                    for (File childFile : files) {
                        children.add(readChild(childFile));
                    }
                    return children;
                }
                return FXCollections.emptyObservableList();
            }
        };
    }




    @FXML
    private void delete(ActionEvent event) {
        File delFile;
        System.out.println("You clicked delete Button!");
        TreeItem<File> selectedItem = treeView.getSelectionModel().getSelectedItem();

        Boolean answer = ConfirmBox.display("CONFIRM!!!","DO YOU WANT TO DELETE PERMANENTLY THIS FILE/FOLDER???");
        if (answer) {
            try {
                //  xoa file
                if (selectedItem.getChildren().isEmpty()) {
                    delFile = new File(selectedItem.getValue().getPath());
                    delFile.delete();
                    selectedItem.getParent().getChildren().remove(selectedItem);
                } else {
                    removeInside(selectedItem); // xoa folder
                }
            }catch(NullPointerException e){
                AlertFunction("Warning!","You haven't chosen a file to delete.", "Choose file and try again.");
                System.out.println(e);
            }
        }
    }
    private void removeInside(TreeItem<File> selectedItem) {
        try {
            // neu folder co chua cac thanh phan con ben trong
            // dung de quy
            if (!selectedItem.getChildren().isEmpty()) {
                int indexer = 0;
                File[] children = selectedItem.getValue().listFiles();
                for (File child : children)
                    removeInside(selectedItem.getChildren().get(indexer));
                indexer++;
            }

            // neu folder rong
            // phan neo de quy
            if (selectedItem.getChildren().isEmpty()) {
                File delFile = new File(selectedItem.getValue().getPath());
                delFile.delete();
                selectedItem.getParent().getChildren().remove(selectedItem);
            }
        }catch(NullPointerException e){
            System.out.println(e);
        }
    }



    public void AlertFunction(String title, String headerText, String contentText){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();

    }

    public void addFile(ActionEvent actionEvent) throws IOException {

        System.out.println("You Clicked Add File Button");
        TreeItem<File> selectedDirectory = treeView.getSelectionModel().getSelectedItem();
        File Dir = selectedDirectory.getValue();
        String fileName;
        try {
            fileName = inputArea.textProperty().get();
            if (!fileName.isEmpty()) {
                File nFile = new File(Dir, fileName);

                Files.createFile(Paths.get(Dir.toString(), fileName));
                TreeItem<File> newTreeFile = new TreeItem<File>(nFile);
                selectedDirectory.getChildren().add(newTreeFile);

            } else {
                AlertFunction("Warning!", "You haven't inserted a file name!","Insert a file name a try again.");
                System.out.println("You didn't input a name for a new file");
                return;

            }
        } catch(NullPointerException e){
            AlertFunction("Warning!", "You haven't chosen a directory!!","Choose a catalog and try again.");
            System.out.println(e);
        } catch(FileAlreadyExistsException e) {
            AlertFunction("Warning!", "This file name has been already exists!", "Enter another name and try again!");
            System.out.println(e);
        }
    }


    public void addDir(ActionEvent actionEvent) throws IOException {

        System.out.println("You clicked Add Directory Button");

        TreeItem<File> selectedDirectory = treeView.getSelectionModel().getSelectedItem();
        File Dir = selectedDirectory.getValue();
        String fileName;
        try {
            fileName = inputArea.textProperty().get();
            if (!fileName.isEmpty()) {
                File nFile = new File(Dir, fileName);

                Files.createDirectory(Paths.get(Dir.toString(), fileName));
                TreeItem<File> newTreeFile = new TreeItem<>(nFile);
                selectedDirectory.getChildren().add(newTreeFile);

            } else{
                AlertFunction("Warning", "You haven't inserted a catalog file name!", "Enter a name and try again.");
                System.out.println("You didn't input a name for a new directory");
                return;
            }

        } catch(NullPointerException e){
            AlertFunction("Warning!", "You haven't chosen a directory!!","Choose a catalog and try again.");
            System.out.println(e);
        } catch(FileAlreadyExistsException e) {
            AlertFunction("Warning!", "This folder name has been already exists!", "Enter another name and try again!");
            System.out.println(e);
        }
    }


    // nested class
    private class FileCell extends TreeCell<File> {
        private FileCell() {
        }

        private TextField textField;
        private Path editingPath;

        @Override
        public void startEdit() {
            super.startEdit();

            createTextField();

            setText(null);
            setGraphic(textField); // hien thi cho nguoi dung thay
            textField.selectAll();
            if (getItem() != null) {
                editingPath = getItem().toPath();
            } else {
                editingPath = null;
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText((String) getItem().getName());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void commitEdit(File f) {
            if (editingPath != null) {
                try {
                    Files.move(editingPath, Paths.get(f.toString()));
                } catch (IOException e) {
                    AlertFunction("Warning!", "This file/folder name has been already existed!", "Enter another name and try again!!");
                    cancelEdit();
                }
            }
            super.commitEdit(f);
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(new File(editingPath.getParent().toString(), textField.getText()));
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });

        }

        private String getString() {
            return getItem() == null ? "" : getItem().getName();

        }

        protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);

//            if (empty) {
//                setText(null);
//                setGraphic(null);
//            } else {
//                if (isEditing()) {
//                    if (textField != null) {
//                        textField.setText(getString());
//                    }
//                    setText(null);
//                    setGraphic(textField);
//                } else {
//                    setText(getString());
//                    setGraphic(getTreeItem().getGraphic());
//                }
//            }

            if(file != null) {
                // root : C:\, D:\, E:\
                if (file.toPath().getNameCount() == 0) {
                    this.setText(file.getAbsolutePath());
                } else if(file.isDirectory() && file.toPath().getNameCount() != 0) { // folder
                    this.setText(file.getName());
                } else if(file.isFile() && file.toPath().getNameCount() != 0) { // file
                    this.setText(file.getName() + " (" + file.length() / 1024 + "KB)");
                }
            } else {
                this.setText((String)null);
                this.setGraphic(null);
            }

        }
    }

    class FileCellFactory implements Callback<TreeView<File>, TreeCell<File>> {
        FileCellFactory() {
        }

        public FileCell call(TreeView<File> p) {
            return Controller.this.new FileCell();
        }
    }
}
