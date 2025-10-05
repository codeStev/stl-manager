package dev.codestev.server.business.importing.gateways;

import dev.codestev.server.business.importing.ModelImportServiceImpl;
import dev.codestev.server.persistence.LibraryRepository;
import dev.codestev.server.persistence.model.Library;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class LibraryGatewayJpa implements ModelImportServiceImpl.LibraryGateway {

    private final LibraryRepository libraryRepository;

    LibraryGatewayJpa(LibraryRepository libraryRepository) {
        this.libraryRepository = libraryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Library> getAllLibraries() {
        return libraryRepository.findAll();
    }
}