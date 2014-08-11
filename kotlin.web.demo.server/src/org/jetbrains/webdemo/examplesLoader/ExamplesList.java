/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.webdemo.examplesLoader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.server.ApplicationSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExamplesList {
    private static final ExamplesList EXAMPLES_LIST = new ExamplesList();

    private static StringBuilder response;
    private static ObjectMapper objectMapper;
    private static Map<String, ExamplesFolder> examplesFolders;

    private ExamplesList() {
        response = new StringBuilder();
        examplesFolders = new HashMap<>();
        objectMapper = new ObjectMapper();
        generateList();
    }

    public static ExamplesList getInstance() {
        return EXAMPLES_LIST;
    }

    public static String updateList() {
        response = new StringBuilder();
        examplesFolders = new HashMap<>();
        ExamplesList.getInstance().generateList();
        return response.toString();
    }

    public static String loadExample(String url){
        url = url.replaceAll("_", " ");
        String folderName = ResponseUtils.getExampleFolderByUrl(url);
        String exampleName = ResponseUtils.getExampleOrProgramNameByUrl(url);

        ExamplesFolder folder = examplesFolders.get(folderName);
        ExampleObject example = folder.examples.get(exampleName);
        ObjectNode responce = new ObjectNode(JsonNodeFactory.instance);

        responce.put("help", example.help);
        responce.put("files", objectMapper.valueToTree(example.files));
        return responce.toString();
    }

    public Collection<ExamplesFolder> getList() {
        return examplesFolders.values();
    }

    public String getListAsString() {
        try {
            return objectMapper.writeValueAsString(examplesFolders.values());
        } catch (IOException e) {
            return "";
        }
    }

    private void generateList() {
        File order = new File(ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + "order");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(order));
            while (reader.ready()) {
                String folderName = reader.readLine();
                File manifest = new File(ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + folderName + File.separator + "manifest.json");
                try {
                    ExamplesFolder examplesFolder = objectMapper.readValue(manifest, ExamplesFolder.class);
                    examplesFolders.put(folderName, examplesFolder);
                } catch (Exception e) {
                    System.err.println("Can't load folder " + folderName + ":\n" + e.getMessage());
                    response.append("Can't load folder " + folderName + ":\n" + e.getMessage());
                }

            }
        } catch (IOException e) {
            System.err.println("Can't read order:\n" + e.getMessage());
            response.append("Can't read order:\n" + e.getMessage());
        }
    }

}
