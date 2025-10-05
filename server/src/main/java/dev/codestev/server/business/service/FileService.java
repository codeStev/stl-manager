package dev.codestev.server.business.service;

import dev.codestev.server.business.mapping.StlFileMapper;
import dev.codestev.server.business.model.FileContent;
import dev.codestev.server.business.model.StlFileDetails;
import dev.codestev.server.persistence.StlFileRepository;
import dev.codestev.server.persistence.model.Library;
import dev.codestev.server.persistence.model.Model;
import dev.codestev.server.persistence.model.StlFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileService {

    private final StlFileRepository stlFileRepository;
    private final StlFileMapper stlFileMapper;


    public FileService(StlFileRepository stlFileRepository, StlFileMapper stlFileMapper) {
        this.stlFileRepository = stlFileRepository;
        this.stlFileMapper = stlFileMapper;
    }

    public StlFileDetails getStlFileDetails(long id){
        StlFile stlFile = stlFileRepository.findById(id).orElse(null);
        return stlFileMapper.toDetails(stlFile);
    }

    public FileContent getFileContent( long fileId) throws IOException {
        StlFile stlFile = stlFileRepository.findById(fileId).orElseThrow(() -> new IOException("File not found"));
        if (stlFile.getModels() == null || stlFile.getModels().isEmpty()) {
            throw new IOException("File has no associated model");
        }
        Model model = stlFile.getModels().stream().findFirst().orElseThrow(() -> new IOException("Model not found"));
        Library library = model.getLibrary();
        if (library == null) {
            throw new IOException("Library not found");
        }
        if (library.getPath() == null || stlFile.getStoragePath() == null) {
            throw new IOException("Invalid file path");
        }
        Path stlPath = Path.of(library.getPath() + "/" + stlFile.getStoragePath());

        String filename = stlPath.getFileName().toString();
        String contentType = Files.probeContentType(stlPath);
        if (contentType == null) {
            contentType = "model/stl";
        }
        long length = Files.size(stlPath);

        InputStreamResource content = new InputStreamResource(Files.newInputStream(stlPath));

        FileContent fileContent = new FileContent();
        fileContent.setContentType(contentType);
        fileContent.setFileLength(length);
        fileContent.setFilePath(String.valueOf(stlPath));
        fileContent.setFileName(filename);
        fileContent.setContent(content);

        return fileContent;
    }

}
