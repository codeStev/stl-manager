package dev.codestev.server.business.service;

import dev.codestev.server.business.mapping.StlFileMapper;
import dev.codestev.server.business.model.StlFileDetails;
import dev.codestev.server.persistence.StlFileRepository;
import dev.codestev.server.persistence.model.StlFile;
import org.springframework.stereotype.Service;

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

}
