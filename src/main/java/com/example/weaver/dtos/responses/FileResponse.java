package com.example.weaver.dtos.responses;

import com.example.weaver.models.Attachment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    private UUID id;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String mimeType;

    private Instant uploadedAt;

    private Long taskId;

    private UUID uploadedBy;

    public static FileResponse toResponse(Attachment attachment) {
        return new FileResponse(attachment.getId(),
                attachment.getFileName(),
                attachment.getFileUrl(),
                attachment.getFileSize(),
                attachment.getMimeType(),
                attachment.getCreatedAt(),
                attachment.getTask().getId(),
                attachment.getUser().getId());
    }
}
