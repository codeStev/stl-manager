package dev.codestev.server.persistence.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "model_preview",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_model_preview_model_path", columnNames = {"model_id", "path"})
        }
)
public class ModelPreview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning side of the relation
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    // Relative path to the image (e.g., models/SomeModel/assets/previews/img1.png)
    @Column(name = "path", nullable = false, length = 500)
    private String path;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "mime", length = 100)
    private String mime;

    @Column(name = "position")
    private Integer position;

    public Long getId() {
        return id;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }


}
