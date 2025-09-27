package com.smartretail.backend.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class FileServiceImpl implements FileService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;

    @Autowired
    public FileServiceImpl(GridFsTemplate gridFsTemplate, GridFSBucket gridFSBucket) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFSBucket = gridFSBucket;
    }

    @Override
    public String uploadImage(MultipartFile file, String filename) throws IOException {
        GridFSUploadOptions options = new GridFSUploadOptions()
                .metadata(new Document("filename", filename)
                        .append("contentType", file.getContentType()));

        ObjectId id = gridFsTemplate.store(file.getInputStream(), filename, file.getContentType(), options.getMetadata());
        System.out.println("[SERVICE] Image uploaded to GridFS with ID: " + id.toHexString());
        return id.toHexString();
    }

    @Override
    public byte[] getImage(String imageId) {
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(imageId))));
        if (gridFSFile == null) {
            throw new RuntimeException("Image not found: " + imageId);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            gridFSBucket.downloadToStream(gridFSFile.getObjectId(), outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download image", e);
        }
    }
}