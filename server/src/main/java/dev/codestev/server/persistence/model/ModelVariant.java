package dev.codestev.server.persistence.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "model_variant",
uniqueConstraints = @jakarta.persistence.UniqueConstraint(name = "uk_model_variant_name", columnNames = {"model_id","name"}))
public class ModelVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @ManyToMany
    @JoinTable(
            name = "model_variant_stl_files",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "stl_file_id")
    )
    @OrderBy("fileName ASC")
    private Set<StlFile> stlFiles = new LinkedHashSet<>();

    @Column(nullable = false)
    private boolean inheritBaseFiles = true;

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

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
        model.addVariant(this);
    }

    public Set<StlFile> getStlFiles() {
        return stlFiles;
    }

    public void setStlFiles(Set<StlFile> stlFiles) {
        this.stlFiles = stlFiles;
    }

    public boolean isInheritBaseFiles() {
        return inheritBaseFiles;
    }

    public void setInheritBaseFiles(boolean inheritBaseFiles) {
        this.inheritBaseFiles = inheritBaseFiles;
    }

    @Transient
    public Set<StlFile> getEffectiveFiles() {
        if (!inheritBaseFiles) return stlFiles;
        Set<StlFile> result = new LinkedHashSet<>(model.getStlFiles());
        result.addAll(stlFiles);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelVariant other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    public void addStlFile(StlFile file) {
        if (file != null && !stlFiles.contains(file) && model.getStlFiles().contains(file)){
            stlFiles.add(file);
        }
    }
}
