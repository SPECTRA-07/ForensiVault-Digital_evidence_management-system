package com.dems.evidence;

import com.dems.cases.CaseEntity;
import com.dems.enums.CaseStatus;
import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import com.dems.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Dedicated validation component responsible for verifying file sizes, MIME types, file extensions,
 * case eligibility, and evidence modification business rules.
 */
@Slf4j
@Component
public class EvidenceValidationService {

    private static final long MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024; // 100 MB

    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"));
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm"));
    private static final Set<String> AUDIO_EXTENSIONS = new HashSet<>(Arrays.asList("mp3", "wav", "aac", "flac", "ogg", "m4a"));
    private static final Set<String> PDF_EXTENSIONS = new HashSet<>(Arrays.asList("pdf"));
    private static final Set<String> ZIP_EXTENSIONS = new HashSet<>(Arrays.asList("zip", "7z", "tar", "gz", "rar"));
    private static final Set<String> DISK_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList("iso", "img", "dd", "vmdk", "e01", "raw", "bin"));

    public void validateFile(MultipartFile file, EvidenceType evidenceType) {
        if (file == null || file.isEmpty()) {
            log.warn("Validation failed: Uploaded file is empty");
            throw new BadRequestException("Uploaded evidence file cannot be empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            log.warn("Validation failed: File size [{}] exceeds limit of 100MB", file.getSize());
            throw new BadRequestException("File size exceeds the maximum upload limit of 100MB.");
        }

        String rawOriginalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(rawOriginalFilename);
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase(Locale.ROOT) : "";

        if (extension.isEmpty()) {
            log.warn("Validation failed: File [{}] has no extension", rawOriginalFilename);
            throw new BadRequestException("Uploaded file must have a valid file extension.");
        }

        validateExtensionAndMimeType(extension, contentType, evidenceType, rawOriginalFilename);
    }

    public void validateCaseEligibility(CaseEntity caseEntity) {
        if (caseEntity == null || !Boolean.TRUE.equals(caseEntity.getActive())) {
            log.warn("Validation failed: Case is null or inactive");
            throw new BadRequestException("Target case does not exist or is inactive.");
        }

        if (caseEntity.getStatus() == CaseStatus.CLOSED || caseEntity.getStatus() == CaseStatus.ARCHIVED) {
            log.warn("Validation failed: Cannot upload evidence to Case ID [{}] with status [{}]",
                    caseEntity.getId(), caseEntity.getStatus());
            throw new BadRequestException("Cannot upload evidence to a closed or archived case.");
        }
    }

    public void validateEvidenceModification(EvidenceEntity evidence) {
        if (evidence == null || !Boolean.TRUE.equals(evidence.getActive())) {
            throw new BadRequestException("Target evidence record does not exist or is inactive.");
        }

        if (evidence.getStatus() == EvidenceStatus.ARCHIVED) {
            log.warn("Validation failed: Attempted to modify archived Evidence ID [{}]", evidence.getId());
            throw new BadRequestException("Archived evidence records cannot be modified.");
        }
    }

    private void validateExtensionAndMimeType(String extension, String contentType, EvidenceType evidenceType, String filename) {
        boolean validExtension = false;
        boolean validMimeType = false;

        switch (evidenceType) {
            case IMAGE:
                validExtension = IMAGE_EXTENSIONS.contains(extension);
                validMimeType = contentType.startsWith("image/");
                break;
            case VIDEO:
                validExtension = VIDEO_EXTENSIONS.contains(extension);
                validMimeType = contentType.startsWith("video/");
                break;
            case AUDIO:
                validExtension = AUDIO_EXTENSIONS.contains(extension);
                validMimeType = contentType.startsWith("audio/");
                break;
            case PDF:
                validExtension = PDF_EXTENSIONS.contains(extension);
                validMimeType = contentType.contains("pdf");
                break;
            case ZIP:
                validExtension = ZIP_EXTENSIONS.contains(extension);
                validMimeType = contentType.contains("zip") || contentType.contains("compressed")
                        || contentType.contains("archive") || contentType.contains("octet-stream");
                break;
            case DISK_IMAGE:
                validExtension = DISK_IMAGE_EXTENSIONS.contains(extension);
                validMimeType = contentType.contains("octet-stream") || contentType.contains("disk")
                        || contentType.contains("image") || contentType.contains("raw")
                        || contentType.contains("x-iso9660-image");
                break;
            case OTHER:
                validExtension = true;
                validMimeType = true;
                break;
        }

        if (!validExtension) {
            log.warn("Validation failed: Extension [{}] is not supported for EvidenceType [{}]", extension, evidenceType);
            throw new BadRequestException("File extension '" + extension + "' does not match declared EvidenceType '" + evidenceType + "'.");
        }

        if (!validMimeType) {
            log.warn("Validation failed: MIME type [{}] is not compatible with EvidenceType [{}]", contentType, evidenceType);
            throw new BadRequestException("File MIME type '" + contentType + "' is not compatible with declared EvidenceType '" + evidenceType + "'.");
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
