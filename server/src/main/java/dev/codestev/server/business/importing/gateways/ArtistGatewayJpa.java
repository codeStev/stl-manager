package dev.codestev.server.business.importing.gateways;

import dev.codestev.server.business.importing.ModelImportServiceImpl;
import dev.codestev.server.persistence.ArtistRepository;
import dev.codestev.server.persistence.model.Artist;
import org.springframework.stereotype.Component;

@Component
public class ArtistGatewayJpa implements ModelImportServiceImpl.ArtistGateway {

    private final ArtistRepository artistRepository;

    ArtistGatewayJpa(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    public Long getOrCreateArtist(String name) {
        Artist byName = artistRepository.findByName(name).orElse(null);
        if (byName == null){
            Artist artist = new Artist();
            artist.setName(name);
            Artist save = artistRepository.save(artist);
            return save.getId();
        }
        return byName.getId();
    }

    @Override
    public void deleteArtistIfOrphan(long artistId) {
        Artist byName = artistRepository.findById(artistId).orElse(null);
        if (byName != null && byName.getModels().isEmpty()) {
            artistRepository.deleteById(artistId);
        }
    }
}