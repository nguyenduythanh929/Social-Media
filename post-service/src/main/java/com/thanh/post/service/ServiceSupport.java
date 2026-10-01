package com.thanh.post.service;

import com.thanh.post.exception.AppException;
import com.thanh.post.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

// Small helpers shared by PostService and CommentService
final class ServiceSupport {
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_IMAGES = 4;
    // file-service names stored files <uuid>.<extension>
    private static final Pattern GENERATED_FILE_NAME = Pattern.compile("[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)?");

    private ServiceSupport() {}

    static String getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Objects.requireNonNull(authentication).getName();
    }

    static Pageable toPageable(int page, int size, Sort sort){
        int safePage = Math.max(page, 1);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);

        return PageRequest.of(safePage - 1, safeSize, sort);
    }

    static String requireContent(String content, int maxLength){
        if (content == null || content.isBlank()) throw new AppException(ErrorCode.CONTENT_REQUIRED);

        String trimmed = content.trim();
        if (trimmed.length() > maxLength) throw new AppException(ErrorCode.CONTENT_TOO_LONG);

        return trimmed;
    }

    // Like requireContent, but blank is allowed (returned as "") when the post has images
    static String optionalContent(String content, int maxLength){
        if (content == null || content.isBlank()) return "";

        return requireContent(content, maxLength);
    }

    // Only accept images that were uploaded to our own file-service (exact download prefix + generated
    // file name), never arbitrary external URLs such as tracking pixels
    static List<String> validateMediaUrls(List<String> mediaUrls, String downloadPrefix){
        if (mediaUrls == null || mediaUrls.isEmpty()) return List.of();
        if (mediaUrls.size() > MAX_IMAGES) throw new AppException(ErrorCode.TOO_MANY_IMAGES);

        for (String url : mediaUrls) {
            boolean valid = url != null
                    && url.startsWith(downloadPrefix)
                    && GENERATED_FILE_NAME.matcher(url.substring(downloadPrefix.length())).matches();

            if (!valid) throw new AppException(ErrorCode.INVALID_MEDIA_URL);
        }

        return List.copyOf(mediaUrls);
    }
}
