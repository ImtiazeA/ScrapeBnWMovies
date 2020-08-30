import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Movie {
    private String movieName;
    private String genre;
    private String year;
    private String directedBy;
    private String casts;
    private String posterUrl;
    private String thumbnailGifUrl;
    private String firstVideoUrl;
    private String secondVideoUrl;
}
