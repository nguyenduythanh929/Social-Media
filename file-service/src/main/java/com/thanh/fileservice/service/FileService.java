package com.thanh.fileservice.service;

import com.thanh.fileservice.dto.FileInfo;
import com.thanh.fileservice.dto.response.FileData;
import com.thanh.fileservice.dto.response.FileResponse;
import com.thanh.fileservice.exception.AppException;
import com.thanh.fileservice.exception.ErrorCode;
import com.thanh.fileservice.mapper.FileMgmtMapper;
import com.thanh.fileservice.repository.FileMgmtRepository;
import com.thanh.fileservice.repository.FileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    // Uploads are served back from our own origin, so only allow image types:
    // an uploaded HTML/SVG file would otherwise run scripts as this site
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    FileRepository fileRepository;
    FileMgmtRepository fileMgmtRepository;
    FileMgmtMapper fileMgmtMapper;

    public FileResponse uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new AppException(ErrorCode.EMPTY_FILE);
        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType().toLowerCase()))
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);


        FileInfo fileInfo = fileRepository.store(file);

        var filemgmt = fileMgmtMapper.toFileMgmt(fileInfo);
        String userId = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        filemgmt.setOwnerId(userId);

        fileMgmtRepository.save(filemgmt);

        return FileResponse.builder()
                .originalFileName(file.getOriginalFilename())
                .url(fileInfo.getUrl())
                .build();
    }

    public FileData download(String fileName) throws IOException {
        var fileMgmt = fileMgmtRepository.findById(fileName)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        var resource = fileRepository.read(fileMgmt);
        return new FileData(fileMgmt.getContentType(), resource);
    }
}
