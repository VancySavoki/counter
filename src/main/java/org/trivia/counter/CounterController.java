package org.trivia.counter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@CrossOrigin
@RestController("counterController")
public class CounterController {
    @Autowired
    CounterServiceImpl counterService;

    private Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/counter")
    public Result<Integer> get() {
        return Result.<Integer>builder()
                .data(counterService.get())
                .msg("ok")
                .status("200")
                .build();
    }
    @PostMapping("/counter")
    public Result<Integer> incrementAndGet() {
        return Result.<Integer>builder()
                .data(counterService.incrementAndGet())
                .msg("ok")
                .status("200")
                .build();
    }
    @PostMapping("/uploadFiles")
    public Result<Integer> uploadFiles(@RequestParam(value = "file") List<MultipartFile> files) {
        if(files == null || files.isEmpty()) {
            return Result.<Integer>builder().error("Cannot post empty request")
                                    .msg("error")
                                    .status("400")
                                    .build();
        }
        Result result = new Result();
        result.msg = "ok";
        result.status = "200";
        List<String> failedFile = new ArrayList<>();
        for (int i = 0; i < files.size() ; i++) {
            MultipartFile file = files.get(i);
            if(file == null) {
                continue;
            }
            if (file.isEmpty()) {
                log.info("Skip empty files {}", file.getOriginalFilename());
            } else {
                try {
                    saveTo(file);
                } catch (IOException e) {
                    failedFile.add(file.getOriginalFilename());
                    log.error(e.getMessage(), e);
                }

            }

        }
        if(!failedFile.isEmpty()) {
            result.status = "500";
            result.msg = "File " + failedFile.toString() + " upload failed";
            result.error = "Parted files upload success";
        }
        return result;


    }

    private void saveTo(MultipartFile multipartFile) throws IOException {
        multipartFile.transferTo(Paths.get("files/" + multipartFile.getOriginalFilename()));
    }
}
