package com.mca.pkms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PkmsApplication {
    public static void main(String[] args) {
        System.setProperty("pdfbox.fontcache",
                System.getProperty("pdfbox.fontcache", System.getProperty("java.io.tmpdir")));
        SpringApplication.run(PkmsApplication.class, args);
    }
}
