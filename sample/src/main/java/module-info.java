module org.openjfx {
    requires javafx.controls;
	requires javafx.graphics;
    requires com.fasterxml.jackson.databind; 

    opens org.openjfx to com.fasterxml.jackson.databind; 
    exports org.openjfx;
    
}
