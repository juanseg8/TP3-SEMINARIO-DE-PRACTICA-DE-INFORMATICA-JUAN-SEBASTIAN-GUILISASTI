module com.stockflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    uses java.sql.Driver;
    
    opens com.stockflow.controller to javafx.fxml;
    opens com.stockflow.model to javafx.base;
    exports com.stockflow;
}
