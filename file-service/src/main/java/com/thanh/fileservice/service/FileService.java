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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {

    FileRepository fileRepository;
    FileMgmtRepository fileMgmtRepository;
    FileMgmtMapper fileMgmtMapper;

    public FileResponse uploadFile(MultipartFile file) throws IOException {

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
