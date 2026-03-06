package com.example.weaver.services;

import com.cloudinary.utils.ObjectUtils;
import com.example.weaver.configs.CloudinaryConfig;
import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.NotFoundException;
import com.example.weaver.models.Attachment;
import com.example.weaver.models.Task;
import com.example.weaver.models.User;
import com.example.weaver.repositories.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final CloudinaryConfig cloudinaryConfig;

    private final AttachmentRepository attachmentRepository;

    public List<Attachment> getFiles(Long taskId) {
        return attachmentRepository.findAttachmentsByTask_Id(taskId);
    }

    public Attachment upload(Task task, User user, MultipartFile file) {
        try {
            String mimeType = file.getContentType();
            if (mimeType == null || mimeType.startsWith("video/")) {
                throw new RuntimeException("File must be an image");
            }
            Map data = this.cloudinaryConfig.cloudinary().uploader().uploadLarge(file.getBytes(), ObjectUtils.asMap(
                    "folder", task.getId().toString(),
                    "resource_type", "image",
                    "chunk_size", 6000000));

            Attachment attachment = Attachment.builder()
                    .fileName(data.get("display_name").toString())
                    .fileUrl(data.get("url").toString())
                    .fileSize(Long.valueOf(data.get("bytes").toString()))
                    .mimeType(mimeType)
                    .task(task)
                    .user(user)
                    .build();
            return attachmentRepository.save(attachment);
        } catch (MaxUploadSizeExceededException | IOException io) {
            throw new RuntimeException(io.getMessage());
        }
    }

    public void delete(UUID id, Long taskId) {
        try {
            Attachment attachment = attachmentRepository.findById(id).orElseThrow(() -> new NotFoundException("File not found"));
            String publicId = taskId.toString() + "/" + attachment.getFileName();
            this.cloudinaryConfig.cloudinary().uploader().destroy(publicId, ObjectUtils.asMap());
            attachmentRepository.delete(attachment);
        } catch (IOException io) {
            throw new RuntimeException(io.getMessage());
        }

    }
}
