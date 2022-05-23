module com.os.filemanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.os.filemanager to javafx.fxml, javafx.base;
    exports com.os.filemanager;

}