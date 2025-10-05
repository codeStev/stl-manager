package dev.codestev.server.persistence.model;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(
        name = "model",
        uniqueConstraints = @UniqueConstraint(name = "uk_model_library_name", columnNames = {"library_id", "name"})
)

public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "model_stl_files",
            joinColumns = @JoinColumn(name = "model_id"),
            inverseJoinColumns = @JoinColumn(name = "stl_file_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_model_stl_file",
                    columnNames = { "model_id", "stl_file_id" }
            )
    )
    @OrderBy("fileName ASC")
    private Set<StlFile> stlFiles = new LinkedHashSet<>();


    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name ASC")
    private List<ModelVariant> variants = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = true) // set optional=false if every Model must have an Artist
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    @Column(name = "thumbnail_width")
    private Integer thumbnailWidth;

    @Column(name = "thumbnail_height")
    private Integer thumbnailHeight;

    @Column(name = "thumbnail_mime", length = 100)
    private String thumbnailMime;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, id ASC")
    private List<ModelPreview> previews = new ArrayList<>();



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }

    public List<ModelVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ModelVariant> variants) {
        this.variants = variants;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void addVariant(ModelVariant variant) {
        if (variants.contains(variant)) return;
        variants.add(variant);
        variant.setModel(this);
    }

    public void removeVariant(ModelVariant variant) {
        variants.remove(variant);
        variant.setModel(null);
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public Integer getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(Integer thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public Integer getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(Integer thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public String getThumbnailMime() {
        return thumbnailMime;
    }

    public void setThumbnailMime(String thumbnailMime) {
        this.thumbnailMime = thumbnailMime;
    }

    public List<ModelPreview> getPreviews() {
        return previews;
    }

    public void setPreviews(List<ModelPreview> previews) {
        this.previews = previews;
    }

    public void addPreview(ModelPreview preview) {
        previews.add(preview);
        preview.setModel(this);
    }

    public void removePreview(ModelPreview preview) {
        previews.remove(preview);
        preview.setModel(null);
    }

    public Set<StlFile> getStlFiles() {
        return stlFiles;
    }

    public void setStlFiles(Set<StlFile> stlFiles) {
        this.stlFiles = stlFiles;
    }

    // Convenience methods to maintain both sides
    public void addStlFile(StlFile file) {
        if (stlFiles.add(file)) {
            file.getModels().add(this);
        }
    }

    public void removeStlFile(StlFile file) {
        if (stlFiles.remove(file)) {
            file.getModels().remove(this);
        }
    }


    @Transient
    public Set<StlFile> getEffectiveFiles() {
        Set<StlFile> result = new HashSet<>(stlFiles);
        for (ModelVariant variant : variants) {
            result.addAll(variant.getEffectiveFiles());
        }
        return result;
    }

}
