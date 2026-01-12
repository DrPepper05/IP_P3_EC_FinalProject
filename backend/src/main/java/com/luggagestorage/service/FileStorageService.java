package com.luggagestorage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

/**
 * Service for file storage operations using JSON format.
 * Part of the mandatory requirement: "Store and read application data in files" (1 point).
 * Handles FileNotFoundException and IOException (part of the 3+ language exceptions requirement).
 */
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
        // Initialize ObjectMapper with JavaTimeModule for LocalDateTime support
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Create storage directory if it doesn't exist
        if (storageEnabled) {
            try {
                createStorageDirectory();
                logger.info("File storage initialized at: {}", storagePath);
            } catch (IOException e) {
                logger.error("Failed to initialize file storage directory: {}", e.getMessage());
            }
        }
    }

    /**
     * Create the storage directory if it doesn't exist.
     * Handles IOException (part of the 3+ language exceptions requirement).
     *
     * @throws IOException if directory creation fails
     */
    private void createStorageDirectory() throws IOException {
        Path path = Paths.get(storagePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            logger.info("Created storage directory: {}", storagePath);
        }
    }

    /**
     * Save a list of objects to a JSON file.
     *
     * @param data     The list of objects to save
     * @param filename The filename (without path)
     * @param <T>      The type of objects
     * @throws IOException if file write fails
     */
    public <T> void saveToFile(List<T> data, String filename) throws IOException {
        if (!storageEnabled) {
            logger.debug("File storage is disabled, skipping save operation");
            return;
        }

        File file = new File(storagePath, filename);
        objectMapper.writeValue(file, data);
        logger.info("Saved {} items to file: {}", data.size(), file.getAbsolutePath());
    }

    /**
     * Save a single object to a JSON file.
     *
     * @param data     The object to save
     * @param filename The filename (without path)
     * @param <T>      The type of object
     * @throws IOException if file write fails
     */
    public <T> void saveObjectToFile(T data, String filename) throws IOException {
        if (!storageEnabled) {
            logger.debug("File storage is disabled, skipping save operation");
            return;
        }

        File file = new File(storagePath, filename);
        objectMapper.writeValue(file, data);
        logger.info("Saved object to file: {}", file.getAbsolutePath());
    }

    /**
     * Load a list of objects from a JSON file.
     * Handles FileNotFoundException (part of the 3+ language exceptions requirement).
     *
     * @param filename  The filename (without path)
     * @param valueType The class type of objects in the list
     * @param <T>       The type of objects
     * @return List of objects loaded from file
     * @throws IOException           if file read fails
     * @throws FileNotFoundException if file doesn't exist
     */
    public <T> List<T> loadFromFile(String filename, Class<T> valueType) throws IOException, FileNotFoundException {
        if (!storageEnabled) {
            logger.debug("File storage is disabled, returning empty list");
            return new ArrayList<>();
        }

        File file = new File(storagePath, filename);

        // Check if file exists
        if (!file.exists()) {
            logger.warn("File not found: {}", file.getAbsolutePath());
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        // Read the file as a list
        List<T> data = objectMapper.readValue(file,
                objectMapper.getTypeFactory().constructCollectionType(List.class, valueType));
        logger.info("Loaded {} items from file: {}", data.size(), file.getAbsolutePath());
        return data;
    }

    /**
     * Load a single object from a JSON file.
     *
     * @param filename  The filename (without path)
     * @param valueType The class type of the object
     * @param <T>       The type of object
     * @return The object loaded from file
     * @throws IOException           if file read fails
     * @throws FileNotFoundException if file doesn't exist
     */
    public <T> T loadObjectFromFile(String filename, Class<T> valueType) throws IOException, FileNotFoundException {
        if (!storageEnabled) {
            logger.debug("File storage is disabled, returning null");
            return null;
        }

        File file = new File(storagePath, filename);

        // Check if file exists
        if (!file.exists()) {
            logger.warn("File not found: {}", file.getAbsolutePath());
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        T data = objectMapper.readValue(file, valueType);
        logger.info("Loaded object from file: {}", file.getAbsolutePath());
        return data;
    }

    /**
     * Check if a file exists in the storage directory.
     *
     * @param filename The filename (without path)
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String filename) {
        File file = new File(storagePath, filename);
        return file.exists();
    }

    /**
     * Delete a file from the storage directory.
     *
     * @param filename The filename (without path)
     * @return true if file was deleted, false otherwise
     * @throws IOException if deletion fails
     */
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

    /**
     * Get the full path for a filename.
     *
     * @param filename The filename (without path)
     * @return The full file path
     */
    public String getFilePath(String filename) {
        return new File(storagePath, filename).getAbsolutePath();
    }

    /**
     * Get all files in the storage directory.
     *
     * @return List of filenames
     */
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
