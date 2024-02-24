module com.udacity.catpoint.security {
    requires java.desktop;
    requires miglayout.swing;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    requires org.slf4j;
    requires imageService;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    opens com.udacity.catpoint.data to com.google.gson;
}