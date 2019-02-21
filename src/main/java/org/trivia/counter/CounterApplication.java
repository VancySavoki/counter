package org.trivia.counter;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class CounterApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(CounterApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path files = Paths.get("files");
        Path counter = Paths.get("counter.txt");
        if(Files.notExists(files)) {
            Files.createDirectory(files);
        }
        if(!Files.isDirectory(files)) {
            throw new RuntimeException("./files/ must be a directory");
        }
        if(!Files.isWritable(files)) {
            throw new RuntimeException("./files/ must be a writable");
        }
        if(Files.notExists(counter)) {
            Files.createFile(counter);
        }


    }
}
