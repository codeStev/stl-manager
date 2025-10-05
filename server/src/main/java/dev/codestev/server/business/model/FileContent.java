package dev.codestev.server.business.model;

import org.springframework.core.io.InputStreamResource;

public class FileContent {

    private String fileName;
    private String filePath;
    private String contentType;
    private InputStreamResource content;
    private long fileLength;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public InputStreamResource getContent() {
        return content;
    }

    public void setContent(InputStreamResource content) {
        this.content = content;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }
}
