package com.luggagestorage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.storage.enabled:true}")
    private boolean storageEnabled;

    @Value("${file.storage.path:./data}")
    private String storagePath;

    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {

        objectMapper = new ObjectMapper();

        // Register Hibernate5Module to handle lazy loading
        Hibernate5Module hibernateModule = new Hibernate5Module();
        hibernateModule.disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION);
        hibernateModule.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, false);
        objectMapper.registerModule(hibernateModule);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        if (storageEnabled) {
            try {
                createStorageDirectory();
                logger.info("File storage initialized at: {}", storagePath);
            } catch (IOException e) {
                logger.error("Failed to initialize file storage directory: {}", e.getMessage());
            }
        }
    }

    private void createStorageDirectory() throws IOException {
        Path path = Paths.get(storagePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            logger.info("Created storage directory: {}", storagePath);
        }
    }

    public <T> void saveToFile(List<T> data, String filename) throws IOException {
        if (!storageEnabled) {
            logger.debug("File storage is disabled, skipping save operation");
            return;
        }

        File file = new File(storagePath, filename);
        objectMapper.writeValue(file, data);
        logger.info("Saved {} items to file: {}", data.size(), file.getAbsolutePath());
    }

    public <T> void saveObjectToFile(T data, String filename) throws IOException {
        if (!storageEnabled) {
            logger.debug("File storage is disabled, skipping save operation");
            return;
        }

        File file = new File(storagePath, filename);
        objectMapper.writeValue(file, data);
        logger.info("Saved object to file: {}", file.getAbsolutePath());
    }

    public <T> List<T> loadFromFile(String filename, Class<T> valueType) throws IOException, FileNotFoundException {
        if (!storageEnabled) {
            logger.debug("File storage is disabled, returning empty list");
            return new ArrayList<>();
        }

        File file = new File(storagePath, filename);

        if (!file.exists()) {
            logger.warn("File not found: {}", file.getAbsolutePath());
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        List<T> data = objectMapper.readValue(file,
                objectMapper.getTypeFactory().constructCollectionType(List.class, valueType));
        logger.info("Loaded {} items from file: {}", data.size(), file.getAbsolutePath());
        return data;
    }

    public <T> T loadObjectFromFile(String filename, Class<T> valueType) throws IOException, FileNotFoundException {
        if (!storageEnabled) {
            logger.debug("File storage is disabled, returning null");
            return null;
        }

        File file = new File(storagePath, filename);

        if (!file.exists()) {
            logger.warn("File not found: {}", file.getAbsolutePath());
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        T data = objectMapper.readValue(file, valueType);
        logger.info("Loaded object from file: {}", file.getAbsolutePath());
        return data;
    }

    public boolean fileExists(String filename) {
        File file = new File(storagePath, filename);
        return file.exists();
    }

    public boolean deleteFile(String filename) throws IOException {
        File file = new File(storagePath, filename);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                logger.info("Deleted file: {}", file.getAbsolutePath());
            }
            return deleted;
        }
        return false;
    }

    public String getFilePath(String filename) {
        return new File(storagePath, filename).getAbsolutePath();
    }

    public List<String> listFiles() {
        List<String> files = new ArrayList<>();
        File directory = new File(storagePath);
        if (directory.exists() && directory.isDirectory()) {
            File[] fileArray = directory.listFiles();
            if (fileArray != null) {
                for (File file : fileArray) {
                    if (file.isFile()) {
                        files.add(file.getName());
                    }
                }
            }
        }
        return files;
    }

    public boolean isStorageEnabled() {
        return storageEnabled;
    }

    public String getStoragePath() {
        return storagePath;
    }
}
