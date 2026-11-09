package com.tradesense.ingestion.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageManager {

    private final Path storageDirectory;

    public FileStorageManager(
            @Value("${tradesense.storage.directory:./data/trade-files}") String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public Path createStoredFile(String fileName) {
        Path storedFile = storageDirectory.resolve(UUID.randomUUID() + extensionOf(fileName)).normalize();
        if (!storedFile.startsWith(storageDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }
        return storedFile;
    }

    public void saveFile(MultipartFile file, Path storedFile) {
        try {
            Files.createDirectories(storageDirectory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, storedFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String absoluteLocation = storedFile.toString();
            if (absoluteLocation.length() > 500) {
                Files.deleteIfExists(storedFile);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Configured storage path exceeds the database column limit");
            }
        } catch (IOException exception) {
            deleteIfExists(storedFile);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store uploaded file", exception);
        }
    }

    public void deleteIfExists(Path storedFile) {
        try {
            Files.deleteIfExists(storedFile);
        } catch (IOException ignored) {
            // Preserve the original failure; cleanup can be retried operationally.
        }
    }

    private String extensionOf(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex < 0 ? "" : fileName.substring(extensionIndex);
    }
}

