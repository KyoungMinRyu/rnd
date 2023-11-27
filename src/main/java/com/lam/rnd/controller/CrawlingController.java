package com.lam.rnd.controller;

import com.lam.rnd.util.JavaMail;
import com.lam.rnd.util.JsonParsing;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
public class CrawlingController
{
    private final String path = "src/main/resources/rnd/";

    private final LocalDateTime localDateTime = LocalDateTime.now();

    @GetMapping("/view/{date}")
    public String index(@PathVariable String date)
    {
        File file = new File(path + date + ".txt");
        if(file.exists())
        {
            return JsonParsing.readFile(path + date + ".txt");
        }
        else
        {
            return "해당일자에 파일이 존재하지 않습니다.";
        }
    }

    @GetMapping("/download/{date}")
    public ResponseEntity<FileSystemResource> fileDownload(@PathVariable String date)
    {
        if (date.indexOf(".") != -1 || date.indexOf("/") != -1)
        {
            return ResponseEntity.badRequest().build();
        }

        File file = new File(path + date + ".txt");

        if (file.exists())
        {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + date + ".txt");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new FileSystemResource(file));
        }
        else
        {
            // 파일이 존재하지 않을 경우 에러 응답
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/all")
    public ResponseEntity<FileSystemResource> allFileDownload()
    {
        try
        {
            JsonParsing.makeZip(path.substring(0, path.length() - 1), path + "../all.zip");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        File file = new File(path + "../all.zip");

        if (file.exists())
        {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "all.zip");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new FileSystemResource(file));
        }
        else
        {
            // 파일이 존재하지 않을 경우 에러 응답
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/mail/{email}/{date}")
    public String requestSendMailWithFile(@PathVariable String email, @PathVariable String date)
    {
        File file = new File(path + date + ".txt");

        if (date.indexOf(".") != -1 || date.indexOf("/") != -1)
        {
            return "허용되는 문자가 아닙니다.";
        }

        if (file.exists())
        {
            if(JavaMail.sendMailWithFile(email,date, date, file))
            {
                return "메일이 발송되었습니다.";
            }
            else
            {
                return "메일주소를 확인하세요 : " + email;
            }
        }
        else
        {
            return "해당 파일이 존재하지 않습니다.";
        }
    }

    @GetMapping("/mail/{email}/all")
    public String requestSendMailWithAllFile(@PathVariable String email)
    {
        try
        {
            JsonParsing.makeZip(path.substring(0, path.length() - 1), path + "../all.zip");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        File file = new File(path + "../all.zip");

        if (file.exists())
        {
            if(JavaMail.sendMailWithFile(email,"All", "All", file))
            {
                return "메일이 발송되었습니다.";
            }
            else
            {
                return "메일주소를 확인하세요 : " + email;
            }
        }
        else
        {
            return "해당 파일이 존재하지 않습니다.";
        }
    }
}