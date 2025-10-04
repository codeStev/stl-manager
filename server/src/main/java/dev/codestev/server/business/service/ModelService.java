package dev.codestev.server.business.service;

import dev.codestev.server.business.mapping.ModelMapper;
import dev.codestev.server.business.model.ModelDetailed;
import dev.codestev.server.persistence.ModelRepository;
import dev.codestev.server.persistence.model.Model;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelService {

    private final ModelRepository modelRepository;
    private final ModelMapper modelMapper;

    public ModelService(ModelRepository modelRepository, ModelMapper modelMapper) {
        this.modelRepository = modelRepository;
        this.modelMapper = modelMapper;
    }

    List<ModelDetailed> getAllModelDetailsByLibraryId(long libraryId){
        List<Model> allModels = modelRepository.findAllByLibrary_Id(libraryId);
        List<ModelDetailed> modelDetailsBusiness = allModels.stream().map(modelMapper::toBusiness).toList();
        return modelDetailsBusiness;

    }


}
