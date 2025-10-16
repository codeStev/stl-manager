
package dev.codestev.server.persistence;

import dev.codestev.server.persistence.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {

    // Basic model queries - load models with only basic associations
    @Query("SELECT DISTINCT m FROM Model m " +
            "LEFT JOIN FETCH m.artist " +
            "LEFT JOIN FETCH m.library " +
            "WHERE m.library.id = :libraryId")
    List<Model> findAllByLibrary_IdBasic(@Param("libraryId") Long libraryId);

    @Query("SELECT DISTINCT m FROM Model m " +
            "LEFT JOIN FETCH m.artist " +
            "LEFT JOIN FETCH m.library " +
            "WHERE m.library.id = :libraryId")
    List<Model> findAllWithArtistAndLibraryByLibrary_Id(@Param("libraryId") Long libraryId);

    @Query("SELECT m FROM Model m " +
            "LEFT JOIN FETCH m.artist " +
            "LEFT JOIN FETCH m.library " +
            "WHERE m.id = :id")
    Optional<Model> findByIdBasic(@Param("id") Long id);

    @Override
    @Query("SELECT DISTINCT m FROM Model m " +
            "LEFT JOIN FETCH m.artist " +
            "LEFT JOIN FETCH m.library " +
            "WHERE m.id = :id")
    Optional<Model> findById(@Param("id") Long id);

    @Query("SELECT DISTINCT m FROM Model m " +
            "LEFT JOIN FETCH m.artist " +
            "LEFT JOIN FETCH m.library " +
            "WHERE m.name = :name AND m.library = :library AND " +
            "(:artistId IS NULL AND m.artist IS NULL OR m.artist.id = :artistId)")
    Optional<Model> findByNameAndLibraryAndArtist_Id(@Param("name") String name, @Param("library") Library library, @Param("artistId") Long artistId);

    // Child entity queries - load specific child collections directly
    @Query("SELECT v FROM ModelVariant v " +
            "LEFT JOIN FETCH v.stlFiles " +
            "WHERE v.model.library.id = :libraryId")
    List<ModelVariant> findVariantsByLibraryId(@Param("libraryId") Long libraryId);

    @Query("SELECT v FROM ModelVariant v " +
            "LEFT JOIN FETCH v.stlFiles " +
            "WHERE v.model.id = :modelId")
    List<ModelVariant> findVariantsByModelId(@Param("modelId") Long modelId);

    @Query("SELECT p FROM ModelPreview p " +
            "WHERE p.model.library.id = :libraryId")
    List<ModelPreview> findPreviewsByLibraryId(@Param("libraryId") Long libraryId);

    @Query("SELECT p FROM ModelPreview p " +
            "WHERE p.model.id = :modelId")
    List<ModelPreview> findPreviewsByModelId(@Param("modelId") Long modelId);

    @Query("SELECT sf FROM StlFile sf " +
            "JOIN sf.models m " +
            "WHERE m.library.id = :libraryId")
    List<StlFile> findStlFilesByLibraryId(@Param("libraryId") Long libraryId);

    @Query("SELECT sf FROM StlFile sf " +
            "JOIN sf.models m " +
            "WHERE m.id = :modelId")
    List<StlFile> findStlFilesByModelId(@Param("modelId") Long modelId);
}