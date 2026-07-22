module financeiro {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.xerial.sqlitejdbc;
    requires java.sql;
    requires java.naming;
    requires jbcrypt;
    requires com.github.librepdf.openpdf;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens controller to javafx.fxml;
    opens model to org.hibernate.orm.core, javafx.base;
    opens dto to javafx.base;
    opens config to org.hibernate.orm.core;

    exports app;
}
