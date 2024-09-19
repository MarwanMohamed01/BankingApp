package com.javaguides.BankingApp.services;

import com.javaguides.BankingApp.user.User;
import com.javaguides.BankingApp.user.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Log4j2
@Service
public class kycService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Uploads KYC document and verifies the user.
     *
     * @param user the user uploading the document
     * @param file the document file to upload
     */
    public void uploadKycDocument(User user, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            log.error("Failed to upload KYC document: File is empty");
            throw new IllegalArgumentException("File is empty");
        }

        try {
            // Save the uploaded file
            String filePath = saveFile(file);

            // Mark the user as KYC verified
            user.setKycVerified(true);
            user.setKycDocumentPath(filePath);
            userRepository.save(user);

            log.info("KYC document uploaded successfully for user: {}. Document path: {}", user.getId(), filePath);
        } catch (IOException e) {
            log.error("Failed to upload KYC document for user: {}", user.getId(), e);
            throw e; // Re-throw the exception after logging
        }
    }

    /**
     * Saves the file to the server.
     *
     * @param file the file to save
     * @return the file path
     * @throws IOException if file saving fails
     */
    private String saveFile(MultipartFile file) throws IOException {
        Path directory = Paths.get("uploads");
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("Created directory for uploads: {}", directory.toAbsolutePath());
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = directory.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File saved successfully: {}", filePath.toAbsolutePath());
        return filePath.toString();
    }
}
