/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.http.MediaType;

@Controller
@SpringBootApplication
public class Main {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

    @RequestMapping("/")
    String index() {
        return "index";
    }

    @RequestMapping("/db")
    String db(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
            ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

            ArrayList<String> output = new ArrayList<String>();
            while (rs.next()) {
                output.add("Read from DB: " + rs.getTimestamp("tick"));
            }

            model.put("records", output);
            return "db";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        if (dbUrl == null || dbUrl.isEmpty()) {
            return new HikariDataSource();
        } else {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            return new HikariDataSource(config);
        }
    }

  
  @RequestMapping(value = "/test")
    public String testConnection(Map<String, Object> model) {

        System.out.println("STARTED");
//        String bashScript = getClass().getClassLoader().getResource("bashscript.sh").getPath();
//      System.out.println("####### running script: " + bashScript);
      String[] cmd = {
              "/bin/sh",
              "-c",
              "mkdir aaa ; cd aaa ; mkdir bbb ; cd bbb"
      };

        execute(cmd);
//        execute("cd test");
//        execute("mkdir test2");
//        execute("cd test2");
//        execute("mkdir test3");
        model.put("success", true);
        return "bash";
    }

    @RequestMapping(value = "/test2")
    public String testConnection2(Map<String, Object> model) throws IOException {

        System.out.println("STARTED");
        test();
        //String bashScript = this.getClass().getClassLoader().getResource("bashscript.sh").getPath();
        //execute(bashScript);
        //model.put("success", true);
        return "bash";
    }

    private void execute(String[] command) {
        try {
            final Process p = Runtime.getRuntime().exec(command);

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;

            while ((line = input.readLine()) != null) {
//                LOGGER.debug(line);
                if ("script_finished".equals(line)) {
                    System.out.println("script finished successfully");
                }
            }
            p.waitFor();
            System.out.println("script finished");

        } catch (Exception e) {
            System.out.println("executing script sh " + e);
        }
    }

    private void test() throws IOException {
        String resource = "/com/example/bashscript.sh";
        ProcessBuilder pb = new ProcessBuilder(resource);

        File workingFolder = new File(resource);
        System.out.println("Exists: " + workingFolder.exists());
        System.out.println("Can read: " + workingFolder.canRead());
        System.out.println("Can execute: " + workingFolder.canExecute());
        pb.directory(workingFolder);

        File file = null;

        URL res = getClass().getResource(resource);
        if (res.toString().startsWith("jar:")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file = File.createTempFile("tempfile", ".tmp");
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                file.deleteOnExit();
            } catch (IOException ex) {

            }
        } else {
            //this will probably work in your IDE, but not from a JAR
            file = new File(res.getFile());
        }

        if (file != null && !file.exists()) {
            throw new RuntimeException("Error: File " + file + " not found!");
        }
    }

}
