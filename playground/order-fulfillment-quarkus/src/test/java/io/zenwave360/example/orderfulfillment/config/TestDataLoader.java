package io.zenwave360.example.orderfulfillment.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TestDataLoader {

    private final List<Class<?>> jpaManagedTypes;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public TestDataLoader(List<Class<?>> jpaManagedTypes) {
        this.jpaManagedTypes = jpaManagedTypes;
    }

    public <T> List<T> loadCollectionTestDataAsObjects(Class<T> collectionClass) {
        var jsonList = loadCollectionTestDataAsJson(collectionClass);
        var objects = new ArrayList<T>();
        for (var json : jsonList) {
            objects.add(read(collectionClass, json));
        }
        return objects;
    }

    public List<String> loadCollectionTestDataAsJson(Class<?> collectionClass) {
        return readDirectoryFilesAsString("src/test/resources/data/jpa/" + collectionClass.getSimpleName());
    }

    protected List<String> readDirectoryFilesAsString(String directory) {
        var files = new File(directory).listFiles();
        if (files == null) {
            return List.of();
        }
        var contents = new ArrayList<String>();
        for (var file : files) {
            try {
                contents.add(Files.readString(file.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return contents;
    }

    public <T> T read(Class<T> type, String json) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading json test data for " + type.getName(), e);
        }
    }
}
