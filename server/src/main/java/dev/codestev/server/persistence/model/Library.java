package dev.codestev.server.persistence.model;

import jakarta.persistence.*;

import java.util.Collection;

@Entity
@Table(
        name = "library",
        uniqueConstraints = @UniqueConstraint(name = "uk_library_name", columnNames = "name")
)
public class Library {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL)
    @OrderBy("name ASC")
    private Collection<Model> models;
    @Column(nullable = false, length = 2000)
    private String path;

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

    public Collection<Model> getModels() {
        return models;
    }

    public void setModels(Collection<Model> models) {
        this.models = models;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void addModel(Model model) {
        models.add(model);
        model.setLibrary(this);
    }

    public void removeModel(Model model) {
        models.remove(model);
        model.setLibrary(null);
    }

}
