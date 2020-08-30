import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ScrapeBnWMovies {


    private Document document;
    private CSVPrinter printer;


    public ScrapeBnWMovies() throws IOException {
        Path file = Paths.get("movie-list.csv");
        BufferedWriter bufferedWriter = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        printer = new CSVPrinter(bufferedWriter,
                CSVFormat.EXCEL.withHeader("Movie Name", "Genre", "Year", "Directed By", "Cast", "Poster",
                        "Thumbnail", "Video Link 1", "Video Link 2")
        );
    }

    public void start() throws IOException {

        Elements genreDivs = scrapeGenre();

        for (Element genreDiv : genreDivs) {
            Elements movieElements = scrapeMovieListByGenre(genreDiv);

            for (Element movieElement : movieElements) {
                Movie movie = scrapeMovie(movieElement.select("a").attr("href"));

                printer.printRecord(
                        movie.getMovieName(),
                        movie.getGenre(),
                        movie.getYear(),
                        movie.getDirectedBy(),
                        movie.getCasts(),
                        movie.getPosterUrl(),
                        movie.getThumbnailGifUrl(),
                        movie.getFirstVideoUrl(),
                        movie.getSecondVideoUrl()
                );
            }

            printer.flush();
        }

    }

    private Elements scrapeGenre() throws IOException {
        document = Jsoup.connect("http://www.bnwmovies.com/").get();
        Elements genreDivs = document.select(".genreicon");

        return genreDivs;
    }

    private Elements scrapeMovieListByGenre(Element genre) throws IOException {

        System.out.println("Scraping --- --- --- " + genre.select("a").text());

        int pageNumber = 0;
        Elements movieList = new Elements();
        do {
            pageNumber++;
            String genrePageUrl = "http://www.bnwmovies.com/" + genre.select("a").attr("href") + "/page/" + pageNumber;

            document = Jsoup.connect(genrePageUrl).get();
            movieList.addAll(document.select(".cattombstone"));

        } while (document.select("a.nextpostslink").size() > 0);

        return movieList;
    }

    private Movie scrapeMovie(String movieLink) throws IOException {

        Document moviePage = Jsoup.connect(movieLink).get();

        String movieName = moviePage.select(".rating_info").text();
        String genre = moviePage.select(".more-about-text li:nth-child(3)").text().replaceAll("Genres: ", "");
        String year = movieName.substring(movieName.length() - 5, movieName.length() - 1);
        String directedBy = moviePage.select(".more-about-text li:nth-child(4)").text().replaceAll("Directed by: ", "");

        Elements castsElements = moviePage.select("div[class$='more-about-movie']:not([itemtype]) li");
        List<String> castList = new ArrayList<>();
        for (Element cast : castsElements) {
            castList.add(cast.text());
        }
        String casts = String.join(", ", castList);

        String posterUrl = moviePage.select(".pinterest-sharing img").attr("src");
        String thumbnailGifUrl = moviePage.select(".video-holder-abs:first-child > video").attr("poster");
        String firstVideoUrl = moviePage.select(".video-holder-abs:first-child > video > source:nth-child(1)").attr("src");
        String secondVideoUrl = moviePage.select(".video-holder-abs:first-child > video > source:nth-child(2)").attr("src");

        return new Movie(movieName, genre, year, directedBy, casts, posterUrl, thumbnailGifUrl, firstVideoUrl, secondVideoUrl);
    }

}
