package dev.codestev.server.persistence.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "artist",
        indexes = {
                @Index(name = "idx_artist_name", columnList = "name"),
                @Index(name = "idx_artist_homepage", columnList = "homepage")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_artist_name", columnNames = {"name"})
        }
)
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String homepage;

    @OneToMany(mappedBy = "artist")
    @OrderBy("name ASC")
    private List<Model> models = new ArrayList<>();

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

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }

    public void addModel(Model model) {
        if (!models.contains(model)) {
            models.add(model);
            model.setArtist(this);
        }
    }

    public void removeModel(Model model) {
        if (models.remove(model)) {
            model.setArtist(null);
        }
    }

    // Equality by id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artist other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

}
